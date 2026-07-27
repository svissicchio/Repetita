package edu.repetita.solvers.sr.rls;

import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.w3c.dom.*;

public class GraphML2DEFO {
    private static void printHelp() {
        System.out.println("Usage: GraphML2DEFO file.graphml output.graph");
        System.exit(1);
    }

    public static void main(String[] args) throws Exception {
        if (args.length < 2) printHelp();

        File xmlFile = new File(args[0]);
        DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
        Document doc = dBuilder.parse(xmlFile);
        doc.getDocumentElement().normalize();

        NodeList keyList = doc.getElementsByTagName("key");
        Map<String, String> nodeKeysMap = new HashMap<>();
        Map<String, String> edgeKeysMap = new HashMap<>();

        for (int i = 0; i < keyList.getLength(); i++) {
            Element el = (Element) keyList.item(i);
            String forAttr = el.getAttribute("for");
            String attrName = el.getAttribute("attr.name");
            String id = el.getAttribute("id");
            if ("node".equals(forAttr)) {
                nodeKeysMap.put(attrName, id);
            } else if ("edge".equals(forAttr)) {
                edgeKeysMap.put(attrName, id);
            }
        }

        String keyNodeLabel = nodeKeysMap.getOrDefault("label", "");
        String keyNodeLatitude = nodeKeysMap.getOrDefault("Latitude", "");
        String keyNodeLongitude = nodeKeysMap.getOrDefault("Longitude", "");

        String keyEdgeLinkLabel = edgeKeysMap.getOrDefault("LinkLabel", "");
        String keyEdgeLinkSpeedRaw = edgeKeysMap.getOrDefault("LinkSpeedRaw", "");

        NodeList graphList = doc.getElementsByTagName("graph");
        if (graphList.getLength() == 0) printHelp();
        Element graph = (Element) graphList.item(0);

        NodeList nodeList = graph.getElementsByTagName("node");
        int nNodes = nodeList.getLength();
        List<Element> nodes = new ArrayList<>();
        List<Map<String, String>> nodeData = new ArrayList<>();
        Map<String, Integer> id2index = new HashMap<>();

        for (int i = 0; i < nNodes; i++) {
            Element nodeEl = (Element) nodeList.item(i);
            nodes.add(nodeEl);
            String id = nodeEl.getAttribute("id");
            id2index.put(id, i);
            nodeData.add(dataSequence2Map(nodeEl));
        }

        NodeList edgeList = graph.getElementsByTagName("edge");
        int nEdges = edgeList.getLength();
        List<Map<String, String>> edgeData = new ArrayList<>();
        int[] edgesSrc = new int[nEdges];
        int[] edgesDest = new int[nEdges];

        for (int i = 0; i < nEdges; i++) {
            Element edgeEl = (Element) edgeList.item(i);
            edgeData.add(dataSequence2Map(edgeEl));
            edgesSrc[i] = id2index.get(edgeEl.getAttribute("source"));
            edgesDest[i] = id2index.get(edgeEl.getAttribute("target"));
        }

        int[] connectedNodes = generateConnected(nNodes, edgesSrc, edgesDest);
        if (connectedNodes.length < nNodes) {
            System.out.println("Removed " + (nNodes - connectedNodes.length) + " unconnected nodes");
        }

        Map<Integer, Integer> index2defo = new HashMap<>();
        for (int i = 0; i < connectedNodes.length; i++) {
            index2defo.put(connectedNodes[i], i);
        }

        double[] nodeLatitude = new double[nNodes];
        double[] nodeLongitude = new double[nNodes];
        boolean[] nodeHasPosition = new boolean[nNodes];

        for (int i : connectedNodes) {
            double[] pos = extractPosition(nodeData.get(i), keyNodeLongitude, keyNodeLatitude);
            if (pos != null) {
                nodeHasPosition[i] = true;
                nodeLongitude[i] = pos[0];
                nodeLatitude[i] = pos[1];
            }
        }

        double[] edgeBandwidth = new double[nEdges];
        boolean[] edgeHasBandwidth = new boolean[nEdges];
        int nEdgeHasBandwidth = 0;

        for (int j = 0; j < nEdges; j++) {
            Double bw = extractBandwidth(edgeData.get(j), keyEdgeLinkLabel, keyEdgeLinkSpeedRaw);
            if (bw != null) {
                nEdgeHasBandwidth++;
                edgeHasBandwidth[j] = true;
                edgeBandwidth[j] = bw;
            }
        }

        double maxDiscrepancy = 20.0;
        double maxBandwidth = 0.0;
        for (double bw : edgeBandwidth) maxBandwidth = Math.max(maxBandwidth, bw);

        for (int edge = 0; edge < nEdges; edge++) {
            if (edgeHasBandwidth[edge] && edgeBandwidth[edge] * maxDiscrepancy < maxBandwidth) {
                edgeBandwidth[edge] = maxBandwidth / maxDiscrepancy;
            }
        }

        if (nEdgeHasBandwidth == 0) {
            Arrays.fill(edgeBandwidth, 1e6);
        } else if (nEdgeHasBandwidth < nEdges) {
            double sumBw = 0.0;
            for (double bw : edgeBandwidth) sumBw += bw;
            double meanBw = sumBw / nEdgeHasBandwidth;
            for (int j = 0; j < nEdges; j++) {
                if (!edgeHasBandwidth[j]) edgeBandwidth[j] = meanBw;
            }
        }

        double[] edgeLag = new double[nEdges];
        boolean[] edgeHasLag = new boolean[nEdges];
        int nEdgesHasLag = 0;

        for (int j = 0; j < nEdges; j++) {
            int src = edgesSrc[j];
            int dest = edgesDest[j];
            if (nodeHasPosition[src] && nodeHasPosition[dest]) {
                nEdgesHasLag++;
                edgeHasLag[j] = true;
                edgeLag[j] = positions2Lag(nodeLongitude[src], nodeLatitude[src], nodeLongitude[dest], nodeLatitude[dest]);
            }
        }

        if (nEdgesHasLag == 0) {
            Arrays.fill(edgeLag, 10);
        } else if (nEdgesHasLag < nEdges) {
            double sumLag = 0.0;
            for (double lag : edgeLag) sumLag += lag;
            double meanLag = sumLag / nEdgesHasLag;
            for (int j = 0; j < nEdges; j++) {
                if (!edgeHasLag[j]) edgeLag[j] = meanLag;
            }
        }

        int[] edgeWeight = new int[nEdges];
        double maxCapa = 0.0;
        for (double bw : edgeBandwidth) maxCapa = Math.max(maxCapa, bw);
        for (int edge = 0; edge < nEdges; edge++) {
            edgeWeight[edge] = (int) (10 * maxCapa / edgeBandwidth[edge]);
        }

        try (PrintWriter outFile = new PrintWriter(new FileWriter(args[1]))) {
            outFile.println("NODES " + connectedNodes.length);
            outFile.println("label x y");

            for (int i : connectedNodes) {
                String label = nodeData.get(i).getOrDefault(keyNodeLabel, "node__" + i);
                String id = nodes.get(i).getAttribute("id");
                String finalLabel = (id + '_' + label).replace(' ', '_');
                outFile.println(finalLabel + " " + nodeLongitude[i] + " " + nodeLatitude[i]);
            }

            outFile.println();
            outFile.println("EDGES " + nEdges);
            outFile.println("label src dest weight bw delay");

            int edgeCounter = 0;
            for (int j = 0; j < nEdges; j++) {
                int src = edgesSrc[j];
                int dest = edgesDest[j];
                if (index2defo.containsKey(src) && index2defo.containsKey(dest)) {
                    outFile.println("edge_" + edgeCounter + " " + index2defo.get(src) + " " + index2defo.get(dest) + " " + edgeWeight[j] + " " + (int) edgeBandwidth[j] + " " + (int) edgeLag[j]);
                    edgeCounter++;
                    outFile.println("edge_" + edgeCounter + " " + index2defo.get(dest) + " " + index2defo.get(src) + " " + edgeWeight[j] + " " + (int) edgeBandwidth[j] + " " + (int) edgeLag[j]);
                    edgeCounter++;
                }
            }
        }
    }

    private static Map<String, String> dataSequence2Map(Element el) {
        Map<String, String> map = new HashMap<>();
        NodeList dataList = el.getElementsByTagName("data");
        for (int i = 0; i < dataList.getLength(); i++) {
            Element data = (Element) dataList.item(i);
            map.put(data.getAttribute("key"), data.getTextContent());
        }
        return map;
    }

    private static int[] generateConnected(int n, int[] src, int[] dest) {
        int[] represent = new int[n];
        for (int i = 0; i < n; i++) represent[i] = i;

        for (int edge = 0; edge < src.length; edge++) {
            int ra = find(represent, src[edge]);
            int rb = find(represent, dest[edge]);
            if (ra != rb) {
                int ma = Math.min(ra, rb);
                int mb = Math.max(ra, rb);
                represent[mb] = ma;
            }
        }

        int[] count = new int[n];
        for (int node = 0; node < n; node++) {
            count[find(represent, node)]++;
        }

        int bestRep = -1;
        int bestCount = 0;
        for (int node = 0; node < n; node++) {
            if (count[node] > bestCount) {
                bestCount = count[node];
                bestRep = node;
            }
        }

        List<Integer> res = new ArrayList<>();
        for (int node = 0; node < n; node++) {
            if (find(represent, node) == bestRep) {
                res.add(node);
            }
        }
        return res.stream().mapToInt(x -> x).toArray();
    }

    private static int find(int[] represent, int i) {
        if (represent[i] == i) return i;
        represent[i] = find(represent, represent[i]);
        return represent[i];
    }

    private static double[] extractPosition(Map<String, String> node, String keyLongitude, String keyLatitude) {
        if (node.containsKey(keyLongitude) && node.containsKey(keyLatitude)) {
            try {
                return new double[]{Double.parseDouble(node.get(keyLongitude)), Double.parseDouble(node.get(keyLatitude))};
            } catch (Exception e) {
                return null;
            }
        }
        return null;
    }

    private static Double extractBandwidth(Map<String, String> edge, String keyLinkLabel, String keyLinkSpeedRaw) {
        if (edge.containsKey(keyLinkSpeedRaw)) {
            return Double.parseDouble(edge.get(keyLinkSpeedRaw)) / 1e3;
        }
        if (!edge.containsKey(keyLinkLabel)) return null;

        String linkLabel = edge.get(keyLinkLabel);

        Pattern namedOC = Pattern.compile("OC-(\\d+).*");
        Pattern namedOCNoDash = Pattern.compile("OC(\\d+).*");
        Pattern namedSTM = Pattern.compile("STM-(\\d+).*");
        Pattern namedSTMMult = Pattern.compile("STM-(\\d+)x(\\d\\d?).*");

        Matcher m = namedOC.matcher(linkLabel);
        if (m.matches()) return Double.parseDouble(m.group(1)) * 51840;

        m = namedOCNoDash.matcher(linkLabel);
        if (m.matches()) return Double.parseDouble(m.group(1)) * 51840;

        m = namedSTMMult.matcher(linkLabel);
        if (m.matches()) return Double.parseDouble(m.group(1)) * 155520 * Double.parseDouble(m.group(2));

        m = namedSTM.matcher(linkLabel);
        if (m.matches()) return Double.parseDouble(m.group(1)) * 155520;

        if ("T1".equals(linkLabel)) return 1544.0;

        String clean1 = linkLabel.replaceAll("(.*)([MG])b/s(.*)", "$1$2bps$3")
                .replaceAll("(.*)([MG])bit/s(.*)", "$1$2bps$3")
                .replaceAll("(.*)([MG])B/s(.*)", "$1$2Bps$3");

        String clean2 = clean1.replaceAll("(.*?)(\\d+)-(\\d+)(.*)", "$1$3$4");
        String clean3 = clean2.replaceAll("<=?(.*)", "$1").replaceAll(">=?(.*)", "$1");

        Pattern mbps = Pattern.compile(".*?(\\d+(\\.\\d+)?)\\s*Mbps.*");
        Pattern gbps = Pattern.compile(".*?(\\d+(\\.\\d+)?)\\s*Gbps.*");
        Pattern gBps = Pattern.compile(".*?(\\d+(\\.\\d+)?)\\s*GBps.*");

        m = mbps.matcher(clean3);
        if (m.matches()) return Double.parseDouble(m.group(1)) * 1e3;

        m = gbps.matcher(clean3);
        if (m.matches()) return Double.parseDouble(m.group(1)) * 1e6;

        m = gBps.matcher(clean3);
        if (m.matches()) return Double.parseDouble(m.group(1)) * 1e6 * 8;

        return null;
    }

    private static double positions2Lag(double lng1, double lat1, double lng2, double lat2) {
        double degreeToRadian = Math.PI / 180.0;
        double lng1r = lng1 * degreeToRadian;
        double lat1r = lat1 * degreeToRadian;
        double lng2r = lng2 * degreeToRadian;
        double lat2r = lat2 * degreeToRadian;

        double lat = lat2r - lat1r;
        double lng = lng2r - lng1r;

        double sinlat = Math.sin(lat / 2);
        double sinlng = Math.sin(lng / 2);
        double d = sinlat * sinlat + Math.cos(lat1r) * Math.cos(lat2r) * (sinlng * sinlng);
        double angle = Math.asin(Math.sqrt(d));

        double delayPerRadian = 1e6 / (15 * Math.PI);
        return angle * delayPerRadian + 5;
    }
}

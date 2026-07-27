package edu.repetita.solvers.sr.defo.parsers;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class TopologyParser {
    public static TopologyData parse(String filePath) throws IOException {
        List<String> nodeLabels = new ArrayList<>();
        List<double[]> coordinates = new ArrayList<>();

        List<String> edgeLabels = new ArrayList<>();
        List<Integer> srcs = new ArrayList<>();
        List<Integer> dests = new ArrayList<>();
        List<Integer> weights = new ArrayList<>();
        List<Integer> capacities = new ArrayList<>();
        List<Integer> latencies = new ArrayList<>();

        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            br.readLine(); // Drop 1
            br.readLine(); // Drop 2

            String line;
            while ((line = br.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty()) break;
                String[] data = line.split("\\s+");
                if (data.length >= 3) {
                    nodeLabels.add(data[0]);
                    double x = Double.parseDouble(data[1]);
                    double y = Double.parseDouble(data[2]);
                    coordinates.add(new double[]{x, y});
                }
            }

            br.readLine(); // Drop edge line 1
            br.readLine(); // Drop edge line 2

            while ((line = br.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty()) break;
                String[] data = line.split("\\s+");
                if (data.length >= 6) {
                    edgeLabels.add(data[0]);
                    srcs.add(Integer.parseInt(data[1]));
                    dests.add(Integer.parseInt(data[2]));
                    weights.add(Integer.parseInt(data[3]));
                    capacities.add(Integer.parseInt(data[4]));
                    latencies.add((int) Math.round(Double.parseDouble(data[5])));
                }
            }
        }

        return new TopologyData(
                nodeLabels.toArray(new String[0]),
                coordinates.toArray(new double[0][]),
                edgeLabels.toArray(new String[0]),
                srcs.stream().mapToInt(x -> x).toArray(),
                dests.stream().mapToInt(x -> x).toArray(),
                weights.stream().mapToInt(x -> x).toArray(),
                capacities.stream().mapToInt(x -> x).toArray(),
                latencies.stream().mapToInt(x -> x).toArray()
        );
    }

    public static void saveAs(String filePath, TopologyData topologyData) {
        OutFile outFile = OutFile.apply(filePath);
        int nEdges = topologyData.edgeDests.length;
        int nNodes = topologyData.nodeLabels.length;

        outFile.writeln("NODES");
        outFile.writeln("label x y");

        for (int i = 0; i < nNodes; i++) {
            String label = topologyData.nodeLabels[i];
            double[] coords = topologyData.nodeCoordinates[i];
            outFile.writeln(label + " " + coords[0] + " " + coords[1]);
        }

        outFile.writeln();
        outFile.writeln("EDGES");
        outFile.writeln("label src dest weight bw delay");

        for (int i = 0; i < nEdges; i++) {
            String label = topologyData.edgeLabels[i];
            int src = topologyData.edgeSrcs[i];
            int dest = topologyData.edgeDests[i];
            int latency = topologyData.edgeLatencies[i];
            int weight = topologyData.edgeWeights[i];
            int bw = topologyData.edgeCapacities[i];
            outFile.writeln(label + " " + src + " " + dest + " " + weight + " " + bw + " " + latency);
        }

        outFile.close();
    }
}

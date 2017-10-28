package edu.repetita.io;

import edu.repetita.core.Demands;
import edu.repetita.core.Topology;
import edu.repetita.utils.datastructures.Conversions;
import scala.io.Source;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Utility class that contains static methods to parse topologies and demands.
 */
final public class RepetitaParser {

    private RepetitaParser() {}

    /**
     * Parses a topology file.
     *
     * @param filename the name of the file to parse
     * @return the topology contained in filename
     * @throws IOException if there is some problem when reading the file
     */
    public static Topology parseTopology(String filename) throws IOException {
        try (Stream<String> lineStream = Files.lines(Paths.get(filename))) {  // autoclose stream
            Iterator<String> lines = lineStream.iterator();

            // Nodes
            ArrayList<String> nodeLabels = new ArrayList<String>();

            // Edges
            ArrayList<String> edgeLabels = new ArrayList<String>();
            ArrayList<Integer> srcs = new ArrayList<Integer>();
            ArrayList<Integer> dests = new ArrayList<Integer>();
            ArrayList<Integer> weights = new ArrayList<Integer>();
            ArrayList<Double> capacities = new ArrayList<Double>();
            ArrayList<Integer> latencies = new ArrayList<Integer>();

            // First two lines of nodes are useless to parse
            lines.next();
            lines.next();

            // Node info: label x_coordinate y_coordinate
            while (lines.hasNext()) {
                String line = lines.next();
                if (line.isEmpty()) break; // stop at empty line

                String[] data = line.split(" ");
                String label = data[0];
                // double x = Double.parseDouble(data[1]);  // not using coordinates
                // double y = Double.parseDouble(data[2]);
                nodeLabels.add(label);
            }

            // First two lines of edges are useless to parse
            lines.next();
            lines.next();

            // Edge info: src dest weight bw delay
            while (lines.hasNext()) {
                String line = lines.next();
                if (line.isEmpty()) break;  // stop at empty line

                String[] data = line.split(" ");
                String label = data[0];
                int src = Integer.parseInt(data[1]);
                int dest = Integer.parseInt(data[2]);
                int weight = Integer.parseInt(data[3]);
                double bw = Double.parseDouble(data[4]);
                int delay = Integer.parseInt(data[5]);

                edgeLabels.add(label);
                srcs.add(src);
                dests.add(dest);
                weights.add(weight);
                capacities.add(bw);
                latencies.add(delay);
            }

            // make topology from parsed info
            String[] nodeLabel = nodeLabels.toArray(new String[0]);  // I hate doing this, Java.
            String[] edgeLabel = edgeLabels.toArray(new String[0]);
            int[] edgeSrc = Conversions.arrayListInteger2arrayint(srcs);
            int[] edgeDest = Conversions.arrayListInteger2arrayint(dests);
            int[] edgeWeight = Conversions.arrayListInteger2arrayint(weights);
            double[] edgeCapacity = Conversions.arrayListDouble2arraydouble(capacities);
            int[] edgeLatency = Conversions.arrayListInteger2arrayint(latencies);

            return new Topology(nodeLabel, edgeLabel, edgeSrc, edgeDest, edgeWeight, edgeCapacity, edgeLatency);
        } catch (Exception e) { // let it crash!
            throw e;
        }
    }

    /**
     * Parses a demands file.
     *
     * @param filename the file containing the topology
     * @return the demands contained in filename
     * @throws IOException if there is some problem when reading the file
     */
    public static Demands parseDemands(String filename) throws IOException {
        try (Stream<String> lineStream = Files.lines(Paths.get(filename))) {  // autoclose stream
            Iterator<String> lines = lineStream.iterator();

            // Demands
            ArrayList<String> labels = new ArrayList<String>();
            ArrayList<Integer> srcs = new ArrayList<Integer>();
            ArrayList<Integer> dests = new ArrayList<Integer>();
            ArrayList<Double> bws = new ArrayList<Double>();

            // First two lines are useless to parse
            lines.next();
            lines.next();

            // Demands info: label src dest bw
            while (lines.hasNext()) {
                String line = lines.next();
                if (line.isEmpty()) break;

                String[] data = line.split(" ");
                String label = data[0];
                int src = Integer.parseInt(data[1]);
                int dest = Integer.parseInt(data[2]);
                double bw = Double.parseDouble(data[3]);

                labels.add(label);
                srcs.add(src);
                dests.add(dest);
                bws.add(bw);
            }

            // make Demands from parsed info

            String label[] = labels.toArray(new String[0]);
            int[] source = Conversions.arrayListInteger2arrayint(srcs);
            int[] dest = Conversions.arrayListInteger2arrayint(dests);
            double[] amount = Conversions.arrayListDouble2arraydouble(bws);

            return new Demands(label, source, dest, amount);
        } catch (Exception e) { // let it crash
            throw e;
        }
    }

    public static Map<String,Map<String,String>> parseExternalSolverFeatures(String filename) {
        String path = ClassLoader.getSystemResource(filename).getPath();
        
        Map<String,Map<String,String>> solverFeatures = new HashMap<>();

        try (InputStream resource = ClassLoader.getSystemResourceAsStream(filename)) {
            List<String> lines =
                    new BufferedReader(new InputStreamReader(resource,
                            StandardCharsets.UTF_8)).lines().collect(Collectors.toList());
            System.out.println("))>"+lines);
            Map<String,String> currFeatures = new HashMap<>();

            for (String line: lines) {
                System.out.println(line);
                if (line.isEmpty() || line.startsWith("#")) continue;

                if (line.startsWith("[")){
                    if (! currFeatures.isEmpty()){
                        solverFeatures.put(currFeatures.get("name"),currFeatures);
                    }
                    currFeatures = new HashMap<>();
                    currFeatures.put(IOConstants.SOLVER_STARTDEF,line);
                    continue;
                }

                String[] featureArray = line.split(IOConstants.SOLVER_KEYVALUESEPARATOR);

                // The first element in the featureArray is the feature name.
                // The other elements constitute the feature value (they can be more than one if separated by '=')
                String featName = featureArray[0].trim();
                String value = featureArray[1];
                for (int i = 2; i < featureArray.length; i++) {
                    value = value.concat(IOConstants.SOLVER_KEYVALUESEPARATOR + featureArray[i]);
                }
                currFeatures.put(featName,value.trim());
            }
            // add last solver
            solverFeatures.put(currFeatures.get("name"),currFeatures);


        } catch (IOException e) {
            System.out.println(e);
        }


        /*
        try {
            Stream<String> lineStream = Files.lines(Paths.get(path));
            Iterator<String> lines = lineStream.iterator();

            Map<String,String> currFeatures = new HashMap<>();

            while (lines.hasNext()){
                String line = lines.next();
                if (line.isEmpty() || line.startsWith("#")) continue;

                if (line.startsWith("[")){
                    if (! currFeatures.isEmpty()){
                        solverFeatures.put(currFeatures.get("name"),currFeatures);
                    }
                    currFeatures = new HashMap<>();
                    currFeatures.put(IOConstants.SOLVER_STARTDEF,line);
                    continue;
                }

                String[] featureArray = line.split(IOConstants.SOLVER_KEYVALUESEPARATOR);

                // The first element in the featureArray is the feature name.
                // The other elements constitute the feature value (they can be more than one if separated by '=')
                String featName = featureArray[0].trim();
                String value = featureArray[1];
                for (int i = 2; i < featureArray.length; i++) {
                    value = value.concat(IOConstants.SOLVER_KEYVALUESEPARATOR + featureArray[i]);
                }
                currFeatures.put(featName,value.trim());
            }

            // add last solver
            solverFeatures.put(currFeatures.get("name"),currFeatures);
        }
        catch (Exception e) { // let it crash
            e.printStackTrace();
            System.err.println("Cannot parse external solver spec file " + filename);
            System.exit(-1);

        }*/

        return solverFeatures;
    }
}

package edu.repetita.solvers.sr.rls.io;

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
        List<Double> capacities = new ArrayList<>();
        List<Integer> latencies = new ArrayList<>();

        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            br.readLine(); // Drop line 1
            br.readLine(); // Drop line 2

            // Nodes
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

            br.readLine(); // Drop line 1 of edges
            br.readLine(); // Drop line 2 of edges

            // Edges
            while ((line = br.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty()) break;
                String[] data = line.split("\\s+");
                if (data.length >= 6) {
                    edgeLabels.add(data[0]);
                    srcs.add(Integer.parseInt(data[1]));
                    dests.add(Integer.parseInt(data[2]));
                    weights.add(Integer.parseInt(data[3]));
                    capacities.add(Double.parseDouble(data[4]));
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
                capacities.stream().mapToDouble(x -> x).toArray(),
                latencies.stream().mapToInt(x -> x).toArray()
        );
    }
}

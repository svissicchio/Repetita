package edu.repetita.solvers.sr.rls.io;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PathsParser {
    public static PathsData parse(String filepath, TopologyData topologyData, DemandsData demandsData) throws IOException {
        Map<String, Integer> nameToNode = new HashMap<>();
        for (int i = 0; i < topologyData.nodeLabels.length; i++) {
            nameToNode.put(topologyData.nodeLabels[i], i);
        }

        Map<String, Integer> nameToDemand = new HashMap<>();
        for (int i = 0; i < demandsData.nDemands; i++) {
            nameToDemand.put(demandsData.demandLabels[i], i);
        }

        double maxLoad = 0.0;
        Map<Integer, int[]> paths = new HashMap<>();
        List<Integer> demandsList = new ArrayList<>();

        try (BufferedReader br = new BufferedReader(new FileReader(filepath))) {
            String line = br.readLine();
            if (line == null || !line.trim().equals("MAXLOAD")) {
                System.out.println("ERROR: expected MAXLOAD section");
            }
            line = br.readLine();
            if (line != null) {
                maxLoad = Double.parseDouble(line.trim());
            }
            br.readLine(); // separator

            line = br.readLine();
            if (line == null || !line.trim().equals("PATHS")) {
                System.out.println("ERROR: expected PATHS section");
            }

            while ((line = br.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty()) break;

                String[] items = line.split("\\s+");
                int demand = nameToDemand.get(items[0]);
                int[] nodes = new int[items.length - 1];
                for (int i = 1; i < items.length; i++) {
                    nodes[i - 1] = nameToNode.get(items[i]);
                }

                paths.put(demand, nodes);
                demandsList.add(demand);
            }
        }

        return new PathsData(maxLoad, paths, demandsList.stream().mapToInt(x -> x).toArray());
    }
}

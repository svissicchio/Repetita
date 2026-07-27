package edu.repetita.solvers.sr.defo.parsers;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class DemandParser {
    public static DemandsData parse(String filepath) throws IOException {
        List<String> labels = new ArrayList<>();
        List<Integer> srcs = new ArrayList<>();
        List<Integer> dests = new ArrayList<>();
        List<Integer> bws = new ArrayList<>();

        try (BufferedReader br = new BufferedReader(new FileReader(filepath))) {
            br.readLine(); // Drop line 1
            br.readLine(); // Drop line 2

            String line;
            while ((line = br.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty()) break;

                String[] data = line.split("\\s+");
                if (data.length >= 4) {
                    labels.add(data[0]);
                    srcs.add(Integer.parseInt(data[1]));
                    dests.add(Integer.parseInt(data[2]));
                    bws.add(Integer.parseInt(data[3]));
                }
            }
        }

        return new DemandsData(
                labels.toArray(new String[0]),
                srcs.stream().mapToInt(x -> x).toArray(),
                dests.stream().mapToInt(x -> x).toArray(),
                bws.stream().mapToInt(x -> x).toArray()
        );
    }

    public static void saveAs(String filePath, DemandsData demandsData) {
        OutFile outFile = OutFile.apply(filePath);
        int nDemands = demandsData.demandLabels.length;

        outFile.writeln("DEMANDS");
        outFile.writeln("label src dest bw");

        for (int i = 0; i < nDemands; i++) {
            String label = demandsData.demandLabels[i];
            int src = demandsData.demandSrcs[i];
            int dest = demandsData.demandDests[i];
            int traffic = demandsData.demandTraffics[i];
            outFile.writeln(label + " " + src + " " + dest + " " + traffic);
        }

        outFile.close();
    }
}

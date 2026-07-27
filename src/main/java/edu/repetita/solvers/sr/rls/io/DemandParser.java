package edu.repetita.solvers.sr.rls.io;

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
        List<Double> bws = new ArrayList<>();

        try (BufferedReader br = new BufferedReader(new FileReader(filepath))) {
            br.readLine(); // Drop first line
            br.readLine(); // Drop second line

            String line;
            while ((line = br.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty()) break;

                String[] data = line.split("\\s+");
                if (data.length >= 4) {
                    labels.add(data[0]);
                    srcs.add(Integer.parseInt(data[1]));
                    dests.add(Integer.parseInt(data[2]));
                    bws.add(Double.parseDouble(data[3]));
                }
            }
        }

        return new DemandsData(
                labels.toArray(new String[0]),
                srcs.stream().mapToInt(x -> x).toArray(),
                dests.stream().mapToInt(x -> x).toArray(),
                bws.stream().mapToDouble(x -> x).toArray()
        );
    }
}

package edu.repetita.solvers.sr.defo.parsers;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class ConstraintParser {
    public static abstract class ParsedConstraint {}

    public static class ParsedPassThrough extends ParsedConstraint {
        public final String demand;
        public final String[][] sets;

        public ParsedPassThrough(String demand, String[][] sets) {
            this.demand = demand;
            this.sets = sets;
        }
    }

    public static ParsedConstraint[] parse(String filePath) throws IOException {
        List<ParsedConstraint> constraints = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            String line;
            while ((line = br.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty()) continue;
                String[] data = line.split("\\s+");
                if ("PassThrough".equals(data[0])) {
                    constraints.add(parsePassThrough(data));
                }
            }
        }
        return constraints.toArray(new ParsedConstraint[0]);
    }

    private static ParsedPassThrough parsePassThrough(String[] data) {
        String demandLabel = data[1];
        List<List<String>> setsList = new ArrayList<>();
        setsList.add(new ArrayList<>());

        for (int i = 2; i < data.length; i++) {
            String symb = data[i];
            if ("|".equals(symb)) {
                setsList.add(new ArrayList<>());
            } else {
                setsList.get(setsList.size() - 1).add(symb);
            }
        }

        String[][] sets = new String[setsList.size()][];
        for (int i = 0; i < setsList.size(); i++) {
            sets[i] = setsList.get(i).toArray(new String[0]);
        }

        return new ParsedPassThrough(demandLabel, sets);
    }
}

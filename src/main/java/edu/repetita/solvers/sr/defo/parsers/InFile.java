package edu.repetita.solvers.sr.defo.parsers;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class InFile {
    private final List<Integer> vals = new ArrayList<>();
    private int index = 0;

    public InFile(String filepath) throws IOException {
        StringBuilder sb = new StringBuilder();
        try (BufferedReader br = new BufferedReader(new FileReader(filepath))) {
            String line;
            while ((line = br.readLine()) != null) {
                sb.append(line).append(" ");
            }
        }
        String[] tokens = sb.toString().split("[ ,\\t]+");
        for (String token : tokens) {
            if (!token.isEmpty()) {
                vals.add(Integer.parseInt(token));
            }
        }
    }

    public int nextInt() {
        index++;
        return vals.get(index - 1);
    }
}

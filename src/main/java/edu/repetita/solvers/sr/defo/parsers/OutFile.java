package edu.repetita.solvers.sr.defo.parsers;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;

public class OutFile {
    private final BufferedWriter file;
    private final boolean critical;
    private final boolean verbose;

    public OutFile(String filepath, boolean critical, boolean verbose) {
        this.critical = critical;
        this.verbose = verbose;
        BufferedWriter f = null;
        try {
            f = new BufferedWriter(new FileWriter(filepath));
        } catch (IOException e) {
            errorHandling(e);
        }
        this.file = f;
    }

    public OutFile(String filepath) {
        this(filepath, true, true);
    }

    public static OutFile apply(String filepath, boolean critical, boolean verbose) {
        return new OutFile(filepath, critical, verbose);
    }

    public static OutFile apply(String filepath) {
        return new OutFile(filepath, true, true);
    }

    private void errorHandling(Exception e) {
        if (verbose) System.out.println(e.getMessage());
        if (critical) System.exit(-1);
    }

    public void write(String line) {
        try {
            if (file != null) file.write(line);
        } catch (IOException e) {
            errorHandling(e);
        }
    }

    public void write(int line) {
        write(String.valueOf(line));
    }

    public void writeln(String line) {
        write(line + "\n");
    }

    public void writeln(int line) {
        writeln(String.valueOf(line));
    }

    public void writeln() {
        write("\n");
    }

    public void close() {
        try {
            if (file != null) file.close();
        } catch (IOException e) {
            errorHandling(e);
        }
    }
}

package edu.repetita.io;

import java.io.*;
import java.util.List;

public class RepetitaWriter {
    private static String outputFilename = null;
    private static String outpathsFilename = null;
    private static int verbose = 0;

    public static void setOutputFilename (String filename){
        outputFilename = filename;
    }

    public static void setOutpathsFilename (String filename){
        outpathsFilename = filename;
    }

    public static void setVerbose(int verboseLevel) {
        verbose = verboseLevel;
    }

    public static void appendToOutput (String content, int referenceVerboseLevel) {
        if (verbose >= referenceVerboseLevel) {
            appendToOutput(content);
        }
    }

    public static void appendToOutput (String content) {
        if (outputFilename != null){
            try {
                FileOutputStream stream = new FileOutputStream(new File(outputFilename), true);
                writeToFile(content, stream);
            }
            catch(Exception e){
                System.err.println("Impossible to write on file " + outputFilename);
                e.printStackTrace();
            }
        }
        else{
            System.out.println(content);
        }
    }

    public static void writeToFile (String content, String outFilename) {
        try {
            FileOutputStream stream = new FileOutputStream(new File(outFilename), false);
            writeToFile(content, stream);
        }
        catch (Exception e){
            System.err.println("Impossible to write on file " + outFilename);
            e.printStackTrace();
        }
    }

    private static void writeToFile (String content, FileOutputStream outFile){
        try {
            PrintWriter out = new PrintWriter(outFile);
            out.println(content);
            out.close();
        }
        catch (Exception e){
            System.err.println("Impossible to write on file " + outFile.toString());
            e.printStackTrace();
        }
    }

    public static void writeToPathFile (String paths) {
        if (outpathsFilename != null) {
            writeToFile(paths, outpathsFilename);
        }
    }

    public static String formatAsListOneColumn(List<String> listOfStrings){
        StringBuilder formatted = new StringBuilder();
        for(String s: listOfStrings){
            formatted.append("- ").append(s).append("\n");
        }
        return formatted.toString();
    }

    public static String formatAsListTwoColumns(List<String> col1, List<String> col2) {
        return formatAsListTwoColumns(col1, col2,"- ");
    }

    public static String formatAsListTwoColumns(List<String> col1, List<String> col2, String rowId) {
        // get the maximum size of strings in col1 (to decide spacing)
        int maxChars = 0;
        for(String s: col1){
            if(s.length() > maxChars){
                maxChars = s.length();
            }
        }
        // format and concatenate all strings
        int colWidth = 25;
        if (colWidth < maxChars){
            colWidth = maxChars + 5;
        }
        StringBuilder formatted = new StringBuilder();
        for(int i=0; i < col1.size(); i++){
            formatted.append(rowId).append(String.format("%1$-" + colWidth + "s %2$s", col1.get(i), col2.get(i))).append("\n");
        }

        return formatted.toString();
    }

    public static String formatAsDocumentation(String content) {
        StringBuilder commented = new StringBuilder();
        for(String line: content.split("\n")){
            commented.append(IOConstants.SOLVER_COMMENTCHAR + " ").append(line).append("\n");
        }
        return commented.toString();
    }

}

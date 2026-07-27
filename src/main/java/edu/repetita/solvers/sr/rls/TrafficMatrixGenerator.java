package edu.repetita.solvers.sr.rls;

import edu.repetita.solvers.sr.rls.io.TopologyData;
import edu.repetita.solvers.sr.rls.io.TopologyParser;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.Random;

public class TrafficMatrixGenerator {
    private static void printHelp() {
        System.out.println("Usage: TrafficMatrixGenerator input.graph [number of matrices, default 1]");
        System.exit(1);
    }

    public static void main(String[] args) throws IOException {
        if (args.length < 1) printHelp();

        String fileName = args[0];
        if (fileName.contains(".")) {
            fileName = fileName.substring(0, fileName.lastIndexOf('.'));
        }

        int nMatrices = (args.length > 1) ? Integer.parseInt(args[1]) : 1;
        if (nMatrices <= 0 || nMatrices > 1000) printHelp();

        TopologyData topologyData = TopologyParser.parse(fileName + ".graph");
        int nNodes = topologyData.nodeLabels.length;
        int nEdges = topologyData.edgeLabels.length;

        double[] sumIncoming = new double[nNodes];
        double[] sumOutgoing = new double[nNodes];

        for (int edge = 0; edge < nEdges; edge++) {
            sumIncoming[topologyData.edgeDests[edge]] += topologyData.edgeCapacities[edge];
            sumOutgoing[topologyData.edgeSrcs[edge]] += topologyData.edgeCapacities[edge];
        }

        int[][] matrix = new int[nNodes][nNodes];
        double[] trafficIn = new double[nNodes];
        double[] trafficOut = new double[nNodes];
        Random random = new Random();

        for (int m = 0; m < nMatrices; m++) {
            for (int i = 0; i < nNodes; i++) {
                trafficIn[i] = random.nextDouble() * sumIncoming[i];
                trafficOut[i] = random.nextDouble() * sumOutgoing[i];
            }

            double sumTrafficIn = Arrays.stream(trafficIn).sum();

            for (int i = 0; i < nNodes; i++) {
                for (int j = 0; j < nNodes; j++) {
                    matrix[i][j] = (int) (trafficOut[i] * trafficIn[j] / sumTrafficIn);
                }
            }

            String outFileName = String.format("%s.%04d.demands", fileName, m);
            try (PrintWriter outFile = new PrintWriter(new FileWriter(outFileName))) {
                outFile.println("DEMANDS");
                outFile.println("label src dest bw");

                int nDemand = 0;
                for (int i = 0; i < nNodes; i++) {
                    for (int j = 0; j < nNodes; j++) {
                        if (i != j) {
                            outFile.println("demand_" + nDemand + " " + i + " " + j + " " + matrix[i][j]);
                            nDemand++;
                        }
                    }
                }
            }
        }
    }
}

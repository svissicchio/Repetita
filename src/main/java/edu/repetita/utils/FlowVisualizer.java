package edu.repetita.utils;

import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.Line2D;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;
import javax.swing.BorderFactory;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

import edu.repetita.core.Topology;
import edu.repetita.simulators.FlowSimulator;

import edu.uci.ics.jung.algorithms.layout.AbstractLayout;
import edu.uci.ics.jung.algorithms.layout.FRLayout;
import edu.uci.ics.jung.graph.DirectedGraph;
import edu.uci.ics.jung.graph.DirectedSparseGraph;
import edu.uci.ics.jung.visualization.BasicVisualizationServer;
import edu.uci.ics.jung.visualization.GraphZoomScrollPane;
import edu.uci.ics.jung.visualization.VisualizationViewer;
import edu.uci.ics.jung.visualization.control.DefaultModalGraphMouse;
import edu.uci.ics.jung.visualization.control.ModalGraphMouse;
import edu.uci.ics.jung.visualization.decorators.EdgeShape;
import edu.uci.ics.jung.visualization.decorators.ToStringLabeller;

/**
 * A modern, interactive visualization for network flows.
 * Displays network topology with dynamic edge coloring based on link utilization
 * (ranging from green for light load, blue, yellow/orange, to red for bottlenecks).
 *
 * @author Steven Gay, REPETITA Team
 */
public class FlowVisualizer {
    private DirectedGraph<Integer, Integer> graph;
    private Topology topology;
    private FlowSimulator flow;

    private AbstractLayout<Integer, Integer> layout;
    private VisualizationViewer<Integer, Integer> viewer;
    private JFrame frame;

    private int nNodes;
    private int nEdges;

    private double[] edgeUtilization;
    private double maxUtilization = 0.0;

    public FlowVisualizer(FlowSimulator flow) {
        this.flow = flow;
        this.topology = flow.getSetting().getTopology();
        this.nNodes = topology.nNodes;
        this.nEdges = topology.nEdges;

        this.edgeUtilization = new double[nEdges];
        updateAllUtilizations();

        // Build JUNG directed graph
        graph = new DirectedSparseGraph<>();
        for (int node = 0; node < nNodes; node++) {
            graph.addVertex(node);
        }

        for (int edge = 0; edge < nEdges; edge++) {
            int source = topology.edgeSrc[edge];
            int dest = topology.edgeDest[edge];
            graph.addEdge(edge, source, dest);
        }

        // Layout algorithm
        layout = new FRLayout<>(graph);
        layout.setSize(new Dimension(850, 650));

        viewer = new VisualizationViewer<>(layout);
        viewer.setBackground(new Color(0xF8, 0xFA, 0xFC)); // Soft off-white background (#F8FAFC)

        // Enable interactive mouse controls (Zoom, Pan, Drag Nodes)
        DefaultModalGraphMouse<Integer, Integer> graphMouse = new DefaultModalGraphMouse<>();
        graphMouse.setMode(ModalGraphMouse.Mode.TRANSFORMING);
        viewer.setGraphMouse(graphMouse);
        viewer.addKeyListener(graphMouse.getModeKeyListener());

        // Edge shape: quad curve to separate bidirectional edges
        viewer.getRenderContext().setEdgeShapeTransformer(EdgeShape.quadCurve(graph));

        // Dynamic edge stroke width based on utilization (1.5px to 5.0px)
        viewer.getRenderContext().setEdgeStrokeTransformer((Integer edgeId) -> {
            double loadRatio = maxUtilization > 0 ? (edgeUtilization[edgeId] / maxUtilization) : 0;
            float width = (float) (1.5 + loadRatio * 3.5);
            return new BasicStroke(width, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND);
        });

        // Dynamic edge colors (Continuous smooth spectrum)
        viewer.getRenderContext().setEdgeDrawPaintTransformer((Integer edgeId) ->
            getColorForUtilization(edgeUtilization[edgeId], maxUtilization)
        );

        // Edge Tooltips (Hover info)
        viewer.setEdgeToolTipTransformer((Integer edgeId) -> {
            int src = topology.edgeSrc[edgeId];
            int dest = topology.edgeDest[edgeId];
            String srcName = (topology.nodeLabel != null && src < topology.nodeLabel.length) ? topology.nodeLabel[src] : "Node " + src;
            String destName = (topology.nodeLabel != null && dest < topology.nodeLabel.length) ? topology.nodeLabel[dest] : "Node " + dest;
            double load = flow.flowOnEdge(edgeId);
            double cap = topology.edgeCapacity[edgeId];
            double pct = cap > 0 ? (load / cap * 100.0) : 0.0;
            return String.format("<html><b>Link #%d</b>: %s &rarr; %s<br/>Load: %.1f / %.1f (<b>%.2f%%</b>)</html>",
                    edgeId, srcName, destName, load, cap, pct);
        });

        // Vertex / Node appearance
        viewer.getRenderContext().setVertexFillPaintTransformer((Integer nodeId) -> new Color(0x1E, 0x29, 0x3B)); // Dark slate
        viewer.getRenderContext().setVertexLabelTransformer((Integer nodeId) -> {
            if (topology.nodeLabel != null && nodeId < topology.nodeLabel.length && topology.nodeLabel[nodeId] != null) {
                return topology.nodeLabel[nodeId];
            }
            return String.valueOf(nodeId);
        });

        // Node Tooltips
        viewer.setVertexToolTipTransformer((Integer nodeId) -> {
            String label = (topology.nodeLabel != null && nodeId < topology.nodeLabel.length) ? topology.nodeLabel[nodeId] : "";
            return String.format("Node #%d %s", nodeId, label);
        });

        // Top Header Panel with Legend & Summary
        JPanel headerPanel = createHeaderPanel();

        // Main Frame setup
        frame = new JFrame("Repetita - Network Flow Visualizer");
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        frame.setLayout(new BorderLayout());
        frame.add(headerPanel, BorderLayout.NORTH);
        frame.add(new GraphZoomScrollPane(viewer), BorderLayout.CENTER);

        frame.setPreferredSize(new Dimension(950, 750));
        frame.pack();
        frame.setLocationRelativeTo(null); // Center on screen
        frame.setVisible(true);
    }

    private JPanel createHeaderPanel() {
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(new Color(0x0F, 0x17, 0x2A)); // Dark Navy (#0F172A)
        header.setBorder(BorderFactory.createEmptyBorder(10, 15, 10, 15));

        JLabel titleLabel = new JLabel(String.format(
            "<html><font color='#FFFFFF' size='4'><b>Network Flow Visualizer</b></font> &nbsp;|&nbsp; " +
            "<font color='#94A3B8'>Nodes: %d &nbsp;&bull;&nbsp; Edges: %d &nbsp;&bull;&nbsp; Max Link Load Ratio: <b>%.2f%%</b></font></html>",
            nNodes, nEdges, maxUtilization * 100.0
        ));
        header.add(titleLabel, BorderLayout.WEST);

        // Legend bar panel
        JPanel legendPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                int w = getWidth();
                int h = getHeight();

                // Draw legend color gradient
                Color[] colors = {
                    new Color(0x94, 0xA3, 0xB8), // Gray (0%)
                    new Color(0x10, 0xB9, 0x81), // Green (low)
                    new Color(0x3B, 0x82, 0xF6), // Blue (mid-low)
                    new Color(0xF5, 0x9E, 0x0B), // Yellow/Amber (mid-high)
                    new Color(0xEF, 0x44, 0x44)  // Red (critical)
                };

                int numCols = colors.length - 1;
                int barW = 120;
                int barH = 12;
                int startX = w - barW - 10;
                int startY = (h - barH) / 2 - 4;

                for (int i = 0; i < barW; i++) {
                    float ratio = (float) i / barW;
                    g2.setColor(getGradColor(ratio));
                    g2.fillRect(startX + i, startY, 1, barH);
                }
                g2.setColor(Color.WHITE);
                g2.drawRect(startX, startY, barW, barH);

                g2.setFont(new Font("SansSerif", Font.PLAIN, 10));
                g2.drawString("0%", startX - 18, startY + 10);
                g2.drawString("Max Load", startX + barW + 5, startY + 10);
            }
        };
        legendPanel.setOpaque(false);
        legendPanel.setPreferredSize(new Dimension(200, 30));
        header.add(legendPanel, BorderLayout.EAST);

        return header;
    }

    private static Color getGradColor(float ratio) {
        if (ratio <= 0.01f) return new Color(0x94, 0xA3, 0xB8); // Idle Slate
        if (ratio < 0.25f) return interpolateColor(new Color(0x10, 0xB9, 0x81), new Color(0x3B, 0x82, 0xF6), ratio / 0.25f);
        if (ratio < 0.60f) return interpolateColor(new Color(0x3B, 0x82, 0xF6), new Color(0xF5, 0x9E, 0x0B), (ratio - 0.25f) / 0.35f);
        if (ratio < 0.90f) return interpolateColor(new Color(0xF5, 0x9E, 0x0B), new Color(0xF9, 0x73, 0x16), (ratio - 0.60f) / 0.30f);
        return interpolateColor(new Color(0xF9, 0x73, 0x16), new Color(0xEF, 0x44, 0x44), Math.min(1.0f, (ratio - 0.90f) / 0.10f));
    }

    private static Color interpolateColor(Color c1, Color c2, float t) {
        t = Math.max(0.0f, Math.min(1.0f, t));
        int r = (int) (c1.getRed() + t * (c2.getRed() - c1.getRed()));
        int g = (int) (c1.getGreen() + t * (c2.getGreen() - c1.getGreen()));
        int b = (int) (c1.getBlue() + t * (c2.getBlue() - c1.getBlue()));
        return new Color(r, g, b);
    }

    private Color getColorForUtilization(double util, double maxUtil) {
        if (util <= 0.0001) {
            return new Color(0x94, 0xA3, 0xB8); // Muted gray for unused links
        }
        float ratio = maxUtil > 0 ? (float) (util / maxUtil) : 0f;
        return getGradColor(ratio);
    }

    public void saveImage(String filePath) {
        int width = frame != null ? frame.getWidth() : 950;
        int height = frame != null ? frame.getHeight() : 750;
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2 = image.createGraphics();
        if (frame != null) {
            frame.paint(g2);
        } else {
            viewer.paint(g2);
        }
        g2.dispose();
        try {
            File outputFile = new File(filePath);
            if (outputFile.getParentFile() != null) {
                outputFile.getParentFile().mkdirs();
            }
            ImageIO.write(image, "png", outputFile);
            System.out.println("Visualization screenshot saved to: " + outputFile.getAbsolutePath());
        } catch (IOException e) {
            System.err.println("Failed to save visualization screenshot: " + e.getMessage());
        }
    }

    public void blockUntilClosed() {
        if (frame == null) return;
        final Object lock = new Object();
        frame.addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosed(java.awt.event.WindowEvent e) {
                synchronized (lock) {
                    lock.notifyAll();
                }
            }
        });
        synchronized (lock) {
            try {
                lock.wait();
            } catch (InterruptedException ignored) {}
        }
    }

    private void updateAllUtilizations() {
        for (int edge = 0; edge < nEdges; edge++) {
            double capacity = topology.edgeCapacity[edge];
            double load = flow.flowOnEdge(edge);
            edgeUtilization[edge] = capacity > 0 ? (load / capacity) : 0;
        }
        updateMax();
    }

    private void updateMax() {
        double max = 0.0;
        for (int edge = 0; edge < nEdges; edge++) {
            max = Math.max(max, edgeUtilization[edge]);
        }
        maxUtilization = max;
    }

    public void updateVisualization() {
        updateAllUtilizations();
        viewer.updateUI();
    }
}

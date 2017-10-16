package edu.repetita.utils;

import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.geom.Line2D;

import javax.swing.JFrame;
import javax.swing.JPanel;

import edu.repetita.core.Topology;
import edu.repetita.simulators.FlowSimulator;
import edu.uci.ics.jung.algorithms.layout.AbstractLayout;
import edu.uci.ics.jung.algorithms.layout.FRLayout;
import edu.uci.ics.jung.graph.DirectedGraph;
import edu.uci.ics.jung.graph.DirectedSparseGraph;
import edu.uci.ics.jung.visualization.BasicVisualizationServer;
import edu.uci.ics.jung.visualization.GraphZoomScrollPane;
import edu.uci.ics.jung.visualization.VisualizationViewer;

/**
 *  A visualization for a flow, that colors edges in black if their utilization is relatively small,
 *  and uses colors from green to red the more they are relatively utilized. 
 *  <p>
 *  Layout is automatic for now.
 * 
 * @author Steven Gay
 */
public class FlowVisualizer {
  private DirectedGraph<Integer, Integer> graph;
  private Topology topology;
  private FlowSimulator flow;
  
  private AbstractLayout<Integer, Integer> layout;
  private VisualizationViewer<Integer, Integer> viewer;
  private JPanel panel;
  
  private int nNodes;
  private int nEdges;
  
  private double[] edgeUtilization;
  private double maxUtilization = 0.0;
  
  /**
   * Builds a visualizer for the flow given at construction.
   * 
   * @param flow the flow to display
   */
  public FlowVisualizer(FlowSimulator flow) {
    this.flow = flow;
    topology = flow.getSetting().getTopology();
    nNodes = topology.nNodes;
    nEdges = topology.nEdges;
    
    edgeUtilization = new double[nEdges];
    updateAllUtilizations();
    
    graph = new DirectedSparseGraph<Integer, Integer>();
    for (int node = 0; node < nNodes; node++) graph.addVertex(node);
    
    for (int edge = 0; edge < nEdges; edge++) {
      int source = topology.edgeSrc[edge];
      int dest   = topology.edgeDest[edge];
      graph.addEdge(edge, source, dest);
    }
    
    layout = new FRLayout<Integer, Integer>(graph);
    viewer = new VisualizationViewer<Integer, Integer>(layout);
    viewer.setBackground(Color.WHITE);
    
    // change edge appearence: thicken stroke, busy edges are colorful, opposite edges do not overlap
    BasicStroke stroke = new BasicStroke(2.0f);
    viewer.getRenderContext().setEdgeStrokeTransformer((Integer edgeId) -> stroke);
    viewer.getRenderContext().setEdgeDrawPaintTransformer((Integer edgeId) -> colorInterpolator(edgeUtilization[edgeId], maxUtilization));
    //viewer.getRenderContext().setEdgeShapeTransformer(EdgeShape.line(graph));
    Line2D line = new Line2D.Float(0.0f, 2.0f, 1.0f, 2.0f);
    viewer.getRenderContext().setEdgeShapeTransformer((Integer edgeId) -> line);
    
    // change vertex appearence
    viewer.getRenderContext().setVertexFillPaintTransformer((Integer nodeId) -> Color.WHITE);
    
    panel = new BasicVisualizationServer<Integer, Integer>(layout);
    panel.add(new GraphZoomScrollPane(viewer), BorderLayout.CENTER);
    
    JFrame frame = new JFrame("Flow Visualizer");
    frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    frame.getContentPane().add(viewer);
    frame.pack();
    frame.setVisible(true);
  }
  
  private void updateUtilization(int edge, double frac) {
    edgeUtilization[edge] = frac;
  }
  
  private void updateAllUtilizations() {
    for (int edge = 0; edge < nEdges; edge++) {
      double frac = flow.flowOnEdge(edge) / topology.edgeCapacity[edge];
      updateUtilization(edge, frac);
    }
  }
  
  private void updateMax() {
    double max = 0.0;
    
    for (int edge = 0; edge < nEdges; edge++) {
      max = Math.max(max, edgeUtilization[edge]);
    }
    
    maxUtilization = max;
  }
  
  /**
   * Updates the visualization's colors according to the current flow.
   */
  public void updateVisualization() {
    updateAllUtilizations();
    updateMax();
    viewer.updateUI();
  }
  
  private double dist(double x, double y) {
    double d = Math.abs(x-y) / maxUtilization;
    return Math.pow(d, 0.15);
  }
      
  private Color colorInterpolator(double frac, double maxUsage) {
    if      (frac >= maxUsage * 0.999999) return Color.RED;
    else if (frac >= maxUsage * 0.99999)  return new Color(0xFF, 0xA0, 0); // orange - red 
    else if (frac >= maxUsage * 0.9999)   return Color.ORANGE;
    else if (frac >= maxUsage * 0.999)    return new Color(0xFF, 0xC0, 0); // yellow - orange
    else if (frac >= maxUsage * 0.99)     return Color.YELLOW;
    else {
      // define RGB value
      float r = 0; // dist(maxUsage / 2, frac)
      float g = (float) (1.0 - dist(maxUsage, frac)); // dist(0.0, frac)
      float b = 0; // dist(maxUsage, frac)
      
      return new Color(r, g, b);
    }
  }
}

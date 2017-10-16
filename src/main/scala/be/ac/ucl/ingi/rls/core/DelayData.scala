package be.ac.ucl.ingi.rls.core

import be.ac.ucl.ingi.rls.ShortestPaths
import be.ac.ucl.ingi.rls._
import be.ac.ucl.ingi.rls.ShortestPaths
import be.ac.ucl.ingi.rls.io.TopologyData

trait DelayData {
  def delay(source: Node, destination: Node): Int
}

/**  For every (source, destination), computes the longest path in the DAG of shortest paths from source to destination with the latencies as weights. */
// TODO: store topological ordering of every DAG in shortest paths to save an ordering pass?
class DelayDataImpl(nNodes: Int, sp: ShortestPaths, topologyData: TopologyData) 
extends DelayData {
  private[this] val delay_ = Array.fill(nNodes, nNodes)(0)
  
  def delay(source: Node, destination: Node) = delay_(source)(destination)
  
  private[this] val ordering = new Array[Int](nNodes)
  private[this] val degrees  = new Array[Int](nNodes)
  
  initialize()
  
  def initialize() = {
    var destination = nNodes
    while (destination > 0) {
      destination -= 1
      // Topological sorting of shortest paths DAG of destination using Kahn's algorithm
      var pOrdering = 0
      
      @inline def addOrdering(node: Int) = {
        ordering(pOrdering) = node
        pOrdering += 1
      }
      
      // Copy degrees to temporary structure, while adding 0-degree nodes to ordering
      var node = nNodes
      while (node > 0) {
        node -= 1
        degrees(node) = sp.nPredecessors(destination)(node)
        if (degrees(node) == 0) addOrdering(node)
      }
      
      // Visit nodes in ordering from first inserted to last, decreasing degree of all successors by 1. If a successor gets degree 0, add it to ordering.
      var pNode = 0
      while (pNode < pOrdering) {
        val node = ordering(pNode)
        
        val successors = sp.successorNodes(destination)(node)
        var pSucc = sp.nSuccessors(destination)(node)
        while (pSucc > 0) {
          pSucc -= 1
          val eNode = successors(pSucc)
          degrees(eNode) -= 1
          if (degrees(eNode) == 0) addOrdering(eNode)
        }
        
        pNode += 1
      }
      // Ordering done!, pNode == pOrdering
      
      assert(ordering(pNode - 1) == destination)
      
      delay_(destination)(destination) = 0  // skip destination
      pNode -= 1
      
      // Visit nodes in reverse order to compute max paths
      while (pNode > 0) {
        pNode -= 1
        val node = ordering(pNode)
        var m = Int.MinValue
        
        val successorNodes = sp.successorNodes(destination)(node) 
        val successorEdges = sp.successorEdges(destination)(node) 
        var pSucc = sp.nSuccessors(destination)(node)
        while (pSucc > 0) {
          pSucc -= 1
          val succNode = successorNodes(pSucc) 
          val edge     = successorEdges(pSucc)

          m = math.max(m, topologyData.edgeLatencies(edge) + delay_(succNode)(destination))
        }
        delay_(node)(destination) = m
      }
    }
  }
}

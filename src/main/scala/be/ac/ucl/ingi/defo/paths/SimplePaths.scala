package be.ac.ucl.ingi.defo.paths

import be.ac.ucl.ingi.defo.core.Topology
import be.ac.ucl.ingi.defo.utils.Dijkstra

class SimplePaths(topology: Topology, weights: Array[Int]) {
  
  require(weights.length == topology.nEdges, "the number of weights does not correspond to the topology.")
  
  private[this] val nNodes = topology.nNodes
  
  private[this] val nPaths: Array[Array[Int]] = Array.tabulate(nNodes)(dest => {
    computeNPath(dest)
  })
  
  final def nPaths(src: Int, dest: Int): Int = nPaths(src)(dest)
  
  private def computeNPath(dest: Int): Array[Int] = {  
    val successors = Dijkstra.shortestPathTo(dest, topology, weights)._1
    val nPaths = new Array[Int](nNodes)   
    // Init
    nPaths(dest) = 1   
    // Compute
    var i = nNodes
    while (i > 0) {
      i -= 1
      computeNPath0(nPaths, successors, i)
    }    
    nPaths
  }
  
  // FIXME NOT TAIL RECURSIVE
  private def computeNPath0(nPaths: Array[Int], successors: Array[List[Int]], node: Int): Int = {
    if (nPaths(node) > 0) nPaths(node)
    else {
      for (edge <- successors(node)) {
        val n = topology.edgeDest(edge)
        nPaths(node) += computeNPath0(nPaths, successors, n)
      }
      nPaths(node)
    }
  }
}

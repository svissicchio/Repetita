package be.ac.ucl.ingi.rls

import be.ac.ucl.ingi.rls.core.Topology
import be.ac.ucl.ingi.rls.structure.ArrayHeapInt
import be.ac.ucl.ingi.rls.core.Topology


/*
 * Shortest Paths are given a topology on which it computes single-source shortest paths.
 * 
 * Given weights on every edge, it returns a DAG of shortest paths represented as such:
 * _ for every node, an ArrayBuffer of in edges. 
 *   
 *  @author Renaud Hartert ren.hartert@gmail.com
 *  @author Steven Gay aashcrahin@gmail.com
 */

class ShortestPaths(topology: Topology, weights: Array[Int]) {
  import ShortestPaths._
  assert(topology.nEdges == weights.length)
  
  // Topology
  private[this] val nNodes = topology.nNodes
  private[this] val nEdges = topology.nEdges
  
  // Shortest path DAGs, per destination
  // dest, node => array of successors of node in DAG of dest
  val successorNodes = Array.tabulate(nNodes, nNodes)((dest, node) => new Array[Int](topology.outEdges(node).length))  
  val successorEdges = Array.tabulate(nNodes, nNodes)((dest, node) => new Array[Int](topology.outEdges(node).length)) 
  val nSuccessors = Array.ofDim[Int](nNodes, nNodes)
  
  // dest, node => array of predecessors of node in DAG of dest
  val predecessorNodes  = Array.tabulate(nNodes, nNodes)((dest, node) => new Array[Int](topology.inEdges(node).length))  
  val predecessorEdges  = Array.tabulate(nNodes, nNodes)((dest, node) => new Array[Int](topology.inEdges(node).length))  
  val nPredecessors = Array.ofDim[Int](nNodes, nNodes)

  // Distance matrix
  private[this] val distance = Array.ofDim[Int](nNodes, nNodes)

  // Permanent heap used to compute a lot of things
  private[this] val heap = new ArrayHeapInt(nNodes)
  private[this] val inHeap = new Array[Boolean](nNodes)
  
  computeShortestPaths()
  
   /** Return true if there is a path from src to dest in the network. */
  final def canReach(src: Int, dest: Int): Boolean = distance(src)(dest) < InfDistance
  
  def computeShortestPaths(): Unit = {
    // compute DAGs to all destinations, represented as successors
    // O(V E log V)
    var dest = nNodes
    while (dest > 0) {
      dest -= 1
      computeShortestPathsTo(dest)
    }
    
    /* Use successors to compute DAGs as predecessors */
    // set all nPredecessors to 0, O(V^2)
    dest = nNodes
    while (dest > 0) {
      dest -= 1
      
      var node = nNodes
      while (node > 0) {
        node -= 1
        nPredecessors(dest)(node) = 0
      }
    }
    
    // browse successor structure: when b successor of a in DAG dest, add a as predecessor of b in DAG dest
    // O(V E)
    dest = nNodes
    while (dest > 0) {
      dest -= 1
      
      var nodeA = nNodes
      while (nodeA > 0) {
        nodeA -= 1
        val succNodes = successorNodes(dest)(nodeA)
        val succEdges = successorEdges(dest)(nodeA)
        var pSucc = nSuccessors(dest)(nodeA)
        while (pSucc > 0) {
          pSucc -= 1
          val nodeB = succNodes(pSucc)
          val edge  = succEdges(pSucc)
          predecessorNodes(dest)(nodeB)(nPredecessors(dest)(nodeB)) = nodeB
          predecessorEdges(dest)(nodeB)(nPredecessors(dest)(nodeB)) = edge
          nPredecessors(dest)(nodeB) += 1
        }
      }
    }
  }
  
  private def computeShortestPathsTo(dest: Int): Unit = {
    // Reset structures
    var i = nNodes
    while (i > 0) {
      i -= 1
      inHeap(i) = false
      distance(i)(dest) = InfDistance
      nSuccessors(dest)(i) = 0
    }

    // Initialize with first event
    distance(dest)(dest) = 0
    heap.enqueue(0, dest)

    // Run Dijkstra's algorithm for single destination shortest path
    while (!heap.isEmpty) {
      // visit next (closest) node
      val node = heap.dequeue()
      inHeap(node) = false
      
      val inEdges = topology.inEdges(node)
      var i = inEdges.length
      while (i > 0) {
        i -= 1
        val edge = inEdges(i)
        val src = topology.edgeSrc(edge)

        // New distance (prevent overflow)
        val oldDistance = distance(node)(dest)
        val edgeWeight = weights(edge)
        val newDist = 
          if (oldDistance > InfDistance - edgeWeight) InfDistance
          else oldDistance + edgeWeight

        // Update distances
        val srcDist = distance(src)(dest)
        val comp = newDist - srcDist
        if (comp < 0) {
          if (inHeap(src)) heap.changeKey(distance(src)(dest), newDist, src) 
          else heap.enqueue(newDist, src)
          distance(src)(dest) = newDist
          successorEdges(dest)(src)(0) = edge
          successorNodes(dest)(src)(0) = node
          nSuccessors(dest)(src) = 1
          inHeap(src) = true
        } 
        else if (comp == 0) {
          val i = nSuccessors(dest)(src)
          successorEdges(dest)(src)(i) = edge
          successorNodes(dest)(src)(i) = node
          nSuccessors(dest)(src) += 1
        }
      }
    }
  }
  

  /**  Some facilities to generate topological ordering of shortest path DAGs from source to destination */ 
  
  private[this] val toVisitStack  = new Array[Int](nEdges + nNodes)
  private[this] val visited  = Array.fill(nNodes)(false)
  private[this] val visiting = Array.fill(nNodes)(false)
  final val topologicalOrdering = new Array[Int](nNodes)
  private[this] val degree = new Array[Int](nNodes)
  
  /** Makes a topological ordering of the nodes in DAG of the destination */
  def makeTopologicalOrdering(dest: Node): Int = {
    var nToVisit = 0
    var node = nNodes
    while (node > 0) {
      node -= 1
      degree(node) = nPredecessors(dest)(node)
      if (degree(node) == 0) {
        toVisitStack(nToVisit) = node
        nToVisit += 1
      }
    }
    
    var nOrder = nNodes
    while (nToVisit > 0) {
      nToVisit -= 1
      val node = toVisitStack(nToVisit)
      nOrder -= 1
      topologicalOrdering(nOrder) = node
      var pSucc = nSuccessors(dest)(node)
      while (pSucc > 0) {
        pSucc -= 1
        val succ = successorNodes(dest)(node)(pSucc)
        degree(succ) -= 1
        if (degree(succ) == 0) {
          toVisitStack(nToVisit) = succ
          nToVisit += 1
        }
      }
    }
    
    assert(nOrder == 0, s"nOrder = $nOrder != nNodes = $nNodes")
    nNodes
  }
  
  /** Makes a topological ordering of the nodes in the subDAG of the destination DAG made of nodes reachable from source */
  // Uses a DFS-based algorithm 
  def makeTopologicalOrdering(source: Node, destination: Node): Int = {
    val successors = successorNodes(destination)
    val nSuccessorNodes = nSuccessors(destination)
    
    toVisitStack(0) = source
    var pStack = 1
    var pOrdering = 0
    
    while (pStack > 0) {
      pStack -= 1  // pop
      val node = toVisitStack(pStack)
      
      if (visiting(node)) {
        visiting(node) = false
        visited(node) = true
        topologicalOrdering(pOrdering) = node
        pOrdering += 1
      }
      else if (!visited(node)) {
        visiting(node) = true
        // toVisitStack(pStack) = node  // push
        pStack +=1
        
        var pSucc = nSuccessorNodes(node)
        while (pSucc > 0) {
          pSucc -= 1
          val succ = successors(node)(pSucc) // push
          toVisitStack(pStack) = succ
          pStack +=1
        }
      }
    }
    
    // clear visited
    val nOrdering = pOrdering
    while (pOrdering > 0) {
      pOrdering -= 1
      visited(topologicalOrdering(pOrdering)) = false
    }
    
    nOrdering
  }

}

object ShortestPaths { 
  final val InfDistance: Int = 1000000000
}

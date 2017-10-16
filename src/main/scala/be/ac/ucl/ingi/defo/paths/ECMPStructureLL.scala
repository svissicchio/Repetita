package be.ac.ucl.ingi.defo.paths

import scala.collection.mutable.HashSet
import be.ac.ucl.ingi.defo.utils.BinaryHeap
import be.ac.ucl.ingi.defo.utils.Dijkstra
import be.ac.ucl.ingi.defo.core.Topology

class ECMPStructureLL(override val topology: Topology, val weights: Array[Int], private val paths: Array[Array[Set[Int]]], private val flows: Array[Array[Array[Double]]], private val latencyMatrix: Array[Array[Int]]) extends ECMPStructure {
  
  override val nSegments = topology.nNodes * topology.nNodes - topology.nNodes
  
  private val segmentIds = Array.ofDim[Int](topology.nNodes, topology.nNodes)
  private val segmentSrcs = Array.ofDim[Int](nSegments)
  private val segmentDests = Array.ofDim[Int](nSegments)
  private val reversedPaths = Array.fill(topology.nEdges)(List[Int]())
  
  // Initializes internal structures 
  {
    var id = 0
    for (src <- topology.Nodes; dest <- topology.Nodes if src != dest) {
      segmentIds(src)(dest) = id
      segmentSrcs(id) = src
      segmentDests(id) = dest
      for (edge <- topology.Edges) {
        if (paths(src)(dest).contains(edge)) {
          reversedPaths(edge) = id :: reversedPaths(edge)
        }
      }
      id += 1
    }
  }
  
  override def weight(linkId: Int): Int = weights(linkId)
  
  override def segmentSrc(segmentId: Int): Int = segmentSrcs(segmentId)
  
  override def segmentDest(segmentId: Int): Int = segmentDests(segmentId)
    
  override def segmentId(src: Int, dest: Int): Int = segmentIds(src)(dest)
  
  override def linkSrc(linkId: Int): Int = topology.edgeSrc(linkId)
  
  override def linkDest(linkId: Int): Int = topology.edgeDest(linkId)

  override def linkId(src: Int, dest: Int): Int = ???
  
  override def latency(segmentId: Int): Int = latencyMatrix(segmentSrc(segmentId))(segmentDest(segmentId))
  
  override def latency(segmentSrc: Int, segmentDest: Int): Int = latencyMatrix(segmentSrc)(segmentDest)
  
  override def segments(linkId: Int): List[Int] = reversedPaths(linkId)
  
  override def links(segmentId: Int): Set[Int] = paths(segmentSrcs(segmentId))(segmentDests(segmentId))
  
  override def links(segmentSrc: Int, segmentDest: Int): Set[Int] = paths(segmentSrc)(segmentDest)

  override def flow(segmentId: Int, linkId: Int): Double = flows(segmentSrcs(segmentId))(segmentDests(segmentId))(linkId)
  
  override def flow(segmentSrc: Int, segmentDest: Int, linkId: Int): Double = flows(segmentSrc)(segmentDest)(linkId)
}

object ECMPStructureLL {
  
  def apply(topology: Topology, weights: Array[Int], latencies: Array[Int]): ECMPStructure = {
    val (paths, flows) = computePathsAndFlows(topology, weights)
    // path(i)(j) is the set of edges in graph between segment implied by node source i and destination j
    val latencyMatrix = Array.tabulate(topology.nNodes ,topology.nNodes){case (s,d) => computeLatency(topology, paths(s)(d), s, d, latencies)}
    new ECMPStructureLL(topology, weights, paths, flows, latencyMatrix) 
  }

  def computeLatency(topology: Topology, edges: Set[Int], source: Int, dest: Int, latencies: Array[Int]): Int = {
    def memoize[A, B](f: A => B): Function1[A, B] = new Function1[A, B] {
      val results = collection.mutable.Map.empty[A, B]
      def apply(in: A) = results.getOrElseUpdate(in, f(in))
    }
    lazy val maxLatency: Function1[(Int, Int), Int] = memoize { t: (Int, Int) =>
      val (s, d) = t
      if (s == d) 0
      else {
        val outEdges = topology.outEdges(s).filter(e => edges.contains(e))
        if (outEdges.isEmpty) {0}
        else outEdges.map(e => latencies(e) + maxLatency((topology.edgeDest(e), d))).max
      }
    }
    
    maxLatency(source,dest)
  }
  
    
  def computePathsAndFlows(topology: Topology, weights: Array[Int]): (Array[Array[Set[Int]]], Array[Array[Array[Double]]]) = {
    val paths = Array.ofDim[Set[Int]](topology.nNodes, topology.nNodes)
    val flows = Array.ofDim[Array[Double]](topology.nNodes, topology.nNodes)
    for (destId <- topology.Nodes) {
      val (pathsTo, distances) = Dijkstra.shortestPathTo(destId, topology, weights)
      for (srcId <- topology.Nodes) {
        paths(srcId)(destId) = collectEdges(topology, srcId, destId, pathsTo).toSet
        flows(srcId)(destId) = buildFlow(topology, srcId, pathsTo, distances, topology.nEdges)
      }
    }  
    (paths, flows)
  }

  private def collectEdges(topology: Topology, srcId: Int, destId: Int, pathsTo: Array[List[Int]]): HashSet[Int] = {
    collectEdges(topology, List(srcId), HashSet(), pathsTo, destId)
  }
  
  @annotation.tailrec
  private def collectEdges(topology: Topology, toVisit: List[Int], edges: HashSet[Int], pathsTo: Array[List[Int]], dest: Int): HashSet[Int] = {
    if (toVisit.isEmpty) edges
    else {
      val nextNodeId = toVisit.head
      var nextVisit = toVisit.tail
      // var newEdges = edges
      pathsTo(nextNodeId).foreach(edge => {
        //newEdges += edge
        edges += edge
        nextVisit = topology.edgeDest(edge) :: nextVisit
      })
      //collectEdges(topology, nextVisit, newEdges, pathsTo, dest)
      collectEdges(topology, nextVisit, edges, pathsTo, dest)
    }
  }
  
  private def buildFlow(topology: Topology, src: Int, pathsTo: Array[List[Int]], distances: Array[Int], nEdges: Int): Array[Double] = {
    val flows = Array.fill(nEdges)(0.0)
    val toSend = Array.fill(distances.size)(0.0)
    val visited = Array.fill(distances.size)(false)
    val queue = new BinaryHeap[Int](distances.size)    
    queue.enqueue(-distances(src), src)
    toSend(src) = 1
    visited(src) = true   
    while (!queue.isEmpty) {
      val node = queue.dequeue()
      val nextHops = pathsTo(node)
      val flow = toSend(node).toDouble / nextHops.size
      for (nextHop <- nextHops) {
        val nextDest = topology.edgeDest(nextHop)
        toSend(nextDest) += flow
        // Rounding error
        if (toSend(nextDest) > 1) toSend(nextDest) = 1.0
        flows(nextHop) = flow
        if (!visited(nextDest)) {
          queue.enqueue(-distances(nextDest), nextDest)
          visited(nextDest) = true
        }     
      }
    }   
    flows
  }
}

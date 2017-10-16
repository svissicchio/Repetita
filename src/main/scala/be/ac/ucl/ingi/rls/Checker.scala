package be.ac.ucl.ingi.rls

import be.ac.ucl.ingi.rls.core.Topology
import be.ac.ucl.ingi.rls.io.{DemandParser, PathsParser, TopologyParser}
import be.ac.ucl.ingi.rls.core._
import be.ac.ucl.ingi.rls.io._

import scala.collection.mutable.ArrayStack

/*
 *  Checker takes a graph, demands, and paths, and checks that the announced max maxLinkLoad is actually the one announced.
 */

object Checker extends App {
  if (args.length < 1) {
    println("Usage: Checker input_stem [timeLimit in seconds]")
    println("Needs .graph, .demands, and .paths files.")
    sys.exit(1)
  }
  
  final val debug = true
  val fileName = args(0)
  val timeLimit = if (args.length > 1) args(1).toInt else 10
  
  /*
   *   Parsing
   */
  
  val topologyData  = TopologyParser.parse(fileName + ".graph")
  val topology      = Topology(topologyData)
  val demandsData = DemandParser.parse(fileName + ".demands")
  
  val pathsData = PathsParser.parse(fileName + ".paths", topologyData, demandsData)
  val nNodes = topology.nNodes
  
  /*
   *  Simulate flow obtained by using given paths, compute max maxLinkLoad.
   */
  
  // compute shortest path DAG to every destination, O(E log V) per dest
  val sp = new ShortestPaths(topology, topologyData.edgeWeights)
  
  // group demand segments per segment destination, O(|segments|)
  val demandSourcePerDestination = Array.fill(nNodes)(ArrayStack.empty[Node])
  val demandAmountPerDestination = Array.fill(nNodes)(ArrayStack.empty[Double])
  
  var demand = demandsData.nDemands
  while (demand > 0) {
    demand -= 1
    val amount = demandsData.demandTraffics(demand)
    
    if (pathsData.hasPath(demand)) {
      val path = pathsData.pathOf(demand)
      
      var p = path.length
      while (p > 1) {
        p -= 1
        val src = path(p-1)
        val dest = path(p)
        
        demandSourcePerDestination(dest).push(src)
        demandAmountPerDestination(dest).push(amount)
      }
    }
    else {
      val src  = demandsData.demandSrcs(demand)
      val dest = demandsData.demandDests(demand)
      demandSourcePerDestination(dest).push(src)
      demandAmountPerDestination(dest).push(amount)
    }
  }
  
  // for every destination, route the demand segments to that destination using topological ordering, O(E) per dest
  val flow = Array.fill(topology.nEdges)(0.0)
  val toRoute = Array.fill(nNodes)(0.0)
  
  var dest = nNodes
  while (dest > 0) {  // expect toRoute to be 0.0 at each node
    dest -= 1
    
    // initialize nodes with sum of segment demands
    var pSource = demandSourcePerDestination(dest).size
    while (pSource > 0) {
      pSource -= 1
      val source = demandSourcePerDestination(dest)(pSource)
      val amount = demandAmountPerDestination(dest)(pSource)
      toRoute(source) += amount
    }
    
    // push flow to destination, using a topological order
    var p = sp.makeTopologicalOrdering(dest)
    val ordering = sp.topologicalOrdering
    while (p > 0) {
      p -= 1
      val src = ordering(p)
      
      var nSucc = sp.nSuccessors(dest)(src)
      val increment = toRoute(src) / nSucc
      toRoute(src) = 0.0

      // add increment amount of flow to successor edges
      val successorEdges = sp.successorEdges(dest)(src)
      var pSucc = nSucc
      while (pSucc > 0) {
        pSucc -= 1
        val edge = successorEdges(pSucc)
        flow(edge) += increment
      }
      
      // add increment amount of flow to successor nodes
      val successorNodes = sp.successorNodes(dest)(src)
      pSucc = nSucc
      while (pSucc > 0) {
        pSucc -= 1
        val node = successorNodes(pSucc)
        toRoute(node) += increment
      }
    }    
  }
  
  // compute max maxLinkLoad
  var maxLoad = 0.0
  var edge = topology.nEdges
  while (edge > 0) {
    edge -= 1
    maxLoad = math.max(maxLoad, flow(edge) / topologyData.edgeCapacities(edge))
  }
  
  println(s"maxLoad is $maxLoad, announced ${pathsData.maxLoad}")
}

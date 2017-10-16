package be.ac.ucl.ingi.defo.modeling

import scala.collection.mutable.Map
import be.ac.ucl.ingi.defo.core.Topology
import be.ac.ucl.ingi.defo.paths.ConnectStructure
import be.ac.ucl.ingi.defo.modeling.units.LoadUnit
import be.ac.ucl.ingi.defo.warning
import be.ac.ucl.ingi.defo.info
import be.ac.ucl.ingi.defo.core.DEFOInstance
import scala.collection.mutable.ArrayBuffer
import be.ac.ucl.ingi.defo.core.CoreSolver
import defo.modeling.variables.DEFOLoadVar
import be.ac.ucl.ingi.defo.core.Topology
import be.ac.ucl.ingi.defo.paths.ConnectStructure
import oscar.algo.array.ArrayStack

class MRProblem(topology: Topology) {

  // Implicit reference to this class
  implicit val self: MRProblem = this

  // Maps labels to their corresponding classes
  private[this] val labelToNode: Map[Symbol, DEFONode] = arrayNodesToMap(topology.nodeLabels)
  private[this] val labelToEdge: Map[Symbol, DEFOEdge] = arrayEdgesToMap(topology.edgeLabels)
  private[this] val labelToDemand: Map[Symbol, DEFODemand] = Map.empty

  implicit final protected def label2Node(label: Symbol): DEFONode = {
    labelToNode.getOrElse(label, sys.error(s"No node referenced by $label"))
  }

  implicit final protected def label2Edge(label: Symbol): DEFOEdge = {
    labelToEdge.getOrElse(label, sys.error(s"No edge referenced by $label"))
  }

  implicit final protected def label2Demand(label: Symbol): DEFODemand = {
    labelToDemand.getOrElse(label, sys.error(s"No demand referenced by $label"))
  }
  
  final protected def existsDemand(label: Symbol): Boolean = labelToDemand.contains(label)
  
  def minimize(objective: DEFOLoadVar): Unit = ()

  // Demands registered
  private[this] val demandSrcs = ArrayBuffer[Int]()
  private[this] val demandDests = ArrayBuffer[Int]()
  private[this] val demandTraffics = ArrayBuffer[Int]()
  private[this] val demandSymbols = ArrayBuffer[Symbol]()

  // Constraints on edges and demands
  private[this] val constraints = new ArrayStack[DEFOConstraint](topology.nNodes)

  // Compute the pair to pair connectivy
  private[this] val reachStruct = new ConnectStructure(topology)

  /** Returns the maximum maxLinkLoad */
  final def maxLoad: DEFOLoadVar = new DEFOLoadVar(-1)
  
  /** Returns an iterable of demands */
  final def demands: Iterable[DEFODemand] = {
    labelToDemand.values
  }
  
  /** Returns an iterable of nodes */
  final def nodes: Iterable[DEFONode] = {
    labelToNode.values
  }

  /** Adds a new constraint to the problem */
  def add(constraint: DEFOConstraint): Unit = {
    constraints.push(constraint)
  }

  /** Creates a new demand */
  final def newDemand(label: String, src: DEFONode, dest: DEFONode, traffic: LoadUnit): Unit = {
    val srcId = src.nodeId
    val destId = dest.nodeId
    val t = traffic.value
    val reachable = reachStruct.reachable(srcId, destId)
    if (!reachable) warning(s"Demand $label dropped: no path from $src to $dest.")
    else if (t <= 0) warning(s"Demand $label dropped: $traffic <= 0.")
    else {
      val symbol = Symbol(label)
      if (labelToDemand.contains(symbol)) warning(s"The label $symbol is already used.")
      else {
        val demand = new DEFODemand(demandSrcs.size, label)
        labelToDemand.+=((symbol, demand))
        demandSrcs.append(srcId)
        demandDests.append(destId)
        demandTraffics.append(t)
        demandSymbols.append(symbol)
      }
    }
  }
 
  /** Creates a new demand */
  final def newDemand(label: String, srcId: Int, destId: Int, traffic: LoadUnit): Unit = {
    val t = traffic.value
    val reachable = reachStruct.reachable(srcId, destId)
    if (!reachable) warning(s"Demand $label dropped: no path from node $srcId to node $destId.")
    else if (t <= 0) warning(s"Demand $label dropped: $traffic <= 0.")
    else {
      val symbol = Symbol(label)
      if (labelToDemand.contains(symbol)) warning(s"The label $symbol is already used.")
      else {
        val demand = new DEFODemand(demandSrcs.size, label)
        labelToDemand.+=((symbol, demand))
        demandSrcs.append(srcId)
        demandDests.append(destId)
        demandTraffics.append(t)
        demandSymbols.append(symbol)
      }
    }
  }

  /** Saves the problem definition and its topology. */
  final def saveAs(filePath: String): Unit = {

    
    
  }

  @inline private def arrayNodesToMap(array: Array[String]): Map[Symbol, DEFONode] = {
    val map = Map.empty[Symbol, DEFONode]
    var i = array.length
    while (i > 0) {
      i -= 1
      val label = Symbol(array(i))
      if (map.contains(label)) sys.error(s"$label is already used")
      else map.+=((label, new DEFONode(i, label.name)))
    }
    map
  }

  @inline private def arrayEdgesToMap(array: Array[String]): Map[Symbol, DEFOEdge] = {
    val map = Map.empty[Symbol, DEFOEdge]
    var i = array.length
    while (i > 0) {
      i -= 1
      val label = Symbol(array(i))
      if (map.contains(label)) sys.error(s"$label is already used")
      else map.+=((label, new DEFOEdge(i, label.name)))
    }
    map
  }
  
  final def assignedPath(demand: DEFODemand, defoptimizer: DEFOptimizer): Array[Int] = {
    defoptimizer.core.bestPaths(demand.demandId)
  }

  final def toInstance(weights: Array[Int], capacities: Array[Int], latencies: Array[Int]): DEFOInstance = {

    val demandConstraints = Array.fill(demandSrcs.size)(ArrayBuffer[DEFOConstraint]())
    val topologyConstraints = ArrayBuffer[DEFOConstraint]()

    var i = constraints.size
    while (i > 0) {
      i -= 1
      val constraint = constraints(i)
      if (constraint.isInstanceOf[DEFODemandConstraint]) {
        val c = constraint.asInstanceOf[DEFODemandConstraint]
        demandConstraints(c.demandId).append(constraint)
      }
      else {
        topologyConstraints.append(constraint)
      }
    }

    new DEFOInstance(
      topology,
      weights,
      demandTraffics.toArray,
      demandSrcs.toArray,
      demandDests.toArray,
      demandConstraints.map(_.toArray),
      topologyConstraints.toArray,
      capacities,
      latencies
    )
  }
}

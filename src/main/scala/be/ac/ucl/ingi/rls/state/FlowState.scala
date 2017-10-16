package be.ac.ucl.ingi.rls.state

import be.ac.ucl.ingi.rls.ShortestPaths
import be.ac.ucl.ingi.rls._
import be.ac.ucl.ingi.rls.ShortestPaths
import be.ac.ucl.ingi.rls.io.DemandsData
import be.ac.ucl.ingi.rls.core.CapacityData


/*
 *  For a given graph, maintains the amount of flow on each edge, and the set of demands using each edge.
 *  A FlowState depends on the PathState and ShortestPath to know who goes where, and DemandsData for the amount of resulting flow.
 *  
 *  The queries it must answer are: amount of flow on given edge, 
 *  number of demands on given edge, and ranking of demands by amount of flow on given edge.  
 *  It can receive one kind of modification, which is "demand d has goes from a to b with bandwidth bw"
 */

abstract class FlowStateChecker(nNodes: Int, nEdges: Int, pathState: PathState, demandsData: DemandsData)
extends ArrayStateDouble(nEdges)
{
  override def check() = {
    updateState()
    super.check()    // call check of monitoring trials
  }
  
  def modify(source: Node, destination: Node, bw: Double): Unit

  protected def initialize() = {
    var demand = pathState.nDemands
    while (demand > 0) {
      demand -= 1
      val path = pathState.path(demand)
      var pos = pathState.size(demand) - 1
      
      while (pos > 0) {
        pos -= 1
        
        val source      = path(pos)
        val destination = path(pos + 1)
        
        modify(source, destination, demandsData.demandTraffics(demand))
      }
    }
  }
  
  override def updateState() = {
    // update my state according to last changes on pathState
    var pChanged = pathState.nChanged
    val changed = pathState.changed
    while (pChanged > 0) {
      pChanged -= 1
      val demand = changed(pChanged)
      val bandwidth = demandsData.demandTraffics(demand)
      
      // localize the place where changes took place
      val currentPath = pathState.path(demand)
      val currentSize = pathState.size(demand)
      val oldPath = pathState.oldPath(demand)
      val oldSize = pathState.oldSize(demand)
      
      
      // invariant: both paths have the same first element and the same last element, so minSize >= 2
      // compute firstdiff, the smallest index s.t. currentPath(_) != oldPath(_)
      val minSize = math.min(currentSize, oldSize)
      var firstDiff = 1                                                                             // moves can not change source, do not look at first element
      while (firstDiff < minSize && currentPath(firstDiff) == oldPath(firstDiff)) firstDiff += 1
      
      var endCurrent = currentSize - 2     // moves can not change destination, do not look at last element
      var endOld     = oldSize - 2
      
      while (firstDiff < endCurrent && firstDiff < endOld && currentPath(endCurrent) == oldPath(endOld)) {
        endCurrent -= 1
        endOld -= 1
      }
      
      //println(currentPath.slice(0, currentSize).mkString(", "))
      //println(oldPath.slice(0, oldSize).mkString(", "))
      //println((currentSize, oldSize, firstDiff, endCurrent, endOld)) 
      
      var p = firstDiff - 1
      while (p <= endCurrent) {
        //println(s"adding ${currentPath(p)} => ${currentPath(p+1)}")
        modify(currentPath(p), currentPath(p+1), bandwidth)
        p += 1
      }

      var q = firstDiff - 1
      while (q <= endOld) {
        //println(s"removing ${oldPath(q)} => ${oldPath(q+1)}")
        modify(oldPath(q), oldPath(q+1), -bandwidth)
        q += 1
      }
    }
  }
  
}


// TODO: factorize by destination for faster initialization. Might be useful if some full recomputations are made, e.g. to remove rounding errors.
class FlowStateRecomputeDAG(nNodes: Int, nEdges: Int, sp: ShortestPaths, pathState: PathState, demandsData: DemandsData) 
extends FlowStateChecker(nNodes, nEdges, pathState, demandsData)
{
  private[this] val toRoute = Array.fill(nNodes)(0.0)

  // change the amount sent from source to destination by bw, which can be positive or negative. 
  override def modify(source: Node, destination: Node, bw: Double): Unit = {
    val successorNodes = sp.successorNodes(destination)
    val successorEdges = sp.successorEdges(destination)
    val nSuccessors = sp.nSuccessors(destination)
    
    // while there is only a simple path from source to destination, just push along the single path
    // this is designed to make the use a topological ordering to a minimum
    var source0 = source
    while (source0 != destination && nSuccessors(source0) == 1) {
      val edge = successorEdges(source0)(0)
      updateValue(edge, values(edge) + bw)
      
      val next = successorNodes(source0)(0)
      source0 = next
    }

    // if job is not done by pushing along simple path,
    // then we make a topological order of nodes we'll have to traverse and push flow in that order  
    if (source0 != destination) {
      val ordering = sp.topologicalOrdering
      var pOrdering = sp.makeTopologicalOrdering(source, destination)
      toRoute(source0) = bw
      
      while (pOrdering > 0) {
        pOrdering -= 1
        val node = ordering(pOrdering)
        
        // push flow to edges, add flow to successor nodes' toRoute
        var pSucc = nSuccessors(node)
        val increment = toRoute(node) / pSucc
        toRoute(node) = 0.0
        
        while (pSucc > 0) {
          pSucc -= 1
          
          val succEdge = successorEdges(node)(pSucc)
          updateValue(succEdge, values(succEdge) + increment)
          
          val succNode = successorNodes(node)(pSucc)
          toRoute(succNode) += increment
        }
        
      }
    }
  }
    
  initialize()
  commitState()
}


// TODO: use ECMP class
class FlowStatePrecomputeDAG(nNodes: Int, nEdges: Int, sp: ShortestPaths, pathState: PathState, demandsData: DemandsData) 
extends FlowStateChecker(nNodes, nEdges, pathState, demandsData)
{
  private[this] val toRoute = Array.fill(nNodes)(0.0)
  
  /*
   *  How to update flow from delta on demands.
   *  Modifying a demand entails modifying flow on source->destination subDAGs;
   *  in this implementation, the set of edges to modify and the fraction to modify is precomputed for every source-destination.   
   */
  
  private[this] val nEdgesToModify   = Array.ofDim[Int](nNodes, nNodes)
  private[this] val edgesToModify    = Array.ofDim[Array[Edge]](nNodes, nNodes)    // edges to modify when adding flow from source to destination
  private[this] val fractionToModify = Array.ofDim[Array[Double]](nNodes, nNodes)  // fraction of the flow to put on each corresponding edge
    
  private def initializeEdgesToModify() = {
    val toRoute = Array.fill(nNodes)(0.0)
    val fractionDAG = Array.ofDim[Double](nEdges)
    val edgesDAG = Array.ofDim[Int](nEdges)  // fraction of the flow on edges, basically a stack with nEdgesDAG pointing to top
    
    var nEdgesDAG = 0
    
    val Nodes = 0 until nNodes
    for (source <- Nodes) for (destination <- Nodes) {
      nEdgesDAG = 0
      
      val ordering = sp.topologicalOrdering
      var nOrdering = sp.makeTopologicalOrdering(source, destination)
      
      // simulate the routing of of flow of size 1.0 from source to destination
      toRoute(source) = 1.0
      while (nOrdering > 0) {
        nOrdering -= 1
        val node = ordering(nOrdering)
        
        var pSucc = sp.nSuccessors(destination)(node)
        val amountToRoute = toRoute(node) / pSucc
        while (pSucc > 0) {
          pSucc -= 1
          val succNode = sp.successorNodes(destination)(node)(pSucc)
          toRoute(succNode) += amountToRoute
          
          val succEdge = sp.successorEdges(destination)(node)(pSucc)
          edgesDAG(nEdgesDAG) = succEdge
          fractionDAG(nEdgesDAG) = amountToRoute
          nEdgesDAG += 1
        }
        
        toRoute(node) = 0.0
      }
      
      // copy edges from temp array to final one
      nEdgesToModify(source)(destination) = nEdgesDAG
      edgesToModify(source)(destination) = Array.ofDim[Int](nEdgesDAG)
      System.arraycopy(edgesDAG, 0, edgesToModify(source)(destination), 0, nEdgesDAG)
      
      // copy flow from temp array, reset array as we go
      fractionToModify(source)(destination) = Array.ofDim[Double](nEdgesDAG)
      var pEdge = nEdgesDAG
      while (pEdge > 0) {
        pEdge -= 1
        val edge = edgesDAG(pEdge)
        fractionToModify(source)(destination)(pEdge) = fractionDAG(pEdge)
      }
    }    
  }
  
  initializeEdgesToModify()
  
  def modify(source: Node, destination: Node, bw: Double): Unit = {
    val edges = edgesToModify(source)(destination)
    val fractions = fractionToModify(source)(destination)
    
    var pEdge = nEdgesToModify(source)(destination)
    while (pEdge > 0) {
      pEdge -= 1
      val edge = edges(pEdge)
      updateValue(edge, values(edge) + bw * fractions(pEdge))
    }
  }
    
  initialize()
  commitState()
}


// same as FlowStateRecomputeDAG, except that flow is recomputed only on commit, nothing is done on check, no message is passed
class FlowStateRecomputeDAGOnCommit(nNodes: Int, nEdges: Int, sp: ShortestPaths, pathState: PathState, demandsData: DemandsData, edgeDemandState: EdgeDemandState) 
extends ArrayStateDouble(nEdges)
{
  override def check() = { true }
  override def updateState() = {}

  override def commit() = {
    updateFlowState()
    super.commit()
  }
  
  protected def initialize() = {
    var demand = pathState.nDemands
    while (demand > 0) {
      demand -= 1
      val path = pathState.path(demand)
      var pos = pathState.size(demand) - 1
      
      while (pos > 0) {
        pos -= 1
        
        val source      = path(pos)
        val destination = path(pos + 1)
        
        modify(demand, source, destination, demandsData.demandTraffics(demand))
      }
    }
  }
  
  private def updateFlowState() = {
    // update my state according to last changes on pathState
    var pChanged = pathState.nChanged
    val changed = pathState.changed
    while (pChanged > 0) {
      pChanged -= 1
      val demand = changed(pChanged)
      val bandwidth = demandsData.demandTraffics(demand)
      
      // localize the place where changes took place
      val currentPath = pathState.path(demand)
      val currentSize = pathState.size(demand)
      val oldPath = pathState.oldPath(demand)
      val oldSize = pathState.oldSize(demand)
      
      // invariant: both paths have the same first element and the same last element, so minSize >= 2
      // compute firstdiff, the smallest index s.t. currentPath(_) != oldPath(_)
      val minSize = math.min(currentSize, oldSize)
      var firstDiff = 1                                                                             // moves can not change source, do not look at first element
      while (firstDiff < minSize && currentPath(firstDiff) == oldPath(firstDiff)) firstDiff += 1
      
      var endCurrent = currentSize - 2     // moves can not change destination, do not look at last element
      var endOld     = oldSize - 2
      
      while (firstDiff < endCurrent && firstDiff < endOld && currentPath(endCurrent) == oldPath(endOld)) {
        endCurrent -= 1
        endOld -= 1
      }
      
      //println(currentPath.slice(0, currentSize).mkString(", "))
      //println(oldPath.slice(0, oldSize).mkString(", "))
      //println((currentSize, oldSize, firstDiff, endCurrent, endOld)) 
      
      var p = firstDiff - 1
      while (p <= endCurrent) {
        //println(s"adding ${currentPath(p)} => ${currentPath(p+1)}")
        modify(demand, currentPath(p), currentPath(p+1), bandwidth)
        p += 1
      }

      var q = firstDiff - 1
      while (q <= endOld) {
        //println(s"removing ${oldPath(q)} => ${oldPath(q+1)}")
        modify(demand, oldPath(q), oldPath(q+1), -bandwidth)
        q += 1
      }
    }
  }
  
  private[this] val toRoute = Array.fill(nNodes)(0.0)

  // change the amount sent from source to destination by bw, which can be positive or negative. 
  def modify(demand: Demand, source: Node, destination: Node, bw: Double): Unit = {
    val successorNodes = sp.successorNodes(destination)
    val successorEdges = sp.successorEdges(destination)
    val nSuccessors = sp.nSuccessors(destination)
    
    // while there is only a simple path from source to destination, just push along the single path
    // this is designed to make the use a topological ordering to a minimum
    var source0 = source
    while (source0 != destination && nSuccessors(source0) == 1) {
      val edge = successorEdges(source0)(0)
      edgeDemandState.updateEdgeDemand(edge, demand, bw)
      updateValue(edge, values(edge) + bw)
      
      val next = successorNodes(source0)(0)
      source0 = next
    }

    // if job is not done by pushing along simple path,
    // then we make a topological order of nodes we'll have to traverse and push flow in that order  
    if (source0 != destination) {
      val ordering = sp.topologicalOrdering
      var pOrdering = sp.makeTopologicalOrdering(source, destination)
      toRoute(source0) = bw
      
      while (pOrdering > 0) {
        pOrdering -= 1
        val node = ordering(pOrdering)
        
        // push flow to edges, add flow to successor nodes' toRoute
        var pSucc = nSuccessors(node)
        val increment = toRoute(node) / pSucc
        toRoute(node) = 0.0
        
        while (pSucc > 0) {
          pSucc -= 1
          
          val succEdge = successorEdges(node)(pSucc)
          updateValue(succEdge, values(succEdge) + increment)
          edgeDemandState.updateEdgeDemand(succEdge, demand, increment)
          
          val succNode = successorNodes(node)(pSucc)
          toRoute(succNode) += increment
        }
        
      }
    }
  }
    
  initialize()
  super.commit()
}

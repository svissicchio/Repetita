package be.ac.ucl.ingi.defo.constraints.paths

import oscar.cp.core.CPPropagStrength
import be.ac.ucl.ingi.defo.core.variables.IncrPathVar
import be.ac.ucl.ingi.defo.constraints.PathConstraint
import be.ac.ucl.ingi.defo.paths.ConnectStructure
import oscar.algo.Inconsistency

class CanReach(val path: IncrPathVar, val connectStruct: ConnectStructure) extends PathConstraint(path.store, "CanReach") {

  override def setup(l: CPPropagStrength): Unit = {
    if (connectStruct.isStronglyConnected) deactivate()
    else {
      init()
      path.callVisitedWhenVisit(this)
    }
  }
    
  @inline
  private def init(): Unit = {
    val failed = path.visitedEdges.exists(edge => {
      !connectStruct.reachable(edge._1, edge._2)
    })
    if (failed) throw Inconsistency
    else visited(path, path.lastVisited, path.lastVisited)
  }
  
  @inline
  private def checkReachable(from: Int, to: Int): Unit = {
    if (!connectStruct.reachable(from, to)) path.remove(to)
    else if (!connectStruct.reachable(to, path.destId)) path.remove(to)
  }
  
  override def visited(path: IncrPathVar, from: Int, to: Int): Unit = {
    val possible = path.possible
    possible.foreach(n => checkReachable(to, n))
  }
}

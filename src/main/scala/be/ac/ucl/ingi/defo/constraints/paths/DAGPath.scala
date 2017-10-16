package be.ac.ucl.ingi.defo.constraints.paths

import oscar.cp.core.CPPropagStrength
import be.ac.ucl.ingi.defo.core.variables.IncrPathVar
import be.ac.ucl.ingi.defo.constraints.PathConstraint
import oscar.cp.core.variables.CPIntVar
import be.ac.ucl.ingi.defo.paths.ECMPStructure


class DAGPath(val path: IncrPathVar, edges: Array[CPIntVar], ecmpStruct: ECMPStructure) extends PathConstraint(path.store, "DAGPath") {

  override def setup(l: CPPropagStrength): Unit = {
    init()
    path.callVisitedWhenVisit(this)

  }

  private def init(): Unit = {
    assert(path.nVisited == 1)
  }

  override def visited(path: IncrPathVar, from: Int, to: Int): Unit = {
    val links = ecmpStruct.links(from, to)
    val nodes = links.map(e => ecmpStruct.topology.edgeSrc(e)).toSet ++ Set(to)
    for (n <- nodes if n != from) {
      val inEdges = ecmpStruct.topology.inEdges(n)
      for (e <- inEdges if !links.contains(e)) {
        edges(e).assign(0)
      }
    }
    for (n <- nodes if n != to) {
      val outEdges = ecmpStruct.topology.outEdges(n)
      for (e <- outEdges if !links.contains(e)) {
        edges(e).assign(0)
      }
    }
  }

}

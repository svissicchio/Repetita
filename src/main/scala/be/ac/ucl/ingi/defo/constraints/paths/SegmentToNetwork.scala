package be.ac.ucl.ingi.defo.constraints.paths

import oscar.cp.core.CPPropagStrength
import oscar.cp.core.variables.{CPIntVar, CPVar}
import be.ac.ucl.ingi.defo.core.variables.IncrPathVar
import be.ac.ucl.ingi.defo.constraints.PathConstraint
import be.ac.ucl.ingi.defo.paths.ECMPStructure
import oscar.algo.Inconsistency

class SegmentToNetwork(path: IncrPathVar, flows: Array[CPIntVar], ecmpStruct: ECMPStructure, traffic: Int) extends PathConstraint(path.store, "SegmentToNetwork") {

  @inline
  private def intFlow(from: Int, to: Int, link: Int): Int = {
    (ecmpStruct.flow(from, to, link) * traffic).ceil.toInt
  }
    
  override def setup(l: CPPropagStrength): Unit = {
    init()
    path.callVisitedWhenVisit(this)
  }

  private def init(): Unit = {
    val nVisited = path.nVisited
    var from = path.nodeAt(0)
    var i = 1
    while (i < nVisited) {
      val to = path.nodeAt(i)
      visited(path, from, to)
      from = to
      i += 1

    }
  }

  override def visited(path: IncrPathVar, from: Int, to: Int): Unit = {
    val links = ecmpStruct.links(from, to)
    links.foreach { l =>
      val flow = intFlow(from, to, l)
      flows(l).assign(flow)
    }
    if (path.isBound) {
      flows.filter(!_.isBound).foreach(_.assign((0)))
    }
  }

  override def associatedVars(): Iterable[CPVar] = Iterable[CPVar]()


}

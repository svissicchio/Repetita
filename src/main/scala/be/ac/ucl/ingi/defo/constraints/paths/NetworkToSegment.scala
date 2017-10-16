package be.ac.ucl.ingi.defo.constraints.paths

import oscar.cp.core.CPPropagStrength
import oscar.cp.core.variables.CPIntVar
import be.ac.ucl.ingi.defo.core.variables.IncrPathVar
import be.ac.ucl.ingi.defo.constraints.PathConstraint
import be.ac.ucl.ingi.defo.paths.ECMPStructure

class NetworkToSegment(val path: IncrPathVar, val flows: Array[CPIntVar], val ecmpStruct: ECMPStructure, val traffic: Int) extends PathConstraint(path.store, "NetworkToSegment") {

  override def setup(l: CPPropagStrength): Unit = {
    propagate()

    path.callVisitedWhenVisit(this)
    for (f <- flows if !f.isBound) {
      f.callPropagateWhenBoundsChange(this)
    }
  }
  
  override def propagate(): Unit = {
    val from = path.lastVisited
    val possible = path.possible
    var i = 0
    while (i < possible.length) {
      val to = possible(i)
      val links = ecmpStruct.links(from, to)
      for (link <- links) {
        val flow = math.ceil(ecmpStruct.flow(from, to, link) * traffic).toInt
        if (flow > flows(link).max) {
          path.remove(to)
        }
      }
      i += 1
    }
  }
  
  override def visited(path: IncrPathVar, from: Int, to: Int) = propagate()
}

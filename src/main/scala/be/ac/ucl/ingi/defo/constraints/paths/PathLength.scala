package be.ac.ucl.ingi.defo.constraints.paths

import oscar.cp.core.CPPropagStrength
import be.ac.ucl.ingi.defo.core.variables.IncrPathVar
import be.ac.ucl.ingi.defo.constraints.PathConstraint
import oscar.algo.Inconsistency
import oscar.cp.core.variables.CPVar

class PathLength(val path: IncrPathVar) extends PathConstraint(path.store, "PathLength") {

  idempotent = true
  
  override def setup(l: CPPropagStrength): Unit = {
    propagate()
    if (!path.isBound) {
      path.callVisitedWhenVisit(this)
      path.length.callPropagateWhenBoundsChange(this)
    }
  }
  
  @inline final override def propagate(): Unit = {
    val maxLength = path.length.max
    val minLength = path.length.min
    val nVisited = path.nVisited
    if (nVisited == maxLength - 1) path.removeAllBut(path.destId)
    else if (nVisited < minLength - 1) path.remove(path.destId)
    else if (nVisited > maxLength) throw Inconsistency
  }
  
  @inline final override def visited(path: IncrPathVar, from: Int, to: Int): Unit = {
    if (to == path.destId) {
      path.length.assign(path.nVisited)
    } else {
      path.length.updateMin(path.nVisited + 1)
    }
    propagate()
  }

  override def associatedVars(): Iterable[CPVar] = Iterable[CPVar]()
}

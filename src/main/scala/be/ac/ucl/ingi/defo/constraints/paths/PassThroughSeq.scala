package be.ac.ucl.ingi.defo.constraints.paths

import oscar.cp.core.CPPropagStrength
import be.ac.ucl.ingi.defo.constraints.PathConstraint
import oscar.algo.reversible.ReversibleInt
import oscar.cp.core.variables.CPIntVar
import be.ac.ucl.ingi.defo.core.variables.IncrPathVar
import oscar.algo.Inconsistency

class PassThroughSeq(path: IncrPathVar, seqNodes: Array[Array[Int]]) extends PathConstraint(path.store, "PassThrough") {

  idempotent = true
  
  private[this] val seqSet = seqNodes.map(_.toSet)
  private[this] val step = new ReversibleInt(s, 0)

  override def setup(l: CPPropagStrength) = {
    path.callVisitedWhenVisit(this)
    path.length.callUpdateBoundsWhenBoundsChange(this)

  }

  @inline final override def visited(path: IncrPathVar, from: Int, to: Int) = {
    if (seqSet(step.value).contains(to)) step.incr()
    if (step.value == seqSet.length) deactivate()
    else checkLength()
  }
  
  @inline final override def propagate() = {
    val visited = path.visited 
    var i = 0
    var s = step.value
    while (i < visited.length && s < seqSet.length) {
      if (seqSet(s).contains(visited(i))) {
        s += 1
      }
      i += 1
    }
    step.value = s
    if (s == seqSet.length) deactivate()
    else if (i >= path.length.max) throw Inconsistency
    else checkLength()
  }
  
  @inline final override def updateBounds(intvar: CPIntVar) = checkLength()

  @inline private def checkLength(): Unit = {
    val maxLength = path.length.max
    val nVisited = path.nVisited
    val delta = maxLength - nVisited
    val nRemaining = seqSet.length - step.value
    if (delta == 0) propagate()
    else if (delta == 1 && this.isActive) throw Inconsistency
    else if (delta > nRemaining) return
    else {
      val possibles = path.possible
      var i = 0
      while (i < possibles.length) {
        val candidate = possibles(i)
        if (!seqSet(step.value).contains(candidate)) {
          path.remove(candidate)
        }
        i += 1
      }
    }
  }
}

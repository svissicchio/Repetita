package be.ac.ucl.ingi.defo.constraints.paths

import oscar.cp.core.CPPropagStrength
import be.ac.ucl.ingi.defo.core.variables.IncrPathVar
import be.ac.ucl.ingi.defo.constraints.PathConstraint
import oscar.algo.Inconsistency
import oscar.cp.core.variables.CPIntVar

class PassThrough(path: IncrPathVar, nodes: Set[Int]) extends PathConstraint(path.store, "PassThrough") {

  idempotent = true
  
  final override def hasPreference: Boolean = isActive
  
  final override val preferences: Array[Int] = nodes.toArray

  override def setup(l: CPPropagStrength): Unit = {
    propagate()
    if (isActive) {
      path.callVisitedWhenVisit(this)
      path.length.callUpdateBoundsWhenBoundsChange(this)
    }
  }

  @inline final override def visited(path: IncrPathVar, from: Int, to: Int): Unit = {
    if (nodes.contains(to)) deactivate()
    else checkLength()
  }
  
  @inline final override def propagate(): Unit = {
    val visited = path.visited 
    var i = 0
    var ok = false
    while (i < visited.length && !ok) {
      ok = nodes.contains(visited(i))
      i += 1
    }
    if (ok) deactivate()
    else if (i >= path.length.max) throw Inconsistency
    else checkLength()
  }
  
  @inline final override def updateBounds(intvar: CPIntVar): Unit = checkLength()

  @inline private def checkLength(): Unit = {
    val maxLength = path.length.max
    val nVisited = path.nVisited
    val delta = maxLength - nVisited
    if (delta == 0) propagate()
    else if (delta == 1 && this.isActive) throw Inconsistency
    else if (delta != 2) return
    else {
      val possibles = path.possible
      var i = 0
      while (i < possibles.length) {
        val candidate = possibles(i)
        if (!nodes.contains(candidate)) {
          path.remove(candidate)
        }
        i += 1
      }
    }
  }


}

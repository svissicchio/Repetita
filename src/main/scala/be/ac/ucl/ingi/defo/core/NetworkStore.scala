package be.ac.ucl.ingi.defo.core

import oscar.cp.core.CPStore
import oscar.cp.core.CPPropagStrength
import be.ac.ucl.ingi.defo.core.variables.PathEventListener
import be.ac.ucl.ingi.defo.core.variables.IncrPathVar
import oscar.cp.core.CPSolver

class NetworkStore(propagStrength: CPPropagStrength) extends CPSolver(propagStrength) {
  
  @inline final def visit(pathVar: IncrPathVar, nodeId: Int): Unit = {
    ???
  }
  
  @inline final def remove(pathVar: IncrPathVar, nodeId: Int): Unit = {
    ???
  }

  final def notifyVisited(listener: PathEventListener, path: IncrPathVar, srcId: Int, sinkId: Int): Unit = {
    var e = listener
    while (e != null) {
      val c = e.constraint
      if (c.isActive) {
        enqueueL1(c, e.priority, c.visited(path, srcId, sinkId))
      }
      e = e.next
    }
  }

  final def notifyPathEvent(listener: PathEventListener, filtering: => Unit): Unit = {
    var e = listener
    while (e != null) {
      val c = e.constraint
      if (c.isActive) {
        enqueueL1(c, e.priority, filtering)
      }
      e = e.next
    }
  }

  final def notifyForbidden(listener: PathEventListener, path: IncrPathVar, nodeId: Int): Unit = {
    var e = listener
    while (e != null) {
      val c = e.constraint
      if (c.isActive) {
        enqueueL1(c, e.priority, c.forbidden(path, nodeId))
      }
      e = e.next
    }
  }
}

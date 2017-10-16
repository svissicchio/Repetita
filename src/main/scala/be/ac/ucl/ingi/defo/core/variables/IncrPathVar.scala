package be.ac.ucl.ingi.defo.core.variables

import oscar.algo.reversible.ReversibleInt
import oscar.algo.reversible.ReversiblePointer
import oscar.cp.core.variables.CPIntVar
import be.ac.ucl.ingi.defo.constraints.PathConstraint
import be.ac.ucl.ingi.defo.constraints.paths.PathLength
import be.ac.ucl.ingi.defo.core.NetworkStore
import oscar.algo.Inconsistency


class IncrPathVar(final val store: NetworkStore, final val origId: Int, final val destId: Int, nNodes: Int, final val name: String = "PATH_VAR") extends Traversable[Int] {

  // Registered constraints
  private final val visitedListener = new ReversiblePointer[PathEventListener](store, null)
  private final val removedListener = new ReversiblePointer[PathEventListener](store, null)
  
  final def preferences: Array[Int] = {
    val prefered = new Array[Int](nNodes)
    var node = visitedListener.value
    while (node != null) {
      val constraint = node.constraint
      if (constraint.hasPreference) {
        val preferences = constraint.preferences
        var i = 0
        while (i < preferences.length) {
          prefered(preferences(i)) = 1
          i += 1
        }
      }
      node = node.next
    }
    node = removedListener.value
    while (node != null) {
      val constraint = node.constraint
      if (constraint.hasPreference) {
        val preferences = constraint.preferences
        var i = 0
        while (i < preferences.length) {
          prefered(preferences(i)) = 1
          i += 1
        }
      }
      node = node.next
    }
    prefered
  }

  // Domain representation
  private final val nodes: Array[Int] = Array.tabulate(nNodes)(i => i)
  private final val positions: Array[Int] = Array.tabulate(nNodes)(i => i)

  // Nodes with a position greater or equal to removedPtr are removed
  private final val removedPtr: ReversibleInt = new ReversibleInt(store, nNodes)
  // Nodes with a position lesser or equal to visitedPtr are visited
  private final val visitedPtr: ReversibleInt = new ReversibleInt(store, 0)

  // Setups the origin
  nodes(origId) = 0
  nodes(0) = origId
  positions(0) = origId
  positions(origId) = 0

  /**
   *  The integer variable that represents the length of the path.
   *  Initial domain: [2, nNodes]
   */
  final val length: CPIntVar = CPIntVar(2, nNodes)(store)

  // Adds the length constraint
  store.add(new PathLength(this))

  /** Returns the number of visited nodes origin included. */
  final def nVisited: Int = visitedPtr.value + 1

  /** Returns the number of possible nodes */
  final def nPossible: Int = removedPtr.value - visitedPtr.value - 1

  /** Returns true if the path has reached its destination */
  final def isBound: Boolean = nodes(visitedPtr.value) == destId

  final def callVisitedWhenVisit(c: PathConstraint): Unit = {
    visitedListener.value = new PathEventListener(visitedListener.value, c, c.priorityBindL1)
  }

  /** Removes the node from the next possible nodes. */
  final def remove(nodeId: Int): Unit = {
    val p1 = positions(nodeId)
    if (p1 <= visitedPtr.value) return
    else if (p1 >= removedPtr.value) return
    else if (nPossible == 1) {
      removedPtr.decr()
      throw Inconsistency
    }
    else {
      removedPtr.decr()
      // Swap the value
      val p2 = removedPtr.value
      val v2 = nodes(p2)
      nodes(p1) = v2
      nodes(p2) = nodeId
      positions(v2) = p1
      positions(nodeId) = p2
      // AC5 propagation
      store.notifyForbidden(visitedListener.value, this, nodeId)
    }
  }

  /** Removes all nodes from the next possible except nodeId. */
  final def removeAllBut(nodeId: Int): Unit = {
    val p1 = positions(nodeId)
    if (p1 <= visitedPtr.value) throw Inconsistency // the node is not possible
    else if (p1 >= removedPtr.value) throw Inconsistency // the node is not possible
    else if (nPossible == 1) return
    else {
      // Swap nodes 
      // WARNING: Do not increment visitedPtr, it 
      // can only be done by the visit function
      val p2 = visitedPtr.value + 1
      val v2 = nodes(p2)
      nodes(p1) = v2
      nodes(p2) = nodeId
      positions(v2) = p1
      positions(nodeId) = p2
      // Removes all other nodes
      removedPtr.value = p2 + 1
      // AC5 propagation
      // TODO: notify all removed nodes
    }
  }

  final def visit(nodeId: Int): Unit = {
    if (isBound) throw Inconsistency
    else {
      // AC5 propagation
      store.notifyVisited(visitedListener.value, this, lastVisited, nodeId)
      // Swap nodes
      visitedPtr.incr()
      val p1 = positions(nodeId)
      val p2 = visitedPtr.value
      val v2 = nodes(p2)
      nodes(p1) = v2
      nodes(p2) = nodeId
      positions(v2) = p1
      positions(nodeId) = p2    
      // Reset possible nodes
      if (nodeId != destId) removedPtr.value = nNodes
      // Remove all possible nodes
      else {
        removedPtr.value = p2 + 1
        // TODO: notify all removed nodes
      }
    }
  }

  /** Returns the id of the last visited node */
  final def lastVisited: Int = nodes(visitedPtr.value)

  /** Returns true if the node can be visited after lastVisited */
  final def isPossible(nodeId: Int): Boolean = {
    val p = positions(nodeId)
    p > visitedPtr.value && p < removedPtr.value
  }

  final def hasVisited(nodeId: Int): Boolean = {
    if (nodeId == origId) true
    else positions(nodeId) <= visitedPtr.value
  }

  final def hasVisited(from: Int, to: Int): Boolean = {
    val p1 = positions(from)
    if (p1 >= visitedPtr.value) false
    else nodes(p1 + 1) == to
  }

  final def position(nodeId: Int): Int = {
    val p = positions(nodeId)
    if (p <= visitedPtr.value) p
    else sys.error("node not visited")
  }

  final def nodeAt(position: Int): Int = {
    if (position <= visitedPtr.value) nodes(position)
    else sys.error("position >= nVisited")
  }

  override def foreach[B](fun: Int => B): Unit = {
    var i = visitedPtr.value + 1
    while (i < removedPtr.value) {
      fun(nodes(i))
      i += 1
    }
  }
  
  final def visited: Array[Int] = Array.tabulate(nVisited)(i => nodes(i))
  
  final def visitedEdges: Iterator[(Int, Int)] = new Iterator[(Int, Int)] {
    var i = 0
    override def hasNext: Boolean = i < visitedPtr.value
    override def next(): (Int, Int) = {
      if (i < visitedPtr.value) throw new NoSuchElementException("next on empty iterator")
      else {
        val edge = (nodes(i), nodes(i+1))
        i += 1
        edge
      }
    }
  }
  
  final def possible: Array[Int] = {
    val n = nPossible
    val possible = Array.ofDim[Int](n)
    val offset = visitedPtr.value + 1
    var i = 0
    while (i < n) {
      possible(i) = nodes(i+offset)
      i += 1
    }
    possible
  }

  override def toString: String = name + ": " + nodes.take(visitedPtr.value + 1).mkString(" -> ") + " | " + mkString(", ")
}

object IncrPathVar {
  def apply(origId: Int, destId: Int, nNodes: Int, name: String = "PATH_VAR")(implicit store: NetworkStore): IncrPathVar = {
    new IncrPathVar(store, origId, destId, nNodes, name)
  }  
}

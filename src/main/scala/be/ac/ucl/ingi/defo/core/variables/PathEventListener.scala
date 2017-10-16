package be.ac.ucl.ingi.defo.core.variables

import be.ac.ucl.ingi.defo.constraints.PathConstraint

class PathEventListener(final val next: PathEventListener, final val constraint: PathConstraint, final val priority: Int) { 
  /** Returns true if there is another event in the stack */
  @inline final def hasNext: Boolean = next != null
  final override def toString: String = s"PathEvent(constraint: $constraint)"
}

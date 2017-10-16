package be.ac.ucl.ingi.defo.constraints

import be.ac.ucl.ingi.defo.core.variables.IncrPathVar
import oscar.cp.core.Constraint
import oscar.cp.core.CPStore
import oscar.cp.core.variables.CPVar

abstract class PathConstraint(s: CPStore, name: String = "PathConstraint") extends Constraint(s, name) {
  
  /** Called when pathVar use the link src -> sink */
  def visited(pathVar: IncrPathVar, srcId: Int, sinkId: Int): Unit = {}
  
  /** Called when a possible node becomes forbidden */
  def forbidden(pathVar: IncrPathVar, nodeId: Int): Unit = {}
  
  def hasPreference: Boolean = false
  
  def preferences: Array[Int] = Array.empty

  override def associatedVars(): Iterable[CPVar] = Iterable[CPVar]()
}

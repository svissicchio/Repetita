package be.ac.ucl.ingi.defo.constraints.paths

import be.ac.ucl.ingi.defo.core.variables.IncrPathVar
import oscar.cp.core.Constraint
import oscar.cp.core.CPPropagStrength
import oscar.cp.core.variables.CPVar

class Remove(path: IncrPathVar, nodeId: Int) extends Constraint(path.store, "Remove") {
  override def setup(l: CPPropagStrength): Unit = {
    path.remove(nodeId)
    deactivate()
  }

  override def associatedVars(): Iterable[CPVar] = Iterable[CPVar]()
}

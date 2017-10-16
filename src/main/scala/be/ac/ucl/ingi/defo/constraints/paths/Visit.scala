package be.ac.ucl.ingi.defo.constraints.paths

import be.ac.ucl.ingi.defo.core.variables.IncrPathVar
import oscar.cp.core.Constraint
import oscar.cp.core.CPPropagStrength
import oscar.cp.core.variables.CPVar

/** 
 *  WARNING:
 *  
 *  - The setup function does not check that nodeId was a 
 *    valid successor when the constraint has been posted.
 *    This could be a problem in case of optimization. 
 *    To avoid this problem, a constraint should be able to 
 *    perform two thing: validate the visit event or not 
 *    and propagate if the event is valid.
 *    
 *  - This constraint should only be used in search. 
 */
class Visit(path: IncrPathVar, nodeId: Int) extends Constraint(path.store, "Visit") {
  override def setup(l: CPPropagStrength): Unit = {
    path.visit(nodeId)
  }

  override def associatedVars(): Iterable[CPVar] = Iterable[CPVar]()
}

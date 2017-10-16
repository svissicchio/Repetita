package defo.modeling.variables

import be.ac.ucl.ingi.defo.modeling.units.RelativeUnit
import be.ac.ucl.ingi.defo.modeling.units.LoadUnit
import be.ac.ucl.ingi.defo.modeling.DEFOLowerLoad
import be.ac.ucl.ingi.defo.modeling.DEFOConstraint
import be.ac.ucl.ingi.defo.modeling.DEFOLowerEqLoad

class DEFOLoadVar(final val edgeId: Int) {
  
  def <(load: LoadUnit): DEFOConstraint = new DEFOLowerLoad(edgeId, load.value, false)
  def <=(load: LoadUnit): DEFOConstraint = new DEFOLowerEqLoad(edgeId, load.value, false)
  
  def <(percent: RelativeUnit): DEFOConstraint = new DEFOLowerLoad(edgeId, percent.value, true)
  def <=(percent: RelativeUnit): DEFOConstraint = new DEFOLowerEqLoad(edgeId, percent.value, true)
  
  def >(load: LoadUnit): DEFOConstraint = ???
  def >=(load: LoadUnit): DEFOConstraint = ???
  
  def >(percent: RelativeUnit): DEFOConstraint = ???
  def >=(percent: RelativeUnit): DEFOConstraint = ???
}

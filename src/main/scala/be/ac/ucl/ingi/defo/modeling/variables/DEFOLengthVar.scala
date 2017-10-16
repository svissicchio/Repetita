package be.ac.ucl.ingi.defo.modeling.variables

import be.ac.ucl.ingi.defo.modeling.DEFOLowerLength
import be.ac.ucl.ingi.defo.modeling.DEFOConstraint
import be.ac.ucl.ingi.defo.modeling.DEFOLowerEqLength

class DEFOLengthVar(demandId: Int) {
  def <(length: Int): DEFOConstraint = new DEFOLowerLength(demandId, length + 2)
  def <=(length: Int): DEFOConstraint = new DEFOLowerEqLength(demandId, length + 2)
  
  def >(length: Int): DEFOConstraint = ???
  def >=(length: Int): DEFOConstraint = ???
}

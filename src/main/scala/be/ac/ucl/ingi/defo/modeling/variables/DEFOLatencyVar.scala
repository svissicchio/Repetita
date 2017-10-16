package be.ac.ucl.ingi.defo.modeling.variables

import be.ac.ucl.ingi.defo.modeling.units.RelativeUnit
import be.ac.ucl.ingi.defo.modeling.units.TimeUnit
import be.ac.ucl.ingi.defo.modeling.DEFOLowerLatency
import be.ac.ucl.ingi.defo.modeling.DEFOConstraint
import be.ac.ucl.ingi.defo.modeling.DEFOLowerEqLatency

class DEFOLatencyVar(final val demandId: Int) {
  
  def <(latency: TimeUnit): DEFOConstraint = new DEFOLowerLatency(demandId, latency.value, false)
  def <=(latency: TimeUnit): DEFOConstraint = new DEFOLowerEqLatency(demandId, latency.value, false)
  
  def <(percent: RelativeUnit): DEFOConstraint = new DEFOLowerLatency(demandId, percent.value, true)
  def <=(percent: RelativeUnit): DEFOConstraint = new DEFOLowerEqLatency(demandId, percent.value, true)
  
  def >(latency: TimeUnit): DEFOConstraint = ???
  def >=(latency: TimeUnit): DEFOConstraint = ???
  
  def >(percent: RelativeUnit): DEFOConstraint = ???
  def >=(percent: RelativeUnit): DEFOConstraint = ???
}

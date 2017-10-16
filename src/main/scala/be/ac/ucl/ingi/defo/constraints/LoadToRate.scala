package be.ac.ucl.ingi.defo.constraints

import oscar.cp.core.variables.{CPIntVar, CPVar}
import oscar.cp.core.Constraint
import oscar.cp.core.CPPropagStrength
class LoadToRate(val load: CPIntVar, val rate: CPIntVar, val step: Int) extends Constraint(load.store, "LoadToRoad") {

  override def setup(l: CPPropagStrength): Unit = {
    init()
    rate.callUpdateBoundsIdxWhenBoundsChange(this, 0)
    load.callUpdateBoundsIdxWhenBoundsChange(this, 1)

  }
  
  private def init(): Unit = {
    updatedRate()
    updatedLoad()
  }
  
  override def updateBoundsIdx(x: CPIntVar, id: Int): Unit = {
    if (id == 0) updatedRate()
    else updatedLoad()
  }
  
  @inline
  private def updatedRate(): Unit = {
    val min = load.min / step
    if (min < rate.min) {
      load.updateMin(step * rate.min)
    }
    else {
      val max = load.max / step
      if (max > rate.max) {
        load.updateMax(step * (rate.max+1) - 1)
      }
    }
  }
  
  @inline
  private def updatedLoad(): Unit = {
    val min = load.min / step
    rate.updateMin(min)
    val max = load.max / step
    rate.updateMax(max)

  }

  override def associatedVars(): Iterable[CPVar] = Iterable[CPVar](load,rate)

}

package be.ac.ucl.ingi.defo.modeling.units

class RelativeUnit(final val value: Int)

class RelativeUnitBuilder(val percent: Int) extends AnyVal {
  @inline final def pct: RelativeUnit = new RelativeUnit(percent)
}

object RelativeUnitBuilder {
  implicit final def intToRelativeUnitBuilder(percent: Int): RelativeUnitBuilder = new RelativeUnitBuilder(percent)
}

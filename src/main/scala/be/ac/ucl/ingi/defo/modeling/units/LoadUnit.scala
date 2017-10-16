package be.ac.ucl.ingi.defo.modeling.units

class LoadUnit(final val value: Int, final override val toString: String)

class LoadUnitBuilder(val load: Int) extends AnyVal {
  @inline final def kbps: LoadUnit = new LoadUnit(load, s"$load kbps")
  @inline final def Mbps: LoadUnit = new LoadUnit(load << 10, s"$load Mbps")
  @inline final def Gbps: LoadUnit = new LoadUnit(load << 20, s"$load Gbps")
}

object LoadUnitBuilder {
  implicit final def intToLoadUnitBuilder(load: Int): LoadUnitBuilder = {
    if (load < 0) sys.error("maxLinkLoad has to be higher or equal to 0.")
    else new LoadUnitBuilder(load)
  }
}

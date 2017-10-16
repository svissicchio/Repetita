package be.ac.ucl.ingi.defo.modeling.units

class TimeUnit(val value: Int, final override val toString: String)

class TimeUnitBuilder(val time: Int) extends AnyVal {
  @inline final def minutes: TimeUnit = new TimeUnit(time * 60000, s"$time min")
  @inline final def s: TimeUnit = new TimeUnit(time * 1000, s"$time s")
  @inline final def ms: TimeUnit = new TimeUnit(time, s"$time ms")
}

object TimeUnitBuilder {
  implicit final def intToTimeUnitBuilder(time: Int): TimeUnitBuilder = {
    if (time < 0) sys.error("time has to be higher or equal to 0.")
    else new TimeUnitBuilder(time)
  }
}

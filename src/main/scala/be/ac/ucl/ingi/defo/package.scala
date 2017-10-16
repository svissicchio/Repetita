package be.ac.ucl.ingi

import be.ac.ucl.ingi.defo.modeling.units.LoadUnitBuilder
import be.ac.ucl.ingi.defo.modeling.units.RelativeUnitBuilder
import be.ac.ucl.ingi.defo.modeling.units.TimeUnitBuilder

package object defo {
  
  // Type aliases
  // ------------
  
  type MRProblem = be.ac.ucl.ingi.defo.modeling.MRProblem
  
  type DEFOptimizer = be.ac.ucl.ingi.defo.modeling.DEFOptimizer
  final val DEFOptimizer = be.ac.ucl.ingi.defo.modeling.DEFOptimizer
  
  type DEFOInstance = be.ac.ucl.ingi.defo.core.DEFOInstance
  final val DEFOInstance = be.ac.ucl.ingi.defo.core.DEFOInstance
  
  
  // Notification functions
  // ----------------------

  private[this] var actWarning: Boolean = true
  private[this] var actInfo: Boolean = true
  
  /** */
  final def activateWarnings: Boolean = actWarning
    
  /** */
  final def activateWarnings_=(b: Boolean): Unit = actWarning = b
    
  /** */
  final def activateInfos: Boolean = actInfo
    
  /** */
  final def activateInfos_=(b: Boolean): Unit = actInfo = b
    
  /** */
  @inline final def info[@specialized T](message: T): Unit = {
    if (actInfo) println(s"[info] $message")
  }
    
  /** */
  @inline final def warning[@specialized T](message: T): Unit = {
    if (actWarning) println(s"[warning] $message")
  }
  
  
  // Implicit unit conversion
  // ------------------------
  
  implicit final def intToLoadUnitBuilder(load: Int): LoadUnitBuilder = {
    if (load < 0) sys.error("maxLinkLoad has to be higher or equal to 0.")
    else new LoadUnitBuilder(load)
  }
  
  implicit final def intToTimeUnitBuilder(time: Int): TimeUnitBuilder = {
    if (time < 0) sys.error("time has to be higher or equal to 0.")
    else new TimeUnitBuilder(time)
  }
  
  implicit final def intToRelativeUnitBuilder(percent: Int): RelativeUnitBuilder = {
    if (percent < 0) sys.error("time has to be higher or equal to 0.")
    else new RelativeUnitBuilder(percent)
  }

}

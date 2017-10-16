package be.ac.ucl.ingi.rls.state

import scala.reflect.ClassTag

/*
 *  Many States are basically arrays of reversible values: 
 *  this class maintains such arrays of values, allowing to update/commit/revert and remembering deltas. 
 */ 

abstract class ArrayState[@specialized(Double, Int) T : ClassTag](nElements: Int)  
extends TrialState
{
  final val values    = Array.ofDim[T](nElements)
  final val oldValues = Array.ofDim[T](nElements)
  
  private[this] val deltaMarker = Array.fill(nElements)(false)
  
  private[this] var nDelta_ = 0
  private[this] val deltaElements_  = Array.ofDim[Int](nElements)
  
  protected def updateValue(element: Int, newValue: T) = {
    if (!deltaMarker(element)) {
      deltaMarker(element) = true 
      deltaElements_(nDelta_) = element
      nDelta_ += 1
    }
    values(element) = newValue
  }
  
  def deltaElements = deltaElements_
  def nDelta = nDelta_
  
  // remove change markers, resets nDelta to 0, restore old values
  override def revertState() = {
    while (nDelta_ > 0) {
      nDelta_ -= 1
      val element = deltaElements_(nDelta_)
      deltaMarker(element) = false
      values(element) = oldValues(element)
    }
  }
  
  // remove change markers, resets nDelta to 0
  override def commitState() = {
    while (nDelta_ > 0) {
      nDelta_ -= 1
      val element = deltaElements_(nDelta_)
      deltaMarker(element) = false
      oldValues(element) = values(element)
    }
  }
}


abstract class ArrayStateDouble(nElements: Int)  
extends TrialState
{
  private type T = Double
  final val values    = Array.ofDim[T](nElements)
  final val oldValues = Array.ofDim[T](nElements)
  
  private[this] val deltaMarker = Array.fill(nElements)(false)
  
  private[this] var nDelta_ = 0
  private[this] val deltaElements_  = Array.ofDim[Int](nElements)
  
  protected def updateValue(element: Int, newValue: T) = {
    if (!deltaMarker(element)) {
      deltaMarker(element) = true 
      deltaElements_(nDelta_) = element
      nDelta_ += 1
    }
    values(element) = newValue
  }
  
  def deltaElements = deltaElements_
  def nDelta = nDelta_
  
  // remove change markers, resets nDelta to 0, restore old values
  override def revertState() = {
    while (nDelta_ > 0) {
      nDelta_ -= 1
      val element = deltaElements_(nDelta_)
      deltaMarker(element) = false
      values(element) = oldValues(element)
    }
  }
  
  // remove change markers, resets nDelta to 0
  override def commitState() = {
    while (nDelta_ > 0) {
      nDelta_ -= 1
      val element = deltaElements_(nDelta_)
      deltaMarker(element) = false
      oldValues(element) = values(element)
    }
  }
}

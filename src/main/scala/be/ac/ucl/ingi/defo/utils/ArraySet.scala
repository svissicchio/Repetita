package be.ac.ucl.ingi.defo.utils

class ArraySet(nElems: Int) {
  
  private[this] val elements = Array.tabulate(nElems)(i => i)
  private[this] val positions = Array.tabulate(nElems)(i => i)
  private[this] var nInside = 0
  
  @inline final def size: Int = nInside
  @inline final def isEmpty: Boolean = nInside == 0
  
  @inline final def apply(idx: Int): Int = {
    if (idx < nInside) elements(idx)
    else throw new IndexOutOfBoundsException
  }
  
  @inline final def add(elem: Int): Unit = {
    val position = positions(elem)
    if (position >= nInside) {
      val elem2 = elements(nInside)
      elements(position) = elem2
      elements(nInside) = elem
      positions(elem2) = position
      positions(elem) = nInside
      nInside += 1
    }
  }
  
  @inline final def contains(elem: Int): Boolean = positions(elem) < nInside
}

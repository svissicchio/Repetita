package be.ac.ucl.ingi.defo.utils

import scala.annotation.tailrec

/** @author Renaud Hartert ren.hartert@gmail.com */

final class ArrayHeapInt(maxSize: Int) {

  private[this] val values: Array[Int] = new Array[Int](maxSize + 1)
  private[this] val keys: Array[Int] = new Array[Int](maxSize + 1)
  private[this] var heapSize: Int = 0

  def minValue: Int = {
    if (heapSize != 0) values(1)
    else throw new NoSuchElementException("empty")
  }

  def minKey: Int = {
    if (heapSize != 0) keys(1)
    else throw new NoSuchElementException("empty")
  }

  def size: Int = heapSize

  def isEmpty: Boolean = heapSize == 0

  def clear(): Unit = heapSize = 0

  def enqueue(key: Int, value: Int): Unit = {
    if (heapSize == maxSize) throw new Exception("the heap is full")
    else {
      heapSize += 1
      keys(heapSize) = key
      values(heapSize) = value
      heapifyBottomUp(heapSize)
    }
  }

  def dequeue(): Int = {
    if (heapSize == 0) throw new NoSuchElementException("empty")
    else {
      val value = values(1)
      values(1) = values(heapSize)
      keys(1) = keys(heapSize)
      heapSize -= 1
      heapifyTopDown(1)
      value
    }
  }

  def remove(key: Int, value: Int): Boolean = {
    if (heapSize == 0) false
    else {
      val id = search(1, key, value)
      if (id > heapSize) false
      else {
        values(id) = values(heapSize)
        keys(id) = keys(heapSize)
        heapSize -= 1
        heapifyTopDown(id)
        true
      }
    }
  }

  def changeKey(oldKey: Int, newKey: Int, value: Int): Boolean = {
    if (heapSize == 0) false
    else {
      val i = search(1, oldKey, value)
      if (i > heapSize) false
      else {
        keys(i) = newKey
        if (i == 1) heapifyTopDown(1)
        else if (newKey < keys(i >> 1)) heapifyBottomUp(i) // i >> 1 = parent
        else heapifyTopDown(i)
        true
      }
    }
  }

  @inline @tailrec private def search(id: Int, key: Int, value: Int): Int = {
    if (id > heapSize) id
    else if (keys(id) == key && values(id) == value) id
    else search(id + 1, key, value)
  }

  @inline @tailrec private def heapifyTopDown(i: Int): Unit = {
    val min = minChild(i)
    if (min != i) {
      val tmpValue = values(i)
      val tmpKey = keys(i)
      values(i) = values(min)
      keys(i) = keys(min)
      values(min) = tmpValue
      keys(min) = tmpKey
      heapifyTopDown(min)
    }
  }

  @inline @tailrec private def heapifyBottomUp(i: Int): Unit = {
    if (i > 1) {
      val p = i >> 1 // parent
      if (keys(p) > keys(i)) {
        val tmpValue = values(i)
        val tmpKey = keys(i)
        values(i) = values(p)
        keys(i) = keys(p)
        values(p) = tmpValue
        keys(p) = tmpKey
        heapifyBottomUp(p)
      }
    }
  }

  @inline private def minChild(i: Int): Int = {
    val l = i << 1 // left child
    val r = l + 1 // right child
    var min = i
    if (l <= heapSize) {
      if (keys(l) < keys(i)) min = l
      else min = i
      if (r <= heapSize && keys(r) < keys(min)) {
        min = r
      }
    }
    min
  }

  override def toString: String = "ArrayHeap(" + values.drop(1).take(heapSize).mkString(", ") + ")"
}

object TestHeap extends App {

  val heap = new ArrayHeapInt(10)
  heap.enqueue(9, 4)
  heap.enqueue(1000000, 5)
  heap.enqueue(2, 2)
  heap.enqueue(4, 3)
  heap.enqueue(-135, 1)
  heap.changeKey(1000000, 0, 5)

  while (!heap.isEmpty) {
    val value = heap.minValue
    val key = heap.minKey
    println(key + " : " + value)
    heap.remove(key, value)
  }
}

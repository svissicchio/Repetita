package be.ac.ucl.ingi.defo.utils

class BinaryHeap[@specialized(Int) V](val maxSize: Int) {

  case class Node(key: Int, value: V)

  private val heap: Array[Node] = Array.fill(maxSize + 1)(null)

  private var heapSize: Int = 0

  def head: V = heap(1).value

  def size: Int = heapSize

  def isEmpty: Boolean = heapSize == 0

  def removeAll(): Unit = heapSize = 0

  def enqueue(key: Int, value: V): Unit = {
    if (heapSize == maxSize) throw new Exception("the heap is full")
    else {
      heapSize += 1
      heap(heapSize) = Node(key, value)
      heapifyBottomUp(heapSize)
    }
  }

  def dequeue(): V = {
    if (isEmpty) throw new NoSuchElementException("empty")
    else {
      val min: Node = heap(1)
      heap(1) = heap(heapSize)
      heap(heapSize) = null
      heapSize -= 1
      heapifyTopDown(1)
      min.value
    }
  }

  def remove(key: Int, value: V): Boolean = {
    if (isEmpty) false
    else {
      val id = search(1, key, value)
      if (id > heapSize) false
      else {
        heap(id) = heap(heapSize)
        heapSize -= 1
        heapifyTopDown(id)
        true
      }
    }
  }

  def changeKey(oldKey: Int, newKey: Int, value: V): Boolean = {
    if (isEmpty) false
    else {
      val i = search(1, oldKey, value)
      if (i > heapSize) false
      else {
        heap(i) = Node(newKey, value)
        if (i == 1) heapifyTopDown(1)
        else if (newKey < heap(parent(i)).key) heapifyBottomUp(i)
        else heapifyTopDown(i)
        true
      }
    }
  }

  @annotation.tailrec
  private def search(id: Int, key: Int, value: V): Int = {
    if (id > heapSize) id
    else if (heap(id).key == key && heap(id).value == value) id
    else search(id + 1, key, value)
  }

  @annotation.tailrec
  private def heapifyTopDown(i: Int): Unit = {
    val min = minSon(i)
    if (min != i) {
      val temp = heap(i)
      heap(i) = heap(min)
      heap(min) = temp
      heapifyTopDown(min)
    }
  }

  @annotation.tailrec
  private def heapifyBottomUp(i: Int): Unit = {
    if (i > 1) {
      val p = parent(i)
      if (heap(p).key > heap(i).key) {
        val temp = heap(i)
        heap(i) = heap(p)
        heap(p) = temp
        heapifyBottomUp(p)
      }
    }
  }

  private def minSon(i: Int): Int = {
    val l = left(i)
    val r = right(i)
    var min = i
    if (l <= heapSize) {
      if (heap(l).key < heap(i).key) min = l
      else min = i
      if (r <= heapSize && heap(r).key < heap(min).key) {
        min = r
      }
    }
    min
  }

  @inline
  private def parent(i: Int) = i / 2
  @inline
  private def left(i: Int) = 2 * i
  @inline
  private def right(i: Int) = 2 * i + 1

  override def toString: String = "BinaryHeap(" + heap.take(heapSize + 1).mkString(", ") + ")"
}

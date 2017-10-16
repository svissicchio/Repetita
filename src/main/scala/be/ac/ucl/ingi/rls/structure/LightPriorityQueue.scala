package be.ac.ucl.ingi.rls.structure

trait LightPriorityQueue[V] {
  def head: V
  def dequeue(): V
  def enqueue(key: Int, value: V): Unit
  def remove(key: Int, value: V): Boolean
  def removeAll(): Unit
  def changeKey(oldKey: Int, newKey: Int, value: V): Boolean
  def size: Int
  def isEmpty: Boolean
}

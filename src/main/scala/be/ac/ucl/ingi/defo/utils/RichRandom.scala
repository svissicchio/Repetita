package be.ac.ucl.ingi.defo.utils

import scala.util.Random

class RichRandom(random: java.util.Random) extends Random(random) {

  final def weightedSelect[@specialized(Int) B](array: Array[B])(prob: B => Int): B = {
    var elem = array(1)
    var acc = prob(elem)
    var i = 1
    while (i < array.length) {
      val e = array(i)
      val p = prob(e)
      acc += p
      if (nextInt(acc) < p) elem = e
      i += 1
    }
    elem
  }

  final def weightedSelect[@specialized(Int) B](col: Traversable[B])(prob: B => Int): B = {
    var acc = prob(col.head)
    var elem = col.head
    col.tail.foreach(e => {
      val p = prob(e)
      acc += p
      if (nextInt(acc) < p) elem = e
    })
    elem
  }

  def weightedShuffle(array: Array[Int], f: Int => Int) = {
    val values = Array.tabulate(array.size)(i => f(array(i)) * random.nextFloat)
    val sorted = (0 until array.size).sortBy(values)
    Array.tabulate(array.size)(i => array(sorted(i)))
  }

  def weightedTake(array: Array[Int], k: Int, f: Int => Int, alpha: Int = 2): Array[Int] = {
    if (k >= array.size) array
    else {
      val selected = Array.ofDim[Int](k)
      val sorted = array.sortBy(f)
      val contained = Array.fill(array.size)(true)
      var nSelected = 0
      while (nSelected < k) {
        var r = (math.pow(nextDouble, alpha) * sorted.size).floor.toInt
        while (!contained(r)) {
          if (r > 0) r -= 1
          else r = array.size - 1
        }
        val elem = sorted(r)
        contained(r) = false
        selected(nSelected) = elem
        nSelected += 1
      }
      selected
    }
  }
}

object RichRandom {
  def apply(seed: Int): RichRandom = new RichRandom(new java.util.Random(0))
  def apply(): RichRandom = new RichRandom(new java.util.Random)
}

package be.ac.ucl.ingi.rls.structure

/**
 *  Fast array-based bit vector.
 *  This class does not check the
 *  validity of the user inputs.
 *
 *  @author Renaud Hartert ren.hartert@gmail.com
 */
final class ArrayBitVector(nBits: Int) {

  private[this] val words = new Array[Long]((nBits >> 6) + 1)

  @inline final def insert(bitId: Int): Unit = {
    val wordId = bitId >> 6
    words(wordId) |= (1L << bitId)
  }

  @inline final def remove(bitId: Int): Unit = {
    val wordId = bitId >> 6
    words(wordId) &= ~(1L << bitId)
  }

  @inline final def apply(bitId: Int): Boolean = {
    val wordId = bitId >> 6
    val word = words(wordId)
    (word & (1L << bitId)) != 0
  }

  @inline final def update(bitId: Int, value: Boolean): Unit = {
    val wordId = bitId >> 6
    if (value) words(wordId) |= (1L << bitId)
    else words(wordId) &= ~(1L << bitId)
  }
}

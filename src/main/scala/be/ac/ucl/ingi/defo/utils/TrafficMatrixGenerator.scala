package defo.utils

import scala.util.Random

object TrafficMatrixGenerator {
	
  def exp(rng: Random): Double = {
    val d = rng.nextDouble()
    -math.log(d)
  } 
  
  def generate(n: Int, seed: Int): Array[Array[Double]] = generate(n, new Random(seed))

  def generate(n: Int, rng: Random): Array[Array[Double]] = {
    
    val tIn = Array.fill(n)(exp(rng))
    val tOut = Array.fill(n)(exp(rng))

    if (tIn.take(n - 1).sum > tOut.take(n - 1).sum) {
      tIn(n - 1) = exp(rng)
      tOut(n - 1) = tIn.sum - tOut.take(n - 1).sum
    }
    else {
      tOut(n - 1) = exp(rng)
      tIn(n - 1) = tOut.sum - tIn.take(n - 1).sum
    }

    val tInTot = tIn.sum
    val tOutTot = tOut.sum

    assert(tInTot == tOutTot)

    // compute the traffic matrix according to the gravity model, see equation (1) in "Simplifying the synthesis of Internet Traffic Matrices", M. Roughan, in CCR 2005
    val tm = Array.tabulate(n, n)((i, j) => (tOut(i) * tIn(j)) / tInTot)

    tm
  }
}

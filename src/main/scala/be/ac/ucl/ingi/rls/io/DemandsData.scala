package be.ac.ucl.ingi.rls.io

class DemandsData(
  final val demandLabels: Array[String],
  final val demandSrcs: Array[Int],
  final val demandDests: Array[Int],
  final val demandTraffics: Array[Double]
) {
  final val nDemands: Int = demandLabels.length
  final val Demands: Range = Range(0, nDemands)
}

object DemandsData {
  def apply(demandLabels: Array[String], demandSrcs: Array[Int], demandDests: Array[Int], demandTraffics: Array[Double]) = {
    new DemandsData(demandLabels.clone(), demandSrcs.clone(), demandDests.clone(), demandTraffics.clone())
  }

}

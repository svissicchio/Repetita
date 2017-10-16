package be.ac.ucl.ingi.defo.parsers

class DemandsData(
  final val demandLabels: Array[String],
  final val demandSrcs: Array[Int],
  final val demandDests: Array[Int],
  final val demandTraffics: Array[Int]
) {
  final val nDemands: Int = demandLabels.length
  final val Demands: Range = Range(0, nDemands)
}

package be.ac.ucl.ingi.defo.parsers

class TopologyData(
    final val nodeLabels: Array[String],
    final val nodeCoordinates: Array[(Double, Double)],
    final val edgeLabels: Array[String],
    final val edgeSrcs: Array[Int],
    final val edgeDests: Array[Int],
    final val edgeWeights: Array[Int],
    final val edgeCapacities: Array[Int],
    final val edgeLatencies: Array[Int]
)

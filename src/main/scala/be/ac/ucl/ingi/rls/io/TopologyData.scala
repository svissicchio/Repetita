package be.ac.ucl.ingi.rls.io

class TopologyData(
    final val nodeLabels: Array[String],
    final val nodeCoordinates: Array[(Double, Double)],
    final val edgeLabels: Array[String],
    final val edgeSrcs: Array[Int],
    final val edgeDests: Array[Int],
    final val edgeWeights: Array[Int],
    final val edgeCapacities: Array[Double],
    final val edgeLatencies: Array[Int]
)

object TopologyData {
  def apply(nodeLabels: Array[String],
    nodeCoordinates: Array[(Double, Double)],
    edgeLabels: Array[String],
    edgeSrcs: Array[Int],
    edgeDests: Array[Int],
    edgeWeights: Array[Int],
    edgeCapacities: Array[Double],
    edgeLatencies: Array[Int]
  ) = new TopologyData(nodeLabels: Array[String],
    nodeCoordinates: Array[(Double, Double)],
    edgeLabels: Array[String],
    edgeSrcs: Array[Int],
    edgeDests: Array[Int],
    edgeWeights: Array[Int],
    edgeCapacities: Array[Double],
    edgeLatencies: Array[Int])
  
  // no coordinates
    def apply(nodeLabels: Array[String],
    edgeLabels: Array[String],
    edgeSrcs: Array[Int],
    edgeDests: Array[Int],
    edgeWeights: Array[Int],
    edgeCapacities: Array[Double],
    edgeLatencies: Array[Int]
  ) = new TopologyData(nodeLabels: Array[String],
    Array.fill(nodeLabels.size)((0.0, 0.0)),
    edgeLabels: Array[String],
    edgeSrcs: Array[Int],
    edgeDests: Array[Int],
    edgeWeights: Array[Int],
    edgeCapacities: Array[Double],
    edgeLatencies: Array[Int])

}

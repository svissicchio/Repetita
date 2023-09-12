package be.ac.ucl.ingi.defo.parsers

class InFile(filepath: String) {

  val lines = scala.io.Source.fromFile(filepath).getLines.reduceLeft(_ + " " + _)
  val vals = lines.split("[ ,\t]").toList.filterNot(_ == "").map(_.toInt)
  var index = 0

  def nextInt() = {
    index += 1
    vals(index - 1)
  }


}



object InFile {
  def apply(filepath: String) = new InFile(filepath)
}
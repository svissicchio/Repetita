package be.ac.ucl.ingi.defo.parsers

import java.io.{BufferedWriter, FileWriter}


class OutFile(filepath: String, critical: Boolean, verbous: Boolean) {

  val file: BufferedWriter = openFile()

  private def openFile(): BufferedWriter = {
    try new BufferedWriter(new FileWriter(filepath))
    catch {
      case e: Error => {
        errorHandling(e)
        null
      }
    }
  }

  private def errorHandling(e: Error): Unit = {
    if (verbous) println(e.getMessage)
    if (critical) System.exit(-1)
  }

  def write(line: String): Unit = {
    try file.write(line)
    catch {
      case e: Error => errorHandling(e)
    }
  }

  def write(line: Int): Unit = {
    write(line)
  }

  def writeln(line: String): Unit = write(line + "\n")

  def writeln(line: Int): Unit = writeln(line)

  def writeln() = write("\n")

  def close() = {
    try file.close()
    catch {
      case e: Error => errorHandling(e)
    }
  }
}

object OutFile {
  def apply(filepath: String, critical: Boolean = true, verbous: Boolean = true) = new OutFile(filepath, critical, verbous)
}

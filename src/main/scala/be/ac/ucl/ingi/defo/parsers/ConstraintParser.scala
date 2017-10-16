package be.ac.ucl.ingi.defo.parsers

import scala.io.Source
import scala.collection.mutable.ArrayBuffer
import be.ac.ucl.ingi.defo.modeling.MRProblem

class ConstraintData {
  
}

abstract class ParsedConstraint 

class ParsedPassThrough(val demand: Symbol, val sets: Array[Array[Symbol]]) extends ParsedConstraint

object ConstraintParser {

  
  def parse(filePath: String): Array[ParsedConstraint] = {
    
    val lines = Source.fromFile(filePath).getLines
    val constraints = ArrayBuffer[ParsedConstraint]()
    while (lines.hasNext) {
      val line = lines.next
      val data = line.split(" ")
      val constraint = data(0)
      constraint match {
        case "PassThrough" => constraints.append(parsePassThrough(data))
      }  
    }  
    constraints.toArray
  }
  
  private def parsePassThrough(data: Array[String]): ParsedPassThrough = {
    val demandLabel = Symbol(data(1))
    var i = 2
    val sets = ArrayBuffer[ArrayBuffer[Symbol]]()
    sets.append(ArrayBuffer[Symbol]())
    while (i < data.length) {
      val symb = data(i)
      if (symb == "|") sets.append(ArrayBuffer[Symbol]())
      else sets.last.append(Symbol(symb))
      i += 1
    }
    new ParsedPassThrough(demandLabel, sets.map(_.toArray).toArray)
  }
}

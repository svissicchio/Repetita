package defo.core

class DEFOException(message: String) extends Exception(message)

class OverConstrainedException(final val demandId: Int, message: String) extends DEFOException(message) 

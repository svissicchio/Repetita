packAutoSettings

packMain := Map("repetita" -> "edu.repetita.main.Main")

lazy val root = (project in file(".")).
  settings(
    name := "repetita",
    scalaVersion := "2.11.4",
    resolvers += "Oscar Snapshots" at "http://artifactory.info.ucl.ac.be/artifactory/libs-snapshot-local/", 
    resolvers += "gurobi0" at "http://cogcomp.org/m2repo/", 
    libraryDependencies += "com.gurobi" % "gurobi" % "7.0.1",
    libraryDependencies += "oscar" %% "oscar-cp" % "4.0.0-SNAPSHOT",
    libraryDependencies += "org.scalatest" %% "scalatest" % "2.2.+" % Test,
    libraryDependencies += "net.sourceforge.collections" % "collections-generic" % "4.01",
    libraryDependencies += "net.sf.jung" % "jung-algorithms" % "2.1.1",
    libraryDependencies += "net.sf.jung" % "jung-visualization" % "2.1.1",
    libraryDependencies += "net.sf.jung" % "jung-graph-impl" % "2.1.1",
    libraryDependencies += "org.scala-lang.modules" %% "scala-xml" % "1.0.4",
    libraryDependencies += "junit" % "junit" % "4.12",
    javaOptions in run += "-Xmx4G"    
  )
  
  
  





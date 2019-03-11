packAutoSettings

packMain := Map("repetita" -> "edu.repetita.main.Main")

lazy val root = (project in file(".")).
  settings(
    name := "repetita",
    scalaVersion := "2.11.4",
    resolvers += "Oscar Snapshots" at "http://artifactory.info.ucl.ac.be/artifactory/libs-snapshot-local/",    
    libraryDependencies += "oscar" %% "oscar-cp" % "4.0.0-SNAPSHOT",
    libraryDependencies += "org.scalatest" %% "scalatest" % "2.2.+" % Test,
    libraryDependencies += "net.sourceforge.collections" % "collections-generic" % "4.01",
    libraryDependencies += "net.sf.jung" % "jung-algorithms" % "2.1.1",
    libraryDependencies += "net.sf.jung" % "jung-visualization" % "2.1.1",
    libraryDependencies += "net.sf.jung" % "jung-graph-impl" % "2.1.1",
    libraryDependencies += "org.scala-lang.modules" %% "scala-xml" % "1.0.4",
    libraryDependencies += "com.fasterxml.jackson.core" % "jackson-core" % "2.9.8",
    libraryDependencies += "com.fasterxml.jackson.core" % "jackson-databind" % "2.9.8",
    javaOptions in run += "-Xmx4G"    
  )





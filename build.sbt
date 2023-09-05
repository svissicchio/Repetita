enablePlugins(PackPlugin)


packMain := Map("repetita" -> "edu.repetita.main.Main")


name := "Repetita"

version := "0.1.0"

scalaVersion := "2.13.6"


resolvers += "Mvn" at "https://cogcomp.seas.upenn.edu/m2repo/"

resolvers += "GitHub pschaus/oscar Maven repo" at "https://maven.pkg.github.com/pschaus/oscar"


// Adding a library dependency for ScalaTest
libraryDependencies ++= Seq(
    "gurobi" % "gurobi" % "5.0.1",
    "oscar" %% "oscar-cp" % "5.0.0",
    "org.scalatest" %% "scalatest" % "3.2.16" % Test,
    "org.apache.commons" % "commons-lang3" % "3.13.0",
    "net.sourceforge.collections" % "collections-generic" % "4.01",
    "net.sf.jung" % "jung-algorithms" % "2.1.1",
    "net.sf.jung" % "jung-visualization" % "2.1.1",
    "net.sf.jung" % "jung-graph-impl" % "2.1.1",
    "org.scala-lang.modules" %% "scala-xml" % "2.2.0",
    "junit" % "junit" % "4.12",
    "com.novocode" % "junit-interface" % "0.11" % Test
)

// Adding a library dependency for ScalaTest

libraryDependencies += "oscar" %% "oscar-cp" % "5.0.0"


// Some common options for the Scala compiler
scalacOptions ++= Seq(
    "-unchecked",
    "-deprecation",
    "-feature"
)

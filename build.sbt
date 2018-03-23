name := "actors-java"

version := "1.0"

scalaVersion := "2.12.4"

libraryDependencies ++= {
  val akkaVersion = "2.5.11"
  val junitVersion = "5.1.0"
  Seq(
    "com.typesafe.akka" %% "akka-actor" % akkaVersion,
    "com.typesafe.akka" %% "akka-testkit" % akkaVersion % Test,
    "org.scalatest" %% "scalatest" % "3.0.5" % Test,
    "org.junit.vintage" % "junit-vintage-engine" % junitVersion % Test,
    "com.novocode" % "junit-interface" % "0.11" % Test
  )
}


crossPaths := false
testOptions in Test := Seq(Tests.Argument(TestFrameworks.JUnit, "-a"))

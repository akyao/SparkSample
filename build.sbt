name := "SparkSample"

version := "1.0"

scalaVersion := "2.10.2"

libraryDependencies ++= Seq(
  "org.apache.spark" %% "spark-core" % "1.6.2" % "compile",
  "org.apache.spark" %% "spark-mllib"  % "1.6.2" % "compile"
)

mainClass in assembly := Some("Example")
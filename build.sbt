name := "SparkSample"

version := "1.0"

scalaVersion := "2.10.2"

libraryDependencies ++= Seq(
  "org.apache.spark" %% "spark-core" % "1.3.1" % "compile",
  "org.apache.spark" %% "spark-mllib"  % "1.3.1" % "compile"
//  ("org.apache.spark" %% "spark-core" % "1.3.1").
//    exclude("org.mortbay.jetty", "servlet-api").
//    exclude("com.google.guava","guava").
//    //exclude("org.apache.spark","spark-network-common_2.11").
//    exclude("org.apache.hadoop","hadoop-yarn-api").
//    exclude("commons-beanutils", "commons-beanutils-core").
//    exclude("commons-collections", "commons-collections").
//    exclude("commons-logging", "commons-logging").
//    exclude("org.spark-project.spark", "unused").
//    exclude("com.esotericsoftware.minlog", "minlog")
)

mainClass in assembly := Some("Example")
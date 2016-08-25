package akyao


import org.apache.spark.{SparkContext, SparkConf}

object WordCount {
  def main(args: Array[String]): Unit = {

    val conf = new SparkConf()
      .setAppName("WordCount Application")
      .setMaster("local[*]")
    val sc = new SparkContext(conf)

    val count = sc.parallelize(Array(1,2,3,4,5,6,7,8,9,10)).count()
    println(count)

    sc.stop
  }

}
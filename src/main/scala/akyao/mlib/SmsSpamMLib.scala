/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package akyao.mlib

import org.apache.spark.mllib.classification.LogisticRegressionWithLBFGS
import org.apache.spark.mllib.evaluation.{MulticlassMetrics, BinaryClassificationMetrics}
import org.apache.spark.mllib.feature.{StandardScaler, HashingTF}
import org.apache.spark.mllib.regression.LabeledPoint
import org.apache.spark.mllib.util.MLUtils
import org.apache.spark.{SparkContext, SparkConf}


object SmsSpamMLib {
  def main(args: Array[String]): Unit = {
    val conf = new SparkConf()
      .setAppName("SmsSpamMLib Application")
      .setMaster("local[*]")
    val sc = new SparkContext(conf)

    val hashingTF = new HashingTF(100000)

    val data = sc.textFile("data/smsspamcollection/SMSSpamCollection")
      .map(_.split("\\s"))
      .map(v => new LabeledPoint(if (v(0) == "spam") 1.0 else 0.0, hashingTF.transform(v.drop(1))))

    val scaler = new StandardScaler().fit(data.map(_.features))
    val scaledData = data.map(v => v.copy(features = scaler.transform(v.features)))

    val lr = new LogisticRegressionWithLBFGS()
    lr.optimizer.setRegParam(2.0)

    val k =4
    val predications = MLUtils.kFold(scaledData, k, 1).map { case (training, test) =>

        val model = lr.run(training)

        test.map(lp => (model.predict(lp.features), lp.label))
    }

    val result = predications.reduce((rdd1, rdd2) => rdd1.union(rdd2))

    val binaryMetrics = new BinaryClassificationMetrics(result)
    println(s"AUC: ${binaryMetrics.areaUnderROC()}")

    val multiclassMetrics = new MulticlassMetrics(result)
    println(s"Confusion matrix: ${multiclassMetrics.confusionMatrix}")

    println(s"TP rate of 0.0=ham  : ${multiclassMetrics.truePositiveRate(0.0)}")
    println(s"TP rate of 1.0=spam : ${multiclassMetrics.truePositiveRate(1.0)}")

    println(s"FP rate of 0.0=ham  : ${multiclassMetrics.falsePositiveRate(0.0)}")
    println(s"FP rate of 1.0=spam : ${multiclassMetrics.falsePositiveRate(1.0)}")

    println(s"Precision of 0.0=ham  : ${multiclassMetrics.precision(0.0)}")
    println(s"Precision of 1.0=spam : ${multiclassMetrics.precision(1.0)}")

    println(s"Recall of 0.0=ham  : ${multiclassMetrics.recall(0.0)}")
    println(s"Recall of 1.0=spam : ${multiclassMetrics.recall(1.0)}")

    sc.stop()
  }
}
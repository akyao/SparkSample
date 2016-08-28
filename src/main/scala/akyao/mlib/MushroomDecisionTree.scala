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

import org.apache.spark.ml.Pipeline
import org.apache.spark.ml.classification.DecisionTreeClassifier
import org.apache.spark.ml.evaluation.BinaryClassificationEvaluator
import org.apache.spark.ml.feature.{RFormula, StringIndexerModel}

import org.apache.spark.sql.SQLContext
import org.apache.spark.{SparkConf, SparkContext}


object MushroomDecisionTree {
  def main(args: Array[String]): Unit = {
    val conf = new SparkConf()
      .setAppName("Mushroom")
      .setMaster("local")

    val sc = new SparkContext(conf)
    val sqlContext = new SQLContext(sc)
    import sqlContext.implicits._

    val featureNames = Seq("_edible", "cap-shape", "cap-surface", "cap-color", "bruises?", "odor",
      "grill-attachment","grill-spacing","grill-size","grill-color","stalk-shape","stalk-root",
      "stalk-surface-above-ring","stalk-surface-below-ring","stalk-color-above-ring",
      "stalk-color-below-ring", /* "veil-type", */ "veil-color", "ring-number", "ring-type",
      "spore-print-color", "population", "habitat")

    // データを読み込んでRDDに
    val rdd = sc.textFile("data/mushroom/agaricus-lepiota.data")
      .map(line => line.split(","))
      .map(v => (v(0), v(1), v(2), v(3), v(4), v(5), v(6), v(7), v(8), v(9),
        v(10), v(11), v(12), v(13), v(14), v(15), /* v(16), */ v(17), v(18), v(19), v(20),
        v(21), v(22)))

    // RDD -> Dataframe
    val _df = rdd.toDF(featureNames: _*)

    //
    val df = new StringIndexerModel(Array("e", "p"))
      .setInputCol("_edible").setOutputCol("edible")
      .transform(_df).drop("_edible").drop("veil-type")

    //
    val formula = new RFormula()
      .setFeaturesCol("features").setLabelCol("label")
      .setFormula("edible ~ .")
      .fit(df)

    // 決定木
    val decisionTree = new DecisionTreeClassifier()
      .setFeaturesCol("features").setLabelCol("edible")
      .setMaxDepth(4)

    val pipeline = new Pipeline()
      .setStages(Array(formula, decisionTree))

    // データを学習用とテスト用に
    val trainingAndTest = df.randomSplit(Array(0.5, 0.5))

    val pipelineModel = pipeline.fit(trainingAndTest(0))

    // 予測
    val prediction = pipelineModel.transform(trainingAndTest(1))

    val auc = new BinaryClassificationEvaluator()
      .evaluate(prediction)

    println(auc)

    sc.stop()
  }
}
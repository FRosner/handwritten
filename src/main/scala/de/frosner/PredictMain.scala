package de.frosner

import java.io.FileInputStream

import org.deeplearning4j.util.ModelSerializer

object PredictMain extends App {

  val model = ModelSerializer.restoreMultiLayerNetwork("model.zip")
  val img = MnistLoader.fromStream(new FileInputStream("src/main/resources/mnist_png/testing/1/2.png"))

}

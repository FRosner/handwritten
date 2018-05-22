package de.frosner

import java.io.FileInputStream

import org.deeplearning4j.scalnet.logging.Logging
import org.deeplearning4j.util.ModelSerializer

object PredictMain extends App with Logging {

  val model = ModelSerializer.restoreMultiLayerNetwork("model.zip")
  val img = MnistLoader.fromStream(new FileInputStream("src/main/resources/mnist_png/testing/1/2.png"))
  logger.info(s"${model.output(img)}")

}

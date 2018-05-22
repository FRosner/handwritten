package de.frosner

import java.io.FileInputStream

import org.datavec.image.loader.NativeImageLoader
import org.deeplearning4j.datasets.iterator.impl.MnistDataSetIterator
import org.deeplearning4j.scalnet.logging.Logging
import org.deeplearning4j.util.ModelSerializer
import org.nd4j.linalg.dataset.api.iterator.DataSetIterator

object PredictMain extends App with Logging {

  val batchSize: Int = 64
  val seed: Int = 123

  val mnistTest: DataSetIterator =
    new MnistDataSetIterator(batchSize, false, seed)
  val model = ModelSerializer.restoreMultiLayerNetwork("model.zip")
  val loader = new NativeImageLoader(MnistLoader.height, MnistLoader.width, 3)
  val img = MnistLoader.fromStream(new FileInputStream("src/main/resources/mnist_png/testing/1/2.png"))
  logger.info(s"${model.output(img)}")

}

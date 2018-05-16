package de.frosner

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
  logger.info(s"Test accuracy = ${model.evaluate(mnistTest).accuracy}")

}

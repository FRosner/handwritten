package de.frosner

import org.deeplearning4j.nn.api.Classifier
import org.nd4j.linalg.api.ndarray.INDArray

// TODO: Use thread safe https://deeplearning4j.org/workspaces#parallelwrapper--parallelinference
class SynchronizedClassifier(val classifier: Classifier) {

  def predict(input: INDArray): Int = synchronized {
    getOnlyElement(classifier.predict(input))
  }

  private def getOnlyElement[T](arr: Array[T]): T = {
    require(arr.length == 1, "Array did not contain only one element.")
    arr.head
  }

}

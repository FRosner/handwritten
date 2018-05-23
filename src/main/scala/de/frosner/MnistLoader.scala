package de.frosner

import java.io.{File, InputStream}
import java.util.Random

import org.datavec.api.io.labels.ParentPathLabelGenerator
import org.datavec.api.records.listener.impl.LogRecordListener
import org.datavec.api.split.FileSplit
import org.datavec.image.loader.NativeImageLoader
import org.datavec.image.recordreader.ImageRecordReader
import org.datavec.image.transform.{MultiImageTransform, ResizeImageTransform, ShowImageTransform}
import org.deeplearning4j.datasets.datavec.RecordReaderDataSetIterator
import org.nd4j.linalg.api.ndarray.INDArray
import org.nd4j.linalg.dataset.api.iterator.DataSetIterator
import org.nd4j.linalg.dataset.api.preprocessor.{DataNormalization, ImagePreProcessingScaler}

object MnistLoader {

  val height: Int = 28
  val width: Int = 28
  val channels: Int = 1
  val nClasses: Int = 10
  val labelIndex = 1
  val batchSize: Int = 64
  val randNumGen = new Random(123)

  def fromDirectory(directory: File): DataSetIterator = {
    val files = new FileSplit(directory, NativeImageLoader.ALLOWED_FORMATS, randNumGen)

    // Extract the parent path as the image label
    val labelMaker = new ParentPathLabelGenerator()

    // Specify shape of images
    val recordReader = new ImageRecordReader(height, width, channels, labelMaker)
    recordReader.initialize(files)

    // Log when records are read
    recordReader.setListeners(new LogRecordListener())

    // Specify iterator
    val data = new RecordReaderDataSetIterator(recordReader, batchSize, labelIndex, nClasses)

    // Scale image values to 0,1
    val scaler: DataNormalization = new ImagePreProcessingScaler(0, 1)
    scaler.fit(data)
    data.setPreProcessor(scaler)

    data
  }

  def fromStream(stream: InputStream): INDArray = {
    val resizer = new ResizeImageTransform(width, height)
    val loader = new NativeImageLoader(height, width, channels, resizer)
    val img = loader.asMatrix(stream)
    val scaler: DataNormalization = new ImagePreProcessingScaler(0, 1)
    scaler.transform(img)
    img
  }

}

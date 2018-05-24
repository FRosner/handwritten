/*
 * Copyright 2016 Skymind
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package de.frosner

import java.io.File

import org.deeplearning4j.eval.Evaluation
import org.deeplearning4j.nn.api.OptimizationAlgorithm
import org.deeplearning4j.nn.conf.NeuralNetConfiguration
import org.deeplearning4j.nn.conf.inputs.InputType
import org.deeplearning4j.nn.conf.layers._
import org.deeplearning4j.nn.multilayer.MultiLayerNetwork
import org.deeplearning4j.nn.weights.WeightInit
import org.deeplearning4j.optimize.listeners.ScoreIterationListener
import org.deeplearning4j.util.ModelSerializer
import org.nd4j.linalg.activations.Activation
import org.nd4j.linalg.learning.config.Nesterovs
import org.nd4j.linalg.lossfunctions.LossFunctions

object TrainMain extends App {

  val hiddenSize = 512
  val seed: Int = 123
  val epochs: Int = 15
  val learningRate: Double = 0.0015
  val decay: Double = 0.005
  val scoreFrequency = 1000
  val numEpochs = 3

  val DATA_PATH = "src/main/resources/mnist_png/"

  val mnistTrain = MnistLoader.fromDirectory(new File(DATA_PATH + "training"))
  val mnistTest = MnistLoader.fromDirectory(new File(DATA_PATH + "testing"))

  val conf = new NeuralNetConfiguration.Builder()
    .seed(seed)
    .l2(0.0005)
    .weightInit(WeightInit.XAVIER)
    .optimizationAlgo(OptimizationAlgorithm.STOCHASTIC_GRADIENT_DESCENT)
    .updater(new Nesterovs(0.01, 0.9))
    .list()
    .layer(
      0,
      new ConvolutionLayer.Builder(5, 5)
        .nIn(MnistLoader.channels)
        .stride(1, 1)
        .nOut(20) // number of filters
        .activation(Activation.IDENTITY)
        .build()
    )
    .layer(
      1,
      new SubsamplingLayer.Builder(PoolingType.MAX)
        .kernelSize(2, 2)
        .stride(2, 2)
        .build()
    )
    .layer(
      2,
      new ConvolutionLayer.Builder(5, 5)
      //Note that nIn need not be specified in later layers
        .stride(1, 1)
        .nOut(50)
        .activation(Activation.IDENTITY)
        .build()
    )
    .layer(
      3,
      new SubsamplingLayer.Builder(PoolingType.MAX)
        .kernelSize(2, 2)
        .stride(2, 2)
        .build()
    )
    .layer(
      4,
      new DenseLayer.Builder()
        .activation(Activation.RELU)
        .nOut(500)
        .build()
    )
    .layer(
      5,
      new OutputLayer.Builder(LossFunctions.LossFunction.NEGATIVELOGLIKELIHOOD)
        .nOut(MnistLoader.nClasses)
        .activation(Activation.SOFTMAX)
        .build()
    )
    .setInputType(InputType.convolutional(MnistLoader.height, MnistLoader.width, MnistLoader.channels))
    .backprop(true)
    .pretrain(false)
    .build();

  val model = new MultiLayerNetwork(conf)

  model.setListeners(new ScoreIterationListener(10))

  // fit once (one epoch)

  for (i <- 1 to numEpochs) {
    model.fit(mnistTrain)

    // evaluate model
    // Create Eval object with 10 possible classes
    val eval = new Evaluation(MnistLoader.nClasses)

    // Evaluate the network
    while (mnistTest.hasNext) {
      val next = mnistTest.next()
      val output = model.output(next.getFeatureMatrix)
      eval.eval(next.getLabels, output)
    }

    println(eval.stats)
    mnistTest.reset()
  }

  ModelSerializer.writeModel(model, "model.zip", false)

}

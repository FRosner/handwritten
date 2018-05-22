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

import org.deeplearning4j.nn.api.OptimizationAlgorithm
import org.deeplearning4j.nn.conf.NeuralNetConfiguration
import org.deeplearning4j.nn.conf.inputs.InputType
import org.deeplearning4j.nn.conf.layers.{DenseLayer, OutputLayer}
import org.deeplearning4j.nn.multilayer.MultiLayerNetwork
import org.deeplearning4j.nn.weights.WeightInit
import org.deeplearning4j.optimize.listeners.ScoreIterationListener
import org.deeplearning4j.scalnet.logging.Logging
import org.deeplearning4j.util.ModelSerializer
import org.nd4j.linalg.activations.Activation
import org.nd4j.linalg.learning.config.Nesterovs
import org.nd4j.linalg.lossfunctions.LossFunctions

/**
 * Two-layer MLP for MNIST using DL4J-style NeuralNet
 * model construction pattern.
 *
 * @author David Kale
 */
object TrainMain extends App with Logging {

  val hiddenSize = 512
  val seed: Int = 123
  val epochs: Int = 15
  val learningRate: Double = 0.0015
  val decay: Double = 0.005
  val scoreFrequency = 1000
  val numEpochs = 1

  val DATA_PATH = "src/main/resources/mnist_png/"

  val mnistTrain = MnistLoader.fromDirectory(new File(DATA_PATH + "training"))
  val mnistTest = MnistLoader.fromDirectory(new File(DATA_PATH + "testing"))

  logger.info("Build model...")

  val conf = new NeuralNetConfiguration.Builder()
    .seed(123)
    .optimizationAlgo(OptimizationAlgorithm.STOCHASTIC_GRADIENT_DESCENT)
    .updater(new Nesterovs(0.006, 0.9))
    .l2(1e-4)
    .list
    .layer(
      0,
      new DenseLayer.Builder()
        .nIn(MnistLoader.height * MnistLoader.width)
        .nOut(100)
        .activation(Activation.RELU)
        .weightInit(WeightInit.XAVIER)
        .build
    )
    .layer(
      1,
      new OutputLayer.Builder(LossFunctions.LossFunction.NEGATIVELOGLIKELIHOOD)
        .nIn(100)
        .nOut(MnistLoader.nClasses)
        .activation(Activation.SOFTMAX)
        .weightInit(WeightInit.XAVIER)
        .build
    )
    .pretrain(false)
    .backprop(true)
    .setInputType(InputType.convolutional(MnistLoader.height, MnistLoader.width, MnistLoader.channels))
    .build

  val model = new MultiLayerNetwork(conf)

  model.setListeners(new ScoreIterationListener(10))

  // fit once (one epoch)
  model.fit(mnistTrain);

  ModelSerializer.writeModel(model, "model.zip", false)

}

package de.frosner

import java.util

import akka.NotUsed
import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.Multipart
import akka.http.scaladsl.model.Multipart.BodyPart
import akka.http.scaladsl.server.Directives._
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.{Source, StreamConverters}
import akka.util.ByteString
import com.typesafe.config.ConfigFactory
import org.deeplearning4j.util.ModelSerializer
import org.nd4j.linalg.api.ndarray.INDArray

import scala.concurrent.Future
import scala.concurrent.duration._
import scala.io.StdIn

/*
curl \
  -F "image=@src/main/resources/mnist_png/testing/1/2.png" \
  localhost:8080/predict
 */
object PredictAPI {

  def main(args: Array[String]) {

    implicit val system = ActorSystem("my-system")
    implicit val materializer = ActorMaterializer()
    // needed for the future flatMap/onComplete in the end
    implicit val executionContext = system.dispatcher

    lazy val model = new SynchronizedClassifier(ModelSerializer.restoreMultiLayerNetwork("model.zip"))

    val route =
      path("") {
        getFromResource("static/index.html") // getFromFile("src/main/resources/static/index.html")
      } ~
        pathPrefix("static") {
          getFromResourceDirectory("static") // getFromDirectory("src/main/resources/static")
        } ~
        path("predict") {
          fileUpload("image") {
            case (fileInfo, fileStream) =>
              val in = fileStream.runWith(StreamConverters.asInputStream(3.seconds))
              val img = invert(MnistLoader.fromStream(in))
              complete(model.predict(img).toString)
          }
        }

    val config = ConfigFactory.load()
    val interface = config.getString("http.interface")
    val port = config.getInt("http.port")
    val bindingFuture = Http().bindAndHandle(route, interface, port)
    println(s"Server online at http://$interface:$port/")
    println("Loading model")
    println(s"Loaded ${model.classifier.getClass.getName}")
  }

  def invert(img: INDArray): INDArray = {
    img.rsubi(1)
    img
  }

}

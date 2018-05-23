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
import org.deeplearning4j.scalnet.logging.Logging
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
object PredictAPI extends Logging {

  def main(args: Array[String]) {

    implicit val system = ActorSystem("my-system")
    implicit val materializer = ActorMaterializer()
    // needed for the future flatMap/onComplete in the end
    implicit val executionContext = system.dispatcher

    val model = new SynchronizedClassifier(ModelSerializer.restoreMultiLayerNetwork("model.zip"))

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

    val bindingFuture = Http().bindAndHandle(route, "localhost", 8080)

    println(s"Server online at http://localhost:8080/\nPress RETURN to stop...")
    StdIn.readLine() // let it run until user presses return
    bindingFuture
      .flatMap(_.unbind()) // trigger unbinding from the port
      .onComplete(_ => system.terminate()) // and shutdown when done
  }

  def invert(img: INDArray): INDArray = {
    img.rsubi(1)
    img
  }

}

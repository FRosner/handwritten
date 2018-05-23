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
          entity(as[Multipart.FormData]) { formData =>
            // collect all parts of the multipart as it arrives into a map
            val allPartsF: Future[Map[String, Source[ByteString, NotUsed]]] = formData.parts
              .mapAsync[(String, Source[ByteString, NotUsed])](1) {
                case b: BodyPart =>
                  b.toStrict(2.seconds).map(strict => b.name -> strict.entity.dataBytes)
              }
              .runFold(Map.empty[String, Source[ByteString, NotUsed]])((map, tuple) => map + tuple)

            val done = allPartsF.map { allParts =>
              val in = allParts("image").runWith(StreamConverters.asInputStream(3.seconds))
              val img = MnistLoader.fromStream(in)
              model.predict(img)
            }

            // when processing have finished create a response for the user
            onSuccess(done) { img =>
              complete {
                img.toString
              }
            }
          }
        }

    val bindingFuture = Http().bindAndHandle(route, "localhost", 8080)

    println(s"Server online at http://localhost:8080/\nPress RETURN to stop...")
    StdIn.readLine() // let it run until user presses return
    bindingFuture
      .flatMap(_.unbind()) // trigger unbinding from the port
      .onComplete(_ => system.terminate()) // and shutdown when done
  }

}

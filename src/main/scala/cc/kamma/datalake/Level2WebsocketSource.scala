package cc.kamma.datalake

import akka.Done
import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.ws
import akka.http.scaladsl.model.ws.{Message, TextMessage}
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.{Keep, Sink, Source}
import scala.concurrent.ExecutionContext.Implicits.global

import scala.concurrent.Future

object Level2WebsocketSource {
  def getSource(productId: String)(implicit actorSystem: ActorSystem, actorMaterializer: ActorMaterializer): Source[String, Future[ws.WebSocketUpgradeResponse]] = {

    val incoming: Sink[String, Future[Done]] =
      Sink.foreach[String](println)

    val outgoing = Source.single(TextMessage(
      """
        |{
        |    "type": "subscribe",
        |    "product_ids": [
        |        "BTC-USD"
        |    ],
        |    "channels": [
        |        "level2"
        |    ]
        |}
      """.stripMargin.replace("BTC-USD", productId))).concat(Source.maybe)

    val flow = Http().webSocketClientFlow("wss://ws-feed.gdax.com")


    def wsMessageToString: Message => Future[String] = {
      case message: TextMessage.Strict =>
        Future.successful(message.text)

      case message: TextMessage.Streamed =>
        val seq: Future[Seq[String]] = message.textStream.runWith(Sink.seq)
        seq.map(seq => seq.mkString)

      case message =>
        Future.successful(message.toString)
    }


    outgoing
      .viaMat(flow)(Keep.right)
      .mapAsync(1)(wsMessageToString)


  }
}

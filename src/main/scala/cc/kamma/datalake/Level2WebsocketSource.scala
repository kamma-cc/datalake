package cc.kamma.datalake

import akka.Done
import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.ws
import akka.http.scaladsl.model.ws.{Message, TextMessage}
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.{Keep, Sink, Source}

import scala.concurrent.ExecutionContext.Implicits.global
import org.json4s._
import org.json4s.native.JsonMethods._
import org.json4s.native.Serialization
import org.json4s.native.Serialization._

import scala.concurrent.Future
import scala.util.parsing.json

object Level2WebsocketSource {
  def getSource(productId: String)(implicit actorSystem: ActorSystem, actorMaterializer: ActorMaterializer): Source[Level2Data, Future[ws.WebSocketUpgradeResponse]] = {

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

    implicit val formats = DefaultFormats

    outgoing
      .viaMat(flow)(Keep.right)
      .mapAsync(1)(wsMessageToString)
      .map(jsonStr => parse(jsonStr))
      .filter(json => List("snapshot", "l2update").contains((json \ "type").extract[String]))
      .map {
        case json if (json \ "type").extract[String] == "snapshot" =>
          val bids = (json \ "bids").extractOrElse[List[List[String]]](Nil)
            .map {
              case List(price, size) => OrderItem(BigDecimal(price), BigDecimal(size))
            }
          val asks = (json \ "asks").extractOrElse[List[List[String]]](Nil)
            .map {
              case List(price, size) => OrderItem(BigDecimal(price), BigDecimal(size))
            }
          Snapshot(bids, asks)
        case json if (json \ "type").extract[String] == "l2update" =>
          val (bids, asks) = (json \ "changes").extractOrElse[List[List[String]]](Nil)
            .map {
              case List(typo, price, size) =>
                (typo, OrderItem(BigDecimal(price), BigDecimal(size)))
            }
            .span {
              case (typo, orderItem) => typo == "buy"
            }
          L2Update(bids.map(_._2), asks.map(_._2))
      }

  }
}

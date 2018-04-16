package cc.kamma.datalake

import akka.actor.{Actor, ActorLogging, Props}
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.Sink

object Level2Actor {

  def props(productId: String) = Props(new Level2Actor(productId))
}

class Level2Actor(productId: String) extends Actor with ActorLogging {

  implicit val system = context.system
  implicit val materializer = ActorMaterializer()

  Level2WebsocketSource.getSource(productId)
    .to(Sink.actorRef(self, Complete()))
    .run()

  override def receive: Receive = {
    case x => println(x)
  }
}

case class Complete()
class Level2Data
case class OrderItem(price: BigDecimal, size: BigDecimal)
case class Snapshot(bids: List[OrderItem], asks: List[OrderItem]) extends Level2Data
case class L2Update(bids: List[OrderItem], asks: List[OrderItem]) extends Level2Data

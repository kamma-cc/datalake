package cc.kamma.datalake

import akka.actor.{Actor, Props}

object Level2Actor {

  def props(productId: String) = Props(new Level2Actor(productId))
}

class Level2Actor(productId: String) extends Actor {
  override def receive: Receive = ???
}

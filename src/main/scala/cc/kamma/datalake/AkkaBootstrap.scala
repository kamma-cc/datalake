package cc.kamma.datalake

import akka.Done
import akka.actor.ActorSystem
import akka.http.scaladsl._
import akka.http.scaladsl.model.ws._
import akka.stream.ActorMaterializer
import akka.stream.scaladsl._
import org.springframework.boot.{ApplicationArguments, ApplicationRunner}
import org.springframework.stereotype.Component

import scala.concurrent.Future


@Component
class AkkaBootstrap extends ApplicationRunner {
  override def run(args: ApplicationArguments): Unit = {


  }
}

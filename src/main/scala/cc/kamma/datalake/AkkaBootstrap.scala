package cc.kamma.datalake

import akka.actor.ActorSystem
import org.springframework.boot.{ApplicationArguments, ApplicationRunner}
import org.springframework.stereotype.Component


@Component
class AkkaBootstrap extends ApplicationRunner {
  override def run(args: ApplicationArguments): Unit = {
    val system = ActorSystem()

    system.actorOf(Level2Actor.props("BTC-USD"))

  }
}

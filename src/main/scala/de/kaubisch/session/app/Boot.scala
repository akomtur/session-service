package de.kaubisch.session.app

import akka.actor.{ActorSystem, Props}
import akka.io.IO
import akka.pattern.ask
import akka.util.Timeout
import de.kaubisch.session.RestSessionService
import spray.can.Http

import scala.concurrent.duration._

/**
 * Created by kaubisch on 04.02.15.
 */
object Boot extends App {
  implicit val system = ActorSystem("session-system")
  val service = system.actorOf(Props[SessionServiceActor], "session-rest-service")

  implicit val timeout = Timeout(30.seconds)
  IO(Http) ? Http.Bind(service, interface = "localhost", port = 8080)
}

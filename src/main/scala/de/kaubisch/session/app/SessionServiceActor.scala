package de.kaubisch.session.app

import akka.actor.{Actor, ActorLogging, ActorRefFactory}
import de.kaubisch.session.RestSessionService
import spray.routing.RoutingSettings

class SessionServiceActor extends Actor with RestSessionService with ActorLogging {

  implicit val rSettings = RoutingSettings.default(context)

  override def receive: Receive = runRoute(sessionRoutes)

  override implicit def actorRefFactory: ActorRefFactory = context
}

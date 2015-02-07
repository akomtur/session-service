package de.kaubisch.session.app

import akka.actor.{Actor, ActorLogging, ActorRefFactory}
import de.kaubisch.session.RestSessionService
import spray.routing.RoutingSettings

/**
 * Created by kaubisch on 06.02.15.
 */
class SessionServiceActor extends Actor with RestSessionService with ActorLogging {

  implicit val rSettings = RoutingSettings.default(context)

  override def receive: Receive = runRoute(sessionRoutes)

  override implicit def actorRefFactory: ActorRefFactory = context
}

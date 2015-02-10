package de.kaubisch.session

import akka.actor.Props
import akka.pattern.ask
import akka.util.Timeout
import de.kaubisch.session.api.SessionJsonConverter._
import de.kaubisch.session.api._
import de.kaubisch.session.api.service._
import spray.http.HttpResponse
import spray.http.StatusCodes._
import spray.httpx.SprayJsonSupport._
import spray.routing.HttpService
import spray.routing.directives.RouteDirectives

import scala.concurrent.duration.DurationInt

trait RestSessionService extends RouteDirectives with HttpService {

  implicit val timeout = Timeout(30.seconds)
  implicit val ec = actorRefFactory.dispatcher

  val service = actorRefFactory.actorOf(Props(new PersistSessionsService(SessionPersistentActor.props)), "session-service")

  val sessionRoutes = {
    pathPrefix("session") {
      get {
        path(Segment) { sessionId: String =>
          complete {
            (service ? GetSession(sessionId)).mapTo[Option[Session]]
          }
        }
      } ~ post {
        path("new") {
          complete {
            (service ? NewSession).mapTo[Option[Session]] map {
              case None => HttpResponse(404)
              case Some(session) => HttpResponse(status=Created, entity=s"/session/${session.id}")
            }
          }
        } ~ path(Segment / Segment) { (sessionId: String, attributeName: String) =>
          entity(as[String]) { body: String =>
            complete {
              service ? AddAttribute(sessionId, attributeName, body) map {
                case _ => HttpResponse(Created)
              } recover {
                case ise: IllegalStateException => HttpResponse(NotAcceptable)
              }
            }
          }
        }
      } ~ delete {
        path(Segment) { sessionId: String =>
          complete {
            service ? DeleteSession(sessionId) map {
              case _ => HttpResponse(OK)
            } recover {
              case _ => {
                HttpResponse(NotAcceptable)
              }
            }
          }
        } ~ path(Segment / Segment) { (sessionId: String, attributeName: String) =>
          complete {
            service ? RemoveAttribute(sessionId, attributeName) map {
              case _ => HttpResponse(OK)
            } recover {
              case _ => HttpResponse(NotAcceptable)
            }
          }
        }
      }
    }
  }

}

package de.kaubisch.api

import akka.actor.{ActorLogging, ActorRefFactory}
import akka.persistence.journal.JournalSpec
import de.kaubisch.session.RestSessionService
import de.kaubisch.session.api.Session
import org.specs2.mutable.Specification
import spray.routing.HttpService
import spray.testkit.Specs2RouteTest
import spray.http.StatusCodes._
import de.kaubisch.session.api.SessionJsonConverter._

/**
 * Created by kaubisch on 06.02.15.
 */
class SessionRouteSpec extends Specification with Specs2RouteTest with HttpService with RestSessionService {
  override def actorRefFactory: ActorRefFactory = system

  sequential

  var sessionId : String = ""

  "the session service endpoint" should {
    "return a 404 when a non existing session id is requested" in {
      Get("/session/123456") ~> sessionRoutes ~> check {
        status mustEqual NotFound
      }
    }

    "return a 406 when adding a new attribute to a non existing session" in {
      Post("/session/12345/attribute", "attributeValue") ~> sessionRoutes ~> check {
        status mustEqual NotAcceptable
      }
    }

    "return 406 when deleting a non existent session" in {
      Delete("/session/12345") ~> sessionRoutes ~> check {
        status mustEqual NotAcceptable
      }
    }

    "create session when requesting a new one" in {
      Post("/session/new") ~> sessionRoutes ~> check {
        status mustEqual Created
        val response = responseAs[String]
        sessionId = response.substring(response.lastIndexOf("/")+1)
        response must be contain "/session/"
      }
    }

    "get session when requesting an existing one" in {
      Get(s"/session/$sessionId") ~> sessionRoutes ~> check {
        println(s"/session/$sessionId")
        status mustEqual OK
        val session = responseAs[Session]
        session.id mustEqual sessionId
      }
    }

    "return 201 when adding an attribute to an existing session" in {
      Post(s"/session/$sessionId/attribute", "attributeValue") ~> sessionRoutes ~> check {
        status mustEqual Created
      }
    }

    "return session with new attribute after adding it" in {
      Get(s"/session/$sessionId") ~> sessionRoutes ~> check {
        val session = responseAs[Session]
        session.attributes.get("attribute") mustEqual Some("attributeValue")
      }
    }

    "return 406 when deleting missing attribute" in {
      Delete(s"/session/$sessionId/otherAttribute") ~> sessionRoutes ~> check {
        status mustEqual NotAcceptable
      }
    }

    "return 200 when deleting existing attribute" in {
      Delete(s"/session/$sessionId/attribute") ~> sessionRoutes ~> check {
        status mustEqual OK
      }
    }

    "return 200 when deleting existing session" in {
      Delete(s"/session/$sessionId") ~> sessionRoutes ~> check {
        status mustEqual OK
      }
    }
  }
}

package de.kaubisch.session.api

import spray.httpx.SprayJsonSupport
import spray.json.DefaultJsonProtocol

object SessionJsonConverter extends DefaultJsonProtocol {
  implicit val sessionFormat = jsonFormat2(Session)
  implicit val sessionUnmarshaller = SprayJsonSupport.sprayJsonUnmarshaller[Session]
}

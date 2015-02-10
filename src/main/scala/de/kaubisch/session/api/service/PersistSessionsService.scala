package de.kaubisch.session.api.service

import java.util.UUID

import akka.actor.{Actor, ActorLogging, ActorRef, Props}
import akka.pattern.{ask, pipe}
import akka.util.Timeout
import de.kaubisch.session.api._

import scala.concurrent.Future
import scala.concurrent.duration.DurationInt


case class GetSession(uuid : String)
case class NewSession()
case class DeleteSession(uuid : String)
case class AddAttribute(uuid: String, key: String, value : String)
case class RemoveAttribute(uuid: String, key: String)

class PersistSessionsService(propsFactory: String => Props) extends Actor with ActorLogging {
  implicit val timeout = Timeout(10.seconds)
  implicit val ec = context.dispatcher

  override def receive: Receive = {
    case NewSession => {
      val id: String = createId()
      log.info(s"new id is $id")
      getById(id) ! CreateSession
      self forward GetSession(id)
    }

    case GetSession(id) =>
      log.info(s"get session for id $id")
      val future: Future[Option[Session]] = (getById(id) ? Get).mapTo[Option[Session]]
      val origin = sender()
      future.onFailure {
        case _ => origin ! None
      }
      future.foreach { value : Option[Session] =>
        log.info("GetSession: value is "+value)
        origin ! value
      }
    case AddAttribute(id, key, value) =>
      pipe(getById(id) ? AddSessionAttribute(key, value)) to sender()

    case DeleteSession(id) =>
      pipe(getById(id) ? DestroySession) to sender()
    case RemoveAttribute(id, key) =>
      pipe(getById(id) ? RemoveSessionAttribute(key)) to sender()
  }

  private def getById(name : String) : ActorRef = {
    val actorName = s"persistent-$name"
    context.child(actorName) getOrElse context.actorOf(propsFactory(name).withDispatcher("receiver-dispatcher"), actorName)
  }

  private def createId() : String = UUID.randomUUID().toString;
}

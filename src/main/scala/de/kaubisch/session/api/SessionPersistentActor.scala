package de.kaubisch.session.api

import akka.actor.{Cancellable, PoisonPill, Props, ActorLogging}
import akka.actor.Status.{Success, Failure}
import akka.persistence.{SnapshotOffer, PersistentActor}

import scala.concurrent.duration._

sealed trait Cmd
case object CreateSession extends Cmd
case object TouchSession extends Cmd
case object DestroySession extends Cmd
case class AddSessionAttribute(key: String, value: String) extends Cmd
case class RemoveSessionAttribute(key: String) extends Cmd

sealed trait Evt
case class SessionCreated(id: String) extends Evt
case class AttributeAdded(key: String, value: String) extends Evt
case class AttributeRemoved(key: String) extends Evt
case object SessionDestroyed extends Evt

case object Get

class SessionPersistentActor(uuid: String) extends PersistentActor with ActorLogging {
  var state : Session = null
  implicit val ec = context.dispatcher

  override def persistenceId: String = uuid

  private var poisonTimer: Cancellable = createKillTimer

  def createKillTimer: Cancellable = {
    context.system.scheduler.scheduleOnce(1 minutes, self, PoisonPill)
  }

  override def postStop(): Unit = log.debug(s"SessionPersistentActor with id $uuid stopped.")

  override def receiveRecover: Receive = {
    case SnapshotOffer(_, snapshot: Session) => state = snapshot
    case e: Evt => updateState(e)
  }

  override def receiveCommand: Receive = init

  def init : Receive = {
    case CreateSession => persist(SessionCreated(uuid))(updateState)
    case _ => sender ! Failure(new IllegalStateException)
  }

  def created : Receive = updateTime andThen {
    case AddSessionAttribute(key, value) => {
      persist(AttributeAdded(key, value))(updateState)
      sendOk
    }
    case RemoveSessionAttribute(key) => {
      if(state.attributes.contains(key)) {
        persist(AttributeRemoved(key))(updateState)
       sendOk
      } else {
        sender() ! Failure(new IllegalArgumentException(s"attribute with name $key not found."))
      }
    }
    case DestroySession => {
      log.debug("delete session")
      persist(SessionDestroyed)(updateState)
      sendOk
    }
    case Get => {
      log.info("state is " + state.toString)
      sender ! Some(state)
    }
  }

  def updateTime: PartialFunction[Any, Any] = {
    case msg: Any => {
      if(!poisonTimer.isCancelled) {
        log.debug("canceling kill timer because the actor gets a request")
        poisonTimer.cancel()
      }
      poisonTimer = createKillTimer
      msg
    }
  }

  def sendOk: Unit = {
    sender() ! Success("ok")
  }
  
  def updateState(event :Evt) = {
    event match {
      case SessionCreated(id) => {
        state = Session(id)
        context become created
      }
      case AttributeAdded(key, value) => state = state.copy(attributes = state.attributes + (key -> value))
      case AttributeRemoved(key) => state = state.copy(attributes = state.attributes - key)
      case SessionDestroyed => {
        state = null
        deleteMessages(lastSequenceNr)
        context.become(init)
      }
    }
  }

}

object SessionPersistentActor {
  def props(name : String) : Props = Props(classOf[SessionPersistentActor], name)
}


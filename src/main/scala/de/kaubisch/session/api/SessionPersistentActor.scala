package de.kaubisch.session.api

import akka.actor.ActorLogging
import akka.actor.Status.{Success, Failure}
import akka.persistence.{SnapshotOffer, PersistentActor}


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
/**
 * Created by kaubisch on 02.02.15.
 */
class SessionPersistentActor(uuid: String) extends PersistentActor with ActorLogging {
  var state : Session = null

  override def persistenceId: String = uuid

  override def receiveRecover: Receive = {
    case SnapshotOffer(_, snapshot: Session) => state = snapshot
    case e: Evt => updateState(e)
  }

  override def receiveCommand: Receive = init

  def init : Receive = {
    case CreateSession => persist(SessionCreated(uuid))(updateState)
    case _ => sender ! Failure(new IllegalStateException)
  }
  def created : Receive = {
    case AddSessionAttribute(key, value) => {
      persist(AttributeAdded(key, value))(updateState)
      sender() ! Success("ok")
    }
    case RemoveSessionAttribute(key) => {
      if(state.attributes.contains(key)) {
        persist(AttributeRemoved(key))(updateState)
        sender() ! Success("ok")
      } else {
        sender() ! Failure(new IllegalArgumentException(s"attribute with name $key not found."))
      }
    }
    case DestroySession => {
      log.debug("delete session")
      persist(SessionDestroyed)(updateState)
      sender() ! Success("ok")
    }
    case Get => {
      log.info("state is " + state.toString)
      sender ! Some(state)
    }
  }

  def updateState(event :Evt) = {
    event match {
      case SessionCreated(id) => {
        state = Session(id)
        context.become(created)
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


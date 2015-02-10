package de.kaubisch.session.api

case class Session(id: String, attributes : Map[String, String] = Map.empty)

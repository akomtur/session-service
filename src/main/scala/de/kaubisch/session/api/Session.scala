package de.kaubisch.session.api

/**
 * Created by kaubisch on 07.02.15.
 */
case class Session(id: String, attributes : Map[String, String] = Map.empty)

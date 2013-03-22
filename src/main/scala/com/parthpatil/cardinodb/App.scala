package com.parthpatil.cardinodb

import com.twitter.finatra._
import com.twitter.finatra.ContentType._
import com.twitter.ostrich.stats.Stats
import scala.collection.mutable
import com.googlecode.javaewah.EWAHCompressedBitmap

object App {

  val KeySeparator = ":"

  // Map of string key to BitSet
  val db = mutable.Map[String, EWAHCompressedBitmap]()

  /**
   * Parse the attributes from string
   * e.g country=US,gender=M,client=FF is converted to following
   * Map("country" -> "US", "gender" -> "M", "client" -> "FF")
   *
   * @param raw
   * @return
   */
  def parseAttributes(raw: String): Map[String, String] = {
    val map = mutable.Map[String, String]()
    raw.split(",") map { x: String =>
      val items = x.split("=")
      map += items(0) -> items(1)
    }

    map.toMap
  }

  /**
   * Add userId to the appropriate BitSet
   */
  def addToDB(app: String, userId: String, attributes: Map[String, String]) {
    attributes foreach { case (dim, dimValue) =>
      val dbKey = getKey(app, dim, dimValue)

      db.getOrElseUpdate(dbKey, new EWAHCompressedBitmap()).set(userId.toInt)
    }
  }

  /**
   * Get the cardinality of the intersection of the BitSets
   */
  def getCardinality(app: String, attributes: Map[String, String]): Int = {
    val bitSets = attributes.flatMap { case (dim, value) =>
      val dbKey = getKey(app, dim, value)
      db.get(dbKey)
    }

    // Take intersection of all the BitSets
    val finalResult = bitSets.foldLeft(bitSets.head) { _ and _ }

    finalResult.cardinality()
  }

  /**
   * Construct the key for the db
   *
   * @param app
   * @param dim
   * @param value
   * @return
   */
  def getKey(app: String, dim: String, value: String): String = {
    app + KeySeparator + dim.toLowerCase + KeySeparator + value.toLowerCase
  }

  class ExampleApp extends Controller {

    get("/update/app/:app/userid/:userid/attributes/:attributes") { request =>
      val app: String = request.routeParams.getOrElse("app", "default_app")
      val userId: String = request.routeParams.getOrElse("userid", "0")
      val raw_attributes: String = request.routeParams.getOrElse("attributes", "default_attributes")

      val attributes = parseAttributes(raw_attributes)
      addToDB(app, userId, attributes)

      render.plain("OK").toFuture
    }

    get("/get_cardinality/app/:app/attributes/:attributes") { request =>
      val app: String = request.routeParams.getOrElse("app", "default_app")
      val raw_attributes: String = request.routeParams.getOrElse("attributes", "default_attributes")

      val attributes = parseAttributes(raw_attributes)

      val cardinality = getCardinality(app, attributes)

      render.plain(cardinality.toString).toFuture
    }
  }

  val app = new ExampleApp

  def main(args: Array[String]) = {
    FinatraServer.register(app)
    FinatraServer.start()
  }
}

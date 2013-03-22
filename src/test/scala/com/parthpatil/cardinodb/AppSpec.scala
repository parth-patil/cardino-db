package com.parthpatil.cardinodb

import org.scalatest.FlatSpec
import org.scalatest.matchers.ShouldMatchers
import com.twitter.finatra.test._
import com.parthpatil.cardinodb._

class AppSpec extends SpecHelper {

  val app = new App.ExampleApp

  "GET /update with user attributes" should "respond with OK" in {
    get("/update/app/uniq_users/userid/1/attributes/gender=M,country=US,client=FF")
    response.body should equal ("OK")
    response.code should equal (200)
  }

  "GET /get_cardinality for data set containing 1 user" should "respond with 1" in {
    // Insert data for test
    get("/update/app/uniq_users/userid/1/attributes/gender=M,country=US,client=FF")
    get("/update/app/uniq_users/userid/2/attributes/gender=F,country=US,client=FF")
    get("/update/app/uniq_users/userid/3/attributes/gender=F,country=IN,client=Chrome")

    // Check expected cardinality
    get("/get_cardinality/app/uniq_users/attributes/gender=M")
    response.body should equal ("1")
    response.code should equal (200)
  }

  "GET /get_cardinality for data set containing 2 users" should "respond with 2" in {
    // Insert data for test
    get("/update/app/uniq_users/userid/1/attributes/gender=M,country=US,client=FF")
    get("/update/app/uniq_users/userid/2/attributes/gender=M,country=US,client=FF")
    get("/update/app/uniq_users/userid/3/attributes/gender=F,country=IN,client=Chrome")

    // Check expected cardinality
    get("/get_cardinality/app/uniq_users/attributes/country=US")
    response.body should equal ("2")
    response.code should equal (200)
  }
}

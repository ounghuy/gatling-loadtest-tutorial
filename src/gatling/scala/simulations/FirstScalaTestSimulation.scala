package simulations

import io.gatling.core.Predef._
import io.gatling.core.structure.ChainBuilder
import io.gatling.http.Predef._

import scala.concurrent.duration._

class FirstScalaTestSimulation extends Simulation {

  val baseUrl = "https://jsonplaceholder.typicode.com"

  val httpProtocol = http
    .baseUrl(baseUrl)
    .userAgentHeader("Mozilla/5.0 (Windows; U; Windows NT 6.1; en-US; rv:1.9.1.5) Gecko/20091102 Firefox/3.5.5 (.NET CLR 3.5.30729)")

  val scn1 = scenario("Scenario 1")
    .exec(RandomLoader.load)

  setUp(scn1.inject(
    constantUsersPerSec(1) during(10 seconds)
  ).protocols(httpProtocol)
  )
}

object RandomLoader {

  val header_0 = Map("Accept" -> "application/json", "Accept-Language" -> "en-US")
  val load = randomSwitch(
  100.00 -> Users.getUsersFromFeed,


  )

  object Users {

    val usersCSV = csv("users.csv").random
    def getUser(id: Int): ChainBuilder = {
      exec(http(s"Get user with id ${id}")
        .get(s"/users/${id}")
        .check(jsonPath("$..name").exists.saveAs("userName")))
        .exec(session => {
          println(s"user id ${id} with name is " + session("userName").as[String]); session
        })
    }

    val getUser = {
      exec(http("Get user")
        .get("/users/1")
        .check(jsonPath("$..name").exists.saveAs("userName")))
        .exec(session => {
          println("user name is " + session("userName").as[String]);
          session
        })
    }

    val getUsersFromFeed = {
      feed(usersCSV)
        .exec(
          http("Get user ${id}")
            .get("/users/${id}")
            .check(jsonPath("$..name").exists.saveAs("name"), jsonPath("$..id").exists.saveAs("id")))
        .exec(s => {
          println(String.format("Get user of [%s - %s]", s("id").as[String], s("name").as[String]));s
        })
    }
  }

}
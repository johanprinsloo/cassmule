package com.example

import cc.spray._
import com.example.mule._
import cc.spray.directives.LongNumber

trait TestService extends Directives {

  val testService = {
    path("") {
      get {
        _.complete("Spray with embedded Cassandra store\n\t Try /api/evse/1")
      }
    } ~
      pathPrefix("api/evse") {
        path(LongNumber) {
          evseId =>
            get {
              _.complete(MulePen.getEvse(evseId))
            } ~
            put {
              content(as[String]) {
                evseContent =>
                  MulePen.putEvse(evseContent, evseId)
                  _.complete("OK for put evse")
              }
            }
        }

      } ~
    path("mu-a6c84d2e-1b3b9037-722e9eba-fccf6d7a") {
      _.complete("42")
    }
  }

}
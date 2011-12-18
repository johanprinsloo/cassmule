package com.example

import cc.spray._
import com.example.mule._
import cc.spray.directives.LongNumber

trait TestService extends Directives {

  val testService = {
    path("") {
      get { _.complete("It works!") }
    } ~
      pathPrefix("api/evse") {
        path(LongNumber) {
          evseId =>
            get {
              _.complete(MulePen.getEvse(evseId))
            } ~
              put {
                content(as[String]) { evseContent =>
                  MulePen.putEvse(evseContent, evseId)
                  _.complete("OK for put evse")
                }
              }
        }

      }
  }

}
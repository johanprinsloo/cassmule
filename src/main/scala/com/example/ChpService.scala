package com.example

import cc.spray._
import com.example.mule._
import cc.spray.directives.LongNumber

trait ChpService extends Directives {

  val evseService = {
    path("") {
      get {
        _.complete("Alt Spray with embedded Cassandra store\n\t Try /api/evse/1")
      }
    } ~
      pathPrefix("api/chp") {
        path(LongNumber) {
          evseId =>
            get {
              _.complete(MulePen.getEvseFree(evseId))
            } ~
            put {
                content(as[String]) {
                  evseContent =>
                    _.complete(MulePen.putEvseFree(evseContent, evseId))
                }
              }
        }

      } ~
      pathPrefix("api/chp/dev") {
        path(LongNumber) {
          evseId =>
            get {
              _.complete(MulePen.getEvseDevice(evseId))
            } ~
              put {
                content(as[String]) {
                  evseContent =>
                    _.complete(MulePen.putEvseDevice(evseContent, evseId))
                }
              }
        }

      } ~
      pathPrefix("api/evse/chp") {
        path(LongNumber) {
          evseId =>
            get {
              _.complete(MulePen.getEvseChp(evseId))
            } ~
              put {
                content(as[String]) {
                  evseContent =>
                    _.complete(MulePen.putEvseChp(evseContent, evseId))
                }
              }
        }
      } ~
      pathPrefix("api/evse/chp") {
        path(LongNumber) {
          evseId =>
            get {
              _.complete(MulePen.getEvseChp(evseId))
            } ~
              put {
                content(as[String]) {
                  evseContent =>
                    _.complete(MulePen.putEvseChp(evseContent, evseId))
                }
              }
        }
      }
  }

}
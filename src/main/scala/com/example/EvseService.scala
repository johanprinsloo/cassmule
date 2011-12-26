package com.example

import cc.spray._
import com.example.mule._
import cc.spray.directives.LongNumber

trait EvseService extends Directives {

  val evseService = {
    path("") {
      get {
        _.complete("Spray with embedded Cassandra store\n\t Try /api/evse/1")
      }
    } ~
    pathPrefix("api/evse") {
      path(LongNumber) {
        evseId =>
          get {
            _.complete(MulePen.getEvseFree(evseId))
          } ~
          put {
            content(as[String]) {
              evseContent =>
                MulePen.putEvseFree(evseContent, evseId)
                _.complete("OK for put evse")
            }
          }
      }

    } ~
    pathPrefix("api/evse/dev") {
      path(LongNumber) {
        evseId =>
          get {
            _.complete(MulePen.getEvseFree(evseId))
          } ~
            put {
              content(as[String]) {
                evseContent =>
                  MulePen.putEvseFree(evseContent, evseId)
                  _.complete("OK for put evse")
              }
            }
      }

    } ~
    pathPrefix("api/evse/chp") {
      path(LongNumber) {
        evseId =>
          get {
            _.complete(MulePen.getEvseFree(evseId))
          } ~
            put {
              content(as[String]) {
                evseContent =>
                  MulePen.putEvseFree(evseContent, evseId)
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
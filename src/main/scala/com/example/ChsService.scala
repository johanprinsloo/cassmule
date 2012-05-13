package com.example

import cc.spray._
import com.example.mule._
import directives.{IntNumber, LongNumber}
import http.{StatusCodes, HttpHeader, HttpResponse}
import tools.nsc.io.Path

trait ChsService extends Directives {

  import StatusCodes._
  val stdHeaders = HttpHeader("Bogus", "bogoheadvalue")::List()

  val chsService = {
    pathPrefix("api/evse" / IntNumber / "chs") {
      evseId =>
        path("") {
          get {
            _.complete("GET the currect changing session information")
          } ~
            put {
              content(as[String]) {
                chsContent =>
                  println("Completing a PUT to api/evse/"+evseId+"/chs/")
                  _.complete( MuleCage.postSession(evseId, chsContent))
              }
            }
        } ~
          pathPrefix(IntNumber) {
            chsId =>
              path("") {
                get {
                  _.complete(">>##>>Get full info for the specific changing session: " + chsId + " for evse: " + evseId)
                } ~
                  put {
                    _.complete("PUT new chs info for : " + chsId)
                  } ~
                  post {
                    _.complete("POST new revision for : " + chsId)
                  } ~
                  delete {
                    _.complete("del " + chsId + " from all keyspaces")
                  }
              } ~
                path("metrics") {
                  get {
                    _.complete("get /api/evse/" + evseId + "/chs/" + chsId + "/metrics")
                  } ~
                  put {
                    content(as[String]) { chsContent =>
                        println("Completing a PUT to api/evse/"+evseId+"/chs/"+chsId+"/metrics")
                        _.complete( MuleCage.putMetrics(evseId,chsId,chsContent) )
                    }
                  }
                } ~
                path("status") {
                  get {
                    _.complete("get /api/evse/" + evseId + "/chs/" + chsId + "/status")
                  }
                } ~
                path("dro") {
                  get {
                    _.complete("get /api/evse/" + evseId + "/chs/" + chsId + "/dro")
                  }
                } ~
                path("pro") {
                  get {
                    _.complete("get /api/evse/" + evseId + "/chs/" + chsId + "/pro")
                  }
                }
          }
    }
  }
}
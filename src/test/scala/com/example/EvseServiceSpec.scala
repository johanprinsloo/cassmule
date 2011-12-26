package com.example

import org.specs2.mutable._
import cc.spray._
import http.HttpContent._
import test._
import http._
import HttpMethods._
import StatusCodes._
import cc.spray.http.MediaTypes._

class EvseServiceSpec extends Specification with SprayTest with EvseService {

  val rootTestContent = "Spray with embedded Cassandra store\n\t Try /api/evse/1"
  val evseTestContent = """{"key1":"value1","key2":"value2","key3":"value3"}"""
  
  "The Test Service" should {
    "return a greeting for GET requests to the root path" in {
      testService(HttpRequest(GET, "/")) {
        evseService
      }.response.content.as[String] mustEqual Right(rootTestContent)
    }

    "leave GET requests to other paths unhandled" in {
      testService(HttpRequest(GET, "/kermit")) {
        evseService
      }.handled must beFalse
    }

    "return a MethodNotAllowed error for POST requests to the root path" in {
      testService(HttpRequest(POST, "/")) {
        evseService
      }.response mustEqual HttpResponse(MethodNotAllowed, "HTTP method not allowed, supported methods: GET")
    }

    "PUT and GET freeform data in evse id 1 " in {
      testService(HttpRequest(PUT, "/api/evse/1",Nil, Some( HttpContent( evseTestContent )) ) ) {
        evseService
      }.handled must beTrue

      testService(HttpRequest(GET, "/api/evse/1" ) ) {
        evseService
      }.response.content.as[String] mustEqual Right(evseTestContent)
    }

  }
  
}
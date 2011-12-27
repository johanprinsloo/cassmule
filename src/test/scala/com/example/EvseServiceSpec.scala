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
  val evseFreeTestContent = """{"key1":"value1","key2":"value2","key3":"value3"}"""
  val evseDevTestContent =  """{"name":"testdevice","netAddress":"54:8f:t5:g7:2r:8u","manufacturer":"ACME","model":"FX300","serial":"98776544532321"}"""
  
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
      testService(HttpRequest(PUT, "/api/evse/1",Nil, Some( HttpContent( evseFreeTestContent )) ) ) {
        evseService
      }.handled must beTrue

      testService(HttpRequest(GET, "/api/evse/1" ) ) {
        evseService
      }.response.content.as[String] mustEqual Right(evseFreeTestContent)
    }

    "PUT badly structured device data in evse id 1 " in {
      testService(HttpRequest(PUT, "/api/evse/dev/1",Nil, Some( HttpContent( evseFreeTestContent )) ) ) {
        evseService
      }.response.status mustEqual BadRequest
    }

    "PUT well structured device data in evse id 1 " in {
      testService(HttpRequest(PUT, "/api/evse/dev/1",Nil, Some( HttpContent( evseDevTestContent )) ) ) {
        evseService
      }.response.status mustEqual Accepted
    }

    "GET well structured device data from evse id 1 " in {
      testService(HttpRequest(GET, "/api/evse/dev/1") ) {
        evseService
      }.response mustEqual HttpResponse(OK, HttpContent( ContentType(`application/json`), evseDevTestContent) )

    }

  }
  
}
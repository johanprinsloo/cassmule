package com.example

import org.specs2.mutable._
import cc.spray._
import http.HttpContent._
import test._
import http._
import HttpMethods._
import StatusCodes._

class TestServiceSpec extends Specification with SprayTest with TestService {

  val evseTestContent = """{"key1":"value1","key2":"value2"}"""
  
  "The Test Service" should {
    "return a greeting for GET requests to the root path" in {
      testService(HttpRequest(GET, "/")) {
        testService
      }.response.content.as[String] mustEqual Right("It works!")
    }

    "leave GET requests to other paths unhandled" in {
      testService(HttpRequest(GET, "/kermit")) {
        testService
      }.handled must beFalse
    }

    "return a MethodNotAllowed error for POST requests to the root path" in {
      testService(HttpRequest(POST, "/")) {
        testService
      }.response mustEqual HttpResponse(MethodNotAllowed, "HTTP method not allowed, supported methods: GET")
    }

//    "PUT standard data in evse id 1 " in {
//      testService(HttpRequest(PUT, "/api/evse/1",Nil, Some( HttpContent( evseTestContent )) ) ) {
//        testService
//      }.handled must beTrue
//    }
  }
  
}
package com.example

import domain._
import mule.MuleParameters._
import org.specs2.mutable._
import cc.spray._
import http.HttpContent._
import test._
import http._
import HttpMethods._
import StatusCodes._
import com.shorrockin.cascal.session._
import compat.Platform
import cc.spray.json._
import cc.spray.json.DefaultJsonProtocol._



class ChsServiceSpec extends Specification with SprayTest with ChsService {

  val hosts = Host(clusterHost, clusterPort, 250) :: Nil
  val params = new PoolParams(10, ExhaustionPolicy.Fail, 500L, 6, 2)
  val pool = new SessionPool(hosts, params, Consistency.One)

  "The Charging Session Service" should {

    "return a 201 for PUT requests to the evse/chs path" in {
      val resp = testService( HttpRequest(PUT, "/api/evse/1/chs/",Nil, Some( HttpContent( "{}" )))) {
        chsService
      }.response

      resp.status mustEqual Created
      resp.headers.map( hdr => hdr.name ).mkString must contain( "Location" ) and contain( "ID" )

      val chsId = resp.headers.find {e => e.name == "ID"}.get.value.toLong

      resp.headers must contain( HttpHeader("Location", "http://{baseurl}/api/evse/1/chs/"+chsId) )

      resp.status mustEqual Created
    }

    "Accept a stream of metrics" in {

      val resp = testService( HttpRequest(PUT, "/api/evse/1/chs/",Nil, Some( HttpContent( "{}" )))) {
        chsService
      }.response

      resp.status mustEqual Created
      resp.headers.map( hdr => hdr.name ).mkString must contain( "Location" ) and contain( "ID" )

      val chsId = resp.headers.find {e => e.name == "ID"}.get.value.toLong

      resp.headers must contain( HttpHeader("Location", "http://{baseurl}/api/evse/1/chs/"+chsId) )

      resp.status mustEqual Created

     val now = Platform.currentTime

      import ChargingMetricsProtocol._

      val metrixes = 1 to 10 map ( i => {
        val cmj = ChargingMetrics( Some(now + (1000*i)),i, 12.654,0.654, 45.654, 1, 0.6547 ).toJson.compactPrint
         testService( HttpRequest(PUT, "/api/evse/1/chs/"+chsId+"/metrics", Nil, Some( HttpContent( cmj )))) {
             chsService
         }.response
      })

      println( metrixes(2).toString )


      true mustEqual true
    }



  }

}
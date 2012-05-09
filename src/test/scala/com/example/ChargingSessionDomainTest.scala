package com.example


import domain._
import org.scalatest.FunSuite
import org.scalatest.matchers.ShouldMatchers

import compat.Platform
import cc.spray.json._
import cc.spray.json.DefaultJsonProtocol._

class ChargingSessionDomainTest extends FunSuite with ShouldMatchers {

  test("Charging Session Domain Objects with optional data supplied") {

    import ChsProtocol._
    val ct = Platform.currentTime
    val chs1 = ChargingSession( "Active",
                                6.9856,
                                ct,
                                "USD",
                                ChargingMetrics(11054,12.654,6.321,2.3654,15,0.654, Some("https://base/api/v1/evse/{id}/chs/{id}/metrics")),
                                PriceOverride(false),
                                DemandResponseOverride(false, Some( "https://base/api/v1/evse/{id}/chs/{id}/pro")),
                                Some( Pev(1, Some("https://base/api/v1/pev/1")) ),
                                Some("https://base/api/v1/evse/{id}/chs/{id}")
                              )

    assert( chs1.totalCost.isInstanceOf[BigDecimal] == true)
    chs1.PEV.get.href should be (Some("https://base/api/v1/pev/1"))

    chs1 should have (
      'sessionStatus ("Active"),
      'startDateTime (ct)
    )

    val chsjs1 = chs1.toJson
    assert( chsjs1.isInstanceOf[JsValue] == true )

    val jsonstr = chsjs1.prettyPrint
    info( "Full Chs:\n" + jsonstr )

    jsonstr should include ("USD")
    jsonstr should include ("https://base/api/v1/evse/{id}/chs/{id}")
    jsonstr should include ("https://base/api/v1/pev/1")

    val newSession = chsjs1.convertTo[ChargingSession]
    assert(newSession.isInstanceOf[ChargingSession] == true)
    newSession should be === (chs1)

    val parsedJson = JsonParser(jsonstr)
    assert(parsedJson.isInstanceOf[JsObject] == true)
    val newSession2 = parsedJson.convertTo[ChargingSession]
    newSession2 should be === (chs1)

  }

  test("Charging Session Domain Objects without optional data") {

    import ChsProtocol._
    val ct = Platform.currentTime
    val chs1 = ChargingSession( "Active",
      6.9856,
      ct,
      "USD",
      ChargingMetrics(11054,12.654,6.321,2.3654,15,0.654),
      PriceOverride(false),
      DemandResponseOverride(false)
    )

    assert( chs1.totalCost.isInstanceOf[BigDecimal] == true)
    chs1.PEV should be (None)

    chs1 should have (
      'sessionStatus ("Active"),
      'startDateTime (ct)
    )

    val chsjs1 = chs1.toJson
    assert( chsjs1.isInstanceOf[JsValue] == true )

    val jsonstr = chsjs1.prettyPrint
    info( "Minimal Chs:\n" + jsonstr )

    jsonstr should include ("USD")

    val newSession = chsjs1.convertTo[ChargingSession]
    assert(newSession.isInstanceOf[ChargingSession] == true)
    newSession should be === (chs1)

    val parsedJson = JsonParser(jsonstr)
    assert(parsedJson.isInstanceOf[JsObject] == true)
    val newSession2 = parsedJson.convertTo[ChargingSession]
    newSession2 should be === (chs1)
  }

  test("Charging metrics standalone"){

    import ChargingMetricsProtocol._

    val chm = ChargingMetrics(11054,12.654,6.321,2.3654,15,0.654, Some("anyurl"))

    chm.href should be (Some("anyurl"))
    chm should have (
      'secondsElapsed  (BigDecimal(11054)),
      'peakKw  (12.654),
      'averageKw  (6.321),
      'totalConsumedkWh (2.3654),
      'intervalTime (15),
      'intervalAvgKw (0.654)
    )

    val chmj = chm.toJson

    val chmjstr = chmj.compactPrint
    info( "Compact Chm:\n" + chmjstr )

    val p_chmj = JsonParser(chmjstr)
    p_chmj should be === (chmj)

    val p_chm = p_chmj.convertTo[ChargingMetrics]
    p_chm should be === (chm)

    val rjasn = """{"secondsElapsed":11054,"peakKw":12.654,"averageKw":6.321,"totalConsumedkWh":2.3654,"intervalTime":15,"intervalAvgKw":0.654,"href":"anyurl"}"""
    val p_raw = JsonParser(rjasn)
    assert(p_raw.isInstanceOf[JsObject]==true)

    val p_rchm = p_raw.convertTo[ChargingMetrics]
    p_rchm should be === (chm)

    rjasn.asJson.convertTo[ChargingMetrics] should be === (chm)

  }

}

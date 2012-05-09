package com.example.domain

import cc.spray.json.DefaultJsonProtocol

case class ChargingMetrics(
                             secondsElapsed : Int,
                             peakKw : BigDecimal,
                             averageKw : BigDecimal,
                             totalConsumedkWh : BigDecimal,
                             intervalTime : Int,
                             intervalAvgKw : BigDecimal,
                             href : Option[String] = None
                           )

object ChargingMetricsProtocol extends DefaultJsonProtocol {
  implicit val chmFormat = jsonFormat7(ChargingMetrics)
}

case class Pev (
                   ID : Long,
                   href : Option[String] = None
                 )

object PevProtocol extends DefaultJsonProtocol {
  implicit val pevFormat = jsonFormat2(Pev)
}

case class PriceOverride (
                             value : Boolean,
                             href : Option[String] = None
                           )

object ProProtocol extends DefaultJsonProtocol {
  implicit val proFormat = jsonFormat2(PriceOverride)
}


case class DemandResponseOverride (
                             value : Boolean,
                             href : Option[String] = None
                           )

object DroProtocol extends DefaultJsonProtocol {
  implicit val droFormat = jsonFormat2(DemandResponseOverride)
}


case class ChargingSession(
                            sessionStatus : String, //TOD Enum handling
                            totalCost : BigDecimal,
                            startDateTime : Long,
                            Currency : String,
                            ChargingMetrics : ChargingMetrics,
                            PriceOverride: PriceOverride,
                            DemandResponseOverride : DemandResponseOverride,
                            PEV : Option[Pev] = None,
                            href : Option[String] = None
                          )

object ChsProtocol extends DefaultJsonProtocol {
  import ChargingMetricsProtocol._
  import PevProtocol._
  import ProProtocol._
  import DroProtocol._

  implicit val chsFormat = jsonFormat9(ChargingSession)
}
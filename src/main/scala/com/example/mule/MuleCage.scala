package com.example.mule

import cc.spray.utils.Logging
import com.shorrockin.cascal.session._
import cc.spray.json._
import compat.Platform
import com.example.domain._
import cc.spray.http._
import me.prettyprint.cassandra.utils.TimeUUIDUtils


object MuleCage extends Logging {

  import com.shorrockin.cascal.utils.Conversions._
  import ChsProtocol._
  import StatusCodes._
  import MuleParameters._

  val hosts = Host(clusterHost, clusterPort, 250) :: Nil
  val params = new PoolParams(10, ExhaustionPolicy.Fail, 500L, 6, 2)
  val pool = new SessionPool(hosts, params, Consistency.One)


  def postSession(evseId: Long, content: String): HttpResponse = {
    var reCode : StatusCode = StatusCodes.InternalServerError
    var reHeaders: List[HttpHeader] = List()
    pool.borrow {
      session =>

        try {

          val now = Platform.currentTime
          val curChsCol = keyspaceMuleName \ chsColFamName \ evseId \ "CurrentChs"

          val lastChs = session.get(curChsCol).isEmpty match {
            case true => session.insert(curChsCol \ 1L); 1L
            case false => {
              long(session.get(curChsCol).get.value)
            }
          }

          //is the last session still active - close it out
          val ckey = (lastChs, "sessionStatus")
          val lscol = keyspaceMuleName \ chsColFamName \ evseId \ ckey
          session.get( lscol ).isEmpty match {
            case true =>
            case false => session.insert( lscol \ "complete")
          }

          val currChsID = lastChs + 1

          val chs: ChargingSession = attemptParseSession(content) match {
            case Some(parsedChs) => parsedChs
            case None => ChargingSession("active", 0.0, now, "USD",
                                          ChargingMetrics( Some(now), 0, 0.0, 0.0, 0.0, 0, 0),
                                          PriceOverride(false),
                                          DemandResponseOverride(false))
          }

         //insert charging session
         import DroProtocol._
         import ProProtocol._
         val schCol =  keyspaceMuleName \ chsColFamName \ evseId
         session.insert( schCol \ (currChsID, "sessionStatus") \ chs.sessionStatus )
         session.insert( schCol \ (currChsID, "totalCost") \ chs.totalCost.toString() )
         session.insert( schCol \ (currChsID, "startDateTime") \ chs.startDateTime )
         session.insert( schCol \ (currChsID, "Currency") \ chs.Currency )
         session.insert( schCol \ (currChsID, "DemandResponseOverride") \ chs.DemandResponseOverride.toJson.compactPrint)
         session.insert( schCol \ (currChsID, "PriceOverride") \ chs.PriceOverride.toJson.compactPrint)

         import ChargingMetricsProtocol._
         val schMetricsCol = keyspaceTimeName \ chsTimeSeriesColFamName \ currChsID
         val ts = TimeUUIDUtils.getTimeUUID( chs.ChargingMetrics.timeStamp.getOrElse(now) )
         session.insert( schMetricsCol \ ts \ chs.ChargingMetrics.toJson.compactPrint )

          //update current session
          session.insert(curChsCol \ currChsID)
          val currChsJson = session.get(keyspaceMuleName \ chsColFamName \ evseId \ currChsID)

          reHeaders = HttpHeader("Location", "http://{baseurl}/api/evse/" + evseId + "/chs/"+currChsID) :: reHeaders
          reHeaders = HttpHeader("ID", currChsID) :: reHeaders
          reCode = StatusCodes.Created
          session.close()

        } catch {
          case ex: Exception => {
            println(">>exc>>"); ex.printStackTrace()
            reCode = InternalServerError
            session.close()
          }
        }


    }
    HttpResponse(reCode, reHeaders)
  }




  def attemptParseSession(jsonstr: String): Option[ChargingSession] = {
    try {
      Some(JsonParser(jsonstr).convertTo[ChargingSession])
    } catch {
      case ex: Exception => println(ex.getMessage); None
    }
  }

  def putMetrics(evseId: Long, chsId: Long, content: String): HttpResponse = {
    var reCode: StatusCode = StatusCodes.InternalServerError
    var reHeaders: List[HttpHeader] = List()
    val now = Platform.currentTime

    pool.borrow {
      session =>
        try {

          import ChargingMetricsProtocol._
          val mt = JsonParser(content).convertTo[ChargingMetrics]
          val ts = mt.timeStamp.getOrElse(now)

          val schMetricsCol = keyspaceTimeName \ chsTimeSeriesColFamName \ chsId
          val tsuuid = TimeUUIDUtils.getTimeUUID( ts )
          session.insert(schMetricsCol \ tsuuid \ mt.toJson.compactPrint)


          reHeaders = HttpHeader("Location", "http://{baseurl}/api/evse/" + evseId + "/chs/"+chsId+"/metrics/"+ts) :: reHeaders
          reHeaders = HttpHeader("ID", ts) :: reHeaders
          reCode = StatusCodes.Created

          session.close()

        } catch {
          case ex: Exception => {
            println(">>exc>>");
            ex.printStackTrace()
            reCode = InternalServerError
            session.close()
          }
        }
    }

    HttpResponse(reCode, reHeaders)
  }
}
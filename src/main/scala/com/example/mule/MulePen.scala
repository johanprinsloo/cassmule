package com.example.mule

import akka.event.EventHandler
import me.prettyprint.cassandra.utils.StringUtils._
import net.liftweb.json._
import me.prettyprint.hector.api.factory.HFactory
import me.prettyprint.hector.api.factory.HFactory.createKeyspace
import me.prettyprint.hector.api.factory.HFactory.createMutator
import me.prettyprint.hector.api.factory.HFactory.createColumn
import me.prettyprint.hector.api.mutation.MutationResult
import me.prettyprint.hector.api.mutation.Mutator
import me.prettyprint.hector.api.beans.ColumnSlice
import me.prettyprint.hector.api.query.{QueryResult, SliceQuery}
import cc.spray.http._
import cc.spray.http.MediaTypes._
import cc.spray._
import com.example.domain.{EvseDomain, Evse}
import me.prettyprint.cassandra.serializers._
import utils.Logging


object MulePen extends Logging  {

  val keyspaceName = "mulespace"
  val colFamName = "evses"
  val deviceColFamName = "evse_dev"
  val chparamColFamName = "evse_chp"

  val cluster = HFactory.getOrCreateCluster("Mule Cluster", "localhost:9160");
  val ko = createKeyspace(keyspaceName, cluster)
  val se = new StringSerializer()
  val sl = new LongSerializer()
  val si = new IntegerSerializer()
  val sd = new DoubleSerializer()
  val sb = new BooleanSerializer()


  def putEvseFree(content: String, evseId: Long): HttpResponse = {

    try {
      val input = parse(content)

      val inmapp : Map[String, String] = input.values.asInstanceOf[Map[String,String]]

      val m: Mutator[String] = createMutator(ko, se)

      inmapp foreach  {
        case (key : String,  value : String) => {
          log.info(" evse : " + evseId.toString + "  inserting : " + key + " : " + value)
          m.insert(evseId.toString, colFamName, HFactory.createStringColumn(key, value))
        }
      }

      HttpResponse(StatusCodes.Created)

    } catch {
      case ex: Exception => {
        HttpResponse(StatusCodes.BadRequest, "No Write: " + ex.getCause.toString + " : " + ex.getMessage)
      }
    }
  }

  def getEvseFree(id: Long): HttpResponse = {
    try {
      val q: SliceQuery[String, String, String] = HFactory.createSliceQuery(ko, se, se ,se )
      q.setColumnFamily(colFamName)
      .setKey(id.toString)
      .setRange("a", "z", false, 20)

      val colist = q.execute().get().getColumns

      var json = "{"
      val it = colist.iterator
      while(it.hasNext ) {
        val col = it.next()
        json += ( "\"" + col.getName + "\":" + "\"" + col.getValue + "\"" )
        if( it.hasNext ) json += ","
      }
      json += "}"

      val content = HttpContent( ContentType(`application/json`), json)
      HttpResponse(200, content)

    } catch {
      case ex: Exception => HttpResponse(404, ex.getMessage + " : " + ex.toString)
    }

  }

  def putEvseDevice(content: String, evseId: Long): HttpResponse = {

    try {
      val input = parse(content)
      //put data
      val inmapp : Map[String, String] = input.values.asInstanceOf[Map[String,String]]

      //validate before commit
      implicit val formats = DefaultFormats
      val evse = input.extract[Evse]


      val m: Mutator[String] = createMutator(ko, se)

      inmapp foreach  {
        case (key : String,  value : String) => {
          println(" evse : " + evseId.toString + "  inserting : " + key + " : " + value)
          m.insert(evseId.toString, deviceColFamName, HFactory.createStringColumn(key, value))
        }
      }

      HttpResponse(StatusCodes.Created)

    } catch {
      case ex: Exception => {
        HttpResponse(StatusCodes.BadRequest, "No Write: " + ex.getCause.toString + " : " + ex.getMessage)
      }
    }
  }

  def getEvseDevice(id: Long): HttpResponse = {
    try {
      val q: SliceQuery[String, String, String] = HFactory.createSliceQuery(ko, se, se ,se )
      q.setColumnFamily(deviceColFamName)
        .setKey(id.toString)
        .setRange("","",false,100)

      val colist = q.execute().get().getColumns

      var json = "{"
      val it = colist.iterator
      while(it.hasNext ) {
        val col = it.next()
        json += ( "\"" + col.getName + "\":" + "\"" + col.getValue + "\"" )
        if( it.hasNext ) json += ","
      }
      json += "}"

      val content = HttpContent( ContentType(`application/json`), json)
      HttpResponse(200, content)

    } catch {
      case ex: Exception => HttpResponse(404, ex.getMessage + " : " + ex.toString)
    }

  }

  def putEvseChp(content: String, evseId: Long): HttpResponse = {

    try {

      //put data
      val inmapp = EvseDomain.parseChpWithOptionalData(content)

      println("hello 1 ")
      val m: Mutator[String] = createMutator(ko, se)

      inmapp foreach  {
        case (key : String,  value : Any) => {
          value match {
            case v : String => {
              println(" evse Chp : " + evseId.toString + "  inserting string: " + key + " : " + v)
              m.insert(evseId.toString, chparamColFamName, HFactory.createStringColumn(key, v))
            }
            case v : java.lang.Long => {
              println(" evse Chp : " + evseId.toString + "  inserting long: " + key + " : " + v)
              m.insert(evseId.toString, chparamColFamName, HFactory.createColumn(key, v, se, sl))
            }
            case v : java.lang.Integer => {
              println(" evse Chp : " + evseId.toString + "  inserting int: " + key + " : " + v)
              m.insert(evseId.toString, chparamColFamName, HFactory.createColumn(key, v, se, si))
            }
            case v : java.lang.Double => {
              println(" evse Chp : " + evseId.toString + "  inserting double: " + key + " : " + v)
              m.insert(evseId.toString, chparamColFamName, HFactory.createColumn(key, v, se, sd))
            }
            case v : java.lang.Boolean => {
              println(" evse Chp : " + evseId.toString + "  inserting boolean: " + key + " : " + v)
              m.insert(evseId.toString, chparamColFamName, HFactory.createColumn(key, v, se, sb))
            }
          }
        }
      }

      HttpResponse(StatusCodes.Created)

    } catch {
      case ex: Exception => {
        HttpResponse(StatusCodes.BadRequest, "No Write: " + ex.getCause.toString + " : " + ex.getMessage)
      }
    }
  }

  def getEvseChp(id: Long): HttpResponse = {
    try {
      val q: SliceQuery[String, String, String] = HFactory.createSliceQuery(ko, se, se ,se )
      q.setColumnFamily(chparamColFamName)
        .setKey(id.toString)
        .setRange("a", "z", false, 50)

      //val result : QueryResult[ColumnSlice[String, String]] = q.execute();

      val colist = q.execute().get().getColumns

      var json = "{"
      val it = colist.iterator
      while(it.hasNext ) {
        val col = it.next()
        json += ( "\"" + col.getName + "\":" + "\"" + col.getValue + "\"" )
        if( it.hasNext ) json += ","
      }
      json += "}"

      //val contenttyoe = ContentType(`application/json`)

      val content = HttpContent( ContentType(`application/json`), json)
      HttpResponse(200, content)

    } catch {
      case ex: Exception => HttpResponse(404, ex.getMessage + " : " + ex.toString)
    }

  }

}
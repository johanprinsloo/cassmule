package com.example.mule

import akka.event.EventHandler
import me.prettyprint.cassandra.utils.StringUtils._
import net.liftweb.json._
import me.prettyprint.hector.api.factory.HFactory
import me.prettyprint.hector.api.factory.HFactory.createKeyspace
import me.prettyprint.cassandra.serializers.StringSerializer
import me.prettyprint.cassandra.serializers.LongSerializer
import me.prettyprint.hector.api.factory.HFactory.createMutator
import me.prettyprint.hector.api.mutation.MutationResult
import me.prettyprint.hector.api.mutation.Mutator
import me.prettyprint.hector.api.beans.ColumnSlice
import me.prettyprint.hector.api.query.{QueryResult, SliceQuery}
import cc.spray.http._
import cc.spray.http.MediaTypes._
import cc.spray._
import com.example.domain.Evse


object MulePen {

  val keyspaceName = "mulespace"
  val colFamName = "evses"

  val cluster = HFactory.getOrCreateCluster("Test Cluster", "localhost:9160");
  val ko = createKeyspace(keyspaceName, cluster)
  val se = new StringSerializer()
  val sl = new LongSerializer()


  def putEvse(content: String, evseId: Long): HttpResponse = {

    try {
      val input = parse(content)

      //validate before commit
      //val evse = input.extract[Evse]

      //put data
      val inmapp : Map[String, String] = input.values.asInstanceOf[Map[String,String]]

      val m: Mutator[String] = createMutator(ko, se)

      inmapp foreach  {
        case (key : String,  value : String) => {
          println(" evse : " + evseId.toString + "  inserting : " + key + " : " + value)
          m.insert(evseId.toString, colFamName, HFactory.createStringColumn(key, value))
        }
      }

      HttpResponse(StatusCodes.Accepted)

    } catch {
      case ex: Exception => {
        ex.printStackTrace()
        HttpResponse(StatusCodes.BadRequest, "No Write: " + ex.getMessage)
      }
    }
  }

  def getEvse(id: Long): HttpResponse = {
    try {
      val q: SliceQuery[String, String, String] = HFactory.createSliceQuery(ko, se, se ,se )
      q.setColumnFamily(colFamName)
      .setKey(id.toString)
      .setRange("a", "z", false, 20);

      //val result : QueryResult[ColumnSlice[String, String]] = q.execute();

      val colist = q.execute().get().getColumns

      var json = "{"
      val it = colist.iterator
      while(it.hasNext() ) {
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
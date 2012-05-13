package com.example.mule

import scala.collection.JavaConversions._
import org.apache.cassandra.service.EmbeddedCassandraService
import org.apache.thrift.transport.TSocket
import org.apache.cassandra.thrift.TBinaryProtocol
import org.apache.cassandra.thrift.Cassandra
import org.apache.cassandra.thrift.CassandraDaemon
import me.prettyprint.cassandra.service.CassandraHostConfigurator
import me.prettyprint.cassandra.service.ThriftCluster
import me.prettyprint.hector.api.factory.HFactory
import me.prettyprint.hector.api.ddl.ColumnFamilyDefinition
import me.prettyprint.hector.api.ddl.ComparatorType
import me.prettyprint.cassandra.service.ThriftKsDef
import java.util.Arrays
import me.prettyprint.cassandra.connection.SpeedForJOpTimer
import me.prettyprint.hector.api.exceptions.HectorException
import me.prettyprint.hector.api.Cluster


object MuleParameters {
  val clusterName = "Mule-Cluster"
  var clusterHost = "localhost"
  var clusterPort = 9160
  val clusterReties = 5

  val keyspaceMuleName = "mulespace"
  val colFamName = "evses"
  val deviceColFamName = "evse_dev"
  val chparamColFamName = "evse_chp"
  val chsColFamName = "evse_chs"
  val replicationFactor = 1

  val keyspaceTimeName = "mulemetricsspace"
  val chsTimeSeriesColFamName = "chs_metrics"
}

object MuleInception {

  import MuleParameters._

  //val cassandra = new EmbeddedCassandraService()
  //cassandra.start()


  def inseminate = {

    val currdir = System.getProperty("user.dir")
    System.setProperty("cassandra.config", "file://" + currdir + "/config/cassandra.yaml")

    try {
      val client = new CassandraClient(clusterHost, clusterPort)
      client.open
    } catch {
      case ex: Exception => {
        println(">>> Starting the embedded Cassandra Deamon")
        try {
          val cassandra = new Runnable {
            val cassandraDaemon = new CassandraDaemon
            cassandraDaemon.init(null)

            def run = cassandraDaemon.start

            val cassandraHostConfig = new CassandraHostConfigurator
            cassandraHostConfig.setOpTimer(new SpeedForJOpTimer(clusterName))
          }
          val t = new Thread(cassandra)
          t.setDaemon(true)
          t.start

        } catch {
          case ex: Exception => ex.printStackTrace()
        }
      }
    }

  }

  def birth {

    getCluster() match {
      case None => println("Could not create a cluster ...")
      case Some(cluster) => {

        try {

          val cfDef: ColumnFamilyDefinition =
            HFactory.createColumnFamilyDefinition(keyspaceMuleName, colFamName, ComparatorType.UTF8TYPE)
          val cfDevDef: ColumnFamilyDefinition =
            HFactory.createColumnFamilyDefinition(keyspaceMuleName, deviceColFamName, ComparatorType.UTF8TYPE)
          val cfChpDef: ColumnFamilyDefinition =
            HFactory.createColumnFamilyDefinition(keyspaceMuleName, chparamColFamName, ComparatorType.UTF8TYPE)
          val cfChsDef: ColumnFamilyDefinition =
            HFactory.createColumnFamilyDefinition(keyspaceMuleName, chsColFamName, ComparatorType.UTF8TYPE)

          val muleKeyspaceDef = HFactory.createKeyspaceDefinition(keyspaceMuleName,
            ThriftKsDef.DEF_STRATEGY_CLASS,
            replicationFactor,
            Arrays.asList(cfDef, cfDevDef, cfChpDef, cfChsDef))

          val testcsf = asScalaBuffer(muleKeyspaceDef.getCfDefs())
          //println(">>> mule keyscape " + testcsf.toList.toString())


          val cfMetricsDef: ColumnFamilyDefinition =
            HFactory.createColumnFamilyDefinition(keyspaceTimeName, chsTimeSeriesColFamName, ComparatorType.TIMEUUIDTYPE)

          val timeKeyspaceDef = HFactory.createKeyspaceDefinition(keyspaceTimeName,
            ThriftKsDef.DEF_STRATEGY_CLASS,
            replicationFactor,
            Arrays.asList(cfMetricsDef))

          val timecfs = asScalaBuffer(timeKeyspaceDef.getCfDefs())
          //println(">>> metrics keyscape " + timecfs.toList.toString())

          //if ((cluster.describeKeyspace(keyspaceMuleName)) != null) cluster.dropKeyspace(keyspaceMuleName, true)
          println(">> mule ks : " + cluster.describeKeyspace(keyspaceMuleName))
          if( cluster.describeKeyspace(keyspaceMuleName) == null )  cluster.addKeyspace(muleKeyspaceDef, true)

          //if ((cluster.describeKeyspace(keyspaceTimeName)) != null) cluster.dropKeyspace(keyspaceTimeName, true)
          println(">> time ks : " + cluster.describeKeyspace(keyspaceTimeName))
          if( cluster.describeKeyspace(keyspaceTimeName) == null ) cluster.addKeyspace(timeKeyspaceDef, true)

          cluster.describeKeyspaces() foreach {
            keysp =>
              println(keysp.getName())
              keysp.getCfDefs() foreach {
                colfam =>
                  println("\t" + colfam.getName() + " : " + colfam.getColumnType()
                    + " : sorted as : " + colfam.getComparatorType().getTypeName())
              }
          }
          true
        } catch {
          case ex: HectorException => ex.printStackTrace();
          case ex: Exception => ex.printStackTrace()
        }

      }
    }


  }

  def getCluster(retries: Int = 0): Option[Cluster] = {

    if (retries < clusterReties) {
      try {
        inseminate
        Some( HFactory.getOrCreateCluster(clusterName, clusterHost + ":" + clusterPort) )
      } catch {
        case ex: HectorException => {
          println("Cluster creation failed on retry " + retries)
          getCluster(retries + 1)
        }
      }
    }
    else None
  }
}

class CassandraClient(host: String, port: Int) {
  val socket = new TSocket(host, port)
  val protocl = new TBinaryProtocol(socket)
  val client = new Cassandra.Client(protocl)
  var isConnected = false

  def open {
    socket.open()
    isConnected = true
  }

  def close {
    socket.close()
    isConnected = false
  }

}
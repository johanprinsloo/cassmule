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

object MuleInception {

  val cassandra = new EmbeddedCassandraService()
  val keyspaceName = "mulespace"
  val colFamName = "evses"
  val replicationFactor = 1

  def inseminate = {
    val currdir = System.getProperty("user.dir")
    System.setProperty("cassandra.config", "file://" + currdir + "/config/cassandra.yaml")

    try {
      val client = new CassandraClient("localhost", 9160)
      client.open
    } catch {
      case ex: Exception => {
        try {
          val cassandra = new Runnable {
            val cassandraDaemon = new CassandraDaemon
            cassandraDaemon.init(null)

            def run = cassandraDaemon.start
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

  def birth = {

    try {

      val cluster = HFactory.getOrCreateCluster("Test Cluster", "localhost:9160")

      val cfDef: ColumnFamilyDefinition =
        HFactory.createColumnFamilyDefinition(keyspaceName, colFamName, ComparatorType.UTF8TYPE)

      val newKeyspaceDef = HFactory.createKeyspaceDefinition(keyspaceName,
        ThriftKsDef.DEF_STRATEGY_CLASS,
        replicationFactor,
        Arrays.asList(cfDef));

      val testcsf = asScalaBuffer(newKeyspaceDef.getCfDefs())
      println(">>> keyscape " + testcsf.toList.toString())

      if ((cluster.describeKeyspace(keyspaceName)) == null) {
        cluster.addKeyspace(newKeyspaceDef, true)
      }

      cluster.describeKeyspaces() foreach {
        keysp =>
          println(keysp.getName())
          keysp.getCfDefs() foreach {
            colfam =>
              println("\t" + colfam.getName() + " : " + colfam.getColumnType()
                + " : sorted as : " + colfam.getComparatorType().getTypeName())
          }
      }
    } catch {
      case ex: Exception => ex.printStackTrace()
    }

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
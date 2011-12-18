package com.example.mule
import org.apache.cassandra.service.EmbeddedCassandraService
import org.apache.thrift.transport.TSocket
import org.apache.cassandra.thrift.TBinaryProtocol
import org.apache.cassandra.thrift.Cassandra
import org.apache.cassandra.thrift.CassandraDaemon

object MuleInception {

  val cassandra = new EmbeddedCassandraService()

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
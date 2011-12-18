package com.example.mule
import akka.event.EventHandler
import net.liftweb.json._


object MulePen {
  
  def putEvse(content: String, evseId: Long) = {
    val input = parse(content)
  }
  
  def getEvse(id: Long): String = {
    EventHandler.info(this, "get evse id : " + id)
    "hi!!"
  }

}
package com.example.domain

import net.liftweb.json.JsonParser.{IntVal, FieldStart, Parser, End}
import net.liftweb.json._


case class Evse( name: String, netAddress: String,  manufacturer: String,  model: String, serial: String)

case class Chp( maxAmps: Double,
                maxVolts: Double,
                revision: Long = 0L,
                delayStart: Long = 0L,
                delayReady: Long = 0L,
                loggingLevel: Int = 0,
                suspended: Boolean = false,
                suspensionReason: String = "" )


object EvseDomain {
  val chpParser = (p: Parser) => {
    def parse: BigInt = p.nextToken match {
      case FieldStart("postalCode") => p.nextToken match {
        case IntVal(code) => code
        case _ => p.fail("expected int")
      }
      case End => p.fail("no field named 'postalCode'")
      case _ => parse
    }

    parse
  }

  def getCCParams(cc: AnyRef) =
    (Map[String, Any]() /: cc.getClass.getDeclaredFields) {(a, f) =>
      f.setAccessible(true)
      a + (f.getName -> f.get(cc))
    }

  def parseChpWithOptionalData(json: String): scala.collection.mutable.Map[String, Any] = {

    val ret = scala.collection.mutable.Map[String, Any]().empty

    try {
      val input = parse(json)
      implicit val formats = DefaultFormats
      println(">>>>>>> input : " +  json)

      //compulsory data
      val maxVolts = (input \ "maxVolts").extractOpt[Double]
      //input.replace("maxAmps" :: "1" :: "tenants" :: "2" :: Nil, JNothing)
      val maxAmps = (input \ "maxAmps").extractOpt[Double]

      println(">>>>>>> got : " +  !maxAmps.isEmpty + !maxVolts.isEmpty)

      if (!maxAmps.isEmpty && !maxVolts.isEmpty) {
        input.replace("maxVolts" :: Nil, JNothing)
        input.replace("maxAmps" :: Nil, JNothing)
        ret("maxVolts") = maxVolts.get
        ret("maxAmps") = maxAmps.get

        //optional data
        ret("delayStart") = (input \ "delayStart").extractOpt[Long] match {
          case Some(x) => input.replace("delayStart" :: Nil, JNothing); x
          case _ => 0L
        }

        ret("delayReady") = (input \ "delayReady").extractOpt[Long] match {
          case Some(x) => input.replace("delayReady" :: Nil, JNothing); x
          case _ => 0L
        }

        ret("loggingLevel") = (input \ "loggingLevel").extractOpt[Int] match {
          case Some(x) => input.replace("loggingLevel" :: Nil, JNothing); x
          case _ => 0
        }

        ret("suspended") = (input \ "suspended").extractOpt[Boolean] match {
          case Some(x) => input.replace("suspended" :: Nil, JNothing); x
          case _ => false
        }

        ret("suspensionReason") = (input \ "suspensionReason").extractOpt[String] match {
          case Some(x) => input.replace("suspensionReason" :: Nil, JNothing); x
          case _ => ""
        }

        //revision
        ret("revision") = System.currentTimeMillis()
        
        println(">>>>>>extracted: " + ret)
      } else {
        throw new Exception("minimum data requirements not met for Evse Chp")
      }

    } catch {
      case ex: Exception => {
        println("parseChpWithOptionalData " + ex.getCause.toString + " : " + ex.getMessage)
        throw ex
      }
    }

    return ret
  }
}


package com.example.mule

import cc.spray.utils.Logging

object MuleCage extends Logging  {

val keyspaceName = "mulespace"
val colFamName = "evses"
val deviceColFamName = "evse_dev"
val chparamColFamName = "evse_chp"


  val hosts  = Host("localhost", 9160, 250) :: Nil
  val params = new PoolParams(10, ExhaustionPolicy.Fail, 500L, 6, 2)
  val pool   = new SessionPool(hosts, params, Consistency.One)
//
//  pool.borrow { session =>
//    log.debug("Count Value: " + session.count("Test" \ "Standard" \ "1"))
//  }
//
//def putEvseFree(content: String, evseId: Long): HttpResponse = {
//
//try {
//val input = parse(content)
//
//val inmapp : Map[String, String] = input.values.asInstanceOf[Map[String,String]]
//
//val m: Mutator[String] = createMutator(ko, se)
//
//inmapp foreach  {
//case (key : String,  value : String) => {
//log.info(" evse : " + evseId.toString + "  inserting : " + key + " : " + value)
//m.insert(evseId.toString, colFamName, HFactory.createStringColumn(key, value))
//}
//}
//
//HttpResponse(StatusCodes.Created)
//
//} catch {
//case ex: Exception => {
//HttpResponse(StatusCodes.BadRequest, "No Write: " + ex.getCause.toString + " : " + ex.getMessage)
//}
//}
}
package com.example

import akka.config.Supervision._
import akka.actor.Supervisor
import akka.actor.Actor._
import cc.spray._
import com.example.mule.MuleInception

class Boot{
  
  val evseModule = new EvseService {
    // bake your module cake here
  }

  val chsModule = new ChsService {  }
  
  MuleInception inseminate
  
  MuleInception birth

  val evseService = actorOf( new HttpService(evseModule.evseService))
  val chsService = actorOf( new HttpService(chsModule.chsService))
  val rootService = actorOf( new RootService(evseService, chsService))

  Supervisor(
    SupervisorConfig(
      OneForOneStrategy(List(classOf[Exception]), 3, 100),
      List(
        Supervise(chsService, Permanent),
        Supervise(evseService, Permanent),
        Supervise(rootService, Permanent)
      )
    )
  )

}
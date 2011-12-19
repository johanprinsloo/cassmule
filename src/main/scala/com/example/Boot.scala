package com.example

import akka.config.Supervision._
import akka.actor.Supervisor
import akka.actor.Actor._
import cc.spray._
import com.example.mule.MuleInception

class Boot {
  
  val mainModule = new TestService {
    // bake your module cake here
  }
  
  MuleInception inseminate
  
  MuleInception birth

  val httpService = actorOf(new HttpService(mainModule.testService))
  val rootService = actorOf(new RootService(httpService))

  Supervisor(
    SupervisorConfig(
      OneForOneStrategy(List(classOf[Exception]), 3, 100),
      List(
        Supervise(httpService, Permanent),
        Supervise(rootService, Permanent)
      )
    )
  )
}
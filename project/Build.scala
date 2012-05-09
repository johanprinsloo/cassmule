import sbt._
import Keys._
import com.github.siasia.WebPlugin

object Build extends sbt.Build {
  import Dependencies._

  lazy val myProject = Project("cassmule", file("."))
    .settings(WebPlugin.webSettings: _*)
    .settings(
      organization  := "com.example",
      version       := "1.0",
      scalaVersion  := "2.9.1",
      scalacOptions := Seq("-deprecation", "-encoding", "utf8"),
      resolvers     ++= Dependencies.resolutionRepos,
      libraryDependencies ++= Seq(
        Compile.akkaActor,
        Compile.sprayServer,
        Compile.lift_json,
        Compile.spray_json,
        Compile.cassandra,
        Compile.hector,
        Compile.cascal,
        Test.specs2,
        Test.scalatest,
        Container.jettyWebApp,
        Container.akkaSlf4j,
        Container.slf4j,
        Container.logback,
        Container.grizzled
      )
    )
}

object Dependencies {
  val resolutionRepos = Seq(
    ScalaToolsSnapshots,
    "Local Maven" at "file://"+Path.userHome+"/.m2/repository/snapshots",   //cascal is built locally
    "Typesafe repo" at "http://repo.typesafe.com/typesafe/releases/",
    "spray repo" at "http://repo.spray.cc/"
  )

  object V {
    val akka    = "1.3.1"
    val spray   = "0.9.0"
    val liftjson = "2.4"
    val sprayjson = "1.1.1"
    val specs2  = "1.7.1"
    val scalatest  = "1.7.2"
    val jetty   = "8.1.0.v20120127"
    val slf4j   = "1.6.4"
    val logback = "1.0.0"
    val cassandra = "1.0.9"
    val hector = "1.0-5"
    val cascal = "1.3-SNAPSHOT"
    val grizzled = "0.6.6"
  }

  object Compile {
    val akkaActor   = "se.scalablesolutions.akka" %  "akka-actor"      % V.akka      % "compile"
    val sprayServer = "cc.spray"                  %  "spray-server"    % V.spray     % "compile"
    val cassandra = "org.apache.cassandra"        %  "cassandra-all"   % V.cassandra % "compile"
    val hector =    "me.prettyprint"				  %  "hector-core"	    % V.hector       % "compile"
    val cascal =    "com.shorrockin"          %% "cascal"           % V.cascal       % "compile"
    val lift_json = "net.liftweb" 			      %%  "lift-json"       % V.liftjson     % "compile"
    val spray_json = "cc.spray"               %%  "spray-json"      % V.sprayjson    % "compile"
  }

  object Test {
    val specs2      = "org.specs2"     %% "specs2"        % V.specs2  % "test"
    val scalatest   = "org.scalatest"  %% "scalatest"     % V.scalatest  % "test"
  }

  object Container {
    val jettyWebApp = "org.eclipse.jetty"         %  "jetty-webapp"    % V.jetty   % "container"
    val akkaSlf4j   = "se.scalablesolutions.akka" %  "akka-slf4j"      % V.akka
    val slf4j       = "org.slf4j"                 %  "slf4j-api"       % V.slf4j
    val logback     = "ch.qos.logback"            %  "logback-classic" % V.logback
    val grizzled    = "org.clapper"               %%  "grizzled-slf4j"  % V.grizzled
  }
}
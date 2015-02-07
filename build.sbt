name := "session-service"

version := "1.0"

scalaVersion := "2.11.1"

scalacOptions := Seq("-unchecked", "-deprecation", "-encoding", "utf8")

resolvers += "krasserm at bintray" at "http://dl.bintray.com/krasserm/maven"

libraryDependencies ++= {
  val akkaVersion         = "2.3.6"
  val sprayVersion        = "1.3.2"
  val persistenceVersion  = "2.3.4"
  val specs2Version       = "2.3.12"
  Seq(
    "io.spray"          %% "spray-can"      % sprayVersion,
    "io.spray"          %% "spray-routing"  % sprayVersion,
    "io.spray"          %%  "spray-json"    % "1.3.1",
    "io.spray"          %% "spray-testkit"  % sprayVersion  % "test",
    "com.typesafe.akka" %% "akka-actor"     % akkaVersion,
    "com.typesafe.akka" %% "akka-persistence-experimental" % persistenceVersion,
    "com.github.krasserm" %% "akka-persistence-testkit" % "0.3.4" % "test",
    "com.typesafe.akka" %% "akka-testkit"   % akkaVersion   % "test"
    //"org.specs2"        %%  "specs2-core"   % specs2Version % "test",
    //"org.specs2" %% "specs2-junit" % specs2Version % "test",
    //"org.specs2" %% "specs2-mock" % specs2Version % "test"
  )
}

Revolver.settings


name := "HealthChecker"
version := "0.1"
scalaVersion in ThisBuild := "2.12.8"
organization in ThisBuild := "org.backsapc"

// https://docs.scala-lang.org/overviews/compiler-options/index.html
val scalacCompileOpts = Seq(
  "-feature",
  "-unchecked",
  // "-deprecation:false", // uncomment if you *must* use deprecated apis
  "-Xfatal-warnings",
  "-Ywarn-value-discard",
  "-Xlint:unsound-match",
  "-deprecation"
)

lazy val akkaHttpVersion = "10.1.8"
lazy val akkaVersion = "2.5.21"

resolvers ++= Seq("snapshots", "releases").map(Resolver.sonatypeRepo)

libraryDependencies ++= Seq(
  "com.typesafe.slick" %% "slick" % "3.3.0",
  "org.slf4j" % "slf4j-nop" % "1.6.4",
  "com.typesafe.slick" %% "slick-hikaricp" % "3.3.0",
  "com.h2database" % "h2" % "1.4.199",
  "com.jason-goodwin" %% "authentikat-jwt" % "0.4.5",
  "com.github.t3hnar" %% "scala-bcrypt" % "4.0",
  "com.typesafe.akka" %% "akka-http" % akkaHttpVersion,
  "com.typesafe.akka" %% "akka-http-spray-json" % akkaHttpVersion,
  "com.typesafe.akka" %% "akka-http-xml" % akkaHttpVersion,
  "com.typesafe.akka" %% "akka-stream" % akkaVersion,
  "com.typesafe.akka" %% "akka-testkit" % akkaVersion % Test,
  "com.typesafe.akka" %% "akka-stream-testkit" % akkaVersion % Test,
  "com.typesafe.akka" %% "akka-http-testkit" % akkaHttpVersion % Test,
  "org.scalatest" %% "scalatest" % "3.0.3" % Test,
  "org.scalamock" %% "scalamock" % "4.1.0" % Test
)

scalacOptions in ThisBuild ++= scalacCompileOpts

enablePlugins(JavaAppPackaging)

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
  "-Xlint:unsound-match"
)

lazy val checker = (project in file("services/checker"))
  .settings(
    name := "checker",
    libraryDependencies ++= Seq(
      "com.typesafe.akka" %% "akka-stream" % "2.5.21",
      "com.typesafe.akka" %% "akka-stream-testkit" % "2.5.21" % Test,
      "com.typesafe.akka" %% "akka-http" % "10.1.7",
      "com.typesafe.akka" %% "akka-http-testkit" % "10.1.7" % Test,
      "org.scalatest"     %% "scalatest" % "3.0.3" % Test
    ),
    scalacOptions in(Compile, compile) ++= scalacCompileOpts
  )

lazy val notification = (project in file("services/notification"))
  .settings(
    name := "notification",
    libraryDependencies ++= Seq(
      "com.typesafe.akka" %% "akka-stream" % "2.5.21",
      "com.typesafe.akka" %% "akka-stream-testkit" % "2.5.21" % Test,
      "com.typesafe.akka" %% "akka-http" % "10.1.7",
      "com.typesafe.akka" %% "akka-http-testkit" % "10.1.7" % Test,
      "org.scalatest"     %% "scalatest" % "3.0.3" % Test
    ),
    scalacOptions in(Compile, compile) ++= scalacCompileOpts
  )

lazy val user = (project in file("services/user"))
  .settings(
    name := "user",
    libraryDependencies ++= Seq(
      "com.typesafe.akka" %% "akka-stream" % "2.5.21",
      "com.typesafe.akka" %% "akka-stream-testkit" % "2.5.21" % Test,
      "com.typesafe.akka" %% "akka-http" % "10.1.7",
      "com.typesafe.akka" %% "akka-http-testkit" % "10.1.7" % Test,
      "org.scalatest"     %% "scalatest" % "3.0.3" % Test
    ),
    scalacOptions in(Compile, compile) ++= scalacCompileOpts
  )

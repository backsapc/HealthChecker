organization in ThisBuild := "org.backsapce"
version in ThisBuild := "1.0-SNAPSHOT"

// the Scala version that will be used for cross-compiled libraries
scalaVersion in ThisBuild := "2.12.4"

val macwire = "com.softwaremill.macwire" %% "macros" % "2.3.0" % "provided"
val scalaTest = "org.scalatest" %% "scalatest" % "3.0.4" % Test

lazy val `healthchecker` = (project in file("."))
  .aggregate(`healthchecker-api`, `healthchecker-impl`, `healthchecker-stream-api`, `healthchecker-stream-impl`)

lazy val `healthchecker-api` = (project in file("healthchecker-api"))
  .settings(
    libraryDependencies ++= Seq(
      lagomScaladslApi
    )
  )

lazy val `healthchecker-impl` = (project in file("healthchecker-impl"))
  .enablePlugins(LagomScala)
  .settings(
    libraryDependencies ++= Seq(
      lagomScaladslPersistenceCassandra,
      lagomScaladslKafkaBroker,
      lagomScaladslTestKit,
      macwire,
      scalaTest
    )
  )
  .settings(lagomForkedTestSettings: _*)
  .dependsOn(`healthchecker-api`)

lazy val `healthchecker-stream-api` = (project in file("healthchecker-stream-api"))
  .settings(
    libraryDependencies ++= Seq(
      lagomScaladslApi
    )
  )

lazy val `healthchecker-stream-impl` = (project in file("healthchecker-stream-impl"))
  .enablePlugins(LagomScala)
  .settings(
    libraryDependencies ++= Seq(
      lagomScaladslTestKit,
      macwire,
      scalaTest
    )
  )
  .dependsOn(`healthchecker-stream-api`, `healthchecker-api`)

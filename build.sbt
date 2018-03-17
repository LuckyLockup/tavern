// *****************************************************************************
// Projects
// *****************************************************************************
lazy val domain = project.settings(settings)
  .settings(
    libraryDependencies ++= Seq(
      "org.scalacheck" %% "scalacheck" % versions.scalaCheck,
      "com.lihaoyi" %% "utest" % versions.utest,
      "org.typelevel" %% "cats-core" % versions.CatsVersion,
      "io.circe" %% "circe-generic" % versions.CirceVersion,
      "io.circe" %% "circe-literal" % versions.CirceVersion,
      "io.circe" %% "circe-generic-extras" % versions.CirceVersion,
      "io.circe" %% "circe-optics" % versions.CirceVersion,
      "io.circe" %% "circe-parser" % versions.CirceVersion,
      "io.circe" %% "circe-java8" % versions.CirceVersion,
      "org.tpolecat" %% "doobie-core" % versions.DoobieVersion,
      "org.tpolecat" %% "doobie-h2" % versions.DoobieVersion,
      "org.tpolecat" %% "doobie-scalatest" % versions.DoobieVersion,
      "org.tpolecat" %% "doobie-hikari" % versions.DoobieVersion,
      "com.h2database" % "h2" % versions.H2Version,
      "org.http4s" %% "http4s-blaze-server" % versions.Http4sVersion,
      "org.http4s" %% "http4s-circe" % versions.Http4sVersion,
      "org.http4s" %% "http4s-dsl" % versions.Http4sVersion,
      "ch.qos.logback" % "logback-classic" % versions.LogbackVersion,
      "org.flywaydb" % "flyway-core" % versions.FlywayVersion,
      "com.github.pureconfig" %% "pureconfig" % versions.PureConfigVersion,
      "org.scalacheck" %% "scalacheck" % versions.ScalaCheckVersion % Test,
      "org.scalatest" %% "scalatest" % versions.ScalaTestVersion % Test
    )
  )

lazy val riichi = project.dependsOn(domain)

lazy val it = project

// *****************************************************************************
// Library dependencies
// *****************************************************************************

lazy val versions = new {
  val scalaCheck = "1.13.5"
  val utest = "0.6.3"
  val CatsVersion = "1.1.0"
  val CirceVersion = "0.9.2"
  val DoobieVersion = "0.5.1"
  val H2Version = "1.4.196"
  val Http4sVersion = "0.18.2"
  val LogbackVersion = "1.2.3"
  val ScalaCheckVersion = "1.13.5"
  val ScalaTestVersion = "3.0.4"
  val FlywayVersion = "4.2.0"
  val PureConfigVersion = "0.9.0"
}

// *****************************************************************************
// Settings
// *****************************************************************************

lazy val settings = commonSettings

lazy val commonSettings =
  Seq(
    scalaVersion := "2.12.4",
    organization := "luckylockup.com",
    organizationName := "Lucky Lockup",
    startYear := Some(2018),
    licenses += ("Do What The Fuck You Want To Public License", url("http://www.wtfpl.net/")),
    scalacOptions ++= Seq(
      "-unchecked",
      "-deprecation",
      "-language:_",
      "-target:jvm-1.8",
      "-encoding", "UTF-8",
      "-Ypartial-unification",
      "-Ywarn-unused-import"
    ),
    Compile / unmanagedSourceDirectories := Seq((Compile / scalaSource).value),
    Test / unmanagedSourceDirectories := Seq((Test / scalaSource).value),
    testFrameworks += new TestFramework("utest.runner.Framework")
  )
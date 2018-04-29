
// *****************************************************************************
// Projects
// *****************************************************************************
lazy val domain = project
  .settings(commonSettings)
  .settings(
    libraryDependencies ++= Seq(
      "org.typelevel" %% "cats-core" % versions.CatsVersion,

      "io.circe" %% "circe-generic" % versions.CirceVersion,
      "io.circe" %% "circe-literal" % versions.CirceVersion,
      "io.circe" %% "circe-generic-extras" % versions.CirceVersion,
      "io.circe" %% "circe-optics" % versions.CirceVersion,
      "io.circe" %% "circe-parser" % versions.CirceVersion,
      "io.circe" %% "circe-java8" % versions.CirceVersion,

      "com.github.pureconfig" %% "pureconfig" % versions.PureConfigVersion,

      "org.scalacheck" %% "scalacheck" % versions.ScalaCheckVersion % Test,
      "org.scalatest" %% "scalatest" % versions.ScalaTestVersion % Test,
      "org.gnieh" %% "diffson-circe" % versions.DiffVersion % Test
    )
  )

lazy val ai = project
  .settings(commonSettings)
  .dependsOn(domain)
  .settings(libraryDependencies ++= Seq(
    "ch.qos.logback" % "logback-classic" % versions.LogbackVersion,
  ))

lazy val riichi = project
  .settings(commonSettings)
  .aggregate(domain, ai)
  .dependsOn(domain, ai)
  .settings(libraryDependencies ++= Seq(
    "com.typesafe.akka" %% "akka-actor" % versions.AkkaVersion,
    "com.typesafe.akka" %% "akka-slf4j" % versions.AkkaVersion,
    "com.typesafe.akka" %% "akka-testkit" % versions.AkkaVersion % Test,

    "com.typesafe.akka" %% "akka-stream" % versions.AkkaVersion,
    "com.typesafe.akka" %% "akka-stream-testkit" % versions.AkkaVersion,

    "com.typesafe.akka" %% "akka-http" % versions.AkkaHttp,
    "com.typesafe.akka" %% "akka-http-testkit" % versions.AkkaHttp,
    "ch.megard" %% "akka-http-cors" % versions.AkkaCors,

    "com.typesafe.akka" %% "akka-remote" % versions.AkkaVersion,
    "com.typesafe.akka" %% "akka-cluster" % versions.AkkaVersion,
    "com.typesafe.akka" %% "akka-cluster-tools" % versions.AkkaVersion,
    "com.typesafe.akka" %% "akka-cluster-sharding" % versions.AkkaVersion,
    "com.typesafe.akka" %% "akka-persistence" % versions.AkkaVersion,
    "com.typesafe.akka" %% "akka-persistence-cassandra" % versions.CassandraPluginVersion,
    "com.typesafe.akka" %% "akka-distributed-data" % versions.AkkaVersion,
    // this allows us to start cassandra from the sample
    "com.typesafe.akka" %% "akka-persistence-cassandra" % versions.CassandraPluginVersion,
    "com.typesafe.akka" %% "akka-persistence-cassandra-launcher" % versions.CassandraPluginVersion % Test,

    "com.typesafe.akka" %% "akka-http" % versions.AkkaHttp,
    "org.tpolecat" %% "doobie-core" % versions.DoobieVersion,
    "org.tpolecat" %% "doobie-h2" % versions.DoobieVersion,
    "org.tpolecat" %% "doobie-scalatest" % versions.DoobieVersion,
    "org.tpolecat" %% "doobie-hikari" % versions.DoobieVersion,
    "com.h2database" % "h2" % versions.H2Version,
    "ch.qos.logback" % "logback-classic" % versions.LogbackVersion,
    "org.flywaydb" % "flyway-core" % versions.FlywayVersion,
    "org.fusesource.leveldbjni" % "leveldbjni-all" % "1.8",

    "de.heikoseeberger" %% "akka-http-circe" % "1.20.1"
  ),
    mainClass in assembly := Some("com.ll.Main")
  )

lazy val it = project
  .settings(commonSettings)
  .dependsOn(domain)
  .settings(
    libraryDependencies ++= Seq(
      "com.typesafe.akka" %% "akka-stream" % versions.AkkaVersion,
      "com.typesafe.akka" %% "akka-stream-testkit" % versions.AkkaVersion,

      "com.typesafe.akka" %% "akka-http" % versions.AkkaHttp,
      "de.heikoseeberger" %% "akka-http-circe" % "1.20.1",
      "ch.qos.logback" % "logback-classic" % "1.2.3",
      "org.scalacheck" %% "scalacheck" % versions.ScalaCheckVersion % Test,
      "org.scalatest" %% "scalatest" % versions.ScalaTestVersion % Test
    )
  )

// *****************************************************************************
// Library dependencies
// *****************************************************************************

lazy val versions = new {
  val AkkaVersion = "2.5.11"
  val AkkaTypedVersion = "2.5.7"
  val AkkaHttp = "10.1.0"
  val AkkaCors = "0.3.0"

  val CassandraPluginVersion = "0.83"

  val scalaCheck = "1.13.5"
  val utest = "0.6.3"

  val CatsVersion = "1.1.0"
  val CirceVersion = "0.9.2"
  val DiffVersion = "2.2.6"
  val DoobieVersion = "0.5.1"
  val H2Version = "1.4.196"
  val Http4sVersion = "0.18.2"
  val LogbackVersion = "1.2.3"
  val ScalaCheckVersion = "1.13.5"
  val ScalaTestVersion = "3.0.4"
  val FlywayVersion = "4.2.0"
  val PureConfigVersion = "0.9.0"
  val monixVersion = "3.0.0-RC1"
}

// *****************************************************************************
// Settings
// *****************************************************************************

lazy val commonSettings =
  Seq(
    version := "0.1-SNAPSHOT",
    scalaVersion := "2.12.4",
    organization := "luckylockup.com",
    organizationName := "Lucky Lockup",
    startYear := Some(2018),
    mainClass in Compile := Some("com.ll.Main"),
    cancelable in Global := true,
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
    testFrameworks += new TestFramework("utest.runner.Framework"),
    test in assembly := {},
    assemblyMergeStrategy in assembly := {
      case PathList("META-INF", xs@_*)    => MergeStrategy.discard
      case "io.netty.versions.properties" => MergeStrategy.first
      case x                              =>
        val oldStrategy = (assemblyMergeStrategy in assembly).value
        oldStrategy(x)
    },
    resolvers += Resolver.bintrayRepo("hseeberger", "maven")
  )

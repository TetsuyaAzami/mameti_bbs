lazy val root = (project in file("."))
  .enablePlugins(PlayScala)
  .settings(
    name := """mameti_bbs""",
    organization := "com.example",
    version := "1.0-SNAPSHOT",
    scalaVersion := "2.13.8",
    libraryDependencies ++= Seq(
      guice,
      jdbc,
      evolutions,
      caffeine,
      "org.postgresql" % "postgresql" % "42.3.3",
      "org.playframework.anorm" %% "anorm" % "2.6.5",
      "org.webjars" % "bootstrap" % "5.0.0",
      "org.webjars" % "font-awesome" % "5.15.4",
      "org.webjars.npm" % "axios" % "0.26.0",
      "org.scalatestplus.play" %% "scalatestplus-play" % "5.0.0" % Test
    ),
    scalacOptions ++= List(
      "-encoding",
      "utf8",
      "-deprecation",
      "-feature",
      "-unchecked",
      "-Xfatal-warnings"
    ),
    javacOptions ++= List("-Xlint:unchecked", "-Xlint:deprecation", "-Werror")
  )

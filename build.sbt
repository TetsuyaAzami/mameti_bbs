import org.eclipse.jgit.api.MergeCommand.FastForwardMode.Merge
lazy val root = (project in file("."))
  .enablePlugins(PlayScala)
  .settings(
    name := """mameti_bbs""",
    organization := "com.example",
    version := "1.0-SNAPSHOT",
    scalaVersion := "2.13.8",
    sources in (Compile, doc) := Seq.empty,
    publishArtifact in (Compile, packageDoc) := false,
    libraryDependencies ++= Seq(
      guice,
      jdbc,
      evolutions,
      play.sbt.PlayImport.cacheApi,
      "com.github.karelcemus" %% "play-redis" % "2.7.0",
      "io.altoo" %% "akka-kryo-serialization" % "2.4.3",
      "mysql" % "mysql-connector-java" % "8.0.28",
      "org.playframework.anorm" %% "anorm" % "2.6.5",
      "org.webjars" % "bootstrap" % "5.0.0",
      "org.webjars" % "font-awesome" % "5.15.4",
      "org.webjars.npm" % "axios" % "0.26.0",
      "com.fasterxml.jackson.module" %% "jackson-module-scala" % "2.12.1",
      "software.amazon.awssdk" % "bom" % "2.17.203",
      "software.amazon.awssdk" % "s3" % "2.17.203",
      "org.scalatestplus.play" %% "scalatestplus-play" % "5.1.0" % Test,
      "org.scalatestplus" %% "mockito-3-4" % "3.2.10.0" % Test
    ),
    scalacOptions ++= List(
      "-encoding",
      "utf8",
      "-deprecation",
      "-feature",
      "-unchecked",
      "-Xfatal-warnings"
    ),
    javaOptions in Test += "-Dconfig.file=/conf/test.conf",
    javacOptions ++= List("-Xlint:unchecked", "-Xlint:deprecation", "-Werror")
  )

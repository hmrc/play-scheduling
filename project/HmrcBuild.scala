import play.core.PlayVersion
import sbt.Keys._
import sbt._
import uk.gov.hmrc.SbtAutoBuildPlugin
import uk.gov.hmrc.versioning.SbtGitVersioning
import uk.gov.hmrc.SbtArtifactory

object HmrcBuild extends Build {

  import uk.gov.hmrc.versioning.SbtGitVersioning.autoImport.majorVersion
  import uk.gov.hmrc.SbtArtifactory.autoImport.makePublicallyAvailableOnBintray

  val appName = "play-scheduling"

  lazy val PlayScheduling = (project in file("."))
    .enablePlugins(SbtAutoBuildPlugin, SbtGitVersioning, SbtArtifactory)
    .settings(majorVersion := 6)
    .settings(makePublicallyAvailableOnBintray := true)
    .settings(
      name := appName,
      scalaVersion := "2.11.12",
      libraryDependencies ++= Seq(
        "uk.gov.hmrc"       %% "mongo-lock"              % "6.10.0-play-25",
        "com.typesafe.play" %% "play"                    % PlayVersion.current % "provided",
        "org.scalatest"     %% "scalatest"               % "3.0.5"             % "test",
        "org.pegdown"       %  "pegdown"                 % "1.6.0"             % "test",
        "ch.qos.logback"    % "logback-classic"          % "1.2.3"             % "test",
        "org.scalatestplus.play" %% "scalatestplus-play" % "2.0.1"             % "test"
      )
    )
}

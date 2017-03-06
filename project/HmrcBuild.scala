import play.core.PlayVersion
import sbt.Keys._
import sbt._
import uk.gov.hmrc.SbtAutoBuildPlugin
import uk.gov.hmrc.versioning.SbtGitVersioning

object HmrcBuild extends Build {

  val appName = "play-scheduling"

  lazy val PlayScheduling = (project in file("."))
    .enablePlugins(SbtAutoBuildPlugin, SbtGitVersioning)
    .settings(
      name := appName,
      scalaVersion := "2.11.7",
      libraryDependencies ++= Seq(
        "com.typesafe.play" %% "play"      % PlayVersion.current % "provided",
        "org.scalatest"     %% "scalatest" % "2.2.4"             % "test",
        "org.pegdown"       %  "pegdown"   % "1.6.0"             % "test",
        "uk.gov.hmrc"       %% "hmrctest"  % "2.3.0"             % "test"
      ),
      developers := List.empty[Developer]
    )
}
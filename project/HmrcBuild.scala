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
    .settings(majorVersion := 5)
    .settings(makePublicallyAvailableOnBintray := true)
    .settings(
      name := appName,
      scalaVersion := "2.11.7",
      libraryDependencies ++= Seq(
        "uk.gov.hmrc"       %% "mongo-lock"         % "5.1.0",
        "uk.gov.hmrc"       %% "play-reactivemongo" % "6.2.0",
        "com.typesafe.play" %% "play"               % PlayVersion.current % "provided",
        "org.scalatest"     %% "scalatest"          % "2.2.4"             % "test",
        "org.pegdown"       %  "pegdown"            % "1.6.0"             % "test",
        "uk.gov.hmrc"       %% "hmrctest"           % "2.3.0"             % "test"
      ),
      developers := List.empty[Developer]
    )
}

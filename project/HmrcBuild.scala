import _root_.play.core.PlayVersion
import sbt.Keys._
import sbt._
import uk.gov.hmrc.SbtAutoBuildPlugin
import uk.gov.hmrc.versioning.SbtGitVersioning


object HmrcBuild extends Build {

  import BuildDependencies._
  import uk.gov.hmrc.DefaultBuildSettings._

  val appName = "play-scheduling"

  lazy val PlayScheduling = (project in file("."))
    .enablePlugins(SbtAutoBuildPlugin, SbtGitVersioning)
    .settings(
      name := appName,
      scalaVersion := "2.11.7",
      libraryDependencies ++= Seq(
        Compile.play,
        Compile.httpVerbs,
        Test.scalaTest,
        Test.hmrcTest,
        Test.pegdown
      ),
      Developers()
    )
}

private object BuildDependencies {

  import _root_.play.core.PlayVersion

  object Compile {
    val httpVerbs = "uk.gov.hmrc" %% "http-verbs" % "2.0.0" % "provided"
    val play = "com.typesafe.play" %% "play"% PlayVersion.current % "provided"
  }

  sealed abstract class Test(scope: String) {
    val scalaTest = "org.scalatest" %% "scalatest" % "2.2.4" % scope
    val pegdown = "org.pegdown" % "pegdown" % "1.5.0" % scope
    val hmrcTest = "uk.gov.hmrc" %% "hmrctest" % "1.4.0" % scope
  }

  object Test extends Test("test")

}
object Developers {

  def apply() = developers := List[Developer]()
}

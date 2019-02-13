import uk.gov.hmrc.SbtArtifactory.autoImport.makePublicallyAvailableOnBintray
import uk.gov.hmrc.{SbtArtifactory, SbtAutoBuildPlugin}
import uk.gov.hmrc.versioning.SbtGitVersioning
import uk.gov.hmrc.versioning.SbtGitVersioning.autoImport.majorVersion

val appName = "play-scheduling"

lazy val library =
  (project in file("."))
    .enablePlugins(SbtAutoBuildPlugin, SbtGitVersioning, SbtArtifactory)
    .settings(
      majorVersion := 6,
      makePublicallyAvailableOnBintray := true,
      name := appName,
      scalaVersion := "2.11.12",
      libraryDependencies ++= AppDependencies.compile ++ AppDependencies.test
    )

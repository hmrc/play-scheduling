val appName = "play-scheduling"

lazy val library =
  Project(appName, file("."))
    .enablePlugins(SbtAutoBuildPlugin, SbtGitVersioning, SbtArtifactory)
    .settings(
      majorVersion := 7,
      makePublicallyAvailableOnBintray := true,
      scalaVersion := "2.11.12",
      libraryDependencies ++= LibDependencies(),
      PlayCrossCompilation.playCrossCompilationSettings
    )

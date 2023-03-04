enablePlugins(ScalaJSBundlerPlugin, GitHubPagesPlugin)

lazy val GitHubUsername = "Kevin-Lee"
lazy val GitHubRepoName = "just-toss"

name := GitHubRepoName

scalaVersion := "2.13.10"

lazy val start = TaskKey[Unit]("start")
lazy val stop = TaskKey[Unit]("stop")

start := (Compile / fastOptJS / startWebpackDevServer).value
stop := (Compile / fastOptJS / stopWebpackDevServer).value

lazy val justToss = (project in file("."))
  .settings(
    gitHubPagesOrgName := GitHubUsername,
    gitHubPagesRepoName := GitHubRepoName,
    gitHubPagesSiteDir := baseDirectory.value / "build",
    (Compile / npmDevDependencies) ++= List(
      "html-webpack-plugin" -> "4.5.1",
      "copy-webpack-plugin" -> "6.4.0",
      "webpack-merge" -> "5.8.0",
    ),
    libraryDependencies += "io.lemonlabs" %%% "scala-uri" % "4.0.2",
    scalacOptions += "-Ymacro-annotations",
    (webpack / version) := "4.44.2",
//    (webpack / version) := "5.64.2",
//    webpackCliVersion := "4.9.1",
    (startWebpackDevServer / version) := "4.5.0",
//    (startWebpackDevServer / version) := "4.11.1",
    webpackResources := baseDirectory.value / "webpack" * "*",
//    useYarn := true,
    webpackDevServerPort := 8888,
    webpackDevServerExtraArgs := Seq("--host", "0.0.0.0"),
    (fastOptJS / webpackConfigFile) := (baseDirectory.value / "webpack" / "webpack-fastopt.config.js").some,
    (fullOptJS / webpackConfigFile) := (baseDirectory.value / "webpack" / "webpack-opt.config.js").some,
    (Test / webpackConfigFile) := (baseDirectory.value / "webpack" / "webpack-core.config.js").some,
    (fastOptJS / webpackDevServerExtraArgs) := Seq("--inline", "--hot"),
    (fastOptJS / webpackBundlingMode) := BundlingMode.LibraryOnly(),
    (Test / requireJsDomEnv) := true,
    addCommandAlias("dev", ";fastOptJS::startWebpackDevServer;~fastOptJS"),
    addCommandAlias("devBuild", ";fastOptJS::startWebpackDevServer;fastOptJS"),
    addCommandAlias("build", "fullOptJS::webpack"),
  )

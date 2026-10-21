enablePlugins(ScalaJSPlugin, GitHubPagesPlugin)

lazy val GitHubUsername = "Kevin-Lee"
lazy val GitHubRepoName = "just-toss"

name := GitHubRepoName

scalaVersion := "2.13.18"

lazy val devSite = TaskKey[File]("devSite", "Builds the development site into target/dev-site")
lazy val build = TaskKey[File]("build", "Builds the production site into build/")

lazy val start = TaskKey[Unit]("start", "Builds the development site and starts the dev server")
lazy val stop = TaskKey[Unit]("stop", "Stops the dev server")

lazy val DevServerHost = "0.0.0.0"
lazy val DevServerPort = 8888

/** Copies the files in `public/` and the Scala.js linker output into `siteDir`. */
def makeSite(publicDir: File, linkerOutputDir: File, siteDir: File): File = {
  IO.delete(siteDir)
  IO.copyDirectory(publicDir, siteDir)
  IO.copyDirectory(linkerOutputDir, siteDir)
  siteDir
}

lazy val justToss = (project in file("."))
  .settings(
    gitHubPagesOrgName := GitHubUsername,
    gitHubPagesRepoName := GitHubRepoName,
    gitHubPagesSiteDir := baseDirectory.value / "build",
    libraryDependencies += "com.indoorvivants" %%% "scala-uri" % "4.2.0",
    scalacOptions += "-Ymacro-annotations",
    scalaJSUseMainModuleInitializer := true,
    devSite := {
      val linkerOutputDir = (Compile / fastLinkJS / scalaJSLinkerOutputDirectory).value
      val _ = (Compile / fastLinkJS).value
      makeSite(baseDirectory.value / "public", linkerOutputDir, target.value / "dev-site")
    },
    build := {
      val linkerOutputDir = (Compile / fullLinkJS / scalaJSLinkerOutputDirectory).value
      val _ = (Compile / fullLinkJS).value
      makeSite(baseDirectory.value / "public", linkerOutputDir, gitHubPagesSiteDir.value)
    },
    watchSources += WatchSource(baseDirectory.value / "public"),
    start := {
      val siteDir = devSite.value
      DevServer.start(siteDir.toPath, DevServerHost, DevServerPort)
      streams.value.log.info(s"Dev server is running at http://localhost:$DevServerPort/")
    },
    stop := {
      DevServer.stop()
      streams.value.log.info("Dev server is stopped")
    },
    addCommandAlias("dev", ";start;~devSite"),
    addCommandAlias("devBuild", "devSite"),
  )

Global / onUnload := (Global / onUnload).value.andThen { state =>
  DevServer.stop()
  state
}

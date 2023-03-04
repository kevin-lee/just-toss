addSbtPlugin("org.scala-js"       % "sbt-scalajs"              % "1.11.0")
//addSbtPlugin("org.portable-scala" % "sbt-scalajs-crossproject" % "1.1.0")

addSbtPlugin("ch.epfl.scala" % "sbt-scalajs-bundler" % "0.20.0")

addSbtPlugin("io.kevinlee" % "sbt-github-pages" % "0.12.0")

val ScalaDevOopsVersion = "2.24.0"
addSbtPlugin("io.kevinlee" % "sbt-devoops-scala"   % ScalaDevOopsVersion)
addSbtPlugin("io.kevinlee" % "sbt-devoops-starter" % ScalaDevOopsVersion)

addSbtPlugin("org.scala-js"       % "sbt-scalajs"              % "1.22.0")
//addSbtPlugin("org.portable-scala" % "sbt-scalajs-crossproject" % "1.1.0")

addSbtPlugin("io.kevinlee" % "sbt-github-pages" % "0.21.0")

val ScalaDevOopsVersion = "3.9.0"
addSbtPlugin("io.kevinlee" % "sbt-devoops-scala"   % ScalaDevOopsVersion)
addSbtPlugin("io.kevinlee" % "sbt-devoops-starter" % ScalaDevOopsVersion)

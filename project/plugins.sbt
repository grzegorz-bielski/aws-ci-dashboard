addSbtPlugin("io.github.davidgregory084" % "sbt-tpolecat" % "0.1.20")
addSbtPlugin("io.spray" % "sbt-revolver" % "0.9.1")
addSbtPlugin("org.portable-scala" % "sbt-scalajs-crossproject" % "1.0.0")
addSbtPlugin("org.scala-js" % "sbt-scalajs" % "1.6.0")
addSbtPlugin("com.github.sbt" % "sbt-native-packager" % "1.9.4")

libraryDependencies += ("org.scala-js" %% "scalajs-env-jsdom-nodejs" % "1.1.0")

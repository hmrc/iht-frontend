resolvers += Resolver.url("HMRC Sbt Plugin Releases", url("https://dl.bintray.com/hmrc/sbt-plugin-releases"))(Resolver.ivyStylePatterns)
resolvers += "HMRC Releases" at "https://dl.bintray.com/hmrc/releases"
resolvers += "SBT Releases" at "https://dl.bintray.com/sbt/sbt-plugin-releases"
resolvers += "Typesafe Releases" at "http://repo.typesafe.com/typesafe/releases/"
resolvers += "scoverage-bintray" at "https://dl.bintray.com/sksamuel/sbt-plugins/"

addSbtPlugin("uk.gov.hmrc" % "sbt-auto-build" % "1.15.0")

addSbtPlugin("uk.gov.hmrc" % "sbt-git-versioning" % "1.17.0")

addSbtPlugin("uk.gov.hmrc" % "sbt-distributables" % "1.3.0")

addSbtPlugin("uk.gov.hmrc" % "sbt-artifactory" % "0.18.0")

addSbtPlugin("com.typesafe.play" % "sbt-plugin" % "2.5.19")

addSbtPlugin("org.ensime" % "ensime-sbt" % "0.1.7")

addSbtPlugin("org.scalastyle" %% "scalastyle-sbt-plugin" % "0.8.0")

addSbtPlugin("org.scoverage" % "sbt-scoverage" % "1.5.0")

addSbtPlugin("org.irundaia.sbt" % "sbt-sassify" % "1.4.9")

addSbtPlugin("net.ground5hark.sbt" % "sbt-concat" % "0.1.9")

addSbtPlugin("com.typesafe.sbt" % "sbt-uglify" % "1.0.4")

addSbtPlugin("com.typesafe.sbt" % "sbt-digest" % "1.1.2")

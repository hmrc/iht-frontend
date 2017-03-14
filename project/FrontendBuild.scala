import sbt._

object FrontendBuild extends Build with MicroService {

  val appName = "iht-frontend"

  override lazy val appDependencies: Seq[ModuleID] = AppDependencies()
}

private object AppDependencies {
  import play.sbt.PlayImport._
  import play.core.PlayVersion

  private val httpCachingClientVersion = "6.1.0"
  private val jsonSchemaValidatorVersion = "2.2.6"
  private val jsonVersion = "20160212"

val compile = Seq(
  ws, cache,
  "uk.gov.hmrc" %% "play-health" % "2.0.0",
  "uk.gov.hmrc" %% "play-ui" % "5.2.0",
  "uk.gov.hmrc" %% "govuk-template" % "5.0.0",
  "uk.gov.hmrc" %% "url-builder" % "2.0.0",
  "uk.gov.hmrc" %% "http-caching-client" % httpCachingClientVersion,
  "uk.gov.hmrc" %% "frontend-bootstrap" % "7.11.0",
  "uk.gov.hmrc" %% "play-partials" % "5.2.0",
  "uk.gov.hmrc" %% "play-config" % "3.0.0",
  "uk.gov.hmrc" %% "play-filters" % "5.6.0",
  "uk.gov.hmrc" %% "logback-json-logger" % "3.1.0",
  "uk.gov.hmrc" %% "passcode-verification" % "4.0.0",
  "uk.gov.hmrc" %% "play-authorised-frontend" % "6.3.0",
  "uk.gov.hmrc" %% "http-verbs" % "6.2.0",
  "uk.gov.hmrc" %% "play-auditing" % "2.6.0",
  "uk.gov.hmrc" %% "domain" % "4.0.0",
  "uk.gov.hmrc" %% "play-language" % "3.0.0",
  "com.github.fge" % "json-schema-validator" % jsonSchemaValidatorVersion,
  "uk.gov.hmrc" %% "play-graphite" % "3.1.0",
  "org.apache.xmlgraphics" % "fop" % "2.1",
  "org.json" % "json" % jsonVersion
)

trait TestDependencies {
  lazy val scope: String = "test"
  lazy val test : Seq[ModuleID] = ???
}

object Test {
  def apply() = new TestDependencies {
    override lazy val test = Seq(
      "uk.gov.hmrc" %% "http-caching-client" % httpCachingClientVersion % scope,
      "uk.gov.hmrc" %% "hmrctest" % "2.2.0" % scope,
      "org.scalatest" %% "scalatest" % "2.2.6" % scope,
//      "org.scalatestplus" %% "play" % "1.2.0" % scope,
      "org.scalatestplus.play" %% "scalatestplus-play" % "1.5.1",
      "org.pegdown" % "pegdown" % "1.6.0" % scope,
      "org.jsoup" % "jsoup" % "1.8.1" % scope,
      "com.typesafe.play" %% "play-test" % PlayVersion.current % scope,
      "org.mockito" % "mockito-all" % "1.10.19" % scope,
      "com.github.fge" % "json-schema-validator" % jsonSchemaValidatorVersion % scope,
      "org.json" % "json" % jsonVersion % scope
    )
  }.test
}

  def apply() = compile ++ Test()
}

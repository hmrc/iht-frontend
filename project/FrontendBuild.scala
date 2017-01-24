import sbt._

object FrontendBuild extends Build with MicroService {

  val appName = "iht-frontend"

  override lazy val appDependencies: Seq[ModuleID] = AppDependencies()
}

private object AppDependencies {
  import play.PlayImport._
  import play.core.PlayVersion

  private val httpCachingClientVersion = "5.6.0"
  private val jsonSchemaValidatorVersion = "2.2.6"
  private val jsonVersion = "20160212"

val compile = Seq(
  ws, cache,
  "uk.gov.hmrc" %% "play-health" % "1.1.0",
  "uk.gov.hmrc" %% "play-ui" % "4.17.2",
  "uk.gov.hmrc" %% "govuk-template" % "4.0.0",
  "uk.gov.hmrc" %% "url-builder" % "1.0.0",
  "uk.gov.hmrc" %% "http-caching-client" % httpCachingClientVersion,
  "uk.gov.hmrc" %% "frontend-bootstrap" % "6.7.0",
  "uk.gov.hmrc" %% "play-partials" % "4.6.0",
  "uk.gov.hmrc" %% "play-config" % "2.1.0",
  "uk.gov.hmrc" %% "play-json-logger" % "2.1.1",
  "uk.gov.hmrc" %% "passcode-verification" % "3.5.0",
  "uk.gov.hmrc" %% "play-authorised-frontend" % "5.8.0",
  "uk.gov.hmrc" %% "http-verbs" % "5.0.0",
  "uk.gov.hmrc" %% "play-auditing" % "1.9.0",
  "uk.gov.hmrc" %% "domain" % "3.7.0",
  "com.github.fge" % "json-schema-validator" % jsonSchemaValidatorVersion,
  "uk.gov.hmrc" %% "play-graphite" % "2.0.0",
  "org.apache.xmlgraphics" % "fop" % "2.1",
  "org.json" % "json" % jsonVersion,
  "uk.gov.hmrc" %% "play-conditional-form-mapping" % "0.2.0"
)

trait TestDependencies {
  lazy val scope: String = "test"
  lazy val test : Seq[ModuleID] = ???
}

object Test {
  def apply() = new TestDependencies {
    override lazy val test = Seq(
      "uk.gov.hmrc" %% "http-caching-client" % httpCachingClientVersion % scope,
      "uk.gov.hmrc" %% "hmrctest" % "1.9.0" % scope,
      "org.scalatest" %% "scalatest" % "2.2.2" % scope,
      "org.scalatestplus" %% "play" % "1.2.0" % scope,
      "org.pegdown" % "pegdown" % "1.4.2" % scope,
      "org.jsoup" % "jsoup" % "1.7.3" % scope,
      "com.typesafe.play" %% "play-test" % PlayVersion.current % scope,
      "org.mockito" % "mockito-all" % "1.9.5" % scope,
      "com.github.fge" % "json-schema-validator" % jsonSchemaValidatorVersion % scope,
      "org.json" % "json" % jsonVersion % scope
    )
  }.test
}

  def apply() = compile ++ Test()
}

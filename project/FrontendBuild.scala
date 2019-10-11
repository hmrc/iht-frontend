import sbt._

object FrontendBuild extends Build with MicroService {

  val appName = "iht-frontend"

  override lazy val appDependencies: Seq[ModuleID] = AppDependencies()
}

private object AppDependencies {
  import play.sbt.PlayImport._
  import play.core.PlayVersion

  private val httpCachingClientVersion = "9.0.0-play-26"
  private val jsonSchemaValidatorVersion = "2.2.6"
  private val jsonVersion = "20180813"
  private val wireMockVersion = "2.25.0"

  private val typesafe = "com.typesafe.play"

  val compile: Seq[ModuleID] = Seq(
    ws, cache,
    "uk.gov.hmrc" %% "url-builder" % "3.1.0",
    "uk.gov.hmrc" %% "http-caching-client" % httpCachingClientVersion,
    "uk.gov.hmrc" %% "bootstrap-play-26" % "1.1.0",
    "uk.gov.hmrc" %% "auth-client" % "2.30.0-play-26",
    "uk.gov.hmrc" %% "play-partials" % "6.9.0-play-26",
    "uk.gov.hmrc" %% "domain" % "5.6.0-play-26",
    "uk.gov.hmrc" %% "govuk-template" % "5.43.0-play-26",
    "uk.gov.hmrc" %% "play-ui" % "8.2.0-play-26",
    "uk.gov.hmrc" %% "play-language" % "3.4.0",
    typesafe %% "play-json" % "2.6.13",
    typesafe %% "play-json-joda" % "2.6.13",
    "com.github.fge" % "json-schema-validator" % jsonSchemaValidatorVersion,
    "org.apache.xmlgraphics" % "fop" % "2.3",
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
        "uk.gov.hmrc" %% "hmrctest" % "3.9.0-play-26" % scope,
        "org.scalatest" %% "scalatest" % "3.0.8" % scope,
        "org.scalatestplus.play" %% "scalatestplus-play" % "3.1.2",
        "org.pegdown" % "pegdown" % "1.6.0" % scope,
        "org.jsoup" % "jsoup" % "1.12.1" % scope,
        "com.typesafe.play" %% "play-test" % PlayVersion.current % scope,
        "org.mockito" % "mockito-core" % "3.1.0" % scope,
        "com.github.fge" % "json-schema-validator" % jsonSchemaValidatorVersion % scope,
        "org.json" % "json" % jsonVersion % scope
      )
    }.test
  }

  val jettyFromWiremockVersion = "9.2.24.v20180105"

  object IntegrationTest {
    def apply() = new TestDependencies {

      override lazy val scope: String = "it"

      override lazy val test = Seq(
        "uk.gov.hmrc" %% "http-caching-client" % httpCachingClientVersion % scope,
        "uk.gov.hmrc" %% "hmrctest" % "3.3.0" % scope,
        "org.scalatest" %% "scalatest" % "3.0.8" % scope,
        "org.scalatestplus.play" %% "scalatestplus-play" % "3.1.2",
        "org.pegdown" % "pegdown" % "1.6.0" % scope,
        "org.jsoup" % "jsoup" % "1.12.1" % scope,
        "com.typesafe.play" %% "play-test" % PlayVersion.current % scope,
        "org.mockito" % "mockito-core" % "3.1.0" % scope,
        "com.github.fge" % "json-schema-validator" % jsonSchemaValidatorVersion % scope,
        "org.json" % "json" % jsonVersion % scope,
        "com.github.tomakehurst" %  "wiremock" % wireMockVersion % scope,
        "com.github.tomakehurst" %  "wiremock-jre8" % wireMockVersion % scope
      )
    }.test
  }

  def apply() = compile ++ Test() ++ IntegrationTest()
}

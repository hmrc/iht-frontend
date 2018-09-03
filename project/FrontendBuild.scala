import sbt._

object FrontendBuild extends Build with MicroService {

  val appName = "iht-frontend"

  override lazy val appDependencies: Seq[ModuleID] = AppDependencies()
}

private object AppDependencies {
  import play.sbt.PlayImport._
  import play.core.PlayVersion

  private val httpCachingClientVersion = "7.1.0"
  private val jsonSchemaValidatorVersion = "2.2.6"
  private val jsonVersion = "20180130"
  private val wireMockVersion = "2.9.0"

val compile = Seq(
  ws, cache,
  "uk.gov.hmrc" %% "url-builder" % "2.1.0",
  "uk.gov.hmrc" %% "http-caching-client" % httpCachingClientVersion,
  "uk.gov.hmrc" %% "frontend-bootstrap" % "10.1.0",
  "uk.gov.hmrc" %% "play-partials" % "6.1.0",
  "uk.gov.hmrc" %% "domain" % "5.2.0",
  "uk.gov.hmrc" %% "play-language" % "3.4.0",
  "com.github.fge" % "json-schema-validator" % jsonSchemaValidatorVersion,
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
      "uk.gov.hmrc" %% "hmrctest" % "3.0.0" % scope,
      "org.scalatest" %% "scalatest" % "3.0.0" % scope,
      "org.scalatestplus.play" %% "scalatestplus-play" % "2.0.0",
      "org.pegdown" % "pegdown" % "1.6.0" % scope,
      "org.jsoup" % "jsoup" % "1.10.2" % scope,
      "com.typesafe.play" %% "play-test" % PlayVersion.current % scope,
      "org.mockito" % "mockito-core" % "2.13.0" % scope,
      "com.github.fge" % "json-schema-validator" % jsonSchemaValidatorVersion % scope,
      "org.json" % "json" % jsonVersion % scope
    )
  }.test
}

  object IntegrationTest {
    def apply() = new TestDependencies {

      override lazy val scope: String = "it"

      override lazy val test = Seq(
        "uk.gov.hmrc" %% "http-caching-client" % httpCachingClientVersion % scope,
        "uk.gov.hmrc" %% "hmrctest" % "3.0.0" % scope,
        "org.scalatest" %% "scalatest" % "3.0.0" % scope,
        "org.scalatestplus.play" %% "scalatestplus-play" % "2.0.0",
        "org.pegdown" % "pegdown" % "1.6.0" % scope,
        "org.jsoup" % "jsoup" % "1.10.2" % scope,
        "com.typesafe.play" %% "play-test" % PlayVersion.current % scope,
        "org.mockito" % "mockito-core" % "2.13.0" % scope,
        "com.github.fge" % "json-schema-validator" % jsonSchemaValidatorVersion % scope,
        "org.json" % "json" % jsonVersion % scope,
        "com.github.tomakehurst"  %  "wiremock" % wireMockVersion % scope
      )
    }.test
  }

  def apply() = compile ++ Test() ++ IntegrationTest()
}

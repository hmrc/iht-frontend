import sbt.ModuleID
import sbt._

object AppDependencies {

  import play.sbt.PlayImport._
  import play.core.PlayVersion

  private val httpCachingClientVersion = "9.1.0-play-26"
  private val bootstrapVersion = "1.13.0"
  private val playPartialsVersion = "6.11.0-play-26"
  private val domainVersion = "5.9.0-play-26"
  private val govUkTemplateVersion = "5.55.0-play-26"
  private val playUiVersion = "8.11.0-play-26"
  private val playLanguageVersion = "4.3.0-play-26"
  private val jsonVersion = "20200518"
  private val wireMockVersion = "2.26.3"
  private val jsoupVersion = "1.13.1"
  private val pegdownVersion = "1.6.0"
  private val mockitoVersion = "3.3.3"
  private val playJsonVersion = "2.6.14"
  private val hmrcTestVersion = "3.9.0-play-26"
  private val scalaTestVersion = "3.0.8"
  private val scalaTestPlusPlayVersion = "3.1.3"

  private val typesafe = "com.typesafe.play"

  val compile: Seq[ModuleID] = Seq(
    ws, cache,
    "uk.gov.hmrc" %% "http-caching-client" % httpCachingClientVersion,
    "uk.gov.hmrc" %% "bootstrap-play-26" % bootstrapVersion,
    "uk.gov.hmrc" %% "play-partials" % playPartialsVersion,
    "uk.gov.hmrc" %% "domain" % domainVersion,
    "uk.gov.hmrc" %% "govuk-template" % govUkTemplateVersion,
    "uk.gov.hmrc" %% "play-ui" % playUiVersion,
    "uk.gov.hmrc" %% "play-language" % playLanguageVersion,
    typesafe %% "play-json" % playJsonVersion,
    typesafe %% "play-json-joda" % playJsonVersion,
    "org.apache.xmlgraphics" % "fop" % "2.3",
    "org.json" % "json" % jsonVersion,
    "org.apache.commons" % "commons-text" % "1.8"
  )

  trait TestDependencies {
    lazy val scope: String = "test"
    lazy val test: Seq[ModuleID] = ???
  }

  object Test {
    def apply(): Seq[ModuleID] = new TestDependencies {
      override lazy val test = Seq(
        "uk.gov.hmrc" %% "http-caching-client" % httpCachingClientVersion % scope,
        "uk.gov.hmrc" %% "hmrctest" % hmrcTestVersion % scope,
        "org.scalatest" %% "scalatest" % scalaTestVersion % scope,
        "org.scalatestplus.play" %% "scalatestplus-play" % scalaTestPlusPlayVersion,
        "org.pegdown" % "pegdown" % pegdownVersion % scope,
        "org.jsoup" % "jsoup" % jsoupVersion % scope,
        "com.typesafe.play" %% "play-test" % PlayVersion.current % scope,
        "org.mockito" % "mockito-core" % mockitoVersion % scope,
        "org.json" % "json" % jsonVersion % scope
      )
    }.test
  }

  val jettyFromWiremockVersion = "9.2.24.v20180105"

  object IntegrationTest {
    def apply(): Seq[ModuleID] = new TestDependencies {

      override lazy val scope: String = "it"

      override lazy val test = Seq(
        "uk.gov.hmrc" %% "http-caching-client" % httpCachingClientVersion % scope,
        "uk.gov.hmrc" %% "hmrctest" % hmrcTestVersion % scope,
        "org.scalatest" %% "scalatest" % scalaTestVersion % scope,
        "org.scalatestplus.play" %% "scalatestplus-play" % scalaTestPlusPlayVersion,
        "org.pegdown" % "pegdown" % pegdownVersion % scope,
        "org.jsoup" % "jsoup" % jsoupVersion % scope,
        "com.typesafe.play" %% "play-test" % PlayVersion.current % scope,
        "org.mockito" % "mockito-core" % mockitoVersion % scope,
        "org.json" % "json" % jsonVersion % scope,
        "com.github.tomakehurst" % "wiremock" % wireMockVersion % scope,
        "com.github.tomakehurst" % "wiremock-jre8" % wireMockVersion % scope
      )
    }.test
  }

  def apply(): Seq[ModuleID] = compile ++ Test() ++ IntegrationTest()
}
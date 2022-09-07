import sbt.ModuleID
import sbt._

object AppDependencies {

  import play.sbt.PlayImport._
  import play.core.PlayVersion

  private val httpCachingClientVersion = "9.5.0-play-28"
  private val bootstrapVersion = "5.24.0"
  private val playPartialsVersion = "8.1.0-play-28"
  private val domainVersion = "6.0.0-play-28"
  private val govUkTemplateVersion = "5.68.0-play-28"
  private val playUiVersion = "9.5.0-play-28"
  private val playLanguageVersion = "5.1.0-play-28"
  private val jsonVersion = "20200518"
  private val wireMockVersion = "2.27.2"
  private val jsoupVersion = "1.14.1"
  private val pegdownVersion = "1.6.0"
  private val mockitoVersion = "3.3.3"
  private val playJsonVersion = "2.6.14"
  private val scalaTestVersion = "3.1.0"
  private val scalaTestPlusPlayVersion = "5.1.0"
  private val scalaTestPlusMockitoVersion = "1.0.0-M2"
  private val flexmarkVersion = "0.35.10"

  private val typesafe = "com.typesafe.play"

  val compile: Seq[ModuleID] = Seq(
    ws, ehcache,
    "uk.gov.hmrc" %% "http-caching-client" % httpCachingClientVersion,
    "uk.gov.hmrc" %% "bootstrap-frontend-play-28" % bootstrapVersion,
    "uk.gov.hmrc" %% "play-partials" % playPartialsVersion,
    "uk.gov.hmrc" %% "domain" % domainVersion,
    "uk.gov.hmrc" %% "govuk-template" % govUkTemplateVersion,
    "uk.gov.hmrc" %% "play-ui" % playUiVersion,
    "uk.gov.hmrc" %% "play-language" % playLanguageVersion,
    typesafe %% "play-json" % playJsonVersion,
    typesafe %% "play-json-joda" % playJsonVersion,
    "org.apache.xmlgraphics" % "fop" % "2.3",
    "org.json" % "json" % jsonVersion,
    "org.apache.commons" % "commons-text" % "1.9"
  )

  trait TestDependencies {
    lazy val scope: String = "test"
    lazy val test: Seq[ModuleID] = ???
  }

  object Test {
    def apply(): Seq[ModuleID] = new TestDependencies {
      override lazy val test = Seq(
        "uk.gov.hmrc" %% "http-caching-client" % httpCachingClientVersion,
        "org.scalatest" %% "scalatest" % scalaTestVersion,
        "org.scalatestplus.play" %% "scalatestplus-play" % scalaTestPlusPlayVersion,
        "org.scalatestplus" %%  "scalatestplus-mockito" % scalaTestPlusMockitoVersion,
        "com.vladsch.flexmark" % "flexmark-all" % flexmarkVersion,
        "org.pegdown" % "pegdown" % pegdownVersion,
        "org.jsoup" % "jsoup" % jsoupVersion,
        "com.typesafe.play" %% "play-test" % PlayVersion.current,
        "org.mockito" % "mockito-core" % mockitoVersion,
        "org.json" % "json" % jsonVersion
      ).map(_ % scope)
    }.test
  }

  val jettyFromWiremockVersion = "9.2.24.v20180105"

  object IntegrationTest {
    def apply(): Seq[ModuleID] = new TestDependencies {

      override lazy val scope: String = "it"

      override lazy val test = Seq(
        "uk.gov.hmrc" %% "http-caching-client" % httpCachingClientVersion,
        "org.scalatest" %% "scalatest" % scalaTestVersion,
        "org.scalatestplus.play" %% "scalatestplus-play" % scalaTestPlusPlayVersion,
        "org.scalatestplus" %%  "scalatestplus-mockito" % scalaTestPlusMockitoVersion,
        "com.vladsch.flexmark" % "flexmark-all" % flexmarkVersion,
        "org.pegdown" % "pegdown" % pegdownVersion,
        "org.jsoup" % "jsoup" % jsoupVersion,
        "com.typesafe.play" %% "play-test" % PlayVersion.current,
        "org.mockito" % "mockito-core" % mockitoVersion,
        "org.json" % "json" % jsonVersion,
        "com.fasterxml.jackson.module" %% "jackson-module-scala" % "2.12.2",
        "com.github.tomakehurst" % "wiremock" % wireMockVersion,
        "com.github.tomakehurst" % "wiremock-jre8" % wireMockVersion
      ).map(_ % scope)
    }.test
  }

  def apply(): Seq[ModuleID] = compile ++ Test() ++ IntegrationTest()
}
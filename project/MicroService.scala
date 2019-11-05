import play.routes.compiler.StaticRoutesGenerator
import play.routes.compiler.InjectedRoutesGenerator
import sbt.Keys._
import sbt._
import uk.gov.hmrc.sbtdistributables.SbtDistributablesPlugin
import uk.gov.hmrc.sbtdistributables.SbtDistributablesPlugin._
import play.sbt.routes.RoutesKeys.routesGenerator
import uk.gov.hmrc.versioning.SbtGitVersioning.autoImport.majorVersion

// imports for Asset Pipeline
import com.typesafe.sbt.digest.Import._
import com.typesafe.sbt.uglify.Import._
import com.typesafe.sbt.web.Import._
import net.ground5hark.sbt.concat.Import._
import uk.gov.hmrc.versioning.SbtGitVersioning

trait MicroService {

  import uk.gov.hmrc._
  import DefaultBuildSettings._

  val appName: String

  lazy val appDependencies : Seq[ModuleID] = ???
  lazy val plugins : Seq[Plugins] = Seq(
    play.sbt.PlayScala,
    SbtAutoBuildPlugin,
    SbtGitVersioning,
    SbtDistributablesPlugin,
    SbtArtifactory
  )
  lazy val playSettings : Seq[Setting[_]] = Seq.empty

  lazy val scoverageSettings = {
    import scoverage.ScoverageKeys
    Seq(
      // Semicolon-separated list of regexs matching classes to exclude
      ScoverageKeys.coverageExcludedPackages := "<empty>;Reverse.*;.*AuthService.*;models/.data/..*;.*BuildInfo.*;prod.Routes;app.Routes;testOnlyDoNotUseInAppConf.Routes;iht.controllers.wraith.*;iht.controllers.testonly.*;iht.views.html.testOnly.*;wraith.Routes;taxreturn.Routes;registration.Routes;iht.auth.*;iht.controllers.auth.*;iht.connector.*;iht.config.*;iht.metrics.*;iht.views.html.ihtHelpers.standard.*;iht.models.Person;iht.models.TaxIdsFormat;iht.forms.testonly.*;iht.controllers.application.tnrb.javascript.*;iht.controllers.application.assets.*;iht.controllers.application.exemptions.*;iht.controllers.application.debts.javascript.*;iht.controllers.javascript.*;iht.controllers.registration.javascript.*;iht.controllers.registration.executor.javascript.*;iht.controllers.registration.deceased.javascript.*;iht.controllers.registration.javascript.*;iht.controllers.application.declaration.javascript.*;iht.controllers.application.gifts.javascript.*;iht.controllers.application.pdf.javascript.*;iht.controllers.application.status.javascript.*;iht.controllers.application.status.javascript.*;iht.controllers.filter.javascript.*;iht.controllers.registration.applicant.javascript.*;iht.views.html.ihtHelpers.custom.*;iht.controllers.application.javascript.*;iht.models.des.ihtReturn.Gift;iht.views.html.application.application_questionnaire;iht.views.html.iht_not_found_template;",
      ScoverageKeys.coverageMinimum := 80,
      ScoverageKeys.coverageFailOnMinimum := false,
      ScoverageKeys.coverageHighlighting := true,
      parallelExecution in Test := false
    )
  }


  lazy val microservice = Project(appName, file("."))
    .enablePlugins(plugins : _*)
    .settings(playSettings ++ scoverageSettings : _*)
//    .settings(scalaSettings: _*)
    .settings(publishingSettings: _*)
    .settings(
      scalaVersion := "2.11.12",
      libraryDependencies ++= appDependencies,
      fork in Test := false,
      retrieveManaged := true,
      evictionWarningOptions in update := EvictionWarningOptions.default.withWarnScalaVersionEviction(false)
     )
    .settings(
      Concat.groups := Seq(
        "javascripts/iht-app.js" -> group(Seq("javascripts/ie9-polyfill-input.js", "javascripts/timeout-dialog.js", "javascripts/autobox.js", "javascripts/show-hide-content.js", "javascripts/iht.js"))
      ),
      uglifyCompressOptions := Seq("unused=false", "dead_code=false"),
      pipelineStages := Seq(digest),
      pipelineStages in Assets := Seq(concat,uglify),
      includeFilter in uglify := GlobFilter("iht-*.js")
    )
    .configs(IntegrationTest)
    .settings(inConfig(IntegrationTest)(Defaults.itSettings): _*)
    .settings(integrationTestSettings())
    .settings(resolvers ++= Seq(Resolver.bintrayRepo("hmrc", "releases"), Resolver.jcenterRepo))
    .settings(majorVersion := 6)
}

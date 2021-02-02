/*
 * Copyright 2021 HM Revenue & Customs
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package iht.controllers.registration

import iht.config.AppConfig
import iht.connector.CachingConnector
import iht.metrics.IhtMetrics
import iht.models.enums.KickOutSource
import iht.utils.CommonHelper
import iht.views.html.registration.kickout._
import javax.inject.Inject
import play.api.i18n.Messages
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents, Request}
import play.twirl.api.HtmlFormat
import uk.gov.hmrc.auth.core.AuthConnector
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController
import uk.gov.hmrc.play.partials.FormPartialRetriever

import scala.concurrent.Future

class KickoutRegControllerImpl @Inject()(val metrics: IhtMetrics,
                                         val formPartialRetriever: FormPartialRetriever,
                                         val cachingConnector: CachingConnector,
                                         val authConnector: AuthConnector,
                                         implicit val appConfig: AppConfig,
                                         val cc: MessagesControllerComponents) extends FrontendController(cc) with KickoutRegController

trait KickoutRegController extends RegistrationController {
  val metrics: IhtMetrics

  implicit val formPartialRetriever: FormPartialRetriever

  def cachingConnector: CachingConnector
  override def guardConditions: Set[Predicate] = Set.empty

  lazy val applicantProbateLocationPageLoad = iht.controllers.registration.applicant.routes.ProbateLocationController.onPageLoad()
  lazy val deceasedPermHomePageLoad = iht.controllers.registration.deceased.routes.DeceasedPermanentHomeController.onPageLoad()
  lazy val deceasedDateOfDeathPageLoad = iht.controllers.registration.deceased.routes.DeceasedDateOfDeathController.onPageLoad()
  lazy val applicantApplyingForProbatePageLoad = iht.controllers.registration.applicant.routes.ApplyingForProbateController.onPageLoad()
  lazy val applicantExecutorOfEstatePageLoad = iht.controllers.registration.applicant.routes.ExecutorOfEstateController.onPageLoad()

  def probateKickoutView(contentLines: Seq[String])(implicit request: Request[_]) =
    kickout_template(Messages("page.iht.registration.applicantDetails.kickout.probate.summary"),
      applicantProbateLocationPageLoad)(contentLines)

  def locationKickoutView(contentLines: Seq[String])(implicit request: Request[_]) =
    kickout_template(Messages("page.iht.registration.deceasedDetails.kickout.location.summary"),
      deceasedPermHomePageLoad)(contentLines)

  def capitalTaxKickoutView(contentLines: Seq[String])(implicit request: Request[_]) =
    kickout_template(Messages("page.iht.registration.deceasedDateOfDeath.kickout.date.capital.tax.summary"),
      deceasedDateOfDeathPageLoad,
      Messages("iht.registration.kickout.returnToTheDateOfDeath"))(contentLines)

  def dateOtherKickoutView(contentLines: Seq[String])(implicit request: Request[_]) = {
    val viewFunc: Seq[String] => HtmlFormat.Appendable =
      kickout_template(Messages("page.iht.registration.deceasedDateOfDeath.kickout.date.other.summary"), deceasedDateOfDeathPageLoad)

    viewFunc(contentLines)
  }

  def notApplyingForProbateKickoutView(contentLines: Seq[String])(implicit request: Request[_]) =
    kickout_expander(Messages("page.iht.registration.notApplyingForProbate.kickout.summary"),
      applicantApplyingForProbatePageLoad, Messages("iht.changeYourAnswer"))(contentLines)

  def notAnExecutorKickoutView(content: String)(implicit request: Request[_]) =
    kickout_template_simple(applicantExecutorOfEstatePageLoad, Messages("iht.changeYourAnswer"))(content)

  def content(implicit messages:Messages): Map[String, Request[_] => HtmlFormat.Appendable] = Map(
    KickoutApplicantDetailsProbateScotland ->
      (request => probateKickoutView(
        Seq(
          messages("iht.registration.kickout.probateLocation.scotland"),
          messages("iht.registration.kickout.ifWantChangeProbateLocation")))(request)),
    KickoutApplicantDetailsProbateNi ->
      (request => probateKickoutView(
        Seq(
          messages("iht.registration.kickout.content"),
          messages("iht.registration.kickout.ifWantChangeProbateLocation")))(request)),
    KickoutDeceasedDetailsLocationScotland ->
      (request => locationKickoutView(
        Seq(
          messages("iht.registration.kickout.probateLocation.scotland"),
          messages("iht.registration.kickout.ifWantChangeDeceasedLocation")))(request)),
    KickoutDeceasedDetailsLocationNI ->
      (request => locationKickoutView(
        Seq(
          messages("iht.registration.kickout.content"),
          messages("iht.registration.kickout.ifWantChangeDeceasedLocation")))(request)),
    KickoutDeceasedDetailsLocationOther ->
      (request => locationKickoutView(
        Seq(
          messages("iht.registration.kickout.content"),
          messages("iht.registration.kickout.ifWantChangeDeceasedLocation")))(request)),
    KickoutDeceasedDateOfDeathDateCapitalTax ->
      (request => capitalTaxKickoutView(Seq(
        messages("iht.registration.kickout.message.phone"),
        messages("iht.registration.kickout.message.phone2"),
        messages("iht.registration.kickout.message.changeTheDate")
      ))(request)),
    KickoutDeceasedDateOfDeathDateOther ->
      (request => dateOtherKickoutView(Seq(
        messages("iht.registration.kickout.content"),
        messages("iht.registration.kickout.message.form2")
      ))(request)),
    KickoutNotApplyingForProbate ->
      (request => notApplyingForProbateKickoutView(Seq(
        messages("page.iht.registration.notApplyingForProbate.kickout.p1"),
        messages("page.iht.registration.notApplyingForProbate.kickout.p2")
      ))(request)),
    KickoutNotAnExecutor ->
      (request => notAnExecutorKickoutView(
        messages("page.iht.registration.notAnExecutor.kickout.p1"))(request))
  )

  def onPageLoad: Action[AnyContent] = authorisedForIht {
    implicit request => {
      withRegistrationDetails { regDetails =>
        cachingConnector.getSingleValue(RegistrationKickoutReasonCachingKey) map { reason =>
          Ok(content(implicitly[Messages])(CommonHelper.getOrException(reason))(request))
        }
      }
    }
  }

  def onSubmit: Action[AnyContent] = authorisedForIht {
    implicit request => // False positive warning. Workaround: scala/bug#11175 -Ywarn-unused:params false positive
      metrics.kickOutCounter(KickOutSource.REGISTRATION)
      Future.successful(Redirect(appConfig.linkRegistrationKickOut))
  }
}

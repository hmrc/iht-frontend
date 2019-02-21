/*
 * Copyright 2019 HM Revenue & Customs
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

import iht.connector.CachingConnector
import iht.constants.IhtProperties
import iht.metrics.IhtMetrics
import iht.models.enums.KickOutSource
import iht.utils.RegistrationKickOutHelper._
import iht.utils.{CommonHelper, DeceasedInfoHelper}
import iht.views.html.registration.kickout._
import javax.inject.Inject
import play.api.Play.current
import play.api.i18n.Messages
import play.api.i18n.Messages.Implicits._
import play.api.mvc.Request
import play.twirl.api.HtmlFormat
import uk.gov.hmrc.auth.core.AuthConnector
import uk.gov.hmrc.play.partials.FormPartialRetriever

import scala.concurrent.Future

class KickoutRegControllerImpl @Inject()(val metrics: IhtMetrics,
                                         val formPartialRetriever: FormPartialRetriever,
                                         val cachingConnector: CachingConnector,
                                         val authConnector: AuthConnector) extends KickoutRegController

trait KickoutRegController extends RegistrationController {
  val metrics: IhtMetrics

  implicit val formPartialRetriever: FormPartialRetriever

  def cachingConnector: CachingConnector
  override def guardConditions: Set[Predicate] = Set.empty

  lazy val applicantProbateLocationPageLoad = iht.controllers.registration.applicant.routes.ProbateLocationController.onPageLoad()
  lazy val deceasedPermHomePageLoad = iht.controllers.registration.deceased.routes.DeceasedPermanentHomeController.onPageLoad()
  lazy val deceasedDateOfDeathPageLoad = iht.controllers.registration.deceased.routes.DeceasedDateOfDeathController.onPageLoad()
  lazy val applicantApplyingForProbatePageLoad = iht.controllers.registration.applicant.routes.ApplyingForProbateController.onPageLoad()

  def probateKickoutView(contentLines: Seq[String])(implicit request: Request[_], messages:Messages) =
    kickout_template(messages("page.iht.registration.applicantDetails.kickout.probate.summary"),
      applicantProbateLocationPageLoad)(contentLines)(request, applicationMessages, formPartialRetriever)

  def locationKickoutView(contentLines: Seq[String])(implicit request: Request[_], messages:Messages) =
    kickout_template(messages("page.iht.registration.deceasedDetails.kickout.location.summary"),
      deceasedPermHomePageLoad)(contentLines)(request, applicationMessages, formPartialRetriever)

  def capitalTaxKickoutView(contentLines: Seq[String])(implicit request: Request[_], messages:Messages) =
    kickout_template(messages("page.iht.registration.deceasedDateOfDeath.kickout.date.capital.tax.summary"),
      deceasedDateOfDeathPageLoad,
      messages("iht.registration.kickout.returnToTheDateOfDeath"))(contentLines)(request, applicationMessages, formPartialRetriever)

  def dateOtherKickoutView(contentLines: Seq[String])(implicit request: Request[_], messages:Messages) =
    kickout_template(messages("page.iht.registration.deceasedDateOfDeath.kickout.date.other.summary"),
      deceasedDateOfDeathPageLoad)(contentLines)(request, applicationMessages,formPartialRetriever)

  def notApplyingForProbateKickoutView(contentLines: Seq[String])(implicit request: Request[_], messages:Messages, deceasedName: String) =
    kickout_template(messages("page.iht.registration.notApplyingForProbate.kickout.summary", deceasedName),
      applicantApplyingForProbatePageLoad)(contentLines)(request, applicationMessages, formPartialRetriever)

  def content(implicit messages:Messages, deceasedName: String): Map[String, Request[_] => HtmlFormat.Appendable] = Map(
    KickoutApplicantDetailsProbateScotland ->
      (request => probateKickoutView(
        Seq(
          messages("iht.registration.kickout.probateLocation.scotland"),
          messages("iht.registration.kickout.ifWantChangeProbateLocation")))(request, messages)),
    KickoutApplicantDetailsProbateNi ->
      (request => probateKickoutView(
        Seq(
          messages("iht.registration.kickout.content"),
          messages("iht.registration.kickout.ifWantChangeProbateLocation")))(request, messages)),
    KickoutDeceasedDetailsLocationScotland ->
      (request => locationKickoutView(
        Seq(
          messages("iht.registration.kickout.probateLocation.scotland"),
          messages("iht.registration.kickout.ifWantChangeDeceasedLocation")))(request, messages)),
    KickoutDeceasedDetailsLocationNI ->
      (request => locationKickoutView(
        Seq(
          messages("iht.registration.kickout.content"),
          messages("iht.registration.kickout.ifWantChangeDeceasedLocation")))(request, messages)),
    KickoutDeceasedDetailsLocationOther ->
      (request => locationKickoutView(
        Seq(
          messages("iht.registration.kickout.content"),
          messages("iht.registration.kickout.ifWantChangeDeceasedLocation")))(request, messages)),
    KickoutDeceasedDateOfDeathDateCapitalTax ->
      (request => capitalTaxKickoutView(Seq(
        messages("iht.registration.kickout.message.phone"),
        messages("iht.registration.kickout.message.phone2"),
        messages("iht.registration.kickout.message.changeTheDate")
      ))(request, messages)),
    KickoutDeceasedDateOfDeathDateOther ->
      (request => dateOtherKickoutView(Seq(
        messages("iht.registration.kickout.content"),
        messages("iht.registration.kickout.message.form2")
      ))(request, messages)),
    KickoutNotApplyingForProbate ->
      (request => notApplyingForProbateKickoutView(Seq(
        messages("page.iht.registration.notApplyingForProbate.kickout.p1", deceasedName),
        messages("iht.ifYouWantToChangeYourAnswer")
      ))(request, messages, deceasedName)))

  def onPageLoad = authorisedForIht {
    implicit request => {
      withRegistrationDetails { regDetails =>
        cachingConnector.getSingleValue(RegistrationKickoutReasonCachingKey) map { reason => {
          val messages = Messages.Implicits.applicationMessages
          val deceasedName = DeceasedInfoHelper.getDeceasedNameOrDefaultString(regDetails)
          Ok(content(messages, deceasedName)(CommonHelper.getOrException(reason))(request))
        }
        }
      }
    }
  }

  def onSubmit = authorisedForIht {
    implicit request =>
      metrics.kickOutCounter(KickOutSource.REGISTRATION)
      Future.successful(Redirect(IhtProperties.linkRegistrationKickOut))
  }
}

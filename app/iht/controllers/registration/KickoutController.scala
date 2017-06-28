/*
 * Copyright 2017 HM Revenue & Customs
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

import javax.inject.{Inject, Singleton}

import iht.constants.IhtProperties
import iht.metrics.Metrics
import iht.models.enums.KickOutSource
import iht.utils.CommonHelper
import iht.utils.RegistrationKickOutHelper._
import iht.views.html.registration.kickout._
import play.api.i18n.{Messages, MessagesApi}
import play.api.mvc.Request
import play.twirl.api.HtmlFormat

import scala.concurrent.Future

@Singleton
class KickoutController @Inject()(val metrics: Metrics, val messagesApi: MessagesApi) extends RegistrationController {
  override def guardConditions: Set[Predicate] = Set.empty

  def probateKickoutView(contentLines: Seq[String])(implicit request: Request[_], messages:Messages) =
    kickout_template(messages("page.iht.registration.applicantDetails.kickout.probate.summary"),
    iht.controllers.registration.applicant.routes.ProbateLocationController.onPageLoad())(contentLines)(request, messagesApi.preferred(request))

  def locationKickoutView(contentLines: Seq[String])(implicit request: Request[_], messages:Messages) =
    kickout_template(messages("page.iht.registration.deceasedDetails.kickout.location.summary"),
    iht.controllers.registration.deceased.routes.DeceasedPermanentHomeController.onPageLoad())(contentLines)(request, messagesApi.preferred(request))

  def capitalTaxKickoutView(contentLines: Seq[String])(implicit request: Request[_], messages:Messages) =
    kickout_template(messages("page.iht.registration.deceasedDateOfDeath.kickout.date.capital.tax.summary"),
    iht.controllers.registration.deceased.routes.DeceasedDateOfDeathController.onPageLoad(),
      messages("iht.registration.kickout.returnToTheDateOfDeath"))(contentLines)(request, messagesApi.preferred(request))

  def dateOtherKickoutView(contentLines: Seq[String])(implicit request: Request[_], messages:Messages) =
    kickout_template(messages("page.iht.registration.deceasedDateOfDeath.kickout.date.other.summary"),
    iht.controllers.registration.deceased.routes.DeceasedDateOfDeathController.onPageLoad())(contentLines)(request, messagesApi.preferred(request))

  def notApplyingForProbateKickoutView(contentLines: Seq[String])(implicit request: Request[_], messages:Messages, deceasedName: String) =
    kickout_template(messages("page.iht.registration.notApplyingForProbate.kickout.summary", deceasedName),
    iht.controllers.registration.applicant.routes.ApplyingForProbateController.onPageLoad())(contentLines)(request, messagesApi.preferred(request))

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
    implicit user => implicit request => {
      withRegistrationDetails { regDetails =>
        cachingConnector.getSingleValue(RegistrationKickoutReasonCachingKey) map { reason => {
          val deceasedName = CommonHelper.getDeceasedNameOrDefaultString(regDetails)
          Ok(content(messagesApi.preferred(request), deceasedName)(CommonHelper.getOrException(reason))(request))
        }
        }
      }
    }
  }

  def onSubmit = authorisedForIht {
    implicit user => implicit request =>
      metrics.kickOutCounter(KickOutSource.REGISTRATION)
      Future.successful(Redirect(ihtProperties.linkRegistrationKickOut))
  }
}

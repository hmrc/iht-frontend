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

import iht.connector.CachingConnector
import iht.constants.IhtProperties
import iht.controllers.IhtConnectors
import iht.metrics.Metrics
import iht.models.enums.KickOutSource
import iht.utils.CommonHelper
import iht.utils.RegistrationKickOutHelper._
import iht.views.html.registration.kickout._
import play.api.i18n.Messages
import play.api.mvc.Request
import play.twirl.api.HtmlFormat
import play.api.i18n.Messages.Implicits._
import play.api.Play.current

import scala.concurrent.Future

object KickoutController extends KickoutController with IhtConnectors {
  def metrics: Metrics = Metrics
}

class KickoutController extends RegistrationController {
  def cachingConnector: CachingConnector

  override def guardConditions: Set[Predicate] = Set.empty

  def metrics: Metrics

  def probateKickoutView(contentLines: Seq[String])(request: Request[_]) =
    kickout_template(Messages("page.iht.registration.applicantDetails.kickout.probate.summary"),
    iht.controllers.registration.applicant.routes.ProbateLocationController.onPageLoad())(contentLines)(request, applicationMessages)

  def locationKickoutView(contentLines: Seq[String])(request: Request[_]) =
    kickout_template(Messages("page.iht.registration.deceasedDetails.kickout.location.summary"),
    iht.controllers.registration.deceased.routes.DeceasedPermanentHomeController.onPageLoad())(contentLines)(request, applicationMessages)

  def capitalTaxKickoutView(contentLines: Seq[String])(request: Request[_]) =
    kickout_template(Messages("page.iht.registration.deceasedDateOfDeath.kickout.date.capital.tax.summary"),
    iht.controllers.registration.deceased.routes.DeceasedDateOfDeathController.onPageLoad())(contentLines)(request, applicationMessages)

  def dateOtherKickoutView(contentLines: Seq[String])(request: Request[_]) =
    kickout_template(Messages("page.iht.registration.deceasedDateOfDeath.kickout.date.other.summary"),
    iht.controllers.registration.deceased.routes.DeceasedDateOfDeathController.onPageLoad())(contentLines)(request, applicationMessages)

  def notApplyingForProbateKickoutView(contentLines: Seq[String])(request: Request[_]) =
    kickout_template(Messages("page.iht.registration.notApplyingForProbate.kickout.summary"),
    iht.controllers.registration.applicant.routes.ApplyingForProbateController.onPageLoad())(contentLines)(request, applicationMessages)

  def content: Map[String, Request[_] => HtmlFormat.Appendable] = Map(
    KickoutApplicantDetailsProbateScotland ->
      (request => probateKickoutView(
        Seq(
          Messages("iht.registration.kickout.probateLocation.scotland"),
          Messages("iht.registration.kickout.ifWantChangeProbateLocation")))(request)),
    KickoutApplicantDetailsProbateNi ->
      (request => probateKickoutView(
        Seq(
          Messages("iht.registration.kickout.content"),
          Messages("iht.registration.kickout.ifWantChangeProbateLocation")))(request)),
    KickoutDeceasedDetailsLocationScotland ->
      (request => locationKickoutView(
        Seq(
          Messages("iht.registration.kickout.probateLocation.scotland"),
          Messages("iht.registration.kickout.ifWantChangeDeceasedLocation")))(request)),
    KickoutDeceasedDetailsLocationNI ->
      (request => locationKickoutView(
        Seq(
          Messages("iht.registration.kickout.content"),
          Messages("iht.registration.kickout.ifWantChangeDeceasedLocation")))(request)),
    KickoutDeceasedDetailsLocationOther ->
      (request => locationKickoutView(
        Seq(
          Messages("iht.registration.kickout.content"),
          Messages("iht.registration.kickout.ifWantChangeDeceasedLocation")))(request)),
    KickoutDeceasedDateOfDeathDateCapitalTax ->
      (request => capitalTaxKickoutView(Seq(
        Messages("iht.registration.kickout.message.phone"),
        Messages("iht.registration.kickout.message.MinDOD"),
        Messages("iht.registration.kickout.message.phone2")
      ))(request)),
    KickoutDeceasedDateOfDeathDateOther ->
      (request => dateOtherKickoutView(Seq(
        Messages("iht.registration.kickout.content"),
        Messages("iht.registration.kickout.message.form2")
      ))(request)),
    KickoutNotApplyingForProbate ->
      (request => notApplyingForProbateKickoutView(Seq(
        Messages("page.iht.registration.notApplyingForProbate.kickout.p1"),
        Messages("iht.ifYouWantToChangeYOurAnswer")
      ))(request)))

  def onPageLoad = authorisedForIht {
    implicit user => implicit request => {
      cachingConnector.getSingleValue(RegistrationKickoutReasonCachingKey) map { reason =>
        Ok(content(CommonHelper.getOrException(reason))(request))
      }
    }
  }

  def onSubmit = authorisedForIht {
    implicit user => implicit request =>
      metrics.kickOutCounter(KickOutSource.REGISTRATION)
      Future.successful(Redirect(IhtProperties.linkRegistrationKickOut))
  }
}

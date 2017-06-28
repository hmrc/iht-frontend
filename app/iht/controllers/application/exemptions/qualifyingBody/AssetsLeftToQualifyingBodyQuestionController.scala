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

package iht.controllers.application.exemptions.qualifyingBody

import javax.inject.{Inject, Singleton}

import iht.constants.IhtProperties
import iht.controllers.application.EstateController
import iht.forms.ApplicationForms
import iht.models.application.ApplicationDetails
import iht.models.application.exemptions._
import iht.utils.CommonHelper
import iht.views.html.application.exemption.qualifyingBody.assets_left_to_qualifying_body_question
import play.api.i18n.MessagesApi
import play.api.mvc.Call

@Singleton
class AssetsLeftToQualifyingBodyQuestionController @Inject()(
                                                              val messagesApi: MessagesApi,
                                                              val ihtProperties: IhtProperties,
                                                              val applicationForms: ApplicationForms) extends EstateController {
  val exemptionsOverviewPage: Call = CommonHelper.addFragmentIdentifier(iht.controllers.application.exemptions.routes.ExemptionsOverviewController.onPageLoad(),
    Some(ihtProperties.ExemptionsOtherID))
  val qualifyingBodyOverviewPage: Call =
    CommonHelper.addFragmentIdentifier(iht.controllers.application.exemptions.qualifyingBody.routes.QualifyingBodiesOverviewController.onPageLoad(),
      Some(ihtProperties.ExemptionsOtherAssetsID))
  val qualifyingBodyDetailsOverviewPage: Call =
    iht.controllers.application.exemptions.qualifyingBody.routes.QualifyingBodyDetailsOverviewController.onPageLoad()

  def onPageLoad = authorisedForIht {
    implicit user => implicit request => {
      estateElementOnPageLoad[BasicExemptionElement](applicationForms.assetsLeftToQualifyingBodyQuestionForm,
        assets_left_to_qualifying_body_question.apply, _.allExemptions.flatMap(_.qualifyingBody))
    }
  }

  val updateApplicationDetails : (ApplicationDetails, Option[String], BasicExemptionElement) => (ApplicationDetails, Option[String]) =
    (appDetails, _, qualifyingBody) => {

      val resetQualifyingBodiesIfApplicable: ApplicationDetails => ApplicationDetails = ad => {
        if (ad.allExemptions.flatMap(_.qualifyingBody.flatMap(_.isSelected)).contains(false)) {
          ad.copy(qualifyingBodies = Nil)
        } else {
          ad
        }
      }

      (resetQualifyingBodiesIfApplicable(appDetails.copy(
        allExemptions = Some(appDetails.allExemptions.fold(
        new AllExemptions(qualifyingBody = Some(qualifyingBody)))
        (_.copy(qualifyingBody = Some(qualifyingBody))))
      )
      ), None)
    }

  def onSubmit = authorisedForIht {
    implicit user => implicit request => {
      estateElementOnSubmitConditionalRedirect[BasicExemptionElement](
        applicationForms.assetsLeftToQualifyingBodyQuestionForm,
        assets_left_to_qualifying_body_question.apply,
        updateApplicationDetails,
        (ad, _) => {
          val qualifyingBodiesOwnedAnswer= CommonHelper.getOrException(ad.allExemptions.flatMap(_.qualifyingBody).flatMap(_.isSelected))
          val preexistingQualifyingBody= ad.qualifyingBodies.nonEmpty

          (qualifyingBodiesOwnedAnswer, preexistingQualifyingBody) match {
            case (false, _) => exemptionsOverviewPage
            case (true, true) => qualifyingBodyOverviewPage
            case (true, false) => qualifyingBodyDetailsOverviewPage
          }
        }
      )
    }
  }

}

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

package iht.controllers.application.exemptions.qualifyingBody

import iht.config.AppConfig
import iht.connector.{CachingConnector, IhtConnector}
import iht.controllers.application.EstateController
import iht.forms.ApplicationForms._
import iht.models.application.ApplicationDetails
import iht.models.application.exemptions._
import iht.utils.CommonHelper
import iht.views.html.application.exemption.qualifyingBody.assets_left_to_qualifying_body_question
import javax.inject.Inject
import play.api.mvc.{Call, MessagesControllerComponents}
import uk.gov.hmrc.auth.core.AuthConnector
import uk.gov.hmrc.auth.core.retrieve.v2.Retrievals.{nino => ninoRetrieval}
import uk.gov.hmrc.play.bootstrap.controller.FrontendController
import uk.gov.hmrc.play.partials.FormPartialRetriever

/**
 * Created by james on 17/08/16.
 */
class AssetsLeftToQualifyingBodyQuestionControllerImpl @Inject()(val ihtConnector: IhtConnector,
                                                                 val cachingConnector: CachingConnector,
                                                                 val authConnector: AuthConnector,
                                                                 val formPartialRetriever: FormPartialRetriever,
                                                                 implicit val appConfig: AppConfig,
                                                                 val cc: MessagesControllerComponents) extends FrontendController(cc) with AssetsLeftToQualifyingBodyQuestionController {

}

trait AssetsLeftToQualifyingBodyQuestionController extends EstateController {


  lazy val exemptionsOverviewPage: Call = CommonHelper.addFragmentIdentifier(
    iht.controllers.application.exemptions.routes.ExemptionsOverviewController.onPageLoad(), Some(appConfig.ExemptionsOtherID))
  lazy val qualifyingBodyOverviewPage: Call =
    CommonHelper.addFragmentIdentifier(
      iht.controllers.application.exemptions.qualifyingBody.routes.QualifyingBodiesOverviewController.onPageLoad(), Some(appConfig.ExemptionsOtherAssetsID))
  lazy val qualifyingBodyDetailsOverviewPage: Call =
    iht.controllers.application.exemptions.qualifyingBody.routes.QualifyingBodyDetailsOverviewController.onPageLoad()

  def onPageLoad = authorisedForIhtWithRetrievals(ninoRetrieval) { userNino =>
    implicit request => {
      estateElementOnPageLoad[BasicExemptionElement](assetsLeftToQualifyingBodyQuestionForm,
        assets_left_to_qualifying_body_question.apply, _.allExemptions.flatMap(_.qualifyingBody), userNino)
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

  def onSubmit = authorisedForIhtWithRetrievals(ninoRetrieval) { userNino =>
    implicit request => {
      estateElementOnSubmitConditionalRedirect[BasicExemptionElement](
        assetsLeftToQualifyingBodyQuestionForm,
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
        },
        userNino
      )
    }
  }

}

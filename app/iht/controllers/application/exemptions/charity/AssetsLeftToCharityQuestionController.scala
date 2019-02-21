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

package iht.controllers.application.exemptions.charity

import iht.connector.{CachingConnector, IhtConnector}
import iht.constants.IhtProperties._
import iht.controllers.application.EstateController
import iht.forms.ApplicationForms._
import iht.models.application.ApplicationDetails
import iht.models.application.exemptions.{AllExemptions, BasicExemptionElement}
import iht.utils.CommonHelper
import iht.views.html.application.exemption.charity.assets_left_to_charity_question
import javax.inject.Inject
import play.api.Play.current
import play.api.i18n.Messages.Implicits._
import uk.gov.hmrc.auth.core.AuthConnector
import uk.gov.hmrc.auth.core.retrieve.v2.Retrievals.{nino => ninoRetrieval}
import uk.gov.hmrc.play.partials.FormPartialRetriever

class AssetsLeftToCharityQuestionControllerImpl @Inject()(val ihtConnector: IhtConnector,
                                                          val cachingConnector: CachingConnector,
                                                          val authConnector: AuthConnector,
                                                          val formPartialRetriever: FormPartialRetriever) extends AssetsLeftToCharityQuestionController {

}

trait AssetsLeftToCharityQuestionController extends EstateController {


  lazy val exemptionsOverviewPage = CommonHelper.addFragmentIdentifier(
    iht.controllers.application.exemptions.routes.ExemptionsOverviewController.onPageLoad(), Some(ExemptionsCharityID))

  lazy val charityOverviewPage = CommonHelper.addFragmentIdentifier(
    iht.controllers.application.exemptions.charity.routes.CharitiesOverviewController.onPageLoad(), Some(ExemptionsCharitiesAssetsID))

  lazy val charityDetailsOverviewPage = CommonHelper.addFragmentIdentifier(
    iht.controllers.application.exemptions.charity.routes.CharityDetailsOverviewController.onPageLoad(), Some(ExemptionsCharitiesAssetsID))

  def onPageLoad = authorisedForIhtWithRetrievals(ninoRetrieval) { userNino =>
    implicit request => {
      estateElementOnPageLoad[BasicExemptionElement](assetsLeftToCharityQuestionForm,
        assets_left_to_charity_question.apply, _.allExemptions.flatMap(_.charity), userNino)
    }
  }

  val updateApplicationDetails: (ApplicationDetails, Option[String],BasicExemptionElement) =>
    (ApplicationDetails, Option[String]) =
    (appDetails, _, charity) => {

      val resetCharitiesIfApplicable: ApplicationDetails => ApplicationDetails = ad => {
        if (ad.allExemptions.flatMap(_.charity.flatMap(_.isSelected)).contains(false)) {
          ad copy (charities = Nil)
        } else {
          ad
        }
      }

      (resetCharitiesIfApplicable(appDetails.copy(
        allExemptions = Some(appDetails.allExemptions.fold
          (new AllExemptions(charity = Some(charity)))
          (_.copy(charity = Some(charity))))
      )
      ),None)
    }

  def onSubmit = authorisedForIhtWithRetrievals(ninoRetrieval) { userNino =>
    implicit request => {
      estateElementOnSubmitConditionalRedirect[BasicExemptionElement](
        assetsLeftToCharityQuestionForm,
        assets_left_to_charity_question.apply,
        updateApplicationDetails,
        (ad, _) => {
          val charitiesOwnedAnswer= CommonHelper.getOrException(ad.allExemptions.flatMap(_.charity).flatMap(_.isSelected))
          val preexistingCharity= ad.charities.nonEmpty

          (charitiesOwnedAnswer, preexistingCharity) match {
            case (false, _) => exemptionsOverviewPage
            case (true, true) => charityOverviewPage
            case (true, false) => charityDetailsOverviewPage
          }
        },
        userNino
      )
    }
  }
}

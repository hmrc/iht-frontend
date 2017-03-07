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

package iht.controllers.application.exemptions.charity

import iht.connector.IhtConnectors
import iht.controllers.application.EstateController
import iht.forms.ApplicationForms._
import iht.metrics.Metrics
import iht.models._
import iht.models.application.ApplicationDetails
import iht.models.application.exemptions.{AllExemptions, BasicExemptionElement}
import iht.utils.CommonHelper
import iht.views.html.application.exemption.charity.assets_left_to_charity_question
import play.api.i18n.Messages.Implicits._
import play.api.Play.current

object AssetsLeftToCharityQuestionController extends AssetsLeftToCharityQuestionController with IhtConnectors {
  def metrics: Metrics = Metrics
}

trait AssetsLeftToCharityQuestionController extends EstateController {
  val exemptionsOverviewPage = iht.controllers.application.exemptions.routes.ExemptionsOverviewController.onPageLoad()
  val charityOverviewPage = iht.controllers.application.exemptions.charity.routes.CharitiesOverviewController.onPageLoad()
  val charityDetailsOverviewPage = iht.controllers.application.exemptions.charity.routes.CharityDetailsOverviewController.onPageLoad()

  def onPageLoad = authorisedForIht {
    implicit user => implicit request => {
      estateElementOnPageLoad[BasicExemptionElement](assetsLeftToCharityQuestionForm,
        assets_left_to_charity_question.apply, _.allExemptions.flatMap(_.charity))
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

  def onSubmit = authorisedForIht {
    implicit user => implicit request => {
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
        }
      )
    }
  }
}

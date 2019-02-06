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

package iht.controllers.application.assets

import iht.config.{AppConfig, FrontendAuthConnector}
import iht.connector.IhtConnectors
import iht.controllers.application.EstateController
import iht.forms.ApplicationForms._
import iht.metrics.Metrics
import iht.models._
import iht.models.application.ApplicationDetails
import iht.models.application.assets._
import iht.models.application.basicElements.BasicEstateElement
import iht.utils.ApplicationKickOutHelper
import iht.views.html.application.asset._
import play.api.i18n.Messages.Implicits._
import play.api.Play.current
import iht.constants.Constants._
import iht.constants.IhtProperties._
import iht.utils.CommonHelper
import javax.inject.Inject
import uk.gov.hmrc.auth.core.AuthConnector
import uk.gov.hmrc.auth.core.PlayAuthConnector
import uk.gov.hmrc.auth.core.retrieve.v2.Retrievals.{nino => ninoRetrieval}

class BusinessInterestsControllerImpl @Inject()() extends BusinessInterestsController with IhtConnectors {
  def metrics : Metrics = Metrics
}

trait BusinessInterestsController extends EstateController {
  override val applicationSection = Some(ApplicationKickOutHelper.ApplicationSectionAssetsBusinessInterests)


  def onPageLoad = authorisedForIhtWithRetrievals(ninoRetrieval) { userNino =>
    implicit request =>
      estateElementOnPageLoad[BasicEstateElement](businessInterestForm, business_interests.apply, _.allAssets.flatMap(_.businessInterest), userNino)
  }

  def onSubmit = authorisedForIhtWithRetrievals(ninoRetrieval) { userNino =>
    implicit request => {
      val updateApplicationDetails: (ApplicationDetails, Option[String], BasicEstateElement) =>
        (ApplicationDetails, Option[String]) =
        (appDetails, _, businessInterest) => {
          val updatedAD = appDetails.copy(allAssets = Some(appDetails.allAssets.fold
            (new AllAssets(action = None, businessInterest = Some(businessInterest)))

          (businessInterest.isOwned match {
            case Some(true) => _.copy(businessInterest = Some(businessInterest))
            case Some(false) => _.copy(businessInterest = Some(businessInterest.copy(value = None)))
            case None => throw new RuntimeException("Not able to retrieve the value of BusinessInterest question")
          })
          ))
          (updatedAD, None)
        }

      estateElementOnSubmit[BasicEstateElement](businessInterestForm,
        business_interests.apply,
        updateApplicationDetails,
        CommonHelper.addFragmentIdentifier(assetsRedirectLocation, Some(AppSectionBusinessInterestID)),
        userNino
      )
    }
  }
}

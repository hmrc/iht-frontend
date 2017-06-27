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

package iht.controllers.application.assets

import javax.inject.{Inject, Singleton}

import iht.connector.IhtConnectors
import iht.constants.IhtProperties._
import iht.controllers.application.EstateController
import iht.forms.ApplicationForms._
import iht.models.application.ApplicationDetails
import iht.models.application.assets._
import iht.models.application.basicElements.BasicEstateElement
import iht.utils.{ApplicationKickOutHelper, CommonHelper}
import iht.views.html.application.asset._
import play.api.i18n.MessagesApi

@Singleton
class BusinessInterestsController @Inject() (val messagesApi: MessagesApi) extends EstateController  with IhtConnectors {
  override val applicationSection = Some(ApplicationKickOutHelper.ApplicationSectionAssetsBusinessInterests)

  def onPageLoad = authorisedForIht {
    implicit user => implicit request =>
      estateElementOnPageLoad[BasicEstateElement](businessInterestForm, business_interests.apply, _.allAssets.flatMap(_.businessInterest))
  }

  def onSubmit = authorisedForIht {
    implicit user => implicit request => {
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
        CommonHelper.addFragmentIdentifier(assetsRedirectLocation, Some(AppSectionBusinessInterestID))
      )
    }
  }
}

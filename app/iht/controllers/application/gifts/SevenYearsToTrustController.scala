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

package iht.controllers.application.gifts

import iht.config.AppConfig
import iht.connector.{CachingConnector, IhtConnector}
import iht.controllers.application.EstateController
import iht.forms.ApplicationForms._
import iht.models.application.ApplicationDetails
import iht.models.application.gifts.AllGifts
import iht.utils.{CommonHelper, ApplicationStatus => AppStatus}
import iht.views.html.application.gift.seven_years_to_trust
import javax.inject.Inject
import play.api.mvc.MessagesControllerComponents
import uk.gov.hmrc.auth.core.AuthConnector
import uk.gov.hmrc.auth.core.retrieve.v2.Retrievals.{nino => ninoRetrieval}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController

class SevenYearsToTrustControllerImpl @Inject()(val ihtConnector: IhtConnector,
                                                val cachingConnector: CachingConnector,
                                                val authConnector: AuthConnector,
                                                val sevenYearsToTrustView: seven_years_to_trust,
implicit val appConfig: AppConfig,
val cc: MessagesControllerComponents) extends FrontendController(cc) with SevenYearsToTrustController {

}

trait SevenYearsToTrustController extends EstateController {

  val sevenYearsToTrustView: seven_years_to_trust
  def onPageLoad = authorisedForIhtWithRetrievals(ninoRetrieval) { userNino =>
    implicit request =>
      estateElementOnPageLoad[AllGifts](giftSevenYearsToTrustForm, sevenYearsToTrustView.apply, _.allGifts, userNino)
  }

  def onSubmit = authorisedForIhtWithRetrievals(ninoRetrieval) { userNino =>
    implicit request => {
      val updateApplicationDetails: (ApplicationDetails, Option[String], AllGifts) =>
        (ApplicationDetails, Option[String]) =
        (appDetails, _, gifts) => {
          val updatedAD = appDetails.copy(status=AppStatus.InProgress, allGifts = Some(appDetails.allGifts.fold
          (new AllGifts(None, None, isToTrust=gifts.isToTrust, None, None))
          (_.copy(isToTrust=gifts.isToTrust))))
          (updatedAD, None)
        }
      estateElementOnSubmit[AllGifts](giftSevenYearsToTrustForm,
        sevenYearsToTrustView.apply,
        updateApplicationDetails,
        CommonHelper.addFragmentIdentifier(iht.controllers.application.gifts.routes.GiftsOverviewController.onPageLoad(), Some(appConfig.GiftsSevenYearsQuestionID2)),
        userNino
      )
    }
  }
}

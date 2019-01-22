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

package iht.controllers.application.gifts

import iht.config.{AppConfig, FrontendAuthConnector}
import iht.connector.IhtConnectors
import iht.constants.IhtProperties._
import iht.controllers.application.EstateController
import iht.forms.ApplicationForms._
import iht.metrics.Metrics
import iht.models.application.ApplicationDetails
import iht.models.application.gifts.AllGifts
import iht.utils.{CommonHelper, ApplicationStatus => AppStatus}
import iht.views.html.application.gift.seven_years_to_trust
import javax.inject.Inject
import play.api.Play.current
import play.api.i18n.Messages.Implicits._
import uk.gov.hmrc.auth.core.AuthConnector
import uk.gov.hmrc.auth.core.PlayAuthConnector
import uk.gov.hmrc.auth.core.retrieve.v2.Retrievals.{nino => ninoRetrieval}

class SevenYearsToTrustControllerImpl @Inject()() extends SevenYearsToTrustController with IhtConnectors {
  def metrics : Metrics = Metrics
}

trait SevenYearsToTrustController extends EstateController {

  def onPageLoad = authorisedForIhtWithRetrievals(ninoRetrieval) { userNino =>
    implicit request =>
      estateElementOnPageLoad[AllGifts](giftSevenYearsToTrustForm, seven_years_to_trust.apply, _.allGifts, userNino)
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
        seven_years_to_trust.apply,
        updateApplicationDetails,
        CommonHelper.addFragmentIdentifier(iht.controllers.application.gifts.routes.GiftsOverviewController.onPageLoad(), Some(GiftsSevenYearsQuestionID2)),
        userNino
      )
    }
  }
}

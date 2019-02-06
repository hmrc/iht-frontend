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
import iht.controllers.application.EstateController
import iht.controllers.ControllerHelper
import iht.forms.ApplicationForms._
import iht.metrics.Metrics
import iht.models.application.ApplicationDetails
import iht.models.application.gifts.AllGifts
import iht.utils.{ApplicationKickOutHelper, ApplicationStatus => AppStatus}
import iht.views.html.application.gift.with_reservation_of_benefit
import play.api.i18n.Messages.Implicits._
import play.api.Play.current
import iht.constants.Constants._
import iht.constants.IhtProperties._
import iht.utils.CommonHelper
import javax.inject.Inject
import uk.gov.hmrc.auth.core.AuthConnector
import uk.gov.hmrc.auth.core.PlayAuthConnector
import uk.gov.hmrc.auth.core.retrieve.v2.Retrievals.{nino => ninoRetrieval}

/**
 *
 * Created by Vineet Tyagi on 14/01/16.
 *
 */
class WithReservationOfBenefitControllerImpl @Inject()() extends WithReservationOfBenefitController with IhtConnectors {
  def metrics : Metrics = Metrics
}

trait WithReservationOfBenefitController extends EstateController{
  override val applicationSection = Some(ApplicationKickOutHelper.ApplicationSectionGiftsWithReservation)


  def onPageLoad = authorisedForIhtWithRetrievals(ninoRetrieval) { userNino =>
    implicit request =>
      estateElementOnPageLoad[AllGifts](giftWithReservationFromBenefitForm, with_reservation_of_benefit.apply, _.allGifts, userNino)
  }

  def onSubmit = authorisedForIhtWithRetrievals(ninoRetrieval) { userNino =>
    implicit request => {
      val updateApplicationDetails: (ApplicationDetails, Option[String], AllGifts) =>
        (ApplicationDetails, Option[String]) =
        (appDetails, _, gifts) => {
          val updatedAD = appDetails.copy(status=AppStatus.InProgress, allGifts = Some(appDetails.allGifts.fold
            (new AllGifts(None, isReservation = gifts.isReservation, None, None, None))
            (_.copy(isReservation = gifts.isReservation))))
          (updatedAD, None)
        }
      estateElementOnSubmit[AllGifts](giftWithReservationFromBenefitForm,
        with_reservation_of_benefit.apply,
        updateApplicationDetails,
        CommonHelper.addFragmentIdentifier(giftsRedirectLocation, Some(GiftsReservationBenefitQuestionID)),
        userNino
      )
    }
  }
}

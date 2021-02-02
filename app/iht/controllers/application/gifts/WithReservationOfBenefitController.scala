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
import iht.utils.{ApplicationKickOutHelper, CommonHelper, ApplicationStatus => AppStatus}
import iht.views.html.application.gift.with_reservation_of_benefit
import javax.inject.Inject
import play.api.mvc.MessagesControllerComponents
import uk.gov.hmrc.auth.core.AuthConnector
import uk.gov.hmrc.auth.core.retrieve.v2.Retrievals.{nino => ninoRetrieval}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController
import uk.gov.hmrc.play.partials.FormPartialRetriever

class WithReservationOfBenefitControllerImpl @Inject()(val ihtConnector: IhtConnector,
                                                       val cachingConnector: CachingConnector,
                                                       val authConnector: AuthConnector,
                                                       val formPartialRetriever: FormPartialRetriever,
                                                       implicit val appConfig: AppConfig,
                                                       val cc: MessagesControllerComponents)
  extends FrontendController(cc) with WithReservationOfBenefitController

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
        CommonHelper.addFragmentIdentifier(giftsRedirectLocation, Some(appConfig.GiftsReservationBenefitQuestionID)),
        userNino
      )
    }
  }
}

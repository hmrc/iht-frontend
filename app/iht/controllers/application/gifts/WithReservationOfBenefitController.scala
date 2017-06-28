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

package iht.controllers.application.gifts

import javax.inject.{Inject, Singleton}

import iht.constants.IhtProperties
import iht.controllers.application.EstateController
import iht.forms.ApplicationForms
import iht.models.application.ApplicationDetails
import iht.models.application.gifts.AllGifts
import iht.utils.{ApplicationKickOutHelper, CommonHelper, ApplicationStatus => AppStatus}
import iht.views.html.application.gift.with_reservation_of_benefit
import play.api.i18n.MessagesApi

/**
 *
 * Created by Vineet Tyagi on 14/01/16.
 *
 */

@Singleton
class WithReservationOfBenefitController @Inject()(
                                                    val messagesApi: MessagesApi,
                                                    applicationForms:ApplicationForms,
                                                    ihtProperties: IhtProperties
                                                  ) extends EstateController{
  override val applicationSection = Some(ApplicationKickOutHelper.ApplicationSectionGiftsWithReservation)

  def onPageLoad = authorisedForIht {
    implicit user => implicit request =>
      estateElementOnPageLoad[AllGifts](applicationForms.giftWithReservationFromBenefitForm, with_reservation_of_benefit.apply, _.allGifts)
  }

  def onSubmit = authorisedForIht {
    implicit user => implicit request => {
      val updateApplicationDetails: (ApplicationDetails, Option[String], AllGifts) =>
        (ApplicationDetails, Option[String]) =
        (appDetails, _, gifts) => {
          val updatedAD = appDetails.copy(status=AppStatus.InProgress, allGifts = Some(appDetails.allGifts.fold
            (new AllGifts(None, isReservation = gifts.isReservation, None, None, None))
            (_.copy(isReservation = gifts.isReservation))))
          (updatedAD, None)
        }
      estateElementOnSubmit[AllGifts](applicationForms.giftWithReservationFromBenefitForm,
        with_reservation_of_benefit.apply,
        updateApplicationDetails,
        CommonHelper.addFragmentIdentifier(giftsRedirectLocation, Some(ihtProperties.GiftsReservationBenefitQuestionID))
      )
    }
  }
}

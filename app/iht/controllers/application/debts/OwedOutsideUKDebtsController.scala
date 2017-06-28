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

package iht.controllers.application.debts

import javax.inject.Inject

import iht.constants.IhtProperties
import iht.controllers.application.EstateController
import iht.forms.ApplicationForms
import iht.models.application.ApplicationDetails
import iht.models.application.debts.{AllLiabilities, BasicEstateElementLiabilities}
import iht.utils.CommonHelper
import iht.views.html.application.debts.owed_outside_uk
import play.api.i18n.MessagesApi

class OwedOutsideUKDebtsController @Inject()(val messagesApi: MessagesApi, val ihtProperties: IhtProperties, val applicationForms: ApplicationForms) extends EstateController {
  def onPageLoad = authorisedForIht {
    implicit user => implicit request =>
      estateElementOnPageLoad[BasicEstateElementLiabilities](debtsOutsideUkForm,
        owed_outside_uk.apply, _.allLiabilities.flatMap(_.debtsOutsideUk))
  }

  def onSubmit = authorisedForIht {
    implicit user => implicit request => {
      val updateApplicationDetails: (ApplicationDetails, Option[String], BasicEstateElementLiabilities) =>
        (ApplicationDetails, Option[String]) =
        (appDetails, _, debtsOutsideUk) => {
          val updatedAD = appDetails.copy(allLiabilities = Some(appDetails.allLiabilities.fold
            (new AllLiabilities(debtsOutsideUk = Some(debtsOutsideUk)))

          (debtsOutsideUk.isOwned match {
            case Some(true) => _.copy(debtsOutsideUk = Some(debtsOutsideUk))
            case Some(false) => _.copy(debtsOutsideUk = Some(debtsOutsideUk.copy(value = None)))
            case None => throw new RuntimeException("Not able to retrieve the value of DebtsOutsideUk question")
          })
          ))
          (updatedAD, None)
        }
      estateElementOnSubmit[BasicEstateElementLiabilities](
        debtsOutsideUkForm,
        owed_outside_uk.apply,
        updateApplicationDetails,
        CommonHelper.addFragmentIdentifier(debtsRedirectLocation, Some(ihtProperties.DebtsOwedOutsideUKID))
      )
    }
  }
}

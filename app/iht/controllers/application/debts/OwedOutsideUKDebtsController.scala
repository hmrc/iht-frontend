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

import iht.controllers.IhtConnectors
import iht.controllers.application.EstateController
import iht.forms.ApplicationForms._
import iht.metrics.Metrics
import iht.models.application.ApplicationDetails
import iht.models.application.debts.{AllLiabilities, BasicEstateElementLiabilities}
import iht.views.html.application.debts.owed_outside_uk
import iht.constants.Constants._

object OwedOutsideUKDebtsController extends OwedOutsideUKDebtsController with IhtConnectors {
  def metrics : Metrics = Metrics
}

trait OwedOutsideUKDebtsController extends EstateController {
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
        addFragmentIdentifier(debtsRedirectLocation, Some(DebtsOwedOutsideUKID))
      )
    }
  }
}

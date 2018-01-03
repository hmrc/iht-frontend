/*
 * Copyright 2018 HM Revenue & Customs
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

import iht.connector.IhtConnectors
import iht.controllers.application.EstateController
import iht.forms.ApplicationForms._
import iht.metrics.Metrics
import iht.models._
import iht.models.application.ApplicationDetails
import iht.models.application.debts._
import iht.utils.{ApplicationStatus => AppStatus}
import iht.views.html.application.debts.any_other_debts
import iht.constants.Constants._
import play.api.i18n.Messages.Implicits._
import play.api.Play.current
import iht.constants.IhtProperties._
import iht.utils.CommonHelper

object AnyOtherDebtsController extends AnyOtherDebtsController with IhtConnectors {
  def metrics : Metrics = Metrics
}

trait AnyOtherDebtsController extends EstateController {
  def onPageLoad = authorisedForIht {
    implicit user => implicit request =>
      estateElementOnPageLoad[BasicEstateElementLiabilities](anyOtherDebtsForm,
        any_other_debts.apply, _.allLiabilities.flatMap(_.other))
  }

  def onSubmit = authorisedForIht {
    implicit user => implicit request => {
      val updateApplicationDetails: (ApplicationDetails, Option[String], BasicEstateElementLiabilities) =>
        (ApplicationDetails, Option[String]) =
        (appDetails, _, anyOtherDebts) => {
          val updatedAD = appDetails.copy(allLiabilities = Some(appDetails.allLiabilities.fold
            (new AllLiabilities(other = Some(anyOtherDebts)))

          (anyOtherDebts.isOwned match {
            case Some(true) => _.copy(other = Some(anyOtherDebts))
            case Some(false) => _.copy(other = Some(anyOtherDebts.copy(value = None)))
            case None => throw new RuntimeException("Not able to retrieve the value of AnyOtherDebts question")
          })
          ))
          (updatedAD, None)
        }
      estateElementOnSubmit[BasicEstateElementLiabilities](
        anyOtherDebtsForm,
        any_other_debts.apply,
        updateApplicationDetails,
        CommonHelper.addFragmentIdentifier(debtsRedirectLocation, Some(DebtsOtherID))
      )
    }
  }
}

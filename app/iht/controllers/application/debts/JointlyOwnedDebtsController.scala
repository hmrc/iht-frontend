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
import iht.utils.{ApplicationStatus => AppStatus}
import iht.views.html.application.debts.jointly_owned

object JointlyOwnedDebtsController extends JointlyOwnedDebtsController with IhtConnectors {
  def metrics : Metrics = Metrics
}

trait JointlyOwnedDebtsController extends EstateController {
  def onPageLoad = authorisedForIht {
    implicit user => implicit request =>
      estateElementOnPageLoad[BasicEstateElementLiabilities](jointlyOwnedDebts,
        jointly_owned.apply, _.allLiabilities.flatMap(_.jointlyOwned))
  }

  def onSubmit = authorisedForIht {
    implicit user => implicit request => {
      val updateApplicationDetails: (ApplicationDetails, Option[String], BasicEstateElementLiabilities) =>
        (ApplicationDetails, Option[String]) =
        (appDetails, _, jointlyOwnedDebts) => {
          val updatedAD = appDetails.copy(allLiabilities = Some(appDetails.allLiabilities.fold
            (new AllLiabilities(jointlyOwned = Some(jointlyOwnedDebts)))

          (jointlyOwnedDebts.isOwned match {
            case Some(true) => _.copy(jointlyOwned = Some(jointlyOwnedDebts))
            case Some(false) => _.copy(jointlyOwned = Some(jointlyOwnedDebts.copy(value = None)))
            case None => throw new RuntimeException("Not able to retrieve the value of JointlyOwnedDebts question")
          })
          ))
          (updatedAD, None)
        }
      estateElementOnSubmit[BasicEstateElementLiabilities](
      jointlyOwnedDebts,
      jointly_owned.apply,
      updateApplicationDetails,
      debtsRedirectLocation)
    }
  }
}

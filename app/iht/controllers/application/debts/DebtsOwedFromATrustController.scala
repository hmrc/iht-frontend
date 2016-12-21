/*
 * Copyright 2016 HM Revenue & Customs
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
import iht.models._
import iht.models.application.ApplicationDetails
import iht.models.application.debts._
import iht.views.html.application.debts._

object DebtsOwedFromATrustController extends DebtsOwedFromATrustController with IhtConnectors {
  def metrics : Metrics = Metrics
}

trait DebtsOwedFromATrustController extends EstateController {

  def onPageLoad = authorisedForIht {
    implicit user => implicit request =>
      estateElementOnPageLoad[BasicEstateElementLiabilities](debtsTrustForm,
        debts_trust.apply, _.allLiabilities.flatMap(_.trust))
  }

  def onSubmit = authorisedForIht {
    implicit user => implicit request => {
      val updateApplicationDetails: (ApplicationDetails, Option[String], BasicEstateElementLiabilities) =>
        (ApplicationDetails, Option[String]) =
        (appDetails, _, debtsTrust) => {
         val updatedAD = appDetails.copy(allLiabilities = Some(appDetails.allLiabilities.fold
            (new AllLiabilities(trust = Some(debtsTrust)))

         (debtsTrust.isOwned match {
           case Some(true) => _.copy(trust = Some(debtsTrust))
           case Some(false) => _.copy(trust = Some(debtsTrust.copy(value = None)))
           case None => throw new RuntimeException("Not able to retrieve the value of DebtsInTrust question")
         })
         ))
          (updatedAD, None)
        }

      estateElementOnSubmit[BasicEstateElementLiabilities](debtsTrustForm,
        debts_trust.apply,
        updateApplicationDetails,
        debtsRedirectLocation,
        Some(createValidationFunction("isOwned", _.isDefined, "error.debts.trusts.select"))
      )
    }
  }
}

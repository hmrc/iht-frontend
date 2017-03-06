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

import iht.connector.IhtConnectors
import iht.controllers.application.EstateController
import iht.forms.ApplicationForms._
import iht.metrics.Metrics
import iht.models.application.ApplicationDetails
import iht.models.application.debts.{AllLiabilities, BasicEstateElementLiabilities}
import iht.views.html.application.debts.funeral_expenses
import play.api.i18n.Messages.Implicits._
import play.api.Play.current

object FuneralExpensesController extends FuneralExpensesController with IhtConnectors {
  def metrics : Metrics = Metrics
}

trait FuneralExpensesController extends EstateController {
  def onPageLoad = authorisedForIht {
    implicit user => implicit request =>
      estateElementOnPageLoad[BasicEstateElementLiabilities](funeralExpensesForm,
        funeral_expenses.apply, _.allLiabilities.flatMap(_.funeralExpenses))
  }

  def onSubmit = authorisedForIht {
    implicit user => implicit request => {
      val updateApplicationDetails: (ApplicationDetails, Option[String], BasicEstateElementLiabilities) =>
        (ApplicationDetails, Option[String]) =
        (appDetails, _, funeralExpenses) => {
          val updatedAD = appDetails.copy(allLiabilities = Some(appDetails.allLiabilities.fold
            (new AllLiabilities(funeralExpenses = Some(funeralExpenses)))

          (funeralExpenses.isOwned match {
            case Some(true) => _.copy(funeralExpenses = Some(funeralExpenses))
            case Some(false) => _.copy(funeralExpenses = Some(funeralExpenses.copy(value = None)))
            case None => throw new RuntimeException("Not able to retrieve the value of FuneralExpenses question")
          })
          ))
          (updatedAD, None)
        }
      estateElementOnSubmit[BasicEstateElementLiabilities](
      funeralExpensesForm,
      funeral_expenses.apply,
      updateApplicationDetails,
      debtsRedirectLocation)
    }
  }
}

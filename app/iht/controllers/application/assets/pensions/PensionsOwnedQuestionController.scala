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

package iht.controllers.application.assets.pensions

import iht.connector.IhtConnectors
import iht.controllers.application.EstateController
import iht.forms.ApplicationForms._
import iht.metrics.Metrics
import iht.models._
import iht.models.application.ApplicationDetails
import iht.models.application.assets._
import iht.views.html.application.asset.pensions.pensions_owned_question
import play.api.i18n.Messages.Implicits._
import play.api.Play.current

object PensionsOwnedQuestionController extends PensionsOwnedQuestionController with IhtConnectors {
  def metrics: Metrics = Metrics
}

trait PensionsOwnedQuestionController extends EstateController {
  val submitUrl = iht.controllers.application.assets.pensions.routes.PensionsOverviewController.onPageLoad()

  def onPageLoad = authorisedForIht {
    implicit user => implicit request =>
      estateElementOnPageLoad[PrivatePension](pensionsOwnedQuestionForm, pensions_owned_question.apply, _.allAssets.flatMap(_.privatePension))
  }

  def onSubmit = authorisedForIht {
    implicit user => implicit request => {
      val updateApplicationDetails: (ApplicationDetails, Option[String], PrivatePension) =>
        (ApplicationDetails, Option[String]) =
        (appDetails, _, privatePension) => {

          val existingValue = appDetails.allAssets.flatMap(_.privatePension.flatMap(_.value))
          val existingChanged = appDetails.allAssets.flatMap(_.privatePension.flatMap(_.isChanged))

          val updatedAD = appDetails.copy(allAssets = Some(appDetails.allAssets.fold
            (new AllAssets(action = None, privatePension = Some(privatePension)))
          (privatePension.isOwned match {
            case Some(true) => _.copy(privatePension = Some(privatePension.copy(value = existingValue, isChanged = existingChanged) ))
            case Some(false) => _.copy(privatePension = Some(privatePension))
            case None => throw new RuntimeException
          }
          )))
          (updatedAD, None)
        }

      estateElementOnSubmitConditionalRedirect[PrivatePension](
        pensionsOwnedQuestionForm,
        pensions_owned_question.apply,
        updateApplicationDetails,
        (ad, _) =>  ad.allAssets.flatMap(allAssets=>allAssets.privatePension).flatMap(_.isOwned) match {
          case Some(true) => submitUrl
          case Some(false) => assetsRedirectLocation
          case _ => throw new RuntimeException("Pensions value does not exist")
        }
      )
    }
  }
}

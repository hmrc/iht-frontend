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

package iht.controllers.application.assets

import iht.connector.IhtConnectors
import iht.controllers.application.EstateController
import iht.forms.ApplicationForms._
import iht.metrics.Metrics
import iht.models._
import iht.models.application.ApplicationDetails
import iht.models.application.assets.AllAssets
import iht.models.application.basicElements.BasicEstateElement
import iht.utils.{ApplicationKickOutHelper, ApplicationStatus => AppStatus}
import iht.views.html.application.asset._
import play.api.i18n.Messages.Implicits._
import play.api.Play.current
import iht.constants.Constants._
import iht.constants.IhtProperties._
import iht.utils.CommonHelper

object NominatedController extends NominatedController with IhtConnectors {
  def metrics : Metrics = Metrics
}

trait NominatedController extends EstateController {
  override val applicationSection = Some(ApplicationKickOutHelper.ApplicationSectionAssetsNominatedAssets)
  def onPageLoad = authorisedForIht {
    implicit user => implicit request =>
      estateElementOnPageLoad[BasicEstateElement](nominatedForm, nominated.apply, _.allAssets.flatMap(_.nominated))
  }

  def onSubmit = authorisedForIht {
    implicit user => implicit request => {
      val updateApplicationDetails: (ApplicationDetails, Option[String], BasicEstateElement) =>
        (ApplicationDetails, Option[String]) =
        (appDetails, _, nominated) => {
          val updatedAD = appDetails.copy(allAssets = Some(appDetails.allAssets.fold
            (new AllAssets(action = None, nominated = Some(nominated)))

          (nominated.isOwned match {
            case Some(true) => _.copy(nominated = Some(nominated))
            case Some(false) => _.copy(nominated = Some(nominated.copy(value = None)))
            case None => throw new RuntimeException("Not able to retrieve the value of NominatedAsset question")
          })
          ))
          (updatedAD, None)
        }
      estateElementOnSubmit[BasicEstateElement](nominatedForm,
        nominated.apply,
        updateApplicationDetails,
        CommonHelper.addFragmentIdentifier(assetsRedirectLocation, Some(AppSectionNominatedID))
      )
    }
  }
}

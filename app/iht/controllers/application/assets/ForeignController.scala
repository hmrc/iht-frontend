/*
 * Copyright 2019 HM Revenue & Customs
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

import iht.config.{AppConfig, FrontendAuthConnector}
import iht.connector.IhtConnectors
import iht.constants.IhtProperties._
import iht.controllers.application.EstateController
import iht.forms.ApplicationForms._
import iht.metrics.Metrics
import iht.models.application.ApplicationDetails
import iht.models.application.assets.AllAssets
import iht.models.application.basicElements.BasicEstateElement
import iht.utils.{ApplicationKickOutHelper, CommonHelper}
import iht.views.html.application.asset._
import javax.inject.Inject
import play.api.Play.current
import play.api.i18n.Messages.Implicits._
import uk.gov.hmrc.auth.core.AuthConnector
import uk.gov.hmrc.auth.core.PlayAuthConnector
import uk.gov.hmrc.auth.core.retrieve.v2.Retrievals.{nino => ninoRetrieval}

class ForeignControllerImpl @Inject()() extends ForeignController with IhtConnectors {
  def metrics : Metrics = Metrics
}

trait ForeignController extends EstateController {
  override val applicationSection = Some(ApplicationKickOutHelper.ApplicationSectionAssetsForeign)


  def onPageLoad = authorisedForIhtWithRetrievals(ninoRetrieval) { userNino =>
    implicit request =>
      estateElementOnPageLoad[BasicEstateElement](foreignForm, foreign.apply, _.allAssets.flatMap(_.foreign), userNino)
  }

  def onSubmit = authorisedForIhtWithRetrievals(ninoRetrieval) { userNino =>
    implicit request => {
      val updateApplicationDetails: (ApplicationDetails, Option[String], BasicEstateElement) =>
        (ApplicationDetails, Option[String]) =
        (appDetails, _, foreign) => {
          val updatedAD = appDetails.copy(allAssets = Some(appDetails.allAssets.fold
            (new AllAssets(action = None, foreign = Some(foreign)))

          (foreign.isOwned match {
            case Some(true) => _.copy(foreign = Some(foreign))
            case Some(false) => _.copy(foreign = Some(foreign.copy(value = None)))
            case None => throw new RuntimeException("Not able to retrieve the value of ForeignAssets question")
          })
          ))
          (updatedAD, None)
        }
      estateElementOnSubmit[BasicEstateElement](foreignForm,
        foreign.apply,
        updateApplicationDetails,
        CommonHelper.addFragmentIdentifier(assetsRedirectLocation, Some(AppSectionForeignID)),
        userNino
      )
    }
  }
}

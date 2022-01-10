/*
 * Copyright 2022 HM Revenue & Customs
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

import iht.config.AppConfig
import iht.connector.{CachingConnector, IhtConnector}
import iht.controllers.application.EstateController
import iht.forms.ApplicationForms._
import iht.models.application.ApplicationDetails
import iht.models.application.debts.{AllLiabilities, BasicEstateElementLiabilities}
import iht.utils.CommonHelper
import iht.views.html.application.debts.jointly_owned
import javax.inject.Inject
import play.api.mvc.MessagesControllerComponents
import uk.gov.hmrc.auth.core.AuthConnector
import uk.gov.hmrc.auth.core.retrieve.v2.Retrievals.{nino => ninoRetrieval}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController

class JointlyOwnedDebtsControllerImpl @Inject()(val ihtConnector: IhtConnector,
                                                val cachingConnector: CachingConnector,
                                                val authConnector: AuthConnector,
                                                val jointlyOwnedView: jointly_owned,
implicit val appConfig: AppConfig,
val cc: MessagesControllerComponents) extends FrontendController(cc) with JointlyOwnedDebtsController {

}

trait JointlyOwnedDebtsController extends EstateController {

  val jointlyOwnedView: jointly_owned
  def onPageLoad = authorisedForIhtWithRetrievals(ninoRetrieval) { userNino =>
    implicit request =>
      estateElementOnPageLoad[BasicEstateElementLiabilities](jointlyOwnedDebts,
        jointlyOwnedView.apply, _.allLiabilities.flatMap(_.jointlyOwned), userNino)
  }

  def onSubmit = authorisedForIhtWithRetrievals(ninoRetrieval) { userNino =>
    implicit request => {
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
        jointlyOwnedView.apply,
        updateApplicationDetails,
        CommonHelper.addFragmentIdentifier(debtsRedirectLocation, Some(appConfig.DebtsOwedJointlyID)),
        userNino
      )
    }
  }
}

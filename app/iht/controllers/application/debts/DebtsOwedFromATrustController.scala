/*
 * Copyright 2021 HM Revenue & Customs
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
import iht.models.application.debts._
import iht.utils.CommonHelper
import iht.views.html.application.debts._
import javax.inject.Inject
import play.api.mvc.MessagesControllerComponents
import uk.gov.hmrc.auth.core.AuthConnector
import uk.gov.hmrc.auth.core.retrieve.v2.Retrievals.{nino => ninoRetrieval}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController
import uk.gov.hmrc.play.partials.FormPartialRetriever

class DebtsOwedFromATrustControllerImpl @Inject()(val ihtConnector: IhtConnector,
                                                  val cachingConnector: CachingConnector,
                                                  val authConnector: AuthConnector,
                                                  val formPartialRetriever: FormPartialRetriever,
                                                  implicit val appConfig: AppConfig,
                                                  val cc: MessagesControllerComponents)
  extends FrontendController(cc) with DebtsOwedFromATrustController

trait DebtsOwedFromATrustController extends EstateController {


  def onPageLoad = authorisedForIhtWithRetrievals(ninoRetrieval) { userNino =>
    implicit request =>
      estateElementOnPageLoad[BasicEstateElementLiabilities](debtsTrustForm,
        owed_from_trust.apply, _.allLiabilities.flatMap(_.trust), userNino)
  }

  def onSubmit = authorisedForIhtWithRetrievals(ninoRetrieval) { userNino =>
    implicit request => {
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
        owed_from_trust.apply,
        updateApplicationDetails,
        CommonHelper.addFragmentIdentifier(debtsRedirectLocation, Some(appConfig.DebtsOwedFromTrustID)),
        userNino
      )
    }
  }
}

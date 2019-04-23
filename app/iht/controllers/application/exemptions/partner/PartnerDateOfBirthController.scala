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

package iht.controllers.application.exemptions.partner

import iht.config.AppConfig
import iht.connector.{CachingConnector, IhtConnector}
import iht.controllers.application.EstateController
import iht.forms.ApplicationForms._
import iht.models.RegistrationDetails
import iht.models.application.exemptions._
import iht.utils.CommonHelper._
import iht.views.html.application.exemption.partner.partner_date_of_birth
import javax.inject.Inject
import play.api.mvc.{MessagesControllerComponents, Request, Result}
import uk.gov.hmrc.auth.core.AuthConnector
import uk.gov.hmrc.auth.core.retrieve.v2.Retrievals.{nino => ninoRetrieval}
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.bootstrap.controller.FrontendController
import uk.gov.hmrc.play.partials.FormPartialRetriever

import scala.concurrent.Future

/**
  * Created by james on 01/08/16.
  */
class PartnerDateOfBirthControllerImpl @Inject()(val cachingConnector: CachingConnector,
                                                 val ihtConnector: IhtConnector,
                                                 val authConnector: AuthConnector,
                                                 override implicit val formPartialRetriever: FormPartialRetriever,
implicit val appConfig: AppConfig,
val cc: MessagesControllerComponents) extends FrontendController(cc) with PartnerDateOfBirthController

trait PartnerDateOfBirthController extends EstateController {


  def cachingConnector: CachingConnector

  def ihtConnector: IhtConnector

  def onPageLoad = authorisedForIhtWithRetrievals(ninoRetrieval) { userNino =>
    implicit request =>
      estateElementOnPageLoad[PartnerExemption](spouseDateOfBirthForm, partner_date_of_birth.apply, _.allExemptions.flatMap(_.partner), userNino)
  }

  def onSubmit = authorisedForIhtWithRetrievals(ninoRetrieval) { userNino =>
    implicit request => {

      withRegistrationDetails { regDetails =>
        val boundForm = spouseDateOfBirthForm.bindFromRequest

        boundForm.fold(
          formWithErrors =>
            Future.successful(Ok(iht.views.html.application.exemption.partner.partner_date_of_birth(formWithErrors, regDetails))),
          pe => saveApplication(getNino(userNino), pe, regDetails)
        )
      }
    }
  }

  def saveApplication(nino: String, pe: PartnerExemption, regDetails: RegistrationDetails)(implicit request: Request[_],
                                                                                           hc: HeaderCarrier): Future[Result] = {
    withApplicationDetails(Some(nino)) { rd =>
      ad =>
        lazy val existingOptionPartnerExemption = ad.allExemptions.flatMap(_.partner)
        val updatedAllExemptions = ad.allExemptions.fold(new AllExemptions(partner = Some(pe)))(
          _ copy (partner = existingOptionPartnerExemption.map(_ copy (dateOfBirth = pe.dateOfBirth))))
        val copyOfAD = ad copy (allExemptions = Some(updatedAllExemptions))
        val applicationDetails = appKickoutUpdateKickout(
          checks = checksEstate,
          prioritySection = applicationSection,
          registrationDetails = regDetails,
          applicationDetails = copyOfAD)
      ihtConnector.saveApplication(nino, applicationDetails, regDetails.acknowledgmentReference).flatMap { _ =>
        Future.successful(Redirect(applicationDetails.kickoutReason.fold(
          addFragmentIdentifier(routes.PartnerOverviewController.onPageLoad(), Some(appConfig.ExemptionsPartnerDobID))
        )(_ => kickoutRedirectLocation)))
        }
    }
  }
}

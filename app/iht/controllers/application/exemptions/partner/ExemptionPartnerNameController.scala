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
import iht.utils.ApplicationKickOutHelper
import iht.utils.CommonHelper._
import iht.views.html.application.exemption.partner.partner_name
import javax.inject.Inject
import play.api.mvc.{MessagesControllerComponents, Request, Result}
import uk.gov.hmrc.auth.core.AuthConnector
import uk.gov.hmrc.auth.core.retrieve.v2.Retrievals.{nino => ninoRetrieval}
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.bootstrap.controller.FrontendController
import uk.gov.hmrc.play.partials.FormPartialRetriever

import scala.concurrent.Future

/**
  * Created by jennygj on 01/08/16.
  */
class ExemptionPartnerNameControllerImpl @Inject()(val cachingConnector: CachingConnector,
                                                   val ihtConnector: IhtConnector,
                                                   val authConnector: AuthConnector,
                                                   override implicit val formPartialRetriever: FormPartialRetriever,
implicit val appConfig: AppConfig,
val cc: MessagesControllerComponents) extends FrontendController(cc) with ExemptionPartnerNameController

trait ExemptionPartnerNameController extends EstateController {
  override val applicationSection = Some(ApplicationKickOutHelper.ApplicationSectionExemptionsSpouse)


  def cachingConnector: CachingConnector

  def ihtConnector: IhtConnector

  def onPageLoad = authorisedForIhtWithRetrievals(ninoRetrieval) { userNino =>
    implicit request => {
      estateElementOnPageLoad[PartnerExemption](
        partnerExemptionNameForm, partner_name.apply, _.allExemptions.flatMap(_.partner), userNino)
    }
  }

  def onSubmit = authorisedForIhtWithRetrievals(ninoRetrieval) { userNino =>
    implicit request => {
      withRegistrationDetails { regDetails =>
        val boundForm = partnerExemptionNameForm.bindFromRequest

        boundForm.fold(
          formWithErrors => {
            Future.successful(BadRequest(iht.views.html.application.exemption.partner.partner_name(
              formWithErrors, regDetails)))
          },
          partnerExemption => {
            saveApplication(getNino(userNino), partnerExemption, regDetails, userNino)
          }
        )
      }
    }
  }

  def saveApplication(nino: String, pe: PartnerExemption, regDetails: RegistrationDetails, userNino: Option[String])
                     (implicit request: Request[_], hc: HeaderCarrier): Future[Result] = {

    withApplicationDetails(userNino) {
      rd =>
        appDetails =>

          val existingPartnerExemptions = appDetails.allExemptions.flatMap(_.partner).getOrElse(
            new PartnerExemption(None, None, None, None, None, None, None))

          val appDetailsCopy = appDetails.allExemptions.fold(
            new AllExemptions(partner = Some(pe)))(_.copy(Some(existingPartnerExemptions.copy(
            firstName = pe.firstName, lastName = pe.lastName))))

          val applicationDetails = appKickoutUpdateKickout(
            checks = checksEstate,
            prioritySection = applicationSection,
            registrationDetails = regDetails,
            applicationDetails = appDetails.copy(allExemptions = Some(appDetailsCopy)))

          ihtConnector.saveApplication(nino, applicationDetails, regDetails.acknowledgmentReference).map { _ =>
            Redirect(applicationDetails.kickoutReason.fold(
              addFragmentIdentifier(routes.PartnerOverviewController.onPageLoad(), Some(appConfig.ExemptionsPartnerNameID))
            )(_ => kickoutRedirectLocation))
          }
    }
  }
}

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

package iht.controllers.application.exemptions.partner

import javax.inject.{Inject, Singleton}

import iht.connector.{CachingConnector, IhtConnector}
import iht.constants.IhtProperties._
import iht.controllers.application.EstateController
import iht.forms.ApplicationForms._
import iht.models.RegistrationDetails
import iht.models.application.exemptions._
import iht.utils.CommonHelper._
import iht.utils.{ApplicationKickOutHelper, CommonHelper}
import iht.views.html.application.exemption.partner.partner_name
import play.api.i18n.MessagesApi
import play.api.mvc.{Request, Result}
import uk.gov.hmrc.play.frontend.auth.AuthContext
import uk.gov.hmrc.play.http.HeaderCarrier

import scala.concurrent.Future

@Singleton
class ExemptionPartnerNameController @Inject()(val messagesApi: MessagesApi) extends EstateController {
  override val applicationSection = Some(ApplicationKickOutHelper.ApplicationSectionExemptionsSpouse)

  def onPageLoad = authorisedForIht {
    implicit user =>
      implicit request => {
        estateElementOnPageLoad[PartnerExemption](
          partnerExemptionNameForm, partner_name.apply, _.allExemptions.flatMap(_.partner))
      }
  }

  def onSubmit = authorisedForIht {
    implicit user =>
      implicit request => {
        withRegistrationDetails { regDetails =>
          val boundForm = partnerExemptionNameForm.bindFromRequest

          boundForm.fold(
            formWithErrors => {
              Future.successful(BadRequest(iht.views.html.application.exemption.partner.partner_name(
                formWithErrors, regDetails)))
            },
            partnerExemption => {
              saveApplication(CommonHelper.getNino(user), partnerExemption, regDetails)
            }
          )
        }
      }
  }

  def saveApplication(nino: String, pe: PartnerExemption, regDetails: RegistrationDetails)
                     (implicit request: Request[_], user: AuthContext, hc: HeaderCarrier): Future[Result] = {

    withApplicationDetails {
      rd =>
        appDetails =>

          val existingPartnerExemptions = appDetails.allExemptions.flatMap(_.partner).getOrElse(
            new PartnerExemption(None, None, None, None, None, None, None))

          val appDetailsCopy = appDetails.allExemptions.fold(
            new AllExemptions(partner = Some(pe)))(_.copy(Some(existingPartnerExemptions.copy(
            firstName = pe.firstName, lastName = pe.lastName))))

          val applicationDetails = ApplicationKickOutHelper.updateKickout(
            checks = ApplicationKickOutHelper.checksEstate,
            prioritySection = applicationSection,
            registrationDetails = regDetails,
            applicationDetails = appDetails.copy(allExemptions = Some(appDetailsCopy)))

          ihtConnector.saveApplication(nino, applicationDetails, regDetails.acknowledgmentReference).map { _ =>
            Redirect(applicationDetails.kickoutReason.fold(
              addFragmentIdentifier(routes.PartnerOverviewController.onPageLoad(), Some(ExemptionsPartnerNameID))
            )(_ => kickoutRedirectLocation))
          }
    }
  }
}

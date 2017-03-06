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

import iht.connector.{CachingConnector, IhtConnector}
import iht.connector.IhtConnectors
import iht.controllers.application.EstateController
import iht.forms.ApplicationForms._
import iht.models.RegistrationDetails
import iht.models.application.exemptions._
import iht.utils.{ApplicationKickOutHelper, CommonHelper}
import iht.views.html.application.exemption.partner.partner_date_of_birth
import play.api.mvc.{Request, Result}
import uk.gov.hmrc.play.frontend.auth.AuthContext
import uk.gov.hmrc.play.http.HeaderCarrier
import play.api.i18n.Messages.Implicits._
import play.api.Play.current
import scala.concurrent.Future

/**
 * Created by james on 01/08/16.
 */
object PartnerDateOfBirthController extends PartnerDateOfBirthController with IhtConnectors

trait PartnerDateOfBirthController extends EstateController {

  def cachingConnector: CachingConnector

  def ihtConnector: IhtConnector

  def onPageLoad = authorisedForIht {
    implicit user => implicit request =>
      estateElementOnPageLoad[PartnerExemption](spouseDateOfBirthForm, partner_date_of_birth.apply, _.allExemptions.flatMap(_.partner))
  }

  def onSubmit = authorisedForIht {
    implicit user => implicit request => {

      val regDetails: RegistrationDetails = cachingConnector.getExistingRegistrationDetails
      val boundForm = spouseDateOfBirthForm.bindFromRequest

      boundForm.fold(
        formWithErrors =>
          Future.successful(Ok(iht.views.html.application.exemption.partner.partner_date_of_birth(formWithErrors, regDetails))),
        pe => saveApplication(CommonHelper.getNino(user), pe, regDetails)
      )
    }
  }

  def saveApplication(nino: String, pe: PartnerExemption, regDetails: RegistrationDetails)(implicit request: Request[_],
                                                                                           hc: HeaderCarrier,
                                                                                           authContext: AuthContext): Future[Result] = {
    withApplicationDetails { rd => ad =>
      lazy val existingOptionPartnerExemption = ad.allExemptions.flatMap(_.partner)
      val updatedAllExemptions = ad.allExemptions.fold(new AllExemptions(partner = Some(pe)))(
        _ copy (partner = existingOptionPartnerExemption.map(_ copy (dateOfBirth = pe.dateOfBirth))))
      val copyOfAD = ad copy (allExemptions = Some(updatedAllExemptions))
      val applicationDetails = ApplicationKickOutHelper.updateKickout(
        checks = ApplicationKickOutHelper.checksEstate,
        prioritySection = applicationSection,
        registrationDetails = regDetails,
        applicationDetails = copyOfAD)
      ihtConnector.saveApplication(nino, applicationDetails, regDetails.acknowledgmentReference).flatMap { _ =>
        Future.successful(Redirect(applicationDetails.kickoutReason.fold(routes.PartnerOverviewController.onPageLoad())(_ => kickoutRedirectLocation)))
      }
    }
  }
}

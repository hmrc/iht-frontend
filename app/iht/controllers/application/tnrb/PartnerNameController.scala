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

package iht.controllers.application.tnrb

import javax.inject.{Inject, Singleton}

import iht.constants.IhtProperties
import iht.controllers.application.EstateController
import iht.forms.TnrbForms._
import iht.models.RegistrationDetails
import iht.models.application.ApplicationDetails
import iht.models.application.tnrb.TnrbEligibiltyModel
import iht.utils._
import iht.utils.tnrb.TnrbHelper
import play.api.i18n.MessagesApi
import play.api.mvc.{Request, Result}
import uk.gov.hmrc.play.http.HeaderCarrier

import scala.concurrent.Future

@Singleton
class PartnerNameController @Inject() (implicit val messagesApi: MessagesApi, val ihtProperties: IhtProperties) extends EstateController {
  override val applicationSection = Some(ApplicationKickOutHelper.ApplicationSectionGiftsWithReservation)
  val cancelUrl = iht.controllers.application.tnrb.routes.TnrbOverviewController.onPageLoad()

  def onPageLoad = authorisedForIht {
    implicit user =>
      implicit request => {
        withRegistrationDetails { registrationDetails =>
          for {
            applicationDetails <- ihtConnector.getApplication(CommonHelper.getNino(user),
              CommonHelper.getOrExceptionNoIHTRef(registrationDetails.ihtReference),
              registrationDetails.acknowledgmentReference)
          } yield {
            applicationDetails match {
              case Some(appDetails) => {

                val filledForm = partnerNameForm.fill(appDetails.increaseIhtThreshold.getOrElse(
                  TnrbEligibiltyModel(None, None, None, None, None, None, None, None, None, None, None)))

                Ok(iht.views.html.application.tnrb.partner_name(
                  filledForm,
                  CommonHelper.getOrException(appDetails.widowCheck).dateOfPreDeceased,
                  CommonHelper.addFragmentIdentifier(cancelUrl, Some(ihtProperties.TnrbSpouseNameID))
                )
                )
              }
              case _ => InternalServerError("Application details not found")
            }
          }
        }
      }
  }

  def onSubmit = authorisedForIht {
    implicit user =>
      implicit request => {
        withRegistrationDetails { regDetails =>
          val applicationDetailsFuture = ihtConnector.getApplication(CommonHelper.getNino(user),
            CommonHelper.getOrExceptionNoIHTRef(regDetails.ihtReference),
            regDetails.acknowledgmentReference)

          val boundForm = partnerNameForm.bindFromRequest

          applicationDetailsFuture.flatMap {
            case Some(appDetails) => {
              boundForm.fold(
                formWithErrors => {
                  Future.successful(BadRequest(iht.views.html.application.tnrb.partner_name(formWithErrors,
                    CommonHelper.getOrException(appDetails.widowCheck).dateOfPreDeceased, cancelUrl)))
                },
                tnrbModel => {
                  saveApplication(CommonHelper.getNino(user), tnrbModel, appDetails, regDetails)
                }
              )
            }
            case _ => Future.successful(InternalServerError("Application details not found"))
          }
        }
      }
  }

  private def saveApplication(nino: String,
                              tnrbModel: TnrbEligibiltyModel,
                              appDetails: ApplicationDetails,
                              regDetails: RegistrationDetails)(implicit request: Request[_],
                                                               hc: HeaderCarrier): Future[Result] = {

    val updatedAppDetails = appDetails.copy(increaseIhtThreshold = Some(appDetails.increaseIhtThreshold.
      fold(new TnrbEligibiltyModel(None, None, None, None, None, None, None,
        firstName = tnrbModel.firstName, lastName = tnrbModel.lastName, None, None))
      (_.copy(firstName = tnrbModel.firstName, lastName = tnrbModel.lastName))))

    ihtConnector.saveApplication(nino, updatedAppDetails, regDetails.acknowledgmentReference) map (_ =>
      TnrbHelper.successfulTnrbRedirect(updatedAppDetails, Some(ihtProperties.TnrbSpouseNameID)))
  }
}

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

import iht.connector.IhtConnectors
import iht.controllers.application.EstateController
import iht.forms.TnrbForms._
import iht.metrics.Metrics
import iht.models.application.ApplicationDetails
import iht.models.application.tnrb.{TnrbEligibiltyModel, WidowCheck}
import iht.models.RegistrationDetails
import iht.utils.tnrb.TnrbHelper
import iht.utils.{ApplicationKickOutHelper, CommonHelper, IhtFormValidator}
import play.api.Logger
import play.api.i18n.MessagesApi
import play.api.mvc.{Request, Result}
import uk.gov.hmrc.play.http.HeaderCarrier
import iht.constants.Constants._
import iht.constants.IhtProperties._

import scala.concurrent.Future

@Singleton
class GiftsMadeBeforeDeathController @Inject() (implicit val messagesApi: MessagesApi) extends EstateController {
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

                val filledForm = giftMadeBeforeDeathForm.fill(appDetails.increaseIhtThreshold.getOrElse(
                  TnrbEligibiltyModel(None, None, None, None, None, None, None, None, None, None, None)))

                Ok(iht.views.html.application.tnrb.gifts_made_before_death(
                  filledForm,
                  appDetails.increaseIhtThreshold.fold(TnrbEligibiltyModel(None, None, None, None, None, None, None, None, None, None, None))(identity),
                  appDetails.widowCheck.fold(WidowCheck(None, None))(identity),
                  CommonHelper.addFragmentIdentifier(cancelUrl, Some(TnrbGiftsGivenAwayID)),
                  registrationDetails
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

          val boundForm = IhtFormValidator.addDeceasedNameToAllFormErrors(giftMadeBeforeDeathForm
            .bindFromRequest, regDetails.deceasedDetails.fold("")(_.name))

          applicationDetailsFuture.flatMap {
            case Some(appDetails) => {
              boundForm.fold(
                formWithErrors => {

                  Future.successful(BadRequest(iht.views.html.application.tnrb.gifts_made_before_death(formWithErrors,
                    appDetails.increaseIhtThreshold.fold(TnrbEligibiltyModel(None, None, None, None, None, None, None, None, None, None, None))(identity),
                    appDetails.widowCheck.fold(WidowCheck(None, None))(identity),
                    cancelUrl, regDetails
                  )))
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
      fold(new TnrbEligibiltyModel(None, isGiftMadeBeforeDeath = tnrbModel.isGiftMadeBeforeDeath, None, None, None, None, None, None,
        None, None, None))(_.copy(isGiftMadeBeforeDeath = tnrbModel.isGiftMadeBeforeDeath))))

    val updatedAppDetailsWithKickOutReason = ApplicationKickOutHelper.updateKickout(checks = ApplicationKickOutHelper.checksTnrbEligibility,
      registrationDetails = regDetails,
      applicationDetails = updatedAppDetails)

    for {
      savedApplicationDetails <- ihtConnector.saveApplication(nino, updatedAppDetailsWithKickOutReason, regDetails.acknowledgmentReference)
    } yield {
      savedApplicationDetails.fold[Result] {
        Logger.warn("Problem storing Application details. Redirecting to InternalServerError")
        InternalServerError
      } { _ =>
        updatedAppDetailsWithKickOutReason.kickoutReason match {
          case Some(reason) => Redirect(iht.controllers.application.routes.KickoutController.onPageLoad())
          case _ => TnrbHelper.successfulTnrbRedirect(updatedAppDetailsWithKickOutReason, Some(TnrbGiftsGivenAwayID))
        }
      }
    }
  }
}

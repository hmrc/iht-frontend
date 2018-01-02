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

package iht.controllers.application.tnrb

import iht.connector.IhtConnectors
import iht.constants.IhtProperties._
import iht.controllers.application.EstateController
import iht.forms.TnrbForms._
import iht.metrics.Metrics
import iht.models.RegistrationDetails
import iht.models.application.ApplicationDetails
import iht.models.application.tnrb.TnrbEligibiltyModel
import iht.utils.tnrb.TnrbHelper
import iht.utils.{ApplicationKickOutNonSummaryHelper, ApplicationKickOutHelper, CommonHelper, IhtFormValidator, StringHelper}
import play.api.Logger
import play.api.Play.current
import play.api.i18n.Messages.Implicits._
import play.api.mvc.{Request, Result}

import scala.concurrent.Future
import uk.gov.hmrc.http.HeaderCarrier

object GiftsWithReservationOfBenefitController extends GiftsWithReservationOfBenefitController with IhtConnectors {
  def metrics: Metrics = Metrics
}

trait GiftsWithReservationOfBenefitController extends EstateController {
  override val applicationSection = Some(ApplicationKickOutHelper.ApplicationSectionGiftsWithReservation)
  val cancelUrl = iht.controllers.application.tnrb.routes.TnrbOverviewController.onPageLoad()

  def onPageLoad = authorisedForIht {
    implicit user =>
      implicit request => {
        withRegistrationDetails { registrationDetails =>
          val deceasedName = CommonHelper.getOrException(registrationDetails.deceasedDetails).name

          for {
            applicationDetails <- ihtConnector.getApplication(StringHelper.getNino(user),
              CommonHelper.getOrExceptionNoIHTRef(registrationDetails.ihtReference),
              registrationDetails.acknowledgmentReference)
          } yield {
            applicationDetails match {
              case Some(appDetails) => {

                val tnrbModel = appDetails.increaseIhtThreshold.getOrElse(
                  TnrbEligibiltyModel(None, None, None, None, None, None, None, None, None, None, None))

                val filledForm = partnerGiftWithResToOtherForm.fill(tnrbModel)

                Ok(iht.views.html.application.tnrb.gifts_with_reservation_of_benefit(
                  filledForm,
                  tnrbModel,
                  deceasedName,
                  CommonHelper.addFragmentIdentifier(cancelUrl, Some(TnrbGiftsWithReservationID))
                ))
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
          val deceasedName = CommonHelper.getOrException(regDetails.deceasedDetails).name

          val applicationDetailsFuture = ihtConnector.getApplication(StringHelper.getNino(user),
            CommonHelper.getOrExceptionNoIHTRef(regDetails.ihtReference),
            regDetails.acknowledgmentReference)

          val boundForm = IhtFormValidator.addDeceasedNameToAllFormErrors(partnerGiftWithResToOtherForm
            .bindFromRequest, regDetails.deceasedDetails.fold("")(_.name))

          applicationDetailsFuture.flatMap {
            case Some(appDetails) => {

              val tnrbModel = appDetails.increaseIhtThreshold.getOrElse(
                TnrbEligibiltyModel(None, None, None, None, None, None, None, None, None, None, None))

              boundForm.fold(
                formWithErrors => {
                  Future.successful(BadRequest(iht.views.html.application.tnrb.gifts_with_reservation_of_benefit(formWithErrors,
                    tnrbModel, deceasedName, cancelUrl)))
                },
                tnrbModel => {
                  saveApplication(StringHelper.getNino(user), tnrbModel, appDetails, regDetails)
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
      fold(new TnrbEligibiltyModel(None, None, None, isPartnerGiftWithResToOther = tnrbModel.isPartnerGiftWithResToOther, None, None, None,
        None, None, None, None))(_.copy(isPartnerGiftWithResToOther = tnrbModel.isPartnerGiftWithResToOther))))

    val updatedAppDetailsWithKickOutReason = ApplicationKickOutNonSummaryHelper.updateKickout(checks = ApplicationKickOutNonSummaryHelper.checksTnrbEligibility,
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
          case _ => TnrbHelper.successfulTnrbRedirect(updatedAppDetailsWithKickOutReason, Some(TnrbGiftsWithReservationID))
        }
      }
    }
  }
}

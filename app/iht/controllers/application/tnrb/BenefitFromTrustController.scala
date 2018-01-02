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
import iht.controllers.application.EstateController
import iht.forms.TnrbForms._
import iht.metrics.Metrics
import iht.models.application.ApplicationDetails
import iht.models.application.tnrb.{WidowCheck, TnrbEligibiltyModel}
import iht.models.RegistrationDetails
import iht.utils._
import iht.utils.tnrb.TnrbHelper
import play.api.Logger
import play.api.data.FormError
import play.api.i18n.Messages
import play.api.mvc.{Request, Result}
import iht.constants.Constants._
import iht.constants.IhtProperties._
import play.api.i18n.Messages.Implicits._
import play.api.Play.current
import scala.concurrent.Future
import iht.utils.CommonHelper
import uk.gov.hmrc.http.HeaderCarrier


object BenefitFromTrustController extends BenefitFromTrustController with IhtConnectors {
  def metrics: Metrics = Metrics
}

trait BenefitFromTrustController extends EstateController {
  override val applicationSection = Some(ApplicationKickOutHelper.ApplicationSectionGiftsWithReservation)
  val cancelUrl = iht.controllers.application.tnrb.routes.TnrbOverviewController.onPageLoad()

  def onPageLoad = authorisedForIht {
    implicit user =>
      implicit request => {
        withRegistrationDetails { registrationDetails =>
          for {
            applicationDetails <- ihtConnector.getApplication(StringHelper.getNino(user),
              CommonHelper.getOrExceptionNoIHTRef(registrationDetails.ihtReference),
              registrationDetails.acknowledgmentReference)
          } yield {
            applicationDetails match {
              case Some(appDetails) => {

                val filledForm = benefitFromTrustForm.fill(appDetails.increaseIhtThreshold.getOrElse(
                  TnrbEligibiltyModel(None, None, None, None, None, None, None, None, None, None, None)))

                val deceasedName = CommonHelper.getOrException(registrationDetails.deceasedDetails).name

                Ok(iht.views.html.application.tnrb.benefit_from_trust(
                  filledForm,
                  appDetails.increaseIhtThreshold.fold(TnrbEligibiltyModel(None, None, None, None, None, None, None, None, None, None, None))(identity),
                  appDetails.widowCheck.fold(WidowCheck(None, None))(identity),
                  deceasedName,
                  CommonHelper.addFragmentIdentifier(cancelUrl, Some(TnrbSpouseBenefitFromTrustID)))
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

          val deceasedName = CommonHelper.getOrException(regDetails.deceasedDetails).name

          val applicationDetailsFuture = ihtConnector.getApplication(StringHelper.getNino(user),
            CommonHelper.getOrExceptionNoIHTRef(regDetails.ihtReference),
            regDetails.acknowledgmentReference)

          val boundForm = benefitFromTrustForm.bindFromRequest

          applicationDetailsFuture.flatMap {
            case Some(appDetails) => {
              boundForm.fold(
                formWithErrors => {
                  Future.successful(BadRequest(iht.views.html.application.tnrb.benefit_from_trust(formWithErrors,
                    appDetails.increaseIhtThreshold.fold(TnrbEligibiltyModel(None, None, None, None, None, None, None, None, None, None, None))(identity),
                    appDetails.widowCheck.fold(WidowCheck(None, None))(identity),
                    deceasedName,
                    cancelUrl)))
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
      fold(new TnrbEligibiltyModel(None, None, None, None, isPartnerBenFromTrust = tnrbModel.isPartnerBenFromTrust,
        None, None, None, None, None, None))(_.copy(isPartnerBenFromTrust = tnrbModel.isPartnerBenFromTrust))))

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
          case _ => TnrbHelper.successfulTnrbRedirect(updatedAppDetailsWithKickOutReason, Some(TnrbSpouseBenefitFromTrustID))
        }
      }
    }
  }
}

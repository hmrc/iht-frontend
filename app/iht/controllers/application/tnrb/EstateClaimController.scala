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
import iht.utils.{ApplicationKickOutHelper, ApplicationKickOutNonSummaryHelper, CommonHelper, StringHelper}
import javax.inject.Inject
import play.api.Logger
import play.api.Play.current
import play.api.i18n.Messages.Implicits._
import play.api.mvc.{Request, Result}
import uk.gov.hmrc.auth.core.AuthConnector
import uk.gov.hmrc.auth.core.PlayAuthConnector
import uk.gov.hmrc.auth.core.retrieve.v2.Retrievals.{nino => ninoRetrieval}
import uk.gov.hmrc.http.HeaderCarrier

import scala.concurrent.Future


class EstateClaimControllerImpl @Inject()() extends EstateClaimController with IhtConnectors {
  def metrics: Metrics = Metrics
}

trait EstateClaimController extends EstateController {
  override val applicationSection = Some(ApplicationKickOutHelper.ApplicationSectionGiftsWithReservation)
  lazy val cancelUrl = iht.controllers.application.tnrb.routes.TnrbOverviewController.onPageLoad()

  def onPageLoad = authorisedForIhtWithRetrievals(ninoRetrieval) { userNino =>

      implicit request => {
        withRegistrationDetails { registrationDetails =>
          for {
            applicationDetails <- ihtConnector.getApplication(StringHelper.getNino(userNino),
              CommonHelper.getOrExceptionNoIHTRef(registrationDetails.ihtReference),
              registrationDetails.acknowledgmentReference)
          } yield {
            applicationDetails match {
              case Some(appDetails) => {

                val filledForm = estateClaimAnyBusinessForm.fill(appDetails.increaseIhtThreshold.getOrElse(
                  TnrbEligibiltyModel(None, None, None, None, None, None, None, None, None, None, None)))

                Ok(iht.views.html.application.tnrb.estate_claim(
                  filledForm,
                  CommonHelper.addFragmentIdentifier(cancelUrl, Some(TnrbEstateReliefID))
                ))
              }
              case _ => InternalServerError("Application details not found")
            }
          }
        }
      }
  }

  def onSubmit = authorisedForIhtWithRetrievals(ninoRetrieval) { userNino =>

      implicit request => {
        withRegistrationDetails { regDetails =>

          val applicationDetailsFuture = ihtConnector.getApplication(StringHelper.getNino(userNino),
            CommonHelper.getOrExceptionNoIHTRef(regDetails.ihtReference),
            regDetails.acknowledgmentReference)

          val boundForm = estateClaimAnyBusinessForm.bindFromRequest

          applicationDetailsFuture.flatMap {
            case Some(appDetails) => {
              boundForm.fold(
                formWithErrors => {
                  Future.successful(BadRequest(iht.views.html.application.tnrb.estate_claim(formWithErrors, cancelUrl)))
                },
                tnrbModel => {
                  saveApplication(StringHelper.getNino(userNino), tnrbModel, appDetails, regDetails)
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
      fold(new TnrbEligibiltyModel(None, None, isStateClaimAnyBusiness = tnrbModel.isStateClaimAnyBusiness, None, None,
        None, None, None, None, None, None))(_.copy(isStateClaimAnyBusiness = tnrbModel.isStateClaimAnyBusiness))))

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
          case Some(reason) => Redirect(iht.controllers.application.routes.KickoutAppController.onPageLoad())
          case _ => TnrbHelper.successfulTnrbRedirect(updatedAppDetailsWithKickOutReason, Some(TnrbEstateReliefID))
        }
      }
    }
  }
}

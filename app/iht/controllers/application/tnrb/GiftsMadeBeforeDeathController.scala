/*
 * Copyright 2022 HM Revenue & Customs
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

import iht.config.AppConfig
import iht.connector.{CachingConnector, IhtConnector}
import iht.controllers.application.EstateController
import iht.forms.TnrbForms._
import iht.models.RegistrationDetails
import iht.models.application.ApplicationDetails
import iht.models.application.tnrb.{TnrbEligibiltyModel, WidowCheck}
import iht.utils.tnrb.TnrbHelper
import iht.utils.{ApplicationKickOutHelper, CommonHelper, IhtFormValidator, StringHelper}
import javax.inject.Inject
import play.api.Logging
import play.api.mvc.{MessagesControllerComponents, Request, Result}
import uk.gov.hmrc.auth.core.AuthConnector
import uk.gov.hmrc.auth.core.retrieve.v2.Retrievals.{nino => ninoRetrieval}
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController
import iht.views.html.application.tnrb.gifts_made_before_death

import scala.concurrent.Future

class GiftsMadeBeforeDeathControllerImpl @Inject()(val ihtConnector: IhtConnector,
                                                   val cachingConnector: CachingConnector,
                                                   val authConnector: AuthConnector,
                                                   val giftsMadeBeforeDeathView: gifts_made_before_death,
                                                   implicit val appConfig: AppConfig,
val cc: MessagesControllerComponents) extends FrontendController(cc) with GiftsMadeBeforeDeathController

trait GiftsMadeBeforeDeathController extends EstateController with StringHelper with TnrbHelper with Logging {

  override val applicationSection = Some(ApplicationKickOutHelper.ApplicationSectionGiftsWithReservation)
  def cancelUrl = iht.controllers.application.tnrb.routes.TnrbOverviewController.onPageLoad
  val giftsMadeBeforeDeathView: gifts_made_before_death

  def onPageLoad = authorisedForIhtWithRetrievals(ninoRetrieval) { userNino =>
      implicit request => {
        withRegistrationDetails { registrationDetails =>
          for {
            applicationDetails <- ihtConnector.getApplication(getNino(userNino),
              CommonHelper.getOrExceptionNoIHTRef(registrationDetails.ihtReference),
              registrationDetails.acknowledgmentReference)
          } yield {
            applicationDetails match {
              case Some(appDetails) => {

                val filledForm = giftMadeBeforeDeathForm.fill(appDetails.increaseIhtThreshold.getOrElse(
                  TnrbEligibiltyModel(None, None, None, None, None, None, None, None, None, None, None)))

                Ok(giftsMadeBeforeDeathView(
                  filledForm,
                  appDetails.increaseIhtThreshold.fold(TnrbEligibiltyModel(None, None, None, None, None, None, None, None, None, None, None))(identity),
                  appDetails.widowCheck.fold(WidowCheck(None, None))(identity),
                  CommonHelper.addFragmentIdentifier(cancelUrl, Some(appConfig.TnrbGiftsGivenAwayID)),
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

  def onSubmit = authorisedForIhtWithRetrievals(ninoRetrieval) { userNino =>

      implicit request => {

        withRegistrationDetails { regDetails =>

          val applicationDetailsFuture = ihtConnector.getApplication(getNino(userNino),
            CommonHelper.getOrExceptionNoIHTRef(regDetails.ihtReference),
            regDetails.acknowledgmentReference)

          val boundForm = IhtFormValidator.addDeceasedNameToAllFormErrors(giftMadeBeforeDeathForm
            .bindFromRequest, regDetails.deceasedDetails.fold("")(_.name))

          applicationDetailsFuture.flatMap {
            case Some(appDetails) => {
              boundForm.fold(
                formWithErrors => {

                  Future.successful(BadRequest(giftsMadeBeforeDeathView(formWithErrors,
                    appDetails.increaseIhtThreshold.fold(TnrbEligibiltyModel(None, None, None, None, None, None, None, None, None, None, None))(identity),
                    appDetails.widowCheck.fold(WidowCheck(None, None))(identity),
                    cancelUrl,
                    regDetails
                  )))
                },
                tnrbModel => {
                  saveApplication(getNino(userNino), tnrbModel, appDetails, regDetails)
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

    val updatedAppDetailsWithKickOutReason = appKickoutUpdateKickout(checks = checksTnrbEligibility,
      registrationDetails = regDetails,
      applicationDetails = updatedAppDetails)

    for {
      savedApplicationDetails <- ihtConnector.saveApplication(nino, updatedAppDetailsWithKickOutReason, regDetails.acknowledgmentReference)
    } yield {
      savedApplicationDetails.fold[Result] {
        logger.warn("Problem storing Application details. Redirecting to InternalServerError")
        InternalServerError
      } { _ =>
        updatedAppDetailsWithKickOutReason.kickoutReason match {
          case Some(reason) => Redirect(iht.controllers.application.routes.KickoutAppController.onPageLoad)
          case _ => successfulTnrbRedirect(updatedAppDetailsWithKickOutReason, Some(appConfig.TnrbGiftsGivenAwayID))
        }
      }
    }
  }
}

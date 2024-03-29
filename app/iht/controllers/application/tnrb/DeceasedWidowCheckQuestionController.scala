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
import play.api.mvc.{MessagesControllerComponents, Request, Result}
import uk.gov.hmrc.auth.core.AuthConnector
import uk.gov.hmrc.auth.core.retrieve.v2.Retrievals.{nino => ninoRetrieval}
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController
import iht.views.html.application.tnrb.deceased_widow_check_question

import scala.concurrent.Future


class DeceasedWidowCheckQuestionControllerImpl @Inject()(val ihtConnector: IhtConnector,
                                                         val cachingConnector: CachingConnector,
                                                         val authConnector: AuthConnector,
                                                         val deceasedWidowCheckQuestionView: deceased_widow_check_question,
                                                         implicit val appConfig: AppConfig,
                                                         val cc: MessagesControllerComponents)
  extends FrontendController(cc) with DeceasedWidowCheckQuestionController

trait DeceasedWidowCheckQuestionController extends EstateController with TnrbHelper with StringHelper {
  override val applicationSection = Some(ApplicationKickOutHelper.ApplicationSectionGiftsWithReservation)

  val deceasedWidowCheckQuestionView: deceased_widow_check_question
  def onPageLoad = authorisedForIhtWithRetrievals(ninoRetrieval) { userNino =>

      implicit request => {
        withRegistrationDetails { registrationDetails =>
          for {
            applicationDetails <- ihtConnector.getApplication(getNino(userNino),
              CommonHelper.getOrExceptionNoIHTRef(registrationDetails.ihtReference),
              registrationDetails.acknowledgmentReference)
          } yield {
            applicationDetails match {
              case Some(appDetails) =>
                val filledForm = deceasedWidowCheckQuestionForm.fill(appDetails.widowCheck.getOrElse(
                  WidowCheck(None, None)))

                Ok(deceasedWidowCheckQuestionView(
                  filledForm,
                  appDetails.widowCheck.fold(WidowCheck(None, None))(identity),
                  appDetails.increaseIhtThreshold.fold(
                    TnrbEligibiltyModel(None, None, None, None, None, None, None, None, None, None, None))(identity),
                  registrationDetails,
                  cancelLinkUrlForWidowCheckPages(appDetails, Some(appConfig.TnrbSpouseMartialStatusID)),
                  cancelLinkTextForWidowCheckPages(appDetails)))
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

          val boundForm = IhtFormValidator.addDeceasedNameToAllFormErrors(deceasedWidowCheckQuestionForm
            .bindFromRequest, regDetails.deceasedDetails.fold("")(_.name))

          applicationDetailsFuture.flatMap {
            case Some(appDetails) => {
              boundForm.fold(
                formWithErrors => {
                  Future.successful(BadRequest(deceasedWidowCheckQuestionView(formWithErrors,
                    appDetails.widowCheck.fold(WidowCheck(None, None))(identity),
                    appDetails.increaseIhtThreshold.fold(
                      TnrbEligibiltyModel(None, None, None, None, None, None, None, None, None, None, None))(identity),
                    regDetails,
                    cancelLinkUrlForWidowCheckPages(appDetails, Some(appConfig.TnrbSpouseMartialStatusID)),
                    cancelLinkTextForWidowCheckPages(appDetails))))
                },
                widowModel => {
                  saveApplication(getNino(userNino), widowModel, appDetails, regDetails)
                }
              )
            }
            case _ => Future.successful(InternalServerError("Application details not found"))
          }
        }
      }
  }

  private def saveApplication(nino: String,
                              widowModel: WidowCheck,
                              appDetails: ApplicationDetails,
                              regDetails: RegistrationDetails)(implicit request: Request[_],
                                                               hc: HeaderCarrier): Future[Result] = {

    val updatedAppDetails = getUpdatedAppDetails(appDetails, widowModel)

    val updatedAppDetailsWithKickOutReason = appKickoutUpdateKickout(checks = checksWidowOpc,
      registrationDetails = regDetails,
      applicationDetails = updatedAppDetails)

    for {
      savedApplicationDetails <- ihtConnector.saveApplication(nino,
        updatedAppDetailsWithKickOutReason,
        regDetails.acknowledgmentReference)
    } yield {
      savedApplicationDetails.fold[Result] {
        logger.warn("Problem storing Application details. Redirecting to InternalServerError")
        InternalServerError
      } {
        appDetails =>
          if (appDetails.widowCheck.fold(false)(_.widowed.fold(false)(identity))) {
            appDetails.isWidowCheckSectionCompleted match {
              case true => Redirect(CommonHelper.addFragmentIdentifier(routes.TnrbOverviewController.onPageLoad, Some(appConfig.TnrbSpouseMartialStatusID)))
              case _ => Redirect(routes.DeceasedWidowCheckDateController.onPageLoad)
            }

          } else {
            Redirect(iht.controllers.application.routes.EstateOverviewController.onPageLoadWithIhtRef(
              appDetails.ihtRef.fold(throw new RuntimeException("No IHT Reference available"))(identity)))
          }
      }
    }
  }

  /**
    * WidowCheck date and Tnrb eligibility data will be wiped out if option No
    * is selected as WidowCheck question
    *
    * @param appDetails
    * @param widowModel
    * @return
    */
  private def getUpdatedAppDetails(appDetails: ApplicationDetails,
                                   widowModel: WidowCheck) = {

    val widowCheckWithNoPredeceasedDate = new WidowCheck(widowed = widowModel.widowed, dateOfPreDeceased = None)

    widowModel.widowed match {
      case Some(true) =>
        appDetails.copy(widowCheck = Some(appDetails.widowCheck.fold(widowCheckWithNoPredeceasedDate)
        (_.copy(widowed = widowModel.widowed))))
      case Some(false) =>
        appDetails.copy(widowCheck = Some(appDetails.widowCheck.fold(widowCheckWithNoPredeceasedDate)
        (_.copy(widowed = widowModel.widowed, dateOfPreDeceased = None))),
          increaseIhtThreshold = None)
      case _ => {
        logger.warn("WidowCheck question has not been answered")
        throw new RuntimeException("WidowCheck question has not been answered")
      }
    }
  }

}

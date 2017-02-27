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

import iht.controllers.IhtConnectors
import iht.controllers.application.EstateController
import iht.forms.TnrbForms._
import iht.metrics.Metrics
import iht.models.application.ApplicationDetails
import iht.models.application.tnrb.{WidowCheck, TnrbEligibiltyModel}
import iht.models.RegistrationDetails
import iht.utils.tnrb.TnrbHelper._
import iht.utils.{ApplicationKickOutHelper, CommonHelper}
import play.api.Logger
import play.api.mvc.{Request, Result}
import uk.gov.hmrc.play.http.HeaderCarrier
import play.api.i18n.Messages.Implicits._
import play.api.Play.current
import iht.constants.Constants._
import iht.constants.IhtProperties._
import scala.concurrent.Future

object DeceasedWidowCheckQuestionController extends DeceasedWidowCheckQuestionController with IhtConnectors {
  def metrics: Metrics = Metrics
}

trait DeceasedWidowCheckQuestionController extends EstateController {
  override val applicationSection = Some(ApplicationKickOutHelper.ApplicationSectionGiftsWithReservation)

  def onPageLoad = authorisedForIht {
    implicit user => implicit request => {
      val registrationDetails = cachingConnector.getExistingRegistrationDetails

      for {
        applicationDetails <- ihtConnector.getApplication(CommonHelper.getNino(user),
          CommonHelper.getOrExceptionNoIHTRef(registrationDetails.ihtReference),
          registrationDetails.acknowledgmentReference)
      } yield {
        applicationDetails match {
          case Some(appDetails) => {

            val filledForm = deceasedWidowCheckQuestionForm.fill(appDetails.widowCheck.getOrElse(
              WidowCheck(None, None)))

            Ok(iht.views.html.application.tnrb.deceased_widow_check_question(
              filledForm,
              appDetails.widowCheck.fold(WidowCheck(None, None))(identity),
              appDetails.increaseIhtThreshold.fold(
                TnrbEligibiltyModel(None, None, None, None, None, None, None, None, None, None, None))(identity),
              registrationDetails,
              cancelLinkUrlForWidowCheckPages(appDetails, Some(TnrbSpouseMartialStatusID)),
              cancelLinkTextForWidowCheckPages(appDetails)))
          }
          case _ => InternalServerError("Application details not found")
        }
      }
    }
  }

  def onSubmit = authorisedForIht {
    implicit user => implicit request => {
      val regDetails = cachingConnector.getExistingRegistrationDetails

      val applicationDetailsFuture = ihtConnector.getApplication(CommonHelper.getNino(user),
        CommonHelper.getOrExceptionNoIHTRef(regDetails.ihtReference),
        regDetails.acknowledgmentReference)

      val boundForm = deceasedWidowCheckQuestionForm.bindFromRequest

      applicationDetailsFuture.flatMap {
        case Some(appDetails) => {
          boundForm.fold(
            formWithErrors => {
              Future.successful(BadRequest(iht.views.html.application.tnrb.deceased_widow_check_question(formWithErrors,
                appDetails.widowCheck.fold(WidowCheck(None, None))(identity),
                appDetails.increaseIhtThreshold.fold(
                  TnrbEligibiltyModel(None, None, None, None, None, None, None, None, None, None, None))(identity),
                regDetails,
                cancelLinkUrlForWidowCheckPages(appDetails, Some(TnrbSpouseMartialStatusID)),
                cancelLinkTextForWidowCheckPages(appDetails))))
            },
            widowModel => {
              saveApplication(CommonHelper.getNino(user), widowModel, appDetails, regDetails)
            }
          )
        }
        case _ => Future.successful(InternalServerError("Application details not found"))
      }
    }
  }

  private def saveApplication(nino: String,
                              widowModel: WidowCheck,
                              appDetails: ApplicationDetails,
                              regDetails: RegistrationDetails)(implicit request: Request[_],
                                                               hc: HeaderCarrier): Future[Result] = {

    val updatedAppDetails = getUpdatedAppDetails(appDetails, widowModel)

    val updatedAppDetailsWithKickOutReason = ApplicationKickOutHelper.updateKickout(checks = ApplicationKickOutHelper.checksWidowOpc,
      registrationDetails = regDetails,
      applicationDetails = updatedAppDetails)

    for {
      savedApplicationDetails <- ihtConnector.saveApplication(nino,
        updatedAppDetailsWithKickOutReason,
        regDetails.acknowledgmentReference)
    } yield {
      savedApplicationDetails.fold[Result] {
        Logger.warn("Problem storing Application details. Redirecting to InternalServerError")
        InternalServerError
      } {
        appDetails =>
          if (appDetails.widowCheck.fold(false)(_.widowed.fold(false)(identity))) {
            appDetails.isWidowCheckSectionCompleted match {
              case true => Redirect(addFragmentIdentifier(routes.TnrbOverviewController.onPageLoad(), Some(TnrbSpouseMartialStatusID)))
              case _ => Redirect(routes.DeceasedWidowCheckDateController.onPageLoad())
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
      case Some(true) => {
        appDetails.copy(widowCheck = Some(appDetails.widowCheck.fold(widowCheckWithNoPredeceasedDate)
        (_.copy(widowed = widowModel.widowed))))
      }

      case Some(false )=> {
        appDetails.copy(widowCheck = Some(appDetails.widowCheck.fold(widowCheckWithNoPredeceasedDate)
        (_.copy(widowed = widowModel.widowed, dateOfPreDeceased = None))),
          increaseIhtThreshold = None)
      }
      case _ => {
        Logger.warn("WidowCheck question has not been answered")
        throw new RuntimeException("WidowCheck question has not been answered")
      }
    }
  }

}

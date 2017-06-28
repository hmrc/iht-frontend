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
import iht.models.application.tnrb.{TnrbEligibiltyModel, WidowCheck}
import iht.utils.tnrb.TnrbHelper
import iht.utils.tnrb.TnrbHelper._
import iht.utils.{ApplicationKickOutHelper, CommonHelper}
import org.joda.time.LocalDate
import play.api.Logger
import play.api.data.Form
import play.api.i18n.MessagesApi
import play.api.mvc.{Request, Result}
import uk.gov.hmrc.play.http.HeaderCarrier

import scala.concurrent.Future

@Singleton
class DeceasedWidowCheckDateController @Inject() (implicit val messagesApi: MessagesApi, val ihtProperties: IhtProperties, val applicationForms: ApplicationForms) extends EstateController {
  override val applicationSection = Some(ApplicationKickOutHelper.ApplicationSectionGiftsWithReservation)

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

                val filledForm = deceasedWidowCheckDateForm.fill(appDetails.widowCheck.getOrElse(
                  WidowCheck(None, None)))

                Ok(iht.views.html.application.tnrb.deceased_widow_check_date(
                  filledForm,
                  appDetails.widowCheck.fold(WidowCheck(None, None))(identity),
                  appDetails.increaseIhtThreshold.fold(TnrbEligibiltyModel(None, None, None, None, None, None, None, None, None, None, None))(identity),
                  registrationDetails,
                  cancelLinkUrlForWidowCheckPages(appDetails, Some(ihtProperties.TnrbSpouseDateOfDeathID)),
                  cancelLinkTextForWidowCheckPages(appDetails)))
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

          val boundForm = deceasedWidowCheckDateForm.bindFromRequest

          applicationDetailsFuture.flatMap {
            case Some(appDetails) => {
              val dateOfMarriage = appDetails.increaseIhtThreshold.flatMap(tnrbModel => tnrbModel.dateOfMarriage)
              additionalErrorsForForm(boundForm, dateOfMarriage).fold(
                formWithErrors => {
                  Future.successful(BadRequest(iht.views.html.application.tnrb.deceased_widow_check_date(formWithErrors,
                    appDetails.widowCheck.fold(WidowCheck(None, None))(identity),
                    appDetails.increaseIhtThreshold.fold(TnrbEligibiltyModel(None, None, None, None, None, None, None, None, None, None, None))(identity),
                    regDetails,
                    cancelLinkUrlForWidowCheckPages(appDetails, Some(ihtProperties.TnrbSpouseDateOfDeathID)),
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
  }

  private def additionalErrorsForForm(boundForm: Form[WidowCheck],
                                      dateOfMarriage: Option[LocalDate]): Form[WidowCheck] = {

    val predeceasedDateOfDeath: Option[LocalDate] = predeceasedDateFromForm(boundForm)

    if (!boundForm.hasErrors && dateOfMarriage.isDefined &&
      CommonHelper.getOrException(dateOfMarriage).compareTo(CommonHelper.getOrException(predeceasedDateOfDeath)) >= 0) {
      boundForm.withError("dateOfPreDeceased", "error.predeceasedDateOfDeath.afterMarriage")
    } else {
      boundForm
    }
  }

  private def predeceasedDateFromForm(boundForm: Form[WidowCheck]): Option[LocalDate] = {
    val year = boundForm("dateOfPreDeceased.year").value.getOrElse("")
    val month = boundForm("dateOfPreDeceased.month").value.getOrElse("")
    val day = boundForm("dateOfPreDeceased.day").value.getOrElse("")

    CommonHelper.createDate(Some(year),
      Some(month),
      Some(day))
  }


  private def saveApplication(nino: String,
                              widowModel: WidowCheck,
                              appDetails: ApplicationDetails,
                              regDetails: RegistrationDetails)(implicit request: Request[_],
                                                               hc: HeaderCarrier): Future[Result] = {

    val updatedAppDetails = appDetails.copy(widowCheck = Some(appDetails.widowCheck.
      fold(new WidowCheck(widowed = Some(true), dateOfPreDeceased = widowModel.dateOfPreDeceased))
      (_.copy(widowed = Some(true), dateOfPreDeceased = widowModel.dateOfPreDeceased))))

    val updatedAppDetailsWithKickOutReason = ApplicationKickOutHelper.updateKickout(checks = ApplicationKickOutHelper.checksWidowOpc,
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
          case _ => TnrbHelper.successfulTnrbRedirect(updatedAppDetailsWithKickOutReason, Some(ihtProperties.TnrbSpouseDateOfDeathID))
        }
      }
    }
  }
}

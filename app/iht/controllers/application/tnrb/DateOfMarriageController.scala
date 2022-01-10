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
import iht.utils.{ApplicationKickOutHelper, CommonHelper, DateHelper, StringHelper}
import javax.inject.Inject
import org.joda.time.LocalDate
import play.api.data.Form
import play.api.i18n.Messages
import play.api.mvc.{MessagesControllerComponents, Request, Result}
import uk.gov.hmrc.auth.core.AuthConnector
import uk.gov.hmrc.auth.core.retrieve.v2.Retrievals.{nino => ninoRetrieval}
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController
import iht.views.html.application.tnrb.date_of_marriage

import scala.concurrent.Future


class DateOfMarriageControllerImpl @Inject()(val ihtConnector: IhtConnector,
                                             val cachingConnector: CachingConnector,
                                             val authConnector: AuthConnector,
                                             val dateOfMarriageView: date_of_marriage,
                                             implicit val appConfig: AppConfig,
                                             val cc: MessagesControllerComponents) extends FrontendController(cc) with DateOfMarriageController

trait DateOfMarriageController extends EstateController with TnrbHelper with StringHelper {
  override val applicationSection = Some(ApplicationKickOutHelper.ApplicationSectionGiftsWithReservation)
  def cancelUrl = iht.controllers.application.tnrb.routes.TnrbOverviewController.onPageLoad()

  private def predeceasedName(appDetails: ApplicationDetails)(implicit messages: Messages) = {
    spouseOrCivilPartnerLabelPossessive(
      tnrbModel = appDetails.increaseIhtThreshold.fold(
        TnrbEligibiltyModel(None, None, None, None, None, None, None, None, None, None, None))(identity),
      widowCheck = CommonHelper.getOrException(appDetails.widowCheck),
      prefixText = messages("page.iht.application.tnrbEligibilty.partner.additional.label.andTheir"),
      wrapName = true)(messages)
  }
  val dateOfMarriageView: date_of_marriage
  def onPageLoad = authorisedForIhtWithRetrievals(ninoRetrieval) { userNino =>

      implicit request => {
        withRegistrationDetails { registrationDetails =>
          val deceasedName = CommonHelper.getOrException(registrationDetails.deceasedDetails).name

          for {
            applicationDetails <- ihtConnector.getApplication(getNino(userNino),
              CommonHelper.getOrExceptionNoIHTRef(registrationDetails.ihtReference),
              registrationDetails.acknowledgmentReference)
          } yield {
            applicationDetails match {
              case Some(appDetails) =>
                val filledForm = dateOfMarriageForm.fill(appDetails.increaseIhtThreshold.getOrElse(
                  TnrbEligibiltyModel(None, None, None, None, None, None, None, None, None, None, None)))

                Ok(dateOfMarriageView(
                  filledForm,
                  appDetails.widowCheck.fold(WidowCheck(None, None))(identity),
                  deceasedName,
                  predeceasedName(appDetails),
                  CommonHelper.addFragmentIdentifier(cancelUrl, Some(appConfig.TnrbSpouseDateOfMarriageID))
                )
                )
              case _ => InternalServerError("Application details not found")
            }
          }
        }
      }
  }

  def onSubmit = authorisedForIhtWithRetrievals(ninoRetrieval) { userNino =>

      implicit request => {
        withRegistrationDetails { regDetails =>
          val deceasedName = CommonHelper.getOrException(regDetails.deceasedDetails).name

          val applicationDetailsFuture = ihtConnector.getApplication(getNino(userNino),
            CommonHelper.getOrExceptionNoIHTRef(regDetails.ihtReference),
            regDetails.acknowledgmentReference)

          val boundForm = dateOfMarriageForm.bindFromRequest

          applicationDetailsFuture.flatMap {
            case Some(appDetails) =>
              val dateOfPreDeceased = CommonHelper.getOrException(CommonHelper.getOrException(appDetails.widowCheck).dateOfPreDeceased)

              additionalErrorsForForm(boundForm, dateOfPreDeceased).fold(
                formWithErrors => {
                  Future.successful(BadRequest(dateOfMarriageView(formWithErrors,
                    appDetails.widowCheck.fold(WidowCheck(None, None))(identity),
                    deceasedName,
                    predeceasedName(appDetails),
                    cancelUrl
                  )))
                },
                tnrbModel => {
                  saveApplication(getNino(userNino), tnrbModel, appDetails, regDetails)
                }
              )
            case _ => Future.successful(InternalServerError("Application details not found"))
          }
        }
      }
  }

  private def additionalErrorsForForm(boundForm: Form[TnrbEligibiltyModel],
                                      predeceasedDate: LocalDate): Form[TnrbEligibiltyModel] = {

    val marriageDate: Option[LocalDate] = marriageDateFromForm(boundForm)

    if (!boundForm.hasErrors && CommonHelper.getOrException(marriageDate).compareTo(predeceasedDate) >= 0) {
      boundForm.withError("dateOfMarriage", "error.predeceasedDateOfMarriage.beforeDateOfDeath")
    } else {
      boundForm
    }
  }

  private def marriageDateFromForm(boundForm: Form[TnrbEligibiltyModel]): Option[LocalDate] = {
    val dateOfMarriageDay = boundForm("dateOfMarriage.day").value.getOrElse("")
    val dateOfMarriageMonth = boundForm("dateOfMarriage.month").value.getOrElse("")
    val dateOfMarriageYear = boundForm("dateOfMarriage.year").value.getOrElse("")

    DateHelper.createDate(Some(dateOfMarriageYear),
      Some(dateOfMarriageMonth),
      Some(dateOfMarriageDay))
  }

  private def saveApplication(nino: String,
                              tnrbModel: TnrbEligibiltyModel,
                              appDetails: ApplicationDetails,
                              regDetails: RegistrationDetails)(implicit request: Request[_],
                                                               hc: HeaderCarrier): Future[Result] = {

    val updatedAppDetails = appDetails.copy(increaseIhtThreshold = Some(appDetails.increaseIhtThreshold.
      fold(new TnrbEligibiltyModel(None, None, None, None, None, None, None, None, None, dateOfMarriage =
        tnrbModel.dateOfMarriage, None))(_.copy(dateOfMarriage = tnrbModel.dateOfMarriage))))

    ihtConnector.saveApplication(nino, updatedAppDetails, regDetails.acknowledgmentReference) map (_ =>
      successfulTnrbRedirect(updatedAppDetails, Some(appConfig.TnrbSpouseDateOfMarriageID)))
  }
}

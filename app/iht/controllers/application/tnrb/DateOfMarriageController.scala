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
import iht.models.application.tnrb.{TnrbEligibiltyModel, WidowCheck}
import iht.models.RegistrationDetails
import iht.utils.tnrb.TnrbHelper
import iht.utils.{ApplicationKickOutHelper, CommonHelper}
import org.joda.time.LocalDate
import play.api.data.Form
import play.api.i18n.Messages
import play.api.mvc.{Request, Result}
import uk.gov.hmrc.play.http.HeaderCarrier
import play.api.i18n.Messages.Implicits._
import play.api.Play.current
import iht.constants.Constants._

import scala.concurrent.Future

object DateOfMarriageController extends DateOfMarriageController with IhtConnectors {
  def metrics : Metrics = Metrics
}

trait DateOfMarriageController extends EstateController{
  override val applicationSection = Some(ApplicationKickOutHelper.ApplicationSectionGiftsWithReservation)
  val cancelUrl = iht.controllers.application.tnrb.routes.TnrbOverviewController.onPageLoad()

  private def predeceasedName(appDetails: ApplicationDetails) = {
    TnrbHelper.spouseOrCivilPartnerLabel(
      tnrbModel = appDetails.increaseIhtThreshold.fold(
        TnrbEligibiltyModel(None, None, None, None, None, None, None, None, None, None, None))(identity),
      widowCheck = CommonHelper.getOrException(appDetails.widowCheck),
      prefixText = Messages("page.iht.application.tnrbEligibilty.partner.additional.label.their"),
      wrapName=true)
  }

  def onPageLoad = authorisedForIht {
    implicit user => implicit request => {
      val registrationDetails = cachingConnector.getExistingRegistrationDetails
      val deceasedName = CommonHelper.getOrException(registrationDetails.deceasedDetails).name

      for {
        applicationDetails <- ihtConnector.getApplication(CommonHelper.getNino(user),
          CommonHelper.getOrExceptionNoIHTRef(registrationDetails.ihtReference),
          registrationDetails.acknowledgmentReference)
      } yield {
        applicationDetails match {
          case Some(appDetails) =>
            val filledForm = dateOfMarriageForm.fill(appDetails.increaseIhtThreshold.getOrElse(
              TnrbEligibiltyModel(None, None, None, None, None, None, None, None, None, None, None)))

            Ok(iht.views.html.application.tnrb.date_of_marriage(
              filledForm,
              appDetails.widowCheck.fold(WidowCheck(None, None))(identity),
              deceasedName,
              predeceasedName(appDetails),
              addFragmentIdentifier(cancelUrl, Some(TnrbSpouseDateOfMarriageID))
            )
            )
          case _ => InternalServerError("Application details not found")
        }
      }
    }
  }

  def onSubmit = authorisedForIht {
    implicit user => implicit request => {
      val regDetails = cachingConnector.getExistingRegistrationDetails
      val deceasedName = CommonHelper.getOrException(regDetails.deceasedDetails).name

      val applicationDetailsFuture = ihtConnector.getApplication(CommonHelper.getNino(user),
        CommonHelper.getOrExceptionNoIHTRef(regDetails.ihtReference),
        regDetails.acknowledgmentReference)

      val boundForm = dateOfMarriageForm.bindFromRequest

      applicationDetailsFuture.flatMap {
        case Some(appDetails) =>
          val dateOfPreDeceased = CommonHelper.getOrException(CommonHelper.getOrException(appDetails.widowCheck).dateOfPreDeceased)

          additionalErrorsForForm(boundForm, dateOfPreDeceased).fold(
            formWithErrors=> {
              Future.successful(BadRequest(iht.views.html.application.tnrb.date_of_marriage(formWithErrors,
                appDetails.widowCheck.fold(WidowCheck(None, None))(identity),
                deceasedName,
                predeceasedName(appDetails),
                cancelUrl
              )))
            },
            tnrbModel => {
              saveApplication(CommonHelper.getNino(user),tnrbModel, appDetails, regDetails)
            }
          )
        case _ => Future.successful(InternalServerError("Application details not found"))
      }
    }
  }

  private def additionalErrorsForForm(boundForm: Form[TnrbEligibiltyModel],
                                      predeceasedDate: LocalDate): Form[TnrbEligibiltyModel] = {

   val marriageDate: Option[LocalDate] = marriageDateFromForm(boundForm)

    if(!boundForm.hasErrors && CommonHelper.getOrException(marriageDate).compareTo(predeceasedDate) >= 0) {
      boundForm.withError("dateOfMarriage", "error.predeceasedDateOfMarriage.beforeDateOfDeath")
    } else{
      boundForm
    }
  }

  private def marriageDateFromForm(boundForm: Form[TnrbEligibiltyModel]): Option[LocalDate] = {
    val dateOfMarriageDay = boundForm("dateOfMarriage.day").value.getOrElse("")
    val dateOfMarriageMonth = boundForm("dateOfMarriage.month").value.getOrElse("")
    val dateOfMarriageYear = boundForm("dateOfMarriage.year").value.getOrElse("")

    CommonHelper.createDate(Some(dateOfMarriageYear),
      Some(dateOfMarriageMonth),
      Some(dateOfMarriageDay))
  }

  private def saveApplication(nino:String,
                              tnrbModel: TnrbEligibiltyModel,
                              appDetails: ApplicationDetails,
                              regDetails: RegistrationDetails)(implicit request: Request[_],
                                                               hc: HeaderCarrier): Future[Result] = {

    val updatedAppDetails = appDetails.copy(increaseIhtThreshold = Some(appDetails.increaseIhtThreshold.
      fold(new TnrbEligibiltyModel(None, None, None, None, None, None, None, None, None, dateOfMarriage =
        tnrbModel.dateOfMarriage, None)) (_.copy(dateOfMarriage = tnrbModel.dateOfMarriage))))

    ihtConnector.saveApplication(nino, updatedAppDetails, regDetails.acknowledgmentReference) map (_ =>
      TnrbHelper.successfulTnrbRedirect(updatedAppDetails, Some(TnrbSpouseDateOfMarriageID)))
  }
 }

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

package iht.controllers

import iht.connector.ExplicitAuditConnector
import iht.constants.{Constants, IhtProperties}
import iht.controllers.auth.IhtBaseController
import iht.events.QuestionnaireEvent
import iht.forms.QuestionnaireForms._
import iht.models.QuestionnaireModel
import iht.utils.{LogHelper, SessionHelper, StringHelper}
import play.api.data.Form
import play.api.mvc._
import play.twirl.api.HtmlFormat.Appendable
import uk.gov.hmrc.play.frontend.controller.{FrontendController, UnauthorisedAction}

trait QuestionnaireController extends FrontendController with IhtBaseController {

  def explicitAuditConnector: ExplicitAuditConnector = ExplicitAuditConnector

  def questionnaireView: (Form[QuestionnaireModel], Request[_]) => Appendable

  def callPageLoad: Call

  val redirectLocationOnMissingNino: Call

  def signOutAndLoadPage = UnauthorisedAction {
    implicit request =>
      Redirect(callPageLoad).withNewSession
        .withSession(Constants.NINO -> SessionHelper.getNinoFromSession(request).fold("")(identity))
  }

  def onPageLoad = UnauthorisedAction {
    implicit request =>{
      val nino = SessionHelper.getNinoFromSession(request)

      nino match {
        case None => Redirect(redirectLocationOnMissingNino)
        case _ => Ok(questionnaireView(questionnaire_form, request))
          .withSession(request.session + (Constants.NINO -> nino.fold("")(identity)))
      }
    }
  }

  def onSubmit:Action[AnyContent]

  def doSubmit(includeIntendReturnQuestion:Boolean) = UnauthorisedAction {
    implicit request =>
      questionnaire_form.bindFromRequest().fold(
        formWithErrors => {
          LogHelper.logFormError(formWithErrors)
          BadRequest(questionnaireView(formWithErrors, request))
        },
        value => {
          val questionnaireEvent = new QuestionnaireEvent(
            feelingAboutExperience = value.feelingAboutExperience.fold("") {
              _.toString
            },
            easyToUse = value.easyToUse.fold("") {
              _.toString
            },
            howCanYouImprove = value.howCanYouImprove.getOrElse(""),
            fullName = value.fullName.getOrElse(""),
            nino = SessionHelper.getNinoFromSession(request).fold("")(identity),
            contactDetails = value.contactDetails.getOrElse(""),
            stageInService = value.stageInService.getOrElse(""),
            intendReturn = if (includeIntendReturnQuestion) {
              value.intendToReturn.fold("")(i => StringHelper.booleanToYesNo(i))
            } else {
              ""
            }
          )
          explicitAuditConnector.sendEvent(questionnaireEvent)
          Redirect(IhtProperties.linkGovUkIht)
        }
      )
  }
}

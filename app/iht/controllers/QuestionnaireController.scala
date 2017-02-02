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

package iht.controllers

import iht.connector.ExplicitAuditConnector
import iht.constants.IhtProperties
import iht.controllers.auth.IhtActions
import iht.events.QuestionnaireEvent
import iht.forms.QuestionnaireForms._
import iht.models.QuestionnaireModel
import iht.utils.{CommonHelper, LogHelper}
import play.api.data.Form
import play.api.mvc.Request
import play.twirl.api.HtmlFormat.Appendable
import uk.gov.hmrc.play.frontend.controller.{FrontendController, UnauthorisedAction}

import scala.concurrent.Future

trait QuestionnaireController extends FrontendController with IhtActions with IhtConnectors {

  val ninoKey = "nino"

  def explicitAuditConnector: ExplicitAuditConnector = ExplicitAuditConnector

  def questionnaireView: (Form[QuestionnaireModel], Request[_]) => Appendable

  def onPageLoad = authorisedForIht {
    implicit user =>
      implicit request => {
        cachingConnector.storeSingleValue(ninoKey, CommonHelper.getNino(user)).flatMap { _ =>
          Future.successful(Ok(questionnaireView(questionnaire_form, request)).withNewSession)
        }
      }
  }

  def onSubmit = UnauthorisedAction.async {
    implicit request => {
      questionnaire_form.bindFromRequest().fold(
        formWithErrors => {
          LogHelper.logFormError(formWithErrors)
          Future.successful(BadRequest(questionnaireView(formWithErrors, request)))
        },
        value => {
          cachingConnector.getSingleValue(ninoKey).flatMap { optionNino =>
            val retrievedNino: String = CommonHelper.getOrException(optionNino)
            val questionnaireEvent = new QuestionnaireEvent(
              feelingAboutExperience = value.feelingAboutExperience.fold("") {
                _.toString
              },
              easyToUse = value.easyToUse.fold("") {
                _.toString
              },
              howCanYouImprove = value.howCanYouImprove.getOrElse(""),
              fullName = value.fullName.getOrElse(""),
              nino = retrievedNino
            )
            explicitAuditConnector.sendEvent(questionnaireEvent)
            cachingConnector.delete(ninoKey).flatMap { _ =>
              Future.successful(Redirect(IhtProperties.linkGovUkIht))
            }
          }
        }
      )
    }
  }
}

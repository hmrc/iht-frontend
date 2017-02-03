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
import play.api.mvc._
import play.twirl.api.HtmlFormat.Appendable
import uk.gov.hmrc.play.frontend.controller.{FrontendController, UnauthorisedAction}
import uk.gov.hmrc.play.frontend.auth.Actions


import scala.concurrent.Future

trait QuestionnaireController extends FrontendController with Actions {

  val ninoKey = "nino"

  def explicitAuditConnector: ExplicitAuditConnector = ExplicitAuditConnector

  def questionnaireView: (Form[QuestionnaireModel], Request[_]) => Appendable

  def show: Action[AnyContent] = UnauthorisedAction {
    implicit request => {
      println("*** :::::::::::::::::::::: *** request in show method ::::   "+request)
      println("*** :::::::::::::::::::::::::: ***** ::: "+request.session.toString())
      val nino = request.session.get("customerNino").getOrElse("")
      println("*********************** ::: NINO ::::: ******************"+nino)
      Redirect(iht.controllers.application.routes.ApplicationQuestionnaireController.onPageLoad()).withNewSession.withSession(request.session+ ("customerNino" -> nino))
    }
  }

  def onPageLoad = UnauthorisedAction {
     implicit request => {
          val nino = request.session.get("customerNino").getOrElse("")
       println("*********************** ::: NINO ::::: ******************"+nino)
          Ok(questionnaireView(questionnaire_form, request)).withSession("customerNino" -> nino)
        }
  }

  def onSubmit = UnauthorisedAction {
    println("************************* In method Questionnaire Controller**********************************")
    implicit request => {
      println("************************* In request of on submit in Questionnaire Controller**********************************")
      questionnaire_form.bindFromRequest().fold(
        formWithErrors => {
          LogHelper.logFormError(formWithErrors)
        BadRequest(questionnaireView(formWithErrors, request))
        },
        value => {
            val retrievedNino: String = request.session.get("customerNino").getOrElse("")
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
            Redirect(iht.controllers.application.routes.ApplicationQuestionnaireController.ook())

        }
      )
    }
  }

  def ook: Action[AnyContent] = UnauthorisedAction(implicit request => Redirect(IhtProperties.linkGovUkIht))

}

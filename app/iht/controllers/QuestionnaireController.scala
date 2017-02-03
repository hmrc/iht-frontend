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

import scala.concurrent.Future

trait QuestionnaireController extends FrontendController with IhtActions with IhtConnectors {

  val ninoKey = "nino"

  def explicitAuditConnector: ExplicitAuditConnector = ExplicitAuditConnector

  def questionnaireView: (Form[QuestionnaireModel], Request[_]) => Appendable

  def onPageLoad = authorisedForIht {
    implicit user =>
      implicit request => {
        cachingConnector.storeSingleValue(ninoKey, CommonHelper.getNino(user)).flatMap { _ =>

          val oldCookie: Cookie = request.cookies("mdtp")
          println("\n*****COOKIE:" + oldCookie)

          /*
mdtp,9f3c910d883933c72f15271ed0cbc1a82663dae1-affinityGroup=Individual&name=1&authToken=Bearer+XlLM91CY3hEHqHlrKX9N0V8PJjegPbWLh4lG5zRW64rZxGBmnnQ4rjwyWA9vCCDpo68rWvduDWgY1RYmyc5rjYbqKXi3T68N1sOsuWoJuAwGQ2HPHSWeSFaw1kcg5fsr3x1FNcbInq30B5D9zDbp8KR%2BeoA3v3PSLkldNYqBmzI1TnU%2FCoN5cH3wc88qbn82&ts=1486119255213&ap=GGW&token=token&sessionId=session-0243dd0e-1a5c-4abf-8d41-df39ec80cbed&csrfToken=b2bec7dba4afab72eec5792336703e88c41d7cfe-1486119253180-3358f28836bfd5588665b65d&userId=%2Fauth%2Foid%2F586e4a664300004300579db4,None,/,None,false,false
           */

          // request.cookies.drop(0)

          //, domain = None, httpOnly = false, maxAge = None, name = "", path = "", secure = false

          val gg: Array[String] = oldCookie.value.split("&")
          val vv: Array[String] = gg.filter { (xx: String) => {
              val tt: Array[String] = xx.split("=")

            //println( "\nIII:" + tt(0) + ":"+ (tt(0) != "authToken"))

              tt(0) != "authToken"
            }
          }

          vv.foreach { xxxxxx =>
            println("\nYYYYY:" + xxxxxx)
          }


          var zzz: String = ""
          vv.foreach { mmm =>
            zzz += mmm + "&"
          }
          zzz = zzz.substring(0, zzz.length - 1)

          println( "\n*************************\nOLD=" + oldCookie.value)
          println( "\n*************************\nNEW=" + zzz)


          val newCookie = oldCookie
            .copy(value = zzz)

          val res = Ok(questionnaireView(questionnaire_form, request))
            .discardingCookies(DiscardingCookie("mdtp")).withCookies(newCookie)

          println( "\n**OLD COOKIE:" + oldCookie)


          println( "\n**NEW COOKIE:" + newCookie)

          Future.successful(res)
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
              Future.successful(Redirect(iht.controllers.application.routes.ApplicationQuestionnaireController.ook()))
            }
          }
        }
      )
    }
  }

  def ook: Action[AnyContent] = UnauthorisedAction(implicit request => Redirect(IhtProperties.linkGovUkIht))

}

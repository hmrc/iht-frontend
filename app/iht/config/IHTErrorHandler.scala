/*
 * Copyright 2020 HM Revenue & Customs
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

package iht.config

import javax.inject.Inject
import play.api.Configuration
import play.api.i18n.{Messages, MessagesApi}
import play.api.mvc.Results.InternalServerError
import play.api.mvc.{Request, RequestHeader, Result}
import play.twirl.api.Html
import uk.gov.hmrc.http.Upstream5xxResponse
import uk.gov.hmrc.play.bootstrap.http.FrontendErrorHandler
import uk.gov.hmrc.play.partials.FormPartialRetriever

class IHTErrorHandler @Inject()(val configuration: Configuration,
                                val messagesApi: MessagesApi,
                                implicit val formPartialRetriever: FormPartialRetriever,
                                implicit val appConfig: AppConfig) extends FrontendErrorHandler {

  override def standardErrorTemplate(pageTitle: String, heading: String, message: String)(implicit request: Request[_]): Html = {
    iht.views.html.iht_error_template()
  }

  override def notFoundTemplate(implicit request: Request[_]): Html = {
    iht.views.html.iht_not_found_template(
      Messages("global.error.pageNotFound404.title"),
      Messages("global.error.pageNotFound404.heading"),
      Messages("global.error.pageNotFound404.message")
    )
  }

  private[config] def desInternalServerErrorTemplate(implicit request: Request[_]): Html = {
    request.uri match {
      case s: String if s.contains("/registration/") => iht.views.html.registration.registration_generic_error()
      case s: String if s.contains("/estate-report/") => iht.views.html.application.application_generic_error()
      case _ =>     standardErrorTemplate(
        Messages("global.error.InternalServerError500.title"),
        Messages("global.error.InternalServerError500.heading"),
        Messages("global.error.InternalServerError500.message")
      )
    }
  }

  override def resolveError(rh: RequestHeader, ex: Throwable): Result = {
    ex match {
      case e: Upstream5xxResponse if e.upstreamResponseCode == 502 &&
        e.message.contains("500 response returned from DES") => InternalServerError(desInternalServerErrorTemplate(Request(rh, Result)))
      case _ => super.resolveError(rh, ex)
    }
  }
}
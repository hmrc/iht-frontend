/*
 * Copyright 2021 HM Revenue & Customs
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

import common.{CommonPlaySpec, WithCommonFakeApplication}
import iht.utils.CommonHelper
import iht.views.html.application.application_generic_error
import iht.views.html.registration.registration_generic_error
import iht.views.html.{iht_error_template, iht_not_found_template}
import org.jsoup.Jsoup
import play.api.Configuration
import play.api.http.Status._
import play.api.i18n.MessagesApi
import play.api.mvc._
import play.api.test.FakeRequest
import play.twirl.api.Html
import uk.gov.hmrc.http.UpstreamErrorResponse
import uk.gov.hmrc.play.bootstrap.frontend.http.ApplicationException

class ApplicationGlobalTest extends CommonPlaySpec with WithCommonFakeApplication {

  val fakedMessagesApi: MessagesApi = fakeApplication.injector.instanceOf[MessagesApi]
  val fakedConfiguration: Configuration = fakeApplication.injector.instanceOf[Configuration]
  implicit val appConfig: AppConfig = fakeApplication.injector.instanceOf[AppConfig]
  val ihtErrorTemplateView: iht_error_template = fakeApplication.injector.instanceOf[iht_error_template]
  val ihtNotFoundTemplateView: iht_not_found_template = fakeApplication.injector.instanceOf[iht_not_found_template]
  val registrationGenericErrorView: registration_generic_error = fakeApplication.injector.instanceOf[registration_generic_error]
  val applicationGenericErrorView: application_generic_error = fakeApplication.injector.instanceOf[application_generic_error]

  def fakeRequest(path: String) = FakeRequest("POST", path)

  class Setup {
    val errorHandler = new IHTErrorHandler(
      fakedConfiguration,
      ihtErrorTemplateView,
      ihtNotFoundTemplateView,
      registrationGenericErrorView,
      applicationGenericErrorView,
      fakedMessagesApi
    )
  }

  "Rendering internalServerErrorTemplate by causing an error" should {
    "on the registration journey" in new Setup {
      val request = fakeRequest("/registration/error").withCookies(new Cookie("PLAY_LANG", "en", Some(1), "", Some(""), true, true))
      lazy val template: Html = errorHandler.desInternalServerErrorTemplate(request)
      lazy val doc = Jsoup.parse(template.body)


      doc.getElementById("checklistLink").attr("href") shouldBe iht.controllers.registration.routes.RegistrationChecklistController.onPageLoad().url
    }

    "on the estate-report journey" in new Setup {
      val request = fakeRequest("/estate-report/error")
      val template = errorHandler.desInternalServerErrorTemplate(request)
      lazy val doc = Jsoup.parse(template.body)

      doc.getElementById("estateReportLink").attr("href") shouldBe iht.controllers.estateReports.routes.YourEstateReportsController.onPageLoad().url
    }

    "on the what-do-you-want-to-do journey" in new Setup {
      val request = fakeRequest("/what-do-you-want-to-do/error")
      val template = errorHandler.desInternalServerErrorTemplate(request)
      lazy val doc = Jsoup.parse(template.body)

      doc.select("article > div > p > a").attr("href") shouldBe "https://www.gov.uk/valuing-estate-of-someone-who-died"
    }
  }

  "Error Handler" should {
    "return INTERNAL_SERVER_ERROR on UpstreamErrorResponse" in new Setup {
      val exception = UpstreamErrorResponse("test", 502, 500)
      val result = errorHandler.resolveError(FakeRequest(), exception)

      result.header.status shouldBe INTERNAL_SERVER_ERROR
    }

    "return SEE_OTHER on Upstream4xxResponse" in new Setup {
      val exception = ApplicationException(Results.Redirect(CommonHelper.addFragmentIdentifier(
        iht.controllers.application.assets.routes.AssetsOverviewController.onPageLoad(),
        Some(appConfig.AppSectionPropertiesID))), "test")
      val result = errorHandler.resolveError(FakeRequest(), exception)

      result.header.status shouldBe SEE_OTHER
    }
  }


}

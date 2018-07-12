/*
 * Copyright 2018 HM Revenue & Customs
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

import iht.constants.IhtProperties.AppSectionPropertiesID
import iht.utils.CommonHelper
import org.jsoup.Jsoup
import play.api.mvc._
import play.api.test.FakeRequest
import play.api.test.Helpers._
import uk.gov.hmrc.http.{Upstream4xxResponse, Upstream5xxResponse}
import uk.gov.hmrc.play.frontend.controller.FrontendController
import uk.gov.hmrc.play.frontend.exceptions.ApplicationException
import uk.gov.hmrc.play.test.{UnitSpec, WithFakeApplication}

import scala.concurrent.Future

class ApplicationGlobalTest extends UnitSpec with WithFakeApplication {

  def fakeRequest(path: String) = FakeRequest("POST", path)

  class fakeController extends FrontendController {
    def onPageLoad: Action[AnyContent] = Action.async { implicit request =>
      Future.failed(new Exception("500 response returned from DES"))
    }
  }


  "Rendering internalServerErrorTemplate by causing an error" should {
    "on the registration journey" in {
      val request = fakeRequest("/registration/error").withCookies(new Cookie("PLAY_LANG", "en", Some(1), "", Some(""), true, true))
      lazy val template = ApplicationGlobal.desInternalServerErrorTemplate(request)
      lazy val doc = Jsoup.parse(template.body)


      doc.getElementById("checklistLink").attr("href") shouldBe iht.controllers.registration.routes.RegistrationChecklistController.onPageLoad().url

    }

    "on the estate-report journey" in {
      val request = fakeRequest("/estate-report/error")
      val template = ApplicationGlobal.desInternalServerErrorTemplate(request)
      lazy val doc = Jsoup.parse(template.body)

      doc.getElementById("estateReportLink").attr("href") shouldBe iht.controllers.estateReports.routes.YourEstateReportsController.onPageLoad().url

    }

    "on the what-do-you-want-to-do journey" in {
      val request = fakeRequest("/what-do-you-want-to-do/error")
      val template = ApplicationGlobal.desInternalServerErrorTemplate(request)
      lazy val doc = Jsoup.parse(template.body)

      doc.getElementsByClass("button").attr("href") shouldBe "https://www.gov.uk/valuing-estate-of-someone-who-died"

    }

  }

  "Error Handler" should {
    "return INTERNAL_SERVER_ERROR on Upstream5xxResponse" in {
      val exception = new Upstream5xxResponse("test", 502, 500)
      val result = ApplicationGlobal.resolveError(FakeRequest(), exception)

      result.header.status shouldBe INTERNAL_SERVER_ERROR
    }

    "return SEE_OTHER on Upstream4xxResponse" in {
      val exception = new ApplicationException("test", Results.Redirect(CommonHelper.addFragmentIdentifier(
        iht.controllers.application.assets.routes.AssetsOverviewController.onPageLoad(),
        Some(AppSectionPropertiesID))), "401")
      val result = ApplicationGlobal.resolveError(FakeRequest(), exception)

      result.header.status shouldBe SEE_OTHER
    }
  }


}

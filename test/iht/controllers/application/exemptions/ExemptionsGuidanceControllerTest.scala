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

package iht.controllers.application.exemptions

import iht.config.AppConfig
import iht.controllers.application.ApplicationControllerTest
import iht.testhelpers.CommonBuilder
import iht.views.HtmlSpec
import iht.views.html.application.exemption.exemptions_guidance
import org.jsoup.nodes.Element
import org.scalatest.BeforeAndAfter
import play.api.mvc.MessagesControllerComponents
import play.api.test.Helpers._
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController

/**
 * Created by jon on 23/07/15.
 */

class ExemptionsGuidanceControllerTest extends ApplicationControllerTest with HtmlSpec with BeforeAndAfter {

  protected abstract class TestController extends FrontendController(mockControllerComponents) with ExemptionsGuidanceController {
    override val cc: MessagesControllerComponents = mockControllerComponents
    override implicit val appConfig: AppConfig = mockAppConfig
    override val exemptionsGuidanceView: exemptions_guidance = app.injector.instanceOf[exemptions_guidance]
  }

  // Create controller object and pass in mock.
  def exemptionsGuidanceController = new TestController {
    override val cachingConnector = mockCachingConnector
    override val ihtConnector = mockIhtConnector
    override val authConnector = mockAuthConnector
  }

  def exemptionsGuidanceControllerNotAuthorised = new TestController {
    override val cachingConnector = mockCachingConnector
    override val ihtConnector = mockIhtConnector
    override val authConnector = mockAuthConnector
  }

  private def setupMocks = {
    val applicationDetails = CommonBuilder.buildApplicationDetails

    createMocksForApplication(mockCachingConnector,
      mockIhtConnector,
      appDetails = Some(applicationDetails),
      regDetails = registrationDetails,
      getAppDetails = true,
      storeAppDetailsInCache = true,
      saveAppDetails = true)
  }

  val registrationDetails = CommonBuilder.buildRegistrationDetails copy(
    deceasedDetails = Some(CommonBuilder.buildDeceasedDetails),
    ihtReference = Some("ABC123")
    )



  "Exemptions Guidance Controller" must {

    "redirect to ida login page on page load when user is not logged in" in {
      setupMocks

      val result = exemptionsGuidanceControllerNotAuthorised.onPageLoad("anIhtReference")(createFakeRequest(isAuthorised = false))
      status(result) must be(SEE_OTHER)
      redirectLocation(result) must be (Some(loginUrl))
    }

    "redirect to ida login page on submit when user is not logged in" in {
      setupMocks

      val result = exemptionsGuidanceControllerNotAuthorised.onSubmit("")(createFakeRequest(isAuthorised = false))
      status(result) must be(SEE_OTHER)
      redirectLocation(result) must be (Some(loginUrl))
    }

    "respond with OK on page load" in {
      setupMocks

      val result = exemptionsGuidanceController.onPageLoad("anIhtReference")(createFakeRequest(authRetrieveNino = false))
      status(result) must be(OK)
    }

    "respond with correct content on page load" in {
      setupMocks

      val result = exemptionsGuidanceController.onPageLoad("anIhtReference")(createFakeRequest(authRetrieveNino = false))
      status(result) must be(OK)

      val resultContent = contentAsString(result)

      resultContent must include(messagesApi("page.iht.application.exemptions.guidance.content1"))
      resultContent must include(messagesApi("page.iht.application.exemptions.guidance.content2"))
      resultContent must include(messagesApi("page.iht.application.exemptions.guidance.content2.linkText"))
      resultContent must include(messagesApi("page.iht.application.exemptions.guidance.content3"))
      resultContent must include(messagesApi("page.iht.application.exemptions.guidance.content4"))
      resultContent must include(messagesApi("iht.estateReport.exemptions.guidance.provideAssetsDetails"))
      resultContent must include(messagesApi("iht.estateReport.exemptions.guidance.debtsSubtracted"))
    }
  }

  "respond with correct url on form action" in {
    setupMocks

    val result = exemptionsGuidanceController.onPageLoad("anIhtReference")(createFakeRequest(authRetrieveNino = false))
    status(result) must be(OK)
    val doc = asDocument(contentAsString(result))
    val formElement: Element = doc.getElementsByTag("form").first
    val url = formElement.attr("action")

    url mustBe iht.controllers.application.exemptions.routes.ExemptionsGuidanceController.onSubmit("anIhtReference").url
  }
}

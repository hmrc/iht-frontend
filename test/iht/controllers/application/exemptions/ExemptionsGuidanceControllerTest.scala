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

package iht.controllers.application.exemptions

import iht.connector.{CachingConnector, IhtConnector}
import iht.controllers.application.ApplicationControllerTest
import iht.testhelpers.CommonBuilder
import iht.testhelpers.MockObjectBuilder._
import iht.views.HtmlSpec
import org.jsoup.nodes.Element
import org.scalatest.BeforeAndAfter
import play.api.i18n.Messages
import play.api.test.Helpers._

/**
 * Created by jon on 23/07/15.
 */

class ExemptionsGuidanceControllerTest extends ApplicationControllerTest with HtmlSpec with BeforeAndAfter {

  var mockCachingConnector = mock[CachingConnector]
  val mockIhtConnector = mock[IhtConnector]

  // Create controller object and pass in mock.
  def exemptionsGuidanceController = new ExemptionsGuidanceController {
    override val cachingConnector = mockCachingConnector
    override val ihtConnector = mockIhtConnector
    override val authConnector = createFakeAuthConnector(isAuthorised = true)
    override val isWhiteListEnabled = false
  }

  def exemptionsGuidanceControllerNotAuthorised = new ExemptionsGuidanceController {
    override val cachingConnector = mockCachingConnector
    override val ihtConnector = mockIhtConnector
    override val authConnector = createFakeAuthConnector(isAuthorised = false)
    override val isWhiteListEnabled = false
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

  before {
    mockCachingConnector = mock[CachingConnector]
  }

  "Exemptions Guidance Controller" must {

    "redirect to ida login page on page load when user is not logged in" in {
      setupMocks

      val result = exemptionsGuidanceControllerNotAuthorised.onPageLoad("anIhtReference")(createFakeRequest())
      status(result) should be(SEE_OTHER)
      redirectLocation(result) should be (Some(loginUrl))
    }

    "redirect to ida login page on submit when user is not logged in" in {
      setupMocks

      val result = exemptionsGuidanceControllerNotAuthorised.onSubmit("")(createFakeRequest())
      status(result) should be(SEE_OTHER)
      redirectLocation(result) should be (Some(loginUrl))
    }

    "respond with OK on page load" in {
      setupMocks

      val result = exemptionsGuidanceController.onPageLoad("anIhtReference")(createFakeRequest())
      status(result) should be(OK)
    }

    "respond with correct content on page load" in {
      setupMocks

      val result = exemptionsGuidanceController.onPageLoad("anIhtReference")(createFakeRequest())
      status(result) should be(OK)

      val resultContent = contentAsString(result)

      resultContent should include(Messages("page.iht.application.exemptions.guidance.content1"))
      resultContent should include(Messages("page.iht.application.exemptions.guidance.content2"))
      resultContent should include(Messages("page.iht.application.exemptions.guidance.content2.linkText"))
      resultContent should include(Messages("page.iht.application.exemptions.guidance.content3"))
      resultContent should include(Messages("page.iht.application.exemptions.guidance.content4"))
      resultContent should include(Messages("iht.estateReport.exemptions.guidance.provideAssetsDetails"))
      resultContent should include(Messages("iht.estateReport.exemptions.guidance.debtsSubtracted"))
    }
  }

  "respond with correct url on form action" in {
    setupMocks

    val result = exemptionsGuidanceController.onPageLoad("anIhtReference")(createFakeRequest())
    status(result) should be(OK)
    val doc = asDocument(contentAsString(result))
    val formElement: Element = doc.getElementsByTag("form").first
    val url = formElement.attr("action")

    url shouldBe iht.controllers.application.exemptions.routes.ExemptionsGuidanceController.onSubmit("anIhtReference").url
  }
}

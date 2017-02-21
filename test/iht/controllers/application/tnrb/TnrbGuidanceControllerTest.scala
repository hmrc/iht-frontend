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

import iht.connector.{CachingConnector, IhtConnector}
import iht.controllers.application.ApplicationControllerTest
import iht.testhelpers.MockObjectBuilder._
import iht.testhelpers.{CommonBuilder, TestHelper}
import iht.views.HtmlSpec
import org.jsoup.nodes.Element
import play.api.i18n.Messages.Implicits._
import play.api.i18n.MessagesApi
import play.api.test.Helpers._

class TnrbGuidanceControllerTest  extends ApplicationControllerTest with HtmlSpec {

  override implicit val messagesApi: MessagesApi = app.injector.instanceOf[MessagesApi]
  val mockCachingConnector = mock[CachingConnector]
  val mockIhtConnector = mock[IhtConnector]

  def controller = new TnrbGuidanceController {
    override val authConnector = createFakeAuthConnector(isAuthorised=true)
    override val cachingConnector = mockCachingConnector
    override val ihtConnector = mockIhtConnector
  }

  def controllerNotAuthorised = new TnrbGuidanceController {
    override val authConnector = createFakeAuthConnector(isAuthorised=false)
    override val cachingConnector = mockCachingConnector
    override val ihtConnector = mockIhtConnector
  }

  def createMocksForRegistrationAndApplicationWithMaritalStatus(maritalStatus:String) = {
    val deceasedDetails = CommonBuilder.buildDeceasedDetails.copy(
      maritalStatus = Some(maritalStatus))
    val registrationDetails = CommonBuilder.buildRegistrationDetails.copy(deceasedDetails = Some(deceasedDetails))

    createMockToGetRegDetailsFromCache(mockCachingConnector, Some(registrationDetails))
    createMockToSaveApplicationDetails(mockIhtConnector)
  }

  "TnrbGuidanceController" must {

    "redirect to login page onPageLoad if the user is not logged in" in {
      val result = controller.onPageLoad(createFakeRequest(isAuthorised = false))
      status(result) should be(SEE_OTHER)
      redirectLocation(result) should be (Some(loginUrl))
    }

    "respond with OK on page load" in {
      createMocksForRegistrationAndApplicationWithMaritalStatus(TestHelper.MaritalStatusWidowed)

      val result = controller.onPageLoad (createFakeRequest())
      status(result) shouldBe OK
      val content = contentAsString(result)
      val doc = asDocument(content)
      assertEqualsValue(doc, "h1",
        messagesApi("iht.estateReport.tnrb.increasingIHTThreshold"))
    }

    "respond with continue link with correct content in on page load when deceased was widowed" in {
      createMocksForRegistrationAndApplicationWithMaritalStatus(TestHelper.MaritalStatusWidowed)

      val result = controller.onPageLoad(createFakeRequest())
      status(result) shouldBe OK
      val content = contentAsString(result)
      val doc = asDocument(content)

      val link: Element = doc.getElementById("continue-to-increasing-threshold-link")
      link.text() shouldBe messagesApi("page.iht.application.tnrb.guidance.continueLink.text")
    }

    "respond with correct link (deceased spouse date of death page) in on page load when deceased was widowed" in {
      createMocksForRegistrationAndApplicationWithMaritalStatus(TestHelper.MaritalStatusWidowed)

      val result = controller.onPageLoad(createFakeRequest())
      status(result) shouldBe OK
      val content = contentAsString(result)
      val doc = asDocument(content)

      val link: Element = doc.getElementById("continue-to-increasing-threshold-link")
      link.attr("href") shouldBe iht.controllers.application.tnrb.routes.DeceasedWidowCheckDateController.onPageLoad().url
    }

    "respond with correct link (TNRB Widow check page) in on page load when deceased was married" in {
      createMocksForRegistrationAndApplicationWithMaritalStatus(TestHelper.MaritalStatusMarried)

      val result = controller.onPageLoad(createFakeRequest())
      status(result) shouldBe OK
      val content = contentAsString(result)
      val doc = asDocument(content)

      val link: Element = doc.getElementById("continue-to-increasing-threshold-link")
      link.attr("href") shouldBe iht.controllers.application.tnrb.routes.DeceasedWidowCheckQuestionController.onPageLoad().url
    }

    "respond with correct link (TNRB Widow check page) in on page load when deceased was divorced" in {
      createMocksForRegistrationAndApplicationWithMaritalStatus(TestHelper.MaritalStatusDivorced)

      val result = controller.onPageLoad(createFakeRequest())
      status(result) shouldBe OK
      val content = contentAsString(result)
      val doc = asDocument(content)

      val link: Element = doc.getElementById("continue-to-increasing-threshold-link")
      link.attr("href") shouldBe iht.controllers.application.tnrb.routes.DeceasedWidowCheckQuestionController.onPageLoad().url
    }

    "respond with correct link (TNRB Widow check page) in on page load when deceased was single" in {
      createMocksForRegistrationAndApplicationWithMaritalStatus(TestHelper.MaritalStatusSingle)

      a [RuntimeException] shouldBe thrownBy {
        await(controller.onPageLoad(createFakeRequest()))
      }
    }
  }
}

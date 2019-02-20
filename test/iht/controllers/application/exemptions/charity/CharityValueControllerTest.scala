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

package iht.controllers.application.exemptions.charity

import iht.controllers.application.ApplicationControllerTest
import iht.forms.ApplicationForms._
import iht.models.application.exemptions.Charity
import iht.testhelpers.MockObjectBuilder._
import iht.testhelpers.{CommonBuilder, MockFormPartialRetriever}
import org.scalatest.BeforeAndAfter
import play.api.test.Helpers._
import uk.gov.hmrc.play.partials.FormPartialRetriever


class CharityValueControllerTest extends ApplicationControllerTest with BeforeAndAfter {


  val defaultCharity = Charity(Some("1"), Some("A Charity 1"), Some("7866667X"), None)
  val referrerURL = "localhost:9070"



  def assetsLeftToCharityValueController = new CharityValueController {
    override val authConnector = mockAuthConnector
    override val cachingConnector = mockCachingConnector
    override val ihtConnector = mockIhtConnector
    override implicit val formPartialRetriever: FormPartialRetriever = MockFormPartialRetriever
  }

  def assetsLeftToCharityValueControllerNotAuthorised = new CharityValueController {
    override val authConnector = mockAuthConnector
    override val cachingConnector = mockCachingConnector
    override val ihtConnector = mockIhtConnector
    override implicit val formPartialRetriever: FormPartialRetriever = MockFormPartialRetriever
  }

  def createMocksForApplicationWithCharity = {
    val ad = CommonBuilder.buildApplicationDetails copy (charities = Seq(defaultCharity))
    createMocksForApplication(mockCachingConnector,
      mockIhtConnector,
      appDetails = Some(ad),
      getAppDetails = true,
      saveAppDetails = true,
      storeAppDetailsInCache = true)
  }

  "AssetsLeftToCharityValueController" must {
    "redirect to login page on PageLoad if the user is not logged in when loading" in {
      val result = assetsLeftToCharityValueControllerNotAuthorised.onEditPageLoad("1")(createFakeRequest(isAuthorised = false))
      status(result) must be(SEE_OTHER)
      redirectLocation(result) must be(Some(loginUrl))
    }

    "redirect to login page on PageLoad if the user is not logged on page submission" in {
      val result = assetsLeftToCharityValueControllerNotAuthorised.onEditSubmit("1")(createFakeRequest(isAuthorised = false))
      status(result) must be(SEE_OTHER)
      redirectLocation(result) must be(Some(loginUrl))
    }

    "redirect to login page on Submit if the user is not logged in" in {
      val result = assetsLeftToCharityValueControllerNotAuthorised.onEditSubmit("1")(createFakeRequest(isAuthorised = false))
      status(result) must be(SEE_OTHER)
      redirectLocation(result) must be(Some(loginUrl))
    }

    "return a view containing the section title on edit page load" in {
      createMocksForApplicationWithCharity

      val result = assetsLeftToCharityValueController.onEditPageLoad("1")(createFakeRequest())
      status(result) must be(OK)
      contentAsString(result) must include(
        messagesApi("page.iht.application.exemptions.charityValue.sectionTitle"))
    }

    "return a view containing the section title on page load" in {
      createMocksForApplicationWithCharity

      val result = assetsLeftToCharityValueController.onPageLoad()(createFakeRequest(authRetrieveNino = false))
      status(result) must be(OK)
      contentAsString(result) must include(
        messagesApi("page.iht.application.exemptions.charityValue.sectionTitle"))
    }

    "if the charity with given id does not exist - load should respond with a server error" in {
      createMocksForApplicationWithCharity

      a[RuntimeException] mustBe thrownBy {
        await(assetsLeftToCharityValueController.onEditPageLoad("897776")(createFakeRequest()))
      }
    }

    "If the charity already has a totalValue it is displayed on teh page" in {
      val ad = CommonBuilder.buildApplicationDetails copy (charities = Seq(Charity(Some("1"), None, None, Some(1000))))
      createMocksForApplication(mockCachingConnector,
        mockIhtConnector,
        appDetails = Some(ad),
        getAppDetails = true,
        saveAppDetails = true,
        storeAppDetailsInCache = true)

      val result = assetsLeftToCharityValueController.onEditPageLoad("1")(createFakeRequest())
      status(result) mustBe (OK)
      contentAsString(result) must include("1000")
    }

    "when given a valid charity id and a value by the user on the form, " +
      "the page should redirect to the next one on submission" in {
      createMocksForApplicationWithCharity
      val valueForm = assetsLeftToCharityValueForm.fill(defaultCharity copy (totalValue = Some(1000)))
      val request = createFakeRequestWithReferrerWithBody(referrerURL = referrerURL,
        host = "localhost:9070",
        data = valueForm.data.toSeq)

      val result = assetsLeftToCharityValueController.onEditSubmit("1")(request)

      status(result) mustBe(SEE_OTHER)
    }

    "when given a valid charity id and a value by the user on the form, " +
      "the page should redirect to the next one on submission when not in edit mode" in {
      createMocksForApplicationWithCharity
      val valueForm = assetsLeftToCharityValueForm.fill(defaultCharity copy (totalValue = Some(1000)))
      val request = createFakeRequestWithReferrerWithBody(referrerURL = referrerURL,
        host = "localhost:9070",
        data = valueForm.data.toSeq)

      val result = assetsLeftToCharityValueController.onSubmit()(request)

      status(result) mustBe(SEE_OTHER)
    }

    "when given a valid charity id and a value by the user on the form, " +
      "the application details must be updated on submission" in {
      createMocksForApplicationWithCharity
      val valueForm = assetsLeftToCharityValueForm.fill(defaultCharity copy (totalValue = Some(1000)))
      val request = createFakeRequestWithReferrerWithBody(referrerURL = referrerURL,
        host = "localhost:9070",
        data = valueForm.data.toSeq)

      val result = assetsLeftToCharityValueController.onEditSubmit("1")(request)

      status(result) mustBe(SEE_OTHER)
      val capturedValue = verifyAndReturnSavedApplicationDetails(mockIhtConnector)
      capturedValue.charities.length mustBe 1
      capturedValue.charities(0).totalValue mustBe Some(1000)
    }

    "when given a valid charity id and a value by the user on the form, " +
      "the charity name and number must not be updated on submission" in {
      createMocksForApplicationWithCharity
      val valueForm = assetsLeftToCharityValueForm.fill(defaultCharity copy (totalValue = Some(1000)))
      val request = createFakeRequestWithReferrerWithBody(referrerURL = referrerURL,
        host = "localhost:9070",
        data = valueForm.data.toSeq)

      val result = assetsLeftToCharityValueController.onEditSubmit("1")(request)

      status(result) mustBe(SEE_OTHER)
      val capturedValue = verifyAndReturnSavedApplicationDetails(mockIhtConnector)
      capturedValue.charities(0).name mustBe defaultCharity.name
      capturedValue.charities(0).number mustBe defaultCharity.number
    }

    behave like controllerOnPageLoadWithNoExistingRegistrationDetails(mockCachingConnector,
      assetsLeftToCharityValueController.onPageLoad(createFakeRequest(authRetrieveNino = false)))
  }
}

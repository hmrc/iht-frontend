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

package iht.controllers.application.exemptions.charity

import iht.connector.{CachingConnector, IhtConnector}
import iht.controllers.application.ApplicationControllerTest
import iht.forms.ApplicationForms._
import iht.models.application.exemptions.Charity
import iht.testhelpers.CommonBuilder
import iht.testhelpers.MockObjectBuilder._
import org.scalatest.BeforeAndAfter
import play.api.i18n.Messages
import play.api.i18n.Messages.Implicits._
import play.api.Play.current
import play.api.test.Helpers._


class CharityValueControllerTest extends ApplicationControllerTest with BeforeAndAfter {
  implicit val messages: Messages = app.injector.instanceOf[Messages]
  val mockCachingConnector = mock[CachingConnector]
  var mockIhtConnector = mock[IhtConnector]
  val defaultCharity = Charity(Some("1"), Some("A Charity 1"), Some("7866667X"), None)
  val referrerURL = "localhost:9070"

  before {
    mockIhtConnector = mock[IhtConnector]
  }

  def assetsLeftToCharityValueController = new CharityValueController {
    override val authConnector = createFakeAuthConnector(isAuthorised = true)
    override val cachingConnector = mockCachingConnector
    override val ihtConnector = mockIhtConnector
  }

  def assetsLeftToCharityValueControllerNotAuthorised = new CharityValueController {
    override val authConnector = createFakeAuthConnector(isAuthorised = false)
    override val cachingConnector = mockCachingConnector
    override val ihtConnector = mockIhtConnector
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
      val result = assetsLeftToCharityValueControllerNotAuthorised.onEditPageLoad("1")(createFakeRequest())
      status(result) should be(SEE_OTHER)
      redirectLocation(result) should be(Some(loginUrl))
    }

    "redirect to login page on PageLoad if the user is not logged on page submission" in {
      val result = assetsLeftToCharityValueControllerNotAuthorised.onEditSubmit("1")(createFakeRequest())
      status(result) should be(SEE_OTHER)
      redirectLocation(result) should be(Some(loginUrl))
    }

    "redirect to login page on Submit if the user is not logged in" in {
      val result = assetsLeftToCharityValueControllerNotAuthorised.onEditSubmit("1")(createFakeRequest())
      status(result) should be(SEE_OTHER)
      redirectLocation(result) should be(Some(loginUrl))
    }

    "return a view containing the section title on page load" in {
      createMocksForApplicationWithCharity

      val result = assetsLeftToCharityValueController.onEditPageLoad("1")(createFakeRequest())
      status(result) should be(OK)
      contentAsString(result) should include(
        Messages("page.iht.application.exemptions.charityValue.sectionTitle"))
    }

    "return a view containing the currency symbol" in {
      createMocksForApplicationWithCharity

      val result = assetsLeftToCharityValueController.onEditPageLoad("1")(createFakeRequest())
      status(result) should be(OK)
      contentAsString(result) should include("&pound;")
    }

    "return a view containing the save and continue button" in {
      createMocksForApplicationWithCharity

      val result = assetsLeftToCharityValueController.onEditPageLoad("1")(createFakeRequest())
      status(result) should be(OK)
      contentAsString(result) should include(Messages("iht.saveAndContinue"))
    }

    "return a view containing a link with the text to 'Return to add a charity'" in {
      createMocksForApplicationWithCharity

      val result = assetsLeftToCharityValueController.onEditPageLoad("1")(createFakeRequest())
      status(result) should be(OK)
      contentAsString(result) should include(
        Messages("iht.estateReport.exemptions.charities.returnToAddACharity"))
    }

    "return a view containing a link with the href pointing to the overview'" in {
      pending
      val result = assetsLeftToCharityValueController.onEditPageLoad("1")(createFakeRequest())
      status(result) should be(OK)
      // Contents should include the route to CharityOverviewController when it exists
    }

    "if the charity with given id does not exist - load should respond with a server error" in {
      createMocksForApplicationWithCharity

      a[RuntimeException] shouldBe thrownBy {
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
      status(result) shouldBe (OK)
      contentAsString(result) should include("1000")
    }

    "when given a valid charity id and a value by the user on the form, " +
      "the page should redirect to the next one on submission" in {
      createMocksForApplicationWithCharity
      val valueForm = assetsLeftToCharityValueForm.fill(defaultCharity copy (totalValue = Some(1000)))
      val request = createFakeRequestWithReferrerWithBody(referrerURL = referrerURL,
                                                          host = "localhost:9070",
                                                          data = valueForm.data.toSeq)

      val result = assetsLeftToCharityValueController.onEditSubmit("1")(request)

      status(result) shouldBe(SEE_OTHER)
    }

    "when given a valid charity id and a value by the user on the form, " +
      "the application details should be updated on submission" in {
      createMocksForApplicationWithCharity
      val valueForm = assetsLeftToCharityValueForm.fill(defaultCharity copy (totalValue = Some(1000)))
      val request = createFakeRequestWithReferrerWithBody(referrerURL = referrerURL,
                                                          host = "localhost:9070",
                                                          data = valueForm.data.toSeq)

      val result = assetsLeftToCharityValueController.onEditSubmit("1")(request)

      status(result) shouldBe(SEE_OTHER)
      val capturedValue = verifyAndReturnSavedApplicationDetails(mockIhtConnector)
      capturedValue.charities.length shouldBe 1
      capturedValue.charities(0).totalValue shouldBe Some(1000)
    }

    "when given a valid charity id and a value by the user on the form, " +
      "the charity name and number should not be updated on submission" in {
      createMocksForApplicationWithCharity
      val valueForm = assetsLeftToCharityValueForm.fill(defaultCharity copy (totalValue = Some(1000)))
      val request = createFakeRequestWithReferrerWithBody(referrerURL = referrerURL,
                                                          host = "localhost:9070",
                                                          data = valueForm.data.toSeq)

      val result = assetsLeftToCharityValueController.onEditSubmit("1")(request)

      status(result) shouldBe(SEE_OTHER)
      val capturedValue = verifyAndReturnSavedApplicationDetails(mockIhtConnector)
      capturedValue.charities(0).name shouldBe defaultCharity.name
      capturedValue.charities(0).number shouldBe defaultCharity.number
    }
  }
}

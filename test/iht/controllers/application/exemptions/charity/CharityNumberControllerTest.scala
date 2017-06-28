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
import iht.models.application.ApplicationDetails
import iht.models.application.exemptions.Charity
import iht.testhelpers.CommonBuilder
import iht.testhelpers.MockObjectBuilder._
import iht.utils.CommonHelper
import org.scalatest.BeforeAndAfter
import play.api.i18n.Messages
import play.api.i18n.Messages.Implicits._
import play.api.Play.current
import play.api.test.Helpers._

class CharityNumberControllerTest extends ApplicationControllerTest with BeforeAndAfter {

  var mockCachingConnector = mock[CachingConnector]
  var mockIhtConnector = mock[IhtConnector]

  def charityNumberController = new CharityNumberController(messagesApi) {
    override val authConnector = createFakeAuthConnector(isAuthorised = true)
    override val cachingConnector = mockCachingConnector
    override val ihtConnector = mockIhtConnector
  }

  def charityNumberControllerNotAuthorised = new CharityNumberController(messagesApi) {
    override val authConnector = createFakeAuthConnector(isAuthorised = false)
    override val cachingConnector = mockCachingConnector
    override val ihtConnector = mockIhtConnector
  }

  val charity1 = CommonBuilder.buildCharity.copy(id = Some("1"),
    name = Some("A Charity 1"),
    number = Some("1111111"),
    totalValue = Some(45.45))

  val charity2 = CommonBuilder.buildCharity.copy(
    id = Some("2"),
    name = Some("A Charity 2"),
    number = Some("2222222"),
    totalValue = Some(46.45)
  )

  val charity3 = CommonBuilder.buildCharity.copy(
    id = Some("3"),
    name = Some("A Charity 3"),
    number = Some("3333333"),
    totalValue = Some(47.45)
  )

  val applicationDetailsTwoCharities = CommonBuilder.buildApplicationDetails copy (charities
    = Seq(charity1, charity2))

  before {
    mockCachingConnector = mock[CachingConnector]
    mockIhtConnector = mock[IhtConnector]
  }

  "CharityNumberControllerTest" must {
    "redirect to login page on PageLoad if the user is not logged in" in {
      val result = charityNumberControllerNotAuthorised.onPageLoad(createFakeRequest())
      status(result) should be(SEE_OTHER)
      redirectLocation(result) should be(Some(loginUrl))
    }

    "redirect to login page on Submit if the user is not logged in" in {
      val result = charityNumberControllerNotAuthorised.onSubmit(createFakeRequest())
      status(result) should be(SEE_OTHER)
      redirectLocation(result) should be(Some(loginUrl))
    }

    "respond with OK on page load with correct content" in {
      createMockForRegistration(mockCachingConnector, getRegDetailsFromCache = true)
      createMocksForApplication(mockCachingConnector,
        mockIhtConnector,
        appDetails = Some(applicationDetailsTwoCharities),
        getAppDetails = true)

      val result = charityNumberController.onPageLoad(createFakeRequest())
      status(result) shouldBe OK
      contentAsString(result) should include(messagesApi("iht.estateReport.exemptions.charities.charityNo.question"))
    }

    "if the charity with given id does not exist - load should respond with a server error" in {
      createMocksForApplication(mockCachingConnector,
        mockIhtConnector,
        appDetails = Some(applicationDetailsTwoCharities),
        getAppDetails = true)

      a [RuntimeException] shouldBe thrownBy {
        await(charityNumberController.onEditPageLoad("897776")(createFakeRequest()))
      }
    }

    "update the existing Charity in ApplicationDetails" in {
      val expectedCharity = Charity(
        id = Some("2"),
        name = Some("A Charity 2"),
        number = Some("3333333"),
        totalValue = Some(46.45)
      )
      val expectedResult = (CommonBuilder.buildApplicationDetails copy (charities = Seq(charity1, expectedCharity)), Some("2"))
      val result = charityNumberController.updateApplicationDetails(applicationDetailsTwoCharities, Some("2"), charity3)
      result shouldBe expectedResult
    }

    "update new charity in ApplicationDetails" in {
      val newCharity = Charity(
        id = None,
        name = Some("A Charity 2"),
        number = Some("3333333"),
        totalValue = Some(46.45)
      )

      val expectedNewCharity = Charity(
        id = Some("3"),
        name = Some("A Charity 2"),
        number = Some("3333333"),
        totalValue = Some(46.45)
      )

      val expectedResult = (CommonBuilder.buildApplicationDetails copy
        (charities = Seq(charity1, charity2, expectedNewCharity)), Some("3"))
      val result = charityNumberController.updateApplicationDetails(applicationDetailsTwoCharities, None, newCharity)
      result shouldBe expectedResult
    }

    "save application and go to Charity Details Overview page on submit" in {
      val appDetails = CommonBuilder.buildApplicationDetails.copy(charities = Seq())

      createMocksForApplication(mockCachingConnector,
        mockIhtConnector,
        appDetails = Some(appDetails),
        getAppDetails = true,
        saveAppDetails = true,
        storeAppDetailsInCache = true)

      val filledForm = charityNumberForm.fill(CommonBuilder.buildCharity.copy(number = Some("123456")))
      implicit val request = createFakeRequest().withFormUrlEncodedBody(filledForm.data.toSeq: _*)

      val result = charityNumberController.onSubmit(request)
      status(result) shouldBe SEE_OTHER
    }

    "show Bad Request if the form is invalid" in {
      val appDetails = CommonBuilder.buildApplicationDetails.copy(charities = Seq())

      createMocksForApplication(mockCachingConnector,
        mockIhtConnector,
        appDetails = Some(appDetails),
        getAppDetails = true,
        saveAppDetails = true,
        storeAppDetailsInCache = true)

      val filledForm = charityNumberForm.fill(CommonBuilder.buildCharity.copy(number = None))
      implicit val request = createFakeRequest().withFormUrlEncodedBody(filledForm.data.toSeq: _*)

      val result = charityNumberController.onSubmit(request)
      status(result) shouldBe BAD_REQUEST
    }

    "save application, update charity and  go to Charity Details Overview page on submit in edit mode" in {
      val mockIhtConnectorTemp: IhtConnector = mock[IhtConnector]
      val mockCachingConnectorTemp: CachingConnector = mock[CachingConnector]
      def charityNumberControllerTemp = new CharityNumberController(messagesApi) {
        override val authConnector = createFakeAuthConnector(isAuthorised = true)
        override val cachingConnector = mockCachingConnectorTemp
        override val ihtConnector = mockIhtConnectorTemp
      }

      lazy val charity1 = CommonBuilder.buildCharity.copy(
        id = Some("1"),
        name = Some("A Charity 1"),
        number = Some("1111111"),
        totalValue = Some(45.45)
      )

      lazy val charity2 = CommonBuilder.buildCharity.copy(
        id = Some("2"),
        name = Some("A Charity 2"),
        number = Some("2222222"),
        totalValue = Some(46.45)
      )

      lazy val appDetails = CommonBuilder.buildApplicationDetails.copy(charities = Seq(charity1, charity2))

      createMockForRegistration(mockCachingConnectorTemp,
        regDetails = Some(CommonBuilder.buildRegistrationDetails),
        getRegDetailsFromCache = true,
        getExistingRegDetailsFromCache = true
      )

      createMocksForApplication(mockCachingConnectorTemp,
        mockIhtConnectorTemp,
        appDetails = Some(appDetails),
        getAppDetails = true,
        saveAppDetails = true,
        storeAppDetailsInCache = true)

      val charityChanged = charity2 copy (number = Some("3333333"))
      val filledCharityNumberForm = charityNumberForm.fill(charityChanged)
      implicit val request = createFakeRequest().withFormUrlEncodedBody(filledCharityNumberForm.data.toSeq: _*)

      val result = await(charityNumberControllerTemp.onEditSubmit("2")(request))

      val appDetailsBeforeSave: ApplicationDetails = verifyAndReturnSavedApplicationDetails(mockIhtConnectorTemp)
      val charityAfterSave: Option[Charity] = appDetailsBeforeSave.charities.find(_.id == Some("2"))
      charityAfterSave shouldBe Some(charityChanged)
      status(result) shouldBe SEE_OTHER
    }

    behave like controllerOnPageLoadWithNoExistingRegistrationDetails(mockCachingConnector,
      charityNumberController.onPageLoad(createFakeRequest()))
  }
}

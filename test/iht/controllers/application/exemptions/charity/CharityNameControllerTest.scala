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
import play.api.test.Helpers._

class CharityNameControllerTest extends ApplicationControllerTest with BeforeAndAfter {

  var mockCachingConnector = mock[CachingConnector]
  var mockIhtConnector = mock[IhtConnector]

  def charityNameController = new CharityNameController {
    override val authConnector = createFakeAuthConnector(isAuthorised = true)
    override val cachingConnector = mockCachingConnector
    override val ihtConnector = mockIhtConnector
  }

  def charityNameControllerNotAuthorised = new CharityNameController {
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

  "CharityNameControllerTest" must {
    "redirect to login page on PageLoad if the user is not logged in" in {
      val result = charityNameControllerNotAuthorised.onPageLoad(createFakeRequest())
      status(result) should be(SEE_OTHER)
      redirectLocation(result) should be(Some(loginUrl))
    }

    "redirect to login page on Submit if the user is not logged in" in {
      val result = charityNameControllerNotAuthorised.onSubmit(createFakeRequest())
      status(result) should be(SEE_OTHER)
      redirectLocation(result) should be(Some(loginUrl))
    }

    "respond with OK on page load with correct content" in {
      createMockForRegistration(mockCachingConnector, getRegDetailsFromCache = true)
      createMocksForApplication(mockCachingConnector,
        mockIhtConnector,
        appDetails = Some(applicationDetailsTwoCharities),
        getAppDetails = true)

      val result = charityNameController.onPageLoad(createFakeRequest())
      status(result) shouldBe OK
      contentAsString(result) should include(Messages("page.iht.application.exemptions.charityName.sectionTitle"))
      contentAsString(result) should include(Messages("iht.saveAndContinue"))
    }

    "respond with OK on page load and correct charity id for first charity" in {
      createMocksForApplication(mockCachingConnector,
        mockIhtConnector,
        appDetails = Some(applicationDetailsTwoCharities),
        getAppDetails = true)

      val result = charityNameController.onEditPageLoad("1")(createFakeRequest())
      status(result) shouldBe OK
      contentAsString(result) should include(CommonHelper.getOrException(charity1.name))
    }

    "respond with OK on page load and correct charity id for second charity" in {
      createMocksForApplication(mockCachingConnector,
        mockIhtConnector,
        appDetails = Some(applicationDetailsTwoCharities),
        getAppDetails = true)

      val result = charityNameController.onEditPageLoad("2")(createFakeRequest())
      status(result) shouldBe OK
      contentAsString(result) should include(CommonHelper.getOrException(charity2.name))
    }

    "update the existing Charity in ApplicationDetails" in {
      val expectedCharity = Charity(
        id = Some("2"),
        name = Some("A Charity 3"),
        number = Some("2222222"),
        totalValue = Some(46.45)
      )
      val expectedResult = (CommonBuilder.buildApplicationDetails copy (charities = Seq(charity1, expectedCharity)), Some("2"))
      val result = charityNameController.updateApplicationDetails(applicationDetailsTwoCharities, Some("2"), charity3)
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

      val filledForm = charityNameForm.fill(CommonBuilder.buildCharity.copy(name = Some("A Charity 2")))
      implicit val request = createFakeRequest().withFormUrlEncodedBody(filledForm.data.toSeq: _*)

      val result = charityNameController.onSubmit(request)
      //TODO: Replace with the Charity Details Overview page URL once that is done
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

      val filledForm = charityNameForm.fill(CommonBuilder.buildCharity.copy(name = Some("")))
      implicit val request = createFakeRequest().withFormUrlEncodedBody(filledForm.data.toSeq: _*)

      val result = charityNameController.onSubmit(request)
      status(result) shouldBe BAD_REQUEST
    }

    "save application, update charity and  go to Charity Details Overview page on submit in edit mode" in {
      val mockIhtConnectorTemp: IhtConnector = mock[IhtConnector]
      val mockCachingConnectorTemp: CachingConnector = mock[CachingConnector]
      def charityNameControllerTemp = new CharityNameController {
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

      val charityChanged = charity2 copy (name = Some("Another Charity 2"))
      val filledCharityNameForm = charityNameForm.fill(charityChanged)
      implicit val request = createFakeRequest().withFormUrlEncodedBody(filledCharityNameForm.data.toSeq: _*)

      val result = await(charityNameControllerTemp.onEditSubmit("2")(request))

      val appDetailsBeforeSave: ApplicationDetails = verifyAndReturnSavedApplicationDetails(mockIhtConnectorTemp)
      val charityAfterSave: Option[Charity] = appDetailsBeforeSave.charities.find(_.id == Some("2"))
      charityAfterSave shouldBe Some(charityChanged)
      //TODO: Replace with the Charity Details Overview page URL once that is done
      status(result) shouldBe SEE_OTHER
    }
  }
}

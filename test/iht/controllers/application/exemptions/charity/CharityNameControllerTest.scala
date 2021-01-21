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

package iht.controllers.application.exemptions.charity

import iht.config.AppConfig
import iht.connector.{CachingConnector, IhtConnector}
import iht.controllers.application.ApplicationControllerTest
import iht.forms.ApplicationForms._
import iht.models.application.ApplicationDetails
import iht.models.application.exemptions.Charity
import iht.testhelpers.{CommonBuilder, MockFormPartialRetriever}
import org.scalatest.BeforeAndAfter
import play.api.mvc.MessagesControllerComponents
import play.api.test.Helpers._
import uk.gov.hmrc.play.bootstrap.controller.FrontendController
import uk.gov.hmrc.play.partials.FormPartialRetriever

class CharityNameControllerTest extends ApplicationControllerTest with BeforeAndAfter {

  protected abstract class TestController extends FrontendController(mockControllerComponents) with CharityNameController {
    override val cc: MessagesControllerComponents = mockControllerComponents
    override implicit val appConfig: AppConfig = mockAppConfig
  }

  def charityNameController = new TestController {
    override val authConnector = mockAuthConnector
    override val cachingConnector = mockCachingConnector
    override val ihtConnector = mockIhtConnector
    override implicit val formPartialRetriever: FormPartialRetriever = MockFormPartialRetriever
  }

  def charityNameControllerNotAuthorised = new TestController {
    override val authConnector = mockAuthConnector
    override val cachingConnector = mockCachingConnector
    override val ihtConnector = mockIhtConnector
    override implicit val formPartialRetriever: FormPartialRetriever = MockFormPartialRetriever
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



  "CharityNameControllerTest" must {
    "redirect to login page on PageLoad if the user is not logged in" in {
      val result = charityNameControllerNotAuthorised.onPageLoad(createFakeRequest(isAuthorised = false, authRetrieveNino = false))
      status(result) must be(SEE_OTHER)
      redirectLocation(result) must be(Some(loginUrl))
    }

    "redirect to login page on Submit if the user is not logged in" in {
      val result = charityNameControllerNotAuthorised.onSubmit(createFakeRequest(isAuthorised = false))
      status(result) must be(SEE_OTHER)
      redirectLocation(result) must be(Some(loginUrl))
    }

    "respond with OK on page load with correct content" in {
      createMockForRegistration(mockCachingConnector, getRegDetailsFromCache = true)
      createMocksForApplication(mockCachingConnector,
        mockIhtConnector,
        appDetails = Some(applicationDetailsTwoCharities),
        getAppDetails = true)

      val result = charityNameController.onPageLoad(createFakeRequest(authRetrieveNino = false))
      status(result) mustBe OK
      contentAsString(result) must include(messagesApi("page.iht.application.exemptions.charityName.sectionTitle"))
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
      result mustBe expectedResult
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
      status(result) mustBe SEE_OTHER
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
      status(result) mustBe BAD_REQUEST
    }

    "save application, update charity and  go to Charity Details Overview page on submit in edit mode" in {
      val mockIhtConnectorTemp: IhtConnector = mock[IhtConnector]
      val mockCachingConnectorTemp: CachingConnector = mock[CachingConnector]
      def charityNameControllerTemp = new TestController {
        override val authConnector = mockAuthConnector
        override val cachingConnector = mockCachingConnectorTemp
        override val ihtConnector = mockIhtConnectorTemp
        override implicit val formPartialRetriever: FormPartialRetriever = mockPartialRetriever
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
      val charityAfterSave: Option[Charity] = appDetailsBeforeSave.charities.find(_.id.contains("2"))
      charityAfterSave mustBe Some(charityChanged)
      status(result) mustBe SEE_OTHER
    }

    behave like controllerOnPageLoadWithNoExistingRegistrationDetails(mockCachingConnector,
      charityNameController.onPageLoad(createFakeRequest(authRetrieveNino = false)))
  }
}

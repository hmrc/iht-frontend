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

package iht.controllers.application.assets.properties

import iht.config.AppConfig
import iht.controllers.application.ApplicationControllerTest
import iht.forms.ApplicationForms._
import iht.models.application.ApplicationDetails

import iht.testhelpers._
import iht.utils.{CommonHelper, DeceasedInfoHelper}
import play.api.mvc.MessagesControllerComponents
import play.api.test.FakeHeaders
import play.api.test.Helpers._
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController
import uk.gov.hmrc.play.partials.FormPartialRetriever

import scala.concurrent.Future

class PropertyValueControllerTest extends ApplicationControllerTest {

  protected abstract class TestController extends FrontendController(mockControllerComponents) with PropertyValueController {
    override val cc: MessagesControllerComponents = mockControllerComponents
    override implicit val appConfig: AppConfig = mockAppConfig
  }

  lazy val regDetails = CommonBuilder.buildRegistrationDetails copy (
    deceasedDetails = Some(CommonBuilder.buildDeceasedDetails), ihtReference = Some("AbC123"))

  lazy val deceasedName = DeceasedInfoHelper.getDeceasedNameOrDefaultString(regDetails)

  def setUpTests(applicationDetails: Option[ApplicationDetails] = None) = {
    createMocksForApplication(mockCachingConnector,
      mockIhtConnector,
      regDetails = regDetails,
      appDetails = applicationDetails,
      getAppDetails = true,
      saveAppDetails = true,
      storeAppDetailsInCache = true)
  }

  def propertyValueController = new TestController {
    override val cachingConnector = mockCachingConnector
//    override val authConnector = mockAuthConnector
    override val ihtConnector = mockIhtConnector
    override val authConnector = mockAuthConnector

    override implicit val formPartialRetriever: FormPartialRetriever = MockFormPartialRetriever
  }

  def propertyValueControllerNotAuthorised = new TestController {
    override val cachingConnector = mockCachingConnector
    override val ihtConnector = mockIhtConnector
    override val authConnector = mockAuthConnector

    override implicit val formPartialRetriever: FormPartialRetriever = MockFormPartialRetriever
  }

  implicit val headerCarrier = FakeHeaders()
  implicit val hc = new HeaderCarrier

  "Property value controller" must {


    "redirect to login page on PageLoad if the user is not logged in" in {
      val result = propertyValueControllerNotAuthorised.onPageLoad(createFakeRequest(isAuthorised = false))
      status(result) must be(SEE_OTHER)
      redirectLocation(result) must be (Some(loginUrl))
    }

    "redirect to login page on Submit if the user is not logged in" in {
      val result = propertyValueControllerNotAuthorised.onSubmit(createFakeRequest(isAuthorised = false))
      status(result) must be(SEE_OTHER)
      redirectLocation(result) must be (Some(loginUrl))
    }

    "return OK on page load" in {
      val applicationDetails = CommonBuilder.buildApplicationDetails

      setUpTests(Some(applicationDetails))

      val result = propertyValueController.onPageLoad()(createFakeRequest(authRetrieveNino = false))
      status(result) must be (OK)
    }

    "display the page title on page load" in {
      createMockToGetRegDetailsFromCache(mockCachingConnector, Future.successful(Some(regDetails)))

      val result = propertyValueController.onPageLoad()(createFakeRequest(authRetrieveNino = false))
      status(result) must be (OK)
      ContentChecker.stripLineBreaks(contentAsString(result)) must include (messagesApi("iht.estateReport.assets.properties.value.question", deceasedName))
    }

    "display property value label on page" in {
      createMockToGetRegDetailsFromCache(mockCachingConnector, Future.successful(Some(regDetails)))

      val result = propertyValueController.onPageLoad()(createFakeRequest(authRetrieveNino = false))
      status(result) must be (OK)
      ContentChecker.stripLineBreaks(contentAsString(result)) must include (messagesApi("iht.estateReport.assets.properties.value.question", deceasedName))
    }

    "display property question sub label on page" in {
      createMockToGetRegDetailsFromCache(mockCachingConnector, Future.successful(Some(regDetails)))

      val result = propertyValueController.onPageLoad()(createFakeRequest(authRetrieveNino = false))
      status(result) must be (OK)
      ContentChecker.stripLineBreaks(contentAsString(result)) must include (messagesApi("page.iht.application.property.value.question.hint1",
                                                      deceasedName))
    }

    "respond with bad request when incorrect value are entered on the page" in {
      implicit val fakePostRequest = createFakeRequest().withFormUrlEncodedBody(("value", "utytyyterrrrrrrrrrrrrr"))

      createMockToGetRegDetailsFromCacheNoOption(mockCachingConnector)

      val result = propertyValueController.onSubmit (fakePostRequest)
      status(result) mustBe (BAD_REQUEST)
    }

    "display the correct title on page in edit mode" in {
      val applicationDetails = iht.testhelpers.CommonBuilder.buildApplicationDetails.
        copy(propertyList = List(CommonBuilder.property))
      val regDetails = CommonBuilder.buildRegistrationDetails1
      val deceasedName = regDetails.deceasedDetails.map(_.name).fold("")(identity)

      setUpTests(Some(applicationDetails))

      val result = propertyValueController.onEditPageLoad("1")(createFakeRequest())
      status(result) must be (OK)
      ContentChecker.stripLineBreaks(contentAsString(result)) must include (messagesApi("iht.estateReport.assets.properties.value.question", deceasedName))
    }

    "redirect to PropertyDetails overview page on submit" in {
      val applicationDetails = iht.testhelpers.CommonBuilder.buildApplicationDetails.
        copy(propertyList = List())

      val formFill = propertyValueForm.fill(CommonBuilder.buildProperty.copy(value = Some(10)))
      implicit val request = createFakeRequest().withFormUrlEncodedBody(formFill.data.toSeq: _*)

      setUpTests(Some(applicationDetails))

      val result = propertyValueController.onSubmit()(request)
      status(result) must be (SEE_OTHER)
      redirectLocation(result) must be (Some(CommonHelper.addFragmentIdentifierToUrl(routes.PropertyDetailsOverviewController.onEditPageLoad("1").url,TestHelper.AssetsPropertiesPropertyValueID)))
    }

    "redirect to PropertyDetails overview page on submit in edit mode" in {
      val propertyId = "1"

      val applicationDetails = iht.testhelpers.CommonBuilder.buildApplicationDetails.
        copy(propertyList = List(CommonBuilder.buildProperty.copy(id = Some(propertyId),
          typeOfOwnership = TestHelper.PropertyTypeDeceasedHome,
          value = Some(1234))))

      val formFill = propertyValueForm.fill(CommonBuilder.buildProperty.copy(value = Some(10)))
      implicit val request = createFakeRequest().withFormUrlEncodedBody(formFill.data.toSeq: _*)

      setUpTests(Some(applicationDetails))

      val result = propertyValueController.onEditSubmit(propertyId)(request)
      status(result) must be (SEE_OTHER)

      redirectLocation(result) must be (Some(CommonHelper.addFragmentIdentifierToUrl(routes.PropertyDetailsOverviewController.onEditPageLoad(propertyId).url,TestHelper.AssetsPropertiesPropertyValueID)))
    }

    "Go to kickout page if kickout reason found" in {
      val applicationDetails = iht.testhelpers.CommonBuilder.buildApplicationDetails.copy(
        propertyList = List(iht.testhelpers.CommonBuilder.property)
      )

      implicit val request = createFakeRequest().withFormUrlEncodedBody(("id", "1"),
        ("address.ukAddressLine1", "a"),
        ("address.ukAddressLine2", "a"),
        ("address.postCode", CommonBuilder.DefaultUkAddress.postCode),
        ("address.countryCode", "GB"),
        ("typeOfOwnership", "Deceased only"),
        ("propertyType", "Deceased's home"),
        ("tenure", "Freehold"),
        ("value", "1000001")
      )

      setUpTests(Some(applicationDetails))

      val result = propertyValueController.onSubmit(request)
      status(result) must be (SEE_OTHER)
      redirectLocation(result) mustBe Some(iht.controllers.application.routes.KickoutAppController.onPageLoad().url)
    }
    "load the page when editing for kickout" in {
      val applicationDetails = iht.testhelpers.CommonBuilder.buildApplicationDetails.copy(
        propertyList = List(iht.testhelpers.CommonBuilder.property)
      )

      setUpTests(Some(applicationDetails))

      val result = propertyValueController.onEditPageLoadForKickout("1")(createFakeRequest())
      status(result) must be (OK)
    }

    "show internal server error when editing where no app details" in {
      val applicationDetails = iht.testhelpers.CommonBuilder.buildApplicationDetails.copy(
        propertyList = List(iht.testhelpers.CommonBuilder.property)
      )

      createMocksForApplication(mockCachingConnector,
        mockIhtConnector,
        appDetails = Some(applicationDetails),
        getAppDetailsObject = None,
        getAppDetails = true,
        saveAppDetails= true,
        storeAppDetailsInCache = true)

      val result = propertyValueController.onEditPageLoad("1")(createFakeRequest())
      status(result) must be (INTERNAL_SERVER_ERROR)
    }

    behave like controllerOnPageLoadWithNoExistingRegistrationDetails(mockCachingConnector,
      propertyValueController.onPageLoad(createFakeRequest(authRetrieveNino = false)))
  }
}

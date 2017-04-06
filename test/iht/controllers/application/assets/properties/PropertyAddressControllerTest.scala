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

package iht.controllers.application.assets.properties

import iht.connector.{CachingConnector, IhtConnector}
import iht.controllers.application.ApplicationControllerTest
import iht.forms.ApplicationForms._
import iht.testhelpers.CommonBuilder
import iht.testhelpers.MockObjectBuilder._
import play.api.i18n.{Messages, MessagesApi}
import play.api.i18n.Messages.Implicits._
import play.api.Play.current
import play.api.test.FakeHeaders
import play.api.test.Helpers._
import uk.gov.hmrc.play.http.HeaderCarrier
import iht.models.application.ApplicationDetails

/**
 * Created by james on 17/06/16.
 */
class PropertyAddressControllerTest extends ApplicationControllerTest {

  val mockCachingConnector = mock[CachingConnector]
  val mockIhtConnector = mock[IhtConnector]

  def setUpTests(applicationDetails: Option[ApplicationDetails] = None) = {
    createMocksForApplication(mockCachingConnector,
      mockIhtConnector,
      appDetails = applicationDetails,
      getAppDetails = true,
      saveAppDetails = true,
      storeAppDetailsInCache = true)
  }

  def propertyAddressController = new PropertyAddressController {
    override val cachingConnector = mockCachingConnector
    override val authConnector = createFakeAuthConnector(isAuthorised = true)
    override val ihtConnector = mockIhtConnector
    override val isWhiteListEnabled = false
  }

  def propertyAddressControllerNotAuthorised = new PropertyAddressController {
    override val cachingConnector = mockCachingConnector
    override val authConnector = createFakeAuthConnector(isAuthorised = false)
    override val ihtConnector = mockIhtConnector
    override val isWhiteListEnabled = false
  }

  implicit val headerCarrier = FakeHeaders()
  implicit val hc = new HeaderCarrier

  "Property address controller" must {

   "redirect to ida login page on PageLoad if the user is not logged in" in {
      val result = propertyAddressControllerNotAuthorised.onPageLoad(createFakeRequest(isAuthorised = false))
      status(result) should be(SEE_OTHER)
      redirectLocation(result) should be (Some(loginUrl))
    }

    "redirect to ida login page on Submit if the user is not logged in" in {
      val result = propertyAddressControllerNotAuthorised.onSubmit(createFakeRequest(isAuthorised = false))
      status(result) should be(SEE_OTHER)
      redirectLocation(result) should be (Some(loginUrl))
    }

    "respond ok on page load" in {
      val result = propertyAddressController.onPageLoad()(createFakeRequest())
      status(result) should be (OK)
    }

    "display the correct title on page" in {
      val result = propertyAddressController.onPageLoad()(createFakeRequest())
      status(result) should be (OK)
      contentAsString(result) should include (messagesApi("iht.estateReport.assets.property.whatIsAddress.question"))
    }

    "display the correct title on page in edit mode" in {
      val applicationDetails = iht.testhelpers.CommonBuilder.buildApplicationDetails.
        copy(propertyList = List(CommonBuilder.property))

      setUpTests(Some(applicationDetails))

      val result = propertyAddressController.onEditPageLoad("1")(createFakeRequest())
      status(result) should be (OK)
      contentAsString(result) should include (messagesApi("iht.estateReport.assets.property.whatIsAddress.question"))
    }

    "respond with INTERNAL_SERVER_ERROR on page load in edit mode when application details could not be retrieved" in {
      setUpTests()

      val result = propertyAddressController.onEditPageLoad("1")(createFakeRequest())
      status(result) should be (INTERNAL_SERVER_ERROR)
    }

    "respond with RuntimeException on page load in edit mode when matched property is not found" in {
      val applicationDetails = iht.testhelpers.CommonBuilder.buildApplicationDetails.
        copy(propertyList = List(CommonBuilder.property.copy(id = Some("1"))))

      setUpTests(Some(applicationDetails))

      intercept[RuntimeException] {
        await(propertyAddressController.onEditPageLoad("2")(createFakeRequest()))
      }
    }

    "redirect to PropertyDetails overview page on submit" in {
      val applicationDetails = iht.testhelpers.CommonBuilder.buildApplicationDetails.
        copy(propertyList = List())

      val formFill = propertyAddressForm.fill(CommonBuilder.property)
      implicit val request = createFakeRequest().withFormUrlEncodedBody(formFill.data.toSeq: _*)

      setUpTests(Some(applicationDetails))

      val result = propertyAddressController.onSubmit()(request)
      status(result) should be (SEE_OTHER)
      redirectLocation(result) should be (Some(routes.PropertyDetailsOverviewController.onEditPageLoad("1").url))
    }

    "redirect to PropertyDetails overview page on submit in edit mode" in {
      val propertyId = "1"

      val applicationDetails = iht.testhelpers.CommonBuilder.buildApplicationDetails.
        copy(propertyList = List(CommonBuilder.buildProperty.copy(id = Some(propertyId),
        address = CommonBuilder.property.address,
        value = Some(1234))))

      val formFill = propertyAddressForm.fill(CommonBuilder.property)
      implicit val request = createFakeRequest().withFormUrlEncodedBody(formFill.data.toSeq: _*)

      setUpTests(Some(applicationDetails))

      val result = propertyAddressController.onEditSubmit(propertyId)(request)
      status(result) should be (SEE_OTHER)
      redirectLocation(result) should be (Some(routes.PropertyDetailsOverviewController.onEditPageLoad(propertyId).url))
    }

    "respond with BAD_REQUEST on submit when request is malformed" in {
      val applicationDetails = iht.testhelpers.CommonBuilder.buildApplicationDetails.
        copy(propertyList = List())

      val formFill = propertyAddressForm.fill(CommonBuilder.property.copy(address = None))
      implicit val request = createFakeRequest().withFormUrlEncodedBody(formFill.data.toSeq: _*)

      setUpTests(Some(applicationDetails))

      val result = propertyAddressController.onSubmit()(request)
      status(result) should be (BAD_REQUEST)
    }
  }
}

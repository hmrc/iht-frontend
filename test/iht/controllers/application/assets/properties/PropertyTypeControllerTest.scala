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
import iht.models.application.ApplicationDetails
import iht.testhelpers.MockObjectBuilder._
import iht.testhelpers.{CommonBuilder, TestHelper}
import play.api.i18n.Messages
import play.api.test.Helpers._

/**
 * Created by jennygj on 17/06/16.
 */
class PropertyTypeControllerTest extends ApplicationControllerTest {

  val mockCachingConnector = mock[CachingConnector]
  val mockIhtConnector = mock[IhtConnector]

  def setUpTests(applicationDetails: ApplicationDetails) = {
    createMocksForApplication(mockCachingConnector,
      mockIhtConnector,
      appDetails = Some(applicationDetails),
      getAppDetails = true,
      saveAppDetails = true,
      storeAppDetailsInCache = true)
  }

  def propertyTypeController = new PropertyTypeController {
    val authConnector = createFakeAuthConnector(isAuthorised = true)
    override val cachingConnector = mockCachingConnector
    override val ihtConnector = mockIhtConnector
  }

  def propertyTypeControllerNotAuthorised = new PropertyTypeController {
    val authConnector = createFakeAuthConnector(isAuthorised = false)
    override val cachingConnector = mockCachingConnector
    override val ihtConnector = mockIhtConnector
  }

  "PropertyTypeController" must {

    "redirect to login page on PageLoad if the user is not logged in" in {
      val result = propertyTypeControllerNotAuthorised.onPageLoad(createFakeRequest())
      status(result) should be(SEE_OTHER)
      redirectLocation(result) should be (Some(loginUrl))
    }

    "redirect to login page on Submit if the user is not logged in" in {
      val result = propertyTypeControllerNotAuthorised.onSubmit(createFakeRequest())
      status(result) should be(SEE_OTHER)
      redirectLocation(result) should be (Some(loginUrl))
    }

    "respond with OK on page load" in {
      val applicationDetails = CommonBuilder.buildApplicationDetails
      setUpTests(applicationDetails)

      val result = propertyTypeController.onPageLoad(createFakeRequest())
      status(result) should be(OK)
    }

    "respond with OK on edit page load" in {
      val id: String = "1"
      val applicationDetails = CommonBuilder.buildApplicationDetails.copy(propertyList = List(CommonBuilder.property))
      setUpTests(applicationDetails)

      val result = propertyTypeController.onEditPageLoad(id)(createFakeRequest())
      status(result) should be(OK)
    }

    "save application and go to property overview page edit mode on submit" in {
      val applicationDetails = iht.testhelpers.CommonBuilder.buildApplicationDetails.copy(propertyList = List())
      val formFill = propertyTypeForm.fill(CommonBuilder.buildProperty.copy(propertyType = TestHelper.PropertyTypeDeceasedHome))
      implicit val request = createFakeRequest().withFormUrlEncodedBody(formFill.data.toSeq: _*)

      setUpTests(applicationDetails)

      val result = propertyTypeController.onSubmit()(request)
      status(result) should be (SEE_OTHER)
      redirectLocation(result) should be (Some(routes.PropertyDetailsOverviewController.onEditPageLoad("1").url))
    }

    "add property to property list should add new property to list if property doesn't exist " in {
      val applicationDetails = CommonBuilder.buildApplicationDetails.copy(propertyList = List())
      setUpTests(applicationDetails)

      val property1 = CommonBuilder.property
      val property2WithoutId = CommonBuilder.property.copy(id = None, value = Some(BigDecimal(2)))
      val property2 = CommonBuilder.property.copy(id = Some("2"), value = Some(BigDecimal(2)))
      val propertyList = List(property1)
      val propertyListNew = List(property1, property2)
      val result = propertyTypeController.addPropertyToPropertyList(property2WithoutId, propertyList)

      result should equal ((propertyListNew, "2"))
    }

    "display the correct title on page load" in {
      val result = propertyTypeController.onPageLoad()(createFakeRequest())
      status(result) should be (OK)
      contentAsString(result) should include (Messages("iht.estateReport.assets.properties.whatKind.question"))
    }

    "display correct options for input radio group" in {
      val result = propertyTypeController.onPageLoad()(createFakeRequest())
      status(result) should be (OK)
      contentAsString(result) should include (Messages("page.iht.application.assets.propertyType.otherResidential.label"))
    }
  }
}

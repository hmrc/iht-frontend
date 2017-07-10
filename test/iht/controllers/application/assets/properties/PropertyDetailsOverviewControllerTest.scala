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
import iht.testhelpers.{CommonBuilder, ContentChecker}
import iht.testhelpers.MockObjectBuilder._
import iht.utils._
import play.api.mvc.{Request, Result}
import play.api.test.FakeHeaders
import play.api.test.Helpers._
import uk.gov.hmrc.play.http.HeaderCarrier

import scala.concurrent.Future

/**
  * Created by james on 16/06/16.
  */
trait PropertyDetailsOverviewControllerBehaviour extends ApplicationControllerTest {

  val mockCachingConnector = mock[CachingConnector]
  val mockIhtConnector = mock[IhtConnector]

  lazy val regDetails = CommonBuilder.buildRegistrationDetails copy(
    deceasedDetails = Some(CommonBuilder.buildDeceasedDetails), ihtReference = Some("AbC123"))

  lazy val deceasedName = DeceasedInfoHelper.getDeceasedNameOrDefaultString(regDetails)

  def propertyDetailsOverviewController = new PropertyDetailsOverviewController {
    override val cachingConnector = mockCachingConnector
    override val authConnector = createFakeAuthConnector(isAuthorised = true)
    override val ihtConnector = mockIhtConnector
    override val isWhiteListEnabled = false
  }

  def propertyDetailsOverviewControllerNotAuthorised = new PropertyDetailsOverviewController {
    override val cachingConnector = mockCachingConnector
    override val authConnector = createFakeAuthConnector(isAuthorised = false)
    override val ihtConnector = mockIhtConnector
    override val isWhiteListEnabled = false
  }

  implicit val headerCarrier = FakeHeaders()
  implicit val hc = new HeaderCarrier

  val applicationDetails = CommonBuilder.buildApplicationDetails copy (
    propertyList = CommonBuilder.buildPropertyList
    )

  def pageLoad(request: Request[_]): Future[Result]

  "Property details overview controller" must {

    "return OK on page load" in {

      createMocksForApplication(mockCachingConnector,
        mockIhtConnector,
        regDetails = regDetails,
        appDetails = Some(applicationDetails),
        getAppDetails = true,
        saveAppDetails = true,
        storeAppDetailsInCache = true)

      val result = pageLoad(createFakeRequest())
      status(result) should be(OK)
    }

    "display the page title on page load" in {
      val result = propertyDetailsOverviewController.onPageLoad()(createFakeRequest())
      status(result) should be(OK)
      contentAsString(result) should include(messagesApi("iht.estateReport.assets.propertyAdd"))
    }

    "display property address details question on page" in {
      val result = propertyDetailsOverviewController.onPageLoad()(createFakeRequest())
      status(result) should be(OK)
      contentAsString(result) should include(messagesApi("iht.estateReport.assets.property.whatIsAddress.question"))
    }

    "display kind of property question on page" in {
      val result = propertyDetailsOverviewController.onPageLoad()(createFakeRequest())
      status(result) should be(OK)
      contentAsString(result) should include(messagesApi("iht.estateReport.assets.properties.whatKind.question"))
    }

    "display how the property was owned question on the page" in {
      val result = propertyDetailsOverviewController.onPageLoad()(createFakeRequest())
      status(result) should be(OK)
      ContentChecker.stripLineBreaks(contentAsString(result)) should include(messagesApi("iht.estateReport.assets.howOwnedByDeceased", deceasedName))
    }

    "display freehold leasehold question on page" in {
      val result = propertyDetailsOverviewController.onPageLoad()(createFakeRequest())
      status(result) should be(OK)
      contentAsString(result) should include(messagesApi("iht.estateReport.assets.properties.freeholdOrLeasehold"))
    }

    "display value of property question on the page" in {
      val result = propertyDetailsOverviewController.onPageLoad()(createFakeRequest())
      status(result) should be(OK)
      contentAsString(result) should include(messagesApi("iht.estateReport.assets.properties.value.question", deceasedName))
    }

    "redirect to properties overview when onEditPageLoad is called with a property ID that does not exist" in {
      val appDetails = CommonBuilder.buildApplicationDetails

      createMocksForApplication(mockCachingConnector,
        mockIhtConnector,
        regDetails = regDetails,
        appDetails = Some(appDetails),
        getAppDetails = true,
        saveAppDetails = true,
        storeAppDetailsInCache = true)

      val result = propertyDetailsOverviewController.onEditPageLoad("1")(createFakeRequest())
      status(result) should be(SEE_OTHER)
    }

    behave like controllerOnPageLoadWithNoExistingRegistrationDetails(mockCachingConnector,
      propertyDetailsOverviewController.onPageLoad(createFakeRequest()))
  }
}

class PropertyDetailsOverviewControllerTest extends PropertyDetailsOverviewControllerBehaviour {
  def pageLoad(request: Request[_]): Future[Result] = propertyDetailsOverviewController.onPageLoad()(createFakeRequest())
}

class PropertyDetailsOverviewControllerInEditModeTest extends PropertyDetailsOverviewControllerBehaviour {
  def pageLoad(request: Request[_]): Future[Result] = propertyDetailsOverviewController.onEditPageLoad("1")(createFakeRequest())
}

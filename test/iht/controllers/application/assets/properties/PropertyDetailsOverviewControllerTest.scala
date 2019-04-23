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

package iht.controllers.application.assets.properties

import iht.config.AppConfig
import iht.controllers.application.ApplicationControllerTest
import iht.controllers.application.declaration.DeclarationController
import iht.testhelpers.{CommonBuilder, ContentChecker, MockFormPartialRetriever}
import iht.utils._
import play.api.mvc.{MessagesControllerComponents, Request, Result}
import play.api.test.FakeHeaders
import play.api.test.Helpers._
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.bootstrap.controller.FrontendController
import uk.gov.hmrc.play.partials.FormPartialRetriever

import scala.concurrent.Future

/**
  * Created by james on 16/06/16.
  */
trait PropertyDetailsOverviewControllerBehaviour extends ApplicationControllerTest {



  lazy val regDetails = CommonBuilder.buildRegistrationDetails copy(
    deceasedDetails = Some(CommonBuilder.buildDeceasedDetails), ihtReference = Some("AbC123"))

  lazy val deceasedName = DeceasedInfoHelper.getDeceasedNameOrDefaultString(regDetails)

  protected abstract class TestController extends FrontendController(mockControllerComponents) with PropertyDetailsOverviewController {
    override val cc: MessagesControllerComponents = mockControllerComponents
    override implicit val appConfig: AppConfig = mockAppConfig
  }

  def propertyDetailsOverviewController = new TestController {
    override val cachingConnector = mockCachingConnector
    override val authConnector = mockAuthConnector
    override val ihtConnector = mockIhtConnector

    override implicit val formPartialRetriever: FormPartialRetriever = MockFormPartialRetriever
  }

  def propertyDetailsOverviewControllerNotAuthorised = new TestController {
    override val cachingConnector = mockCachingConnector
    override val authConnector = mockAuthConnector
    override val ihtConnector = mockIhtConnector

    override implicit val formPartialRetriever: FormPartialRetriever = MockFormPartialRetriever
  }

  implicit val headerCarrier = FakeHeaders()
  implicit val hc = new HeaderCarrier

  val applicationDetails = CommonBuilder.buildApplicationDetails copy (
    propertyList = CommonBuilder.buildPropertyList
    )

  def pageLoad(request: Request[_]): Future[Result]

  class Setup {
    createMocksForApplication(mockCachingConnector,
      mockIhtConnector,
      regDetails = regDetails,
      appDetails = Some(applicationDetails),
      getAppDetails = true,
      saveAppDetails = true,
      storeAppDetailsInCache = true)
  }

  "Property details overview controller" must {

    "return OK on page load" in new Setup {
      val result = pageLoad(createFakeRequest())
      status(result) must be(OK)
    }

    "display the page title on page load" in new Setup {

      val result = propertyDetailsOverviewController.onPageLoad()(createFakeRequest(authRetrieveNino = false))
      status(result) must be(OK)
      contentAsString(result) must include(messagesApi("iht.estateReport.assets.propertyAdd"))
    }

    "display property address details question on page" in new Setup {
      val result = propertyDetailsOverviewController.onPageLoad()(createFakeRequest(authRetrieveNino = false))
      status(result) must be(OK)
      contentAsString(result) must include(messagesApi("iht.estateReport.assets.property.whatIsAddress.question"))
    }

    "display kind of property question on page" in new Setup {
      val result = propertyDetailsOverviewController.onPageLoad()(createFakeRequest(authRetrieveNino = false))
      status(result) must be(OK)
      contentAsString(result) must include(messagesApi("iht.estateReport.assets.properties.whatKind.question"))
    }

    "display how the property was owned question on the page" in new Setup {
      val result = propertyDetailsOverviewController.onPageLoad()(createFakeRequest(authRetrieveNino = false))
      status(result) must be(OK)
      ContentChecker.stripLineBreaks(contentAsString(result)) must include(messagesApi("iht.estateReport.assets.howOwnedByDeceased", deceasedName))
    }

    "display freehold leasehold question on page" in new Setup {
      val result = propertyDetailsOverviewController.onPageLoad()(createFakeRequest(authRetrieveNino = false))
      status(result) must be(OK)
      contentAsString(result) must include(messagesApi("iht.estateReport.assets.properties.freeholdOrLeasehold"))
    }

    "display value of property question on the page" in new Setup {
      val result = propertyDetailsOverviewController.onPageLoad()(createFakeRequest(authRetrieveNino = false))
      status(result) must be(OK)
      ContentChecker.stripLineBreaks(contentAsString(result)) must include(messagesApi("iht.estateReport.assets.properties.value.question", deceasedName))
    }

    "redirect to properties overview when onEditPageLoad is called with a property ID that does not exist" in new Setup {
      val appDetails = CommonBuilder.buildApplicationDetails

      createMocksForApplication(mockCachingConnector,
        mockIhtConnector,
        regDetails = regDetails,
        appDetails = Some(appDetails),
        getAppDetails = true,
        saveAppDetails = true,
        storeAppDetailsInCache = true)

      val result = propertyDetailsOverviewController.onEditPageLoad("1")(createFakeRequest())
      status(result) must be(SEE_OTHER)
    }

    behave like controllerOnPageLoadWithNoExistingRegistrationDetails(mockCachingConnector,
      propertyDetailsOverviewController.onPageLoad(createFakeRequest(authRetrieveNino = false)))
  }
}

class PropertyDetailsOverviewControllerTest extends PropertyDetailsOverviewControllerBehaviour {
  def pageLoad(request: Request[_]): Future[Result] = propertyDetailsOverviewController.onPageLoad()(createFakeRequest(authRetrieveNino = false))
}

class PropertyDetailsOverviewControllerInEditModeTest extends PropertyDetailsOverviewControllerBehaviour {
  def pageLoad(request: Request[_]): Future[Result] = propertyDetailsOverviewController.onEditPageLoad("1")(createFakeRequest())
}

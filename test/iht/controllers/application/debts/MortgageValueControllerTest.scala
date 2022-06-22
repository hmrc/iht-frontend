/*
 * Copyright 2022 HM Revenue & Customs
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

package iht.controllers.application.debts

import iht.config.AppConfig
import iht.controllers.application.ApplicationControllerTest
import iht.forms.ApplicationForms._
import iht.testhelpers.TestHelper._
import iht.testhelpers.{CommonBuilder, ContentChecker}
import iht.utils._
import iht.views.html.application.debts.mortgage_value
import play.api.mvc.MessagesControllerComponents
import play.api.test.FakeHeaders
import play.api.test.Helpers._
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController

import scala.concurrent.Await
import scala.concurrent.duration.Duration


class MortgageValueControllerTest extends ApplicationControllerTest {

  protected abstract class TestController extends FrontendController(mockControllerComponents) with MortgageValueController {
    override val cc: MessagesControllerComponents = mockControllerComponents
    override implicit val appConfig: AppConfig = mockAppConfig
    override val mortgageValueView: mortgage_value = app.injector.instanceOf[mortgage_value]
  }

  def mortgageValueController = new TestController {
    override val cachingConnector = mockCachingConnector
    override val authConnector = mockAuthConnector
    override val ihtConnector = mockIhtConnector

  }

  def mortgageValueControllerNotAuthorised = new TestController {
    override val cachingConnector = mockCachingConnector
    override val authConnector = mockAuthConnector
    override val ihtConnector = mockIhtConnector
  }

  implicit val headerCarrier = FakeHeaders()
  implicit val hc = new HeaderCarrier

  "MortgageValueController controller" must {

    "redirect to ida login page on PageLoad if the user is not logged in" in {
      val result = mortgageValueControllerNotAuthorised.onPageLoad("1")(createFakeRequest(isAuthorised = false))
      status(result) must be(SEE_OTHER)
      redirectLocation(result) must be (Some(loginUrl))
    }

    "redirect to ida login page on Submit if the user is not logged in" in {
      val result = mortgageValueControllerNotAuthorised.onSubmit("1")(createFakeRequest(isAuthorised = false))
      status(result) must be(SEE_OTHER)
      redirectLocation(result) must be (Some(loginUrl))
    }

    "respond ok on page load" in {
      val applicationDetails = iht.testhelpers.CommonBuilder.buildApplicationDetails.
        copy(propertyList = List(CommonBuilder.property))

      createMocksForApplication(mockCachingConnector,
        mockIhtConnector,
        appDetails = Some(applicationDetails),
        getAppDetails = true,
        saveAppDetails= true,
        storeAppDetailsInCache = true)

      val result = mortgageValueController.onPageLoad("1")(createFakeRequest())
      status(result) must be (OK)
    }

    "respond ok on page load when it has some debts but mortgage" in {
      val applicationDetails = iht.testhelpers.CommonBuilder.buildApplicationDetails.
        copy(propertyList = List(CommonBuilder.property),
          allLiabilities = Some(CommonBuilder.buildAllLiabilities.copy(mortgages = None)))

      createMocksForApplication(mockCachingConnector,
        mockIhtConnector,
        appDetails = Some(applicationDetails),
        getAppDetails = true,
        saveAppDetails= true,
        storeAppDetailsInCache = true)

      val result = mortgageValueController.onPageLoad("1")(createFakeRequest())
      status(result) must be (OK)
    }

    "display the correct title on page" in {

      val applicationDetails = iht.testhelpers.CommonBuilder.buildApplicationDetails.
        copy(propertyList = List(CommonBuilder.property))

      val regDetails = buildRegistrationDetailsWithDeceasedAndIhtRefDetails

      createMocksForApplication(mockCachingConnector,
        mockIhtConnector,
        regDetails = regDetails,
        appDetails = Some(applicationDetails),
        getAppDetails = true,
        saveAppDetails= true,
        storeAppDetailsInCache = true)

      val result = mortgageValueController.onPageLoad("1")(createFakeRequest())
      status(result) must be (OK)
      ContentChecker.stripLineBreaks(contentAsString(result)) must include (
        messagesApi("page.iht.application.debts.mortgageValue.title",
          DeceasedInfoHelper.getDeceasedNameOrDefaultString(regDetails)))
    }

    "respond with bad request when form has some error" in {
      val applicationDetails = iht.testhelpers.CommonBuilder.buildApplicationDetails.
        copy(propertyList = List(CommonBuilder.property))

      val formFill = mortgagesForm.fill(CommonBuilder.buildMortgage.copy("1", None, None))

      implicit val request = createFakeRequest().withFormUrlEncodedBody(formFill.data.toSeq: _*)

      createMocksForApplication(mockCachingConnector,
        mockIhtConnector,
        appDetails = Some(applicationDetails),
        getAppDetails = true,
        saveAppDetails= true,
        storeAppDetailsInCache = true)

      val result = mortgageValueController.onSubmit("1")(request)

      status(result) must be (BAD_REQUEST)

    }

    "respond with Internal server when there is no application details" in {
      val formFill = mortgagesForm.fill(CommonBuilder.buildMortgage.copy("1", None, None))

      implicit val request = createFakeRequest().withFormUrlEncodedBody(formFill.data.toSeq: _*)

      createMocksForApplication(mockCachingConnector,
        mockIhtConnector,
        appDetails = None,
        getAppDetails = true,
        saveAppDetails= true,
        storeAppDetailsInCache = true)

      val result = mortgageValueController.onSubmit("1")(request)
      status(result) must be (INTERNAL_SERVER_ERROR)
    }
    
    "respond with Runtime exception when there if no matched property found" in {
      val applicationDetails = CommonBuilder.buildApplicationDetails.
        copy(propertyList = List(CommonBuilder.property))

      val formFill = mortgagesForm.fill(CommonBuilder.buildMortgage.copy("1", isOwned = Some(true),
        value = Some(BigDecimal(2000))))

      implicit val request = createFakeRequest().withFormUrlEncodedBody(formFill.data.toSeq: _*)

      createMocksForApplication(mockCachingConnector,
        mockIhtConnector,
        appDetails = Some(applicationDetails),
        getAppDetails = true,
        saveAppDetails= true,
        storeAppDetailsInCache = true)

      intercept[RuntimeException] {
        Await.result(mortgageValueController.onSubmit("3")(request), Duration.Inf)
      }
    }

    "redirect to Mortgage overview page on submit" in {
      val formFill = mortgagesForm.fill(CommonBuilder.buildMortgage.copy(isOwned = Some(true),
        value = Some(BigDecimal(2000))))

      val applicationDetails = iht.testhelpers.CommonBuilder.buildApplicationDetails.
        copy(propertyList = List(CommonBuilder.property, CommonBuilder.property2),
          allLiabilities = Some(CommonBuilder.buildAllLiabilitiesWithAllSectionsFilled))

      implicit val request = createFakeRequest().withFormUrlEncodedBody(formFill.data.toSeq: _*)

      createMocksForApplication(mockCachingConnector,
        mockIhtConnector,
        appDetails = Some(applicationDetails),
        getAppDetails = true,
        saveAppDetails= true,
        storeAppDetailsInCache = true)

      val result = mortgageValueController.onSubmit("1")(request)

      status(result) must be (SEE_OTHER)
      redirectLocation(result) must be (Some(CommonHelper.addFragmentIdentifierToUrl(routes.MortgagesOverviewController.onPageLoad.url, DebtsMortgagesPropertyID + "1")))
    }
  }
}

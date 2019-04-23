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

package iht.controllers.application.exemptions.qualifyingBody

import iht.config.AppConfig
import iht.controllers.application.ApplicationControllerTest
import iht.controllers.registration.applicant.ApplicantTellUsAboutYourselfController
import iht.testhelpers.{CommonBuilder, MockFormPartialRetriever}
import play.api.mvc.MessagesControllerComponents
import play.api.test.FakeHeaders
import play.api.test.Helpers._
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.bootstrap.controller.FrontendController
import uk.gov.hmrc.play.partials.FormPartialRetriever

class QualifyingBodyDetailsOverviewControllerTest extends ApplicationControllerTest {

  protected abstract class TestController extends FrontendController(mockControllerComponents) with QualifyingBodyDetailsOverviewController {
    override val cc: MessagesControllerComponents = mockControllerComponents
    override implicit val appConfig: AppConfig = mockAppConfig
  }

  def qualifyingBodyDetailsOverviewController = new TestController {
    override val cachingConnector = mockCachingConnector
    override val authConnector = mockAuthConnector
    override val ihtConnector = mockIhtConnector

    override implicit val formPartialRetriever: FormPartialRetriever = MockFormPartialRetriever
  }

  def qualifyingBodyDetailsOverviewControllerNotAuthorised = new TestController {
    override val cachingConnector = mockCachingConnector
    override val authConnector = mockAuthConnector
    override val ihtConnector = mockIhtConnector

    override implicit val formPartialRetriever: FormPartialRetriever = MockFormPartialRetriever
  }

  implicit val headerCarrier = FakeHeaders()
  implicit val hc = new HeaderCarrier

  "QualifyingBody details overview controller" must {

    "return OK on page load" in {

      val applicationDetails = CommonBuilder.buildApplicationDetails

      createMocksForApplication(mockCachingConnector,
        mockIhtConnector,
        appDetails = Some(applicationDetails),
        getAppDetails = true,
        saveAppDetails = true,
        storeAppDetailsInCache = true)

      val result = qualifyingBodyDetailsOverviewController.onPageLoad()(createFakeRequest())
      status(result) must be(OK)
    }

    "display the page title on page load" in {
      val applicationDetails = CommonBuilder.buildApplicationDetails
      createMocksForApplication(mockCachingConnector,
        mockIhtConnector,
        appDetails = Some(applicationDetails),
        getAppDetails = true,
        saveAppDetails = true,
        storeAppDetailsInCache = true)

      val result = qualifyingBodyDetailsOverviewController.onPageLoad()(createFakeRequest())
      status(result) must be(OK)
      contentAsString(result) must include(
        messagesApi("iht.estateReport.assets.qualifyingBodyAdd"))
    }

    "show page in edit mode" in {
      val firstQualifyingBody = CommonBuilder.qualifyingBody
      val applicationModel = CommonBuilder.buildApplicationDetails.copy(qualifyingBodies = Seq(firstQualifyingBody))

      createMocksForApplication(mockCachingConnector,
        mockIhtConnector,
        appDetails = Some(applicationModel),
        getAppDetails = true,
        saveAppDetails = true,
        storeAppDetailsInCache = true)

      val result = qualifyingBodyDetailsOverviewController.onEditPageLoad("1")(createFakeRequest())
      status(result) must be(OK)
    }

    "respond with Internal Server Error where application details could not be retrieved" in {

      createMocksForApplication(mockCachingConnector,
        mockIhtConnector,
        appDetails = None,
        getAppDetails = true,
        saveAppDetails = true,
        storeAppDetailsInCache = true)

        val result = qualifyingBodyDetailsOverviewController.onEditPageLoad("2")(createFakeRequest())
        status(result) must be (INTERNAL_SERVER_ERROR)

    }

    "throw RuntimeException when qualifyingBody ID is accessed that does not exist" in {
      val firstQualifyingBody = CommonBuilder.qualifyingBody
      val applicationModel = CommonBuilder.buildApplicationDetails.copy(qualifyingBodies = Seq(firstQualifyingBody))

      createMocksForApplication(mockCachingConnector,
        mockIhtConnector,
        appDetails = Some(applicationModel),
        getAppDetails = true,
        saveAppDetails = true,
        storeAppDetailsInCache = true)

      intercept[RuntimeException] {
        val result = qualifyingBodyDetailsOverviewController.onEditPageLoad("2")(createFakeRequest())
        status(result) must be (INTERNAL_SERVER_ERROR)
      }
    }
  }
}

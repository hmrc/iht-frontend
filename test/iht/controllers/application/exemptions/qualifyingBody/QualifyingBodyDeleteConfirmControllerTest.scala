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
import iht.controllers.application.exemptions.ExemptionsOverviewController

import iht.testhelpers.TestHelper._
import iht.testhelpers.{CommonBuilder, MockFormPartialRetriever}
import iht.utils.CommonHelper._
import org.scalatest.BeforeAndAfter
import play.api.mvc.MessagesControllerComponents
import play.api.test.Helpers._
import uk.gov.hmrc.play.bootstrap.controller.FrontendController
import uk.gov.hmrc.play.partials.FormPartialRetriever

class QualifyingBodyDeleteConfirmControllerTest extends ApplicationControllerTest with BeforeAndAfter {

  protected abstract class TestController extends FrontendController(mockControllerComponents) with QualifyingBodyDeleteConfirmController {
    override val cc: MessagesControllerComponents = mockControllerComponents
    override implicit val appConfig: AppConfig = mockAppConfig
  }

  def qualifyingBodyDeleteConfirmController = new TestController {
    override val authConnector = mockAuthConnector
    override val cachingConnector = mockCachingConnector
    override val ihtConnector = mockIhtConnector
    override implicit val formPartialRetriever: FormPartialRetriever = MockFormPartialRetriever
  }

  def qualifyingBodyDeleteControllerNotAuthorised = new TestController {
    override val authConnector = mockAuthConnector
    override val cachingConnector = mockCachingConnector
    override val ihtConnector = mockIhtConnector
    override implicit val formPartialRetriever: FormPartialRetriever = MockFormPartialRetriever
  }

  val qualifyingBody1 = CommonBuilder.buildQualifyingBody.copy(
    id = Some("1"),
    name = Some("A QualifyingBody 1"),
    totalValue = Some(45.45))

  val qualifyingBody2 = CommonBuilder.buildQualifyingBody.copy(
    id = Some("2"),
    name = Some("A QualifyingBody 2"),
    totalValue = Some(46.45)
  )

  val applicationDetailsTwoQualifyingBodies = CommonBuilder.buildApplicationDetails copy (qualifyingBodies
    = Seq(qualifyingBody1, qualifyingBody2))

  "QualifyingBodyDeleteConfirmControllerTest" must {
    "redirect to login page on PageLoad if the user is not logged in" in {
      val result = qualifyingBodyDeleteControllerNotAuthorised.onPageLoad("1")(createFakeRequest(isAuthorised = false))
      status(result) must be(SEE_OTHER)
      redirectLocation(result) must be(Some(loginUrl))
    }

    "redirect to login page on Submit if the user is not logged in" in {
      val result = qualifyingBodyDeleteControllerNotAuthorised.onSubmit("1")(createFakeRequest(isAuthorised = false))
      status(result) must be(SEE_OTHER)
      redirectLocation(result) must be(Some(loginUrl))
    }

    "display main section title message on page load" in {
      createMockForRegistration(mockCachingConnector, getRegDetailsFromCache = true)
      createMocksForApplication(mockCachingConnector,
        mockIhtConnector,
        appDetails = Some(applicationDetailsTwoQualifyingBodies),
        getAppDetails = true)

      val result = qualifyingBodyDeleteConfirmController.onPageLoad("1")(createFakeRequest())
      status(result) mustBe OK
      contentAsString(result) must include(messagesApi("iht.estateReport.exemptions.qualifyingBodies.confirmDeleteQualifyingBody"))
    }
  }

  "when given a valid qualifyingBody id the qualifyingBody should redirect" in {
    createMockForRegistration(mockCachingConnector, getRegDetailsFromCache = true)
    createMocksForApplication(mockCachingConnector,
      mockIhtConnector,
      appDetails = Some(applicationDetailsTwoQualifyingBodies),
      getAppDetails = true,
      saveAppDetails = true)

    val result = qualifyingBodyDeleteConfirmController.onSubmit("1")(createFakeRequest())

    status(result) mustBe(SEE_OTHER)
    redirectLocation(result) must be(Some(addFragmentIdentifierToUrl(routes.QualifyingBodiesOverviewController.onPageLoad().url, ExemptionsOtherAddID)))
  }

  "when given a valid qualifyingBody id the qualifyingBody must be deleted in load" in {
    createMockForRegistration(mockCachingConnector, getRegDetailsFromCache = true)
    createMocksForApplication(mockCachingConnector,
      mockIhtConnector,
      appDetails = Some(applicationDetailsTwoQualifyingBodies),
      getAppDetails = true,
      saveAppDetails = true)

    val result = qualifyingBodyDeleteConfirmController.onSubmit("1")(createFakeRequest())

    status(result) mustBe(SEE_OTHER)
    val capturedValue = verifyAndReturnSavedApplicationDetails(mockIhtConnector)
    capturedValue.qualifyingBodies.length mustBe 1
    capturedValue.qualifyingBodies(0).id.getOrElse("") mustBe("2")
  }

  "when given a invalid qualifyingBody id during the load, we should get and internal server error" in {
    createMockForRegistration(mockCachingConnector, getRegDetailsFromCache = true)
    createMocksForApplication(mockCachingConnector,
      mockIhtConnector,
      appDetails = Some(applicationDetailsTwoQualifyingBodies),
      getAppDetails = true,
      saveAppDetails = true)

    val result = qualifyingBodyDeleteConfirmController.onPageLoad("999999")(createFakeRequest())

    status(result) mustBe(INTERNAL_SERVER_ERROR)
  }

  "when given a invalid qualifyingBody id during the submit, we should get and internal server error" in {
    createMockForRegistration(mockCachingConnector, getRegDetailsFromCache = true)
    createMocksForApplication(mockCachingConnector,
      mockIhtConnector,
      appDetails = Some(applicationDetailsTwoQualifyingBodies),
      getAppDetails = true,
      saveAppDetails = true)

    val result = qualifyingBodyDeleteConfirmController.onSubmit("999999")(createFakeRequest())

    status(result) mustBe(INTERNAL_SERVER_ERROR)
  }
}

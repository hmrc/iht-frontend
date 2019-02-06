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

package iht.controllers.application.exemptions.charity

import iht.connector.{CachingConnector, IhtConnector}
import iht.controllers.application.ApplicationControllerTest
import iht.testhelpers.{MockFormPartialRetriever, CommonBuilder}
import iht.testhelpers.MockObjectBuilder._
import org.scalatest.BeforeAndAfter
import play.api.i18n.Messages
import play.api.i18n.Messages.Implicits._
import play.api.Play.current
import play.api.test.Helpers._
import uk.gov.hmrc.play.partials.FormPartialRetriever

class CharityDeleteConfirmControllerTest extends ApplicationControllerTest with BeforeAndAfter {

  def charityDeleteConfirmController = new CharityDeleteConfirmController {
    override val authConnector = mockAuthConnector
    override val cachingConnector = mockCachingConnector
    override val ihtConnector = mockIhtConnector
    override implicit val formPartialRetriever: FormPartialRetriever = MockFormPartialRetriever
  }

  def charityDeleteControllerNotAuthorised = new CharityDeleteConfirmController {
    override val authConnector = mockAuthConnector
    override val cachingConnector = mockCachingConnector
    override val ihtConnector = mockIhtConnector
    override implicit val formPartialRetriever: FormPartialRetriever = MockFormPartialRetriever
  }

  val charity1 = CommonBuilder.buildCharity.copy(
    id = Some("1"),
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

  "CharityDeleteConfirmControllerTest" must {
    "redirect to login page on PageLoad if the user is not logged in" in {
      val result = charityDeleteControllerNotAuthorised.onPageLoad("1")(createFakeRequest(isAuthorised = false))
      status(result) must be(SEE_OTHER)
      redirectLocation(result) must be(Some(loginUrl))
    }

    "redirect to login page on Submit if the user is not logged in" in {
      val result = charityDeleteControllerNotAuthorised.onSubmit("1")(createFakeRequest(isAuthorised = false))
      status(result) must be(SEE_OTHER)
      redirectLocation(result) must be(Some(loginUrl))
    }

    "display main section title message on page load" in {
      createMockForRegistration(mockCachingConnector, getRegDetailsFromCache = true)
      createMocksForApplication(mockCachingConnector,
        mockIhtConnector,
        appDetails = Some(applicationDetailsTwoCharities),
        getAppDetails = true)

      val result = charityDeleteConfirmController.onPageLoad("1")(createFakeRequest())
      status(result) mustBe OK
      contentAsString(result) must include(messagesApi("page.iht.application.exemptions.charityDelete.sectionTitle"))
    }
  }

  "display a confirm and delete button" in {
    createMockForRegistration(mockCachingConnector, getRegDetailsFromCache = true)
    createMocksForApplication(mockCachingConnector,
      mockIhtConnector,
      appDetails = Some(applicationDetailsTwoCharities),
      getAppDetails = true)

    val result = charityDeleteConfirmController.onPageLoad("1")(createFakeRequest())
    status(result) mustBe OK
    contentAsString(result) must include(messagesApi("site.button.confirmDelete"))

  }

  "when given a valid charity id the charity should redirect" in {
    createMockForRegistration(mockCachingConnector, getRegDetailsFromCache = true)
    createMocksForApplication(mockCachingConnector,
      mockIhtConnector,
      appDetails = Some(applicationDetailsTwoCharities),
      getAppDetails = true,
      saveAppDetails = true)

    val result = charityDeleteConfirmController.onSubmit("1")(createFakeRequest())

    status(result) mustBe(SEE_OTHER)
    redirectLocation(result) must be(Some(routes.CharitiesOverviewController.onPageLoad().url))
  }

  "when given a valid charity id the charity must be deleted in load" in {
    createMockForRegistration(mockCachingConnector, getRegDetailsFromCache = true)
    createMocksForApplication(mockCachingConnector,
      mockIhtConnector,
      appDetails = Some(applicationDetailsTwoCharities),
      getAppDetails = true,
      saveAppDetails = true)

    val result = charityDeleteConfirmController.onSubmit("1")(createFakeRequest())

    status(result) mustBe(SEE_OTHER)
    val capturedValue = verifyAndReturnSavedApplicationDetails(mockIhtConnector)
    capturedValue.charities.length mustBe 1
    capturedValue.charities(0).id.getOrElse("") mustBe("2")
  }

  "when given a invalid charity id during the load, we should get and internal server error" in {
    createMockForRegistration(mockCachingConnector, getRegDetailsFromCache = true)
    createMocksForApplication(mockCachingConnector,
      mockIhtConnector,
      appDetails = Some(applicationDetailsTwoCharities),
      getAppDetails = true,
      saveAppDetails = true)

    val result = charityDeleteConfirmController.onPageLoad("999999")(createFakeRequest())

    status(result) mustBe(INTERNAL_SERVER_ERROR)
  }

  "when given a invalid charity id during the submit, we should get and internal server error" in {
    createMockForRegistration(mockCachingConnector, getRegDetailsFromCache = true)
    createMocksForApplication(mockCachingConnector,
      mockIhtConnector,
      appDetails = Some(applicationDetailsTwoCharities),
      getAppDetails = true,
      saveAppDetails = true)

    val result = charityDeleteConfirmController.onSubmit("999999")(createFakeRequest())

    status(result) mustBe(INTERNAL_SERVER_ERROR)
  }
}

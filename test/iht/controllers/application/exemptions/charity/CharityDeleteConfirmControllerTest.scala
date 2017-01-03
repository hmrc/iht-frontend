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

package iht.controllers.application.exemptions.charity

import iht.connector.{CachingConnector, IhtConnector}
import iht.controllers.application.ApplicationControllerTest
import iht.testhelpers.CommonBuilder
import iht.testhelpers.MockObjectBuilder._
import org.scalatest.BeforeAndAfter
import play.api.i18n.Messages
import play.api.test.Helpers._

class CharityDeleteConfirmControllerTest extends ApplicationControllerTest with BeforeAndAfter {
  var mockCachingConnector = mock[CachingConnector]
  var mockIhtConnector = mock[IhtConnector]

  before {
    mockIhtConnector = mock[IhtConnector]
  }

  def charityDeleteConfirmController = new CharityDeleteConfirmController {
    override val authConnector = createFakeAuthConnector(isAuthorised = true)
    override val cachingConnector = mockCachingConnector
    override val ihtConnector = mockIhtConnector
  }

  def charityDeleteControllerNotAuthorised = new CharityDeleteConfirmController {
    override val authConnector = createFakeAuthConnector(isAuthorised = false)
    override val cachingConnector = mockCachingConnector
    override val ihtConnector = mockIhtConnector
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
      val result = charityDeleteControllerNotAuthorised.onPageLoad("1")(createFakeRequest())
      status(result) should be(SEE_OTHER)
      redirectLocation(result) should be(Some(loginUrl))
    }

    "redirect to login page on Submit if the user is not logged in" in {
      val result = charityDeleteControllerNotAuthorised.onSubmit("1")(createFakeRequest())
      status(result) should be(SEE_OTHER)
      redirectLocation(result) should be(Some(loginUrl))
    }

    "display main section title message on page load" in {
      createMockForRegistration(mockCachingConnector, getRegDetailsFromCache = true)
      createMocksForApplication(mockCachingConnector,
        mockIhtConnector,
        appDetails = Some(applicationDetailsTwoCharities),
        getAppDetails = true)

      val result = charityDeleteConfirmController.onPageLoad("1")(createFakeRequest())
      status(result) shouldBe OK
      contentAsString(result) should include(Messages("page.iht.application.exemptions.charityDelete.sectionTitle"))
    }

    "display main back link text" in {
      createMockForRegistration(mockCachingConnector, getRegDetailsFromCache = true)
      createMocksForApplication(mockCachingConnector,
        mockIhtConnector,
        appDetails = Some(applicationDetailsTwoCharities),
        getAppDetails = true)

      val result = charityDeleteConfirmController.onPageLoad("1")(createFakeRequest())
      status(result) shouldBe OK
      contentAsString(result) should include(Messages("iht.estateReport.exemptions.charities.returnToAssetsLeftToCharities"))
    }

    "contain href with link back to overview page" in {
      pending
      createMockForRegistration(mockCachingConnector, getRegDetailsFromCache = true)
      createMocksForApplication(mockCachingConnector,
        mockIhtConnector,
        appDetails = Some(applicationDetailsTwoCharities),
        getAppDetails = true)

      val result = charityDeleteConfirmController.onPageLoad("1")(createFakeRequest())
      status(result) shouldBe OK
      //TODO Should link to charities overview page when it exists
    }
  }

  "display a confirm and delete button" in {
    createMockForRegistration(mockCachingConnector, getRegDetailsFromCache = true)
    createMocksForApplication(mockCachingConnector,
      mockIhtConnector,
      appDetails = Some(applicationDetailsTwoCharities),
      getAppDetails = true)

    val result = charityDeleteConfirmController.onPageLoad("1")(createFakeRequest())
    status(result) shouldBe OK
    contentAsString(result) should include(Messages("site.button.confirmDelete"))

  }

  "display the charity name in" in {
    createMockForRegistration(mockCachingConnector, getRegDetailsFromCache = true)
    createMocksForApplication(mockCachingConnector,
      mockIhtConnector,
      appDetails = Some(applicationDetailsTwoCharities),
      getAppDetails = true)

    val result = charityDeleteConfirmController.onPageLoad("1")(createFakeRequest())
    status(result) shouldBe OK
    contentAsString(result) should include(charity1.name.get)
  }

  "when given a valid charity id the charity should redirect" in {
    createMockForRegistration(mockCachingConnector, getRegDetailsFromCache = true)
    createMocksForApplication(mockCachingConnector,
      mockIhtConnector,
      appDetails = Some(applicationDetailsTwoCharities),
      getAppDetails = true,
      saveAppDetails = true)

    val result = charityDeleteConfirmController.onSubmit("1")(createFakeRequest())

    status(result) shouldBe(SEE_OTHER)
    redirectLocation(result) should be(Some(routes.CharitiesOverviewController.onPageLoad().url))
  }

  "when given a valid charity id the charity should be deleted in load" in {
    createMockForRegistration(mockCachingConnector, getRegDetailsFromCache = true)
    createMocksForApplication(mockCachingConnector,
      mockIhtConnector,
      appDetails = Some(applicationDetailsTwoCharities),
      getAppDetails = true,
      saveAppDetails = true)

    val result = charityDeleteConfirmController.onSubmit("1")(createFakeRequest())

    status(result) shouldBe(SEE_OTHER)
    val capturedValue = verifyAndReturnSavedApplicationDetails(mockIhtConnector)
    capturedValue.charities.length shouldBe 1
    capturedValue.charities(0).id.getOrElse("") shouldBe("2")
  }

  "when given a invalid charity id during the load, we should get and internal server error" in {
    createMockForRegistration(mockCachingConnector, getRegDetailsFromCache = true)
    createMocksForApplication(mockCachingConnector,
      mockIhtConnector,
      appDetails = Some(applicationDetailsTwoCharities),
      getAppDetails = true,
      saveAppDetails = true)

    val result = charityDeleteConfirmController.onPageLoad("999999")(createFakeRequest())

    status(result) shouldBe(INTERNAL_SERVER_ERROR)
  }

  "when given a invalid charity id during the submit, we should get and internal server error" in {
    createMockForRegistration(mockCachingConnector, getRegDetailsFromCache = true)
    createMocksForApplication(mockCachingConnector,
      mockIhtConnector,
      appDetails = Some(applicationDetailsTwoCharities),
      getAppDetails = true,
      saveAppDetails = true)

    val result = charityDeleteConfirmController.onSubmit("999999")(createFakeRequest())

    status(result) shouldBe(INTERNAL_SERVER_ERROR)
  }
}

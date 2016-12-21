/*
 * Copyright 2016 HM Revenue & Customs
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

import iht.connector.{CachingConnector, IhtConnector}
import iht.controllers.application.ApplicationControllerTest
import iht.testhelpers.CommonBuilder
import iht.testhelpers.MockObjectBuilder._
import org.scalatest.BeforeAndAfter
import play.api.i18n.Messages
import play.api.test.Helpers._

class QualifyingBodyDeleteConfirmControllerTest extends ApplicationControllerTest with BeforeAndAfter {
  var mockCachingConnector = mock[CachingConnector]
  var mockIhtConnector = mock[IhtConnector]

  before {
    mockIhtConnector = mock[IhtConnector]
  }

  def qualifyingBodyDeleteConfirmController = new QualifyingBodyDeleteConfirmController {
    override val authConnector = createFakeAuthConnector(isAuthorised = true)
    override val cachingConnector = mockCachingConnector
    override val ihtConnector = mockIhtConnector
  }

  def qualifyingBodyDeleteControllerNotAuthorised = new QualifyingBodyDeleteConfirmController {
    override val authConnector = createFakeAuthConnector(isAuthorised = false)
    override val cachingConnector = mockCachingConnector
    override val ihtConnector = mockIhtConnector
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

  val qualifyingBody3 = CommonBuilder.buildQualifyingBody.copy(
    id = Some("3"),
    name = Some("A QualifyingBody 3"),
    totalValue = Some(47.45)
  )

  val applicationDetailsTwoQualifyingBodies = CommonBuilder.buildApplicationDetails copy (qualifyingBodies
    = Seq(qualifyingBody1, qualifyingBody2))

  "QualifyingBodyDeleteConfirmControllerTest" must {
    "redirect to login page on PageLoad if the user is not logged in" in {
      val result = qualifyingBodyDeleteControllerNotAuthorised.onPageLoad("1")(createFakeRequest())
      status(result) should be(SEE_OTHER)
      redirectLocation(result) should be(Some(loginUrl))
    }

    "redirect to login page on Submit if the user is not logged in" in {
      val result = qualifyingBodyDeleteControllerNotAuthorised.onSubmit("1")(createFakeRequest())
      status(result) should be(SEE_OTHER)
      redirectLocation(result) should be(Some(loginUrl))
    }

    "display main section title message on page load" in {
      createMockForRegistration(mockCachingConnector, getRegDetailsFromCache = true)
      createMocksForApplication(mockCachingConnector,
        mockIhtConnector,
        appDetails = Some(applicationDetailsTwoQualifyingBodies),
        getAppDetails = true)

      val result = qualifyingBodyDeleteConfirmController.onPageLoad("1")(createFakeRequest())
      status(result) shouldBe OK
      contentAsString(result) should include(Messages("iht.estateReport.exemptions.qualifyingBodies.confirmDeleteQualifyingBody"))
    }

    "display main back link text" in {
      createMockForRegistration(mockCachingConnector, getRegDetailsFromCache = true)
      createMocksForApplication(mockCachingConnector,
        mockIhtConnector,
        appDetails = Some(applicationDetailsTwoQualifyingBodies),
        getAppDetails = true)

      val result = qualifyingBodyDeleteConfirmController.onPageLoad("1")(createFakeRequest())
      status(result) shouldBe OK
      contentAsString(result) should include(Messages("iht.estateReport.exemptions.qualifyingBodies.returnToAssetsLeftToQualifyingBodies"))
    }

    "contain href with link back to overview page" in {
      pending
      createMockForRegistration(mockCachingConnector, getRegDetailsFromCache = true)
      createMocksForApplication(mockCachingConnector,
        mockIhtConnector,
        appDetails = Some(applicationDetailsTwoQualifyingBodies),
        getAppDetails = true)

      val result = qualifyingBodyDeleteConfirmController.onPageLoad("1")(createFakeRequest())
      status(result) shouldBe OK
      //TODO Should link to qualifyingBodies overview page when it exists
    }
  }

  "display a confirm and delete button" in {
    createMockForRegistration(mockCachingConnector, getRegDetailsFromCache = true)
    createMocksForApplication(mockCachingConnector,
      mockIhtConnector,
      appDetails = Some(applicationDetailsTwoQualifyingBodies),
      getAppDetails = true)

    val result = qualifyingBodyDeleteConfirmController.onPageLoad("1")(createFakeRequest())
    status(result) shouldBe OK
    contentAsString(result) should include(Messages("site.button.confirmDelete"))

  }

  "display the qualifyingBody name in" in {
    createMockForRegistration(mockCachingConnector, getRegDetailsFromCache = true)
    createMocksForApplication(mockCachingConnector,
      mockIhtConnector,
      appDetails = Some(applicationDetailsTwoQualifyingBodies),
      getAppDetails = true)

    val result = qualifyingBodyDeleteConfirmController.onPageLoad("1")(createFakeRequest())
    status(result) shouldBe OK
    contentAsString(result) should include(qualifyingBody1.name.get)
  }

  "when given a valid qualifyingBody id the qualifyingBody should redirect" in {
    createMockForRegistration(mockCachingConnector, getRegDetailsFromCache = true)
    createMocksForApplication(mockCachingConnector,
      mockIhtConnector,
      appDetails = Some(applicationDetailsTwoQualifyingBodies),
      getAppDetails = true,
      saveAppDetails = true)

    val result = qualifyingBodyDeleteConfirmController.onSubmit("1")(createFakeRequest())

    status(result) shouldBe(SEE_OTHER)
    redirectLocation(result) should be(Some(routes.QualifyingBodiesOverviewController.onPageLoad().url))
  }

  "when given a valid qualifyingBody id the qualifyingBody should be deleted in load" in {
    createMockForRegistration(mockCachingConnector, getRegDetailsFromCache = true)
    createMocksForApplication(mockCachingConnector,
      mockIhtConnector,
      appDetails = Some(applicationDetailsTwoQualifyingBodies),
      getAppDetails = true,
      saveAppDetails = true)

    val result = qualifyingBodyDeleteConfirmController.onSubmit("1")(createFakeRequest())

    status(result) shouldBe(SEE_OTHER)
    val capturedValue = verifyAndReturnSavedApplicationDetails(mockIhtConnector)
    capturedValue.qualifyingBodies.length shouldBe 1
    capturedValue.qualifyingBodies(0).id.getOrElse("") shouldBe("2")
  }

  "when given a invalid qualifyingBody id during the load, we should get and internal server error" in {
    createMockForRegistration(mockCachingConnector, getRegDetailsFromCache = true)
    createMocksForApplication(mockCachingConnector,
      mockIhtConnector,
      appDetails = Some(applicationDetailsTwoQualifyingBodies),
      getAppDetails = true,
      saveAppDetails = true)

    val result = qualifyingBodyDeleteConfirmController.onPageLoad("999999")(createFakeRequest())

    status(result) shouldBe(INTERNAL_SERVER_ERROR)
  }

  "when given a invalid qualifyingBody id during the submit, we should get and internal server error" in {
    createMockForRegistration(mockCachingConnector, getRegDetailsFromCache = true)
    createMocksForApplication(mockCachingConnector,
      mockIhtConnector,
      appDetails = Some(applicationDetailsTwoQualifyingBodies),
      getAppDetails = true,
      saveAppDetails = true)

    val result = qualifyingBodyDeleteConfirmController.onSubmit("999999")(createFakeRequest())

    status(result) shouldBe(INTERNAL_SERVER_ERROR)
  }
}

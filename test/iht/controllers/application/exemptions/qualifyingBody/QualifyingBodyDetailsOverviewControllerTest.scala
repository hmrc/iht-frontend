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

package iht.controllers.application.exemptions.qualifyingBody

import iht.connector.{CachingConnector, IhtConnector}
import iht.controllers.application.ApplicationControllerTest
import iht.models.application.ApplicationDetails
import iht.testhelpers.CommonBuilder
import iht.testhelpers.MockObjectBuilder._
import iht.utils.CommonHelper
import play.api.i18n.Messages
import play.api.test.FakeHeaders
import play.api.test.Helpers._
import uk.gov.hmrc.play.http.HeaderCarrier

class QualifyingBodyDetailsOverviewControllerTest extends ApplicationControllerTest {
  val mockCachingConnector = mock[CachingConnector]
  val mockIhtConnector = mock[IhtConnector]

  def qualifyingBodyDetailsOverviewController = new QualifyingBodyDetailsOverviewController {
    override val cachingConnector = mockCachingConnector
    override val authConnector = createFakeAuthConnector(isAuthorised = true)
    override val ihtConnector = mockIhtConnector
    override val isWhiteListEnabled = false
  }

  def qualifyingBodyDetailsOverviewControllerNotAuthorised = new QualifyingBodyDetailsOverviewController {
    override val cachingConnector = mockCachingConnector
    override val authConnector = createFakeAuthConnector(isAuthorised = false)
    override val ihtConnector = mockIhtConnector
    override val isWhiteListEnabled = false
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
        storeAppDetailsInCache = true,
        getAppDetailsTempFromCache = true)

      val result = qualifyingBodyDetailsOverviewController.onPageLoad()(createFakeRequest())
      status(result) should be(OK)
    }

    "display the page title on page load" in {
      val result = qualifyingBodyDetailsOverviewController.onPageLoad()(createFakeRequest())
      status(result) should be(OK)
      contentAsString(result) should include(
        Messages("iht.estateReport.assets.qualifyingBodyAdd"))
    }

    "display qualifyingBody name question on page" in {
      val result = qualifyingBodyDetailsOverviewController.onPageLoad()(createFakeRequest())
      status(result) should be(OK)
      contentAsString(result) should include(Messages(
        "iht.estateReport.qualifyingBodies.qualifyingBodyName"))
    }

    "display value of assets left to qualifyingBody question on the page" in {
      val result = qualifyingBodyDetailsOverviewController.onPageLoad()(createFakeRequest())
      status(result) should be(OK)
      contentAsString(result) should include(Messages(
        "page.iht.application.exemptions.overview.qualifyingBody.detailsOverview.value.title"))
    }

    "load the page with qualifyingBody details where one qualifyingBody stored" in {

      val firstQualifyingBody = iht.testhelpers.CommonBuilder.qualifyingBody
      val firstQualifyingBodyName = iht.testhelpers.CommonBuilder.qualifyingBody.name.get

      val applicationModel = new ApplicationDetails(qualifyingBodies = Seq(firstQualifyingBody))

      createMocksForApplication(mockCachingConnector,
        mockIhtConnector,
        appDetails = Some(applicationModel),
        getAppDetails = true,
        saveAppDetails = true,
        storeAppDetailsInCache = true)

      val result = qualifyingBodyDetailsOverviewController.onEditPageLoad("1")(createFakeRequest())

      status(result) should be(OK)
      contentAsString(result) should include(firstQualifyingBodyName)
    }

    "load the page with details second qualifyingBody details where two qualifyingBodies stored" in {

      val firstQualifyingBody = iht.testhelpers.CommonBuilder.qualifyingBody
      val secondQualifyingBody = iht.testhelpers.CommonBuilder.qualifyingBody copy (
        id = Some("2"), name = Some("A Charity 2"),
        totalValue = Some(BigDecimal(5000)))
      val secondQualifyingBodyName = secondQualifyingBody.name.get
      val secondQualifyingBodyValue = CommonHelper.numberWithCommas(secondQualifyingBody.totalValue.get)

      val applicationModel = new ApplicationDetails(qualifyingBodies = Seq(firstQualifyingBody, secondQualifyingBody))

      createMocksForApplication(mockCachingConnector,
        mockIhtConnector,
        appDetails = Some(applicationModel),
        getAppDetails = true,
        saveAppDetails = true,
        storeAppDetailsInCache = true)

      val result = qualifyingBodyDetailsOverviewController.onEditPageLoad("2")(createFakeRequest())

      status(result) should be(OK)
      contentAsString(result) should include(secondQualifyingBodyName)
      contentAsString(result) should include(secondQualifyingBodyValue)
      contentAsString(result) should include(Messages("iht.change"))
    }

    "throw RuntimeException when qualifyingBody ID is accessed that does not exist" in {

      val firstQualifyingBody = iht.testhelpers.CommonBuilder.qualifyingBody
      val applicationModel = new ApplicationDetails(qualifyingBodies = Seq(firstQualifyingBody))

      createMocksForApplication(mockCachingConnector,
        mockIhtConnector,
        appDetails = Some(applicationModel),
        getAppDetails = true,
        saveAppDetails = true,
        storeAppDetailsInCache = true)

      intercept[RuntimeException] {
        val result = qualifyingBodyDetailsOverviewController.onEditPageLoad("2")(createFakeRequest())
        status(result) should be (INTERNAL_SERVER_ERROR)
      }
    }
  }
}

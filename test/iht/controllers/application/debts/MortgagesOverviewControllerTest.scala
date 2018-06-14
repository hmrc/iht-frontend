/*
 * Copyright 2018 HM Revenue & Customs
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

import iht.connector.{CachingConnector, IhtConnector}
import iht.controllers.application.ApplicationControllerTest
import iht.models.application.assets.Property
import iht.models.application.debts._
import iht.testhelpers.{CommonBuilder, MockFormPartialRetriever}
import iht.testhelpers.MockObjectBuilder._
import play.api.i18n.Messages
import play.api.i18n.Messages.Implicits._
import play.api.Play.current
import play.api.test.Helpers._
import uk.gov.hmrc.play.partials.FormPartialRetriever

import scala.concurrent.{Await, Future}
import scala.concurrent.duration._
import uk.gov.hmrc.http.HeaderCarrier

class MortgagesOverviewControllerTest extends ApplicationControllerTest {
  implicit val hc = new HeaderCarrier()
  var mockCachingConnector = mock[CachingConnector]
  val mockIhtConnector = mock[IhtConnector]

  def mortgagesOverviewController = new MortgagesOverviewController {
    override val cachingConnector = mockCachingConnector
    override val authConnector = createFakeAuthConnector(isAuthorised = true)
    override val ihtConnector = mockIhtConnector

    override implicit val formPartialRetriever: FormPartialRetriever = MockFormPartialRetriever
  }

  before {
    mockCachingConnector = mock[CachingConnector]
  }

  "MortgagesOverviewController controller" must {

    "return OK on Page Load where app details exist and mortgages exist" in {
      val propertyList = List[Property]( Property(Some("1"), Some(CommonBuilder.DefaultUkAddress),
        Some("Deceased's home"), Some("Deceased only"), Some("Leasehold"), Some(BigDecimal(9))) )

      val applicationDetails =  CommonBuilder.buildApplicationDetails copy (
        allLiabilities = Some( AllLiabilities(mortgages = Some(MortgageEstateElement(isOwned = Some(true), mortgageList=List())))),
        propertyList = propertyList)

      createMocksForApplication(mockCachingConnector,
        mockIhtConnector,
        appDetails = Some(applicationDetails),
        getAppDetails = true)
      
      val result = mortgagesOverviewController.onPageLoad()(createFakeRequest())
      status(result) should be (OK)
    }

    "return OK on Page Load where app details exist, liabilities exist and mortgages don't exist" in {
      val propertyList = List[Property]( Property(Some("1"), Some(CommonBuilder.DefaultUkAddress),
        Some("Deceased's home"), Some("Deceased only"), Some("Leasehold"), Some(BigDecimal(9))) )

      val applicationDetails  =  CommonBuilder.buildApplicationDetails copy (
        allLiabilities = Some( AllLiabilities()),
        propertyList = propertyList)

      createMocksForApplication(mockCachingConnector,
        mockIhtConnector,
        appDetails = Some(applicationDetails),
        getAppDetails = true)

      val result = mortgagesOverviewController.onPageLoad()(createFakeRequest())
      status(result) should be (OK)
    }

    "return OK on Page Load and find no properties added message where there is no properties" in {

      val applicationDetails  =  CommonBuilder.buildApplicationDetails copy (
        allLiabilities = Some( AllLiabilities()))

      createMocksForApplication(mockCachingConnector,
        mockIhtConnector,
        appDetails = Some(applicationDetails),
        getAppDetails = true)

      val result = mortgagesOverviewController.onPageLoad()(createFakeRequest())
      status(result) should be (OK)
      contentAsString(result) should include(messagesApi("page.iht.application.debts.mortgages.noProperties.description"))
    }

    "return OK on Page Load where app details exist and liabilities don't exist" in {
      val propertyList = List[Property]( Property(Some("1"), Some(CommonBuilder.DefaultUkAddress),
        Some("Deceased's home"), Some("Deceased only"), Some("Leasehold"), Some(BigDecimal(9))) )

      val applicationDetails  = CommonBuilder.buildApplicationDetails copy (
        allLiabilities = None,
        propertyList = propertyList)

      createMocksForApplication(mockCachingConnector,
        mockIhtConnector,
        appDetails = Some(applicationDetails),
        getAppDetails = true)

      val result = mortgagesOverviewController.onPageLoad()(createFakeRequest())
      status(result) should be (OK)
    }

    "return OK on Page Load where app details don't exist" in {
      createMocksForApplication(mockCachingConnector,
        mockIhtConnector,
        appDetails = None,
        getAppDetails = true)

      val result = mortgagesOverviewController.onPageLoad()(createFakeRequest())
      status(result) should be (OK)
    }

    "return OK on Page Load where no IHT reference" in {
      val registrationDetails = CommonBuilder.buildRegistrationDetails copy (
        deceasedDetails = Some(CommonBuilder.buildDeceasedDetails),
        ihtReference = None
        )

      createMockToGetRegDetailsFromCacheNoOption(mockCachingConnector, Future.successful(Some(registrationDetails)))

      a[RuntimeException] shouldBe thrownBy {
        await(mortgagesOverviewController.onPageLoad()(createFakeRequest()))
      }
    }

    "return exception on Page Load where no reg details" in {
      createMockToThrowExceptionWhileGettingExistingRegDetails(mockCachingConnector, "bla")

      a[RuntimeException] shouldBe thrownBy {
        Await.result(mortgagesOverviewController.onPageLoad()(createFakeRequest()), Duration.Inf)
      }
    }

    behave like controllerOnPageLoadWithNoExistingRegistrationDetails(mockCachingConnector,
      mortgagesOverviewController.onPageLoad(createFakeRequest()))
  }
}

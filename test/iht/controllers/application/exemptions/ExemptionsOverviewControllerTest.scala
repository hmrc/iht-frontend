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

package iht.controllers.application.exemptions

import iht.config.AppConfig
import iht.controllers.application.ApplicationControllerTest
import iht.testhelpers.CommonBuilder
import iht.views.html.application.exemption.exemptions_overview
import org.scalatest.BeforeAndAfter
import play.api.mvc.MessagesControllerComponents
import play.api.test.Helpers._
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController

class ExemptionsOverviewControllerTest extends ApplicationControllerTest with BeforeAndAfter{

  protected abstract class TestController extends FrontendController(mockControllerComponents) with ExemptionsOverviewController {
    override val cc: MessagesControllerComponents = mockControllerComponents
    override implicit val appConfig: AppConfig = mockAppConfig
    override val exemptionsOverviewView: exemptions_overview = app.injector.instanceOf[exemptions_overview]
  }

  def exemptionsSummaryController = new TestController {
    override val authConnector = mockAuthConnector
    override val cachingConnector = mockCachingConnector
    override val ihtConnector = mockIhtConnector
  }
  def exemptionsSummaryControllerNotAuthorised = new TestController {
    override val authConnector = mockAuthConnector
    override val cachingConnector = mockCachingConnector
    override val ihtConnector = mockIhtConnector
  }

  val defaultPartnerExemption = CommonBuilder.buildPartnerExemption
  val defaultCharityExemption = Seq(CommonBuilder.charity)
  val defaultQualifyingBodiesExemption = Seq(CommonBuilder.qualifyingBody)
  val allExemptions = CommonBuilder.buildAllExemptions copy (partner=Some(defaultPartnerExemption))
  val applicationDetails = CommonBuilder.buildApplicationDetails copy (allExemptions = Some(allExemptions))



  "ExemptionsOverviewController" must {
    "respond with OK on page load" in {

      createMocksForApplication(mockCachingConnector,
        mockIhtConnector,
        appDetails = Some(applicationDetails),
        getAppDetails = true,
        saveAppDetails = true,
        storeAppDetailsInCache = true)

      val result = exemptionsSummaryController.onPageLoad (createFakeRequest())
      status(result) must be (OK)
      contentAsString(result) must include(messagesApi("page.iht.application.exemptions.title"))
    }


    "respond with amended applicationDetails object with hasSeenExemptionGuidance set to true where it was false " +
      "previously on page load" in {
      val applicationDetails = CommonBuilder.buildApplicationDetails copy (allExemptions = Some(allExemptions),
        hasSeenExemptionGuidance=Some(false))

      createMocksForApplication(mockCachingConnector,
        mockIhtConnector,
        appDetails = Some(applicationDetails),
        getAppDetails = true,
        saveAppDetails = true,
        storeAppDetailsInCache = true)

      await(exemptionsSummaryController.onPageLoad (createFakeRequest()))

      val adStored = verifyAndReturnSavedApplicationDetails(mockIhtConnector)
      adStored.hasSeenExemptionGuidance mustBe Some(true)
    }

    "respond with amended applicationDetails object with hasSeenExemptionGuidance set to true where it was None " +
      "previously on page load" in {
      val applicationDetails = CommonBuilder.buildApplicationDetails copy (allExemptions = Some(allExemptions),
        hasSeenExemptionGuidance=None)

      createMocksForApplication(mockCachingConnector,
        mockIhtConnector,
        appDetails = Some(applicationDetails),
        getAppDetails = true,
        saveAppDetails = true,
        storeAppDetailsInCache = true)

      await(exemptionsSummaryController.onPageLoad (createFakeRequest()))

      val adStored = verifyAndReturnSavedApplicationDetails(mockIhtConnector)
      adStored.hasSeenExemptionGuidance mustBe Some(true)
    }

    "redirect to ida login page on page load if user is not logged in" in {
      createMocksForApplication(mockCachingConnector,
        mockIhtConnector,
        appDetails = Some(applicationDetails),
        getAppDetails = true,
        saveAppDetails = true,
        storeAppDetailsInCache = true)

      val result = exemptionsSummaryControllerNotAuthorised.onPageLoad(createFakeRequest(isAuthorised = false))
      redirectLocation(result) must be (Some(loginUrl))

    }

    "respond with error on page load when there is no ApplicationDetails" in {
      createMocksForApplication(mockCachingConnector,
        mockIhtConnector,
        appDetails = None,
        getAppDetails = true,
        saveAppDetails = true,
        storeAppDetailsInCache = true)

      a[RuntimeException] mustBe thrownBy {
        await(exemptionsSummaryController.onPageLoad (createFakeRequest()))
      }
    }

    "display correct value for partner exemption if asset value entered" in {
      createMocksForApplication(mockCachingConnector,
        mockIhtConnector,
        appDetails = Some(applicationDetails),
        getAppDetails = true,
        saveAppDetails = true,
        storeAppDetailsInCache = true)

      val result = exemptionsSummaryController.onPageLoad(createFakeRequest())
      status(result) must be (OK)
      contentAsString(result) must include ("120")
    }

    "display correct value for qualifying body total exemption value if defined" in {
      createMocksForApplication(mockCachingConnector,
        mockIhtConnector,
        appDetails = Some(applicationDetails.copy(qualifyingBodies=defaultQualifyingBodiesExemption)),
        getAppDetails = true,
        saveAppDetails = true,
        storeAppDetailsInCache = true)

      val result = exemptionsSummaryController.onPageLoad(createFakeRequest())
      status(result) must be (OK)
      contentAsString(result) must include ("12,345")
    }
  }
}

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
import iht.controllers.application.exemptions.qualifyingBody.{routes => qualifyingBodyRoutes}
import iht.models.application.ApplicationDetails
import iht.models.application.exemptions.QualifyingBody
import iht.testhelpers.CommonBuilder
import iht.testhelpers.CommonBuilder._
import iht.testhelpers.MockObjectBuilder._
import iht.utils.CommonHelper
import play.api.i18n.Messages
import play.api.test.Helpers._
import uk.gov.hmrc.play.http.HeaderCarrier

class QualifyingBodiesOverviewControllerTest extends ApplicationControllerTest {
  implicit val hc = new HeaderCarrier()
  var mockCachingConnector: CachingConnector = null
  var mockIhtConnector: IhtConnector = null

  val appDetailsWithNoQualifyingBodies = CommonBuilder.buildApplicationDetails.copy(
    allExemptions = Some(CommonBuilder.buildAllExemptions.copy(
      qualifyingBody = Some(CommonBuilder.buildBasicExemptionElement.copy(isSelected = Some(true))))))

  val appDetailsWithQualifyingBodyUnanswered = CommonBuilder.buildApplicationDetails

  val qualifyingBody1 = QualifyingBody(Some("1"), Some("QB 1"), Some(123.45))
  val qualifyingBody2 = QualifyingBody(Some("2"), Some("QB 2"), Some(678.9))

  val appDetailsWithQualifyingBodies = appDetailsWithNoQualifyingBodies.copy(
    qualifyingBodies = Seq(qualifyingBody1, qualifyingBody2)
  )

  def setupMocks(appDetails: ApplicationDetails) = createMocksForApplication(mockCachingConnector,
    mockIhtConnector,
    appDetails = Some(appDetails),
    getAppDetails = true,
    saveAppDetails = true,
    storeAppDetailsInCache = true)

  def request = createFakeRequest()

  def requestUnauthorised = createFakeRequest(isAuthorised = false)

  before {
    mockCachingConnector = mock[CachingConnector]
    mockIhtConnector = mock[IhtConnector]
  }

  def controller = getController()

  def controllerNotAuthorised = getController(authorised = false)

  private def getController(authorised: Boolean = true) = new QualifyingBodiesOverviewController {
    override val cachingConnector = mockCachingConnector
    override val ihtConnector = mockIhtConnector
    override val authConnector = createFakeAuthConnector(isAuthorised = authorised)
    override val isWhiteListEnabled = false
  }

  "Qualifying Bodies Overview Controller" must {
    "redirect to login page on PageLoad if the user is not logged in" in {
      val result = controllerNotAuthorised.onPageLoad()(requestUnauthorised)
      status(result) shouldBe SEE_OTHER
      redirectLocation(result) shouldBe Some(loginUrl)
    }

    "throw an illegal page navigation error when accessed before the Yes/No question is answered" in {
      setupMocks(appDetailsWithQualifyingBodyUnanswered)

      val exception = intercept[Exception] {
        await(controller.onPageLoad()(request))
      }

      exception.getMessage should include("Illegal page navigation")
    }

    "load the page when no qualifying bodies are set up" in {

      val regDetails = buildRegistrationDetails copy (
        deceasedDetails = Some(buildDeceasedDetails), ihtReference = Some("AbC123"))

      setupMocks(appDetailsWithNoQualifyingBodies)

      val result = controller.onPageLoad()(request)
      status(result) shouldBe OK

      val content = contentAsString(result)
      content should include(Messages("iht.estateReport.exemptions.qualifyingBodies.assetsLeftToQualifyingBodies.title"))
      info("section title is present")
      content should include(Messages("page.iht.application.exemptions.qualifyingBodyOverview.lede"))
      info("lede paragraph is present")
      content should include(Messages("iht.estateReport.exemptions.qualifyingBodies.howFindOutQualifies"))
      info("progressive reveal link is present")
      content should include(Messages("iht.estateReport.exemptions.qualifyingBodies.assetLeftToQualifyingBody.helptext"))
      info("progressive reveal text is present")
      content should include(Messages("page.iht.application.exemptions.qualifyingBodyOverview.question",
                                      CommonHelper.getDeceasedNameOrDefaultString(regDetails)))
      info("question label is present")
      content should include(Messages("page.iht.application.exemptions.qualifyingBodyOverview.noQualifyingBodies.text"))
      info("'no qualifying bodies' text is present")
      content should include(Messages("site.link.return.exemptions"))
      info("return to exemptions link is present")
      content should include(iht.controllers.application.exemptions.routes.ExemptionsOverviewController.onPageLoad().url)
      info("link to the exemptions overview is present")
    }

    "include a link to change the Qualifying Bodies question when no qualifying bodies are set up" in {
      setupMocks(appDetailsWithNoQualifyingBodies)

      val result = controller.onPageLoad()(request)
      pending // TODO: Change this to check for the link to Qualifying Bodies question when implemented
      contentAsString(result) should include(qualifyingBodyRoutes.QualifyingBodiesOverviewController.onPageLoad.url)
    }

    "load the page when two qualifying bodies are set up" in {
      setupMocks(appDetailsWithQualifyingBodies)

      val result = controller.onPageLoad()(request)
      status(result) shouldBe OK
      contentAsString(result) should include("QB 1")
      info("qualifying body 1's name is present")
      contentAsString(result) should include("QB 2")
      info("qualifying body 2's name is present")
      contentAsString(result) should include("&pound;123.45")
      info("qualifying body 1's value is present")
      contentAsString(result) should include("&pound;678.9")
      info("qualifying body 2's value is present")

      contentAsString(result) should not include Messages("page.iht.application.exemptions.qualifyingBodyOverview.noQualifyingBodies.text")
      info("'no qualifying bodies' text is not present")

      contentAsString(result) should include(Messages("site.link.return.exemptions"))
      info("return to exemptions link is present")
      contentAsString(result) should include(iht.controllers.application.exemptions.routes.ExemptionsOverviewController.onPageLoad().url)
      info("link to the exemptions overview is present")

      pending // TODO: Change these four lines to check the Change and Delete links for the two qualifying bodies when implemented
      contentAsString(result) should include(qualifyingBodyRoutes.QualifyingBodiesOverviewController.onPageLoad.url)
      contentAsString(result) should include(qualifyingBodyRoutes.QualifyingBodiesOverviewController.onPageLoad.url)
      contentAsString(result) should include(qualifyingBodyRoutes.QualifyingBodiesOverviewController.onPageLoad.url)
      contentAsString(result) should include(qualifyingBodyRoutes.QualifyingBodiesOverviewController.onPageLoad.url)
    }
  }
}

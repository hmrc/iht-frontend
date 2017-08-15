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

package iht.controllers.estateReports

import iht.connector.{CachingConnector, IhtConnector}
import iht.controllers.application.ApplicationControllerTest
import iht.models.application.{ApplicationDetails, IhtApplication}
import iht.testhelpers.MockObjectBuilder._
import iht.testhelpers.{MockFormPartialRetriever, CommonBuilder, TestHelper}
import iht.utils.CommonHelper
import org.mockito.Matchers._
import org.mockito.Mockito._
import org.mockito.invocation.InvocationOnMock
import org.mockito.stubbing.Answer
import play.api.i18n.MessagesApi
import play.api.i18n.Messages.Implicits._
import play.api.test.FakeHeaders
import play.api.test.Helpers._
import uk.gov.hmrc.play.http.{HeaderCarrier, Upstream4xxResponse}
import uk.gov.hmrc.play.partials.FormPartialRetriever

import scala.concurrent._
/**
 *
 * Created by Vineet Tyagi on 18/06/15.
 *
 */
class YourEstateReportsControllerTest  extends ApplicationControllerTest{

  val mockCachingConnector = mock[CachingConnector]
  val mockIhtConnector = mock[IhtConnector]

  implicit val headerCarrier = FakeHeaders()
  implicit val listOfApplication=prepareDataForPage()

  implicit val hc = new HeaderCarrier
  // Create controller object and pass in mock.
  def yourEstateReportsController = new YourEstateReportsController {
    override val cachingConnector = mockCachingConnector
    override val ihtConnector = mockIhtConnector
    override val authConnector = createFakeAuthConnector(isAuthorised=true)

    override implicit val formPartialRetriever: FormPartialRetriever = MockFormPartialRetriever
  }

  def yourEstateReportsControllerNotAuthorised = new YourEstateReportsController {
    override val cachingConnector = mockCachingConnector
    override val ihtConnector = mockIhtConnector
    override val authConnector = createFakeAuthConnector(isAuthorised=false)

    override implicit val formPartialRetriever: FormPartialRetriever = MockFormPartialRetriever
  }

  "YourEstateReportsController" must {

    val appDetails = CommonBuilder.buildApplicationDetailsWithAllAssets

    "redirect to GG login page on PageLoad if the user is not logged in" in {
      val result = yourEstateReportsControllerNotAuthorised.onPageLoad(createFakeRequest(isAuthorised = false))
      status(result) should be(SEE_OTHER)
      redirectLocation(result) should be (Some(loginUrl))
    }

    "respond with OK on page load" in {

      createCommonMocksForYourEstateReportsController(mockIhtConnector, ihtAppList = prepareDataForPage)

      val result =yourEstateReportsController.onPageLoad (createFakeRequest())
      status(result) shouldBe (OK)
      contentAsString(result) should include(messagesApi("page.iht.home.title"))
      contentAsString(result) should include(messagesApi("page.iht.home.applicationList.table.guidance.label"))
    }

   "respond with OK on page load when des status Awaiting Return and nothing in secure storage" in {
      val applicationDetails = Some(CommonBuilder.buildApplicationDetails.copy(status=TestHelper.AppStatusNotStarted))

      createCommonMocksForYourEstateReportsController(mockIhtConnector,
        appDetails = applicationDetails,
        ihtAppList = prepareDataForPage1)

      val result =yourEstateReportsController.onPageLoad (createFakeRequest())
      status(result) shouldBe (OK)
      contentAsString(result) should include(messagesApi("iht.notStarted"))
    }

    "respond with OK on page load when des status Awaiting Return and something in secure storage" in {
      val applicationDetails = Some(CommonBuilder.buildApplicationDetails.copy(status=TestHelper.AppStatusInProgress))

      createCommonMocksForYourEstateReportsController(mockIhtConnector,
        appDetails = applicationDetails,
        ihtAppList = prepareDataForPage1)

      val result =yourEstateReportsController.onPageLoad (createFakeRequest())
      status(result) shouldBe (OK)
      contentAsString(result) should include(messagesApi("iht.inProgress"))
    }

    "respond with OK on page load when des status Closed" in {
      val applicationDetails = Some(CommonBuilder.buildApplicationDetails.copy(status=TestHelper.AppStatusInProgress))

      createCommonMocksForYourEstateReportsController(mockIhtConnector,
        appDetails = applicationDetails,
        ihtAppList = prepareDataForPage2)

      val result =yourEstateReportsController.onPageLoad (createFakeRequest())
      status(result) shouldBe (OK)
      contentAsString(result) should include(messagesApi("iht.closed"))
    }

    "respond with OK on page load when des status In Review" in {
      val applicationDetails = Some(CommonBuilder.buildApplicationDetails.copy(status=TestHelper.AppStatusInProgress))

      createCommonMocksForYourEstateReportsController(mockIhtConnector,
        appDetails = applicationDetails,
        ihtAppList = prepareDataForPage3)

      val result =yourEstateReportsController.onPageLoad (createFakeRequest())
      status(result) shouldBe (OK)
      contentAsString(result) should include(messagesApi("iht.inReview"))
    }

    "respond normally when 404 occurs" in {
      when(mockIhtConnector.getCaseList(any())(any()))
        .thenAnswer(new Answer[Future[Seq[IhtApplication]]] {
        override def answer(invocation: InvocationOnMock): Future[Seq[IhtApplication]] = {
          Future.failed(new Upstream4xxResponse("", 404, 404, Map()))
        }})
      val result = yourEstateReportsController.onPageLoad(createFakeRequest())
      status(result) shouldBe (OK)
    }
  }

  private def prepareDataForPage1():Seq[IhtApplication]={
    Seq(CommonBuilder.buildIhtApplication.copy(currentStatus = TestHelper.AppStatusAwaitingReturn))
  }

  private def prepareDataForPage2():Seq[IhtApplication]={
    Seq(CommonBuilder.buildIhtApplication.copy(currentStatus = TestHelper.AppStatusClosed))
  }

  private def prepareDataForPage3():Seq[IhtApplication]={
    Seq(CommonBuilder.buildIhtApplication.copy(currentStatus = TestHelper.AppStatusInReview))
  }

  private def prepareDataForPage4():Seq[IhtApplication]={
    Seq(CommonBuilder.buildIhtApplication.copy(currentStatus = TestHelper.AppStatusClearanceGranted))
  }

  private def prepareDataForPage():Seq[IhtApplication]={
    Seq(CommonBuilder.buildIhtApplication,
      CommonBuilder.buildIhtApplication,
      CommonBuilder.buildIhtApplication,
      CommonBuilder.buildIhtApplication,
      CommonBuilder.buildIhtApplication)
  }

  private def createCommonMocksForYourEstateReportsController(ihtConnector: IhtConnector,
                                                    appDetails: Option[ApplicationDetails] = Some(CommonBuilder.buildApplicationDetails),
                                                    ihtAppList: Seq[IhtApplication],
                                                    getAppDetails: Boolean = true) = {

    createMockToGetCaseList(ihtConnector, ihtAppList)
    createMockToGetRegDetailsFromCacheNoOption(mockCachingConnector, CommonBuilder.buildRegistrationDetails)

    if(getAppDetails) {
      createMockToGetApplicationDetails(ihtConnector, appDetails)
    }
  }
}

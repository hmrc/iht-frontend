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

package iht.controllers.estateReports

import iht.connector.{CachingConnector, IhtConnector}
import iht.controllers.application.ApplicationControllerTest
import iht.models.application.{ApplicationDetails, IhtApplication}
import iht.testhelpers.MockObjectBuilder._
import iht.testhelpers.{CommonBuilder, MockFormPartialRetriever, TestHelper}
import iht.utils.DeceasedInfoHelper
import org.joda.time.LocalDate
import org.mockito.ArgumentMatchers._
import org.mockito.Mockito._
import org.mockito.invocation.InvocationOnMock
import org.mockito.stubbing.Answer
import play.api.test.FakeHeaders
import play.api.test.Helpers._
import uk.gov.hmrc.play.partials.FormPartialRetriever

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent._
import uk.gov.hmrc.http.{HeaderCarrier, Upstream4xxResponse}

import scala.concurrent.duration.Duration

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
    "return the right status from getStatus" when{
      "the app status is Under Enquiry" in{
        val ihtApp = buildIhtApplicationAndSetStatus("Under Enquiry")
        await(yourEstateReportsController.getStatus(nino = "AB123456C", ihtApp , mockIhtConnector)) shouldBe "In Review"
      }

      "the app status is Closed" in{
        val ihtApp = buildIhtApplicationAndSetStatus("Closed")
        await(yourEstateReportsController.getStatus(nino = "AB123456C", ihtApp , mockIhtConnector)) shouldBe "Closed"
      }

      "the app status is Ineligible Application" in{
        val ihtApp = buildIhtApplicationAndSetStatus("Ineligible Application")
        await(yourEstateReportsController.getStatus(nino = "AB123456C", ihtApp , mockIhtConnector)) shouldBe "Ineligible Application"
      }

      "the app status is undefined" in{
        createMockToGetApplicationDetails(mockIhtConnector)

        val ihtApp = buildIhtApplicationAndSetStatus("")
        await(yourEstateReportsController.getStatus(nino = "AB123456C", ihtApp , mockIhtConnector)) shouldBe ""
      }

      "the app status is Awaiting Return" in{
        createMockToGetApplicationDetails(mockIhtConnector)

        val ihtApp = buildIhtApplicationAndSetStatus("Awaiting Return")
        await(yourEstateReportsController.getStatus(nino = "AB123456C", ihtApp , mockIhtConnector)) shouldBe "In Progress"
      }
    }

    "redirect to GG login page on PageLoad if the user is not logged in" in {
      val result = yourEstateReportsControllerNotAuthorised.onPageLoad(createFakeRequest(isAuthorised = false))
      status(result) should be(SEE_OTHER)
      redirectLocation(result) should be (Some(loginUrl))
    }

    "respond with OK on page load" in {

      createCommonMocksForYourEstateReportsController(mockIhtConnector, ihtAppList = prepareDataForPage())

      val result =yourEstateReportsController.onPageLoad (createFakeRequest())
      status(result) shouldBe OK
      contentAsString(result) should include(messagesApi("page.iht.home.title"))
      contentAsString(result) should include(messagesApi("page.iht.home.applicationList.table.guidance.label"))
    }

   "respond with OK on page load when des status Awaiting Return and nothing in secure storage" in {
      val applicationDetails = Some(CommonBuilder.buildApplicationDetails.copy(status=TestHelper.AppStatusNotStarted))

      createCommonMocksForYourEstateReportsController(mockIhtConnector,
        appDetails = applicationDetails,
        ihtAppList = prepareDataForPage1())

      val result =yourEstateReportsController.onPageLoad (createFakeRequest())
      status(result) shouldBe OK
      contentAsString(result) should include(messagesApi("iht.notStarted"))
    }

    "respond with OK on page load when des status Awaiting Return and something in secure storage" in {
      val applicationDetails = Some(CommonBuilder.buildApplicationDetails.copy(status=TestHelper.AppStatusInProgress))

      createCommonMocksForYourEstateReportsController(mockIhtConnector,
        appDetails = applicationDetails,
        ihtAppList = prepareDataForPage1())

      val result =yourEstateReportsController.onPageLoad (createFakeRequest())
      status(result) shouldBe OK
      contentAsString(result) should include(messagesApi("iht.inProgress"))
    }

    "respond with OK on page load when des status Closed" in {
      val applicationDetails = Some(CommonBuilder.buildApplicationDetails.copy(status=TestHelper.AppStatusInProgress))

      createCommonMocksForYourEstateReportsController(mockIhtConnector,
        appDetails = applicationDetails,
        ihtAppList = prepareDataForPage2())

      val result =yourEstateReportsController.onPageLoad (createFakeRequest())
      status(result) shouldBe OK
      contentAsString(result) should include(messagesApi("iht.closed"))
    }

    "respond with OK on page load when des status In Review" in {
      val applicationDetails = Some(CommonBuilder.buildApplicationDetails.copy(status=TestHelper.AppStatusInProgress))

      createCommonMocksForYourEstateReportsController(mockIhtConnector,
        appDetails = applicationDetails,
        ihtAppList = prepareDataForPage3())

      val result =yourEstateReportsController.onPageLoad (createFakeRequest())
      status(result) shouldBe OK
      contentAsString(result) should include(messagesApi("iht.inReview"))
    }

    "respond normally when No Content occurs" in {
      when(mockIhtConnector.getCaseList(any())(any()))
        .thenAnswer(new Answer[Future[Seq[IhtApplication]]] {
        override def answer(invocation: InvocationOnMock): Future[Seq[IhtApplication]] = {
          Future.successful(Nil)
        }})
      val result = yourEstateReportsController.onPageLoad(createFakeRequest())
      status(result) shouldBe OK
    }

    "respond normally when 404 occurs" in {
      when(mockIhtConnector.getCaseList(any())(any()))
        .thenAnswer(new Answer[Future[Seq[IhtApplication]]] {
          override def answer(invocation: InvocationOnMock): Future[Seq[IhtApplication]] = {
            Future.failed(new Upstream4xxResponse("", 404, 404, Map()))
          }})
      val result = yourEstateReportsController.onPageLoad(createFakeRequest())
      status(result) shouldBe OK
    }
  }

  private def buildIhtApplicationAndSetStatus(status: String) = IhtApplication(
    ihtRefNo = "A1234567",
    firstName = "test",
    lastName = "test",
    dateOfBirth = new LocalDate(1992, 12, 11),
    dateOfDeath = new LocalDate(2015, 12, 11),
    nino = "AB123456C",
    entryType = "Free Estate",
    role = "Lead Executor",
    registrationDate = new LocalDate(2015, 12, 12),
    currentStatus = status ,
    acknowledgmentReference = "test"
  )

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

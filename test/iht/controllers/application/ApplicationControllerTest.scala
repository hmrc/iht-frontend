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

package iht.controllers.application

import iht.config.AppConfig
import iht.connector.{CachingConnector, IhtConnector}
import iht.testhelpers.MockObjectBuilder
import iht.utils.IhtSection
import iht.views.ViewTestHelper
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito._
import play.api.i18n.MessagesApi
import play.api.mvc.{AnyContentAsEmpty, Result}
import play.api.test.Helpers.{SEE_OTHER, redirectLocation, status => playStatus}
import play.api.test.{DefaultAwaitTimeout, FakeRequest}
import uk.gov.hmrc.auth.core.{AuthConnector, AuthenticateHeaderParser}
import uk.gov.hmrc.play.partials.FormPartialRetriever

import scala.concurrent.Future

trait ApplicationControllerTest extends ViewTestHelper with DefaultAwaitTimeout with MockObjectBuilder {
  def loginUrl = buildLoginUrl(IhtSection.Application)

  def controllerOnPageLoadWithNoExistingRegistrationDetails(mockCachingConnector: => CachingConnector,
                                                            func: => Future[Result]): Unit = {
    "respond with redirect to application overview when no registration details found in cache" in {
      createMockToGetRegDetailsFromCache(mockCachingConnector, None)
      val result = func
      playStatus(result) must be(SEE_OTHER)
      redirectLocation(result) mustBe Some(iht.controllers.estateReports.routes.YourEstateReportsController.onPageLoad.url)
    }
  }

  val mockCachingConnector: CachingConnector = mock[CachingConnector]
  val mockIhtConnector: IhtConnector = mock[IhtConnector]
  val mockAuthConnector: AuthConnector = mock[AuthConnector]
  val mockPartialRetriever: FormPartialRetriever = mock[FormPartialRetriever]
  val mockMessagesApi: MessagesApi = mock[MessagesApi]
  val mockAppConfig: AppConfig = app.injector.instanceOf[AppConfig]

  override def createFakeRequest(isAuthorised: Boolean = true,
                                 referer: Option[String] = None,
                                 authRetrieveNino: Boolean = true): FakeRequest[AnyContentAsEmpty.type] = {
    if (isAuthorised) {
      if (authRetrieveNino) {
        when(mockAuthConnector.authorise[Option[String]](any(), any())(any(), any())).thenReturn(Future.successful(Some(fakeNino)))
      } else {
        when(mockAuthConnector.authorise[Unit](any(), any())(any(), any())).thenReturn(Future.successful(()))
      }
    } else {
      when(mockAuthConnector.authorise(any(), any())(any(), any())).thenReturn(Future.failed(AuthenticateHeaderParser.parse(Map())))
    }

    super.createFakeRequest(isAuthorised, referer, authRetrieveNino)
  }

  override def beforeEach(): Unit = {
    reset(mockCachingConnector)
    reset(mockIhtConnector)
    reset(mockAuthConnector)
    reset(mockPartialRetriever)
    reset(mockMessagesApi)
    super.beforeEach()
  }
}

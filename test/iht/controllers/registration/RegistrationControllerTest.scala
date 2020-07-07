/*
 * Copyright 2020 HM Revenue & Customs
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

package iht.controllers.registration

import iht.config.AppConfig
import iht.connector.{CachingConnector, IhtConnector}
import iht.metrics.IhtMetrics
import iht.testhelpers.MockObjectBuilder
import iht.utils.IhtSection
import iht.{FakeIhtApp, TestUtils}
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito._
import org.scalatest.BeforeAndAfterEach
import org.scalatestplus.mockito.MockitoSugar
import play.api.i18n.{I18nSupport, Lang, MessagesApi}
import play.api.mvc.{AnyContentAsEmpty, MessagesControllerComponents}
import play.api.test.{FakeHeaders, FakeRequest}
import uk.gov.hmrc.auth.core.{AuthenticateHeaderParser, PlayAuthConnector}
import uk.gov.hmrc.http.HeaderCarrier

import scala.concurrent.Future

trait RegistrationControllerTest extends FakeIhtApp with MockitoSugar with TestUtils with BeforeAndAfterEach with I18nSupport with MockObjectBuilder {

  implicit val messagesApi: MessagesApi = app.injector.instanceOf[MessagesApi]
  def loginUrl = buildLoginUrl(IhtSection.Registration)

  implicit val headerCarrier = FakeHeaders()
  implicit val hc = new HeaderCarrier
  val referrerURL = "http://localhost:9070/inheritance-tax/registration/addExecutor"
  val host = "localhost:9070"

  val mockCachingConnector: CachingConnector = mock[CachingConnector]
  val mockAuthConnector: PlayAuthConnector = mock[PlayAuthConnector]
  val mockIhtMetrics: IhtMetrics = mock[IhtMetrics]
  val mockMessagesApi: MessagesApi = mock[MessagesApi]
  val mockIhtConnector: IhtConnector = mock[IhtConnector]

  val mockControllerComponents: MessagesControllerComponents = app.injector.instanceOf[MessagesControllerComponents]
  implicit val mockAppConfig: AppConfig = app.injector.instanceOf[AppConfig]
  implicit val lang = Lang("en")

  override def beforeEach(): Unit = {
    reset(mockCachingConnector)
    reset(mockAuthConnector)
    reset(mockIhtConnector)
    reset(mockIhtMetrics)
    reset(mockMessagesApi)
    super.beforeEach()
  }

  override def createFakeRequest(isAuthorised: Boolean = true, referer: Option[String] = None, authRetrieveNino: Boolean = true): FakeRequest[AnyContentAsEmpty.type] = {
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

  def request = createFakeRequest(isAuthorised = true)
  def unauthorisedRequest = createFakeRequest(isAuthorised = false)
}

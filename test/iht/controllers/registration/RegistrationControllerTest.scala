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

package iht.controllers.registration

import iht.connector.CachingConnector
import iht.utils.IhtSection
import iht.{FakeIhtApp, TestUtils}
import org.scalatest.BeforeAndAfter
import org.scalatest.mock.MockitoSugar
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.test.FakeHeaders
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.test.UnitSpec

trait RegistrationControllerTest extends UnitSpec with FakeIhtApp with MockitoSugar with TestUtils with BeforeAndAfter with I18nSupport {

  implicit val messagesApi: MessagesApi = app.injector.instanceOf[MessagesApi]
  def loginUrl = buildLoginUrl(IhtSection.Registration)

  implicit val headerCarrier = FakeHeaders()
  implicit val hc = new HeaderCarrier
  val referrerURL = "http://localhost:9070/inheritance-tax/registration/addExecutor"
  val host = "localhost:9070"

  var mockCachingConnector: CachingConnector = null

  def request = createFakeRequest(isAuthorised = true)
  def unauthorisedRequest = createFakeRequest(isAuthorised = false)
}

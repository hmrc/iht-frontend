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

package iht

import iht.config.FrontendAuthConnector
import iht.testhelpers.CommonBuilder
import org.scalatest.Suite
import play.api.mvc.AnyContentAsEmpty
import play.api.test.{FakeApplication, FakeRequest}
import uk.gov.hmrc.domain.{Nino, SaUtr}
import uk.gov.hmrc.play.frontend.auth.connectors.domain._
import uk.gov.hmrc.play.http.{HeaderCarrier, SessionKeys}
import uk.gov.hmrc.play.test.WithFakeApplication

import scala.concurrent.Future

trait FakeIhtApp extends WithFakeApplication {
  this: Suite =>

  override lazy val fakeApplication = FakeApplication()

  val fakeNino = CommonBuilder.DefaultNino

  def createFakeRequest(isAuthorised: Boolean = true): FakeRequest[AnyContentAsEmpty.type] = {
    val userId = "ID-" + fakeNino
    if (isAuthorised) {
      FakeRequest().withSession(
        SessionKeys.sessionId -> s"session-$userId",
        SessionKeys.userId -> userId,
        SessionKeys.token -> "some-gg-token").withHeaders(
        "Accept-Language" -> "en-GB"
      )
    } else {
      FakeRequest().withHeaders(
        "Accept-Language" -> "en-GB"
      )
    }
  }

  def createFakeAuthority(isAuthorised: Boolean = true) = {
    val nino = fakeNino
    if (isAuthorised) {
      Authority("ID-" + nino, Accounts(iht = Some(IhtAccount(s"/iht/${nino}", Nino(nino))),
        sa = Some(SaAccount("/sa/individual/1234567890", SaUtr("1234567890"))),
        paye = Some(PayeAccount(s"/paye/${nino}", Nino(nino)))
      ), None, None, CredentialStrength.Strong, ConfidenceLevel.L200, None, None, None, "")
    } else {
      Authority("ID-NOT_AUTHORISED", Accounts(), None, None, CredentialStrength.None, ConfidenceLevel.L0, None, None, None, "")
    }
  }

  def createFakeAuthConnector(isAuthorised: Boolean = true) = new FrontendAuthConnector {
    override val serviceUrl: String = null
    override lazy val http = null

    override def currentAuthority(implicit hc: HeaderCarrier): Future[Option[Authority]] = {
      Future.successful(Some(createFakeAuthority(isAuthorised)))
    }
  }

  def createFakeRequestWithReferrer(isAuthorised: Boolean = true, referrerURL: String, host: String)
  : FakeRequest[AnyContentAsEmpty.type] = createFakeRequest(isAuthorised = true).withHeaders(("referer", referrerURL), ("host", host))

  def createFakeRequestWithReferrerWithBody(isAuthorised: Boolean = true, referrerURL: String, host: String, data: Seq[(String, String)])
  = createFakeRequest(isAuthorised = true).withHeaders(("referer", referrerURL), ("host", host)).withFormUrlEncodedBody(data: _*)

  def createFakeRequestWithBody(isAuthorised: Boolean = true, data: Seq[(String, String)])
  = createFakeRequest(isAuthorised = true).withFormUrlEncodedBody(data: _*)

  def createFakeRequestWithUri(path: String): FakeRequest[AnyContentAsEmpty.type] = {
     val fr = createFakeRequest()
     FakeRequest(fr.method, path, fr.headers, fr.body, fr.remoteAddress, fr.version, fr.id, fr.tags, fr.secure)
  }
}

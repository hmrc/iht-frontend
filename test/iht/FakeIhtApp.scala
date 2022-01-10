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

package iht

import iht.constants.Constants
import iht.testhelpers.{CommonBuilder, NinoBuilder}
import org.scalatest._
import org.scalatestplus.play.PlaySpec
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.http.HeaderNames
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.mvc.AnyContentAsEmpty
import play.api.test.FakeRequest
import play.api.{Application, Mode}
import scala.language.implicitConversions
import uk.gov.hmrc.http.SessionKeys

import scala.concurrent.Future

trait FakeIhtApp extends PlaySpec with GuiceOneAppPerSuite {
  this: TestSuite =>

  val config: Map[String, _] = Map("application.secret" -> "Its secret",
                      "passcodeAuthentication.enabled" -> false,
                      "passcodeAuthentication.regime" -> "iht",
                      "metrics.enabled" -> true)

//  override implicit lazy val app : Application = new GuiceApplicationBuilder().in(Mode.Test).configure(config).build()
  override def fakeApplication(): Application = new GuiceApplicationBuilder().in(Mode.Test).configure(config).build()

  val fakeNino = CommonBuilder.DefaultNino

  implicit def liftFuture[A](v: A) = Future.successful(v)

  def createFakeRequest(isAuthorised: Boolean = true, referer: Option[String] = None, authRetrieveNino: Boolean = true): FakeRequest[AnyContentAsEmpty.type] = {
    val userId = "ID-" + fakeNino
    if (isAuthorised) {
      FakeRequest().withSession(
        Constants.NINO -> NinoBuilder.randomNino.nino,
        SessionKeys.sessionId -> s"session-$userId",
        "Accept-Language" -> "en-GB"
      )
    } else {
      FakeRequest().withHeaders(
        "Accept-Language" -> "en-GB",
        HeaderNames.REFERER -> referer.getOrElse("")
      )
    }
  }

  def createFakeRequestWithReferrer(isAuthorised: Boolean = true, referrerURL: String, host: String, authRetrieveNino: Boolean = true)
  : FakeRequest[AnyContentAsEmpty.type] = createFakeRequest(isAuthorised = true, authRetrieveNino = authRetrieveNino).withHeaders(("referer", referrerURL), ("host", host))

  def createFakeRequestWithReferrerWithBody(isAuthorised: Boolean = true, referrerURL: String, host: String, data: Seq[(String, String)], authRetrieveNino: Boolean = true)
  = createFakeRequest(isAuthorised = true, authRetrieveNino = authRetrieveNino).withHeaders(("referer", referrerURL), ("host", host)).withFormUrlEncodedBody(data: _*)

  def createFakeRequestWithBody(isAuthorised: Boolean = true, data: Seq[(String, String)])
  = createFakeRequest(isAuthorised = true).withFormUrlEncodedBody(data: _*)

  def createFakeRequestWithUri(path: String, authRetrieveNino: Boolean = true): FakeRequest[AnyContentAsEmpty.type] = {
     val fr = createFakeRequest(authRetrieveNino = authRetrieveNino)
     FakeRequest(fr.method, path, fr.headers, fr.body, fr.remoteAddress, fr.version, fr.id, fr.secure)
  }
}

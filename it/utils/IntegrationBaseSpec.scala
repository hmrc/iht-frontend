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

package utils

import akka.util.Timeout
import org.scalatest.{BeforeAndAfterAll, BeforeAndAfterEach}
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.test.DefaultAwaitTimeout
import play.api.{Application, Configuration}

import scala.concurrent.duration._
import org.scalatest.OptionValues
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpecLike
import play.api.mvc.Result
import scala.language.implicitConversions

trait IntegrationBaseSpec extends AnyWordSpecLike with Matchers with OptionValues
  with GuiceOneAppPerSuite
  with WiremockHelper
  with BeforeAndAfterEach
  with BeforeAndAfterAll
  with DefaultAwaitTimeout {

  override implicit def defaultAwaitTimeout: Timeout = 5.seconds

  import scala.concurrent.duration._
  import scala.concurrent.{Await, Future}
  implicit val defaultTimeout: FiniteDuration = 5.seconds
  implicit def extractAwait[A](future: Future[A]): A = await[A](future)
  def await[A](future: Future[A])(implicit timeout: Duration): A = Await.result(future, timeout)
  // Convenience to avoid having to wrap andThen() parameters in Future.successful
  implicit def liftFuture[A](v: A): Future[A] = Future.successful(v)
  def status(of: Result): Int = of.header.status

  val localHost = "localhost"
  val localPort: Int = 19001
  val localUrl  = s"http://$localHost:$localPort"

  val additionalConfiguration: Seq[(String, Any)] = Seq.empty

  override implicit lazy val app: Application = new GuiceApplicationBuilder()
    .configure(Configuration("testserver.port" -> s"$localPort"))
    .configure(Configuration("play.http.router" -> "testOnlyDoNotUseInAppConf.Routes"))
    .configure(Configuration("microservice.services.iht.port" -> s"${WiremockHelper.wiremockPort}"))
    .configure(Configuration("auditing.consumer.baseUri.port" -> s"${WiremockHelper.wiremockPort}"))
    .configure(Configuration("metrics.enabled" -> true))
    .configure(Configuration(additionalConfiguration: _*))
    .build()

  override def beforeEach() = {
    resetWiremock()
  }

  override def beforeAll() = {
    super.beforeAll()
    startWiremock()
  }

  override def afterAll() = {
    stopWiremock()
    super.afterAll()
  }
}

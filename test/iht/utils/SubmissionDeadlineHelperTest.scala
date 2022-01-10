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

package iht.utils

import iht.config.AppConfig
import iht.connector.IhtConnector
import iht.models.application.IhtApplication
import iht.testhelpers.{CommonBuilder, MockObjectBuilder}
import iht.{FakeIhtApp, TestUtils}
import org.joda.time.LocalDate
import org.mockito.Mockito._
import org.scalatest.BeforeAndAfterEach
import org.scalatest.concurrent.ScalaFutures
import org.scalatestplus.mockito.MockitoSugar
import uk.gov.hmrc.http.HeaderCarrier

import scala.concurrent.Future

class SubmissionDeadlineHelperTest extends FakeIhtApp with MockitoSugar with TestUtils with BeforeAndAfterEach with ScalaFutures with MockObjectBuilder {

  implicit val mockAppConfig: AppConfig = app.injector.instanceOf[AppConfig]
  val mockIhtConnector: IhtConnector = mock[IhtConnector]

  override def beforeEach(): Unit = {
    reset(mockIhtConnector)
    super.beforeEach()
  }

  "Submission deadline helper" must {
    "return a date of thirteen months minus a day when given a sensible nino, ihtReference, and ihtConnector" in {
      implicit val headerCarrier = HeaderCarrier()

      val registrationDate = new LocalDate() //today

      val application = IhtApplication(
          ihtRefNo = CommonBuilder.DefaultIhtRefNo,
          firstName = CommonBuilder.DefaultFirstName,
          lastName = CommonBuilder.DefaultLastName,
          dateOfBirth = CommonBuilder.DefaultDateOfBirth,
          dateOfDeath = CommonBuilder.DefaultDOD,
          nino = CommonBuilder.DefaultNino,
          entryType = CommonBuilder.DefaultEntryType,
          role = CommonBuilder.DefaultRole,
          registrationDate = registrationDate,
          currentStatus = CommonBuilder.DefaultCurrentStatus,
          acknowledgmentReference = CommonBuilder.defaultAckRef
        )
      createMockToGetCaseList(mockIhtConnector, Seq(application.copy(CommonBuilder.DefaultIhtRefNo)))

      val result: Future[LocalDate] = SubmissionDeadlineHelper(fakeNino, CommonBuilder.DefaultIhtRefNo, mockIhtConnector, headerCarrier)

      whenReady(result){ r =>
        r must equal (registrationDate plusMonths 13 minusDays 1)
      }
    }

    "throw a runtime exception with an appropriate diagnostic message if the reference is not found" in {
      implicit val headerCarrier = HeaderCarrier()

      createMockToGetCaseList(mockIhtConnector, Nil)

      val result = SubmissionDeadlineHelper(fakeNino, CommonBuilder.DefaultIhtRefNo, mockIhtConnector, headerCarrier)
      result.failed.futureValue mustBe a[RuntimeException]
    }
  }

}

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

package iht.utils

import iht.connector.IhtConnector
import iht.models.application.IhtApplication
import iht.testhelpers.CommonBuilder
import iht.testhelpers.MockObjectBuilder._
import iht.{FakeIhtApp, TestUtils}
import org.joda.time.LocalDate
import org.scalatest.BeforeAndAfter
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.mock.MockitoSugar
import uk.gov.hmrc.play.test.UnitSpec

import scala.concurrent.Future
import uk.gov.hmrc.http.HeaderCarrier

class SubmissionDeadlineHelperTest extends UnitSpec with FakeIhtApp with MockitoSugar with TestUtils with BeforeAndAfter with ScalaFutures {

  var mockIhtConnector = mock[IhtConnector]

  before {
    mockIhtConnector = mock[IhtConnector]
  }

  "Submission deadline helper" must {
    "return a date of thirteen months minus a day when given a sensible nino, ihtReference, and ihtConnector" in {
      implicit val headerCarrier = new HeaderCarrier()

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
          acknowledgmentReference = CommonBuilder.DefaultAcknowledgmentReference
        )
      createMockToGetCaseList(mockIhtConnector, Seq(application.copy(CommonBuilder.DefaultIhtRefNo)))

      val result: Future[LocalDate] = SubmissionDeadlineHelper(fakeNino, CommonBuilder.DefaultIhtRefNo, mockIhtConnector, headerCarrier)

      whenReady(result){ r =>
        r should equal (registrationDate plusMonths(13) minusDays(1))
      }
    }

    "throw a runtime exception with an appropriate diagnostic message if the reference is not found" in {
      implicit val headerCarrier = new HeaderCarrier()

      createMockToGetCaseList(mockIhtConnector, Nil)

      val result = SubmissionDeadlineHelper(fakeNino, CommonBuilder.DefaultIhtRefNo, mockIhtConnector, headerCarrier)
      result.failed.futureValue shouldBe a[RuntimeException]
    }
  }

}

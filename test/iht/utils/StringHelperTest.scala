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

package iht.utils

import iht.FakeIhtApp
import iht.models.application.ApplicationDetails
import iht.models.application.exemptions.BasicExemptionElement
import iht.models.{DeceasedDateOfDeath, RegistrationDetails}
import iht.testhelpers._
import org.joda.time.LocalDate
import org.scalatest.mock.MockitoSugar
import play.api.i18n.Messages
import play.api.test.FakeRequest
import uk.gov.hmrc.play.frontend.auth.connectors.domain.{Accounts, ConfidenceLevel, CredentialStrength}
import uk.gov.hmrc.play.frontend.auth.{AuthContext, LoggedInUser, Principal}
import uk.gov.hmrc.play.http.HeaderCarrier
import uk.gov.hmrc.play.http.logging.SessionId
import uk.gov.hmrc.play.test.UnitSpec

import scala.collection.immutable.ListMap

class StringHelperTest extends UnitSpec with FakeIhtApp with MockitoSugar {
  "parseAssignmensToSeqTuples" must {
    "parse correctly a valid seq of 2 assignments with spaces before or after key values" in {
      val result = StringHelper.parseAssignmentsToSeqTuples(
        "aaa  =bbb,ccc=  ddd"
      )
      result shouldBe Seq(
        ("aaa","bbb"),
        ("ccc","ddd")
      )
    }

    "parse correctly a valid seq of 1 assignment with spaces before and after key values" in {
      val result = StringHelper.parseAssignmentsToSeqTuples(
        "aaa  =   bbb"
      )
      result shouldBe Seq(
        ("aaa","bbb")
      )
    }

    "parse correctly an empty string" in {
      val result = StringHelper.parseAssignmentsToSeqTuples(
        ""
      )
      result shouldBe Seq()
    }

    "throw an exception if invalid assignments are given (no equals symbols)" in {
      a[RuntimeException] shouldBe thrownBy {
        StringHelper.parseAssignmentsToSeqTuples("aaa,bbb")
      }
    }

    "throw an exception if invalid assignments are given (too many equals symbols)" in {
      a[RuntimeException] shouldBe thrownBy {
        StringHelper.parseAssignmentsToSeqTuples("aaa=bbb=ccc")
      }
    }
  }

}

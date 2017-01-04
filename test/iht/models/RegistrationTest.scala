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

package iht.models

import iht.FakeIhtApp
import iht.testhelpers.CommonBuilder
import iht.utils.CommonHelper
import org.scalatest.mock.MockitoSugar
import uk.gov.hmrc.play.test.UnitSpec

/**
 *
 * Created by Vineet Tyagi on 01/09/15.
 *
 */
class RegistrationTest extends UnitSpec with FakeIhtApp with MockitoSugar {

  val registrationDetailsWithValues = tempDetails.registrationDetailsWithValues()
  val emptyRegistrationDetails = tempDetails.emptyRegistrationDetails()

  "Registration models" must {

    "return returnId if it is present" in {
      val returnId = registrationDetailsWithValues.updatedReturnId
      assert(returnId=="1234567890","Value of returnId is 1234567890")
    }

    "throw exception if returns contains more than one record" in {

      an [RuntimeException] shouldBe thrownBy (CommonBuilder.buildRegistrationDetails.copy(returns = Seq(CommonBuilder.buildReturnDetails,
        CommonBuilder.buildReturnDetails)).updatedReturnId)
      }

    "throw exception if returns have no record" in {

      an [RuntimeException] shouldBe thrownBy (CommonBuilder.buildRegistrationDetails.updatedReturnId)
    }
   }
}

object tempDetails {

  val DefaultIHTReference=Some("ABC1234567890")
  val DefaultAcknowledgmentReference = CommonHelper.generateAcknowledgeReference

  def emptyRegistrationDetails() = {
    RegistrationDetails( deceasedDateOfDeath=None,
      applicantDetails=None,
      deceasedDetails=None,
      coExecutors = Seq(),
      ihtReference = DefaultIHTReference,
      acknowledgmentReference = DefaultAcknowledgmentReference,
      returns = Seq()
    )
  }

  def registrationDetailsWithValues()= {
    RegistrationDetails(
      deceasedDateOfDeath=Some(CommonBuilder.buildDeceasedDateOfDeath),
      applicantDetails=Some(CommonBuilder.buildApplicantDetails),
      deceasedDetails=Some(CommonBuilder.buildDeceasedDetails),
      coExecutors = Seq(),
      ihtReference = DefaultIHTReference,
      acknowledgmentReference = DefaultAcknowledgmentReference,
      returns = Seq(CommonBuilder.buildReturnDetails)
    )
  }
}

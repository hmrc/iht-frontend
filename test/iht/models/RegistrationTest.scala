/*
 * Copyright 2019 HM Revenue & Customs
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
import iht.config.AppConfig
import iht.testhelpers.CommonBuilder
import iht.utils.{StringHelper, StringHelperFixture}
import org.scalatest.mock.MockitoSugar

class RegistrationTest extends FakeIhtApp with MockitoSugar with StringHelper {

  implicit val appConfig: AppConfig = app.injector.instanceOf[AppConfig]

  val registrationDetailsWithValues = tempDetails.registrationDetailsWithValues()
  val emptyRegistrationDetails = tempDetails.emptyRegistrationDetails()

  "Registration models" must {

    "return returnId if it is present" in {
      val returnId = registrationDetailsWithValues.updatedReturnId
      assert(returnId=="1234567890","Value of returnId is 1234567890")
    }

    "throw exception if returns contains more than one record" in {

      an [RuntimeException] mustBe thrownBy (CommonBuilder.buildRegistrationDetails.copy(returns = Seq(CommonBuilder.buildReturnDetails,
        CommonBuilder.buildReturnDetails)).updatedReturnId)
      }

    "throw exception if returns have no record" in {

      an [RuntimeException] mustBe thrownBy (CommonBuilder.buildRegistrationDetails.updatedReturnId)
    }
   }

  "ApplicantDetails" must {
    "return a proper name when a first and second name are present" in {
      val ad = CommonBuilder.buildApplicantDetails
      ad.name mustBe ad.firstName.get + " " + ad.lastName.get
    }

    "return an empty string if first and second names are not present." in {
      val ad = CommonBuilder.buildApplicantDetails copy(firstName = None, lastName = None)
      ad.name mustBe " "
    }
  }

  "Deceased details" must {
    "respond correctly when isCompleted is called for completed object" in {
      val dd = CommonBuilder.buildDeceasedDetails
      dd.isCompleted mustBe true
    }
    "respond correctly when isCompleted is called for non-completed object" in {
      val dd = CommonBuilder.buildDeceasedDetails copy(firstName=None)
      dd.isCompleted mustBe false
    }
    "respond correctly when ninoFormatted is called" in {
      val dd = CommonBuilder.buildDeceasedDetails copy(nino = Some("a a 1 2 34 5 6c"))
      dd.ninoFormatted mustBe Some("AA123456C")
    }
    "respond correctly when ninoFormatted is called with nino of None" in {
      val dd = CommonBuilder.buildDeceasedDetails copy(nino = None)
      dd.ninoFormatted mustBe None
    }
  }

  "CoExecutor" must {
    "respond correctly when ninoFormatted is called" in {
      val coExec = CommonBuilder.buildCoExecutor copy(nino = "a a 1 2 34 5 6c")
      coExec.ninoFormatted mustBe "AA123456C"
    }
  }
}

object tempDetails {

  val DefaultIHTReference=Some("ABC1234567890")
  def defaultAckRef(implicit appConfig: AppConfig): String =
    StringHelperFixture().generateAcknowledgeReference

  def emptyRegistrationDetails()(implicit appConfig: AppConfig) = {
    RegistrationDetails( deceasedDateOfDeath=None,
      applicantDetails=None,
      deceasedDetails=None,
      coExecutors = Seq(),
      ihtReference = DefaultIHTReference,
      acknowledgmentReference = defaultAckRef,
      returns = Seq()
    )
  }

  def registrationDetailsWithValues()(implicit appConfig: AppConfig)= {
    RegistrationDetails(
      deceasedDateOfDeath=Some(CommonBuilder.buildDeceasedDateOfDeath),
      applicantDetails=Some(CommonBuilder.buildApplicantDetails),
      deceasedDetails=Some(CommonBuilder.buildDeceasedDetails),
      coExecutors = Seq(),
      ihtReference = DefaultIHTReference,
      acknowledgmentReference = defaultAckRef,
      returns = Seq(CommonBuilder.buildReturnDetails)
    )
  }
}

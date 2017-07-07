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
import iht.models.DeceasedDateOfDeath
import iht.testhelpers._
import org.joda.time.LocalDate
import org.scalatest.mock.MockitoSugar
import play.api.i18n.{I18nSupport, MessagesApi}
import uk.gov.hmrc.play.test.UnitSpec

class DeceasedInfoHelperTest extends UnitSpec with FakeIhtApp with MockitoSugar with I18nSupport {

  implicit val messagesApi: MessagesApi = app.injector.instanceOf[MessagesApi]

  "getDeceasedNameOrDefaultString" must {
    val firstName = "first"
    val lastName = "last"
    val deceasedDetails = CommonBuilder.buildDeceasedDetails.copy(firstName = Some(firstName), lastName = Some(lastName))
    val regDetails = CommonBuilder.buildRegistrationDetails.copy(deceasedDetails = Some(deceasedDetails))

    "return Deceased name where deceased details exists " in {
      DeceasedInfoHelper.getDeceasedNameOrDefaultString(regDetails) shouldBe firstName+" "+lastName
    }

    "return default string where deceased details does not exists " in {
      val regDetailsWithNODeceasedDetails = regDetails.copy(deceasedDetails = None)
      DeceasedInfoHelper.getDeceasedNameOrDefaultString(regDetailsWithNODeceasedDetails) shouldBe messagesApi("iht.the.deceased")
    }
  }

  "Predicate isThereADateOfDeath returns true when there is a date of death" in {
    DeceasedInfoHelper.isThereADateOfDeath(CommonBuilder.buildRegistrationDetails copy(
      deceasedDateOfDeath = Some(DeceasedDateOfDeath(new LocalDate(2000,10,10)))), ""
    ) shouldBe true
  }

  "Predicate isThereADateOfDeath returns false when there is no date of death" in {
    DeceasedInfoHelper.isThereADateOfDeath(CommonBuilder.buildRegistrationDetails copy(
      deceasedDateOfDeath = None), "") shouldBe false
  }

  "Predicate isThereADeceasedDomicile returns true when there's a deceased domicile" in {
    DeceasedInfoHelper.isThereADeceasedDomicile(CommonBuilder.buildRegistrationDetails copy(
      deceasedDetails = Some(CommonBuilder.buildDeceasedDetails)
      ), "") shouldBe true
  }

  "Predicate isThereADeceasedDomicile returns false when there's no deceased domicile" in {
    DeceasedInfoHelper.isThereADeceasedDomicile(CommonBuilder.buildRegistrationDetails copy(
      deceasedDetails = Some(CommonBuilder.buildDeceasedDetails.copy(domicile = None))
      ), "") shouldBe false
  }

  "Predicate isThereADeceasedFirstName returns true when there's a deceased first name" in {
    DeceasedInfoHelper.isThereADeceasedFirstName(CommonBuilder.buildRegistrationDetails copy(
      deceasedDetails = Some(CommonBuilder.buildDeceasedDetails)
      ), "") shouldBe true
  }

  "Predicate isThereADeceasedFirstName returns false when there's no deceased first name" in {
    DeceasedInfoHelper.isThereADeceasedFirstName(CommonBuilder.buildRegistrationDetails copy(
      deceasedDetails = Some(CommonBuilder.buildDeceasedDetails.copy(firstName = None))
      ), "") shouldBe false
  }

  "Predicate isDeceasedAddressQuestionAnswered returns true when deceased address " +
    "question answered " in {
    DeceasedInfoHelper.isDeceasedAddressQuestionAnswered(CommonBuilder.buildRegistrationDetails copy(
      deceasedDetails = Some(CommonBuilder.buildDeceasedDetails)
      ), "") shouldBe true
  }

  "Predicate isDeceasedAddressQuestionAnswered returns false when deceased address question " +
    "not answered " in {
    DeceasedInfoHelper.isDeceasedAddressQuestionAnswered(CommonBuilder.buildRegistrationDetails copy(
      deceasedDetails = Some(CommonBuilder.buildDeceasedDetails.copy(isAddressInUK = None))
      ), "") shouldBe false
  }

  "Predicate isThereADeceasedAddress returns true when there is a deceased address" in {
    DeceasedInfoHelper.isThereADeceasedAddress(CommonBuilder.buildRegistrationDetails copy(
      deceasedDetails = Some(CommonBuilder.buildDeceasedDetails)
      ), "") shouldBe true
  }

  "Predicate isThereADeceasedAddress returns false when there is no deceased address" in {
    DeceasedInfoHelper.isThereADeceasedAddress(CommonBuilder.buildRegistrationDetails copy(
      deceasedDetails = Some(CommonBuilder.buildDeceasedDetails.copy(ukAddress = None))
      ), "") shouldBe false
  }

}

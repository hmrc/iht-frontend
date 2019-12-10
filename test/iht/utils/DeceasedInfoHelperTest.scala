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

package iht.utils

import iht.FakeIhtApp
import iht.config.AppConfig
import iht.models.DeceasedDateOfDeath
import iht.testhelpers._
import org.joda.time.LocalDate
import org.scalatestplus.mockito.MockitoSugar
import play.api.i18n.{I18nSupport, Lang, MessagesApi}
import play.api.mvc.MessagesControllerComponents

class DeceasedInfoHelperTest extends FakeIhtApp with MockitoSugar with I18nSupport {

  implicit val controllerComponents: MessagesControllerComponents = app.injector.instanceOf[MessagesControllerComponents]
  implicit val mockAppConfig: AppConfig = app.injector.instanceOf[AppConfig]

  implicit val lang = Lang.defaultLang
  implicit val messagesApi = controllerComponents.messagesApi
  implicit val messages = messagesApi.preferred(Seq(lang)).messages

  "getDeceasedNameOrDefaultString" must {
    val firstName = "first"
    val lastName = "last"
    val deceasedDetails = CommonBuilder.buildDeceasedDetails.copy(firstName = Some(firstName), lastName = Some(lastName))
    val regDetails = CommonBuilder.buildRegistrationDetails.copy(deceasedDetails = Some(deceasedDetails))

    "return Deceased name where deceased details exists " in {
      DeceasedInfoHelper.getDeceasedNameOrDefaultString(regDetails) mustBe firstName+" "+lastName
    }

    "return default string where deceased details does not exists " in {
      val regDetailsWithNODeceasedDetails = regDetails.copy(deceasedDetails = None)
      DeceasedInfoHelper.getDeceasedNameOrDefaultString(regDetailsWithNODeceasedDetails) mustBe messagesApi("iht.the.deceased")
    }
  }

  "Predicate isThereADateOfDeath returns true when there is a date of death" in {
    DeceasedInfoHelper.isThereADateOfDeath(CommonBuilder.buildRegistrationDetails copy(
      deceasedDateOfDeath = Some(DeceasedDateOfDeath(new LocalDate(2000,10,10)))), ""
    ) mustBe true
  }

  "Predicate isThereADateOfDeath returns false when there is no date of death" in {
    DeceasedInfoHelper.isThereADateOfDeath(CommonBuilder.buildRegistrationDetails copy(
      deceasedDateOfDeath = None), "") mustBe false
  }

  "Predicate isThereADeceasedDomicile returns true when there's a deceased domicile" in {
    DeceasedInfoHelper.isThereADeceasedDomicile(CommonBuilder.buildRegistrationDetails copy(
      deceasedDetails = Some(CommonBuilder.buildDeceasedDetails)
      ), "") mustBe true
  }

  "Predicate isThereADeceasedDomicile returns false when there's no deceased domicile" in {
    DeceasedInfoHelper.isThereADeceasedDomicile(CommonBuilder.buildRegistrationDetails copy(
      deceasedDetails = Some(CommonBuilder.buildDeceasedDetails.copy(domicile = None))
      ), "") mustBe false
  }

  "Predicate isThereADeceasedFirstName returns true when there's a deceased first name" in {
    DeceasedInfoHelper.isThereADeceasedFirstName(CommonBuilder.buildRegistrationDetails copy(
      deceasedDetails = Some(CommonBuilder.buildDeceasedDetails)
      ), "") mustBe true
  }

  "Predicate isThereADeceasedFirstName returns false when there's no deceased first name" in {
    DeceasedInfoHelper.isThereADeceasedFirstName(CommonBuilder.buildRegistrationDetails copy(
      deceasedDetails = Some(CommonBuilder.buildDeceasedDetails.copy(firstName = None))
      ), "") mustBe false
  }

  "Predicate isDeceasedAddressQuestionAnswered returns true when deceased address " +
    "question answered " in {
    DeceasedInfoHelper.isDeceasedAddressQuestionAnswered(CommonBuilder.buildRegistrationDetails copy(
      deceasedDetails = Some(CommonBuilder.buildDeceasedDetails)
      ), "") mustBe true
  }

  "Predicate isDeceasedAddressQuestionAnswered returns false when deceased address question " +
    "not answered " in {
    DeceasedInfoHelper.isDeceasedAddressQuestionAnswered(CommonBuilder.buildRegistrationDetails copy(
      deceasedDetails = Some(CommonBuilder.buildDeceasedDetails.copy(isAddressInUK = None))
      ), "") mustBe false
  }

  "Predicate isThereADeceasedAddress returns true when there is a deceased address" in {
    DeceasedInfoHelper.isThereADeceasedAddress(CommonBuilder.buildRegistrationDetails copy(
      deceasedDetails = Some(CommonBuilder.buildDeceasedDetails)
      ), "") mustBe true
  }

  "Predicate isThereADeceasedAddress returns false when there is no deceased address" in {
    DeceasedInfoHelper.isThereADeceasedAddress(CommonBuilder.buildRegistrationDetails copy(
      deceasedDetails = Some(CommonBuilder.buildDeceasedDetails.copy(ukAddress = None))
      ), "") mustBe false
  }

}

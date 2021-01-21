/*
 * Copyright 2021 HM Revenue & Customs
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

package iht.forms.application.exemptions

import iht.FakeIhtApp
import iht.forms.ApplicationForms._
import iht.forms.FormTestHelper
import iht.models.application.exemptions.{BasicExemptionElement, PartnerExemption}
import iht.testhelpers.CommonBuilder

class ExemptionsFormsTest extends FormTestHelper with FakeIhtApp {
  val sampleName = CommonBuilder.firstNameGenerator
  val sampleCharityName = "LXWZWZZWL"
  val sampleCharityNumber = "abcde12"
  val sampleQualifyingBodyName = "LXWZWZZWL"
  val sampleQualifyingBodyNumber = "abcde12"

  def partnerExemption(isAssetForDeceasedPartner: String,
                       isPartnerHomeInUK: String,
                       firstName: String,
                       lastName: String,
                       day: String,
                       month: String,
                       year: String,
                       nino: String,
                       totalAssets: String) =
    Map(
      "isAssetForDeceasedPartner" -> isAssetForDeceasedPartner,
      "isPartnerHomeInUK" -> isPartnerHomeInUK,
      "firstName" -> firstName,
      "lastName" -> lastName,
      "dateOfBirth.day" -> day,
      "dateOfBirth.month" -> month,
      "dateOfBirth.year" -> year,
      "nino" -> nino,
      "totalAssets" -> totalAssets
    )

  lazy val completePartnerExemption = partnerExemption(
    "true", "true", sampleName, sampleName, "01", "01", "2000",
    CommonBuilder.DefaultNino, CommonBuilder.DefaultTotalAssets.toString)

  "assetsLeftToSpouseQuestionForm" must {
    behave like yesNoQuestion[PartnerExemption]("isAssetForDeceasedPartner",
      assetsLeftToSpouseQuestionForm, _.isAssetForDeceasedPartner, "error.isAssetForDeceasedPartner.select")
  }

  "partnerPermanentHomeQuestionForm" must {
    behave like yesNoQuestion[PartnerExemption]("isPartnerHomeInUK",
      partnerPermanentHomeQuestionForm, _.isPartnerHomeInUK, "error.isPartnerHomeInUK.select")
  }

  "partnerExemptionNameForm" must {
    "give an error when the first name is not supplied" in {
      val expectedError = singleError("firstName", "error.firstName.give")
      val data = formData("firstName", "")
      checkForContainsError(partnerExemptionNameForm, data, expectedError)
    }

    "give no error when the first name is exactly max length" in {
      val data = formData("firstName", "a" * 40)
      checkForNotContainsError(partnerExemptionNameForm, "firstName", data)
    }

    "give an error when the first name is too long" in {
      val expectedError = singleError("firstName", "error.firstName.giveUsingXCharsOrLess")
      val data = formData("firstName", "a" * 41)
      checkForContainsError(partnerExemptionNameForm, data, expectedError)
    }

    "give no error when a valid first and last name is supplied" in {
      val data = formData("firstName", sampleName, "lastName", sampleName)
      val boundModel = formWithNoError(partnerExemptionNameForm, data)
      boundModel.firstName mustBe Some(sampleName)
      boundModel.lastName mustBe Some(sampleName)
    }

    "give an error when the last name is not supplied" in {
      val expectedError = singleError("lastName", "error.lastName.give")
      val data = formData("lastName", "")
      checkForContainsError(partnerExemptionNameForm, data, expectedError)
    }

    "give no error when the last name is exactly max length" in {
      val data = formData("lastName", "a" * 40)
      checkForNotContainsError(partnerExemptionNameForm, "lastName", data)
    }

    "give an error when the last name is too long" in {
      val expectedError = singleError("lastName", "error.lastName.giveUsingXCharsOrLess")
      val data = formData("lastName", "a" * 41)
      checkForContainsError(partnerExemptionNameForm, data, expectedError)
    }
  }

  "spouseDateOfBirthForm" must {
    behave like dateOfBirth(completePartnerExemption, spouseDateOfBirthForm)
  }

  "partnerNinoForm" must {
    behave like nino(completePartnerExemption, partnerNinoForm)
  }

  "partnerValueForm" must {
    behave like mandatoryCurrencyValue("totalAssets", partnerValueForm)
  }

  "assetsLeftToCharityQuestionForm" must {
    behave like yesNoQuestion[BasicExemptionElement]("isAssetForCharity",
      assetsLeftToCharityQuestionForm, _.isSelected, "error.isAssetForCharity.select")
  }

  "charityNameForm" must {
    "give no error when a valid name is supplied" in {
      val data = formData("name", sampleCharityName)
      formWithNoError(charityNameForm, data).name mustBe Some(sampleCharityName)
    }

    "give an error when the name is not supplied" in {
      val expectedError = singleError("name", "error.charityName.enterName")
      val data = formData("name", "")
      checkForContainsError(charityNameForm, data, expectedError)
    }

    "give an error when the name is too long" in {
      val expectedError = singleError("name", "error.charityName.giveUsing35CharactersOrLess")
      val data = formData("name", "a" * 100)
      checkForContainsError(charityNameForm, data, expectedError)
    }
  }

  "charityNumberForm" must {
    "give no error when a valid number is supplied" in {
      val data = formData("charityNumber", sampleCharityNumber)
      formWithNoError(charityNumberForm, data).number mustBe Some(sampleCharityNumber)
    }

    "give an error when the number is not supplied" in {
      val expectedError = singleError("charityNumber", "error.charityNumber.give")
      val data = formData("charityNumber", "")
      checkForContainsError(charityNumberForm, data, expectedError)
    }

    "give an error when the number is too long" in {
      val expectedError = singleError("charityNumber", "error.charityNumber.enterUsingOnly6Or7Numbers")
      val data = formData("charityNumber", "a" * 11)
      checkForContainsError(charityNumberForm, data, expectedError)
    }
  }

  "assetsLeftToCharityValueForm" must {
    behave like mandatoryCurrencyValue("totalValue", assetsLeftToCharityValueForm)
  }

  "assetsLeftToQualifyingBodyQuestionForm" must {
    behave like yesNoQuestion[BasicExemptionElement]("isAssetForQualifyingBody",
      assetsLeftToQualifyingBodyQuestionForm, _.isSelected, "error.isAssetForQualifyingBody.select")
  }

  "qualifyingBodyNameForm" must {
    "give no error when a valid name is supplied" in {
      val data = formData("name", sampleQualifyingBodyName)
      formWithNoError(qualifyingBodyNameForm, data).name mustBe Some(sampleQualifyingBodyName)
    }

    "give an error when the name is not supplied" in {
      val expectedError = singleError("name", "error.qualifyingBodyName.enterName")
      val data = formData("name", "")
      checkForContainsError(qualifyingBodyNameForm, data, expectedError)
    }

    "give an error when the name is too long" in {
      val expectedError = singleError("name", "error.qualifyingBodyName.giveUsing35CharactersOrLess")
      val data = formData("name", "a" * 100)
      checkForContainsError(qualifyingBodyNameForm, data, expectedError)
    }
  }

  "qualifyingBodyValueForm" must {
    behave like mandatoryCurrencyValue("totalValue", qualifyingBodyValueForm)
  }

}

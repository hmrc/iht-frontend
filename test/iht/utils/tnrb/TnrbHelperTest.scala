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

package iht.utils.tnrb

import iht.FakeIhtApp
import iht.constants.IhtProperties
import iht.controllers.application.tnrb.routes
import iht.models.application.tnrb.WidowCheck
import iht.testhelpers._
import org.joda.time.LocalDate
import org.scalatest.mock.MockitoSugar
import play.api.i18n.Messages
import play.api.test.Helpers._
import uk.gov.hmrc.play.test.UnitSpec
import iht.testhelpers.TestHelper._

/**
 *
 * Created by Vineet Tyagi on 28/05/15.
 *
 * This Class contains the Unit Tests for iht.utils.tnrb.TnrbHelper
 */
class TnrbHelperTest extends UnitSpec with FakeIhtApp with MockitoSugar {

  lazy val spouseOrCivilPartnerFirstName = CommonBuilder.firstNameGenerator
  lazy val spouseOrCivilPartnerLastName = CommonBuilder.surnameGenerator

  "spouseOrCivilPartnerLabelWithOptions" must {
    "return spouse or CivilPartner name when name has been entered" in {
      val tnrbModel = CommonBuilder.buildTnrbEligibility copy(firstName = Some(spouseOrCivilPartnerFirstName), lastName = Some(spouseOrCivilPartnerLastName))
      val widowCheck = CommonBuilder.buildWidowedCheck copy (dateOfPreDeceased = Some(civilPartnershipExclusionDatePlusOne))
      val result = TnrbHelper.spouseOrCivilPartnerLabelWithOptions(Some(tnrbModel), Some(widowCheck))
      result should be(spouseOrCivilPartnerFirstName + " " + spouseOrCivilPartnerLastName)
    }

    "return prefix plus spouse or CivilPartner message when name has not been entered and date of death is after " +
      "Civil Partnership Inclusion date" in {
      val tnrbModel = CommonBuilder.buildTnrbEligibility copy(firstName = None, lastName = None)
      val widowCheck = CommonBuilder.buildWidowedCheck copy (dateOfPreDeceased = Some(civilPartnershipExclusionDatePlusOne))
      val result = TnrbHelper.spouseOrCivilPartnerLabelWithOptions(Some(tnrbModel), Some(widowCheck), Some("prefix"))
      result should be("prefix " + Messages(spouseOrCivilPartnerMessageKey))
    }

    "return prefix plus spouse message when name has not been entered and date of death is before " +
      "Civil Partnership Inclusion date" in {
      val tnrbModel = CommonBuilder.buildTnrbEligibility copy(firstName = None, lastName = None)
      val widowCheck = CommonBuilder.buildWidowedCheck copy (dateOfPreDeceased = Some(civilPartnershipExclusionDateMinusOne))
      val result = TnrbHelper.spouseOrCivilPartnerLabelWithOptions(Some(tnrbModel), Some(widowCheck), Some("prefix"))
      result should be("prefix " + Messages(spouseMessageKey))
    }
  }

  "spouseOrCivilPartnerLabel" must {
    "return spouse or CivilPartner name when name has been entered" in {
      val tnrbModel = CommonBuilder.buildTnrbEligibility copy(firstName = Some(spouseOrCivilPartnerFirstName), lastName = Some(spouseOrCivilPartnerLastName))
      val widowCheck = CommonBuilder.buildWidowedCheck copy (dateOfPreDeceased = Some(civilPartnershipExclusionDatePlusOne))
      val result = TnrbHelper.spouseOrCivilPartnerLabel(tnrbModel, widowCheck)
      result should be(spouseOrCivilPartnerFirstName + " " + spouseOrCivilPartnerLastName)
    }

    "return prefix plus spouse or CivilPartner message when name has not been entered and date of death is after " +
      "Civil Partnership Inclusion date" in {
      val tnrbModel = CommonBuilder.buildTnrbEligibility copy(firstName = None, lastName = None)
      val widowCheck = CommonBuilder.buildWidowedCheck copy (dateOfPreDeceased = Some(civilPartnershipExclusionDatePlusOne))
      val result = TnrbHelper.spouseOrCivilPartnerLabel(tnrbModel, widowCheck, "prefix")
      result should be("prefix " + Messages(spouseOrCivilPartnerMessageKey))
    }

    "return prefix plus spouse message when name has not been entered and date of death is before " +
      "Civil Partnership Inclusion date" in {
      val tnrbModel = CommonBuilder.buildTnrbEligibility copy(firstName = None, lastName = None)
      val widowCheck = CommonBuilder.buildWidowedCheck copy (dateOfPreDeceased = Some(civilPartnershipExclusionDateMinusOne))
      val result = TnrbHelper.spouseOrCivilPartnerLabel(tnrbModel, widowCheck, "prefix")
      result should be("prefix " + Messages(spouseMessageKey))
    }
  }

  "spouseOrCivilPartnerNameLabel" must {
    "return spouse or CivilPartner name when name has been entered" in {
      val tnrbModel = CommonBuilder.buildTnrbEligibility copy(firstName = Some(spouseOrCivilPartnerFirstName), lastName = Some(spouseOrCivilPartnerLastName))
      val widowCheck = CommonBuilder.buildWidowedCheck copy (dateOfPreDeceased = Some(civilPartnershipExclusionDatePlusOne))
      val result = TnrbHelper.spouseOrCivilPartnerNameLabel(tnrbModel, widowCheck)
      result should be(Messages("iht.name.upperCaseInitial"))
    }

    "return prefix plus spouse or CivilPartner message when name has not been entered and date of death is after " +
      "Civil Partnership Inclusion date" in {
      val tnrbModel = CommonBuilder.buildTnrbEligibility copy(firstName = None, lastName = None)
      val widowCheck = CommonBuilder.buildWidowedCheck copy (dateOfPreDeceased = Some(civilPartnershipExclusionDatePlusOne))
      val result = TnrbHelper.spouseOrCivilPartnerNameLabel(tnrbModel, widowCheck, "prefix")
      result should be("prefix " + Messages(spouseOrCivilPartnerMessageKey))
    }

    "return prefix plus spouse message when name has not been entered and date of death is before " +
      "Civil Partnership Inclusion date" in {
      val tnrbModel = CommonBuilder.buildTnrbEligibility copy(firstName = None, lastName = None)
      val widowCheck = CommonBuilder.buildWidowedCheck copy (dateOfPreDeceased = Some(civilPartnershipExclusionDateMinusOne))
      val result = TnrbHelper.spouseOrCivilPartnerNameLabel(tnrbModel, widowCheck, "prefix")
      result should be("prefix " + Messages(spouseMessageKey))
    }
  }

  "preDeceasedMaritalStatusLabel" must {
    "return spouse or CivilPartner name when name has been entered" in {
      val tnrbModel = CommonBuilder.buildTnrbEligibility copy(firstName = Some(spouseOrCivilPartnerFirstName), lastName = Some(spouseOrCivilPartnerLastName))
      val widowCheck = CommonBuilder.buildWidowedCheck copy (dateOfPreDeceased = Some(civilPartnershipExclusionDatePlusOne))
      val result = TnrbHelper.preDeceasedMaritalStatusLabel(tnrbModel, widowCheck)
      result should be(spouseOrCivilPartnerFirstName + " " + spouseOrCivilPartnerLastName + " " + Messages(marriedMessageKey))
    }

    "return prefix plus spouse or CivilPartner message when name has not been entered and date of death is after " +
      "Civil Partnership Inclusion date" in {
      val tnrbModel = CommonBuilder.buildTnrbEligibility copy(firstName = None, lastName = None)
      val widowCheck = CommonBuilder.buildWidowedCheck copy (dateOfPreDeceased = Some(civilPartnershipExclusionDatePlusOne))
      val result = TnrbHelper.preDeceasedMaritalStatusLabel(tnrbModel, widowCheck)
      result should be(Messages("page.iht.application.tnrbEligibilty.theDeceased.label") + " "  +
        Messages(marriedOrInCivilPartnershipMessageKey))
    }

    "return prefix plus spouse message when name has not been entered and date of death is before " +
      "Civil Partnership Inclusion date" in {
      val tnrbModel = CommonBuilder.buildTnrbEligibility copy(firstName = None, lastName = None)
      val widowCheck = CommonBuilder.buildWidowedCheck copy (dateOfPreDeceased = Some(civilPartnershipExclusionDateMinusOne))
      val result = TnrbHelper.preDeceasedMaritalStatusLabel(tnrbModel, widowCheck)
      result should be(Messages("page.iht.application.tnrbEligibilty.theDeceased.label") + " " +
        Messages(marriedMessageKey))
    }
  }

  "spouseOrCivilPartnerMessage" must {
    "return spouse message as the date is before Civil Partnership Inclusion date" in {
      val result = TnrbHelper.spouseOrCivilPartnerMessage(Some(civilPartnershipExclusionDateMinusOne))
      result should be(Messages(spouseMessageKey))
    }

    "return spouse or CivilPartner message as the date is equal to Civil Partnership Inclusion date" in {
      val result = TnrbHelper.spouseOrCivilPartnerMessage(Some(civilPartnershipExclusionDate))
      result should be(Messages(spouseOrCivilPartnerMessageKey))
    }

    "return spouse or CivilPartner message as the date is after Civil Partnership Inclusion date" in {
      val result = TnrbHelper.spouseOrCivilPartnerMessage(Some(civilPartnershipExclusionDatePlusOne))
      result should be(Messages(spouseOrCivilPartnerMessageKey))
    }
  }

  "preDeceasedMaritalStatusSubLabel" must {
    "return spouse message as the date is before Civil Partnership Inclusion date" in {
      val result = TnrbHelper.preDeceasedMaritalStatusSubLabel(Some(civilPartnershipExclusionDateMinusOne))
      result should be(Messages(marriedMessageKey))
    }

    "return spouse or CivilPartner message as the date is equal to Civil Partnership Inclusion date" in {
      val result = TnrbHelper.preDeceasedMaritalStatusSubLabel(Some(civilPartnershipExclusionDate))
      result should be(Messages(marriedOrInCivilPartnershipMessageKey))
    }

    "return spouse or CivilPartner message as the date is after Civil Partnership Inclusion date" in {
      val result = TnrbHelper.preDeceasedMaritalStatusSubLabel(Some(civilPartnershipExclusionDatePlusOne))
      result should be(Messages(marriedOrInCivilPartnershipMessageKey))
    }
  }

  "marriageOrCivilPartnerShipLabelForPdf" must {
    "return spouse message as the date is before Civil Partnership Inclusion date" in {
      val result = TnrbHelper.marriageOrCivilPartnerShipLabelForPdf(Some(civilPartnershipExclusionDateMinusOne))
      result should be(Messages("page.iht.application.tnrbEligibilty.partner.marriage.label"))
    }

    "return spouse or CivilPartner message as the date is equal to Civil Partnership Inclusion date" in {
      val result = TnrbHelper.marriageOrCivilPartnerShipLabelForPdf(Some(civilPartnershipExclusionDate))
      result should be(Messages("page.iht.application.tnrbEligibilty.partner.marriageOrCivilPartnership.label"))
    }

    "return spouse or CivilPartner message as the date is after Civil Partnership Inclusion date" in {
      val result = TnrbHelper.marriageOrCivilPartnerShipLabelForPdf(Some(civilPartnershipExclusionDatePlusOne))
      result should be(Messages("page.iht.application.tnrbEligibilty.partner.marriageOrCivilPartnership.label"))
    }
  }

  "successfulTnrbRedirect" must {
    "redirect to Tnrb success page if it matches the happy path" in {
      val applicationDetails = CommonBuilder.buildApplicationDetails.copy(increaseIhtThreshold =
        Some(CommonBuilder.buildTnrbEligibility.copy(firstName = Some(spouseOrCivilPartnerFirstName), lastName = Some(spouseOrCivilPartnerLastName),
          dateOfMarriage= Some(new LocalDate(1984, 12, 11)))),
        widowCheck = Some(CommonBuilder.buildWidowedCheck))
      val result = TnrbHelper.successfulTnrbRedirect(applicationDetails)
      redirectLocation(result) should be(Some(routes.TnrbSuccessController.onPageLoad().url))
    }

    "redirect to Tnrb overview page if it does not match the happy path" in {
      val applicationDetails = CommonBuilder.buildApplicationDetails.copy(increaseIhtThreshold =
        Some(CommonBuilder.buildTnrbEligibility.copy( isPartnerLivingInUk=Some(false),
          dateOfMarriage= Some(new LocalDate(1984, 12, 11)))),
        widowCheck = Some(CommonBuilder.buildWidowedCheck))
      val result = TnrbHelper.successfulTnrbRedirect(applicationDetails)
      redirectLocation(result) should be(Some(routes.TnrbOverviewController.onPageLoad().url))
    }
  }

  "cancelLinkUrlForWidowCheckPages" must {
    "return Call to EstateOverviewController if widow check date empty" in {
      val ihtRef = "ihtRef"
      val ad = CommonBuilder.buildApplicationDetails.copy(ihtRef=Some(ihtRef),
        widowCheck= Some(CommonBuilder.buildWidowedCheck copy(dateOfPreDeceased=None)))
      val expectedResult = iht.controllers.application.routes.EstateOverviewController.onPageLoadWithIhtRef(ihtRef)
      val result = TnrbHelper.cancelLinkUrlForWidowCheckPages(ad)
      result shouldBe expectedResult
    }

    "return Call to TnrbOverviewController if widow check date is not empty" in {
      val ihtRef = "ihtRef"
      val ad = CommonBuilder.buildApplicationDetails.copy(
        widowCheck= Some(CommonBuilder.buildWidowedCheck copy(dateOfPreDeceased=Some(LocalDate.now()))))
      val expectedResult = iht.controllers.application.tnrb.routes.TnrbOverviewController.onPageLoad
      val result = TnrbHelper.cancelLinkUrlForWidowCheckPages(ad)
      result shouldBe expectedResult
    }
  }

  "cancelLinkTextForWidowCheckPages" must {
    "return \"Return to estate overview\" if widow check date empty" in {
      val ihtRef = "ihtRef"
      val ad = CommonBuilder.buildApplicationDetails.copy(ihtRef=Some(ihtRef),
        widowCheck= Some(CommonBuilder.buildWidowedCheck copy(dateOfPreDeceased=None)))
      val expectedResult = Messages("iht.estateReport.returnToEstateOverview")
      val result = TnrbHelper.cancelLinkTextForWidowCheckPages(ad)
      result shouldBe expectedResult
    }

    "return \"Return to increasing the threshold\" if widow check date is not empty" in {
      val ihtRef = "ihtRef"
      val ad = CommonBuilder.buildApplicationDetails.copy(
        widowCheck= Some(CommonBuilder.buildWidowedCheck copy(dateOfPreDeceased=Some(LocalDate.now()))))
      val expectedResult = Messages("page.iht.application.tnrb.returnToIncreasingThreshold")
      val result = TnrbHelper.cancelLinkTextForWidowCheckPages(ad)
      result shouldBe expectedResult
    }
  }

  "getEntryPointForTnrb" must {
    "return WidowedCheck question page url when marital status is other than single and widowed and" +
      "widowcheck question has not been answered" in {
      val regDetailsDeceasedMarried = CommonBuilder.buildRegistrationDetails.copy(
        deceasedDetails = Some(CommonBuilder.buildDeceasedDetails.copy(maritalStatus = Some(TestHelper.MaritalStatusMarried))))

      val appDetails = CommonBuilder.buildApplicationDetails

      TnrbHelper.getEntryPointForTnrb(regDetailsDeceasedMarried, appDetails) shouldBe
        iht.controllers.application.tnrb.routes.DeceasedWidowCheckQuestionController.onPageLoad()

    }
  }

  "getEntryPointForTnrb" must {
    "return Tnrb Overview url when marital status is other than single and widowed and" +
      "widowcheck date question has been answered" in {
      val regDetailsDeceasedMarried = CommonBuilder.buildRegistrationDetails.copy(
        deceasedDetails = Some(CommonBuilder.buildDeceasedDetails.copy(
          maritalStatus = Some(TestHelper.MaritalStatusMarried))))

      val appDetails = CommonBuilder.buildApplicationDetails.copy(widowCheck = Some(CommonBuilder.buildWidowedCheck))

      TnrbHelper.getEntryPointForTnrb(regDetailsDeceasedMarried, appDetails) shouldBe
        iht.controllers.application.tnrb.routes.TnrbOverviewController.onPageLoad()

    }
  }

  "getEntryPointForTnrb" must {
    "return WidowedCheck date question page url when marital status widowed and" +
      "widowcheck date question has not been answered" in {
      val regDetailsDeceasedWidowed = CommonBuilder.buildRegistrationDetails.copy(
        deceasedDetails = Some(CommonBuilder.buildDeceasedDetails.copy(
          maritalStatus = Some(TestHelper.MaritalStatusWidowed))))

      val appDetails = CommonBuilder.buildApplicationDetails

      TnrbHelper.getEntryPointForTnrb(regDetailsDeceasedWidowed, appDetails) shouldBe
        iht.controllers.application.tnrb.routes.DeceasedWidowCheckDateController.onPageLoad()

    }
  }

  "getEntryPointForTnrb" must {
    "throw RunTime exception if Marital status is not known" in {
      val regDetailsDeceasedWidowed = CommonBuilder.buildRegistrationDetails.copy(
        deceasedDetails = Some(CommonBuilder.buildDeceasedDetails.copy(maritalStatus = Some("InCorrect"))))

      val appDetails = CommonBuilder.buildApplicationDetails

      intercept[RuntimeException] {
        TnrbHelper.getEntryPointForTnrb(regDetailsDeceasedWidowed, appDetails)
      }
    }
  }

  "urlForIncreasingThreshold" must {
    "return deceasedWidowCheckDatePage if Marital status is widowed" in {

     TnrbHelper.urlForIncreasingThreshold(IhtProperties.statusWidowed) should be
      iht.controllers.application.tnrb.routes.DeceasedWidowCheckDateController.onPageLoad()
    }
  }

  "urlForIncreasingThreshold" must {
    "return deceasedWidowCheckDatePage if Marital status is either Divorced or Married" in {

      TnrbHelper.urlForIncreasingThreshold(IhtProperties.statusWidowed) should be
      iht.controllers.application.tnrb.routes.DeceasedWidowCheckQuestionController.onPageLoad()
    }
  }

  "urlForIncreasingThreshold" must {
    "throw RunTime exception if Marital status is not known" in {
     val maritalStatus = "NOt_Known"
      intercept[RuntimeException] {
        TnrbHelper.urlForIncreasingThreshold(maritalStatus)
      }

    }
  }

}

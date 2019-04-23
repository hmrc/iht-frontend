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

package iht.utils.tnrb

import akka.japi.Option.Some
import iht.FakeIhtApp
import iht.config.AppConfig
import iht.controllers.application.tnrb.routes
import iht.testhelpers.TestHelper._
import iht.testhelpers.{ContentChecker, _}
import org.joda.time.LocalDate
import org.scalatest.mock.MockitoSugar
import play.api.i18n.{Lang, Messages, MessagesApi}
import play.api.mvc.MessagesControllerComponents
import play.api.test.Helpers._

class TnrbHelperTest extends FakeIhtApp with MockitoSugar with TnrbHelper {

  val mockControllerComponents: MessagesControllerComponents = app.injector.instanceOf[MessagesControllerComponents]
  implicit val messagesApi: MessagesApi = mockControllerComponents.messagesApi
  implicit val lang = Lang.defaultLang
  implicit val messages: Messages = messagesApi.preferred(Seq(lang)).messages
  implicit val mockAppConfig: AppConfig = app.injector.instanceOf[AppConfig]
  val appConfig = mockAppConfig

  lazy val spouseOrCivilPartnerFirstName = CommonBuilder.firstNameGenerator
  lazy val spouseOrCivilPartnerLastName = CommonBuilder.surnameGenerator

  val deceasedName = CommonBuilder.firstNameGenerator

  "previousSpouseOrCivilPartner" must {
    "return previous spouse or civil partner name when it exists" in {
      val tnrbModel = CommonBuilder.buildTnrbEligibility
      val widowCheck = CommonBuilder.buildWidowedCheck copy (dateOfPreDeceased = Some(civilPartnershipExclusionDatePlusOne))
      val result = previousSpouseOrCivilPartner(Some(tnrbModel), Some(widowCheck), deceasedName)
      result must be(tnrbModel.Name.toString)
    }

    "return \"dd's previous spouse or civil partner\" when no name exists and date after cp date" in {
      val tnrbModel = CommonBuilder.buildTnrbEligibility copy(firstName = None, lastName = None)
      val widowCheck = CommonBuilder.buildWidowedCheck copy (dateOfPreDeceased = Some(civilPartnershipExclusionDatePlusOne))
      val result = previousSpouseOrCivilPartner(Some(tnrbModel), Some(widowCheck), deceasedName)
      result must be(s"$deceasedName’s previous spouse or civil partner")
    }

    "return \"dd's previous spouse\" when no name exists and date before cp date" in {
      val tnrbModel = CommonBuilder.buildTnrbEligibility copy(firstName = None, lastName = None)
      val widowCheck = CommonBuilder.buildWidowedCheck copy (dateOfPreDeceased = Some(civilPartnershipExclusionDateMinusOne))
      val result = previousSpouseOrCivilPartner(Some(tnrbModel), Some(widowCheck), deceasedName)
      result must be(s"$deceasedName’s previous spouse")
    }
  }

  "mutateContent" must {
    "not mutate when english" in {
      val content = "abc gan priod ghi"
      mutateContent(content, "en") mustBe content
    }

    "mutate when welsh" in {
      val content = "abc gan priod ghi"
      mutateContent(content, "cy") mustBe "abc gan briod ghi"
    }
  }

  "vowelConsciousAnd" must {

    "always return 'and' when English language is selected" in {
      vowelConsciousAnd("John Smith", "en") mustBe "page.iht.application.tnrbEligibilty.partner.additional.label.and"
    }

    "return 'ac' when Welsh language is selected and the predeceased name starts with a vowel" in {
      vowelConsciousAnd("Anne Smith", "cy") mustBe "page.iht.application.tnrbEligibilty.partner.additional.label.andAfterVowel"
      vowelConsciousAnd("Yvonne Smith", "cy") mustBe "page.iht.application.tnrbEligibilty.partner.additional.label.andAfterVowel"
    }

    "return 'a' when Welsh language is selected and the predeceased name starts with a consonant" in {
      vowelConsciousAnd("John Smith", "cy") mustBe "page.iht.application.tnrbEligibilty.partner.additional.label.andAfterConsonant"
      vowelConsciousAnd("Sarah Smith", "cy") mustBe "page.iht.application.tnrbEligibilty.partner.additional.label.andAfterConsonant"
    }

  }

  "spouseOrCivilPartnerLabelGenitive" must {
    "return spouse or CivilPartner name when name has been entered" in {
      val tnrbModel = CommonBuilder.buildTnrbEligibility copy(firstName = Some(spouseOrCivilPartnerFirstName), lastName = Some(spouseOrCivilPartnerLastName))
      val widowCheck = CommonBuilder.buildWidowedCheck copy (dateOfPreDeceased = Some(civilPartnershipExclusionDatePlusOne))
      val result = spouseOrCivilPartnerLabelGenitive(tnrbModel, widowCheck)
      result must be(spouseOrCivilPartnerFirstName + " " + spouseOrCivilPartnerLastName)
    }

    "return prefix plus spouse or CivilPartner message when name has not been entered and date of death is after " +
      "Civil Partnership Inclusion date" in {
      val tnrbModel = CommonBuilder.buildTnrbEligibility copy(firstName = None, lastName = None)
      val widowCheck = CommonBuilder.buildWidowedCheck copy (dateOfPreDeceased = Some(civilPartnershipExclusionDatePlusOne))
      val result = spouseOrCivilPartnerLabelGenitive(tnrbModel, widowCheck, "prefix")
      result must be("prefix’s " + messagesApi(spouseOrCivilPartnerMessageKey))
    }

    "return prefix plus spouse message when name has not been entered and date of death is before " +
      "Civil Partnership Inclusion date" in {
      val tnrbModel = CommonBuilder.buildTnrbEligibility copy(firstName = None, lastName = None)
      val widowCheck = CommonBuilder.buildWidowedCheck copy (dateOfPreDeceased = Some(civilPartnershipExclusionDateMinusOne))
      val result = spouseOrCivilPartnerLabelGenitive(tnrbModel, widowCheck, "prefix")
      result must be("prefix’s " + messagesApi(spouseMessageKey))
    }
  }

  "spouseOrCivilPartnerNameLabel" must {
    "return spouse or CivilPartner name when name has been entered" in {
      val tnrbModel = CommonBuilder.buildTnrbEligibility copy(firstName = Some(spouseOrCivilPartnerFirstName), lastName = Some(spouseOrCivilPartnerLastName))
      val widowCheck = CommonBuilder.buildWidowedCheck copy (dateOfPreDeceased = Some(civilPartnershipExclusionDatePlusOne))
      val result = spouseOrCivilPartnerNameLabel(tnrbModel, widowCheck)
      result must be(messagesApi("iht.name.upperCaseInitial"))
    }

    "return prefix plus spouse or CivilPartner message when name has not been entered and date of death is after " +
      "Civil Partnership Inclusion date" in {
      val tnrbModel = CommonBuilder.buildTnrbEligibility copy(firstName = None, lastName = None)
      val widowCheck = CommonBuilder.buildWidowedCheck copy (dateOfPreDeceased = Some(civilPartnershipExclusionDatePlusOne))
      val result = spouseOrCivilPartnerNameLabel(tnrbModel, widowCheck, "prefix")
      result must be("prefix " + messagesApi(spouseOrCivilPartnerMessageKey))
    }

    "return prefix plus spouse message when name has not been entered and date of death is before " +
      "Civil Partnership Inclusion date" in {
      val tnrbModel = CommonBuilder.buildTnrbEligibility copy(firstName = None, lastName = None)
      val widowCheck = CommonBuilder.buildWidowedCheck copy (dateOfPreDeceased = Some(civilPartnershipExclusionDateMinusOne))
      val result = spouseOrCivilPartnerNameLabel(tnrbModel, widowCheck, "prefix")
      result must be("prefix " + messagesApi(spouseMessageKey))
    }
  }

  "preDeceasedMaritalStatusLabel" must {
    "return spouse or CivilPartner name when name has been entered" in {
      val tnrbModel = CommonBuilder.buildTnrbEligibility copy(firstName = Some(spouseOrCivilPartnerFirstName), lastName = Some(spouseOrCivilPartnerLastName))
      val widowCheck = CommonBuilder.buildWidowedCheck copy (dateOfPreDeceased = Some(civilPartnershipExclusionDatePlusOne))
      val result = preDeceasedMaritalStatusLabel(tnrbModel, widowCheck)
      result must be(spouseOrCivilPartnerFirstName + " " + spouseOrCivilPartnerLastName + " " + messagesApi(marriedMessageKey))
    }

    "return prefix plus spouse or CivilPartner message when name has not been entered and date of death is after " +
      "Civil Partnership Inclusion date" in {
      val tnrbModel = CommonBuilder.buildTnrbEligibility copy(firstName = None, lastName = None)
      val widowCheck = CommonBuilder.buildWidowedCheck copy (dateOfPreDeceased = Some(civilPartnershipExclusionDatePlusOne))
      val result = preDeceasedMaritalStatusLabel(tnrbModel, widowCheck)
      result must be(messagesApi("iht.the.deceased") + " " + messagesApi(marriedOrInCivilPartnershipMessageKey))
    }

    "return prefix plus spouse message when name has not been entered and date of death is before " +
      "Civil Partnership Inclusion date" in {
      val tnrbModel = CommonBuilder.buildTnrbEligibility copy(firstName = None, lastName = None)
      val widowCheck = CommonBuilder.buildWidowedCheck copy (dateOfPreDeceased = Some(civilPartnershipExclusionDateMinusOne))
      val result = preDeceasedMaritalStatusLabel(tnrbModel, widowCheck)
      result must be(messagesApi("iht.the.deceased") + " " +
        messagesApi(marriedMessageKey))
    }
  }

  "spouseOrCivilPartnerMessage" must {
    "return spouse message as the date is before Civil Partnership Inclusion date" in {
      val result = spouseOrCivilPartnerMessage(Some(civilPartnershipExclusionDateMinusOne))
      result must be("spouse")
    }

    "return spouse or CivilPartner message as the date is equal to Civil Partnership Inclusion date" in {
      val result = spouseOrCivilPartnerMessage(Some(civilPartnershipExclusionDate))
      result must be("spouse or civil partner")
    }

    "return spouse or CivilPartner message as the date is after Civil Partnership Inclusion date" in {
      val result = spouseOrCivilPartnerMessage(Some(civilPartnershipExclusionDatePlusOne))
      result must be("spouse or civil partner")
    }
  }

  "preDeceasedMaritalStatusSubLabel" must {
    "return spouse message as the date is before Civil Partnership Inclusion date" in {
      val result = preDeceasedMaritalStatusSubLabel(Some(civilPartnershipExclusionDateMinusOne))
      result must be("married")
    }

    "return spouse or CivilPartner message as the date is equal to Civil Partnership Inclusion date" in {
      val result = preDeceasedMaritalStatusSubLabel(Some(civilPartnershipExclusionDate))
      result must be("married or in a civil partnership")
    }

    "return spouse or CivilPartner message as the date is after Civil Partnership Inclusion date" in {
      val result = preDeceasedMaritalStatusSubLabel(Some(civilPartnershipExclusionDatePlusOne))
      result must be("married or in a civil partnership")
    }
  }

  "marriageOrCivilPartnerShipLabelForPdf" must {
    "return spouse message as the date is before Civil Partnership Inclusion date" in {
      val result = marriageOrCivilPartnerShipLabelForPdf(Some(civilPartnershipExclusionDateMinusOne))
      result must be(messagesApi("page.iht.application.tnrbEligibilty.partner.marriage.label"))
    }

    "return spouse or CivilPartner message as the date is equal to Civil Partnership Inclusion date" in {
      val result = marriageOrCivilPartnerShipLabelForPdf(Some(civilPartnershipExclusionDate))
      result must be(messagesApi("page.iht.application.tnrbEligibilty.partner.marriageOrCivilPartnership.label"))
    }

    "return spouse or CivilPartner message as the date is after Civil Partnership Inclusion date" in {
      val result = marriageOrCivilPartnerShipLabelForPdf(Some(civilPartnershipExclusionDatePlusOne))
      result must be(messagesApi("page.iht.application.tnrbEligibilty.partner.marriageOrCivilPartnership.label"))
    }
  }

  "successfulTnrbRedirect" must {
    "redirect to Tnrb success page if it matches the happy path" in {
      val applicationDetails = CommonBuilder.buildApplicationDetails.copy(increaseIhtThreshold =
        Some(CommonBuilder.buildTnrbEligibility.copy(firstName = Some(spouseOrCivilPartnerFirstName), lastName = Some(spouseOrCivilPartnerLastName),
          dateOfMarriage= Some(new LocalDate(1984, 12, 11)))),
        widowCheck = Some(CommonBuilder.buildWidowedCheck))
      val result = successfulTnrbRedirect(applicationDetails)
      val actualResult: Option[String] = redirectLocation(result)
      val expectedResult: Option[String] = Some(routes.TnrbSuccessController.onPageLoad().url)
      actualResult must be(expectedResult)
    }

    "redirect to Tnrb overview page if it does not match the happy path" in {
      val applicationDetails = CommonBuilder.buildApplicationDetails.copy(increaseIhtThreshold =
        Some(CommonBuilder.buildTnrbEligibility.copy( isPartnerLivingInUk=Some(false),
          dateOfMarriage= Some(new LocalDate(1984, 12, 11)))),
        widowCheck = Some(CommonBuilder.buildWidowedCheck))
      val result = successfulTnrbRedirect(applicationDetails)

      val actualResult: Option[String] = redirectLocation(result)
      val expectedResult: Option[String] = Some(routes.TnrbOverviewController.onPageLoad().url)
      actualResult must be(expectedResult)
    }
  }

  "cancelLinkUrlForWidowCheckPages" must {
    "return Call to EstateOverviewController if widow check date empty" in {
      val ihtRef = "ihtRef"
      val ad = CommonBuilder.buildApplicationDetails.copy(ihtRef=Some(ihtRef),
        widowCheck= Some(CommonBuilder.buildWidowedCheck copy(dateOfPreDeceased=None)))
      val expectedResult = iht.controllers.application.routes.EstateOverviewController.onPageLoadWithIhtRef(ihtRef)
      val result = cancelLinkUrlForWidowCheckPages(ad)
      result mustBe expectedResult
    }

    "return Call to TnrbOverviewController if widow check date is not empty" in {
      val ihtRef = "ihtRef"
      val ad = CommonBuilder.buildApplicationDetails.copy(
        widowCheck= Some(CommonBuilder.buildWidowedCheck copy(dateOfPreDeceased=Some(LocalDate.now()))))
      val expectedResult = iht.controllers.application.tnrb.routes.TnrbOverviewController.onPageLoad
      val result = cancelLinkUrlForWidowCheckPages(ad)
      result mustBe expectedResult
    }
  }

  "cancelLinkTextForWidowCheckPages" must {
    "return \"Return to estate overview\" if widow check date empty" in {
      val ihtRef = "ihtRef"
      val ad = CommonBuilder.buildApplicationDetails.copy(ihtRef=Some(ihtRef),
        widowCheck= Some(CommonBuilder.buildWidowedCheck copy(dateOfPreDeceased=None)))
      val expectedResult = messagesApi("iht.estateReport.returnToEstateOverview")
      val result = cancelLinkTextForWidowCheckPages(ad)
      result mustBe expectedResult
    }

    "return \"Return to increasing the threshold\" if widow check date is not empty" in {
      val ihtRef = "ihtRef"
      val ad = CommonBuilder.buildApplicationDetails.copy(
        widowCheck= Some(CommonBuilder.buildWidowedCheck copy(dateOfPreDeceased=Some(LocalDate.now()))))
      val expectedResult = messagesApi("page.iht.application.tnrb.returnToIncreasingThreshold")
      val result = cancelLinkTextForWidowCheckPages(ad)
      result mustBe expectedResult
    }
  }

  "getEntryPointForTnrb" must {
    "return WidowedCheck question page url when marital status is other than single and widowed and" +
      "widowcheck question has not been answered" in {
      val regDetailsDeceasedMarried = CommonBuilder.buildRegistrationDetails.copy(
        deceasedDetails = Some(CommonBuilder.buildDeceasedDetails.copy(maritalStatus = Some(TestHelper.MaritalStatusMarried))))

      val appDetails = CommonBuilder.buildApplicationDetails

      getEntryPointForTnrb(regDetailsDeceasedMarried, appDetails) mustBe
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

      getEntryPointForTnrb(regDetailsDeceasedMarried, appDetails) mustBe
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

      getEntryPointForTnrb(regDetailsDeceasedWidowed, appDetails) mustBe
        iht.controllers.application.tnrb.routes.DeceasedWidowCheckDateController.onPageLoad()

    }
  }

  "getEntryPointForTnrb" must {
    "throw RunTime exception if Marital status is not known" in {
      val regDetailsDeceasedWidowed = CommonBuilder.buildRegistrationDetails.copy(
        deceasedDetails = Some(CommonBuilder.buildDeceasedDetails.copy(maritalStatus = Some("InCorrect"))))

      val appDetails = CommonBuilder.buildApplicationDetails

      intercept[RuntimeException] {
        getEntryPointForTnrb(regDetailsDeceasedWidowed, appDetails)
      }
    }
  }

  "urlForIncreasingThreshold" must {
    "return deceasedWidowCheckDatePage if Marital status is widowed" in {

     urlForIncreasingThreshold(mockAppConfig.statusWidowed) must be
      iht.controllers.application.tnrb.routes.DeceasedWidowCheckDateController.onPageLoad()
    }
  }

  "urlForIncreasingThreshold" must {
    "return deceasedWidowCheckDatePage if Marital status is either Divorced or Married" in {

      urlForIncreasingThreshold(mockAppConfig.statusWidowed) must be
      iht.controllers.application.tnrb.routes.DeceasedWidowCheckQuestionController.onPageLoad()
    }
  }

  "urlForIncreasingThreshold" must {
    "throw RunTime exception if Marital status is not known" in {
     val maritalStatus = "NOt_Known"
      intercept[RuntimeException] {
        urlForIncreasingThreshold(maritalStatus)
      }

    }
  }

  "spouseOrCivilPartnerName" must {
    "return the spouse name" in {
      val tnrbModel = CommonBuilder.buildTnrbEligibility copy(firstName = Some(spouseOrCivilPartnerFirstName),
                                                              lastName = Some(spouseOrCivilPartnerLastName))

      ContentChecker.stripLineBreaks(spouseOrCivilPartnerName(tnrbModel, "pretext")) mustBe
                      spouseOrCivilPartnerFirstName+" "+spouseOrCivilPartnerLastName
    }
    "return the pretext string when there is no spouse name" in {
      val tnrbModel = CommonBuilder.buildTnrbEligibility.copy(firstName = None, lastName = None)

      spouseOrCivilPartnerName(tnrbModel, "pretext") mustBe "pretext"
    }
  }

}

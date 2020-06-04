/*
 * Copyright 2020 HM Revenue & Customs
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

package iht.models.application

import iht.FakeIhtApp
import iht.config.AppConfig
import iht.models.application.assets._
import iht.models.application.exemptions.BasicExemptionElement
import iht.models.application.tnrb.WidowCheck
import iht.testhelpers.{AssetsWithAllSectionsSetToNoBuilder, CommonBuilder, TestHelper}
import org.joda.time.LocalDate
import org.scalatestplus.mockito.MockitoSugar
import play.api.i18n.{Lang, Messages, MessagesApi}
import play.api.mvc.MessagesControllerComponents

/**
  * Created by yusuf on 13/07/15.
  */
class ApplicationDetailsTest extends FakeIhtApp with MockitoSugar {

  val applicationDetailsWithValues = CommonBuilder.buildApplicationDetailsWithAssetsGiftsAndDebts
  val emptyApplicationDetails = CommonBuilder.buildApplicationDetails
  val ukAddress = Some(CommonBuilder.DefaultUkAddress)

  val mockControllerComponents: MessagesControllerComponents = app.injector.instanceOf[MessagesControllerComponents]
  implicit val mockAppConfig: AppConfig = app.injector.instanceOf[AppConfig]
  implicit val lang: Lang = Lang.defaultLang
  val messagesApi: MessagesApi = mockControllerComponents.messagesApi
  implicit val messages: Messages = messagesApi.preferred(Seq(lang)).messages

  def propertyValue(value: BigDecimal) = Some(value)

  def buildProperty(id: Option[String],
                            value: Option[BigDecimal]) = {
    CommonBuilder.buildProperty.copy(
      id = id,
      address = ukAddress,
      propertyType = TestHelper.PropertyTypeDeceasedHome,
      typeOfOwnership = TestHelper.TypesOfOwnershipDeceasedOnly,
      tenure = TestHelper.TenureFreehold,
      value = value
    )
  }

  private def buildCharity(id: Option[String],
                           name: Option[String] = Some("A Charity"),
                           number: Option[String] = Some("234"),
                           totalValue: Option[BigDecimal]) = {
    CommonBuilder.buildCharity.copy(id = id, name = name, number = number, totalValue = totalValue)
  }

  private def buildQualifyingBody(id: Option[String],
                                  name:Option[String] = Some("Qualifying Body"),
                                  totalValue: Option[BigDecimal]) = {
    CommonBuilder.buildQualifyingBody.copy(id = id, name = name, totalValue = totalValue)
  }

  private def buildPreviousYearGift(giftValue: Option[BigDecimal] = None,
                                    exemptionValue: Option[BigDecimal] = None) = {
    CommonBuilder.buildPreviousYearsGifts.copy(value = giftValue, exemptions = exemptionValue)
  }


  "areAllAssetsCompleted" must {

    "return Some(true) when all the assets section are completed" in {
      applicationDetailsWithValues.areAllAssetsCompleted mustBe Some(true)
    }

    "return Some(false) when all the assets section are not completed" in {
      val assets = CommonBuilder.buildApplicationDetailsWithAllAssets.allAssets.getOrElse(CommonBuilder.buildAllAssets)
      val inCompleteHeldAndTrustSection = CommonBuilder.buildAssetsHeldInTrust.copy(isOwned = Some(true))

      val appDetailsWithAssetsCompleted = CommonBuilder.buildApplicationDetailsWithAllAssets.copy(
        allAssets = Some(assets.copy(heldInTrust = Some(inCompleteHeldAndTrustSection))))

      appDetailsWithAssetsCompleted.areAllAssetsCompleted mustBe Some(false)
    }
  }

  "isCompleteProperties" must {
    "return Some(true) when the properties section is complete" in {
      applicationDetailsWithValues.isCompleteProperties mustBe Some(true)
    }

    "return Some(false) when the properties section is not complete" in {

      val appDetails = applicationDetailsWithValues.copy(propertyList = Nil)

      appDetails.isCompleteProperties mustBe Some(false)
    }

    "return None when the properties section has not been started at all" in {
      emptyApplicationDetails.isCompleteProperties mustBe empty
    }
  }

  "totalAssetsValue" must {
    "return the correct value of the assets" in {
      applicationDetailsWithValues.totalAssetsValue mustBe BigDecimal(26190)
    }

    "return 0 as the assets value when there is no asset entered" in {
      emptyApplicationDetails.totalAssetsValue mustBe BigDecimal(0)
    }

  }

  "totalPastYearsGiftsOption" must {

    "return correct value with option when gift values and exemptions entered" in {
      val giftsForSevenYearsList = List(
        CommonBuilder.buildPreviousYearsGifts.copy(yearId = Some("1"), value = Some(BigDecimal(2000))))

      val gifts = CommonBuilder.buildAllGifts.copy(isGivenAway = Some(true), isToTrust = Some(false),
        isReservation = Some(false), isGivenInLast7Years = Some(true))
      val appDetails = emptyApplicationDetails.copy(allGifts = Some(gifts), giftsList = Some(giftsForSevenYearsList))

      appDetails.totalPastYearsGiftsOption mustBe Some(2000.00)
    }

    "return correct value with option when gift values entered but not exemptions" in {
      val appDetails = CommonBuilder.buildApplicationDetails.copy(giftsList =
        Some(
          Seq(buildPreviousYearGift(Some(100), None), buildPreviousYearGift(Some(50), None))
        ))

      appDetails.totalPastYearsGiftsOption must be(Some(BigDecimal(150)))
    }

    "return None where gift values are not provided" in {
      val gifts = CommonBuilder.buildAllGifts.copy(isGivenAway = Some(true), isToTrust = Some(false),
        isReservation = Some(false), isGivenInLast7Years = Some(true))
      val appDetails = emptyApplicationDetails.copy(allGifts = Some(gifts))

      appDetails.totalPastYearsGiftsOption mustBe empty
    }
  }

  "totalPastYearsGiftsValueExcludingExemptionsOption" must {
    "return correct value with option when gifts values and exemptions entered" in {
      val appDetails = emptyApplicationDetails.copy(giftsList =
        Some(
          Seq(buildPreviousYearGift(Some(100), Some(30)), buildPreviousYearGift(Some(50), Some(30)))
        ))

      appDetails.totalPastYearsGiftsValueExcludingExemptionsOption mustBe Some(BigDecimal(150))
    }

    "return correct value with option when gifts values entered but not exemptions" in {
      val appDetails = emptyApplicationDetails.copy(giftsList =
        Some(
          Seq(buildPreviousYearGift(Some(100), None), buildPreviousYearGift(Some(50), None))
        ))

      appDetails.totalPastYearsGiftsValueExcludingExemptionsOption mustBe Some(BigDecimal(150))
    }

    "return None when exemptions entered but no gift values" in {
      val appDetails = emptyApplicationDetails.copy(giftsList =
        Some(
          Seq(buildPreviousYearGift(None, Some(50)), buildPreviousYearGift(None, Some(30)))
        ))

      appDetails.totalPastYearsGiftsValueExcludingExemptionsOption mustBe empty
    }
  }

  "totalPastYearsGiftsExemptionsOption" must {
    "return correct value with option when gift and exemptions entered" in {
      val appDetails = emptyApplicationDetails.copy(giftsList =
        Some(
          Seq(buildPreviousYearGift(Some(100), Some(30)), buildPreviousYearGift(Some(50), Some(30)))
        ))

      appDetails.totalPastYearsGiftsExemptionsOption mustBe Some(BigDecimal(60))
    }

    "return correct value with option when exemptions but no gift values entered" in {
      val appDetails = emptyApplicationDetails.copy(giftsList =

        Some(
          Seq(buildPreviousYearGift(None, Some(30)), buildPreviousYearGift(None, Some(30)))
        ))

      appDetails.totalPastYearsGiftsExemptionsOption mustBe Some(BigDecimal(60))
    }

    "return None when gift values entered but no exemptions" in {
      val appDetails = emptyApplicationDetails.copy(giftsList =
        Some(
          Seq(buildPreviousYearGift(Some(100), None), buildPreviousYearGift(Some(50), None))
        ))

      appDetails.totalPastYearsGiftsExemptionsOption mustBe empty
    }

    "return None when neither gifts nor exemptions entered" in {
      val appDetails = emptyApplicationDetails.copy(giftsList =
        Some(
          Seq(buildPreviousYearGift(), buildPreviousYearGift())
        ))

      appDetails.totalPastYearsGiftsExemptionsOption mustBe empty
    }
  }

  "isValueEnteredForPastYearsGifts" must {

    "return true if value entered" in {
      val appDetails = emptyApplicationDetails.copy(giftsList = Some(Seq(CommonBuilder.buildPreviousYearsGifts)))

      appDetails.isValueEnteredForPastYearsGifts mustBe true
    }

    "return false is no value entered" in {
      val appDetails = emptyApplicationDetails.copy(giftsList =
        Some(Seq(CommonBuilder.buildPreviousYearsGifts.copy(value = None, exemptions = None))))

      appDetails.isValueEnteredForPastYearsGifts mustBe false
    }
  }

  "areAllGiftSectionsCompleted" must {

    "return Some(true) when the gifts section is completed and user has declared gifts" in {
      val gifts = CommonBuilder.buildAllGifts.copy(isGivenAway = Some(true), isToTrust = Some(false),
        isReservation = Some(false), isGivenInLast7Years = Some(true))
      val giftsValues = Seq(CommonBuilder.buildPreviousYearsGifts)
      val appDetails = emptyApplicationDetails.copy(allGifts = Some(gifts), giftsList = Some(giftsValues))
      appDetails.areAllGiftSectionsCompleted mustBe Some(true)
    }

    "return Some(true) when the gifts section is completed and user has no gifts to declare" in {
      val gifts = AssetsWithAllSectionsSetToNoBuilder.buildAllGifts
      val appDetails = emptyApplicationDetails.copy(allGifts = Some(gifts))
      appDetails.areAllGiftSectionsCompleted mustBe Some(true)
    }

    "return Some(false) when the gifts section is not completed because user has not answered all questions" in {
      val gifts = CommonBuilder.buildAllGifts.copy(isGivenAway = Some(true))
      val appDetails = emptyApplicationDetails.copy(allGifts = Some(gifts))
      appDetails.areAllGiftSectionsCompleted mustBe Some(false)
    }

    "return Some(false) when the gifts section is not completed because user has not entered any gift values" in {
      val gifts = CommonBuilder.buildAllGifts.copy(isGivenAway = Some(true), isToTrust = Some(false),
        isReservation = Some(false), isGivenInLast7Years = Some(true))
      val appDetails = emptyApplicationDetails.copy(allGifts = Some(gifts))
      appDetails.areAllGiftSectionsCompleted mustBe Some(false)
    }

    "return Some(false) when the gifts given away has answered yes and no gift values have been provided " in {
      val gifts = CommonBuilder.buildAllGifts.copy(isGivenAway = Some(true))
      val appDetails = emptyApplicationDetails.copy(allGifts = Some(gifts), giftsList = None)
      appDetails.areAllGiftSectionsCompleted mustBe Some(false)
    }

    "return Some(false) when the gifts section is not completed because user has provided gifts value but has not" +
      "given answered for other questions" in {
      val gifts = CommonBuilder.buildAllGifts.copy(isGivenAway = Some(true))
      val giftsValues = Seq(CommonBuilder.buildPreviousYearsGifts)

      val appDetails = emptyApplicationDetails.copy(allGifts = Some(gifts), giftsList = Some(giftsValues))
      appDetails.areAllGiftSectionsCompleted mustBe Some(false)
    }

  }

  "areAllDebtsCompleted" must {

    "return Some(true) when all debts section are complete" in {
      val appDetails = AssetsWithAllSectionsSetToNoBuilder.buildApplicationDetails
      appDetails.areAllDebtsCompleted mustBe Some(true)
    }

    "return Some(false) when all but one of debts section are complete" in {
      val appDetails = AssetsWithAllSectionsSetToNoBuilder.buildApplicationDetails copy(
        allLiabilities = Some(AssetsWithAllSectionsSetToNoBuilder.buildAllLiabilities),
        allAssets = Some(CommonBuilder.buildAllAssets.copy(properties = Some(Properties(isOwned = Some(true))))
        ))
      appDetails.areAllDebtsCompleted mustBe Some(false)
    }
  }

  "totalLiabilitiesValue" must {

    "return correct value if liabilities value is other than 0" in {
      applicationDetailsWithValues.totalLiabilitiesValue mustBe BigDecimal(17400)
    }

    "return the liabilities value as 0 if there is no liabilities entered" in {
      emptyApplicationDetails.totalLiabilitiesValue mustBe BigDecimal(0)
    }
  }

  "totalLiabilitiesValueOption" must {

    "total liabilities value calculated correctly" in {
      val appDetails = CommonBuilder.buildApplicationDetails copy (
        allLiabilities = Some(CommonBuilder.buildAllLiabilitiesWithAllSectionsFilled))
      appDetails.totalLiabilitiesValueOption mustBe Some(BigDecimal(17400))
    }

    "total liabilities value None where no liabilities" in {
      val appDetails = CommonBuilder.buildApplicationDetails copy (allLiabilities = None)
      appDetails.totalLiabilitiesValueOption mustBe None
    }
  }

  "isCompleteMortgages" must {

    "return Some(true) when there is a property and no mortgage on the property" in {

      val propertyList = List(buildProperty(Some("1"), Some(BigDecimal(0))))

      val appDetails = emptyApplicationDetails.copy(
        propertyList = propertyList,
        allAssets = Some(
          CommonBuilder.buildAllAssets.copy(properties =
            Some(CommonBuilder.buildProperties.copy(isOwned = Some(true))))),
        allLiabilities = Some(CommonBuilder.buildAllLiabilities.copy(
          mortgages = Some(CommonBuilder.buildMortgageEstateElement.copy(isOwned = Some(false)))))
      )

      appDetails.isCompleteMortgages mustBe Some(true)
    }

    "return Some(false) when there is a property and mortgage section is not complete" in {

      val propertyList = List(buildProperty(Some("1"), Some(BigDecimal(0))))

      val appDetails = emptyApplicationDetails.copy(
        propertyList = propertyList,
        allAssets = Some(
          CommonBuilder.buildAllAssets.copy(properties =
            Some(CommonBuilder.buildProperties.copy(isOwned = Some(true))))),
        allLiabilities = Some(CommonBuilder.buildAllLiabilities.copy(
          mortgages = None))
      )

      appDetails.isCompleteMortgages mustBe Some(false)
    }

    "return Some(false) when property and mortgages list size does not match" in {

      val propertyList = List(buildProperty(Some("1"), Some(BigDecimal(100))),
        buildProperty(Some("2"), Some(BigDecimal(1000))))

      val mortgage1 = CommonBuilder.buildMortgage.copy(
        id = "1", value = Some(BigDecimal(5000)), isOwned = Some(true))

      val mortgageList = List(mortgage1)

      val appDetails = emptyApplicationDetails.copy(
        propertyList = propertyList,
        allAssets = Some(
          CommonBuilder.buildAllAssets.copy(properties =
            Some(CommonBuilder.buildProperties.copy(isOwned = Some(true))))),
        allLiabilities = Some(CommonBuilder.buildAllLiabilities.copy(
          mortgages = Some(CommonBuilder.buildMortgageEstateElement.copy(isOwned = Some(true), mortgageList))))
      )

      appDetails.isCompleteMortgages mustBe Some(false)
    }

    "return None when the properties section has not been started at all" in {
      emptyApplicationDetails.isCompleteProperties mustBe empty
    }
  }

  "areAllAssetsGiftsAndDebtsCompleted" must {

    "return true when assets, gifts, debts sections are complete and no exemptions" in {
      applicationDetailsWithValues.areAllAssetsGiftsAndDebtsCompleted mustBe true
    }

    "return true when assets, gifts and debts sections are complete" in {
      val appDetails = applicationDetailsWithValues.copy(
        allExemptions = Some(CommonBuilder.buildAllExemptions.copy(
          partner = Some(CommonBuilder.buildPartnerExemption),
          charity = Some(CommonBuilder.buildBasicExemptionElement.copy(Some(true))),
          qualifyingBody = Some(CommonBuilder.buildBasicExemptionElement.copy(Some(false))))),
        charities = Seq(CommonBuilder.buildCharity.copy(
          Some("1"), Some("testCharity"), Some("123456"), Some(BigDecimal(80000))))
      )

      appDetails.areAllAssetsGiftsAndDebtsCompleted mustBe true
    }

    "return false when any of or all of assets, gifts and debts sections are not complete" in {
      emptyApplicationDetails.areAllAssetsGiftsAndDebtsCompleted mustBe false
    }
  }

  "isCompleteCharities" must {

    "return Some(true) when user has charities and has completed those charities" in {

      val appDetails = emptyApplicationDetails.copy(
        allExemptions = Some(CommonBuilder.buildAllExemptions.copy(
          charity = Some(CommonBuilder.buildBasicExemptionElement.copy(Some(true)))
        )),
        charities = Seq(CommonBuilder.buildCharity.copy(Some("1"), Some("abc"), Some("121212"),
          Some(BigDecimal(1000))))
      )

      appDetails.isCompleteCharities mustBe Some(true)
    }

    "return Some(true) when charities question has answered as no" in {

      val appDetails = emptyApplicationDetails.copy(
        allExemptions = Some(CommonBuilder.buildAllExemptions.copy(
          charity = Some(BasicExemptionElement(Some(false)))
        )),
        charities = Nil
      )

      appDetails.isCompleteCharities mustBe Some(true)
    }

    "return Some(false) when charities question is answered yes but no charity has been added" in {
      val appDetails = emptyApplicationDetails.copy(
        allExemptions = Some(CommonBuilder.buildAllExemptions.copy(
          charity = Some(CommonBuilder.buildBasicExemptionElement.copy(Some(true)))
        )),
        charities = Nil
      )

      appDetails.isCompleteCharities mustBe Some(false)
    }

    "return Some(false) when charities question is answered yes but no charity value has been added" in {
      val appDetails = emptyApplicationDetails.copy(
        allExemptions = Some(CommonBuilder.buildAllExemptions.copy(
          charity = Some(CommonBuilder.buildBasicExemptionElement.copy(Some(true)))
        )),
        charities = Seq(CommonBuilder.buildCharity.copy(Some("1"), Some("abc"), Some("121212")))
      )

      appDetails.isCompleteCharities mustBe Some(false)
    }

    "return None when the charities section has not been started at all" in {
      emptyApplicationDetails.isCompleteCharities mustBe empty
    }
  }

  "isCompleteQualifyingBodies" must {

    "return Some(true) when user has qualifying bodies and has completed those qualifying bodies" in {

      val appDetails = emptyApplicationDetails.copy(
        allExemptions = Some(CommonBuilder.buildAllExemptions.copy(
          qualifyingBody = Some(CommonBuilder.buildBasicExemptionElement.copy(Some(true)))
        )),
        qualifyingBodies = Seq(CommonBuilder.buildQualifyingBody.copy(Some("1"), Some("abc"), Some(BigDecimal(1000))))
      )

      appDetails.isCompleteQualifyingBodies mustBe Some(true)
    }

    "return Some(true) when qualifying bodies question has answered as no" in {

      val appDetails = emptyApplicationDetails.copy(
        allExemptions = Some(CommonBuilder.buildAllExemptions.copy(
          qualifyingBody = Some(CommonBuilder.buildBasicExemptionElement.copy(Some(false)))
        )),
        qualifyingBodies = Nil
      )

      appDetails.isCompleteQualifyingBodies mustBe Some(true)
    }

    "return Some(false) when qualifying bodies question is answered yes but no qualifying body  has been added" in {
      val appDetails = emptyApplicationDetails.copy(
        allExemptions = Some(CommonBuilder.buildAllExemptions.copy(
          qualifyingBody = Some(CommonBuilder.buildBasicExemptionElement.copy(Some(true)))
        )),
        qualifyingBodies = Nil
      )

      appDetails.isCompleteQualifyingBodies mustBe Some(false)
    }

    "return Some(false) when qualifying bodies question is answered yes but " +
      "no qualifying bodies value has been added" in {
      val appDetails = emptyApplicationDetails.copy(
        allExemptions = Some(CommonBuilder.buildAllExemptions.copy(
          qualifyingBody = Some(CommonBuilder.buildBasicExemptionElement.copy(Some(true)))
        )),
        qualifyingBodies = Seq(CommonBuilder.buildQualifyingBody.copy(Some("1"), Some("abc")))
      )

      appDetails.isCompleteQualifyingBodies mustBe Some(false)
    }

    "return None when the qualifying bodies section has not been started at all" in {
      emptyApplicationDetails.isCompleteQualifyingBodies mustBe empty
    }
  }

  "isExemptionsCompletedWithNoValue" must {

    "return true when all exemptions sections have been completed to indicate their are no exemptions" in {
      val partner = Some(CommonBuilder.buildPartnerExemption.copy(isAssetForDeceasedPartner = Some(false)))
      val charities = Some(CommonBuilder.buildBasicExemptionElement.copy(isSelected = Some(false)))
      val qualifyingBodies = Some(CommonBuilder.buildBasicExemptionElement.copy(isSelected = Some(false)))
      val exemptions = CommonBuilder.buildAllExemptions.copy(partner, charities, qualifyingBodies)

      val appDetails = emptyApplicationDetails.copy(allExemptions = Some(exemptions))
      appDetails.isExemptionsCompletedWithNoValue mustBe true
    }

    "return false when all exemptions sections have not been completed" in {
      val partner = Some(CommonBuilder.buildPartnerExemption)
      val charities = Some(CommonBuilder.buildBasicExemptionElement.copy(isSelected = None))
      val qualifyingBodies = Some(CommonBuilder.buildBasicExemptionElement.copy(isSelected = None))
      val exemptions = CommonBuilder.buildAllExemptions.copy(partner, charities, qualifyingBodies)

      val appDetails = emptyApplicationDetails.copy(allExemptions = Some(exemptions))
      appDetails.isExemptionsCompletedWithNoValue mustBe false
    }
  }

  "isExemptionsCompletedWithoutPartnerExemptionWithNoValue" must {

    "return true when all exemptions sections, other than partner, " +
      "have been completed to indicate their are no exemptions " in {
      val charities = Some(CommonBuilder.buildBasicExemptionElement.copy(isSelected = Some(false)))
      val qualifyingBodies = Some(CommonBuilder.buildBasicExemptionElement.copy(isSelected = Some(false)))
      val exemptions = CommonBuilder.buildAllExemptions.copy(None, charities, qualifyingBodies)

      val appDetails = CommonBuilder.buildApplicationDetails.copy(allExemptions = Some(exemptions))
      appDetails.isExemptionsCompletedWithoutPartnerExemptionWithNoValue mustBe true
    }

    "return false when all exemptions section have not been completed" in {
      val charities = Some(CommonBuilder.buildBasicExemptionElement.copy(isSelected = None))
      val qualifyingBodies = Some(CommonBuilder.buildBasicExemptionElement.copy(isSelected = None))
      val exemptions = CommonBuilder.buildAllExemptions.copy(None, charities, qualifyingBodies)

      val appDetails = CommonBuilder.buildApplicationDetails.copy(allExemptions = Some(exemptions))
      appDetails.isExemptionsCompletedWithoutPartnerExemptionWithNoValue mustBe false
    }
  }

  "noExemptionsHaveBeenAnswered" must {

    "return true when no exemptions section has been answered" in {
      emptyApplicationDetails.noExemptionsHaveBeenAnswered mustBe true
    }

    "return false when no exemptions section has been answered" in {
      val appDetails = emptyApplicationDetails.copy(
      allExemptions = Some(CommonBuilder.buildAllExemptions.copy(
        qualifyingBody = Some(CommonBuilder.buildBasicExemptionElement.copy(Some(true)))
      )))

      appDetails.noExemptionsHaveBeenAnswered mustBe false
    }
  }

  "isValueEnteredForExemptions" must {

    "return true if at least one of the exemptions section has been answered " in {
      val charities = Some(CommonBuilder.buildBasicExemptionElement.copy(isSelected = None))
      val qualifyingBodies = Some(CommonBuilder.buildBasicExemptionElement.copy(isSelected = None))
      val exemptions = CommonBuilder.buildAllExemptions.copy(None, charities, qualifyingBodies)

      val appDetails = emptyApplicationDetails.copy(allExemptions = Some(exemptions))
      appDetails.isValueEnteredForExemptions mustBe true
    }

    "return false when no exemptions section has been answered " in {
      emptyApplicationDetails.isValueEnteredForExemptions mustBe false
    }
  }

  "totalExemptionsValue" must {

    "return correct value of all exemptions" in {
      val charity = buildCharity(id = Some("1"), totalValue = Some(BigDecimal(44.45)))
      val qualifyingBody = buildQualifyingBody(id = Some("1"), totalValue = Some(BigDecimal(12345)))

      val appDetailsWithExemptions = applicationDetailsWithValues.copy(
        charities = Seq(charity),
        qualifyingBodies = Seq(qualifyingBody)
      )

      appDetailsWithExemptions.totalExemptionsValue mustBe BigDecimal(12389.45)
    }

    "return 0 as all exemptions value" in {
      emptyApplicationDetails.totalExemptionsValue mustBe BigDecimal(0)
    }

  }

  "totalExemptionsValueOption" must {

    "return correct optional value when exemptions are entered " in {
      val charity = buildCharity(id = Some("1"), totalValue = Some(BigDecimal(44.45)))
      val qualifyingBody = buildQualifyingBody(id = Some("1"), totalValue = Some(BigDecimal(12345)))

      val appDetailsWithExemptions = applicationDetailsWithValues.copy(
        charities = Seq(charity),
        qualifyingBodies = Seq(qualifyingBody)
      )

      appDetailsWithExemptions.totalExemptionsValueOption mustBe Some(BigDecimal(12389.45))
    }

    "return None where no exemption is entered" in {
     emptyApplicationDetails.totalExemptionsValueOption mustBe empty
    }
  }

  "netValueAfterExemptionAndDebtsForPositiveExemption" must {

    "return correct value when there is no exemptions" in {
      applicationDetailsWithValues.netValueAfterExemptionAndDebtsForPositiveExemption mustBe BigDecimal(29193)
    }

    "return correct value when there is positive exemptions value" in {

      val charity = buildCharity(id = Some("1"), totalValue = Some(BigDecimal(200)))
      val qualifyingBody = buildQualifyingBody(id = Some("1"), totalValue = Some(BigDecimal(100)))

      val appDetailsWithExemptions = applicationDetailsWithValues.copy(
        charities = Seq(charity),
        qualifyingBodies = Seq(qualifyingBody)
      )

      appDetailsWithExemptions.netValueAfterExemptionAndDebtsForPositiveExemption mustBe BigDecimal(11493)
    }
  }

  "isWidowCheckSectionCompleted" must {

    "return false if no widow check section" in {
      val appDetails = emptyApplicationDetails.copy(widowCheck = None)
      appDetails.isWidowCheckSectionCompleted mustBe false
    }

    "return true if widow check section, no boolean value but a valid date" in {
      val appDetails = emptyApplicationDetails.copy(widowCheck =
        Some(CommonBuilder.buildWidowedCheck.copy(None, Some(new LocalDate(2011, 12, 12)))))
      appDetails.isWidowCheckSectionCompleted mustBe true
    }

    "return false if widow check section with boolean value of true but no date value" in {
      val appDetails = emptyApplicationDetails.copy(widowCheck = Some(WidowCheck(Some(true), None)))
      appDetails.isWidowCheckSectionCompleted mustBe false
    }

    "return true if widow check section with boolean value of false and date value" in {
      val appDetails = emptyApplicationDetails.copy(widowCheck =
        Some(CommonBuilder.buildWidowedCheck.copy(Some(false), Some(new LocalDate(2011, 12, 12)))))
      appDetails.isWidowCheckSectionCompleted mustBe true
    }

    "return true if widow check section with boolean value of true and date value" in {
      val appDetails = emptyApplicationDetails.copy(widowCheck =
        Some(CommonBuilder.buildWidowedCheck.copy(Some(true), Some(new LocalDate(2011, 12, 12)))))
      appDetails.isWidowCheckSectionCompleted mustBe true
    }

  }

  "isWidowCheckQuestionAnswered" must {

    "return true when question has been answered" in {
      val widowCheck = CommonBuilder.buildWidowedCheck.copy(Some(true))
      val appDetails =  emptyApplicationDetails.copy(widowCheck = Some(widowCheck))

      appDetails.isWidowCheckQuestionAnswered mustBe true
    }

    "return false when question has not been answered" in {
      val widowCheck = CommonBuilder.buildWidowedCheck.copy(None,Some(new LocalDate(2011, 12, 12)))
      val appDetails = emptyApplicationDetails.copy(widowCheck = Some(widowCheck))
      appDetails.isWidowCheckQuestionAnswered mustBe false
    }

    "return false when tnrb flow has not been started at all" in {
      emptyApplicationDetails.isWidowCheckQuestionAnswered mustBe false
    }
  }

  "isSuccessfulTnrbCase" must {

    "return true when WidowCheck and TnrbEligibility questions are answered" in {
      val widowCheck = CommonBuilder.buildWidowedCheck.copy(Some(true),Some(new LocalDate(2011, 12, 12)))
      val tnrbEligibilty = CommonBuilder.buildTnrbEligibility

      val appDetails = emptyApplicationDetails.copy(widowCheck = Some(widowCheck),
        increaseIhtThreshold = Some(tnrbEligibilty))

      appDetails.isSuccessfulTnrbCase mustBe true
    }

    "return false when WidowCheck section has not been started" in {

      val appDetails = emptyApplicationDetails

      appDetails.isSuccessfulTnrbCase mustBe false
    }

    "return false when none of TnrbEligibility questions is answered" in {
      val widowCheck = CommonBuilder.buildWidowedCheck.copy(Some(true),Some(new LocalDate(2011, 12, 12)))

      val appDetails = emptyApplicationDetails.copy(widowCheck = Some(widowCheck))

      appDetails.isSuccessfulTnrbCase mustBe false
    }

    "return false when WidowCheck and TnrbEligibility section are not complete" in {
      val widowCheck = CommonBuilder.buildWidowedCheck.copy(None,Some(new LocalDate(2011, 12, 12)))
      val tnrbEligibilty = CommonBuilder.buildTnrbEligibility

      val appDetails = emptyApplicationDetails.copy(widowCheck = Some(widowCheck),
        increaseIhtThreshold = Some(tnrbEligibilty))

      appDetails.isSuccessfulTnrbCase mustBe false
    }

    "return false when WidowCheck section is not complete" in {
      val widowCheck = CommonBuilder.buildWidowedCheck.copy(Some(true),None)
      val tnrbEligibilty = CommonBuilder.buildTnrbEligibility

      val appDetails = emptyApplicationDetails.copy(widowCheck = Some(widowCheck),
        increaseIhtThreshold = Some(tnrbEligibilty))

      appDetails.isSuccessfulTnrbCase mustBe false
    }

    "return false when TnrbEligibility section is not complete" in {
      val widowCheck = CommonBuilder.buildWidowedCheck.copy(Some(true),Some(new LocalDate(2011, 12, 12)))
      val tnrbEligibilty = CommonBuilder.buildTnrbEligibility.copy(dateOfMarriage = None)

      val appDetails = emptyApplicationDetails.copy(widowCheck = Some(widowCheck),
        increaseIhtThreshold = Some(tnrbEligibilty))

      appDetails.isSuccessfulTnrbCase mustBe false
    }

    "return false when TnrbEligibility section has any kickout condition" in {
      val widowCheck = CommonBuilder.buildWidowedCheck.copy(Some(true),Some(new LocalDate(2011, 12, 12)))
      val tnrbEligibilty = CommonBuilder.buildTnrbEligibility.copy(isPartnerLivingInUk = Some(false))

      val appDetails = emptyApplicationDetails.copy(widowCheck = Some(widowCheck),
        increaseIhtThreshold = Some(tnrbEligibilty))

      appDetails.isSuccessfulTnrbCase mustBe false
    }
  }

  "totalValue" must {
    "return correct value of assets entered" in {
      applicationDetailsWithValues.totalValue mustBe BigDecimal(29193)
    }

    "return assets value as 0" in {
      emptyApplicationDetails.totalValue mustBe BigDecimal(0)
    }
  }

  "totalNetValue" must {
    "return corret net value when there is no exemptions" in {
      applicationDetailsWithValues.totalNetValue mustBe BigDecimal(11793)
    }

    "return corret net value when there are some exemptions entered" in {

      val charity = buildCharity(id = Some("1"), totalValue = Some(BigDecimal(1000)))
      val qualifyingBody = buildQualifyingBody(id = Some("1"), totalValue = Some(BigDecimal(100)))

      val appDetailsWithExemptions = applicationDetailsWithValues.copy(
        charities = Seq(charity),
        qualifyingBodies = Seq(qualifyingBody)
      )

      appDetailsWithExemptions.totalNetValue mustBe BigDecimal(10693)
    }
  }

  "currentThreshold" must {

    "return threshold as 650K if Tnrb is completed and successful" in {
      val appDetails = emptyApplicationDetails.copy(increaseIhtThreshold = Some(CommonBuilder.buildTnrbEligibility),
        widowCheck = Some(CommonBuilder.buildWidowedCheck.copy(Some(true), Some(new LocalDate(2012, 1, 1)))))

      appDetails.currentThreshold mustBe mockAppConfig.transferredNilRateBand
    }

    "return threshold as 325K if Tnrb has not been started" in {
      emptyApplicationDetails.currentThreshold mustBe mockAppConfig.exemptionsThresholdValue
    }

    "return threshold as 325K if Tnrb is only partially completed" in {
      val appDetails = emptyApplicationDetails.copy(increaseIhtThreshold = Some(CommonBuilder.buildTnrbEligibility copy
        (isGiftMadeBeforeDeath = Some(true))))

      appDetails.currentThreshold mustBe mockAppConfig.exemptionsThresholdValue
    }
  }

}

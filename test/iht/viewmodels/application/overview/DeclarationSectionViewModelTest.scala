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

package iht.viewmodels.application.overview

import iht.models.application.exemptions.BasicExemptionElement
import iht.testhelpers.{CommonBuilder, TestHelper}
import iht.{FakeIhtApp, TestUtils}
import org.joda.time.LocalDate
import org.scalatest.BeforeAndAfter
import org.scalatest.mock.MockitoSugar
import uk.gov.hmrc.play.test.UnitSpec
import iht.testhelpers.TestHelper._

class DeclarationSectionViewModelTest
  extends UnitSpec with FakeIhtApp with MockitoSugar with TestUtils with BeforeAndAfter {

  val applicationDetails = CommonBuilder.buildApplicationDetails
  val regDetails = CommonBuilder.buildRegistrationDetails.copy(
    deceasedDetails = Some(CommonBuilder.buildDeceasedDetails.copy(maritalStatus = Some(TestHelper.MaritalStatusMarried))),
        deceasedDateOfDeath = Some(CommonBuilder.buildDeceasedDateOfDeath.copy(LocalDate.now().minusYears(1)))
  )
  val regDetailsDeceasedSingle = CommonBuilder.buildRegistrationDetails.copy(
    deceasedDetails = Some(CommonBuilder.buildDeceasedDetails.copy(maritalStatus = Some(TestHelper.MaritalStatusSingle))))

  val regDetailsDeceasedMarried = CommonBuilder.buildRegistrationDetails.copy(
    deceasedDetails = Some(CommonBuilder.buildDeceasedDetails.copy(maritalStatus = Some(TestHelper.MaritalStatusMarried))))

 private def buildShareableBasicElement(singleValue: BigDecimal, jointValue: BigDecimal) = {
    CommonBuilder.buildShareableBasicElementExtended.copy(Some(singleValue),
      Some(jointValue), Some(true), Some(true))
  }

  "DeclarationSectionViewModel" must {

   "create declarationSectionStatus as InComplete" when {
      "any of Assets, Gifts and Debts are incomplete" in {
        val  incompleteAppDetails= applicationDetails.copy(
          allAssets = Some(CommonBuilder.buildAllAssetsWithAllSectionsFilled))

        val viewModel = DeclarationSectionViewModel(regDetails, incompleteAppDetails)
        viewModel.declarationSectionStatus shouldBe InComplete
      }
    }

    "create declarationSectionStatus as InComplete" when {
      "Assets, Gifts, Debts are complete but exemptions are not complete and estate has no tnrb" in {

        val incompleteExemptions = CommonBuilder.buildAllExemptions.copy(
          partner = Some(CommonBuilder.buildPartnerExemption))
        val  incompleteAppDetails = CommonBuilder.buildApplicationDetailsWithAssetsGiftsAndDebts.copy(
          allExemptions = Some(incompleteExemptions))

        val viewModel = DeclarationSectionViewModel(regDetails, incompleteAppDetails)
        viewModel.declarationSectionStatus shouldBe InComplete
      }
    }

    "create declarationSectionStatus as InComplete" when {
      "Assets, Gifts, Debts, Exemptions are complete but  tnrb is not complete" in {

        val incompleteTnrb = CommonBuilder.buildTnrbEligibility.copy(firstName = None)
        val  incompleteAppDetails= CommonBuilder.buildApplicationDetailsWithAssetsGiftsAndDebts.copy(
          allExemptions = Some(CommonBuilder.buildAllExemptions.copy(
            partner = Some(CommonBuilder.buildPartnerExemption),
            charity = Some(BasicExemptionElement(Some(true))),
            qualifyingBody = Some(BasicExemptionElement(Some(false))))),
          charities = Seq(CommonBuilder.buildCharity.copy(
            Some("1"),Some("testCharity"),Some("123456"), Some(BigDecimal(8000)))),
          increaseIhtThreshold = Some(incompleteTnrb),
          widowCheck = Some(CommonBuilder.buildWidowedCheck)
        )

        val viewModel = DeclarationSectionViewModel(regDetails, incompleteAppDetails)
        viewModel.declarationSectionStatus shouldBe InComplete
      }
    }

    "create declarationSectionStatus as NotDeclarable" when {
      "Assets, Gifts, Debts are completed and Estate value is above the threshold with no exemptions and tnrb and no kickout" in {
        val completeAppDetails = CommonBuilder.buildApplicationDetailsWithAssetsGiftsAndDebts.copy(
          allAssets = Some(CommonBuilder.buildAllAssetsWithAllSectionsFilled.copy(
            money = Some(buildShareableBasicElement(200000, 150000))))
        )

        val viewModel = DeclarationSectionViewModel(regDetails, completeAppDetails)
        viewModel.declarationSectionStatus shouldBe NotDeclarable
      }
    }

    "create declarationSectionStatus as NotDeclarable" when {
      "Assets, Gifts, Debts are completed and Estate value is above the threshold with tnrb and no kickout" in {
        val completeAppDetails = CommonBuilder.buildApplicationDetailsWithAssetsGiftsAndDebts.copy(
          allAssets = Some(CommonBuilder.buildAllAssetsWithAllSectionsFilled.copy(
            money = Some(buildShareableBasicElement(400000, 450000)))),
          increaseIhtThreshold = Some(CommonBuilder.buildTnrbEligibility),
          widowCheck = Some(CommonBuilder.buildWidowedCheck)
        )

        val viewModel = DeclarationSectionViewModel(regDetails, completeAppDetails)
        viewModel.declarationSectionStatus shouldBe NotDeclarable
      }
    }

    "create declarationSectionStatus as NotDeclarable" when {
      "Assets, Gifts, Debts are completed and Estate value is above the threshold with exemptions, tnrb and no kickout" in {
        val completeAppDetails = CommonBuilder.buildApplicationDetailsWithAssetsGiftsAndDebts.copy(
          allAssets = Some(CommonBuilder.buildAllAssetsWithAllSectionsFilled.copy(
                      money = Some(buildShareableBasicElement(400000, 450000)))),
          allExemptions = Some(CommonBuilder.buildAllExemptions.copy(
            partner = Some(CommonBuilder.buildPartnerExemption),
            charity = Some(BasicExemptionElement(Some(true))),
            qualifyingBody = Some(BasicExemptionElement(Some(false))))),
          charities = Seq(CommonBuilder.buildCharity.copy(
            Some("1"),Some("testCharity"),Some("123456"), Some(BigDecimal(8000)))),
          increaseIhtThreshold = Some(CommonBuilder.buildTnrbEligibility),
          widowCheck = Some(CommonBuilder.buildWidowedCheck)
        )

        val viewModel = DeclarationSectionViewModel(regDetails, completeAppDetails)
        viewModel.declarationSectionStatus shouldBe NotDeclarable
      }
    }

    "create declarationSectionStatus as NotDeclarable" when {
      "Assets, Gifts, Debts are completed and Estate value is above the threshold with exemptions " +
        "and widowcheck question is answered No" in {
        val completeAppDetails = CommonBuilder.buildApplicationDetailsWithAssetsGiftsAndDebts.copy(
          allAssets = Some(CommonBuilder.buildAllAssetsWithAllSectionsFilled.copy(
            money = Some(buildShareableBasicElement(400000,450000))
          )),
          allExemptions = Some(CommonBuilder.buildAllExemptions.copy(
            partner = Some(CommonBuilder.buildPartnerExemption),
            charity = Some(BasicExemptionElement(Some(true))),
            qualifyingBody = Some(BasicExemptionElement(Some(false))))),
          charities = Seq(CommonBuilder.buildCharity.copy(
            Some("1"),Some("testCharity"),Some("123456"), Some(BigDecimal(8000)))),
          widowCheck = Some(CommonBuilder.buildWidowedCheck.copy(widowed = Some(false)))
        )

        val viewModel = DeclarationSectionViewModel(regDetails, completeAppDetails)
        viewModel.declarationSectionStatus shouldBe NotDeclarable
      }
    }

    "create declarationSectionStatus as NotDeclarable" when {
      "Assets, Gifts, Debts are completed and Estate value is more than £1 M with no exemptions and tnrb " in {
        val completeAppDetails = CommonBuilder.buildApplicationDetailsWithAssetsGiftsAndDebts.copy(
          allAssets = Some(CommonBuilder.buildAllAssetsWithAllSectionsFilled.copy(
            money = Some(buildShareableBasicElement(800000, 450000)))))

        val viewModel = DeclarationSectionViewModel(regDetails, completeAppDetails)
        viewModel.declarationSectionStatus shouldBe NotDeclarable
      }
    }

    "create declarationSectionStatus as NotDeclarable" when {
      "Assets, Gifts, Debts are completed and Estate value is more than £325 K after exemptions and Deceased is single " in {
        val completeAppDetails = CommonBuilder.buildApplicationDetailsWithAssetsGiftsAndDebts.copy(
          allAssets = Some(CommonBuilder.buildAllAssetsWithAllSectionsFilled.copy(
            money = Some(buildShareableBasicElement(800000, 450000)))))

        val viewModel = DeclarationSectionViewModel(regDetailsDeceasedSingle, completeAppDetails)
        viewModel.declarationSectionStatus shouldBe NotDeclarable
      }
    }

    "create declarationSectionStatus as NotDeclarable" when {
      "Assets, Gifts, Debts are completed and Estate value is more than £325 K and has kickout in Exemptions " in {
        val completeAppDetails = CommonBuilder.buildApplicationDetailsWithAssetsGiftsAndDebts.copy(
          allAssets = Some(CommonBuilder.buildAllAssetsWithAllSectionsFilled.copy(
            money = Some(buildShareableBasicElement(400000, 4000)))),
          allExemptions = Some(CommonBuilder.buildAllExemptions.copy(
            partner = Some(CommonBuilder.buildPartnerExemption.copy(isPartnerHomeInUK = Some(false))),
            charity = Some(BasicExemptionElement(Some(true))),
            qualifyingBody = Some(BasicExemptionElement(Some(false))))),
          charities = Seq(CommonBuilder.buildCharity.copy(
            Some("1"),Some("testCharity"),Some("123456"), Some(BigDecimal(80000)))))

        val viewModel = DeclarationSectionViewModel(regDetailsDeceasedMarried, completeAppDetails)
        viewModel.declarationSectionStatus shouldBe InComplete
      }
    }

    "create declarationSectionStatus as Declarable" when {
      "Assets, Gifts, Debts are completed and Estate value is below the threshold with no kick out" in {
        val completeAppDetails = CommonBuilder.buildApplicationDetailsWithAssetsGiftsAndDebts

        val viewModel = DeclarationSectionViewModel(regDetails, completeAppDetails)

        viewModel.declarationSectionStatus shouldBe Declarable
      }
    }

    "create declarationSectionStatus as Declarable" when {
      "Assets, Gifts, Debts are complete, Estate value is below the threshold after exemptions " +
        "and widowcheck question is answered No" in {
        val completeAppDetails = CommonBuilder.buildApplicationDetailsWithAssetsGiftsAndDebts.copy(
          allExemptions = Some(CommonBuilder.buildAllExemptions.copy(
            partner = Some(CommonBuilder.buildPartnerExemption),
            charity = Some(BasicExemptionElement(Some(true))),
            qualifyingBody = Some(BasicExemptionElement(Some(false))))),
          charities = Seq(CommonBuilder.buildCharity.copy(
            Some("1"),Some("testCharity"),Some("1456"), Some(BigDecimal(8000)))),
          widowCheck = Some(CommonBuilder.buildWidowedCheck.copy(widowed = Some(false)))
        )

        val viewModel = DeclarationSectionViewModel(regDetails, completeAppDetails)

        viewModel.declarationSectionStatus shouldBe Declarable
      }
    }

    "create declarationSectionStatus as Declarable" when {
      "all the required application sections are complete and total estate value is below threshold" +
        " after exemptions and tnrb" in {

        val propertyList = List(CommonBuilder.buildProperty.copy(Some("1"), Some(CommonBuilder.DefaultUkAddress),
          TestHelper.PropertyTypeDeceasedHome, TestHelper.TypesOfOwnershipDeceasedOnly,
          TestHelper.TenureFreehold, Some(230000)),
          CommonBuilder.buildProperty.copy(Some("2"), Some(CommonBuilder.DefaultUkAddress),
            TestHelper.PropertyTypeDeceasedHome, TestHelper.TypesOfOwnershipDeceasedOnly,
            TestHelper.TenureFreehold, Some(230000)))

        val appDetails = CommonBuilder.buildApplicationDetailsWithAssetsGiftsAndDebts.copy(
          propertyList = propertyList,
          allExemptions = Some(CommonBuilder.buildAllExemptions.copy(
            partner = Some(CommonBuilder.buildPartnerExemption),
            charity = Some(BasicExemptionElement(Some(true))),
            qualifyingBody = Some(BasicExemptionElement(Some(false))))),
          charities = Seq(CommonBuilder.buildCharity.copy(
            Some("1"),Some("testCharity"),Some("123456"), Some(BigDecimal(80000)))),
          increaseIhtThreshold = Some(CommonBuilder.buildTnrbEligibility),
          widowCheck = Some(CommonBuilder.buildWidowedCheck))

        val viewModel = DeclarationSectionViewModel(regDetails, appDetails)

        viewModel.declarationSectionStatus shouldBe Declarable
      }
    }

    "create declarationSectionStatus as Declarable" when {
      "all the required application sections are complete, deceased is single and " +
        "net estate value is below threshold after exemptions" in {

        val propertyList = List(CommonBuilder.buildProperty.copy(Some("1"), Some(CommonBuilder.DefaultUkAddress),
          TestHelper.PropertyTypeDeceasedHome, TestHelper.TypesOfOwnershipDeceasedOnly,
          TestHelper.TenureFreehold, Some(230000)),
          CommonBuilder.buildProperty.copy(Some("2"), Some(CommonBuilder.DefaultUkAddress),
            TestHelper.PropertyTypeDeceasedHome, TestHelper.TypesOfOwnershipDeceasedOnly,
            TestHelper.TenureFreehold, Some(230000)))

        val appDetails = CommonBuilder.buildApplicationDetailsWithAssetsGiftsAndDebts.copy(
          propertyList = propertyList,
          allExemptions = Some(CommonBuilder.buildAllExemptions.copy(
            partner = Some(CommonBuilder.buildPartnerExemption),
            charity = Some(BasicExemptionElement(Some(true))),
            qualifyingBody = Some(BasicExemptionElement(Some(false))))),
          charities = Seq(CommonBuilder.buildCharity.copy(
            Some("1"),Some("testCharity"),Some("123456"), Some(BigDecimal(80000))),
            CommonBuilder.buildCharity.copy(
              Some("2"),Some("testCharity"),Some("123768"), Some(BigDecimal(80000))))
         )

        val viewModel = DeclarationSectionViewModel(regDetailsDeceasedSingle, appDetails)

        viewModel.declarationSectionStatus shouldBe Declarable
      }
    }

  }
}

/*
 * Copyright 2016 HM Revenue & Customs
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

package iht.models.application.debts

import iht.testhelpers.{AssetsWithAllSectionsSetToNoBuilder, CommonBuilder}
import org.scalatest.mock.MockitoSugar
import uk.gov.hmrc.play.test.UnitSpec

class AllLiabilitiesTest extends UnitSpec with MockitoSugar {

  "areAllDebtsSectionsAnsweredNo" must {
    "returns true when all sections answered no in debts" in {
      val appDetails = AssetsWithAllSectionsSetToNoBuilder.buildApplicationDetails
      appDetails.allLiabilities.map(_.areAllDebtsSectionsAnsweredNo) shouldBe Some(true)
    }

    "returns false when all but 1 sections answered no in debts" in {
      val appDetails = AssetsWithAllSectionsSetToNoBuilder.buildApplicationDetails copy (
        allLiabilities = Some(AssetsWithAllSectionsSetToNoBuilder.buildAllLiabilities copy (
          funeralExpenses = Some(BasicEstateElementLiabilities(isOwned = Some(true), value = None)))
        ))
      appDetails.allLiabilities.map(_.areAllDebtsSectionsAnsweredNo) shouldBe Some(false)
    }
  }

  "areAllDebtsExceptMortgagesCompleted" must {
    "display true when all debts section are complete" in {
      val appDetails = AssetsWithAllSectionsSetToNoBuilder.buildApplicationDetails
      appDetails.allLiabilities.flatMap(_.areAllDebtsExceptMortgagesCompleted) shouldBe Some(true)
    }

    "display false when all but one of debts section are complete" in {
      val appDetails = AssetsWithAllSectionsSetToNoBuilder.buildApplicationDetails copy (
        allLiabilities = Some(AssetsWithAllSectionsSetToNoBuilder.buildAllLiabilities copy (
          funeralExpenses = Some(BasicEstateElementLiabilities(isOwned = Some(true), value = None)))
        ))
      appDetails.allLiabilities.flatMap(_.areAllDebtsExceptMortgagesCompleted) shouldBe Some(false)
    }

    "display false when it has only one completed debt section" in {
      val appDetails = CommonBuilder.buildApplicationDetails copy (
        allLiabilities = Some(CommonBuilder.buildAllLiabilities copy (
          funeralExpenses = Some(CommonBuilder.buildBasicEstateElementLiabilities.copy(
            isOwned = Some(false), value = None)),
          trust = None,
          debtsOutsideUk = None,
          jointlyOwned = None,
          other = None)
        ))

      appDetails.allLiabilities.flatMap(_.areAllDebtsExceptMortgagesCompleted) shouldBe Some(false)
    }

    "display false when all of the debts section are not complete" in {

      val allLiabilities = Some(CommonBuilder.buildAllLiabilities copy (
        funeralExpenses = Some(CommonBuilder.buildBasicEstateElementLiabilities.copy(
          isOwned = Some(true), value = Some(BigDecimal(1000000)))),
        trust = Some(CommonBuilder.buildBasicEstateElementLiabilities.copy(
          isOwned = Some(true), value = Some(BigDecimal(1000000)))),
        debtsOutsideUk = Some(CommonBuilder.buildBasicEstateElementLiabilities.copy(
          isOwned = Some(true), Some(BigDecimal(1000000)))),
        jointlyOwned = Some(CommonBuilder.buildBasicEstateElementLiabilities.copy(
          isOwned = None, value = None)),
        other = Some(CommonBuilder.buildBasicEstateElementLiabilities.copy(
          isOwned = Some(true), Some(BigDecimal(1000000))))))

      val appDetails = CommonBuilder.buildApplicationDetails copy(
        allLiabilities = allLiabilities,
        allAssets = Some(CommonBuilder.buildAllAssets.copy(properties = Some(CommonBuilder.buildProperties)))
        )

      appDetails.areAllDebtsCompleted shouldBe Some(false)
    }

  }

  "doesAnyDebtSectionHaveAValue" must {
    "return true when one debts section has a value" in {
      val appDetails = AssetsWithAllSectionsSetToNoBuilder.buildApplicationDetails copy (
        allLiabilities = Some(AssetsWithAllSectionsSetToNoBuilder.buildAllLiabilities copy (
          funeralExpenses = Some(BasicEstateElementLiabilities(isOwned = Some(true), value = Some(BigDecimal(22)))))
        ))
      appDetails.allLiabilities.map(_.doesAnyDebtSectionHaveAValue) shouldBe Some(true)
    }

    "return false when no debts section has a value" in {
      val appDetails = AssetsWithAllSectionsSetToNoBuilder.buildApplicationDetails copy (
        allLiabilities = Some(AssetsWithAllSectionsSetToNoBuilder.buildAllLiabilities copy(
          funeralExpenses = None,
          trust = None,
          debtsOutsideUk = None,
          jointlyOwned = None,
          other = None,
          mortgages = None
          )
        ))
      appDetails.allLiabilities.map(_.doesAnyDebtSectionHaveAValue) shouldBe Some(false)
    }
  }

  "totalValue" must {
    "return the correct value" in {
      val appDetails = AssetsWithAllSectionsSetToNoBuilder.buildApplicationDetails copy (
        allLiabilities = Some(AssetsWithAllSectionsSetToNoBuilder.buildAllLiabilities copy(
          funeralExpenses = Some(BasicEstateElementLiabilities(isOwned = Some(true), value = Some(BigDecimal(22)))),
          trust = Some(BasicEstateElementLiabilities(isOwned = Some(true), value = Some(BigDecimal(122)))),
          debtsOutsideUk = Some(BasicEstateElementLiabilities(isOwned = Some(true), value = Some(BigDecimal(222)))),
          jointlyOwned = Some(BasicEstateElementLiabilities(isOwned = Some(true), value = Some(BigDecimal(322)))),
          other = Some(BasicEstateElementLiabilities(isOwned = Some(true), value = Some(BigDecimal(422))))
          )
        ))
      appDetails.allLiabilities.map(_.totalValue()) shouldBe Some(BigDecimal(1110))
    }
  }

  "mortgageValue" must {
    "return the correct value" in {
      val appDetails = AssetsWithAllSectionsSetToNoBuilder.buildApplicationDetails copy (
        allLiabilities = Some(AssetsWithAllSectionsSetToNoBuilder.buildAllLiabilities copy(
          mortgages = Some(MortgageEstateElement(isOwned = Some(true),
            mortgageList = List( Mortgage("", Some(434), Some(true)),
              Mortgage("", Some(2331), Some(true)))))
          )
        ))
      appDetails.allLiabilities.map(_.mortgageValue) shouldBe Some(BigDecimal(2765))
    }
  }
}

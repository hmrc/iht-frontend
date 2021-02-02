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

package iht.models.application

import iht.config.AppConfig
import iht.models.application.assets._
import iht.models.application.debts.AllLiabilities
import iht.models.application.exemptions._
import iht.models.application.gifts._
import iht.models.application.tnrb.{TnrbEligibiltyModel, WidowCheck}
import iht.utils.{CommonHelper, ApplicationStatus => AppStatus}
import play.api.libs.json.Json

case class ApplicationDetails(allAssets: Option[AllAssets] = None,
                              propertyList: List[Property] = Nil,
                              allLiabilities: Option[AllLiabilities] = None,
                              allExemptions: Option[AllExemptions] = None,
                              allGifts: Option[AllGifts] = None,
                              giftsList: Option[Seq[PreviousYearsGifts]] = None,
                              charities: Seq[Charity] = Seq(),
                              qualifyingBodies: Seq[QualifyingBody] = Seq(),
                              widowCheck: Option[WidowCheck] = None,
                              increaseIhtThreshold: Option[TnrbEligibiltyModel] = None,
                              status: String = AppStatus.InProgress,
                              kickoutReason: Option[String] = None,
                              ihtRef: Option[String] = None,
                              reasonForBeingBelowLimit: Option[String] = None,
                              hasSeenExemptionGuidance: Option[Boolean] = Some(false)
                             ) {

  //Assets section starts

  def areAllAssetsCompleted = CommonHelper.aggregateOfSeqOfOption(
    Seq[Option[Boolean]](isCompleteProperties,
      allAssets.flatMap(_.money).flatMap(_.isComplete),
      allAssets.flatMap(_.household).flatMap(_.isComplete),
      allAssets.flatMap(_.vehicles).flatMap(_.isComplete),
      allAssets.flatMap(_.privatePension).flatMap(_.isComplete),
      allAssets.flatMap(_.stockAndShare).flatMap(_.isComplete),
      allAssets.flatMap(_.insurancePolicy).flatMap(_.isComplete),
      allAssets.flatMap(_.businessInterest).flatMap(_.isComplete),
      allAssets.flatMap(_.nominated).flatMap(_.isComplete),
      allAssets.flatMap(_.heldInTrust).flatMap(_.isComplete),
      allAssets.flatMap(_.foreign).flatMap(_.isComplete),
      allAssets.flatMap(_.moneyOwed).flatMap(_.isComplete),
      allAssets.flatMap(_.other).flatMap(_.isComplete))
  )

  def isCompleteProperties: Option[Boolean] = {
    val propertiesIsOwned = allAssets.flatMap(_.properties).flatMap(_.isOwned)
    val allPropertiesComplete = propertiesIsOwned.map(isOwned =>
      if(isOwned) propertyList.nonEmpty && propertyList.forall(_.isComplete) else true )

    (propertiesIsOwned, allPropertiesComplete) match {
      case (None, _) => None
      case (Some(true), Some(false)) => Some(false)
      case _ => Some(true)
    }
  }

  def totalAssetsValue: BigDecimal =
    allAssets.map(_.totalValueWithoutProperties).getOrElse(BigDecimal(0)) +
      propertyList.map(_.value.getOrElse(BigDecimal(0))).sum

  //Assets section ends

  //Gifts section starts

  def totalPastYearsGiftsOption: Option[BigDecimal] = {
    val values = giftsList.getOrElse(Nil).map(x => x.value).filter(_.isDefined)
    val exemptions = giftsList.getOrElse(Nil).map(x => x.exemptions).filter(_.isDefined)

    val totalValue = if (values.nonEmpty) {Some(values.flatten.sum)} else None
    val totalExemptions = if (exemptions.nonEmpty) {Some(exemptions.flatten.sum)} else None

    (totalValue, totalExemptions) match {
      case (Some(v), Some(e)) => Some(v - e)
      case (Some(v), None) => Some(v)
      case _ => None
    }
  }

  def totalPastYearsGiftsValueExcludingExemptions: BigDecimal =
    CommonHelper.getOrZero(totalPastYearsGiftsValueExcludingExemptionsOption)

  def totalPastYearsGiftsValueExcludingExemptionsOption: Option[BigDecimal] = {
    val values = giftsList.getOrElse(Nil).map(x => x.value).filter(_.isDefined)

    val totalValue = if (values.nonEmpty) {Some(values.flatten.sum)} else None

    totalValue match {
      case Some(v) => Some(v)
      case _ => None
    }
  }

  def totalPastYearsGiftsExemptions: BigDecimal =
    CommonHelper.getOrZero(totalPastYearsGiftsExemptionsOption)


  def totalPastYearsGiftsExemptionsOption: Option[BigDecimal] = {
    val exemptions = giftsList.getOrElse(Nil).map(x => x.exemptions).filter(_.isDefined)

    val totalExemptions = if (exemptions.nonEmpty) {Some(exemptions.flatten.sum)} else None

    totalExemptions match {
      case Some(e) => Some(e)
      case _ => None
    }
  }

  def isValueEnteredForPastYearsGifts: Boolean = {
    giftsList.getOrElse(Nil).exists(xx=>xx.value.isDefined || xx.exemptions.isDefined)
  }

  def areAllGiftSectionsCompleted: Option[Boolean] = {
    allGifts match {
      case Some(gifts) =>
        val givenAway = gifts.isGivenAway
        val isReservation = gifts.isReservation
        val isToTrust = gifts.isToTrust
        val isGivenInLast7Years = gifts.isGivenInLast7Years

        (givenAway, isReservation, isToTrust, isGivenInLast7Years, isValueEnteredForPastYearsGifts) match {
          case (Some(false), Some(false), Some(false), Some(false), _) => Some(true)
          case (Some(true), Some(_), Some(_), Some(_), true) => Some(true)
          case (Some(true), None, _, _, true) => Some(false)
          case (Some(true), _, None, _, true) => Some(false)
          case (Some(true), _, _, None, true) => Some(false)
          case (Some(true), _, _, _, false) => Some(false)
          case _ => Some(false)
        }
      case _ => None
    }
  }

  //Gifts section ends

  //Debts section starts

  def areAllDebtsCompleted: Option[Boolean] =
    CommonHelper.aggregateOfSeqOfOption{
      Seq(allLiabilities.flatMap(_.areAllDebtsExceptMortgagesCompleted), isCompleteMortgages)
    }

  def totalLiabilitiesValue:BigDecimal = CommonHelper.getOrZero(totalLiabilitiesValueOption)

  def totalLiabilitiesValueOption: Option[BigDecimal] = {
    if (allLiabilities.map(_.doesAnyDebtSectionHaveAValue).fold(false)(identity)) {
      allLiabilities.map(_.totalValue())
    } else {
      None
    }
  }

  def isCompleteMortgages: Option[Boolean] = {
    def checkIsCompleteMortgagesForNonEmptyPropertyList = {
      val allMortgagesComplete = allLiabilities.flatMap(_.mortgages).map(_.mortgageList).
        fold(false)(_.forall(_.isComplete.getOrElse(false)))

      val mortgageListSize = allLiabilities.flatMap(_.mortgages).map(_.mortgageList).fold(0)(_.size)
      val sizeNotMatched = propertyList.size != mortgageListSize

      (sizeNotMatched, allMortgagesComplete) match {
        case (true, _) => Some(false)
        case (false, false) => Some(false)
        case (false, true) => Some(true)
      }
    }

    val propertiesIsOwned: Option[Boolean] = allAssets.flatMap(_.properties).flatMap(_.isOwned)

    propertiesIsOwned match {
      case Some(true) if propertyList.nonEmpty => checkIsCompleteMortgagesForNonEmptyPropertyList
      case Some(true) => Some(false)
      case Some(false) => Some(true)
      case None => None
    }
  }

  //Debts section ends

  //Exemptions section starts
  def areAllAssetsGiftsAndDebtsCompleted: Boolean =
    (areAllAssetsCompleted, areAllGiftSectionsCompleted, areAllDebtsCompleted) match {
      case (Some(true), Some(true), Some(true)) => true
      case _ => false
    }

  def isCompleteCharities: Option[Boolean] = {
    val charitiesIsOwned = allExemptions.flatMap(_.charity).flatMap(_.isSelected)
    val allCharitiesComplete = charitiesIsOwned.map(isOwned =>
      if(isOwned) charities.nonEmpty && !charities.exists(!_.isComplete) else true )

    (charitiesIsOwned, allCharitiesComplete) match {
      case (None, _) => None
      case (Some(true), Some(false)) => Some(false)
      case _ => Some(true)
    }
  }

  def isCompleteQualifyingBodies: Option[Boolean] = {
    val qualifyingBodiesIsOwned = allExemptions.flatMap(_.qualifyingBody).flatMap(_.isSelected)
    val allQualifyingBodiesComplete = qualifyingBodiesIsOwned.map(isOwned =>
      if(isOwned) qualifyingBodies.nonEmpty && !qualifyingBodies.exists(!_.isComplete) else true )

    (qualifyingBodiesIsOwned, allQualifyingBodiesComplete) match {
      case (None, _) => None
      case (Some(true), Some(false)) => Some(false)
      case _ => Some(true)
    }
  }

  def isExemptionsCompletedWithNoValue = allExemptions.fold(false){_.isExemptionsSectionCompletedWithNoValue}

  def isExemptionsCompletedWithoutPartnerExemptionWithNoValue =
    allExemptions.fold(false){_.isExemptionsSectionCompletedWithoutPartnerExemptionWithNoValue}

  def noExemptionsHaveBeenAnswered: Boolean = allExemptions.isEmpty

  def isValueEnteredForExemptions: Boolean = {
    allExemptions.fold(false)(_.charity.nonEmpty) ||
      allExemptions.fold(false)(_.qualifyingBody.nonEmpty) ||
      allExemptions.fold(false)(_.partner.fold(false)(_.totalAssets.exists(partner => true))) ||
      charities.nonEmpty || qualifyingBodies.nonEmpty
  }

  def totalExemptionsValue:BigDecimal = charities.map(_.totalValue.fold(BigDecimal(0))(identity)).sum +
    qualifyingBodies.map(_.totalValue.fold(BigDecimal(0))(identity)).sum +
    allExemptions.flatMap(_.partner.map(_.totalAssets.getOrElse(BigDecimal(0)))).sum

  def totalExemptionsValueOption: Option[BigDecimal] = {
    if (charities.map(_.totalValue).isEmpty &&
      qualifyingBodies.map(_.totalValue).isEmpty &&
      allExemptions.flatMap(_.partner.flatMap(_.totalAssets)).isEmpty) {
      None
    } else {
      Some(totalExemptionsValue)
    }
  }

  def netValueAfterExemptionAndDebtsForPositiveExemption:BigDecimal = {
    if (totalExemptionsValue>0) (totalValue - totalLiabilitiesValue) - totalExemptionsValue else totalValue-totalExemptionsValue
  }

  //Exemptions section ends

  //Tnrb section starts

  def isWidowCheckSectionCompleted: Boolean = widowCheck.flatMap(_.dateOfPreDeceased).fold(false)(_=>true)

  def isWidowCheckQuestionAnswered: Boolean = widowCheck.flatMap(_.widowed).fold(false)(_=>true)

  def isSuccessfulTnrbCase: Boolean = {
    (increaseIhtThreshold, widowCheck)  match {
      case (Some(TnrbEligibiltyModel(Some(true), Some(false), Some(false), Some(false),
                                    Some(false), Some(true), Some(true), Some(_), Some(_), Some(_), _)),
      Some(WidowCheck(Some(true), Some(_)))) => true
      case _ => false
    }
  }

  //Tnrb section ends

  def totalValue:BigDecimal = totalAssetsValue + CommonHelper.getOrZero(totalPastYearsGiftsOption)

  def totalNetValue:BigDecimal = (totalAssetsValue + CommonHelper.getOrZero(totalPastYearsGiftsOption)) - totalExemptionsValue - totalLiabilitiesValue

  def currentThreshold(implicit appConfig: AppConfig): BigDecimal =
    if (isSuccessfulTnrbCase) appConfig.transferredNilRateBand else appConfig.exemptionsThresholdValue
}

object ApplicationDetails {
  implicit val formats = Json.format[ApplicationDetails]
}

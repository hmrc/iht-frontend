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

package iht.controllers.testonly

import iht.models.application.ApplicationDetails
import iht.models.application.assets._
import iht.models.application.basicElements.{BasicEstateElement, ShareableBasicEstateElement}
import iht.models.application.debts._
import iht.models.application.gifts._
import iht.utils.ApplicationStatus

object TestOnlyDataGenerator {
  val buildShareableBasicEstateElement = ShareableBasicEstateElement(isOwned = Some(false),value= None, isOwnedShare=Some(false), shareValue= None)
  val buildPrivatePension = PrivatePension(
    isChanged = None,
    value = None,
    isOwned = Some(false)
  )
  val buildStockAndShare = StockAndShare(
    valueNotListed= None,
    valueListed = None,
    value = None,
    isNotListed = Some(false),
    isListed = Some(false)
  )

  val buildInsurancePolicy = InsurancePolicy(
    isAnnuitiesBought = None,
    isInsurancePremiumsPayedForSomeoneElse = Some(false),
    value = None,
    shareValue = None,
    policyInDeceasedName = Some(false),
    isJointlyOwned = Some(false),
    isInTrust = None,
    coveredByExemption = None,
    sevenYearsBefore = None,
    moreThanMaxValue = None
  )

  val buildBasicElement = BasicEstateElement(
    isOwned = Some(false),
    value= None
  )

  val buildAssetsHeldInTrust = HeldInTrust(
    isOwned=Some(false),
    isMoreThanOne = None,
    value =None
  )

  val buildProperties = Properties(
    isOwned=Some(false)
  )

  val buildAllAssets = AllAssets(
    action = None,
    money= Some(buildShareableBasicEstateElement),
    household = Some(buildShareableBasicEstateElement),
    vehicles = Some(buildShareableBasicEstateElement),
    privatePension = Some(buildPrivatePension),
    stockAndShare = Some(buildStockAndShare),
    insurancePolicy = Some(buildInsurancePolicy),
    businessInterest = Some(buildBasicElement),
    nominated = Some(buildBasicElement),
    heldInTrust = Some(buildAssetsHeldInTrust),
    foreign = Some(buildBasicElement),
    moneyOwed = Some(buildBasicElement),
    other = Some(BasicEstateElement(
      isOwned = Some(true),
      // scalastyle:off magic.number
      value= Some(BigDecimal(1000))
      // scalastyle:on magic.number
    )),
    properties = Some(buildProperties)
  )

  val buildBasicEstateElementLiabilities = BasicEstateElementLiabilities(
    isOwned = Some(false),
    value= None
  )

  val buildMortgageEstateElement = MortgageEstateElement(
    isOwned = Some(false)
  )

  val buildAllLiabilities= AllLiabilities(
    funeralExpenses=Some(buildBasicEstateElementLiabilities),
    trust = Some(buildBasicEstateElementLiabilities),
    debtsOutsideUk = Some(buildBasicEstateElementLiabilities),
    jointlyOwned = Some(buildBasicEstateElementLiabilities),
    other = Some(buildBasicEstateElementLiabilities),
    mortgages = Some(buildMortgageEstateElement)
  )

  val buildAllGifts = AllGifts(isGivenAway = Some(false),
    isReservation = Some(false),
    isToTrust  = Some(false),
    isGivenInLast7Years  = Some(false),
    action = None)

  def buildApplicationDetails(ihtRef:Option[String]) = ApplicationDetails(
    allAssets=Some(buildAllAssets),
    propertyList=Nil,
    allLiabilities=Some(buildAllLiabilities),
    allExemptions=None,
    allGifts = Some(buildAllGifts),
    charities= Seq(),
    qualifyingBodies = Seq(),
    widowCheck=None,
    increaseIhtThreshold=None,
    status= ApplicationStatus.InProgress,
    kickoutReason=None,
    ihtRef=ihtRef
  )
}

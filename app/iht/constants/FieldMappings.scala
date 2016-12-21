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

package iht.constants

import iht.constants.IhtProperties._
import play.api.i18n.Messages
import iht.constants.Constants._

import scala.collection.immutable.ListMap
object FieldMappings {

  val Yes="1"
  val No="0"

  val applicantCountryMap = ListMap(
    createMapEntry(applicantCountryEnglandOrWales, "iht.countries.englandOrWales"),
    createMapEntry(applicantCountryScotland , "iht.countries.scotland"),
    createMapEntry(applicantCountryNorthernIreland , "iht.countries.northernIreland")
  )

  val domicileMap = ListMap(
    createMapEntry(domicileEnglandOrWales,"iht.countries.englandOrWales"),
    createMapEntry(domicileScotland,"iht.countries.scotland"),
    createMapEntry(domicileNorthernIreland,"iht.countries.northernIreland"),
    createMapEntry(domicileOther,"page.iht.registration.deceasedDetails.domicile.other.label")
  )

  val maritalStatusMap = ListMap(
    createMapEntry(statusMarried,"page.iht.registration.deceasedDetails.maritalStatus.civilPartnership.label"),
    createMapEntry(statusDivorced,"page.iht.registration.deceasedDetails.maritalStatus.civilPartner.label"),
    createMapEntry(statusWidowed,"page.iht.registration.deceasedDetails.maritalStatus.widowed.label"),
    createMapEntry(statusSingle,"page.iht.registration.deceasedDetails.maritalStatus.single.label")
  )

  val maritalStatusWithHintMap = ListMap(
    statusMarried -> ((Messages("page.iht.registration.deceasedDetails.maritalStatus.civilPartnership.label"),
      Some(Messages("page.iht.registration.deceasedDetails.maritalStatus.civilPartnership.hint")))),
    statusDivorced -> ((Messages("page.iht.registration.deceasedDetails.maritalStatus.civilPartner.label"),
      Some(Messages("page.iht.registration.deceasedDetails.maritalStatus.civilPartner.hint")))),
    statusWidowed -> ((Messages("page.iht.registration.deceasedDetails.maritalStatus.widowed.label"),
      Some(Messages("page.iht.registration.deceasedDetails.maritalStatus.widowed.hint")))),
    statusSingle -> ((Messages("page.iht.registration.deceasedDetails.maritalStatus.single.label"),
      Some(Messages("page.iht.registration.deceasedDetails.maritalStatus.single.hint"))))
  )

  val yesNoMap = ListMap(
    Yes -> Messages("iht.yes"),
    No -> Messages("iht.no")
  )

  val propertyType = ListMap(
    createMapEntry(propertyTypeDeceasedHome , "page.iht.application.assets.propertyType.deceasedHome.label"),
    createMapEntry(propertyTypeOtherResidentialBuilding , "page.iht.application.assets.propertyType.otherResidential.label"),
    createMapEntry(propertyTypeNonResidential , "page.iht.application.assets.propertyType.nonResidential.label")
  )

  val typesOfOwnership = ListMap(
    ownershipDeceasedOnly -> ((Messages("page.iht.application.assets.typeOfOwnership.deceasedOnly.label"),
      Some(Messages("page.iht.application.assets.typeOfOwnership.deceasedOnly.hint")), Some(false))),
    ownershipJoint -> ((Messages("page.iht.application.assets.typeOfOwnership.joint.label"),
      Some(Messages("page.iht.application.assets.typeOfOwnership.joint.hint")), Some(true))),
    ownershipInCommon -> ((Messages("page.iht.application.assets.typeOfOwnership.inCommon.label"),
      Some(Messages("page.iht.application.assets.typeOfOwnership.inCommon.hint")), Some(true)))
  )

  val tenures = ListMap(
    tenureFreehold -> ((Messages("page.iht.application.assets.tenure.freehold.label"),
      Some(Messages("page.iht.application.assets.tenure.freehold.hint")), Some(false))),
    tenureLeasehold -> ((Messages("page.iht.application.assets.tenure.leasehold.label"),
      Some(Messages("page.iht.application.assets.tenure.leasehold.hint")), Some(false)))
  )

  val questionnaireEasyToUse = ListMap(
    createMapEntry(questionnaireEasyToUseVeryEasy,"page.iht.questionnaire.easy-to-use.very-easy"),
    createMapEntry(questionnaireEasyToUseEasy,"page.iht.questionnaire.easy-to-use.easy"),
    createMapEntry(questionnaireEasyToUseNeither,"page.iht.questionnaire.easy-to-use.neither"),
    createMapEntry(questionnaireEasyToUseDifficult,"page.iht.questionnaire.easy-to-use.difficult"),
    createMapEntry(questionnaireEasyToUseVeryDifficult,"page.iht.questionnaire.easy-to-use.very-difficult")
  )

  val questionnaireFeelingAboutExperience = ListMap(
    createMapEntry(questionnaireFeelingAboutExperienceVerySatisfied,"page.iht.questionnaire.feelingAboutExperience.verySatisfied"),
    createMapEntry(questionnaireFeelingAboutExperienceSatisfied,"page.iht.questionnaire.feelingAboutExperience.satisfied"),
    createMapEntry(questionnaireFeelingAboutExperienceNeither,"page.iht.questionnaire.feelingAboutExperience.neither"),
    createMapEntry(questionnaireFeelingAboutExperienceDissatisfied,"page.iht.questionnaire.feelingAboutExperience.dissatisfied"),
    createMapEntry(questionnaireFeelingAboutExperienceVeryDissatisfied,"page.iht.questionnaire.feelingAboutExperience.veryDissatisfied")
  )

  val filterChoices = ListMap(
    continueEstateReport -> Tuple3(Messages("page.iht.filter.filter.choice.main.continue"), None, None),
    register -> Tuple3(Messages("page.iht.filter.filter.choice.main.register"), Some(Messages("page.iht.filter.filter.choice.main.register.hint")), None),
    alreadyStarted -> Tuple3(Messages("page.iht.filter.filter.choice.main.alreadyStarted"), None, None),
    agent -> Tuple3(Messages("page.iht.filter.filter.choice.main.agent"), None, None))

  val domicileChoices = ListMap(
    createMapEntry(englandOrWales, "iht.countries.englandOrWales"),
    createMapEntry(scotland, "iht.countries.scotland"),
    createMapEntry(northernIreland, "iht.countries.northernIreland"),
    createMapEntry(otherCountry, "page.iht.filter.domicile.choice.other")
  )

  val estimateChoices = ListMap(
    createMapEntry(under325000, "page.iht.filter.estimate.choice.under"),
    createMapEntry(between325000and1million, "page.iht.filter.estimate.choice.between"),
    createMapEntry(moreThan1million, "page.iht.filter.estimate.choice.over")
  )
  
  val filterChoicesWithoutHints = ListMap(
    continueEstateReport -> Messages("page.iht.filter.filter.choice.main.continue"),
    register -> Messages("page.iht.filter.filter.choice.main.register"),
    alreadyStarted -> Messages("page.iht.filter.filter.choice.main.alreadyStarted"),
    agent -> Messages("page.iht.filter.filter.choice.main.agent"))

  /**
   * Read the key from application.conf file and return the key value pair
   *
   * @param x
   * @param y
   * @return String
   */
  private def createMapEntry(x : String, y : String) =
    x -> Messages(y)
}

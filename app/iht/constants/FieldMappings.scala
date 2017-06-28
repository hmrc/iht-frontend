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

package iht.constants

import javax.inject.{Inject, Singleton}

import play.api.i18n.Messages

import scala.collection.immutable.ListMap

@Singleton
class FieldMappings @Inject() (
                                ihtProperties: IhtProperties,
                                constants: Constants
                              ){

  val Yes="1"
  val No="0"

  def applicantCountryMap(implicit messages: Messages) = ListMap(
    createMapEntry(ihtProperties.applicantCountryEnglandOrWales, "iht.countries.englandOrWales")(messages),
    createMapEntry(ihtProperties.applicantCountryScotland, "iht.countries.scotland")(messages),
    createMapEntry(ihtProperties.applicantCountryNorthernIreland, "iht.countries.northernIreland")(messages)
  )

  def domicileMap(implicit messages: Messages) = ListMap(
    createMapEntry(ihtProperties.domicileEnglandOrWales, "iht.countries.englandOrWales")(messages),
    createMapEntry(ihtProperties.domicileScotland, "iht.countries.scotland")(messages),
    createMapEntry(ihtProperties.domicileNorthernIreland, "iht.countries.northernIreland")(messages),
    createMapEntry(ihtProperties.domicileOther, "iht.common.other")(messages)
  )

  def maritalStatusMap (implicit messages: Messages) = ListMap(
    createMapEntry(ihtProperties.statusMarried,"page.iht.registration.deceasedDetails.maritalStatus.civilPartnership.label")(messages),
    createMapEntry(ihtProperties.statusDivorced,"page.iht.registration.deceasedDetails.maritalStatus.civilPartner.label")(messages),
    createMapEntry(ihtProperties.statusWidowed,"page.iht.registration.deceasedDetails.maritalStatus.widowed.label")(messages),
    createMapEntry(ihtProperties.statusSingle,"page.iht.registration.deceasedDetails.maritalStatus.single.label")(messages)
  )

  def propertyType(implicit messages: Messages): ListMap[String, String] = ListMap(
    createMapEntry(ihtProperties.propertyTypeDeceasedHome , "page.iht.application.assets.propertyType.deceasedHome.label")(messages),
    createMapEntry(ihtProperties.propertyTypeOtherResidentialBuilding , "page.iht.application.assets.propertyType.otherResidential.label")(messages),
    createMapEntry(ihtProperties.propertyTypeNonResidential , "page.iht.application.assets.propertyType.nonResidential.label")(messages)
  )

  def typesOfOwnership(deceasedName: String)(implicit messages: Messages): ListMap[String, (String, Some[String], Some[Boolean])] = ListMap(
    ihtProperties.ownershipDeceasedOnly -> ((messages("page.iht.application.assets.typeOfOwnership.deceasedOnly.label", deceasedName),
      Some(messages("page.iht.application.assets.typeOfOwnership.deceasedOnly.hint", deceasedName)), Some(false))),
    ihtProperties.ownershipJoint -> ((messages("page.iht.application.assets.typeOfOwnership.joint.label"),
      Some(messages("page.iht.application.assets.typeOfOwnership.joint.hint", deceasedName)), Some(true))),
    ihtProperties.ownershipInCommon -> ((messages("page.iht.application.assets.typeOfOwnership.inCommon.label"),
      Some(messages("page.iht.application.assets.typeOfOwnership.inCommon.hint", deceasedName)), Some(true)))
  )

  def tenures(deceasedName: String)(implicit messages: Messages) = ListMap(
    ihtProperties.tenureFreehold -> ((messages("page.iht.application.assets.tenure.freehold.label"),
      Some(messages("page.iht.application.assets.tenure.freehold.hint", deceasedName)), Some(false))),
    ihtProperties.tenureLeasehold -> ((messages("page.iht.application.assets.tenure.leasehold.label"),
      Some(messages("page.iht.application.assets.tenure.leasehold.hint", deceasedName)), Some(false)))
  )

  def questionnaireEasyToUse (implicit messages: Messages) = ListMap(
    createMapEntry(ihtProperties.questionnaireEasyToUseVeryEasy,"page.iht.questionnaire.easy-to-use.very-easy")(messages),
    createMapEntry(ihtProperties.questionnaireEasyToUseEasy,"page.iht.questionnaire.easy-to-use.easy")(messages),
    createMapEntry(ihtProperties.questionnaireEasyToUseNeither,"page.iht.questionnaire.easy-to-use.neither")(messages),
    createMapEntry(ihtProperties.questionnaireEasyToUseDifficult,"page.iht.questionnaire.easy-to-use.difficult")(messages),
    createMapEntry(ihtProperties.questionnaireEasyToUseVeryDifficult,"page.iht.questionnaire.easy-to-use.very-difficult")(messages)
  )

  def questionnaireFeelingAboutExperience(implicit messages: Messages) = ListMap(
    createMapEntry(ihtProperties.questionnaireFeelingAboutExperienceVerySatisfied,"page.iht.questionnaire.feelingAboutExperience.verySatisfied")(messages),
    createMapEntry(ihtProperties.questionnaireFeelingAboutExperienceSatisfied,"page.iht.questionnaire.feelingAboutExperience.satisfied")(messages),
    createMapEntry(ihtProperties.questionnaireFeelingAboutExperienceNeither,"page.iht.questionnaire.feelingAboutExperience.neither")(messages),
    createMapEntry(ihtProperties.questionnaireFeelingAboutExperienceDissatisfied,"page.iht.questionnaire.feelingAboutExperience.dissatisfied")(messages),
    createMapEntry(ihtProperties.questionnaireFeelingAboutExperienceVeryDissatisfied,"page.iht.questionnaire.feelingAboutExperience.veryDissatisfied")(messages)
  )

  def filterChoices(implicit messages: Messages) = ListMap(
    constants.continueEstateReport -> Tuple3(messages("page.iht.filter.filter.choice.main.continue"), None, None),
    constants.register ->
      Tuple3(messages("page.iht.filter.filter.choice.main.register"),
        Some(messages("page.iht.filter.filter.choice.main.register.hint")), None),
    constants.alreadyStarted -> Tuple3(messages("page.iht.filter.filter.choice.main.alreadyStarted"), None, None),
    constants.agent -> Tuple3(messages("page.iht.filter.filter.choice.main.agent"), None, None))

  def domicileChoices(implicit messages: Messages) =
    ListMap(
      createMapEntry(constants.englandOrWales, "iht.countries.englandOrWales")(messages),
      createMapEntry(constants.scotland, "iht.countries.scotland")(messages),
      createMapEntry(constants.northernIreland,  "iht.countries.northernIreland")(messages),
      createMapEntry(constants.otherCountry, "page.iht.filter.domicile.choice.other")(messages)
    )

  def estimateChoices(implicit messages: Messages) = ListMap(
    createMapEntry(constants.under325000, "page.iht.filter.estimate.choice.under")(messages),
    createMapEntry(constants.between325000and1million, "page.iht.filter.estimate.choice.between")(messages),
    createMapEntry(constants.moreThan1million, "page.iht.filter.estimate.choice.over")(messages)
  )

  def filterChoicesWithoutHints(implicit messages: Messages) = ListMap(
    createMapEntry(constants.continueEstateReport, "page.iht.filter.filter.choice.main.continue")(messages),
    createMapEntry(constants.register, "page.iht.filter.filter.choice.main.register")(messages),
    createMapEntry(constants.alreadyStarted, "page.iht.filter.filter.choice.main.alreadyStarted")(messages),
    createMapEntry(constants.agent, "page.iht.filter.filter.choice.main.agent")(messages))

  /**
   * Read the key from application.conf file and return the key value pair
   *
   * @param x
   * @param y
   * @return String
   */
  private def createMapEntry(x : String, y : String)(messages: Messages) =
    x -> messages(y)
}

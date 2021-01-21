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

package iht.constants

import iht.config.AppConfig
import iht.constants.Constants._
import play.api.i18n.Messages

import scala.collection.immutable.ListMap

object FieldMappings {

  val Yes="1"
  val No="0"

  def applicantCountryMap(implicit messages: Messages, appConfig: AppConfig) = ListMap(
    createMapEntry(appConfig.applicantCountryEnglandOrWales, "iht.countries.englandOrWales")(messages),
    createMapEntry(appConfig.applicantCountryScotland, "iht.countries.scotland")(messages),
    createMapEntry(appConfig.applicantCountryNorthernIreland, "iht.countries.northernIreland")(messages)
  )

  def domicileMap(implicit messages: Messages, appConfig: AppConfig) = ListMap(
    createMapEntry(appConfig.domicileEnglandOrWales, "iht.countries.englandOrWales")(messages),
    createMapEntry(appConfig.domicileScotland, "iht.countries.scotland")(messages),
    createMapEntry(appConfig.domicileNorthernIreland, "iht.countries.northernIreland")(messages),
    createMapEntry(appConfig.domicileOther, "iht.common.other")(messages)
  )

  def maritalStatusMap (implicit messages: Messages, appConfig: AppConfig) = ListMap(
    createMapEntry(appConfig.statusMarried,"page.iht.registration.deceasedDetails.maritalStatus.civilPartnership.label")(messages),
    createMapEntry(appConfig.statusDivorced,"page.iht.registration.deceasedDetails.maritalStatus.civilPartner.label")(messages),
    createMapEntry(appConfig.statusWidowed,"page.iht.registration.deceasedDetails.maritalStatus.widowed.label")(messages),
    createMapEntry(appConfig.statusSingle,"page.iht.registration.deceasedDetails.maritalStatus.single.label")(messages)
  )

  def propertyType(implicit messages: Messages, appConfig: AppConfig): ListMap[String, String] = ListMap(
    createMapEntry(appConfig.propertyTypeDeceasedHome , "page.iht.application.assets.propertyType.deceasedHome.label")(messages),
    createMapEntry(appConfig.propertyTypeOtherResidentialBuilding , "page.iht.application.assets.propertyType.otherResidential.label")(messages),
    createMapEntry(appConfig.propertyTypeNonResidential , "page.iht.application.assets.propertyType.nonResidential.label")(messages)
  )

  def typesOfOwnership(deceasedName: String)(implicit messages: Messages, appConfig: AppConfig): ListMap[String, (String, Some[String], Some[Boolean])] = ListMap(
    appConfig.ownershipDeceasedOnly -> ((messages("page.iht.application.assets.typeOfOwnership.deceasedOnly.label", deceasedName),
      Some(messages("page.iht.application.assets.typeOfOwnership.deceasedOnly.hint", deceasedName)), Some(false))),
    appConfig.ownershipJoint -> ((messages("page.iht.application.assets.typeOfOwnership.joint.label"),
      Some(messages("page.iht.application.assets.typeOfOwnership.joint.hint", deceasedName)), Some(true))),
    appConfig.ownershipInCommon -> ((messages("page.iht.application.assets.typeOfOwnership.inCommon.label"),
      Some(messages("page.iht.application.assets.typeOfOwnership.inCommon.hint", deceasedName)), Some(true)))
  )

  def tenures(deceasedName: String)(implicit messages: Messages, appConfig: AppConfig) = ListMap(
    appConfig.tenureFreehold -> ((messages("page.iht.application.assets.tenure.freehold.label"),
      Some(messages("page.iht.application.assets.tenure.freehold.hint", deceasedName)), Some(false))),
    appConfig.tenureLeasehold -> ((messages("page.iht.application.assets.tenure.leasehold.label"),
      Some(messages("page.iht.application.assets.tenure.leasehold.hint", deceasedName)), Some(false)))
  )

  def questionnaireEasyToUse (implicit messages: Messages, appConfig: AppConfig) = ListMap(
    createMapEntry(appConfig.questionnaireEasyToUseVeryEasy,"page.iht.questionnaire.easy-to-use.very-easy")(messages),
    createMapEntry(appConfig.questionnaireEasyToUseEasy,"page.iht.questionnaire.easy-to-use.easy")(messages),
    createMapEntry(appConfig.questionnaireEasyToUseNeither,"page.iht.questionnaire.easy-to-use.neither")(messages),
    createMapEntry(appConfig.questionnaireEasyToUseDifficult,"page.iht.questionnaire.easy-to-use.difficult")(messages),
    createMapEntry(appConfig.questionnaireEasyToUseVeryDifficult,"page.iht.questionnaire.easy-to-use.very-difficult")(messages)
  )

  def questionnaireFeelingAboutExperience(implicit messages: Messages, appConfig: AppConfig) = ListMap(
    createMapEntry(appConfig.questionnaireFeelingAboutExperienceVerySatisfied,"page.iht.questionnaire.feelingAboutExperience.verySatisfied")(messages),
    createMapEntry(appConfig.questionnaireFeelingAboutExperienceSatisfied,"page.iht.questionnaire.feelingAboutExperience.satisfied")(messages),
    createMapEntry(appConfig.questionnaireFeelingAboutExperienceNeither,"page.iht.questionnaire.feelingAboutExperience.neither")(messages),
    createMapEntry(appConfig.questionnaireFeelingAboutExperienceDissatisfied,"page.iht.questionnaire.feelingAboutExperience.dissatisfied")(messages),
    createMapEntry(appConfig.questionnaireFeelingAboutExperienceVeryDissatisfied,"page.iht.questionnaire.feelingAboutExperience.veryDissatisfied")(messages)
  )

  def questionnaireActivity(implicit messages: Messages, appConfig: AppConfig) = ListMap(
    createMapEntry(appConfig.questionnaireActivityRegister, "page.iht.questionnaire.activity.register")(messages),
    createMapEntry(appConfig.questionnaireActivityEstateReport, "page.iht.questionnaire.activity.estateReport")(messages),
    createMapEntry(appConfig.questionnaireActivityDeclareApp, "page.iht.questionnaire.activity.declare-app")(messages)
  )

  def filterChoices(implicit messages: Messages): ListMap[String, (String, Option[String], None.type)] = ListMap(
    register -> Tuple3(messages("page.iht.filter.filter.choice.main.register"), Some(messages("page.iht.filter.filter.choice.main.register.hint")), None),
    alreadyStarted -> Tuple3(messages("page.iht.filter.filter.choice.main.alreadyStarted"), None, None),
    continueEstateReport -> Tuple3(messages("page.iht.filter.filter.choice.main.continue"), None, None),
    agent -> Tuple3(messages("page.iht.filter.filter.choice.main.agent"), None, None))

  def domicileChoices(implicit messages: Messages) =
    ListMap(
      createMapEntry(englandOrWales, "iht.countries.englandOrWales")(messages),
      createMapEntry(scotland, "iht.countries.scotland")(messages),
      createMapEntry(northernIreland,  "iht.countries.northernIreland")(messages),
      createMapEntry(otherCountry, "page.iht.filter.domicile.choice.other")(messages)
    )

  def filterJointlyOwnedChoices(implicit messages: Messages) = ListMap(
    createMapEntry(filterJointlyOwnedYes, "page.iht.filter.jointlyowned.yes")(messages),
    createMapEntry(filterJointlyOwnedNo, "page.iht.filter.jointlyowned.no")(messages)
  )

  def estimateChoices(implicit messages: Messages) = ListMap(
    createMapEntry(under325000, "page.iht.filter.estimate.choice.under")(messages),
    createMapEntry(between325000and1million, "page.iht.filter.estimate.choice.between")(messages),
    createMapEntry(moreThan1million, "page.iht.filter.estimate.choice.over")(messages)
  )

  def filterChoicesWithoutHints(implicit messages: Messages) = ListMap(
    createMapEntry(continueEstateReport, "page.iht.filter.filter.choice.main.continue")(messages),
    createMapEntry(register, "page.iht.filter.filter.choice.main.register")(messages),
    createMapEntry(alreadyStarted, "page.iht.filter.filter.choice.main.alreadyStarted")(messages),
    createMapEntry(agent, "page.iht.filter.filter.choice.main.agent")(messages))

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

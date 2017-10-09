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

import iht.constants.IhtProperties._
import play.api.i18n.Messages
import iht.constants.Constants._
import play.api.i18n.Messages.Implicits._
import play.api.Play.current
import play.api.mvc.Request
import scala.collection.immutable.ListMap

object FieldMappings {

  val Yes="1"
  val No="0"

  def applicantCountryMap(implicit messages: Messages) = ListMap(
    createMapEntry(applicantCountryEnglandOrWales, "iht.countries.englandOrWales")(messages),
    createMapEntry(applicantCountryScotland, "iht.countries.scotland")(messages),
    createMapEntry(applicantCountryNorthernIreland, "iht.countries.northernIreland")(messages)
  )

  def domicileMap(implicit messages: Messages) = ListMap(
    createMapEntry(domicileEnglandOrWales, "iht.countries.englandOrWales")(messages),
    createMapEntry(domicileScotland, "iht.countries.scotland")(messages),
    createMapEntry(domicileNorthernIreland, "iht.countries.northernIreland")(messages),
    createMapEntry(domicileOther, "iht.common.other")(messages)
  )

  def maritalStatusMap (implicit messages: Messages) = ListMap(
    createMapEntry(statusMarried,"page.iht.registration.deceasedDetails.maritalStatus.civilPartnership.label")(messages),
    createMapEntry(statusDivorced,"page.iht.registration.deceasedDetails.maritalStatus.civilPartner.label")(messages),
    createMapEntry(statusWidowed,"page.iht.registration.deceasedDetails.maritalStatus.widowed.label")(messages),
    createMapEntry(statusSingle,"page.iht.registration.deceasedDetails.maritalStatus.single.label")(messages)
  )

  def propertyType(implicit messages: Messages): ListMap[String, String] = ListMap(
    createMapEntry(propertyTypeDeceasedHome , "page.iht.application.assets.propertyType.deceasedHome.label")(messages),
    createMapEntry(propertyTypeOtherResidentialBuilding , "page.iht.application.assets.propertyType.otherResidential.label")(messages),
    createMapEntry(propertyTypeNonResidential , "page.iht.application.assets.propertyType.nonResidential.label")(messages)
  )

  def typesOfOwnership(deceasedName: String)(implicit messages: Messages): ListMap[String, (String, Some[String], Some[Boolean])] = ListMap(
    ownershipDeceasedOnly -> ((messages("page.iht.application.assets.typeOfOwnership.deceasedOnly.label", deceasedName),
      Some(messages("page.iht.application.assets.typeOfOwnership.deceasedOnly.hint", deceasedName)), Some(false))),
    ownershipJoint -> ((messages("page.iht.application.assets.typeOfOwnership.joint.label"),
      Some(messages("page.iht.application.assets.typeOfOwnership.joint.hint", deceasedName)), Some(true))),
    ownershipInCommon -> ((messages("page.iht.application.assets.typeOfOwnership.inCommon.label"),
      Some(messages("page.iht.application.assets.typeOfOwnership.inCommon.hint", deceasedName)), Some(true)))
  )

  def tenures(deceasedName: String)(implicit messages: Messages) = ListMap(
    tenureFreehold -> ((messages("page.iht.application.assets.tenure.freehold.label"),
      Some(messages("page.iht.application.assets.tenure.freehold.hint", deceasedName)), Some(false))),
    tenureLeasehold -> ((messages("page.iht.application.assets.tenure.leasehold.label"),
      Some(messages("page.iht.application.assets.tenure.leasehold.hint", deceasedName)), Some(false)))
  )

  def questionnaireEasyToUse (implicit messages: Messages) = ListMap(
    createMapEntry(questionnaireEasyToUseVeryEasy,"page.iht.questionnaire.easy-to-use.very-easy")(messages),
    createMapEntry(questionnaireEasyToUseEasy,"page.iht.questionnaire.easy-to-use.easy")(messages),
    createMapEntry(questionnaireEasyToUseNeither,"page.iht.questionnaire.easy-to-use.neither")(messages),
    createMapEntry(questionnaireEasyToUseDifficult,"page.iht.questionnaire.easy-to-use.difficult")(messages),
    createMapEntry(questionnaireEasyToUseVeryDifficult,"page.iht.questionnaire.easy-to-use.very-difficult")(messages)
  )

  def questionnaireFeelingAboutExperience(implicit messages: Messages) = ListMap(
    createMapEntry(questionnaireFeelingAboutExperienceVerySatisfied,"page.iht.questionnaire.feelingAboutExperience.verySatisfied")(messages),
    createMapEntry(questionnaireFeelingAboutExperienceSatisfied,"page.iht.questionnaire.feelingAboutExperience.satisfied")(messages),
    createMapEntry(questionnaireFeelingAboutExperienceNeither,"page.iht.questionnaire.feelingAboutExperience.neither")(messages),
    createMapEntry(questionnaireFeelingAboutExperienceDissatisfied,"page.iht.questionnaire.feelingAboutExperience.dissatisfied")(messages),
    createMapEntry(questionnaireFeelingAboutExperienceVeryDissatisfied,"page.iht.questionnaire.feelingAboutExperience.veryDissatisfied")(messages)
  )

  def questionnaireActivity(implicit messages: Messages) = ListMap(
    createMapEntry(questionnaireActivityRegister, "page.iht.questionnaire.activity.register")(messages),
    createMapEntry(questionnaireActivityEstateReport, "page.iht.questionnaire.activity.estateReport")(messages),
    createMapEntry(questionnaireActivityDeclareApp, "page.iht.questionnaire.activity.declare-app")(messages)
  )

  def filterChoices(implicit messages: Messages) = ListMap(
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

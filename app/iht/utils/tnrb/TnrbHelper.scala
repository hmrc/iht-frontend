/*
 * Copyright 2018 HM Revenue & Customs
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

import iht.constants.{Constants, IhtProperties}
import iht.models.RegistrationDetails
import iht.models.application.ApplicationDetails
import iht.models.application.tnrb.{TnrbEligibiltyModel, WidowCheck}
import iht.utils.CommonHelper
import iht.views.html._
import org.joda.time.LocalDate
import play.api.i18n.Messages
import play.api.mvc.Results.Redirect
import play.api.mvc.{Call, Result}
import org.apache.commons.lang3.StringEscapeUtils

/**
  * Created by vineet on 27/04/16.
  */
object TnrbHelper {

  val tnrbOverviewPage = iht.controllers.application.tnrb.routes.TnrbOverviewController.onPageLoad()
  val deceasedWidowCheckDatePage = iht.controllers.application.tnrb.routes.DeceasedWidowCheckDateController.onPageLoad()
  val deceasedWidowCheckQuestionPage = iht.controllers.application.tnrb.routes.DeceasedWidowCheckQuestionController.onPageLoad()

  def previousSpouseOrCivilPartner(optionTnrbModel: Option[TnrbEligibiltyModel],
                                   optionWidowCheck: Option[WidowCheck],
                                   deceasedName: String)(implicit messages: Messages): String = {

    val predeceasedName = optionTnrbModel.map(_.Name.toString.trim).fold("")(identity)
    if (predeceasedName.nonEmpty) {
      StringEscapeUtils.escapeHtml4(predeceasedName)
    } else {
      spouseOrCivilPartnerMessageText(
        "page.iht.application.tnrb.kickout.previousSpouse",
        "page.iht.application.tnrb.kickout.previousSpouseOrCivilPartner",
        optionWidowCheck.flatMap(_.dateOfPreDeceased),
        deceasedName
      )(messages)
    }
  }

  /**
    * This produces content like: <prefix>'s spouse or civil partner, i.e. genitive grammatical case.
    */
  def spouseOrCivilPartnerLabelGenitive(tnrbModel: TnrbEligibiltyModel,
                                        widowCheck: WidowCheck,
                                        prefixText: String = "",
                                        wrapName: Boolean = false)(implicit messages: Messages): String = {
    if (tnrbModel.Name.toString.trim != "") {
      if (wrapName) {
        ihtHelpers.custom.name(tnrbModel.Name.toString).toString
      } else {
        StringEscapeUtils.escapeHtml4(tnrbModel.Name.toString)
      }
    } else {
      messages("page.iht.application.TnrbEligibilty.spouseOrCivilPartner.ofPerson",
        prefixText, messages(spouseOrCivilPartnerMessage(widowCheck.dateOfPreDeceased)(messages)))
    }
  }

  /**
    * This produces content like: <prefix> (e.g. their) spouse or civil partner, i.e. possessive.
    */
  def spouseOrCivilPartnerLabelPossessive(tnrbModel: TnrbEligibiltyModel,
                                          widowCheck: WidowCheck,
                                          prefixText: String = "",
                                          wrapName: Boolean = false)(implicit messages: Messages): String = {
    if (tnrbModel.Name.toString.trim != "") {
      val and = messages(vowelConsciousAnd(tnrbModel.Name.toString, messages.lang.code))
      if (wrapName) {
        and + " " + ihtHelpers.custom.name(tnrbModel.Name.toString).toString
      } else {
        and + " " + StringEscapeUtils.escapeHtml4(tnrbModel.Name.toString)
      }
    } else {
      prefixText + " " + spouseOrCivilPartnerMessage(widowCheck.dateOfPreDeceased)(messages)
    }
  }

  /**
    * Returns the Spouse name (if exists) otherwise returns the pretext string
    */
  def spouseOrCivilPartnerName(tnrbModel: TnrbEligibiltyModel,
                               prefixText: String = "",
                               wrapName: Boolean = true): String = {
    CommonHelper.withValue(tnrbModel.Name.toString.trim) {
      case name if name.isEmpty => prefixText
      case name => if (wrapName) {
        ihtHelpers.custom.name(name).toString
      } else {
        StringEscapeUtils.escapeHtml4(name)
      }
    }
  }


  def marriageOrCivilPartnerShipLabel(widowCheck: WidowCheck)(implicit messages: Messages): String =
    spouseOrCivilPartnerMessageText(messagesKeySpouse = "page.iht.application.tnrbEligibilty.partner.marriage.label",
      messagesKeyPartner = "page.iht.application.tnrbEligibilty.partner.marriageOrCivilPartnership.label",
      dateOfPreDeceased = widowCheck.dateOfPreDeceased)(messages)

  def spouseOrCivilPartnerNameLabel(tnrbModel: TnrbEligibiltyModel,
                                    widowCheck: WidowCheck,
                                    prefixText: String = "")(implicit messages: Messages): String = {
    if (tnrbModel.Name.toString.trim != "") {
      messages("iht.name.upperCaseInitial")
    } else {
      prefixText + " " + messages(spouseOrCivilPartnerMessage(widowCheck.dateOfPreDeceased)(messages))
    }
  }

  def preDeceasedMaritalStatusLabel(tnrbModel: TnrbEligibiltyModel,
                                    widowCheck: WidowCheck)(implicit messages: Messages): String = {
    if (tnrbModel.Name.toString.trim != "") {
      StringEscapeUtils.escapeHtml4(tnrbModel.Name.toString) + " " + messages("page.iht.application.tnrbEligibilty.partner.married.label")
    } else {
      messages("iht.the.deceased") + " " +
        messages(preDeceasedMaritalStatusSubLabel(widowCheck.dateOfPreDeceased)(messages))
    }
  }

  def spouseOrCivilPartnerMessage(dateOfPreDeceased: Option[LocalDate])(implicit messages: Messages): String =
    spouseOrCivilPartnerMessageText(messagesKeySpouse = "page.iht.application.TnrbEligibilty.spouse.commonText",
      messagesKeyPartner = "page.iht.application.TnrbEligibilty.spouseOrCivilPartner.commonText",
      dateOfPreDeceased = dateOfPreDeceased)(messages)

  def preDeceasedMaritalStatusSubLabel(dateOfPreDeceased: Option[LocalDate])(implicit messages: Messages): String =
    spouseOrCivilPartnerMessageText(messagesKeySpouse = "page.iht.application.tnrbEligibilty.partner.married.label",
      messagesKeyPartner = "page.iht.application.tnrbEligibilty.partner.marriedOrCivilPartnership.label",
      dateOfPreDeceased = dateOfPreDeceased)(messages)

  def marriageOrCivilPartnerShipLabelForPdf(date: Option[LocalDate])(implicit messages: Messages): String =
    spouseOrCivilPartnerMessageText(messagesKeySpouse = "page.iht.application.tnrbEligibilty.partner.marriage.label",
      messagesKeyPartner = "page.iht.application.tnrbEligibilty.partner.marriageOrCivilPartnership.label",
      dateOfPreDeceased = date)(messages)

  def successfulTnrbRedirect(appDetails: ApplicationDetails, linkHash: Option[String] = None): Result = {
    if (appDetails.isSuccessfulTnrbCase) {
      Redirect(iht.controllers.application.tnrb.routes.TnrbSuccessController.onPageLoad())
    } else {
      Redirect(CommonHelper.addFragmentIdentifier(iht.controllers.application.tnrb.routes.TnrbOverviewController.onPageLoad(), linkHash))
    }
  }

  def cancelLinkUrlForWidowCheckPages(appDetails: ApplicationDetails, linkHash: Option[String] = None) = if (appDetails.isWidowCheckSectionCompleted) {
    CommonHelper.addFragmentIdentifier(iht.controllers.application.tnrb.routes.TnrbOverviewController.onPageLoad, linkHash)
  } else {
    iht.controllers.application.routes.EstateOverviewController.onPageLoadWithIhtRef(CommonHelper.getOrException(appDetails.ihtRef))
  }

  def cancelLinkTextForWidowCheckPages(appDetails: ApplicationDetails)(implicit messages: Messages) = if (appDetails.isWidowCheckSectionCompleted) {
    messages("page.iht.application.tnrb.returnToIncreasingThreshold")
  } else {
    messages("iht.estateReport.returnToEstateOverview")
  }

  private def spouseOrCivilPartnerMessageText(messagesKeySpouse: String,
                                              messagesKeyPartner: String,
                                              dateOfPreDeceased: Option[LocalDate],
                                              params: String*
                                             )(implicit messages: Messages) = {
    val key = dateOfPreDeceased match {
      case Some(date) if isBeforeCivilPartnershipDate(date) => messagesKeySpouse
      case _ => messagesKeyPartner
    }
    messages(key, params: _*)
  }

  /*
    Due to the welsh grammatical rule of "consonant soft mutation" the word
    "priod" ("marriage") changes to "briod" when preceded by the word "gan".
 */
  def mutateContent(s:String, language:String) = {
    if (language == "cy") {
      s.replace(Constants.contentMutation._1, Constants.contentMutation._2)
    } else {
      s
    }
  }

  def vowelConsciousAnd(predeceasedName: String, language:String) = {
    val firstLetterOfPDName = predeceasedName.trim.toLowerCase.charAt(0)
    if(language == "en") {
      "page.iht.application.tnrbEligibilty.partner.additional.label.and"
    } else {
      if(Constants.welshVowels.contains(firstLetterOfPDName)) {
        "page.iht.application.tnrbEligibilty.partner.additional.label.andAfterVowel"
      } else {
        "page.iht.application.tnrbEligibilty.partner.additional.label.andAfterConsonant"
      }
    }
  }

  private def isBeforeCivilPartnershipDate(dateOfPreDeceased: LocalDate): Boolean = {
    val civilPartnerDate = IhtProperties.dateOfCivilPartnershipInclusion
    dateOfPreDeceased.isBefore(civilPartnerDate)
  }

  /**
    * Retrieves the Tnrb flow URL as per User interaction/inputs given.
    *
    * @param rd
    * @param ad
    * @return
    */
  def getEntryPointForTnrb(rd: RegistrationDetails,
                           ad: ApplicationDetails) = {
    ad.isWidowCheckSectionCompleted match {
      case true => tnrbOverviewPage
      case _ => urlForIncreasingThreshold(CommonHelper.getOrException(rd.deceasedDetails.flatMap(_.maritalStatus)))
    }
  }

  /**
    * Partial function, which will throw an exception if Marital Status value is not in range
    */
  val urlForIncreasingThreshold: PartialFunction[String, Call] = {
    case IhtProperties.statusWidowed => deceasedWidowCheckDatePage
    case maritalStatus if maritalStatus == IhtProperties.statusMarried || maritalStatus == IhtProperties.statusDivorced =>
      deceasedWidowCheckQuestionPage
  }

}

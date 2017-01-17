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

package iht.utils.tnrb

import iht.constants.IhtProperties
import iht.models.application.ApplicationDetails
import iht.models.application.tnrb.{WidowCheck, TnrbEligibiltyModel}
import iht.models.RegistrationDetails
import iht.utils.CommonHelper
import org.joda.time.LocalDate
import play.api.i18n.Messages
import play.api.mvc.Results.Redirect
import play.api.mvc.{Call, Result}
import iht.views.html._


/**
  * Created by vineet on 27/04/16.
  */
object TnrbHelper {

  val tnrbOverviewPage= iht.controllers.application.tnrb.routes.TnrbOverviewController.onPageLoad()
  val deceasedWidowCheckDatePage= iht.controllers.application.tnrb.routes.DeceasedWidowCheckDateController.onPageLoad()
  val deceasedWidowCheckQuestionPage = iht.controllers.application.tnrb.routes.DeceasedWidowCheckQuestionController.onPageLoad()

  def spouseOrCivilPartnerLabelWithOptions(optionTnrbModel: Option[TnrbEligibiltyModel],
                                optionWidowCheck: Option[WidowCheck],
                                optionPrefixText: Option[String]=None): String  = {
    optionTnrbModel.flatMap{tnrbModel=>
      val name = tnrbModel.Name.toString.trim
      if(name.length==0) None else Some(name)
    }.fold{
      val dateOfPreDeceased = optionWidowCheck.flatMap(_.dateOfPreDeceased)
      optionPrefixText.fold("")(identity) + " " + spouseOrCivilPartnerMessage(dateOfPreDeceased)
    }(identity)
  }

  def spouseOrCivilPartnerLabel(tnrbModel: TnrbEligibiltyModel,
                                widowCheck: WidowCheck,
                                prefixText: String="",
                                wrapName: Boolean = false): String  = {
    if(tnrbModel.Name.toString.trim!=""){
      if(wrapName) {
        ihtHelpers.name(tnrbModel.Name.toString).toString
      }else{
        tnrbModel.Name.toString
      }
    } else {
      prefixText + " " + spouseOrCivilPartnerMessage(widowCheck.dateOfPreDeceased)
    }
  }

  /**
    * Returns the Spouse name (if exists) otherwise returns the pretext string
    */
  def spouseOrCivilPartnerName(tnrbModel: TnrbEligibiltyModel,
                                prefixText: String="",
                               wrapName: Boolean = true): String  = {
    CommonHelper.withValue(tnrbModel.Name.toString.trim) {
      case name if name.isEmpty => prefixText
      case name => if(wrapName) {ihtHelpers.name(name).toString}else{name}
    }
  }


  def marriageOrCivilPartnerShipLabel(widowCheck: WidowCheck): String =
    spouseOrCivilPartnerMessageText(messagesKeySpouse = "page.iht.application.tnrbEligibilty.partner.marriage.label",
      messagesKeyPartner = "page.iht.application.tnrbEligibilty.partner.marriageOrCivilPartnership.label",
      dateOfPreDeceased = widowCheck.dateOfPreDeceased)

  def spouseOrCivilPartnerNameLabel(tnrbModel: TnrbEligibiltyModel,
                                    widowCheck: WidowCheck,
                                    prefixText: String=""): String={
    if(tnrbModel.Name.toString.trim!=""){
      Messages("iht.name.upperCaseInitial")
    } else {
      prefixText + " " + spouseOrCivilPartnerMessage(widowCheck.dateOfPreDeceased)
    }
  }

  def preDeceasedMaritalStatusLabel(tnrbModel: TnrbEligibiltyModel,
                                    widowCheck: WidowCheck): String = {
    if(tnrbModel.Name.toString.trim!=""){
      tnrbModel.Name.toString + " " + Messages("page.iht.application.tnrbEligibilty.partner.married.label")
    } else {
      Messages("iht.the.deceased") + " " +
        preDeceasedMaritalStatusSubLabel(widowCheck.dateOfPreDeceased)
    }
  }

  def spouseOrCivilPartnerMessage(dateOfPreDeceased: Option[LocalDate]): String =
    spouseOrCivilPartnerMessageText(messagesKeySpouse = "page.iht.application.TnrbEligibilty.spouse.commonText",
    messagesKeyPartner = "page.iht.application.TnrbEligibilty.spouseOrCivilPartner.commonText",
    dateOfPreDeceased = dateOfPreDeceased)

  def preDeceasedMaritalStatusSubLabel(dateOfPreDeceased: Option[LocalDate]): String =
    spouseOrCivilPartnerMessageText(messagesKeySpouse = "page.iht.application.tnrbEligibilty.partner.married.label",
      messagesKeyPartner = "page.iht.application.tnrbEligibilty.partner.marriedOrCivilPartnership.label",
      dateOfPreDeceased = dateOfPreDeceased)

  def marriageOrCivilPartnerShipLabelForPdf(date: Option[LocalDate]): String =
    spouseOrCivilPartnerMessageText(messagesKeySpouse = "page.iht.application.tnrbEligibilty.partner.marriage.label",
      messagesKeyPartner = "page.iht.application.tnrbEligibilty.partner.marriageOrCivilPartnership.label",
      dateOfPreDeceased = date)

  def successfulTnrbRedirect(appDetails: ApplicationDetails): Result = {
    if(appDetails.isSuccessfulTnrbCase) {
      Redirect(iht.controllers.application.tnrb.routes.TnrbSuccessController.onPageLoad())
    } else {
      Redirect(iht.controllers.application.tnrb.routes.TnrbOverviewController.onPageLoad())
    }
  }

  def cancelLinkUrlForWidowCheckPages(appDetails: ApplicationDetails) = if(appDetails.isWidowCheckSectionCompleted) {
    iht.controllers.application.tnrb.routes.TnrbOverviewController.onPageLoad
  }else {
    iht.controllers.application.routes.EstateOverviewController.onPageLoadWithIhtRef(CommonHelper.getOrException(appDetails.ihtRef))
  }

  def cancelLinkTextForWidowCheckPages(appDetails: ApplicationDetails) = if(appDetails.isWidowCheckSectionCompleted) {
    Messages("page.iht.application.tnrb.returnToIncreasingThreshold")
  }else {
    Messages("iht.estateReport.returnToEstateOverview")
  }

  private def spouseOrCivilPartnerMessageText(messagesKeySpouse:String,
                                              messagesKeyPartner:String,
                                              dateOfPreDeceased: Option[LocalDate]) =
  {
    val key = dateOfPreDeceased match {
      case Some(date) if isBeforeCivilPartnershipDate(date) => messagesKeySpouse
      case _ => messagesKeyPartner
    }
    Messages(key)
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
      case maritalStatus if maritalStatus == IhtProperties.statusMarried || maritalStatus ==IhtProperties.statusDivorced =>
        deceasedWidowCheckQuestionPage
  }

}

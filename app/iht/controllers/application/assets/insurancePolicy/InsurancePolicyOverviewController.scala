/*
 * Copyright 2022 HM Revenue & Customs
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

package iht.controllers.application.assets.insurancePolicy

import iht.config.AppConfig
import iht.connector.{CachingConnector, IhtConnector}
import iht.controllers.application.EstateController
import iht.metrics.IhtMetrics
import iht.models.RegistrationDetails
import iht.models.application.ApplicationDetails
import iht.models.application.assets._
import iht.utils.CommonHelper._
import iht.utils.DeceasedInfoHelper
import iht.utils.OverviewHelper._
import javax.inject.Inject
import play.api.i18n.Messages
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.auth.core.AuthConnector
import uk.gov.hmrc.auth.core.retrieve.v2.Retrievals.{nino => ninoRetrieval}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController
import iht.views.html.application.asset.insurancePolicy.insurance_policies_overview

import scala.concurrent.Future

class InsurancePolicyOverviewControllerImpl @Inject()(val metrics: IhtMetrics,
                                                      val ihtConnector: IhtConnector,
                                                      val cachingConnector: CachingConnector,
                                                      val authConnector: AuthConnector,
                                                      val insurancePoliciesOverviewView: insurance_policies_overview,
                                                      implicit val appConfig: AppConfig,
                                                      val cc: MessagesControllerComponents) extends FrontendController(cc) with InsurancePolicyOverviewController

trait InsurancePolicyOverviewController extends EstateController {


  private val q1: ApplicationDetails => Option[Boolean] = ad => ad.allAssets.flatMap(_.insurancePolicy).flatMap(_.isInsurancePremiumsPayedForSomeoneElse)
  private val q2: ApplicationDetails => Option[Boolean] = ad => ad.allAssets.flatMap(_.insurancePolicy).flatMap(_.moreThanMaxValue)
  private val q3: ApplicationDetails => Option[Boolean] = ad => ad.allAssets.flatMap(_.insurancePolicy).flatMap(_.isAnnuitiesBought)

  private val displayQ1: ApplicationDetails => Boolean = ad => q1(ad).fold(false)(_ => true)
  private val displayQ2: ApplicationDetails => Boolean = ad => q1(ad).fold(false)(identity)
  private val displayQ3: ApplicationDetails => Boolean = ad => q2(ad).fold(false)(_ => true)
  private val displayQ4: ApplicationDetails => Boolean = ad => q3(ad).fold(false)(_ => true)

  val insurancePoliciesOverviewView: insurance_policies_overview
  private def section3YesNoItems(insurancePolicy: InsurancePolicy, rd: RegistrationDetails)(implicit messages: Messages) = {
    val maritalStatusKey = mapMaritalStatus(rd)
    Seq[QuestionAnswer](
      QuestionAnswer(insurancePolicy.isInsurancePremiumsPayedForSomeoneElse, routes.InsurancePolicyDetailsPayingOtherController.onPageLoad,
        ad => displayQ1(ad),
        if (maritalStatusKey == "married") {
          Messages("page.iht.application.assets.insurance.policies.overview.other.question1.yes.screenReader.link.value.married",
            DeceasedInfoHelper.getDeceasedNameOrDefaultString(rd))
        } else {
          Messages("page.iht.application.assets.insurance.policies.overview.other.question1.yes.screenReader.link.value.notMarried",
            DeceasedInfoHelper.getDeceasedNameOrDefaultString(rd))
        },
        if (maritalStatusKey == "married") {
          Messages("page.iht.application.assets.insurance.policies.overview.other.question1.no.screenReader.link.value.married",
            DeceasedInfoHelper.getDeceasedNameOrDefaultString(rd))
        } else {
          Messages("page.iht.application.assets.insurance.policies.overview.other.question1.no.screenReader.link.value.notMarried",
            DeceasedInfoHelper.getDeceasedNameOrDefaultString(rd))
        },
        if (maritalStatusKey == "married") {
          Messages("page.iht.application.assets.insurance.policies.overview.other.question1.none.screenReader.link.value.married",
            DeceasedInfoHelper.getDeceasedNameOrDefaultString(rd))
        } else {
          "page.iht.application.assets.insurance.policies.overview.other.question1.none.screenReader.link.value.notMarried"
        }),
      QuestionAnswer(insurancePolicy.moreThanMaxValue, routes.InsurancePolicyDetailsMoreThanMaxValueController.onPageLoad,
        ad => displayQ2(ad),
        Messages("page.iht.application.assets.insurance.policies.overview.other.question2.yes.screenReader.link.value",
          DeceasedInfoHelper.getDeceasedNameOrDefaultString(rd)),
        Messages("page.iht.application.assets.insurance.policies.overview.other.question2.no.screenReader.link.value",
          DeceasedInfoHelper.getDeceasedNameOrDefaultString(rd)),
        Messages("page.iht.application.assets.insurance.policies.overview.other.question2.none.screenReader.link.value",
          DeceasedInfoHelper.getDeceasedNameOrDefaultString(rd))),
      QuestionAnswer(insurancePolicy.isAnnuitiesBought, routes.InsurancePolicyDetailsAnnuityController.onPageLoad,
        ad => displayQ3(ad),
        Messages("page.iht.application.assets.insurance.policies.overview.other.question3.yes.screenReader.link.value",
          DeceasedInfoHelper.getDeceasedNameOrDefaultString(rd)),
        Messages("page.iht.application.assets.insurance.policies.overview.other.question3.no.screenReader.link.value",
          DeceasedInfoHelper.getDeceasedNameOrDefaultString(rd)),
        Messages("page.iht.application.assets.insurance.policies.overview.other.question3.none.screenReader.link.value",
          DeceasedInfoHelper.getDeceasedNameOrDefaultString(rd))),
      QuestionAnswer(insurancePolicy.isInTrust, routes.InsurancePolicyDetailsInTrustController.onPageLoad,
        ad => displayQ4(ad),
        Messages("page.iht.application.assets.insurance.policies.overview.other.question4.yes.screenReader.link.value",
          DeceasedInfoHelper.getDeceasedNameOrDefaultString(rd)),
        Messages("page.iht.application.assets.insurance.policies.overview.other.question4.no.screenReader.link.value",
          DeceasedInfoHelper.getDeceasedNameOrDefaultString(rd)),
        Messages("page.iht.application.assets.insurance.policies.overview.other.question4.none.screenReader.link.value",
          DeceasedInfoHelper.getDeceasedNameOrDefaultString(rd)))
    )
  }

  private def createSection1(regDetails: RegistrationDetails, insurancePolicy: InsurancePolicy)(implicit messages: Messages): Section = {
    createSectionFromYesNoValueQuestions(
      id = "deceased",
      title = Some(messages("iht.estateReport.assets.insurancePolicies.payingOutToDeceased",
        DeceasedInfoHelper.getDeceasedNameOrDefaultString(regDetails, true))),
      linkUrl = routes.InsurancePolicyDetailsDeceasedOwnController.onPageLoad,
      sectionLevelLinkAccessibilityText = messages(
        "page.iht.application.assets.insurance.policies.overview.deceased.giveAnswer.screenReader.link.value",
        DeceasedInfoHelper.getDeceasedNameOrDefaultString(regDetails)),
      questionLevelLinkAccessibilityTextYes = messages(
        "page.iht.application.assets.insurance.policies.overview.deceased.yes.screenReader.link.value",
        DeceasedInfoHelper.getDeceasedNameOrDefaultString(regDetails)),
      questionLevelLinkAccessibilityTextNo = messages(
        "page.iht.application.assets.insurance.policies.overview.deceased.no.screenReader.link.value",
        DeceasedInfoHelper.getDeceasedNameOrDefaultString(regDetails)),
      questionLevelLinkAccessibilityTextValue =
        if (insurancePolicy.value.isDefined) {
          "page.iht.application.assets.insurance.policies.overview.deceased.amount.screenReader.link.value"
        }
        else {
          "page.iht.application.assets.insurance.policies.overview.deceased.amount.screenReader.link.novalue"
        },
      questionAnswerExprYesNo = insurancePolicy.policyInDeceasedName,
      questionAnswerExprValue = insurancePolicy.value,
      questionTitleYesNoMessage = messages("iht.estateReport.insurancePolicies.ownName.question",
        DeceasedInfoHelper.getDeceasedNameOrDefaultString(regDetails, true)),
      questionTitleValueMessage = messages("iht.estateReport.assets.insurancePolicies.totalValueOwnedAndPayingOut",
        DeceasedInfoHelper.getDeceasedNameOrDefaultString(regDetails)),
      sectionLinkId = appConfig.InsurancePayingToDeceasedSectionID,
      questionLinkID = appConfig.InsurancePayingToDeceasedYesNoID,
      answerLinkID = appConfig.InsurancePayingToDeceasedValueID
    )
  }

  private def createSection2(regDetails: RegistrationDetails, insurancePolicy: InsurancePolicy)(implicit messages: Messages): Section = {
    createSectionFromYesNoValueQuestions(
      id = "joint",
      title = Some(messages("page.iht.application.assets.insurance.policies.overview.joint.title")),
      linkUrl = routes.InsurancePolicyDetailsJointController.onPageLoad,

      sectionLevelLinkAccessibilityText = messages(
        "page.iht.application.assets.insurance.policies.overview.joint.giveAnswer.screenReader.link.value",
        DeceasedInfoHelper.getDeceasedNameOrDefaultString(regDetails)),
      questionLevelLinkAccessibilityTextYes = messages(
        "page.iht.application.assets.insurance.policies.overview.joint.yes.screenReader.link.value",
        DeceasedInfoHelper.getDeceasedNameOrDefaultString(regDetails)),
      questionLevelLinkAccessibilityTextNo = messages(
        "page.iht.application.assets.insurance.policies.overview.joint.no.screenReader.link.value",
        DeceasedInfoHelper.getDeceasedNameOrDefaultString(regDetails)),
      questionLevelLinkAccessibilityTextValue =
        if (insurancePolicy.shareValue.isDefined) {
          "page.iht.application.assets.insurance.policies.overview.joint.amount.screenReader.link.value"
        }
        else {
          "page.iht.application.assets.insurance.policies.overview.joint.amount.screenReader.link.novalue"
        },
      questionAnswerExprYesNo = insurancePolicy.isJointlyOwned,
      questionAnswerExprValue = insurancePolicy.shareValue,
      questionTitleYesNoMessage = messages("iht.estateReport.insurancePolicies.jointlyHeld.question",
        DeceasedInfoHelper.getDeceasedNameOrDefaultString(regDetails, true)),
      questionTitleValueMessage = messages("iht.estateReport.assets.insurancePolicies.totalValueOfDeceasedsShare",
        DeceasedInfoHelper.getDeceasedNameOrDefaultString(regDetails)),
      sectionLinkId = appConfig.InsuranceJointlyHeldSectionID,
      questionLinkID = appConfig.InsuranceJointlyHeldYesNoID,
      answerLinkID = appConfig.InsuranceJointlyHeldValueID
    )
  }

  private def createSection3(regDetails: RegistrationDetails, ad: ApplicationDetails, insurancePolicy: InsurancePolicy)
                            (implicit messages: Messages): Section = {
    createSectionFromYesNoQuestions(
      id = "other",
      title = Some(messages("iht.estateReport.assets.insurancePolicies.premiumsPaidByOther",
        DeceasedInfoHelper.getDeceasedNameOrDefaultString(regDetails, true))),
      linkUrl = routes.InsurancePolicyDetailsPayingOtherController.onPageLoad,
      sectionLevelLinkAccessibilityText = messages("page.iht.application.assets.insurance.policies.overview.other.giveAnswer.screenReader.link.value",
        DeceasedInfoHelper.getDeceasedNameOrDefaultString(regDetails, true)),
      questionAnswersPlusChangeLinks = section3YesNoItems(insurancePolicy, regDetails),
      questionTitlesMessagesFileItems = Seq(
        messages("iht.estateReport.insurancePolicies.premiumsNotPayingOut.question",
          DeceasedInfoHelper.getDeceasedNameOrDefaultString(regDetails, true)),
        messages("iht.estateReport.insurancePolicies.overLimitNotOwnEstate.question",
          DeceasedInfoHelper.getDeceasedNameOrDefaultString(regDetails, true)),
        messages("iht.estateReport.assets.insurancePolicies.buyAnnuity.question",
          DeceasedInfoHelper.getDeceasedNameOrDefaultString(regDetails, true)),
        messages("page.iht.application.assets.insurance.policies.overview.other.question4",
          DeceasedInfoHelper.getDeceasedNameOrDefaultString(regDetails, true))
      ),
      ad,
      regDetails,
      sectionLinkId = appConfig.InsurancePaidForSomeoneElseSectionID,
      questionLinkIds = Seq(
        appConfig.InsurancePaidForSomeoneElseYesNoID,
        appConfig.InsurancePremiumnsYesNoID,
        appConfig.InsuranceAnnuityYesNoID,
        appConfig.InsurancePlacedInTrustYesNoID
      )
    )
  }

  def onPageLoad: Action[AnyContent] = authorisedForIhtWithRetrievals(ninoRetrieval) { userNino =>
    implicit request => {
      withRegistrationDetails { regDetails =>
        val applicationDetailsFuture: Future[Option[ApplicationDetails]] = ihtConnector
          .getApplication(getNino(userNino), getOrExceptionNoIHTRef(regDetails.ihtReference),
            regDetails.acknowledgmentReference)

        applicationDetailsFuture.map { optionApplicationDetails =>
          val ad = getOrExceptionNoApplication(optionApplicationDetails)
          val optionInsurancePolicy: Option[InsurancePolicy] = ad.allAssets.flatMap(allAssets => allAssets.insurancePolicy)
          val insurancePolicy: InsurancePolicy = optionInsurancePolicy
            .fold[InsurancePolicy](new InsurancePolicy(None, None, None, None, None, None, None, None, None, None))(identity)

          val seqSection1 = createSection1(regDetails, insurancePolicy)
          val seqSection2 = createSection2(regDetails, insurancePolicy)
          val seqSection3 = createSection3(regDetails, ad, insurancePolicy)

          Ok(insurancePoliciesOverviewView(regDetails,
            Seq(seqSection1, seqSection2, seqSection3),
            Some(iht.controllers.application.assets.routes.AssetsOverviewController.onPageLoad),
            "page.iht.application.return.to.assetsOf"))
        }
      }
    }
  }
}

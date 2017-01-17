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

package iht.controllers.application.assets.insurancePolicy

import iht.controllers.IhtConnectors
import iht.controllers.application.EstateController
import iht.metrics.Metrics
import iht.models.application.ApplicationDetails
import iht.models.application.assets._
import iht.models.RegistrationDetails
import iht.utils.CommonHelper
import iht.utils.CommonHelper._
import iht.utils.OverviewHelper._
import play.api.i18n.Messages

import scala.concurrent.Future

object InsurancePolicyOverviewController extends InsurancePolicyOverviewController with IhtConnectors {
  def metrics: Metrics = Metrics
}

trait InsurancePolicyOverviewController extends EstateController {
  private val q1: ApplicationDetails => Option[Boolean] = ad => ad.allAssets.flatMap(_.insurancePolicy).flatMap(_.isInsurancePremiumsPayedForSomeoneElse)
  private val q2: ApplicationDetails => Option[Boolean] = ad => ad.allAssets.flatMap(_.insurancePolicy).flatMap(_.moreThanMaxValue)
  private val q3: ApplicationDetails => Option[Boolean] = ad => ad.allAssets.flatMap(_.insurancePolicy).flatMap(_.isAnnuitiesBought)

  private val displayQ1: ApplicationDetails => Boolean = ad=> q1(ad).fold(false)(_=>true)
  private val displayQ2: ApplicationDetails => Boolean = ad=> q1(ad).fold(false)(identity)
  private val displayQ3: ApplicationDetails => Boolean = ad=> q2(ad).fold(false)(_=>true)
  private val displayQ4: ApplicationDetails => Boolean = ad=> q3(ad).fold(false)(_=>true)

  private def section3YesNoItems(insurancePolicy: InsurancePolicy, rd:RegistrationDetails) = {
    val maritalStatusKey = mapMaritalStatus(rd)
    Seq[QuestionAnswer](
      QuestionAnswer(insurancePolicy.isInsurancePremiumsPayedForSomeoneElse, routes.InsurancePolicyDetailsPayingOtherController.onPageLoad(),
        ad=>displayQ1(ad),
        if(maritalStatusKey=="married"){
          "page.iht.application.assets.insurance.policies.overview.other.question1.yes.screenReader.link.value.married"
        } else {
          "page.iht.application.assets.insurance.policies.overview.other.question1.yes.screenReader.link.value.notMarried"
        },
        if(maritalStatusKey=="married"){
          "page.iht.application.assets.insurance.policies.overview.other.question1.no.screenReader.link.value.married"
        } else {
          "page.iht.application.assets.insurance.policies.overview.other.question1.no.screenReader.link.value.notMarried"
        },
        if(maritalStatusKey=="married"){
          "page.iht.application.assets.insurance.policies.overview.other.question1.none.screenReader.link.value.married"
        } else {
          "page.iht.application.assets.insurance.policies.overview.other.question1.none.screenReader.link.value.notMarried"
        }),
      QuestionAnswer(insurancePolicy.moreThanMaxValue, routes.InsurancePolicyDetailsMoreThanMaxValueController.onPageLoad(),
        ad=>displayQ2(ad),
        "page.iht.application.assets.insurance.policies.overview.other.question2.yes.screenReader.link.value",
        "page.iht.application.assets.insurance.policies.overview.other.question2.no.screenReader.link.value",
        "page.iht.application.assets.insurance.policies.overview.other.question2.none.screenReader.link.value"),
      QuestionAnswer(insurancePolicy.isAnnuitiesBought, routes.InsurancePolicyDetailsAnnuityController.onPageLoad(),
        ad=> displayQ3(ad) ,
        "page.iht.application.assets.insurance.policies.overview.other.question3.yes.screenReader.link.value",
        "page.iht.application.assets.insurance.policies.overview.other.question3.no.screenReader.link.value",
        "page.iht.application.assets.insurance.policies.overview.other.question3.none.screenReader.link.value"),
      QuestionAnswer(insurancePolicy.isInTrust, routes.InsurancePolicyDetailsInTrustController.onPageLoad(),
        ad=>displayQ4(ad),
        "page.iht.application.assets.insurance.policies.overview.other.question4.yes.screenReader.link.value",
        "page.iht.application.assets.insurance.policies.overview.other.question4.no.screenReader.link.value",
        "page.iht.application.assets.insurance.policies.overview.other.question4.none.screenReader.link.value")
    )
  }

  def onPageLoad = authorisedForIht {
    implicit user => implicit request => {
      val regDetails = cachingConnector.getExistingRegistrationDetails
      val applicationDetailsFuture: Future[Option[ApplicationDetails]] = ihtConnector
        .getApplication(getNino(user), getOrExceptionNoIHTRef(regDetails.ihtReference),
          regDetails.acknowledgmentReference)

      applicationDetailsFuture.map { optionApplicationDetails =>
        val ad = getOrExceptionNoApplication(optionApplicationDetails)
        val optionInsurancePolicy: Option[InsurancePolicy] = ad.allAssets.flatMap(allAssets => allAssets.insurancePolicy)
        val insurancePolicy: InsurancePolicy = optionInsurancePolicy
          .fold[InsurancePolicy](new InsurancePolicy(None, None, None, None, None, None, None, None, None,None))(identity)

        val seqSection1 = createSectionFromYesNoValueQuestions(
          id = "deceased",
          title = Some(Messages("iht.estateReport.assets.insurancePolicies.payingOutToDeceased",
                                CommonHelper.getDeceasedNameOrDefaultString(regDetails))),
          linkUrl = routes.InsurancePolicyDetailsDeceasedOwnController.onPageLoad(),
          sectionLevelLinkAccessibilityText = "page.iht.application.assets.insurance.policies.overview.deceased.giveAnswer.screenReader.link.value",
          questionLevelLinkAccessibilityTextYes = "page.iht.application.assets.insurance.policies.overview.deceased.yes.screenReader.link.value",
          questionLevelLinkAccessibilityTextNo = "page.iht.application.assets.insurance.policies.overview.deceased.no.screenReader.link.value",
          questionLevelLinkAccessibilityTextValue = "page.iht.application.assets.insurance.policies.overview.deceased.amount.screenReader.link.value",
          questionAnswerExprYesNo = insurancePolicy.policyInDeceasedName,
          questionAnswerExprValue = insurancePolicy.value,
          questionTitleYesNoMessage = Messages("iht.estateReport.insurancePolicies.ownName.question",
                                                CommonHelper.getDeceasedNameOrDefaultString(regDetails)),
          questionTitleValueMessage = Messages("iht.estateReport.assets.insurancePolicies.totalValueOwnedAndPayingOut")
        )
        val seqSection2 = createSectionFromYesNoValueQuestions(
          id = "joint",
          title = Some(Messages("page.iht.application.assets.insurance.policies.overview.joint.title")),
          linkUrl = routes.InsurancePolicyDetailsJointController.onPageLoad(),
          sectionLevelLinkAccessibilityText = "page.iht.application.assets.insurance.policies.overview.joint.giveAnswer.screenReader.link.value",
          questionLevelLinkAccessibilityTextYes = "page.iht.application.assets.insurance.policies.overview.joint.yes.screenReader.link.value",
          questionLevelLinkAccessibilityTextNo = "page.iht.application.assets.insurance.policies.overview.joint.no.screenReader.link.value",
          questionLevelLinkAccessibilityTextValue = "page.iht.application.assets.insurance.policies.overview.joint.amount.screenReader.link.value",
          questionAnswerExprYesNo = insurancePolicy.isJointlyOwned,
          questionAnswerExprValue = insurancePolicy.shareValue,
          questionTitleYesNoMessage = Messages("iht.estateReport.insurancePolicies.jointlyHeld.question",
                                              CommonHelper.getDeceasedNameOrDefaultString(regDetails)),
          questionTitleValueMessage = Messages("iht.estateReport.assets.insurancePolicies.totalValueOfDeceasedsShare")
        )
        val seqSection3 = createSectionFromYesNoQuestions(
          id = "other",
          title = Some(Messages("iht.estateReport.assets.insurancePolicies.premiumsPaidByOther",
                           CommonHelper.getDeceasedNameOrDefaultString(regDetails))),
          linkUrl = routes.InsurancePolicyDetailsPayingOtherController.onPageLoad(),
          sectionLevelLinkAccessibilityText = "page.iht.application.assets.insurance.policies.overview.other.giveAnswer.screenReader.link.value",
          questionAnswersPlusChangeLinks = section3YesNoItems(insurancePolicy, regDetails),
          questionTitlesMessagesFileItems = Seq(
            Messages("iht.estateReport.insurancePolicies.premiumsNotPayingOut.question",
                      CommonHelper.getDeceasedNameOrDefaultString(regDetails)),
            Messages("iht.estateReport.insurancePolicies.overLimitNotOwnEstate.question",
                      CommonHelper.getDeceasedNameOrDefaultString(regDetails)),
            Messages("iht.estateReport.assets.insurancePolicies.buyAnnuity.question",
                      CommonHelper.getDeceasedNameOrDefaultString(regDetails)),
            Messages("page.iht.application.assets.insurance.policies.overview.other.question4",
                      CommonHelper.getDeceasedNameOrDefaultString(regDetails))
          ),
          ad, regDetails)

        Ok(iht.views.html.application.asset.insurancePolicy.insurance_policies_overview(regDetails,
          Seq(seqSection1, seqSection2, seqSection3),
          Some(iht.controllers.application.assets.routes.AssetsOverviewController.onPageLoad()),
          "page.iht.application.return.to.assetsOf"))
      }
    }
  }
}

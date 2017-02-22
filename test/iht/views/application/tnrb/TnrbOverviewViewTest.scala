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

package iht.views.application.tnrb

import iht.testhelpers.{CommonBuilder, ContentChecker}
import iht.utils.CommonHelper
import iht.utils.tnrb.TnrbHelper
import iht.views.ViewTestHelper
import iht.views.html.application.tnrb.tnrb_overview
import org.jsoup.nodes.Element
import org.jsoup.select.Elements
import play.api.i18n.Messages.Implicits._

class TnrbOverviewViewTest extends ViewTestHelper {

  val ihtReference =  "ABC"
  val regDetails = CommonBuilder.buildRegistrationDetails1.copy(ihtReference = Some(ihtReference))
  val widowCheckModel = CommonBuilder.buildWidowedCheck
  val tnrbModel = CommonBuilder.buildTnrbEligibility

  "tnrb Overview page" must {

    "show the correct title" in {
      implicit val request = createFakeRequest()
      val view = tnrb_overview(regDetails, widowCheckModel, tnrbModel, ihtReference).toString
      val doc = asDocument(view)
      val headers: Elements = doc.getElementsByTag("h1")
      headers.size() shouldBe 1
      headers.first().text() shouldBe messagesApi("iht.estateReport.tnrb.increasingIHTThreshold")
    }

    "show the correct browser title" in {
      implicit val request = createFakeRequest()
      val view = tnrb_overview(regDetails, widowCheckModel, tnrbModel, ihtReference).toString
      val doc = asDocument(view)
      assertEqualsValue(doc, "title",
        messagesApi("iht.estateReport.tnrb.increasingThreshold") + " " + messagesApi("site.title.govuk"))
    }

    "show the correct guidance paragraphs" in {
      implicit val request = createFakeRequest()
      val view = ContentChecker.stripLineBreaks(tnrb_overview(regDetails, widowCheckModel, tnrbModel, ihtReference).toString)
      view should include(messagesApi("page.iht.application.tnrbEligibilty.overview.guidance1",
                          CommonHelper.getDeceasedNameOrDefaultString(regDetails)))
      view should include(messagesApi("page.iht.application.tnrbEligibilty.overview.guidance2",
                            TnrbHelper.spouseOrCivilPartnerLabel(tnrbModel, widowCheckModel,
                             messagesApi("page.iht.application.tnrbEligibilty.partner.additional.label.the.deceased",
                                        CommonHelper.getDeceasedNameOrDefaultString(regDetails))),
                             CommonHelper.getOrException(widowCheckModel.dateOfPreDeceased).getYear.toString ))
      view should include(messagesApi("iht.estateReport.completeEverySection"))

    }

    "show the correct headings and all the questions text" in {

      val deceasedName = CommonHelper.getOrException(regDetails.deceasedDetails).name
      val predeceasedName = TnrbHelper.spouseOrCivilPartnerLabel(tnrbModel, widowCheckModel,
                                            messagesApi("page.iht.application.tnrbEligibilty.partner.additional.label.the"))

      implicit val request = createFakeRequest()
      val view = tnrb_overview(regDetails, widowCheckModel, tnrbModel, ihtReference).toString
      val doc = asDocument(view)

      assertEqualsValue(doc, "h2#tnrb-partner-estate",
                               messagesApi("page.iht.application.tnrbEligibilty.overview.partnerEstate.questions.heading",
                                 TnrbHelper.spouseOrCivilPartnerLabel(tnrbModel, widowCheckModel,
                                 messagesApi("page.iht.application.tnrbEligibilty.partner.additional.label.the.deceased",
                                          CommonHelper.getDeceasedNameOrDefaultString(regDetails))),
                                 CommonHelper.getOrException(widowCheckModel.dateOfPreDeceased).getYear.toString))

      assertEqualsValue(doc, "li#home-in-uk span",
                                  messagesApi("iht.estateReport.tnrb.permanentHome.question", predeceasedName, deceasedName))
      assertEqualsValue(doc, "li#gifts-given-away span",
                                  messagesApi("iht.estateReport.tnrb.giftsMadeBeforeDeath.question", predeceasedName))
      assertEqualsValue(doc, "li#gifts-with-reservation span",
                                  messagesApi("page.iht.application.tnrbEligibilty.overview.giftsWithReservation.question",
                                              predeceasedName, deceasedName))
      assertEqualsValue(doc, "li#state-claim-any-business span",
                                  messagesApi("iht.estateReport.tnrb.stateClaim.question"))
      assertEqualsValue(doc, "li#is-partner-ben-from-trust span",
                                  messagesApi("iht.estateReport.tnrb.benefitFromTrust.question", predeceasedName))
      assertEqualsValue(doc, "li#is-estate-below-iht-threshold-applied span",
                                  messagesApi("page.iht.application.tnrbEligibilty.overview.charity.question",
                                              predeceasedName, deceasedName))
      assertEqualsValue(doc, "li#is-joint-asset-passed span",
                                  messagesApi("page.iht.application.tnrbEligibilty.overview.jointlyOwned.question",
                                              predeceasedName, deceasedName))

      assertEqualsValue(doc, "h2#tnrb-partner-personal-details",
                              messagesApi("page.iht.application.tnrbEligibilty.overview.partner.personalDetails.heading",
                                TnrbHelper.spouseOrCivilPartnerLabel(tnrbModel, widowCheckModel,
                                 messagesApi("page.iht.application.tnrbEligibilty.partner.additional.label.the.deceased",
                                          CommonHelper.getDeceasedNameOrDefaultString(regDetails)))))

     assertEqualsValue(doc, "li#partner-marital-status span",
        messagesApi("iht.estateReport.tnrb.partner.married",
          TnrbHelper.preDeceasedMaritalStatusSubLabel(widowCheckModel.dateOfPreDeceased),
          TnrbHelper.spouseOrCivilPartnerMessage(widowCheckModel.dateOfPreDeceased)))

    assertEqualsValue(doc, "li#date-of-preDeceased span",
        messagesApi("page.iht.application.tnrbEligibilty.overview.partner.dod.question",
          TnrbHelper.spouseOrCivilPartnerLabel(tnrbModel,
            widowCheckModel,
            messagesApi("page.iht.application.tnrbEligibilty.partner.additional.label.the.deceased",
                    CommonHelper.getDeceasedNameOrDefaultString(regDetails)))))

     assertEqualsValue(doc, "li#partner-name span",
        messagesApi("page.iht.application.tnrbEligibilty.overview.partner.name.question",
          TnrbHelper.spouseOrCivilPartnerNameLabel(tnrbModel,
                          widowCheckModel,
                          messagesApi("page.iht.application.tnrbEligibilty.partner.additional.label.name.of.the"))))

      assertEqualsValue(doc, "li#date-of-marriage span",
                   messagesApi("iht.estateReport.tnrb.dateOfMarriage",
                          TnrbHelper.marriageOrCivilPartnerShipLabel(widowCheckModel)))

    }

    "show the correct links for all the questions" in {
      implicit val request = createFakeRequest()
      val view = tnrb_overview(regDetails, widowCheckModel, tnrbModel, ihtReference).toString
      val doc = asDocument(view)

      val homeInTheUKQuestion = doc.getElementById("home-in-uk-link")
      homeInTheUKQuestion.attr("href") shouldBe
        iht.controllers.application.tnrb.routes.PermanentHomeController.onPageLoad.url

      val giftsGivenAwayQuestion = doc.getElementById("gifts-given-away-link")
      giftsGivenAwayQuestion.attr("href") shouldBe
        iht.controllers.application.tnrb.routes.GiftsMadeBeforeDeathController.onPageLoad.url

      val giftsWithReservationQuestion = doc.getElementById("gifts-with-reservation-link")
      giftsWithReservationQuestion.attr("href") shouldBe
        iht.controllers.application.tnrb.routes.GiftsWithReservationOfBenefitController.onPageLoad.url

      val claimAnyBusinessUKQuestion = doc.getElementById("state-claim-any-business-link")
      claimAnyBusinessUKQuestion.attr("href") shouldBe
        iht.controllers.application.tnrb.routes.EstateClaimController.onPageLoad.url

      val partnerBenefitFromTrustQuestion = doc.getElementById("is-partner-ben-from-trust-link")
      partnerBenefitFromTrustQuestion.attr("href") shouldBe
        iht.controllers.application.tnrb.routes.BenefitFromTrustController.onPageLoad.url

      val estatePassedToCharityKQuestion = doc.getElementById("is-estate-below-iht-threshold-applied-link")
      estatePassedToCharityKQuestion.attr("href") shouldBe
        iht.controllers.application.tnrb.routes.EstatePassedToDeceasedOrCharityController.onPageLoad.url

      val jointAssetsPassedQuestion = doc.getElementById("is-joint-asset-passed-link")
      jointAssetsPassedQuestion.attr("href") shouldBe
        iht.controllers.application.tnrb.routes.JointlyOwnedAssetsController.onPageLoad.url

      val partnerMaritalStatusQuestion = doc.getElementById("partner-marital-status-link")
      partnerMaritalStatusQuestion.attr("href") shouldBe
        iht.controllers.application.tnrb.routes.DeceasedWidowCheckQuestionController.onPageLoad.url

      val dateOfPreDeceasedQuestion = doc.getElementById("date-of-preDeceased-link")
      dateOfPreDeceasedQuestion.attr("href") shouldBe
        iht.controllers.application.tnrb.routes.DeceasedWidowCheckDateController.onPageLoad.url

      val partnerNameQuestion = doc.getElementById("partner-name-link")
      partnerNameQuestion.attr("href") shouldBe
        iht.controllers.application.tnrb.routes.PartnerNameController.onPageLoad.url

      val dateOfMarriageQuestion = doc.getElementById("date-of-marriage-link")
      dateOfMarriageQuestion.attr("href") shouldBe
        iht.controllers.application.tnrb.routes.DateOfMarriageController.onPageLoad.url


    }

    "show a return to estate overview button which has specified iht reference" in {
      implicit val request = createFakeRequest()
      val view = tnrb_overview(regDetails, widowCheckModel, tnrbModel, ihtReference).toString
      val doc = asDocument(view)
      val button: Element = doc.getElementById("return-button")
      button.text() shouldBe messagesApi("iht.estateReport.returnToEstateOverview")
      button.className() shouldBe "button"
      button.attr("href") shouldBe
        iht.controllers.application.routes.EstateOverviewController.onPageLoadWithIhtRef(ihtReference).url
   }

  }
}

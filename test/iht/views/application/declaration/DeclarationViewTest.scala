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

package iht.views.application.declaration

import iht.forms.ApplicationForms
import iht.testhelpers.CommonBuilder
import iht.utils.CommonHelper._
import iht.utils.DeclarationReason
import iht.viewmodels.application.DeclarationViewModel
import iht.views.ViewTestHelper
import iht.views.html.application.declaration.declaration
import org.jsoup.nodes.Document
import play.api.i18n.Messages

class DeclarationViewTest extends ViewTestHelper {

  lazy val prologue1Id = "prologue1"
  lazy val prologue2Id = "prologue2"
  lazy val prologue3Id = "prologue3"
  lazy val prologue4Id = "prologue4"

  lazy val confirmationCheckBoxId = "isDeclared"

  lazy val summaryTextId = "summary-text"
  lazy val summaryBullet1TextId = "summary-bullet1-text"
  lazy val summaryBullet2TextId = "summary-bullet2-text"
  lazy val summaryBullet3TextId = "summary-bullet3-text"

  lazy val mainTextId = "main-text"
  lazy val mainBullet1TextId = "main-bullet1-text"
  lazy val mainBullet2TextId = "main-bullet2-text"
  lazy val mainBullet3TextId = "main-bullet3-text"
  lazy val mainBullet4TextId = "main-bullet4-text"
  lazy val mainBullet5TextId = "main-bullet5-text"
  lazy val mainBullet6TextId = "main-bullet6-text"

  lazy val youMayFaceProsecutionMsgKey = "iht.estateReport.declaration.youMayFaceProsecution"
  lazy val executorsMayFaceProsecution = "iht.estateReport.declaration.coExecutors.mayFaceProsecution"
  lazy val withholdInformationMsgKey = "iht.estateReport.declaration.withholdInformation"
  lazy val dontTellHMRCMsgKey = "iht.estateReport.declaration.dontTellHMRC"

  lazy val declaringThatMsgKey = "iht.estateReport.declaration.declaringThat"
  lazy val completedAllReasonableEnquiriesMsgKey = "iht.estateReport.declaration.completedAllReasonableEnquiries"
  lazy val correctAndCompleteMsgKey = "iht.estateReport.declaration.correctAndComplete"
  lazy val noInheritanceTaxPayableMsgKey = "iht.estateReport.noInheritanceTaxPayable"
  lazy val estateValueBeforeExemptionsLessThan1MillionMsgKey = "iht.estateReport.declaration.estateValueBeforeExemptionsLessThan1Million"
  lazy val haveProvidedNonMatchingDetails = "iht.estateReport.declaration.haveProvidedNonMatchingDetails"
  lazy val deceasedMarriedWhenPartnerDied = "iht.estateReport.declaration.deceasedMarriedWhenPartnerDied"
  lazy val didntUseAnyOfThreshold = "iht.estateReport.declaration.didntUseAnyOfThreshold"

  lazy val coExecutorsPrologue1MsgKey = "iht.estateReport.declaration.coExecutors.prologue1"
  lazy val coExecutorsPrologue2MsgKey = "iht.estateReport.declaration.coExecutors.prologue2"
  lazy val coExecutorsPrologue3MsgKey = "iht.estateReport.declaration.coExecutors.prologue3"
  lazy val coExecutorsPrologue4MsgKey = "iht.estateReport.declaration.coExecutors.prologue4"
  lazy val coExecutorsConfirmationTextMsgKey= "iht.estateReport.declaration.coExecutors.confirmationText"

  lazy val regDetails = CommonBuilder.buildRegistrationDetails1

  def declarationView(isMultipleExecutor: Boolean, declarationType: String) = {
    implicit val request = createFakeRequest()

    val dvm = new DeclarationViewModel(ApplicationForms.declarationForm,
      declarationType = declarationType,
      executors = regDetails.coExecutors,
      isMultipleExecutor = isMultipleExecutor,
      registrationDetails = regDetails,
      riskMessageFromEdh = None
    )

    val view = declaration(dvm).toString
    asDocument(view)
  }

  def assertSingleExecutorSection(doc: Document) = {
    assertNotRenderedById(doc, prologue1Id)
    assertNotRenderedById(doc, prologue2Id)
    assertNotRenderedById(doc, prologue3Id)
    assertNotRenderedById(doc, prologue4Id)
  }

  def assertMultipleExecutorsSection(doc: Document) = {
    assertEqualsValue(doc, s"#$prologue1Id", Messages(coExecutorsPrologue1MsgKey))
    assertEqualsValue(doc, s"#$prologue2Id", Messages(coExecutorsPrologue2MsgKey))
    assertEqualsValue(doc, s"#$prologue3Id", Messages(coExecutorsPrologue3MsgKey))
    assertRenderedById(doc, prologue4Id)
    assertContainsText(doc, Messages(coExecutorsPrologue4MsgKey))
    assertContainsText(doc, escapeSpace(CommonBuilder.DefaultCoExecutor1.name))
    assertContainsText(doc, escapeSpace(CommonBuilder.DefaultCoExecutor2.name))
    assertContainsText(doc, escapeSpace(CommonBuilder.DefaultCoExecutor3.name))

    assertRenderedById(doc, confirmationCheckBoxId)
    assertContainsText(doc, Messages(coExecutorsConfirmationTextMsgKey))

    assertLinkHasValue(doc, "pdf-summary-link", iht.controllers.application.pdf.routes.PDFController.onPreSubmissionPDF.url)
    assertEqualsValue(doc, "p a#pdf-summary-link", Messages("iht.estateReport.copyOfTheEstateReportAndPrint"))
  }

  "Declaration Page" must {
   "show correct title and browserTitle" in {
     val page = declarationView(isMultipleExecutor = false, declarationType = DeclarationReason.ValueLessThanNilRateBand).toString
     titleShouldBeCorrect(page, Messages("iht.estateReport.declaration.title"))
     browserTitleShouldBeCorrect(page, Messages("iht.estateReport.declaration.title"))

   }
  }

  "Declaration type for Single Executor" must {

    "display correct contents for declaration type ValueLessThanNilRateBand" in {
      val doc = declarationView(isMultipleExecutor = false, declarationType = DeclarationReason.ValueLessThanNilRateBand)

      assertSingleExecutorSection(doc)

      assertEqualsValue(doc, s"#$summaryTextId", Messages(youMayFaceProsecutionMsgKey))
      assertEqualsValue(doc, s"#$summaryBullet1TextId", Messages(withholdInformationMsgKey))
      assertEqualsValue(doc, s"#$summaryBullet2TextId", Messages(dontTellHMRCMsgKey))

      assertNotRenderedById(doc, summaryBullet3TextId)

      assertEqualsValue(doc, s"#$mainTextId", Messages(declaringThatMsgKey))
      assertEqualsValue(doc, s"#$mainBullet1TextId", Messages(completedAllReasonableEnquiriesMsgKey))
      assertEqualsValue(doc, s"#$mainBullet2TextId", Messages(correctAndCompleteMsgKey))
      assertEqualsValue(doc, s"#$mainBullet3TextId", Messages(noInheritanceTaxPayableMsgKey))

      assertNotRenderedById(doc, mainBullet4TextId)
      assertNotRenderedById(doc, mainBullet5TextId)
      assertNotRenderedById(doc, mainBullet6TextId)
    }

    "display correct contents for declaration type ValueLessThanNilRateBandAfterExemption" in {
      val doc = declarationView(isMultipleExecutor = false, declarationType = DeclarationReason.ValueLessThanNilRateBandAfterExemption)

      assertSingleExecutorSection(doc)

      assertEqualsValue(doc, s"#$summaryTextId", Messages(youMayFaceProsecutionMsgKey))
      assertEqualsValue(doc, s"#$summaryBullet1TextId", Messages(withholdInformationMsgKey))
      assertEqualsValue(doc, s"#$summaryBullet2TextId", Messages(dontTellHMRCMsgKey))

      assertNotRenderedById(doc, summaryBullet3TextId)

      assertEqualsValue(doc, s"#$mainTextId", Messages(declaringThatMsgKey))
      assertEqualsValue(doc, s"#$mainBullet1TextId", Messages(completedAllReasonableEnquiriesMsgKey))
      assertEqualsValue(doc, s"#$mainBullet2TextId", Messages(correctAndCompleteMsgKey))
      assertEqualsValue(doc, s"#$mainBullet3TextId", escapePound(Messages(estateValueBeforeExemptionsLessThan1MillionMsgKey)))
      assertEqualsValue(doc, s"#$mainBullet4TextId", Messages(noInheritanceTaxPayableMsgKey))

      assertNotRenderedById(doc, mainBullet5TextId)
      assertNotRenderedById(doc, mainBullet6TextId)
    }

    "display correct contents for declaration type ValueLessThanTransferredNilRateBand" in {
      val doc = declarationView(isMultipleExecutor = false, declarationType = DeclarationReason.ValueLessThanTransferredNilRateBand)

      assertSingleExecutorSection(doc)

      assertEqualsValue(doc, s"#$summaryTextId", Messages(youMayFaceProsecutionMsgKey))
      assertEqualsValue(doc, s"#$summaryBullet1TextId", Messages(withholdInformationMsgKey))
      assertEqualsValue(doc, s"#$summaryBullet2TextId", Messages(dontTellHMRCMsgKey))
      assertEqualsValue(doc, s"#$summaryBullet3TextId", Messages(haveProvidedNonMatchingDetails))

      assertEqualsValue(doc, s"#$mainTextId", Messages(declaringThatMsgKey))
      assertEqualsValue(doc, s"#$mainBullet1TextId", Messages(deceasedMarriedWhenPartnerDied))
      assertEqualsValue(doc, s"#$mainBullet2TextId", Messages(didntUseAnyOfThreshold))
      assertEqualsValue(doc, s"#$mainBullet3TextId", Messages(completedAllReasonableEnquiriesMsgKey))
      assertEqualsValue(doc, s"#$mainBullet4TextId", Messages(correctAndCompleteMsgKey))
      assertEqualsValue(doc, s"#$mainBullet5TextId", Messages(noInheritanceTaxPayableMsgKey))

      assertNotRenderedById(doc, mainBullet6TextId)
    }

    "display correct contents for declaration type ValueLessThanTransferredNilRateBandAfterExemption" in {
      val doc = declarationView(isMultipleExecutor = false, declarationType = DeclarationReason.ValueLessThanTransferredNilRateBandAfterExemption)

      assertSingleExecutorSection(doc)

      assertEqualsValue(doc, s"#$summaryTextId", Messages(youMayFaceProsecutionMsgKey))
      assertEqualsValue(doc, s"#$summaryBullet1TextId", Messages(withholdInformationMsgKey))
      assertEqualsValue(doc, s"#$summaryBullet2TextId", Messages(dontTellHMRCMsgKey))
      assertEqualsValue(doc, s"#$summaryBullet3TextId", Messages(haveProvidedNonMatchingDetails))

      assertEqualsValue(doc, s"#$mainTextId", Messages(declaringThatMsgKey))
      assertEqualsValue(doc, s"#$mainBullet1TextId", Messages(deceasedMarriedWhenPartnerDied))
      assertEqualsValue(doc, s"#$mainBullet2TextId", Messages(didntUseAnyOfThreshold))
      assertEqualsValue(doc, s"#$mainBullet3TextId", Messages(completedAllReasonableEnquiriesMsgKey))
      assertEqualsValue(doc, s"#$mainBullet4TextId", Messages(correctAndCompleteMsgKey))
      assertEqualsValue(doc, s"#$mainBullet5TextId", escapePound(Messages(estateValueBeforeExemptionsLessThan1MillionMsgKey)))
      assertEqualsValue(doc, s"#$mainBullet6TextId", Messages(noInheritanceTaxPayableMsgKey))
    }
  }

  "Declaration type for Multiple Executors" must {

    "display correct contents for declaration type ValueLessThanNilRateBand" in {
      val doc = declarationView(isMultipleExecutor = true, declarationType = DeclarationReason.ValueLessThanNilRateBand)

      assertMultipleExecutorsSection(doc)

      assertEqualsValue(doc, s"#$summaryTextId", Messages(executorsMayFaceProsecution))
      assertEqualsValue(doc, s"#$summaryBullet1TextId", Messages(withholdInformationMsgKey))
      assertEqualsValue(doc, s"#$summaryBullet2TextId", Messages(dontTellHMRCMsgKey))

      assertNotRenderedById(doc, summaryBullet3TextId)

      assertEqualsValue(doc, s"#$mainTextId", Messages(declaringThatMsgKey))
      assertEqualsValue(doc, s"#$mainBullet1TextId", Messages(completedAllReasonableEnquiriesMsgKey))
      assertEqualsValue(doc, s"#$mainBullet2TextId", Messages(correctAndCompleteMsgKey))
      assertEqualsValue(doc, s"#$mainBullet3TextId", Messages(noInheritanceTaxPayableMsgKey))

      assertNotRenderedById(doc, mainBullet4TextId)
      assertNotRenderedById(doc, mainBullet5TextId)
      assertNotRenderedById(doc, mainBullet6TextId)
    }


    "display correct contents for declaration type ValueLessThanNilRateBandAfterExemption" in {
      val doc = declarationView(isMultipleExecutor = true, declarationType = DeclarationReason.ValueLessThanNilRateBandAfterExemption)

      assertMultipleExecutorsSection(doc)

      assertEqualsValue(doc, s"#$summaryTextId", Messages(executorsMayFaceProsecution))
      assertEqualsValue(doc, s"#$summaryBullet1TextId", Messages(withholdInformationMsgKey))
      assertEqualsValue(doc, s"#$summaryBullet2TextId", Messages(dontTellHMRCMsgKey))

      assertNotRenderedById(doc, summaryBullet3TextId)

      assertEqualsValue(doc, s"#$mainTextId", Messages(declaringThatMsgKey))
      assertEqualsValue(doc, s"#$mainBullet1TextId", Messages(completedAllReasonableEnquiriesMsgKey))
      assertEqualsValue(doc, s"#$mainBullet2TextId", Messages(correctAndCompleteMsgKey))
      assertEqualsValue(doc, s"#$mainBullet3TextId", escapePound(Messages(estateValueBeforeExemptionsLessThan1MillionMsgKey)))
      assertEqualsValue(doc, s"#$mainBullet4TextId", Messages(noInheritanceTaxPayableMsgKey))

      assertNotRenderedById(doc, mainBullet5TextId)
      assertNotRenderedById(doc, mainBullet6TextId)
    }

    "display correct contents for declaration type ValueLessThanTransferredNilRateBand" in {
      val doc = declarationView(isMultipleExecutor = true, declarationType = DeclarationReason.ValueLessThanTransferredNilRateBand)

      assertMultipleExecutorsSection(doc)

      assertEqualsValue(doc, s"#$summaryTextId", Messages(executorsMayFaceProsecution))
      assertEqualsValue(doc, s"#$summaryBullet1TextId", Messages(withholdInformationMsgKey))
      assertEqualsValue(doc, s"#$summaryBullet2TextId", Messages(dontTellHMRCMsgKey))
      assertEqualsValue(doc, s"#$summaryBullet3TextId", Messages(haveProvidedNonMatchingDetails))

      assertEqualsValue(doc, s"#$mainTextId", Messages(declaringThatMsgKey))
      assertEqualsValue(doc, s"#$mainBullet1TextId", Messages(deceasedMarriedWhenPartnerDied))
      assertEqualsValue(doc, s"#$mainBullet2TextId", Messages(didntUseAnyOfThreshold))
      assertEqualsValue(doc, s"#$mainBullet3TextId", Messages(completedAllReasonableEnquiriesMsgKey))
      assertEqualsValue(doc, s"#$mainBullet4TextId", Messages(correctAndCompleteMsgKey))
      assertEqualsValue(doc, s"#$mainBullet5TextId", Messages(noInheritanceTaxPayableMsgKey))

      assertNotRenderedById(doc, mainBullet6TextId)
    }

    "display correct contents for declaration type ValueLessThanTransferredNilRateBandAfterExemption" in {
      val doc = declarationView(isMultipleExecutor = true, declarationType = DeclarationReason.ValueLessThanTransferredNilRateBandAfterExemption)

      assertMultipleExecutorsSection(doc)

      assertEqualsValue(doc, s"#$summaryTextId", Messages(executorsMayFaceProsecution))
      assertEqualsValue(doc, s"#$summaryBullet1TextId", Messages(withholdInformationMsgKey))
      assertEqualsValue(doc, s"#$summaryBullet2TextId", Messages(dontTellHMRCMsgKey))
      assertEqualsValue(doc, s"#$summaryBullet3TextId", Messages(haveProvidedNonMatchingDetails))

      assertEqualsValue(doc, s"#$mainTextId", Messages(declaringThatMsgKey))
      assertEqualsValue(doc, s"#$mainBullet1TextId", Messages(deceasedMarriedWhenPartnerDied))
      assertEqualsValue(doc, s"#$mainBullet2TextId", Messages(didntUseAnyOfThreshold))
      assertEqualsValue(doc, s"#$mainBullet3TextId", Messages(completedAllReasonableEnquiriesMsgKey))
      assertEqualsValue(doc, s"#$mainBullet4TextId", Messages(correctAndCompleteMsgKey))
      assertEqualsValue(doc, s"#$mainBullet5TextId", escapePound(Messages(estateValueBeforeExemptionsLessThan1MillionMsgKey)))
      assertEqualsValue(doc, s"#$mainBullet6TextId", Messages(noInheritanceTaxPayableMsgKey))
    }

  }

}

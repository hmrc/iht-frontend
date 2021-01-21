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

package iht.views.application

import iht.testhelpers.SharableOverviewData
import iht.views.helpers.GenericOverviewHelper

trait ShareableElementOverviewViewBehaviour extends GenericOverviewHelper with SharableOverviewData{

  def pageTitle: String
  def browserTitle: String
  def guidanceParagraphs: Set[String]
  def ownHeadingElementId: String
  def jointlyOwnedHeadingElementId: String
  def urlToOwnPage: String
  def urlToJointlyOwnedPage: String
  def ownHeaderText: String
  def jointlyOwnedHeaderText: String
  def ownQuestionRowId: String
  def ownQuestionText: String
  def ownValueRowId: String
  def ownValueText: String
  def jointlyOwnedQuestionRowId: String
  def jointlyOwnedQuestionText: String
  def jointlyOwnedValueRowId: String
  def jointlyOwnedValueText: String
  def deceasedName: String
  def linkHash: String = ""

  def viewWithQuestionsAnsweredNo: String
  def viewWithQuestionsAnsweredYes: String
  def viewWithQuestionsUnanswered: String
  def viewWithValues: String

  def overviewPage()  = {
    overviewViewWithGenericContents()
    overviewViewWithQuestionsUnanswered("questionsUnanswered view")
    overviewViewWithQuestionsAnsweredNo("questionsAnsweredNo view")
    overviewViewWithQuestionsAnsweredYes("questionsAnsweredYes view")
    overviewViewWithValues("view with values")
  }

  def overviewViewWithGenericContents() = {

    "have no message keys in html" in {
      noMessageKeysShouldBePresent(viewWithQuestionsAnsweredNo)
    }

    "have the correct title" in {
      titleShouldBeCorrect(viewWithQuestionsAnsweredNo, pageTitle)
    }

    "have the correct browser title" in {
      browserTitleShouldBeCorrect(viewWithQuestionsAnsweredNo, browserTitle)
    }

    "show the correct guidance paragraphs" in {
      for (paragraph <- guidanceParagraphs) messagesShouldBePresent(viewWithQuestionsAnsweredNo, paragraph)
    }

    "show the correct return link with right text" in {
      val doc = asDocument(viewWithQuestionsAnsweredNo)
      val returnLink = doc.getElementById("return-button")
      returnLink.attr("href") mustBe iht.controllers.application.assets.routes.AssetsOverviewController.onPageLoad().url + "#" + linkHash
      returnLink.text() mustBe messagesApi("page.iht.application.return.to.assetsOf",deceasedName)
    }
  }

  def overviewViewWithQuestionsUnanswered(sectionName: String) = {
    "have no message keys in html when the questions are unanswered" in {
      noMessageKeysShouldBePresent(viewWithQuestionsUnanswered)
    }

    "show the 'owned only by the deceased' question header as unanswered with a link to give details in " + sectionName in {
      headerQuestionShouldBeUnanswered(asDocument(viewWithQuestionsUnanswered), ownHeadingElementId, ownHeaderText, urlToOwnPage)
    }

    "show the 'jointly owned' question header as unanswered with a link to give details in " + sectionName in {
      headerQuestionShouldBeUnanswered(asDocument(viewWithQuestionsUnanswered),
                                jointlyOwnedHeadingElementId, jointlyOwnedHeaderText, urlToJointlyOwnedPage)
    }

    "not show the 'owned only by the deceased' question row in " + sectionName in {
      assertNotRenderedById(asDocument(viewWithQuestionsUnanswered), s"$ownQuestionRowId-block")
    }

    "not show the 'value owned only be the deceased' row in " + sectionName in {
      assertNotRenderedById(asDocument(viewWithQuestionsUnanswered), ownValueRowId)
    }

    "not show the 'jointly owned' question row in " + sectionName in {
      assertNotRenderedById(asDocument(viewWithQuestionsUnanswered), s"$jointlyOwnedQuestionRowId-block")
    }

    "not show the 'value jointly owned' row in " + sectionName in {
      assertNotRenderedById(asDocument(viewWithQuestionsUnanswered), jointlyOwnedValueRowId)
    }
  }

  def overviewViewWithQuestionsAnsweredNo(sectionName: String) = {
    "have no message keys in html when the questions are all answered no" in {
      noMessageKeysShouldBePresent(viewWithQuestionsAnsweredNo)
    }

    "show the 'only owned by the deceased' question header as answered with no link in " + sectionName in {
      headerShouldBeAnswered(asDocument(viewWithQuestionsAnsweredNo), ownHeadingElementId, ownHeaderText)
    }

    "show the 'jointly owned' question header as answered with no link in " + sectionName in {
      headerShouldBeAnswered(asDocument(viewWithQuestionsAnsweredNo), jointlyOwnedHeadingElementId, jointlyOwnedHeaderText)
    }

    "show the 'owned only by the deceased' question row with an answer of No in " + sectionName in {
      rowShouldBeAnswered(asDocument(viewWithQuestionsAnsweredNo), ownQuestionRowId, ownQuestionText, "No", "iht.change", urlToOwnPage)
    }

    "not show the 'value owned only be the deceased' row in " + sectionName in {
      assertNotRenderedById(asDocument(viewWithQuestionsAnsweredNo), ownValueRowId)
    }

    "show the 'jointly owned' question row with an answer of No in " + sectionName in {
      rowShouldBeAnswered(asDocument(viewWithQuestionsAnsweredNo),
        jointlyOwnedQuestionRowId, jointlyOwnedQuestionText, "No", "iht.change", urlToJointlyOwnedPage)
    }

    "not show the 'value jointly owned' row in " + sectionName in {
      assertNotRenderedById(asDocument(viewWithQuestionsAnsweredNo), jointlyOwnedValueRowId)
    }
  }

  def overviewViewWithQuestionsAnsweredYes(sectionName: String) = {
    "have no message keys in html when the questions are all answered yes" in {
      noMessageKeysShouldBePresent(viewWithQuestionsAnsweredYes)
    }

    "show the 'only owned by the deceased' question header as answered with no link in " + sectionName in {
      headerShouldBeAnswered(asDocument(viewWithQuestionsAnsweredYes), ownHeadingElementId, ownHeaderText)
    }

    "show the 'jointly owned' question header as answered with no link in " + sectionName in {
      headerShouldBeAnswered(asDocument(viewWithQuestionsAnsweredYes), jointlyOwnedHeadingElementId, jointlyOwnedHeaderText)
    }

    "show the 'owned only by the deceased' question row with an answer of Yes in " + sectionName in {
      rowShouldBeAnswered(asDocument(viewWithQuestionsAnsweredYes), ownQuestionRowId, ownQuestionText, "Yes", "iht.change", urlToOwnPage)
    }

    "show the 'value owned only be the deceased' row as unanswered in " + sectionName in {
      rowShouldBeAnswered(asDocument(viewWithQuestionsAnsweredYes), ownValueRowId, ownValueText, "", "site.link.giveAValue", urlToOwnPage)
    }

    "show the 'jointly owned' question row with an answer of Yes in " + sectionName in {
      rowShouldBeAnswered(asDocument(viewWithQuestionsAnsweredYes),
                      jointlyOwnedQuestionRowId, jointlyOwnedQuestionText, "Yes", "iht.change", urlToJointlyOwnedPage)
    }

    "show the 'value jointly owned' row as unanswered in " + sectionName in {
      rowShouldBeAnswered(asDocument(viewWithQuestionsAnsweredYes),
                      jointlyOwnedValueRowId, jointlyOwnedValueText, "", "site.link.giveAValue", urlToJointlyOwnedPage)
    }
  }

  def overviewViewWithValues(sectionName: String) = {
    "have no message keys in html when there are values" in {
      noMessageKeysShouldBePresent(viewWithValues)
    }

    "show the 'only owned by the deceased' question header as answered with no link in " + sectionName in {
      headerShouldBeAnswered(asDocument(viewWithValues), ownHeadingElementId, ownHeaderText)
    }

    "show the 'jointly owned' question header as answered with no link in " + sectionName in {
      headerShouldBeAnswered(asDocument(viewWithValues), jointlyOwnedHeadingElementId, jointlyOwnedHeaderText)
    }

    "show the 'owned only by the deceased' question row with an answer of Yes in " + sectionName in {
      rowShouldBeAnswered(asDocument(viewWithValues), ownQuestionRowId, ownQuestionText, "Yes", "iht.change", urlToOwnPage)
    }

    "show the 'value owned only by the deceased' row a value in " + sectionName in {
      rowShouldBeAnswered(asDocument(viewWithValues), ownValueRowId, ownValueText, ownedAmountDisplay, "iht.change", urlToOwnPage)
    }

    "show the 'jointly owned' question row with an answer of Yes in " + sectionName in {
      rowShouldBeAnswered(asDocument(viewWithValues),
                      jointlyOwnedQuestionRowId, jointlyOwnedQuestionText, "Yes", "iht.change", urlToJointlyOwnedPage)
    }

    "show the 'value jointly owned' row with a value in " + sectionName in {
      rowShouldBeAnswered(asDocument(viewWithValues),
                      jointlyOwnedValueRowId, jointlyOwnedValueText, jointAmountDisplay, "iht.change", urlToJointlyOwnedPage)
    }
  }
}

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

package iht.views.application

import iht.models.application.basicElements.ShareableBasicEstateElement
import iht.views.ViewTestHelper
import iht.views.helpers.GenericOverviewHelper._
import org.jsoup.nodes.Document
import play.api.i18n.Messages
import play.api.i18n.Messages.Implicits._
import play.api.Play.current
import play.api.mvc.AnyContentAsEmpty
import play.api.test.FakeRequest

trait ShareableElementOverviewViewBehaviour extends ViewTestHelper {

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

  val dataWithQuestionsAnsweredNo =
    Some(ShareableBasicEstateElement(value = None, shareValue = None, isOwned = Some(false), isOwnedShare = Some(false)))

  val dataWithQuestionsAnsweredYes =
    Some(ShareableBasicEstateElement(value = None, shareValue = None, isOwned = Some(true), isOwnedShare = Some(true)))

  val ownedAmount = 1234.0
  val ownedAmountDisplay = "£1,234.00"
  val jointAmount = 2345.0
  val jointAmountDisplay = "£2,345.00"

  val dataWithValues =
    Some(ShareableBasicEstateElement(value = Some(ownedAmount), shareValue = Some(jointAmount),
      isOwned = Some(true), isOwnedShare = Some(true)))

  def fixture(data: Option[ShareableBasicEstateElement]) = new {
    implicit val request: FakeRequest[AnyContentAsEmpty.type] = createFakeRequest()
    val view: String = ""
    val doc: Document = new Document("")
  }

  def overviewView() = {

    "have the correct title" in {
      val f = fixture(dataWithQuestionsAnsweredNo)
      titleShouldBeCorrect(f.view, pageTitle)
    }

    "have the correct browser title" in {
      val f = fixture(dataWithQuestionsAnsweredNo)
      browserTitleShouldBeCorrect(f.view, browserTitle)
    }

    "show the correct guidance paragraphs" in {
      val f = fixture(dataWithQuestionsAnsweredNo)
      for (paragraph <- guidanceParagraphs) messagesShouldBePresent(f.view, paragraph)
    }

    "show the correct return link with right text" in {
      val f = fixture(dataWithQuestionsAnsweredNo)
      val returnLink = f.doc.getElementById("return-button")
      returnLink.attr("href") shouldBe iht.controllers.application.assets.routes.AssetsOverviewController.onPageLoad().url
      returnLink.text() shouldBe Messages("page.iht.application.return.to.assetsOf",deceasedName)
    }
  }

  def overviewViewWithQuestionsUnanswered() = {
    "show the 'owned only by the deceased' question header as being unanswered with a link to give details" in {
      val f = fixture(None)
      headerQuestionShouldBeUnanswered(f.doc, ownHeadingElementId, ownHeaderText, urlToOwnPage)
    }

    "show the 'jointly owned' question header as being unanswered with a link to give details" in {
      val f = fixture(None)
      headerQuestionShouldBeUnanswered(f.doc, jointlyOwnedHeadingElementId, jointlyOwnedHeaderText, urlToJointlyOwnedPage)
    }

    "not show the 'owned only by the deceased' question row" in {
      val f = fixture(None)
      assertNotRenderedById(f.doc, ownQuestionRowId)
    }

    "not show the 'value owned only be the deceased' row" in {
      val f = fixture(None)
      assertNotRenderedById(f.doc, ownValueRowId)
    }

    "not show the 'jointly owned' question row" in {
      val f = fixture(None)
      assertNotRenderedById(f.doc, jointlyOwnedQuestionRowId)
    }

    "not show the 'value jointly owned' row" in {
      val f = fixture(None)
      assertNotRenderedById(f.doc, jointlyOwnedValueRowId)
    }
  }

  def overviewViewWithQuestionsAnsweredNo() = {

    "show the 'only owned by the deceased' question header as being answered with no link" in {
      val f = fixture(dataWithQuestionsAnsweredNo)
      headerShouldBeAnswered(f.doc, ownHeadingElementId, ownHeaderText)
    }

    "show the 'jointly owned' question header as being answered with no link" in {
      val f = fixture(dataWithQuestionsAnsweredNo)
      headerShouldBeAnswered(f.doc, jointlyOwnedHeadingElementId, jointlyOwnedHeaderText)
    }

    "show the 'owned only by the deceased' question row with an answer of No" in {
      val f = fixture(dataWithQuestionsAnsweredNo)
      rowShouldBeAnswered(f.doc, ownQuestionRowId, ownQuestionText, "No", "iht.change", urlToOwnPage)
    }

    "not show the 'value owned only be the deceased' row" in {
      val f = fixture(dataWithQuestionsAnsweredNo)
      assertNotRenderedById(f.doc, ownValueRowId)
    }

    "show the 'jointly owned' question row with an answer of No" in {
      val f = fixture(dataWithQuestionsAnsweredNo)
      rowShouldBeAnswered(f.doc, jointlyOwnedQuestionRowId, jointlyOwnedQuestionText, "No", "iht.change", urlToJointlyOwnedPage)
    }

    "not show the 'value jointly owned' row" in {
      val f = fixture(dataWithQuestionsAnsweredNo)
      assertNotRenderedById(f.doc, jointlyOwnedValueRowId)
    }
  }

  def overviewViewWithQuestionsAnsweredYes() = {

    "show the 'only owned by the deceased' question header as being answered with no link" in {
      val f = fixture(dataWithQuestionsAnsweredYes)
      headerShouldBeAnswered(f.doc, ownHeadingElementId, ownHeaderText)
    }

    "show the 'jointly owned' question header as being answered with no link" in {
      val f = fixture(dataWithQuestionsAnsweredYes)
      headerShouldBeAnswered(f.doc, jointlyOwnedHeadingElementId, jointlyOwnedHeaderText)
    }

    "show the 'owned only by the deceased' question row with an answer of Yes" in {
      val f = fixture(dataWithQuestionsAnsweredYes)
      rowShouldBeAnswered(f.doc, ownQuestionRowId, ownQuestionText, "Yes", "iht.change", urlToOwnPage)
    }

    "show the 'value owned only be the deceased' row as unanswered" in {
      val f = fixture(dataWithQuestionsAnsweredYes)
      rowShouldBeAnswered(f.doc, ownValueRowId, ownValueText, "", "site.link.giveAValue", urlToOwnPage)
    }

    "show the 'jointly owned' question row with an answer of Yes" in {
      val f = fixture(dataWithQuestionsAnsweredYes)
      rowShouldBeAnswered(f.doc, jointlyOwnedQuestionRowId, jointlyOwnedQuestionText, "Yes", "iht.change", urlToJointlyOwnedPage)
    }

    "show the 'value jointly owned' row as unanswered" in {
      val f = fixture(dataWithQuestionsAnsweredYes)
      rowShouldBeAnswered(f.doc, jointlyOwnedValueRowId, jointlyOwnedValueText, "", "site.link.giveAValue", urlToJointlyOwnedPage)
    }
  }

  def overviewViewWithValues() = {

    "show the 'only owned by the deceased' question header as being answered with no link" in {
      val f = fixture(dataWithValues)
      headerShouldBeAnswered(f.doc, ownHeadingElementId, ownHeaderText)
    }

    "show the 'jointly owned' question header as being answered with no link" in {
      val f = fixture(dataWithValues)
      headerShouldBeAnswered(f.doc, jointlyOwnedHeadingElementId, jointlyOwnedHeaderText)
    }

    "show the 'owned only by the deceased' question row with an answer of Yes" in {
      val f = fixture(dataWithValues)
      rowShouldBeAnswered(f.doc, ownQuestionRowId, ownQuestionText, "Yes", "iht.change", urlToOwnPage)
    }

    "show the 'value owned only by the deceased' row a value" in {
      val f = fixture(dataWithValues)
      rowShouldBeAnswered(f.doc, ownValueRowId, ownValueText, ownedAmountDisplay, "iht.change", urlToOwnPage)
    }

    "show the 'jointly owned' question row with an answer of Yes" in {
      val f = fixture(dataWithValues)
      rowShouldBeAnswered(f.doc, jointlyOwnedQuestionRowId, jointlyOwnedQuestionText, "Yes", "iht.change", urlToJointlyOwnedPage)
    }

    "show the 'value jointly owned' row with a value" in {
      val f = fixture(dataWithValues)
      rowShouldBeAnswered(f.doc, jointlyOwnedValueRowId, jointlyOwnedValueText, jointAmountDisplay, "iht.change", urlToJointlyOwnedPage)
    }
  }
}

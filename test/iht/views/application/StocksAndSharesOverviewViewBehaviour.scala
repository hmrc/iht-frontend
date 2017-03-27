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

import iht.models.application.assets.StockAndShare
import iht.views.ViewTestHelper
import iht.views.helpers.GenericOverviewHelper._
import org.jsoup.nodes.Document
import play.api.i18n.Messages
import play.api.i18n.Messages.Implicits._
import play.api.Play.current
import play.api.mvc.AnyContentAsEmpty
import play.api.test.FakeRequest

trait StocksAndSharesOverviewViewBehaviour extends ViewTestHelper {

  def pageTitle: String
  def browserTitle: String
  def guidanceParagraphs: Set[String]
  def stocksAndSharesListedHeadingElementId: String
  def stocksAndSharesNotListedHeadingElementId: String
  def urlToStocksAnsSharesListedPage: String
  def urlToStocksAnsSharesNotListedPage: String
  def stocksAndSharesListedHeaderText: String
  def stocksAndSharesNotListedHeaderText: String
  def stocksAndSharesListedRowId: String
  def stocksAndSharesListedQuestionText: String
  def stocksAndSharesListedValueRowId: String
  def stocksAndSharesListedValueText: String
  def stocksAndSharesNotListedQuestionRowId: String
  def stocksAndSharesNotListedQuestionText: String
  def stocksAndSharesNotListedValueRowId: String
  def stocksAndSharesNotListedValueText: String
  def deceasedName: String
  def linkHash: String = ""

  val dataWithQuestionsAnsweredNo =
    Some(StockAndShare(valueNotListed = None, valueListed = None,value = None, isNotListed = Some(false), isListed = Some(false)))

  val dataWithQuestionsAnsweredYes =
    Some(StockAndShare(valueNotListed = None, valueListed = None,value = None, isNotListed = Some(true), isListed = Some(true)))

  val stocksListedAmount = 1234.0
  val stocksListedAmountDisplay = "£1,234.00"
  val stocksNotListedAmount = 2345.0
  val stocksNotListedAmountDisplay = "£2,345.00"

  val dataWithValues =
    Some(StockAndShare(valueListed = Some(stocksListedAmount), valueNotListed = Some(stocksNotListedAmount), value = None,
      isNotListed = Some(true), isListed = Some(true)))

  def fixture(data: Option[StockAndShare]) = new {
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
      returnLink.attr("href") shouldBe iht.controllers.application.assets.routes.AssetsOverviewController.onPageLoad().url + "#" + linkHash
      returnLink.text() shouldBe messagesApi("page.iht.application.return.to.assetsOf",deceasedName)
    }
  }

  def overviewViewWithQuestionsUnanswered() = {
    "show the 'Stocks and shares listed on an exchange' question header as being unanswered with a link to give details" in {
      val f = fixture(None)
      headerQuestionShouldBeUnanswered(f.doc,
        stocksAndSharesListedHeadingElementId, stocksAndSharesListedHeaderText, urlToStocksAnsSharesListedPage)
    }

    "show the 'Stocks and shares not listed on an exchange' question header as being unanswered with a link to give details" in {
      val f = fixture(None)
      headerQuestionShouldBeUnanswered(f.doc,
        stocksAndSharesNotListedHeadingElementId, stocksAndSharesNotListedHeaderText, urlToStocksAnsSharesNotListedPage)
    }

    "not show the 'Stocks and shares listed on an exchange' question row" in {
      val f = fixture(None)
      assertNotRenderedById(f.doc, s"$stocksAndSharesListedRowId-block")
    }

    "not show the 'Stocks and shares listed on an exchange' row" in {
      val f = fixture(None)
      assertNotRenderedById(f.doc, stocksAndSharesListedValueRowId)
    }

    "not show the 'Stocks and shares not listed on an exchange' question row" in {
      val f = fixture(None)
      assertNotRenderedById(f.doc, s"$stocksAndSharesNotListedQuestionRowId-block")
    }

    "not show the 'Stocks and shares not listed on an exchange' row" in {
      val f = fixture(None)
      assertNotRenderedById(f.doc, stocksAndSharesNotListedValueRowId)
    }
  }

  def overviewViewWithQuestionsAnsweredNo() = {

    "show the 'Stocks and shares listed on an exchange' question header as being answered with no link" in {
      val f = fixture(dataWithQuestionsAnsweredNo)
      headerShouldBeAnswered(f.doc, stocksAndSharesListedHeadingElementId, stocksAndSharesListedHeaderText)
    }

    "show the 'Stocks and shares not listed on an exchange' question header as being answered with no link" in {
      val f = fixture(dataWithQuestionsAnsweredNo)
      headerShouldBeAnswered(f.doc, stocksAndSharesNotListedHeadingElementId, stocksAndSharesNotListedHeaderText)
    }

    "show the 'Stocks and shares listed on an exchange' question row with an answer of No" in {
      val f = fixture(dataWithQuestionsAnsweredNo)
      rowShouldBeAnswered(f.doc,
        stocksAndSharesListedRowId, stocksAndSharesListedQuestionText, "No", "iht.change", urlToStocksAnsSharesListedPage)
    }

    "not show the 'value of listed stocks and shares' row" in {
      val f = fixture(dataWithQuestionsAnsweredNo)
      assertNotRenderedById(f.doc, stocksAndSharesListedValueRowId)
    }

    "show the 'Stocks and shares not listed on an exchange' question row with an answer of No" in {
      val f = fixture(dataWithQuestionsAnsweredNo)
      rowShouldBeAnswered(f.doc, stocksAndSharesNotListedQuestionRowId,
        stocksAndSharesNotListedQuestionText, "No", "iht.change", urlToStocksAnsSharesNotListedPage)
    }

    "not show the 'value of stocks and shares not listed' row" in {
      val f = fixture(dataWithQuestionsAnsweredNo)
      assertNotRenderedById(f.doc, stocksAndSharesNotListedValueRowId)
    }
  }

  def overviewViewWithQuestionsAnsweredYes() = {

    "show the 'Stocks and shares listed on an exchange' question header as being answered with no link" in {
      val f = fixture(dataWithQuestionsAnsweredYes)
      headerShouldBeAnswered(f.doc, stocksAndSharesListedHeadingElementId, stocksAndSharesListedHeaderText)
    }

    "show the 'Stocks and shares not listed on an exchange' question header as being answered with no link" in {
      val f = fixture(dataWithQuestionsAnsweredYes)
      headerShouldBeAnswered(f.doc, stocksAndSharesNotListedHeadingElementId, stocksAndSharesNotListedHeaderText)
    }

    "show the 'Stocks and shares listed on an exchange' question row with an answer of Yes" in {
      val f = fixture(dataWithQuestionsAnsweredYes)
      rowShouldBeAnswered(f.doc, stocksAndSharesListedRowId,
        stocksAndSharesListedQuestionText, "Yes", "iht.change", urlToStocksAnsSharesListedPage)
    }

    "show the 'value of listed stocks and shares' row as unanswered" in {
      val f = fixture(dataWithQuestionsAnsweredYes)

      rowShouldBeAnswered(f.doc, stocksAndSharesListedValueRowId,
        stocksAndSharesListedValueText, "", "site.link.giveAValue", urlToStocksAnsSharesListedPage)
    }

    "show the 'Stocks and shares not listed on an exchange' question row with an answer of Yes" in {
      val f = fixture(dataWithQuestionsAnsweredYes)
      rowShouldBeAnswered(f.doc, stocksAndSharesNotListedQuestionRowId,
        stocksAndSharesNotListedQuestionText, "Yes", "iht.change", urlToStocksAnsSharesNotListedPage)
    }

    "show the 'value of stocks and shares not listed' row as unanswered" in {
      val f = fixture(dataWithQuestionsAnsweredYes)

      rowShouldBeAnswered(f.doc, stocksAndSharesNotListedValueRowId,
        stocksAndSharesNotListedValueText, "", "site.link.giveAValue", urlToStocksAnsSharesNotListedPage)
    }
  }

  def overviewViewWithValues() = {

    "show the 'Stocks and shares listed on an exchange' question header as being answered with no link" in {
      val f = fixture(dataWithValues)
      headerShouldBeAnswered(f.doc, stocksAndSharesListedHeadingElementId, stocksAndSharesListedHeaderText)
    }

    "show the 'Stocks and shares not listed on an exchange' question header as being answered with no link" in {
      val f = fixture(dataWithValues)
      headerShouldBeAnswered(f.doc, stocksAndSharesNotListedHeadingElementId, stocksAndSharesNotListedHeaderText)
    }

    "show the 'Stocks and shares listed on an exchange' question row with an answer of Yes" in {
      val f = fixture(dataWithValues)
      rowShouldBeAnswered(f.doc, stocksAndSharesListedRowId,
        stocksAndSharesListedQuestionText, "Yes", "iht.change", urlToStocksAnsSharesListedPage)
    }

    "show the 'value of listed stocks and shares' row a value" in {
      val f = fixture(dataWithValues)
      rowShouldBeAnswered(f.doc, stocksAndSharesListedValueRowId,
        stocksAndSharesListedValueText, stocksListedAmountDisplay, "iht.change", urlToStocksAnsSharesListedPage)
    }

    "show the 'Stocks and shares not listed on an exchange' question row with an answer of Yes" in {
      val f = fixture(dataWithValues)
      rowShouldBeAnswered(f.doc, stocksAndSharesNotListedQuestionRowId,
        stocksAndSharesNotListedQuestionText, "Yes", "iht.change", urlToStocksAnsSharesNotListedPage)
    }

    "show the 'value of stocks and shares not listed' row with a value" in {
      val f = fixture(dataWithValues)
      rowShouldBeAnswered(f.doc, stocksAndSharesNotListedValueRowId,
        stocksAndSharesNotListedValueText, stocksNotListedAmountDisplay, "iht.change", urlToStocksAnsSharesNotListedPage)
    }
  }
}

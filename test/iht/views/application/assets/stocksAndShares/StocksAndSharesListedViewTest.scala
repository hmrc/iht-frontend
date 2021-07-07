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

package iht.views.application.assets.stocksAndShares

import iht.controllers.application.assets.stocksAndShares.routes
import iht.forms.ApplicationForms._
import iht.models.application.assets.StockAndShare
import iht.testhelpers.CommonBuilder
import iht.views.application.ShareableElementInputViewBehaviour
import iht.views.html.application.asset.stocksAndShares.stocks_and_shares_listed
import play.api.data.Form
import play.twirl.api.HtmlFormat.Appendable

class StocksAndSharesListedViewTest extends ShareableElementInputViewBehaviour[StockAndShare] {

  lazy val regDetails = CommonBuilder.buildRegistrationDetails1
  lazy val deceasedName = regDetails.deceasedDetails.fold("")(x => x.name)

  override def pageTitle = messagesApi("iht.estateReport.assets.stocksAndSharesListed")
  override def browserTitle = messagesApi("page.iht.application.assets.stocksAndShares.listed.browserTitle")

  override def questionTitle = messagesApi("iht.estateReport.assets.stocksAndShares.listed.question", deceasedName)
  override def valueQuestion = messagesApi("iht.estateReport.assets.stocksAndShares.valueOfListed")
  override def hasValueQuestionHelp = false
  override def valueQuestionHelp = ""
  override def valueInputBoxId = "valueListed"
  override def returnLinkText = messagesApi("site.link.return.stocksAndShares")
  override def returnLinkUrl = routes.StocksAndSharesOverviewController.onPageLoad().url
  override def linkHash = appConfig.AssetsStocksListedID
  override def formTarget = Some(routes.StocksAndSharesListedController.onSubmit())
  override def form: Form[StockAndShare] = stockAndShareListedForm
  lazy val stocksAndSharesListedView: stocks_and_shares_listed = app.injector.instanceOf[stocks_and_shares_listed]

  override def formToView: Form[StockAndShare] => Appendable =
    form => stocksAndSharesListedView(form, regDetails)


  "Stocks and Shares Listed View" must {
    behave like yesNoValueViewWithErrorSummaryBox
  }
}

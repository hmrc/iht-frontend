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

package iht.views.application.overview

import iht.testhelpers.CommonBuilder
import iht.viewmodels.application.overview.ReducingEstateValueSectionViewModel
import iht.views.HtmlSpec
import iht.views.html.application.overview.reducing_estate_value_section
import iht.{FakeIhtApp, TestUtils}
import org.jsoup.select.Elements
import org.scalatest.BeforeAndAfter
import org.scalatest.mock.MockitoSugar
import play.api.i18n.Messages
import play.api.i18n.Messages.Implicits._
import play.api.Play.current
import uk.gov.hmrc.play.test.UnitSpec

class ReducingEstateValueSectionViewTest extends UnitSpec with FakeIhtApp with MockitoSugar with TestUtils with HtmlSpec with BeforeAndAfter {

  def appDetails = CommonBuilder.buildApplicationDetails
  def appDetailsWithSomeExemptionsAndLiabilities = CommonBuilder.buildSomeExemptions(CommonBuilder.buildApplicationDetails) copy (
                                                    allLiabilities = Some(CommonBuilder.buildEveryLiability))
  def regDetails = CommonBuilder.buildRegistrationDetails4
  def viewModel = ReducingEstateValueSectionViewModel(appDetailsWithSomeExemptionsAndLiabilities, regDetails)


  "reducing the estate value section" must {
    implicit val request = createFakeRequest()

    "have the correct title" in {
      val view = reducing_estate_value_section(viewModel)
      val doc = asDocument(view)
      val header = doc.getElementsByTag("h2")
      header.text() should include(Messages("page.iht.application.overview.reducingTheEstateValue.total"))
    }

    "contain the Exemptions row" in {
      val view = reducing_estate_value_section(viewModel)
      val doc = asDocument(view)
      assertRenderedById(doc, "exemptions-row")
    }

    "contain the totals row" in {
      val view = reducing_estate_value_section(viewModel)
      val doc = asDocument(view)
      assertRenderedById(doc, "reducing-estate-totals-row")
    }

    "contain the Debts row if there are debts in the model" in {
      val view = reducing_estate_value_section(viewModel)
      val doc = asDocument(view)
      assertRenderedById(doc, "debts-row")
    }

    "does not contain the Debts row if there are no values for exemptions" in {
      val appDetails1 = CommonBuilder.buildExemptionsWithNoValues(appDetails) copy (
                                                          allLiabilities = Some(CommonBuilder.buildEveryLiability))
      val viewModel = ReducingEstateValueSectionViewModel(appDetails1, regDetails)
      val view = reducing_estate_value_section(viewModel)
      val doc = asDocument(view)
      assertNotRenderedById(doc, "debts-row")
    }

    "does not contain the Debts row if there are values equaling zero for exemptions" in {
      val appDetails1 = CommonBuilder.buildExemptionsWithPartnerZeroValue(appDetails) copy (
                                                           allLiabilities = Some(CommonBuilder.buildEveryLiability))
      val viewModel = ReducingEstateValueSectionViewModel(appDetails1, regDetails)
      val view = reducing_estate_value_section(viewModel)
      val doc = asDocument(view)
      assertNotRenderedById(doc, "debts-row")
    }
  }
}

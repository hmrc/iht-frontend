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
import iht.views.ViewTestHelper
import iht.views.html.application.overview.reducing_estate_value_section
import play.api.i18n.Messages.Implicits._

class ReducingEstateValueSectionViewTest extends ViewTestHelper {

  def appDetails = CommonBuilder.buildApplicationDetails
  def appDetailsWithSomeExemptionsAndLiabilities = CommonBuilder.buildSomeExemptions(CommonBuilder.buildApplicationDetails) copy (
                                                    allLiabilities = Some(CommonBuilder.buildEveryLiability))
  def regDetails = CommonBuilder.buildRegistrationDetails4
  def viewModel = ReducingEstateValueSectionViewModel(appDetailsWithSomeExemptionsAndLiabilities, regDetails)

  "reducing the estate value section" must {

    "have the correct title" in {
      implicit val request = createFakeRequest()
      val view = reducing_estate_value_section(viewModel)
      val doc = asDocument(view)
      val header = doc.getElementsByTag("h2")
      header.text() should include(messagesApi("page.iht.application.overview.reducingTheEstateValue.total"))
    }

    "contain the Exemptions row" in {
      implicit val request = createFakeRequest()
      val view = reducing_estate_value_section(viewModel)
      val doc = asDocument(view)
      assertRenderedById(doc, "exemptions-row")
    }

    "contain the totals row" in {
      implicit val request = createFakeRequest()
      val view = reducing_estate_value_section(viewModel)
      val doc = asDocument(view)
      assertRenderedById(doc, "reducing-estate-totals-row")
    }

    "contain the Debts row if there are debts in the model" in {
      implicit val request = createFakeRequest()
      val view = reducing_estate_value_section(viewModel)
      val doc = asDocument(view)
      assertRenderedById(doc, "debts-row")
    }

    "does not contain the Debts row if there are no values for exemptions" in {
      implicit val request = createFakeRequest()
      val appDetails1 = CommonBuilder.buildExemptionsWithNoValues(appDetails) copy (
                                                          allLiabilities = Some(CommonBuilder.buildEveryLiability))
      val viewModel = ReducingEstateValueSectionViewModel(appDetails1, regDetails)
      val view = reducing_estate_value_section(viewModel)
      val doc = asDocument(view)
      assertNotRenderedById(doc, "debts-row")
    }

    "does not contain the Debts row if there are values equaling zero for exemptions" in {
      implicit val request = createFakeRequest()
      val appDetails1 = CommonBuilder.buildExemptionsWithPartnerZeroValue(appDetails) copy (
                                                           allLiabilities = Some(CommonBuilder.buildEveryLiability))
      val viewModel = ReducingEstateValueSectionViewModel(appDetails1, regDetails)
      val view = reducing_estate_value_section(viewModel)
      val doc = asDocument(view)
      assertNotRenderedById(doc, "debts-row")
    }
  }
}

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

package iht.views.application.debts

import iht.constants.FieldMappings
import iht.testhelpers.{CommonBuilder, TestHelper}
import iht.utils.CommonHelper
import iht.views.ViewTestHelper
import play.api.i18n.Messages
import play.api.test.Helpers._
import iht.views.html.application.debts.mortgages_overview
import iht.constants.Constants._

/**
  * Created by vineet on 15/11/16.
  */
class MortgagesOverviewViewTest extends ViewTestHelper{

  val ihtReference = Some("ABC1A1A1A")
  val regDetails = CommonBuilder.buildRegistrationDetails.copy(ihtReference = ihtReference,
    deceasedDetails = Some(CommonBuilder.buildDeceasedDetails.copy(
      maritalStatus = Some(TestHelper.MaritalStatusMarried))),
    deceasedDateOfDeath = Some(CommonBuilder.buildDeceasedDateOfDeath))

  val fakeRequest = createFakeRequest(isAuthorised = false)
  val debtsOverviewPageUrl= iht.controllers.application.debts.routes.DebtsOverviewController.onPageLoad()

  private def mortgageOverview() = {
    val returnLinkText = Messages("site.link.return.debts")
    val view = mortgages_overview(Nil, Nil, FieldMappings.typesOfOwnership,
      regDetails, debtsOverviewPageUrl, returnLinkText)(fakeRequest)

    contentAsString(view)
  }

  "MortgagesOverview Page" must {

    "contain the title, browser title " in {
      val view = mortgageOverview
      val doc = asDocument(view)

      titleShouldBeCorrect(view, Messages("iht.estateReport.debts.mortgages"))
      browserTitleShouldBeCorrect(view, Messages("iht.estateReport.debts.mortgages"))
    }

    "contain the correct guidance" in {
      val view = mortgageOverview

      messagesShouldBePresent(view, Messages("page.iht.application.debts.mortgages.description.p1"))
      messagesShouldBePresent(view, Messages("page.iht.application.debts.mortgages.description",
                                                CommonHelper.getDeceasedNameOrDefaultString(regDetails)))
      messagesShouldBePresent(view, Messages("page.iht.application.debts.mortgages.description.p3",
                                                CommonHelper.getDeceasedNameOrDefaultString(regDetails)))
    }

   "show the correct text and link for the return link" in {
      val view = mortgageOverview
      val doc = asDocument(view)

      val link = doc.getElementById("return-button")
      link.text shouldBe Messages("site.link.return.debts")
      link.attr("href") shouldBe debtsOverviewPageUrl.url + "#" + DebtsMortgagesID
    }
  }
}

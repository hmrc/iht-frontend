/*
 * Copyright 2022 HM Revenue & Customs
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

package iht.views.filter

import iht.views.ViewTestHelper
import iht.views.html.filter.use_checker
import play.api.test.Helpers.{contentAsString, _}

class UseCheckerView extends ViewTestHelper {

  val fakeRequest = createFakeRequest(isAuthorised = false)
  lazy val useCheckerView: use_checker = app.injector.instanceOf[use_checker]

  "Use Checker view" must {
    "have no message keys in html" in {
      val result = useCheckerView()(fakeRequest, messages)
      val view = asDocument(contentAsString(result)).toString
      noMessageKeysShouldBePresent(view)
    }

    "display the correct title" in {
      val result = useCheckerView()(fakeRequest, messages)
      val doc = asDocument(contentAsString(result))
      val title = doc.getElementsByTag("h1").first
      title.text must be(messagesApi("page.iht.filter.useChecker.heading"))
    }

    "display the correct browser title" in {
      val result = useCheckerView()(fakeRequest, messages)
      val doc = asDocument(contentAsString(result))
      val browserTitle = doc.getElementsByTag("title").first
      browserTitle.text must be(messagesApi("page.iht.filter.useChecker.title") + " – Register to complete an Inheritance Tax estate report – GOV.UK")
    }

    "contain a 'Previous answers' section" in {
      val result = useCheckerView()(fakeRequest, messages)
      val doc = asDocument(contentAsString(result))
      assertRenderedById(doc, "previous-answers")
    }

    "contain a 'Start again' link to go back to the domicile page" in {
      val result = useCheckerView()(fakeRequest, messages)
      val doc = asDocument(contentAsString(result))
      val link = doc.getElementById("start-again")
      link.text() must be(messagesApi("iht.startAgain"))
      link.attr("href") must be(iht.controllers.filter.routes.DomicileController.onPageLoad().url)
    }

    "contain a row showing the user's answer to the previous deceasedBefore2022 question" in {
      val result = useCheckerView()(fakeRequest, messages)
      val doc = asDocument(contentAsString(result))
      val row = doc.getElementById("deceased-before-2022-row")
      row.text() must include(messagesApi("page.iht.filter.deceased.before.2022.heading"))
      row.text() must include(messagesApi("iht.no"))
    }

    "contain a 'Change' link to go back to the deceasedBefore2022 page" in {
      val result = useCheckerView()(fakeRequest, messages)
      val doc = asDocument(contentAsString(result))
      val link = doc.getElementById("change-deceased-before-2022")
      link.text() must include(messagesApi("iht.change"))
      link.attr("href") must be(iht.controllers.filter.routes.DeceasedBefore2022Controller.onPageLoad().url)
    }

    "contain a row showing the user's answer to the previous domicile question" in {
      val result = useCheckerView()(fakeRequest, messages)
      val doc = asDocument(contentAsString(result))
      val row = doc.getElementById("domicile-row")
      row.text() must include(messagesApi("page.iht.registration.deceasedPermanentHome.title"))
      row.text() must include(messagesApi("iht.countries.englandOrWales"))
    }

    "contain a 'Change' link to go back to the domicile page" in {
      val result = useCheckerView()(fakeRequest, messages)
      val doc = asDocument(contentAsString(result))
      val link = doc.getElementById("change-domicile")
      link.text() must include(messagesApi("iht.change"))
      link.attr("href") must be(iht.controllers.filter.routes.DomicileController.onPageLoad().url)
    }
  }
}

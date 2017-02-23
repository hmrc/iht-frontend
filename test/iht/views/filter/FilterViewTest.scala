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

package iht.views.filter

import iht.FakeIhtApp
import iht.views.HtmlSpec
import iht.views.html.filter.filter_view
import play.api.test.Helpers._
import play.api.i18n.Messages.Implicits._
import uk.gov.hmrc.play.test.UnitSpec
import iht.forms.FilterForms.filterForm
import org.jsoup.nodes.Element

/**
  * Created by adwelly on 20/10/2016.
  */
class FilterViewTest extends UnitSpec with FakeIhtApp with HtmlSpec {

  val fakeRequest = createFakeRequest(isAuthorised = false)

  "filter_view" must {
    "generate appropriate content for the title" in {
      val result = filter_view(filterForm)(fakeRequest, applicationMessages)
      val doc = asDocument(contentAsString(result))
      val titleElement = doc.getElementsByTag("h1").first

      titleElement.text should include(messagesApi("iht.whatDoYouWantToDo"))
    }

    "generate appropriate content for the browser title" in {
      val result = filter_view(filterForm)(fakeRequest, applicationMessages)
      val doc = asDocument(contentAsString(result))
      val titleElement = doc.getElementsByTag("title").first

      titleElement.text should include(messagesApi("iht.whatDoYouWantToDo"))
    }

    "contain an appropriate field set" in {
      val result = filter_view(filterForm)(fakeRequest, applicationMessages)
      val doc = asDocument(contentAsString(result))
      val fieldSet = doc.getElementsByTag("fieldset")
      val id = fieldSet.attr("id")
      id should be("filter-choices-container")
    }

    "contain the first radio button with the text 'I want to continue an estate report that I've already started' and no hint" in {
      val result = filter_view(filterForm)(fakeRequest, applicationMessages)
      val doc = asDocument(contentAsString(result))
      val mainSpan = doc.getElementById("filter-choices-continue-main")
      mainSpan.text() should be(messagesApi("page.iht.filter.filter.choice.main.continue"))
    }

    "contain the first radio button without a hint" in {
      pending
    }

    "contain the second radio button with the text 'I want to register so I can tell HMRC about a person's estate" in {
      val result = filter_view(filterForm)(fakeRequest, applicationMessages)
      val doc = asDocument(contentAsString(result))
      val mainElement = doc.getElementById("filter-choices-register-main")
      mainElement.text() should be(messagesApi("page.iht.filter.filter.choice.main.register"))
    }

    "contains a second button with the hint 'You’ll be asked a couple of questions first to make sure you’re using the right service.'" in {
      val result = filter_view(filterForm)(fakeRequest, applicationMessages)
      val doc = asDocument(contentAsString(result))
      val hintElement = doc.getElementById("filter-choices-register-hint")
      hintElement.text() should be(messagesApi("page.iht.filter.filter.choice.main.register.hint"))
    }

    "contain the third radio button with the text 'I've already started registration and want to continue'" in {
      val result = filter_view(filterForm)(fakeRequest, applicationMessages)
      val doc = asDocument(contentAsString(result))

      assertEqualsMessage(doc, "label#filter-choices-already-started-label > div", "page.iht.filter.filter.choice.main.alreadyStarted")
    }

    "contain the third radio button without a hint" in {
      pending
    }

    "contain the fourth radio button with the text 'I'm an agent and reporting on behalf of a client'" in {
      val result = filter_view(filterForm)(fakeRequest, applicationMessages)
      val doc = asDocument(contentAsString(result))

      assertEqualsMessage(doc, "label#filter-choices-agent-label > div", "page.iht.filter.filter.choice.main.agent")
    }

    "contain the fourth radio button without a hint" in {
      pending
    }

    "contain a continue button with the text 'Continue'" in {
      val result = filter_view(filterForm)(fakeRequest, applicationMessages)
      val doc = asDocument(contentAsString(result))
      val button = doc.select("input#continue").first

      button.attr("value") should be(messagesApi("iht.continue"))
    }

    "contain a form with the action attribute set to the FilterController onSubmit URL" in {
      val result = filter_view(filterForm)(fakeRequest, applicationMessages)
      val doc = asDocument(contentAsString(result))
      val formElement = doc.getElementsByTag("form").first

      formElement.attr("action") should be(iht.controllers.filter.routes.FilterController.onSubmit().url)
    }
  }
}

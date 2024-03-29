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

package iht.views.application

import iht.views.ViewTestHelper
import iht.views.html.application.non_lead_executor
import play.api.i18n.{Lang, Messages}

class NonLeadExecutorViewTest extends ViewTestHelper {

  implicit val request = createFakeRequest()
  lazy val nonLeadExecutorView: non_lead_executor = app.injector.instanceOf[non_lead_executor]

  lazy val view = nonLeadExecutorView()
  lazy val doc = asDocument(view.body)

  class WelshView {

    implicit val lang = Lang("cy")
    implicit val messages: Messages = mockControllerComponents.messagesApi.preferred(Seq(lang)).messages
    lazy val view = nonLeadExecutorView()
    lazy val doc = asDocument(view.body)
  }

  "NonLeadExecutor view" when {

    "rendered in English" must {
      "have the correct title" in {
        doc.title() mustBe "This estate was registered by a different user"
      }

      "have a single heading" in {
        lazy val heading = doc.select("h1")

        heading.size() mustBe 1
        heading.text() mustBe "This estate was registered by a different user"
      }

      "have two paragraphs with the correct content" in {
        doc.select("article > p").size() mustBe 2
        doc.select("article > p").get(0).text() mustBe
          "The estate report is complete and can now be submitted. You need to ask the executor that registered this estate to sign in and submit it to HMRC."
        doc.select("article > p").get(1).text() mustBe
          "If you are not sure who registered this estate, use the link below to contact the IHT help desk."
      }

      "must have a signout button" in {
        lazy val signOut = doc.select("a.button")
        signOut.text mustBe "Sign out"
        signOut.attr("href") mustBe "/inheritance-tax/signout"
      }
    }

    "rendered in Welsh" must {
      "have the correct title" in new WelshView {
        doc.title() mustBe "Cofrestrwyd yr ystâd hon gan ddefnyddiwr gwahanol"
      }

      "have a single heading" in new WelshView {
        lazy val heading = doc.select("h1")

        heading.size() mustBe 1
        heading.text() mustBe "Cofrestrwyd yr ystâd hon gan ddefnyddiwr gwahanol"
      }

      "have two paragraphs with the correct content" in new WelshView {
        doc.select("article > p").size() mustBe 2
        doc.select("article > p").get(0).text() mustBe messages("page.application.kickout.nonLeadExec.para1")
        doc.select("article > p").get(1).text() mustBe messages("page.application.kickout.nonLeadExec.para2")
      }

      "must have a signout button" in new WelshView{
        lazy val signOut = doc.select("a.button")
        signOut.text mustBe "Allgofnodi"
        signOut.attr("href") mustBe "/inheritance-tax/signout"
      }
    }
  }
}

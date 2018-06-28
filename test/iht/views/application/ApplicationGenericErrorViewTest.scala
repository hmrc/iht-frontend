/*
 * Copyright 2018 HM Revenue & Customs
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
import iht.views.helpers.MessagesHelper
import iht.views.html.application.timeout_application
import org.jsoup.Jsoup
import play.api.i18n.Messages.Implicits._
import play.api.test.FakeRequest

class ApplicationGenericErrorViewTest extends ViewTestHelper with MessagesHelper {

  implicit lazy val fakeRequest = FakeRequest()
  lazy val view = iht.views.html.application.application_generic_error()
  lazy val doc = Jsoup.parse(view.body)

  "ApplicationGenericError" must {

    "have the correct title" in {
      doc.title() shouldBe pageIhtIVFailureTechnicalIssueHeading

    }
    "have the correct first paragraph" in {
      doc.select("p").eq(2).text shouldBe errorApplicationSystemErrorp1
    }

    "have the correct second paragraph" in {
      doc.select("p").eq(3).text shouldBe ihtApplicationTimeoutp1
    }

    "have the correct third paragraph" in {
      doc.select("p").eq(4).text shouldBe pageIhtIVFailureYouCanAlso
    }

    "have a list" that {
      "have first entry of" in {
        doc.select("ul > li").eq(1).text shouldBe pageIhtIVFailureReportWithPaperForm
      }

      "have second entry of" in {
        doc.select("ul > li").eq(2).text shouldBe pageIhtIVFailureaskForHelp
      }
    }
  }

}

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

package iht.views

import iht.views.html.private_beta_landing_page

/**
  * Created by vineet on 15/11/16.
  */

class PrivateBetaLandingPageViewTest extends ViewTestHelper{

  def doc  = {

    implicit val request = createFakeRequest()
    asDocument(private_beta_landing_page()(request, messagesApi.preferred(request), formPartialRetriever).toString())
  }

  "PrivateBetaLandingPageView" must {

    "have the correct title and browser title" in {

      titleShouldBeCorrect(doc.toString, messagesApi("site.application.title"))
      browserTitleShouldBeCorrect(doc.toString, messagesApi("site.application.title"))

    }

    "have the correct contents" in {

      doc.toString should include (messagesApi("iht.private.beta.landing.page.info1"))
      doc.toString should include (messagesApi("iht.private.beta.landing.page.info2"))
      doc.toString should include (messagesApi("iht.private.beta.landing.page.info3"))
      doc.toString should include (messagesApi("iht.page"))
      doc.toString should include (messagesApi("iht.period"))

    }

    "have the correct links" in {

      val linkValuingEstateOfDeceased = doc.getElementById("valuing-estate-of-deceased")
      linkValuingEstateOfDeceased.text shouldBe messagesApi("iht.private.beta.landing.page.info3")
      linkValuingEstateOfDeceased.attr("href") shouldBe "https://www.gov.uk/valuing-estate-of-someone-who-died/forms"

      val linkGoToGovUk = doc.getElementById("iht-registration")
      linkGoToGovUk.text shouldBe messagesApi("iht.goToGovUk")
      linkGoToGovUk.attr("href") shouldBe "https://www.gov.uk/valuing-estate-of-someone-who-died/forms"

    }

  }
}

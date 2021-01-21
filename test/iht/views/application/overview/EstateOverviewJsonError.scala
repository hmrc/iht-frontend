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

package iht.views.application.overview

import iht.views.ViewTestHelper
import iht.views.helpers.MessagesHelper
import org.jsoup.Jsoup
import play.api.test.FakeRequest
import uk.gov.hmrc.play.bootstrap.controller.FrontendController
import iht.config.AppConfig

class EstateOverviewJsonError  extends ViewTestHelper with MessagesHelper {

  implicit lazy val fakeRequest = FakeRequest()
  lazy val view = iht.views.html.application.overview.estate_overview_json_error()
  lazy val doc = Jsoup.parse(view.body)

  "EstateOverviewJsonError" must {

    "have the correct first paragraph" in {
      doc.select("p").eq(2).text mustBe errorEstateOverviewJsonErrorp1
    }

    "have the correct second paragraph" in {
      doc.select("p").eq(3).text mustBe errorEstateOverviewJsonErrorp2
    }
  }

}

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

package iht.views.registration

import iht.TestUtils
import iht.views.ViewTestHelper
import iht.views.html.registration.registration_summary_coexecutor_panel
import play.api.i18n.Lang

class RegistrationSummaryCoExecutorPanelTest extends ViewTestHelper with TestUtils {

  "RegistrationSummaryCoExecutorPanelTest" must {

    "have no message keys in html" in {
      implicit val request = createFakeRequest()
      implicit val lang = Lang.defaultLang


      val view = registration_summary_coexecutor_panel(Seq())(request, messages, lang, appConfig).toString
      noMessageKeysShouldBePresent(view)
    }

    "link to the others applying for probate change" in {
      implicit val request = createFakeRequest()
      implicit val lang = Lang.defaultLang

      registration_summary_coexecutor_panel(Seq())(request, messages, lang, appConfig).toString must include
        iht.controllers.registration.executor.routes.OthersApplyingForProbateController.onPageLoadFromOverview().url
    }
  }
}

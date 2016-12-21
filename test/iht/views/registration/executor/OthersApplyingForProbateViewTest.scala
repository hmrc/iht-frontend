/*
 * Copyright 2016 HM Revenue & Customs
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

package iht.views.registration.executor

import iht.{FakeIhtApp, TestUtils}
import iht.forms.registration.CoExecutorForms.othersApplyingForProbateForm
import iht.views.HtmlSpec
import iht.views.html.registration.executor.others_applying_for_probate
import play.api.mvc.Call
import uk.gov.hmrc.play.test.UnitSpec

class OthersApplyingForProbateViewTest extends UnitSpec with FakeIhtApp with TestUtils with HtmlSpec {

  "Others Applying for Probate View" must {

    "have a fieldset with the Id 'answer'" in {
      val view = others_applying_for_probate(othersApplyingForProbateForm, Call("", ""))(createFakeRequest()).toString

      asDocument(view).getElementsByTag("fieldset").first.id shouldBe "answer"
    }
  }
}

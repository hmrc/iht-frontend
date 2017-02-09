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

package iht.views.registration.executor

import iht.controllers.ControllerHelper.Mode
import iht.{FakeIhtApp, TestUtils}
import iht.forms.registration.CoExecutorForms.coExecutorPersonalDetailsEditForm
import iht.views.HtmlSpec
import iht.views.html.registration.executor.coexecutor_personal_details
import play.api.mvc.Call
import uk.gov.hmrc.play.test.UnitSpec
import play.api.i18n.Messages.Implicits._
import play.api.Play.current

class CoexecutorPersonalDetailsViewTest extends UnitSpec with FakeIhtApp with TestUtils with HtmlSpec {

  "Coexecutor Personal Details View" must {

    "have a fieldset with the Id 'date-of-birth'" in {
      val view = coexecutor_personal_details(coExecutorPersonalDetailsEditForm, Mode.Edit, Call("", ""))(createFakeRequest(), applicationMessages).toString

      asDocument(view).getElementsByTag("fieldset").first.id shouldBe "date-of-birth"
    }
  }
}

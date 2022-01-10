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

package iht.forms.application.declaration

import iht.forms.ApplicationForms._
import iht.forms.FormTestHelper
import play.api.data.Form

class CheckedEverythingFormsTest extends FormTestHelper {
  "not give an error when answered Yes" in {
    val data = formData("hasChecked", "true")
    val form: Form[Option[Boolean]] = checkedEverythingQuestionForm.bind(data)
    form.hasErrors mustBe false
  }

  "not give an error when answered No" in {
    val data = formData("hasChecked", "false")
    val form: Form[Option[Boolean]] = checkedEverythingQuestionForm.bind(data)
    form.hasErrors mustBe false
  }

  "give an error when the question is not answered" in {
    val data = Map[String, String]()
    val form: Form[Option[Boolean]] = checkedEverythingQuestionForm.bind(data)
    form.hasErrors mustBe true
  }
}

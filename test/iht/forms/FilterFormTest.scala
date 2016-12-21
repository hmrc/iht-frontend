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

package iht.forms

import iht.FakeIhtApp
import iht.forms.FilterForms._
import play.api.data.FormError
import iht.constants.Constants._

/**
  * Created by adwelly on 21/10/2016.
  */
class FilterFormTest extends FormTestHelper with FakeIhtApp {
  "FilterForm" must {
    "give an error when no value is provided" in {
      val data = Map[String, String]()
      val expectedErrors = Seq(FormError(filterChoices, "error.selectAnswer"))

      checkForError(filterForm, data, expectedErrors)
    }

    "give an error when blank value is provided" in {
      val data = Map[String, String](filterChoices -> "")
      val expectedErrors = Seq(FormError(filterChoices, "error.invalid"))

      checkForError(filterForm, data, expectedErrors)
    }
  }

  "Domicile Form" must {
    "give an error when no value is provided" in {
      val data = Map[String, String]()
      val expectedErrors = Seq(FormError(domicile, "error.selectAnswer"))

      checkForError(domicileForm, data, expectedErrors)
    }

    "give an error when blank value is provided" in {
      val data = Map[String, String](domicile -> "")
      val expectedErrors = Seq(FormError(domicile, "error.invalid"))

      checkForError(domicileForm, data, expectedErrors)
    }
  }
}

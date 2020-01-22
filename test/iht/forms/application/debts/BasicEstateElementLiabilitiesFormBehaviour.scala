/*
 * Copyright 2020 HM Revenue & Customs
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

package iht.forms.application.debts

import iht.FakeIhtApp
import iht.forms.FormTestHelper
import iht.models.application.debts.BasicEstateElementLiabilities
import iht.testhelpers.CommonBuilder
import play.api.data.Form
import play.api.libs.json.Json

trait BasicEstateElementLiabilitiesFormBehaviour extends FormTestHelper with FakeIhtApp {

  private def dataAsJson(model: BasicEstateElementLiabilities) = Json.toJson(model)
  private def defaultModel = CommonBuilder.buildBasicEstateElementLiabilities

  private def estateElementLiability(value: String, isOwned: String = "true") =
    Map("value" -> value, "isOwned" -> isOwned)

  def form:Form[BasicEstateElementLiabilities]
  def selectErrorKey:String

  def basicEstateElementLiability()  = {
    "give an error when the question is not answered" in {
      val basicEstateElementLiabilityModel = defaultModel.copy(isOwned = None, value = None)
      val expectedErrors = error("isOwned", selectErrorKey)

      checkForError(form, dataAsJson(basicEstateElementLiabilityModel), expectedErrors)
    }

    "not give an error when answered Yes" in {
      val basicEstateElementLiabilityModel = defaultModel.copy(isOwned = Some(true), value = None)

      formWithNoError(form, dataAsJson(basicEstateElementLiabilityModel)) mustBe basicEstateElementLiabilityModel
    }

    "not give an error when answered No" in {
      val basicEstateElementLiabilityModel = defaultModel.copy(isOwned = Some(false), value = None)

      formWithNoError(form, dataAsJson(basicEstateElementLiabilityModel)) mustBe basicEstateElementLiabilityModel
    }

    behave like currencyValue[BasicEstateElementLiabilities]("value", form)
  }
}

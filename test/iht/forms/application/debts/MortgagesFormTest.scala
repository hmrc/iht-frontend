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

package iht.forms.application.debts

import iht.FakeIhtApp
import iht.forms.ApplicationForms._
import iht.forms.FormTestHelper
import iht.models.application.debts.Mortgage
import iht.testhelpers.CommonBuilder
import play.api.libs.json.Json

class MortgagesFormTest extends FormTestHelper with FakeIhtApp {

  private def dataAsJson(model: Mortgage) = Json.toJson(model)
  private lazy val defaultMortgageModel = CommonBuilder.buildMortgage

  "MortgagesForm" must {
   "give an error when the question is not answered" in {
      val mortgageModel = CommonBuilder.buildMortgage.copy(isOwned = None, value = None)
      val expectedErrors = error("isOwned", "error.debts.mortgage.select")

     checkForError(mortgagesForm, dataAsJson(mortgageModel), expectedErrors)
    }

    "not give an error when answered Yes" in {
      val mortgageModel = defaultMortgageModel.copy(id= "", isOwned = Some(true), value = None)

      formWithNoError(mortgagesForm, dataAsJson(mortgageModel)) mustBe mortgageModel
    }

    "not give an error when answered No" in {
      val mortgageModel = defaultMortgageModel.copy(id= "", isOwned = Some(false), value = None)

      formWithNoError(mortgagesForm, dataAsJson(mortgageModel)) mustBe mortgageModel
    }

    behave like currencyValue[Mortgage]("value", mortgagesForm)
  }

}

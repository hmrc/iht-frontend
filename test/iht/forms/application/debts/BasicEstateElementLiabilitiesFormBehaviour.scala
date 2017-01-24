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
  def valueErrorKey:String

  def basicEstateElementLiability()  = {

    behave like yesNoQuestionAndValue[BasicEstateElementLiabilities](
      "isOwned",
      "value",
      form,
      _.isOwned,
      _.value,
      selectErrorKey,
      valueErrorKey
    )
  }
}

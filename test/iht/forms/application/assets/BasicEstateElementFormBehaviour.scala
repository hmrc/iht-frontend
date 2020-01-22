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

package iht.forms.application.assets

import iht.forms.FormTestHelper
import iht.models.application.basicElements.BasicEstateElement
import play.api.data.Form


trait BasicEstateElementFormBehaviour extends FormTestHelper{

  def basicEstateElementForm(form: Form[BasicEstateElement], selectErrorKey:String = "error.selectAnswer") = {
      behave like yesNoQuestion[BasicEstateElement]("isOwned", form, _.isOwned, selectErrorKey)
      behave like currencyValue[BasicEstateElement]("value", form)
  }

}

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

package iht.forms.application.assets

import iht.forms.FormTestHelper
import iht.models.application.basicElements.ShareableBasicEstateElement
import play.api.data.Form


trait ShareableBasicEstateElementFormBehaviour extends FormTestHelper{

  def deceasedOwnedForm(form: Form[ShareableBasicEstateElement], selectErrorKey:String = "error.selectAnswer") = {
    behave like yesNoQuestion[ShareableBasicEstateElement]("isOwned", form, _.isOwned, selectErrorKey)
    behave like currencyValue[ShareableBasicEstateElement]("value", form)
  }

  def jointlyOwnedForm(form: Form[ShareableBasicEstateElement], selectErrorKey:String = "error.selectAnswer") = {
    behave like yesNoQuestion[ShareableBasicEstateElement]("isOwnedShare", form, _.isOwnedShare, selectErrorKey)
    behave like currencyValue[ShareableBasicEstateElement]("shareValue", form)
  }
}

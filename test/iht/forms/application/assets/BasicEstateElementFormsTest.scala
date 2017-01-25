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

package iht.forms.application.assets

import iht.forms.ApplicationForms._

/**
  * Created by vineet on 15/12/16.
  */
class BasicEstateElementFormsTest extends BasicEstateElementFormBehaviour {

  "ForeignForm" must {
    behave like basicEstateElementForm(foreignForm, "error.assets.foreign.select")
  }

  "MoneyOwedForm" must {
    behave like basicEstateElementForm(moneyOwedForm, "error.assets.moneyOwedToDeceased.select")
  }

  "OtherForm" must {
    behave like basicEstateElementForm(otherForm, "error.assets.other.select")
  }

  "BusinessInterestForm" must {
    behave like basicEstateElementForm(businessInterestForm, "error.assets.businessInterest.select")
  }

  "NominatedForm" must {
    behave like basicEstateElementForm(nominatedForm, "error.assets.nominated.select")
  }
}
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
import iht.models.application.basicElements.BasicEstateElement
import iht.models.application.debts.BasicEstateElementLiabilities

/**
  * Created by vineet on 15/12/16.
  */
class BasicEstateElementFormsTest extends BasicEstateElementFormBehaviour {

  "ForeignForm" must {
    behave like yesNoQuestionAndValue[BasicEstateElement](
      "isOwned",
      "value",
      foreignForm,
      _.isOwned,
      _.value,
      "error.assets.foreign.select",
      "error.estateReport.value.give"
    )
  }

  "MoneyOwedForm" must {
    //behave like basicEstateElementForm(moneyOwedForm, "error.assets.moneyOwedToDeceased.select")
    behave like yesNoQuestionAndValue[BasicEstateElement](
      "isOwned",
      "value",
      moneyOwedForm,
      _.isOwned,
      _.value,
      "error.assets.moneyOwedToDeceased.select",
      "error.estateReport.value.give"
    )
  }

  "OtherForm" must {
    //behave like basicEstateElementForm(otherForm, "error.assets.other.select")
    behave like yesNoQuestionAndValue[BasicEstateElement](
      "isOwned",
      "value",
      otherForm,
      _.isOwned,
      _.value,
      "error.assets.other.select",
      "error.estateReport.value.give"
    )
  }

  "BusinessInterestForm" must {
    //behave like basicEstateElementForm(businessInterestForm, "error.assets.businessInterest.select")
    behave like yesNoQuestionAndValue[BasicEstateElement](
      "isOwned",
      "value",
      businessInterestForm,
      _.isOwned,
      _.value,
      "error.assets.businessInterest.select",
      "error.estateReport.value.give"
    )
  }

  "NominatedForm" must {
    //behave like basicEstateElementForm(nominatedForm, "error.assets.nominated.select")
    behave like yesNoQuestionAndValue[BasicEstateElement](
      "isOwned",
      "value",
      nominatedForm,
      _.isOwned,
      _.value,
      "error.assets.nominated.select",
      "error.estateReport.value.give"
    )
  }
}

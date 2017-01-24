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
import iht.forms.FormTestHelper
import iht.models.application.assets.StockAndShare
import play.api.data.Form

/**
  * Created by vineet on 15/12/16.
  */
class StocksAndSharesFormsTest extends FormTestHelper {

  lazy val stocksAndSharesListedErrorMsgKeySelect = "error.assets.stocksAndShares.listed.select"
  lazy val stocksAndSharesListedErrorMsgKeyEnterValue = "error.estateReport.value.give"

  lazy val stocksAndSharesNotListedErrorMsgKeySelect = "error.assets.stocksAndShares.notListed.select"
  lazy val stocksAndSharesNotListedErrorMsgKeyEnterValue = "error.estateReport.value.give"

  "StockAndSharesListed" must {
    behave like yesNoQuestionAndValue[StockAndShare](
      "isListed",
      "valueListed",
      stockAndShareListedForm,
      _.isListed,
      _.valueListed,
      stocksAndSharesListedErrorMsgKeySelect,
      stocksAndSharesListedErrorMsgKeyEnterValue
    )
  }

  "StockAndSharesNotListed" must {
    behave like yesNoQuestionAndValue[StockAndShare](
      "isNotListed",
      "valueNotListed",
      stockAndShareNotListedForm,
      _.isNotListed,
      _.valueNotListed,
      stocksAndSharesNotListedErrorMsgKeySelect,
      stocksAndSharesNotListedErrorMsgKeyEnterValue
    )
  }
}

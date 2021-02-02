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

package iht.forms

import iht.FakeIhtApp
import iht.models.QuestionnaireModel
import play.api.data.Form

class QuestionnaireFormsTest extends FakeIhtApp {

  "QuestionnaireForms#questionnaire_form" must {
    "generate a form from the QuestionnaireModel" in {
      val form = QuestionnaireForms.questionnaire_form
      form.isInstanceOf[Form[QuestionnaireModel]]
    }
  }

}

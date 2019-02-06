/*
 * Copyright 2019 HM Revenue & Customs
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

import iht.models.QuestionnaireModel
import play.api.data.Form
import play.api.data.Forms._

/**
 * Created by yasar on 10/9/15.
 */
object QuestionnaireForms {
  val questionnaire_form = Form[QuestionnaireModel](
    // scalastyle:off magic.number
    mapping(
      "feelingAboutExperience" -> optional(number(1,5)),
      "easytouse" -> optional(number(1,5)),
      "howcanyouimprove" -> optional(text(minLength = 1, maxLength = 1200)),
      "fullName" -> optional(text),
      "contactDetails" -> optional(text),
      "stageInService" -> optional(text),
      "intendToReturn" -> optional(boolean)
    )(QuestionnaireModel.apply)(QuestionnaireModel.unapply)
  )
}

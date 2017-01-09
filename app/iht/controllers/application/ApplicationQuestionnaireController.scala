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

package iht.controllers.application

import iht.controllers.{IhtConnectors, QuestionnaireController}
import iht.utils.IhtSection
import iht.views.html.application.application_questionnaire

object ApplicationQuestionnaireController extends ApplicationQuestionnaireController  with IhtConnectors {}

trait ApplicationQuestionnaireController extends ApplicationController with QuestionnaireController {
  override lazy val ihtSection = IhtSection.Application
  override def questionnaireView = (form, request) => application_questionnaire(form)(request)
}

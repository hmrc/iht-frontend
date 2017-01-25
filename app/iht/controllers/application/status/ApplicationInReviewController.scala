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

package iht.controllers.application.status

import iht.controllers.IhtConnectors
import play.api.mvc.Request
import play.api.i18n.Messages.Implicits._
import play.api.Play.current


object ApplicationInReviewController extends ApplicationStatusController with IhtConnectors {
  def getView = (ihtReference, deceasedName, probateDetails) => (request: Request[_]) =>
      iht.views.html.application.status.in_review_application(ihtReference, deceasedName, probateDetails)(request)

}

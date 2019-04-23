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

package iht.views.iv.failurepages

import iht.views.GenericNonSubmittablePageBehaviour
import iht.views.html.iv.failurepages.precondition_failed

class PreconditionFailedViewTest extends GenericNonSubmittablePageBehaviour {

  def guidanceParagraphs = Set(
    messagesApi("page.iht.iv.failure.preconditionFailed.failureReason")
  )

  def pageTitle = messagesApi("page.iht.iv.failure.preconditionFailed.heading")

  def browserTitle = messagesApi("page.iht.iv.failure.preconditionFailed.heading")

  def view: String = precondition_failed()(createFakeRequest(), messages, formPartialRetriever, appConfig).toString

  override def exitComponent = None

  "Precondition Failed View" must {
    behave like nonSubmittablePage()
  }
}

/*
 * Copyright 2018 HM Revenue & Customs
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

import iht.views.html.iv.failurepages.locked_out
import iht.views.{ExitComponent, GenericNonSubmittablePageBehaviour}
import play.api.i18n.Messages.Implicits._

class LockedOutViewTest extends GenericNonSubmittablePageBehaviour {

  def guidanceParagraphs = Set(
    messagesApi("page.iht.iv.failure.youCanAlso")
  )

  def pageTitle = messagesApi("page.iht.iv.failure.lockedOut.heading")

  def browserTitle = messagesApi("page.iht.iv.failure.lockedOut.heading")

  def view: String = locked_out()(createFakeRequest(), applicationMessages, formPartialRetriever).toString

  override def exitComponent = None

  "Locked Out View" must {
    behave like nonSubmittablePage()
  }
}

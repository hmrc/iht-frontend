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

package iht.views.iv.failurepages

import iht.views.html.iv.failurepages.locked_out
import iht.views.{ExitComponent, GenericNonSubmittablePageBehaviour}
import play.api.i18n.Messages.Implicits._

class LockedOutViewTest extends GenericNonSubmittablePageBehaviour {
  implicit val request = createFakeRequest()

  def guidanceParagraphs = Set(
    messagesApi("page.iht.iv.failure.lockedOut.p1"),
    messagesApi("page.iht.iv.failure.lockedOut.p2")
  )

  def pageTitle = messagesApi("page.iht.iv.failure.lockedOut.title")

  def browserTitle = messagesApi("page.iht.iv.failure.lockedOut.title")

  def view: String = locked_out().toString

  override def exitComponent = Some(
    ExitComponent(
      iht.controllers.routes.PrivateBetaLandingPageController.showLandingPage(),
      messagesApi("iht.iv.exit")
    )
  )

  "Locked Out View" must {
    behave like nonSubmittablePage()
  }
}

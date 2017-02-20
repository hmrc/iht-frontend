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

import iht.views.html.iv.failurepages.incomplete
import iht.views.{ExitComponent, GenericNonSubmittablePageBehaviour}
import play.api.i18n.Messages.Implicits._

class IncompleteViewTest extends GenericNonSubmittablePageBehaviour {

  implicit val request = createFakeRequest()

  def guidanceParagraphs = Set(
    messagesApi("iht.iv.unableToContinue"),
    messagesApi("iht.iv.tryAgainLater")
  )

  def pageTitle = messagesApi("error.problem")

  def browserTitle = messagesApi("error.problem")

  def view: String = incomplete().toString

  override def exitComponent = Some(
    ExitComponent(
      iht.controllers.routes.PrivateBetaLandingPageController.showLandingPage(),
      messagesApi("iht.iv.tryAgain")
    )
  )

  "Incomplete View" must {
    behave like nonSubmittablePage()
  }
}

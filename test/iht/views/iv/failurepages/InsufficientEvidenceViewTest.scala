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

import iht.testhelpers.CommonBuilder
import iht.views.html.iv.failurepages.insufficient_evidence
import iht.views.{ExitComponent, GenericNonSubmittablePageBehaviour}
import play.api.i18n.Messages.Implicits._

class InsufficientEvidenceViewTest extends GenericNonSubmittablePageBehaviour {

  def guidanceParagraphs = Set(
    messagesApi("iht.iv.unableToContinue"),
    messagesApi("iht.iv.tryAgainLater")
  )

  def pageTitle = messagesApi("error.problem")

  def browserTitle = messagesApi("error.problem")

  def view: String = insufficient_evidence(CommonBuilder.DefaultCall1.url)(createFakeRequest(), applicationMessages).toString

  override def exitComponent = Some(
    ExitComponent(
      CommonBuilder.DefaultCall1,
      messagesApi("iht.iv.tryAgain")
    )
  )

  "Insufficient Evidence View" must {
    behave like nonSubmittablePage()
  }
}

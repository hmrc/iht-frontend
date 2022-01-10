/*
 * Copyright 2022 HM Revenue & Customs
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
import iht.views.html.iv.failurepages.timeout
import iht.views.{ExitComponent, GenericNonSubmittablePageBehaviour}

class TimeOutViewTest extends GenericNonSubmittablePageBehaviour {

  def guidanceParagraphs = Set(
    messagesApi("page.iht.iv.failure.timeout.notSaved")
  )

  def pageTitle = messagesApi("page.iht.iv.failure.timeout.heading")

  def browserTitle = messagesApi("page.iht.iv.failure.timeout.heading")
  lazy val timeoutView: timeout = app.injector.instanceOf[timeout]

  def view: String = timeoutView(CommonBuilder.DefaultCall1.url)(createFakeRequest(), messages).toString

  override def exitComponent = Some(
    ExitComponent(
      CommonBuilder.DefaultCall1,
      messagesApi("iht.iv.signIn")
    )
  )

  "Time Out View" must {
    behave like nonSubmittablePage()

    "show the 'your answers have been saved' message" when {
      "the user is completing their application" in {
        val applicationView = timeoutView("/estate-report")(createFakeRequest(), messages).toString
        applicationView must include(messagesApi("page.iht.iv.failure.timeout.saved"))
        applicationView must not include messagesApi("page.iht.iv.failure.timeout.notSaved")
      }
    }
  }
}

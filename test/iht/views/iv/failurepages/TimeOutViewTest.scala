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
import iht.views.html.iv.failurepages.timeout
import iht.views.{ExitComponent, GenericNonSubmittablePageBehaviour}
import play.api.i18n.Messages

class TimeOutViewTest extends GenericNonSubmittablePageBehaviour {
  def guidanceParagraphs = Set(
    Messages("page.iht.iv.failure.timeout.p1"),
    Messages("page.iht.iv.failure.timeout.p2")
  )

  def pageTitle = Messages("page.iht.iv.failure.timeout.title")

  def browserTitle = Messages("page.iht.iv.failure.timeout.title")

  def view: String = timeout(CommonBuilder.DefaultCall1.url).toString

  override def exitComponent = Some(
    ExitComponent(
      CommonBuilder.DefaultCall1,
      Messages("iht.startAgain")
    )
  )

  "Time Out View" must {
    behave like nonSubmittablePage()
  }
}

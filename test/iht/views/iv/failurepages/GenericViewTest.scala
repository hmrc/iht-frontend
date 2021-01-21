/*
 * Copyright 2021 HM Revenue & Customs
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
import iht.views.html.iv.failurepages.generic
import iht.views.{ExitComponent, GenericNonSubmittablePageBehaviour}

class GenericViewTest extends GenericNonSubmittablePageBehaviour {

  def guidanceParagraphs = Set(
    messagesApi("page.iht.iv.failure.youCanReport", appConfig.linkIHT205PDF),
    messagesApi("page.iht.iv.failure.ifYouThink")
  )

  def pageTitle = messagesApi("page.iht.iv.failure.couldNotConfirmIdentity")

  def browserTitle = messagesApi("page.iht.iv.failure.couldNotConfirmIdentity")

  def view: String = generic(CommonBuilder.DefaultCall1.url)(createFakeRequest(), messages, formPartialRetriever, appConfig).toString

  override def exitComponent = Some(
    ExitComponent(
      CommonBuilder.DefaultCall1,
      messagesApi("iht.iv.tryAgain")
    )
  )

  "Failed Matching View" must {
    behave like nonSubmittablePage()
  }
}

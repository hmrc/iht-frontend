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
import iht.views.html.iv.failurepages.technical_issue
import iht.views.{ExitComponent, GenericNonSubmittablePageBehaviour}

class TechnicalIssueViewTest extends GenericNonSubmittablePageBehaviour {
  lazy val technicalIssueView: technical_issue = app.injector.instanceOf[technical_issue]

  def guidanceParagraphs = Set(
    messagesApi("page.iht.iv.failure.youCanAlso")
  )

  def pageTitle = messagesApi("page.iht.iv.failure.technicalIssue.heading")

  def browserTitle = messagesApi("page.iht.iv.failure.technicalIssue.heading")

  def view: String = technicalIssueView(CommonBuilder.DefaultCall1.url)(createFakeRequest(), messages).toString

  override def exitComponent = Some(
    ExitComponent(
      CommonBuilder.DefaultCall1,
      messagesApi("iht.iv.tryAgain")
    )
  )

  "Technical Issue View" must {
    behave like nonSubmittablePage()
  }
}

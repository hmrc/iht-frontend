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

package iht.views

import iht.testhelpers.TestHelper
import iht.views.html.{deadlines_application, deadlines_registration}
import play.api.mvc.{AnyContentAsEmpty, Call}
import play.api.test.FakeRequest

class DeadlinesApplicationViewTest extends GenericNonSubmittablePageBehaviour {
  implicit def request: FakeRequest[AnyContentAsEmpty.type] = createFakeRequest()

  override val exitId: String = "exit-to-iht400"

  override def guidanceParagraphs = Set(
    messagesApi("page.iht.deadlines.p1"),
    messagesApi("page.iht.deadlines.p2"),
    messagesApi("page.iht.deadlines.p3")
  )

  override def pageTitle = messagesApi("page.iht.deadlines.title")

  override def browserTitle = messagesApi("page.iht.deadlines.title")

  override def exitComponent = Some(
    ExitComponent(
      Call("GET", TestHelper.LinkEstateReportKickOut),
      messagesApi("page.iht.filter.paperform.million.exit")
    )
  )

  override def view =
    deadlines_application(request, messagesApi.preferred(request), formPartialRetriever).toString

  "Deadlines view" must {
    behave like nonSubmittablePage()

    behave like link("pay-early",
      TestHelper.LinkPayEarly,
      messagesApi("page.iht.deadlines.anchorText"))
  }
}

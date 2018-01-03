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

package iht.views.filter

import iht.views.ViewTestHelper
import iht.views.html.filter.filter_jointly_owned
import play.api.data.Form
import play.api.data.Forms.{optional, single, text}
import play.api.i18n.Messages.Implicits._
import play.api.mvc.AnyContentAsEmpty
import play.api.test.FakeRequest
import play.api.test.Helpers.{contentAsString, _}

class FilterJointlyOwnedViewTest extends ViewTestHelper {

  val fakeRequest: FakeRequest[AnyContentAsEmpty.type] = createFakeRequest(isAuthorised = false)
  val fakeForm =  Form(single("s"-> optional(text)))

  "filter_jointly_owned" must {

    "have no message keys in html" in {
      val result = filter_jointly_owned(fakeForm)(fakeRequest, applicationMessages, formPartialRetriever)
      val view = asDocument(contentAsString(result)).toString
      noMessageKeysShouldBePresent(view)
    }

  }

}

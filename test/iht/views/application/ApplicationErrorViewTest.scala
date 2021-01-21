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

package iht.views.application

import iht.views.GenericNonSubmittablePageBehaviour
import iht.views.html.application.application_error

trait ApplicationErrorViewBehaviour extends GenericNonSubmittablePageBehaviour {

  override def pageTitle = messagesApi("error.problem")

  override def browserTitle = messagesApi("error.problem")

  override def exitComponent = None
}

class ApplicationErrorViewServiceUnavailableTest extends ApplicationErrorViewBehaviour {
  override def guidanceParagraphs = Set(messagesApi("error.report.redo"))

  override def view: String = application_error("serviceUnavailable")(createFakeRequest(), messages, formPartialRetriever, appConfig).toString

  "Application error view for service unavailable" must {
    behave like nonSubmittablePage()
  }
}

class ApplicationErrorViewRequestTimeOutTest extends ApplicationErrorViewBehaviour {
  override def guidanceParagraphs = Set(messagesApi("error.cannotSend"))

  override def view: String = application_error("requestTimeOut")(createFakeRequest(), messages, formPartialRetriever, appConfig).toString

  "Application error view for request timeOut" must {
    behave like nonSubmittablePage()

    "not have service unavailable content" in {
      messagesShouldNotBePresent(view, messagesApi("error.report.redo"))
    }
  }
}

class ApplicationErrorViewSomeOtherTest extends ApplicationErrorViewBehaviour {
  override def guidanceParagraphs = Set(messagesApi("error.cannotSend"), messagesApi("error.report.redo"))

  override def view: String = application_error("someOther")(createFakeRequest(), messages, formPartialRetriever, appConfig).toString

  "Application error view for some other error" must {
    behave like nonSubmittablePage()
  }
}

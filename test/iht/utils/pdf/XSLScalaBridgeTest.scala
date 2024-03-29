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

package iht.utils.pdf

import iht.FakeIhtApp
import org.scalatestplus.mockito.MockitoSugar
import play.api.i18n.{I18nSupport, Lang, Messages, MessagesApi}
import play.api.test.FakeRequest

class XSLScalaBridgeTest extends FakeIhtApp with MockitoSugar with I18nSupport {

  implicit val messagesApi: MessagesApi = app.injector.instanceOf[MessagesApi]
  implicit val request = FakeRequest()
  val messages: Messages = messagesApi.preferred(request)
  implicit val lang = Lang.defaultLang

  "getMessagesText" must {
    "return the correct string" in {

      val result = XSLScalaBridge(messages).getMessagesText("iht.the.deceased")

      result mustBe  messagesApi("iht.the.deceased")
    }
  }

  "getMessagesTextWithParameter" must {
    "return the correct string" in {

      val name = "John"

      val result = XSLScalaBridge(messages).getMessagesTextWithParameter("iht.estateReport.assets.moneyOwned", name)

      result mustBe  messagesApi("iht.estateReport.assets.moneyOwned", name)
    }
  }

  "getMessagesTextWithParameters" must {
    "return the correct string" in {
      val name1 = "John"
      val name2 = "Smith"

      val result = XSLScalaBridge(messages).getMessagesTextWithParameters("pdf.inheritance.tax.application.summary.p1",
        name1, name2)

      result mustBe  messagesApi("pdf.inheritance.tax.application.summary.p1", name1, name2)
    }

    "return the correct string with 3 parameters" in {
      val parameter1 = "John"
      val parameter2 = "Smith"
      val parameter3 = "Sam"

      val result = XSLScalaBridge(messages).getMessagesTextWithParameters("iht.estateReport.tnrb.partner.married",
        parameter1, parameter2, parameter3)

      result mustBe  messagesApi("iht.estateReport.tnrb.partner.married", parameter1, parameter2, parameter3)
    }
  }

  "getDateForDisplay" must {
    "return correctly formatted date" in {
      val result = XSLScalaBridge(messages).getDateForDisplay("2000-12-12")
      result mustBe "12 December 2000"
    }

    "return empty string when there is no date passed to be formatted" in {
      val result = XSLScalaBridge(messages).getDateForDisplay("")
      result mustBe ""
    }
  }
}

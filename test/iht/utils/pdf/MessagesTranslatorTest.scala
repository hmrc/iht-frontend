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

package iht.utils.pdf

import iht.FakeIhtApp
import org.scalatest.mock.MockitoSugar
import play.api.i18n.Messages
import play.api.i18n.Messages.Implicits._
import play.api.Play.current
import uk.gov.hmrc.play.test.UnitSpec

/**
  * Created by vineet on 21/11/16.
  */
class MessagesTranslatorTest extends UnitSpec with FakeIhtApp with MockitoSugar {

  "getMessagesText" must {
    "return the correct string" in {

      val result = MessagesTranslator.getMessagesText("iht.the.deceased")

      result shouldBe  Messages("iht.the.deceased")
    }
  }

  "getMessagesTextWithParameter" must {
    "return the correct string" in {

      val name = "John"

      val result = MessagesTranslator.getMessagesTextWithParameter("iht.estateReport.assets.moneyOwned", name)

      result shouldBe  Messages("iht.estateReport.assets.moneyOwned", name)
    }
  }

  "getMessagesTextWithParameters" must {
    "return the correct string" in {
      val name1 = "John"
      val name2 = "Smith"

      val result = MessagesTranslator.getMessagesTextWithParameters("pdf.inheritance.tax.application.summary.p1",
        name1, name2)

      result shouldBe  Messages("pdf.inheritance.tax.application.summary.p1", name1, name2)
    }

    "return the correct string with 3 parameters" in {
      val parameter1 = "John"
      val parameter2 = "Smith"
      val parameter3 = "Sam"

      val result = MessagesTranslator.getMessagesTextWithParameters("iht.estateReport.tnrb.partner.married",
        parameter1, parameter2, parameter3)

      result shouldBe  Messages("iht.estateReport.tnrb.partner.married", parameter1, parameter2, parameter3)
    }
  }
}

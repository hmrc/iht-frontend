/*
 * Copyright 2019 HM Revenue & Customs
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

package iht.utils

import iht.FakeIhtApp
import org.mockito.Mockito._
import org.scalatest.mock.MockitoSugar
import play.api.i18n.{I18nSupport, Lang, Messages, MessagesApi}
import uk.gov.hmrc.play.test.UnitSpec

/**
 *
 * This Class contains the Unit Tests for iht.utils.CommonHelper
 */
class MessagesHelperTest extends FakeIhtApp with MockitoSugar with I18nSupport {

  val msg = Map("en" -> Map("a.a"->"a"), "cy" -> Map("a.a" -> "w"))
  implicit val messagesApi: MessagesApi = mock[MessagesApi]
  val referrerURL="http://localhost:9070/inheritance-tax/registration/addExecutor"
  val host="localhost:9070"

  val lang = Lang("en")
  val messages = new Messages(lang, messagesApi)

  "translateToPreferredLanguage" must {
    "return content when language same" in {
      when(messagesApi.messages).thenReturn(msg)
      MessagesHelper.translateToPreferredLanguage("a", messages, "en") mustBe "a"
    }
    "return content translated when language different" in {
      when(messagesApi.messages).thenReturn(msg)
      MessagesHelper.translateToPreferredLanguage("a", messages, "cy") mustBe "w"
    }
  }

  "messagesForLang" must {
    "return english messages file when language in en" in {
      when(messagesApi.messages).thenReturn(msg)
      MessagesHelper.messagesForLang(messages, "en").lang mustBe Lang("en")
    }
    "return welsh messages file when language in cy" in {
      when(messagesApi.messages).thenReturn(msg)
      MessagesHelper.messagesForLang(messages, "cy").lang mustBe Lang("cy")
    }
  }

  "englishMessages" must {
    "return item from englishMessages when the key exists" in {
      when(messagesApi.messages).thenReturn(msg)
      MessagesHelper.englishMessages("a.a", messages) mustBe Some("a")
    }
    "return None from englishMessages when the key does not exist" in {
      when(messagesApi.messages).thenReturn(msg)
      MessagesHelper.englishMessages("x.a", messages) mustBe None
    }
  }
}

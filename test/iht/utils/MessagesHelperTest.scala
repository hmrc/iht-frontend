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
import iht.views.helpers.MessagesHelper
import org.mockito.Mockito
import org.mockito.Mockito._
import org.scalatestplus.mockito.MockitoSugar
import play.api.i18n.{I18nSupport, Lang, Messages, MessagesApi}
import play.api.mvc.MessagesControllerComponents
import play.api.test.FakeRequest

class MessagesHelperTest extends FakeIhtApp with MockitoSugar with I18nSupport with MessagesHelper {

  val msg: Map[String, Map[String, String]] = Map("en" -> Map("a.a" -> "a"), "cy" -> Map("a.a" -> "w"))
  val referrerURL = "http://localhost:9070/inheritance-tax/registration/addExecutor"
  val host = "localhost:9070"

  lazy val mockControllerComponents: MessagesControllerComponents = app.injector.instanceOf[MessagesControllerComponents]
  implicit lazy val messagesApi: MessagesApi = mockControllerComponents.messagesApi
  lazy val messagesApiSpy: MessagesApi = Mockito.spy(mockControllerComponents.messagesApi)
  lazy val messages: Messages = messagesApiSpy.preferred(Seq(Lang.defaultLang)).messages

  "englishMessages" must {
    "return item from englishMessages when the key exists" in {
      when(messagesApiSpy.messages).thenReturn(msg)
      MessagesHelper.englishMessages("a.a", messages)(FakeRequest()) mustBe Some("a")
    }
    "return None from englishMessages when the key does not exist" in {
      when(messagesApiSpy.messages).thenReturn(msg)
      MessagesHelper.englishMessages("x.a", messages)(FakeRequest()) mustBe None
    }
  }
}

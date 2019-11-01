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

package iht.controllers

import iht.FakeIhtApp
import iht.utils.CustomLanguageUtils
import org.mockito.Mockito._
import org.scalatestplus.mockito.MockitoSugar
import play.api.Play
import play.api.i18n.{Lang, Messages, MessagesApi}
import play.api.mvc.{Cookie, Cookies, RequestHeader}
import play.api.test.FakeRequest

class LanguageControlUtilsTest extends FakeIhtApp with MockitoSugar {

  "LanguageControlUtils" must {

    val mockMessagesApi: MessagesApi = mock[MessagesApi]
    "get the current language if the language has been set in the cookies" in {
      val locale: String = "fr"
      val fakeRequest = FakeRequest().withCookies(Cookie(Play.langCookieName(mockMessagesApi), locale))

      LanguageControlUtils.getCurrentLang(fakeRequest, mockMessagesApi) mustBe Lang(locale)
    }

    "get the current language from messagesApi#preferred if they haven't been sent as a cookie" in {
      val locale: String = "de"
      val language = Lang(locale)
      val mockMessages: Messages = mock[Messages]
      val mockRequestHeader: RequestHeader = mock[RequestHeader]
      val mockCookies = mock[Cookies]

      when(mockRequestHeader.cookies).thenReturn(mockCookies)
      when(mockCookies.get(Play.langCookieName(mockMessagesApi))).thenReturn(None)
      when(mockMessagesApi.preferred(mockRequestHeader.acceptLanguages)).thenReturn(mockMessages)
      when(mockMessages.lang).thenReturn(language)

      LanguageControlUtils.getCurrentLang(mockRequestHeader, mockMessagesApi) mustBe Lang(locale)
    }

    "current language settings should be found" in {

      implicit val requestHeader = mock[RequestHeader]
      implicit val messages = mock[Messages]

      val cookie = Cookie("PLAY_LANG", "UK")
      val cookies = Cookies(Seq(cookie))

      when(requestHeader.cookies).thenReturn(cookies)
      CustomLanguageUtils.getCurrentLang mustBe Lang("UK")
    }

    "current language settings should NOT be found" in {

      implicit val requestHeader = mock[RequestHeader]
      implicit val messages = mock[Messages]

      val cookies = Cookies(Seq.empty[Cookie])

      when(messages.lang).thenReturn(Lang("CY"))
      when(requestHeader.cookies).thenReturn(cookies)

      CustomLanguageUtils.getCurrentLang mustBe Lang("CY")
    }
  }
}

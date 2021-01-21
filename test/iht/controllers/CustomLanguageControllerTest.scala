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

package iht.controllers

import iht.config.AppConfig
import iht.views.ViewTestHelper
import org.mockito.Mockito._
import play.api.Play
import play.api.mvc.{Cookie, Result}
import play.api.test.FakeRequest
import uk.gov.hmrc.play.language.LanguageUtils

import scala.concurrent.Await
import scala.concurrent.duration._

class CustomLanguageControllerTest extends ViewTestHelper {

  val mockAppConfig: AppConfig = mock[AppConfig]
  val mockLanguageUtils: LanguageUtils = mock[LanguageUtils]
  val welsh: String = "cymraeg"
  val welshLocale: String = "cy"
  val englishLocale: String = "en"

  "CustomLanguageController#langToCall" must {
    val locale = "cy"
    val mockAppConfig: AppConfig = mock[AppConfig]
    "call switchToLanguage(lang) if welsh is enabled" in {
      when(mockAppConfig.isWelshEnabled).thenReturn(true)
      val customLanguageController = new CustomLanguageController(mockAppConfig, mockLanguageUtils, mockControllerComponents)

      customLanguageController.langToCall(locale) mustBe iht.controllers.routes.CustomLanguageController.switchToLanguage(locale)
    }
    "call switchToLanguage('english') if welsh is not enabled" in {
      when(mockAppConfig.isWelshEnabled).thenReturn(false)
      val customLanguageController = new CustomLanguageController(mockAppConfig, mockLanguageUtils, mockControllerComponents)

      customLanguageController.langToCall(locale) mustBe iht.controllers.routes.CustomLanguageController.switchToLanguage("english")
    }
  }

  "CustomLanguageController#switchToLanguage" must {
    "redirect successfully with welsh set as the language if isWelshEnabled is true" in {
      when(mockAppConfig.isWelshEnabled) thenReturn true
      val customLanguageController = new CustomLanguageController(mockAppConfig, mockLanguageUtils, mockControllerComponents)

      val redirectResult = customLanguageController.switchToLanguage(welsh)(FakeRequest())
      val result: Result = Await.result(redirectResult, 5.seconds)

      assert(result.newCookies.contains(Cookie(Play.langCookieName, welshLocale, httpOnly = false)))
    }
    "redirect successfully with english set as the language if isWelshEnabled is false" in {
      when(mockAppConfig.isWelshEnabled) thenReturn false
      val customLanguageController = new CustomLanguageController(mockAppConfig, mockLanguageUtils, mockControllerComponents)

      val redirectResult = customLanguageController.switchToLanguage(welsh)(FakeRequest())
      val result: Result = Await.result(redirectResult, 5.seconds)

      assert(result.newCookies.contains(Cookie(Play.langCookieName, englishLocale, httpOnly = false)))
    }
  }

}

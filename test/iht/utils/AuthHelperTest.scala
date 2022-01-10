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

package iht.utils

import java.net.URLEncoder

import iht.config.AppConfig
import iht.views.ViewTestHelper
import org.mockito.Mockito._

class AuthHelperTest extends ViewTestHelper {

  val mockAppConfig = mock[AppConfig]
  val testIvUrlUplift = "test-iv-url-uplift"
  val testPostIVRedirectUrlApplication = "test-post-iv-redirect-url-application"
  val testPostIVRedirectUrlRegistration = "test-post-iv-redirect-url-registration"
  val notAuthorisedRedirectUrlRegistration = "test-not-authorised-redirect-url-registration"
  val notAuthorisedRedirectUrlApplication = "test-not-authorised-redirect-url-application"
  when(mockAppConfig.ivUrlUplift) thenReturn testIvUrlUplift
  when(mockAppConfig.postIVRedirectUrlApplication) thenReturn testPostIVRedirectUrlApplication
  when(mockAppConfig.postIVRedirectUrlRegistration) thenReturn  testPostIVRedirectUrlRegistration
  when(mockAppConfig.notAuthorisedRedirectUrlApplication) thenReturn notAuthorisedRedirectUrlApplication
  when(mockAppConfig.notAuthorisedRedirectUrlRegistration) thenReturn notAuthorisedRedirectUrlRegistration
  val authHelper = new AuthHelper {
    override val appConfig: AppConfig = mockAppConfig
  }

  "AuthHelper#getIVUrlForFailedConfidenceLevel" must {
    "construct a url from the given ihtSection" in {
      val confidenceLevel = 200
      authHelper.getIVUrlForFailedConfidenceLevel(IhtSection.Registration, confidenceLevel) mustBe
        testIvUrlUplift +
        s"completionURL=${URLEncoder.encode(testPostIVRedirectUrlRegistration, "UTF-8")}" +
        s"&failureURL=${URLEncoder.encode(notAuthorisedRedirectUrlRegistration, "UTF-8")}" +
        s"&confidenceLevel=$confidenceLevel"

      authHelper.getIVUrlForFailedConfidenceLevel(IhtSection.Application, confidenceLevel) mustBe
        testIvUrlUplift +
          s"completionURL=${URLEncoder.encode(testPostIVRedirectUrlApplication, "UTF-8")}" +
          s"&failureURL=${URLEncoder.encode(notAuthorisedRedirectUrlApplication, "UTF-8")}" +
          s"&confidenceLevel=$confidenceLevel"
    }
  }
}

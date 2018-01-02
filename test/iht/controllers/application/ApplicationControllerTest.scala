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

package iht.controllers.application

import iht.connector.CachingConnector
import iht.testhelpers.MockObjectBuilder.createMockToGetRegDetailsFromCache
import iht.utils.IhtSection
import iht.views.ViewTestHelper
import play.api.mvc.{Request, Result}
import play.api.test.Helpers.{SEE_OTHER, redirectLocation}
import play.api.test.Helpers.{contentAsString, _}

import scala.concurrent.Future

trait ApplicationControllerTest extends ViewTestHelper {
  def loginUrl = buildLoginUrl(IhtSection.Application)

  def controllerOnPageLoadWithNoExistingRegistrationDetails(mockCachingConnector: => CachingConnector,
                                                            func: => Future[Result]) = {
    "respond with redirect to application overview when no registration details found in cache" in {
      createMockToGetRegDetailsFromCache(mockCachingConnector, None)
      val result = func
      status(result) should be(SEE_OTHER)
      redirectLocation(result) shouldBe Some(iht.controllers.estateReports.routes.YourEstateReportsController.onPageLoad().url)
    }
  }
}

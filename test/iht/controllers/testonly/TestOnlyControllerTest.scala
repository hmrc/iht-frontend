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

package iht.controllers.testonly

import iht.connector.{CachingConnector, IhtConnector}
import iht.controllers.application.ApplicationControllerTest
import iht.models.application.ApplicationDetails
import iht.testhelpers.AssetsWithAllSectionsSetToNoBuilder
import iht.testhelpers.MockObjectBuilder._
import org.scalatest.BeforeAndAfter
import play.api.test.Helpers._

class TestOnlyControllerTest extends ApplicationControllerTest with BeforeAndAfter {

  def testOnlyController = new TestOnlyController {
    override val cachingConnector = mockCachingConnector
    override val ihtConnector = mockIhtConnector
    override val authConnector = mockAuthConnector

  }

  "Test Only Controller" must {

    "fill all assets and debts and gifts sections with no values when fillApplication is called" in {
      createMocksForApplication(mockCachingConnector,
        mockIhtConnector,
        getAppDetails = true,
        saveAppDetails = true,
        storeAppDetailsInCache = true)
      val result = await(testOnlyController.fillApplication(createFakeRequest(isAuthorised = true)))
      val expectedAppDetailsBeforeSave = AssetsWithAllSectionsSetToNoBuilder.buildApplicationDetails
      val appDetailsBeforeSave: ApplicationDetails = verifyAndReturnSavedApplicationDetails(mockIhtConnector)
      status(result) mustBe OK
      appDetailsBeforeSave mustBe expectedAppDetailsBeforeSave
    }

  }
}

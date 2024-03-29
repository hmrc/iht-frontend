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

package iht.controllers.testonly

import iht.config.AppConfig
import iht.controllers.application.ApplicationControllerTest
import iht.models.application.ApplicationDetails
import iht.testhelpers.AssetsWithAllSectionsSetToNoBuilder
import iht.views.html.testOnly.{store_application_details, store_application_details_result, store_registration_details, store_registration_details_result}
import org.scalatest.BeforeAndAfter
import play.api.mvc.MessagesControllerComponents
import play.api.test.Helpers._
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController

class TestOnlyControllerTest extends ApplicationControllerTest with BeforeAndAfter {

  protected abstract class TestController extends FrontendController(mockControllerComponents) with TestOnlyController {
    override val cc: MessagesControllerComponents = mockControllerComponents
    override implicit val appConfig: AppConfig = mockAppConfig
    override val storeRegistrationDetailsView: store_registration_details = app.injector.instanceOf[store_registration_details]
    override val storeRegistrationDetailsResultView: store_registration_details_result = app.injector.instanceOf[store_registration_details_result]
    override val storeApplicationDetailsView: store_application_details = app.injector.instanceOf[store_application_details]
    override val storeApplicationDetailsResultView: store_application_details_result = app.injector.instanceOf[store_application_details_result]
  }

  def testOnlyController = new TestController {
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
      val result = await(testOnlyController.fillApplication(createFakeRequest()))
      val expectedAppDetailsBeforeSave = AssetsWithAllSectionsSetToNoBuilder.buildApplicationDetails
      val appDetailsBeforeSave: ApplicationDetails = verifyAndReturnSavedApplicationDetails(mockIhtConnector)
      status(result) mustBe OK
      appDetailsBeforeSave mustBe expectedAppDetailsBeforeSave
    }

  }
}

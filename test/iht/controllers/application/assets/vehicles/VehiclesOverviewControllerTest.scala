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

package iht.controllers.application.assets.vehicles

import iht.connector.{CachingConnector, IhtConnector}
import iht.controllers.application.ApplicationControllerTest
import iht.testhelpers.CommonBuilder
import iht.testhelpers.MockObjectBuilder._
import play.api.i18n.Messages
import play.api.test.Helpers._

/**
  *
  * Created by Vineet Tyagi on 07/12/15.
  *
  */
class VehiclesOverviewControllerTest extends ApplicationControllerTest{

  val mockCachingConnector: CachingConnector = mock[CachingConnector]
  val mockIhtConnector: IhtConnector = mock[IhtConnector]

  def vehiclesOverviewController = new VehiclesOverviewController {
    override val authConnector = createFakeAuthConnector(isAuthorised=true)
    override val cachingConnector = mockCachingConnector
    override val ihtConnector = mockIhtConnector
  }

  val allAssets=CommonBuilder.buildAllAssets
  val applicationDetails=CommonBuilder.buildApplicationDetails copy (allAssets = Some(allAssets))

  "VehiclesOverviewController" must {

    "redirect to login page onPageLoad if the user is not logged in" in {
      val result = vehiclesOverviewController.onPageLoad(createFakeRequest(isAuthorised = false))
      status(result) should be(SEE_OTHER)
      redirectLocation(result) should be (Some(loginUrl))
    }

    "respond with OK on page load" in {

      createMocksForApplication(cachingConnector= mockCachingConnector,
        ihtConnector = mockIhtConnector ,
        appDetails = Some(applicationDetails),
        getAppDetails = true)

      val result = vehiclesOverviewController.onPageLoad (createFakeRequest())
      status(result) shouldBe (OK)
      contentAsString(result) should include(Messages("iht.estateReport.assets.vehicles"))
    }
  }
}

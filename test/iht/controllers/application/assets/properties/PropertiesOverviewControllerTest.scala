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

package iht.controllers.application.assets.properties

import iht.connector.{CachingConnector, IhtConnector}
import iht.controllers.application.ApplicationControllerTest
import iht.testhelpers.{MockFormPartialRetriever, CommonBuilder}
import iht.testhelpers.MockObjectBuilder._
import iht.utils.CommonHelper
import play.api.i18n.{Messages, MessagesApi}
import play.api.test.Helpers._
import uk.gov.hmrc.play.partials.FormPartialRetriever

/**
 * Created by sam on 22/12/15.
 */
class PropertiesOverviewControllerTest extends ApplicationControllerTest {




  def propertiesOverviewController = new PropertiesOverviewController {
    override val authConnector = mockAuthConnector
    override val cachingConnector = mockCachingConnector
    override val ihtConnector = mockIhtConnector
    override implicit val formPartialRetriever: FormPartialRetriever = MockFormPartialRetriever
  }

  def propertiesOverviewControllerNotAuthorised = new PropertiesOverviewController {
    override val authConnector = mockAuthConnector
    override val cachingConnector = mockCachingConnector
    override val ihtConnector = mockIhtConnector
    override implicit val formPartialRetriever: FormPartialRetriever = MockFormPartialRetriever
  }

  "PropertiesOwnedController" must {

    "redirect to login page on PageLoad if the user is not logged in" in {
      val result = propertiesOverviewControllerNotAuthorised.onPageLoad(createFakeRequest(isAuthorised = false))
      status(result) must be(SEE_OTHER)
      redirectLocation(result) must be (Some(loginUrl))
    }

    "respond with OK on page load" in {
      val applicationDetails = CommonBuilder.buildApplicationDetails.copy(propertyList = List(CommonBuilder.property))

      createMocksForApplication(mockCachingConnector,
        mockIhtConnector,
        appDetails = Some(applicationDetails),
        getAppDetails = true,
        saveAppDetails= true,
        storeAppDetailsInCache = true)

      val result = propertiesOverviewController.onPageLoad (createFakeRequest())
      status(result) must be (OK)
    }

    "display value of house with numberWithCommas formatting" in {

      val applicationDetails = CommonBuilder.buildApplicationDetails.copy(propertyList = List(CommonBuilder.property.copy(value=Some(BigDecimal(120000)))))

      createMocksForApplication(mockCachingConnector,
        mockIhtConnector,
        appDetails = Some(applicationDetails),
        getAppDetails = true,
        saveAppDetails= true,
        storeAppDetailsInCache = true)

      val result = propertiesOverviewController.onPageLoad()(createFakeRequest())
      status(result) must be (OK)
      contentAsString(result) must include (CommonHelper.numberWithCommas(120000))
    }

    behave like controllerOnPageLoadWithNoExistingRegistrationDetails(mockCachingConnector,
      propertiesOverviewController.onPageLoad(createFakeRequest()))
  }
}

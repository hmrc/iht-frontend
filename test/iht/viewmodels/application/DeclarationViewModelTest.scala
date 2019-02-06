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

package iht.viewmodels.application

import iht.connector.IhtConnector
import iht.controllers.application.ApplicationControllerTest
import iht.testhelpers.CommonBuilder._
import iht.testhelpers.MockObjectBuilder._
import play.api.data.Form
import play.api.data.Forms._
import uk.gov.hmrc.http.HeaderCarrier


class DeclarationViewModelTest extends ApplicationControllerTest{

  implicit val hc = new HeaderCarrier

  before {
    createMockToGetApplicationDetails(mockIhtConnector)
    createMockToGetRealtimeRiskMessage(mockIhtConnector, None)
  }

  "DeclarationViewModel" must {

    val appDetails = buildApplicationDetails
    val form = Form(single("value" -> boolean))
    val nino = ""

    "create declaration form" in {

      val regDetails = buildRegistrationDetails
      implicit val fakeRequest = createFakeRequest()

      DeclarationViewModel(form, appDetails, regDetails, nino, mockIhtConnector, None).declarationForm must be (form)
    }

    "create executors" in {

      val executors = Seq(buildCoExecutor, buildCoExecutor)
      val regDetails = buildRegistrationDetails.copy(coExecutors = executors)
      implicit val fakeRequest = createFakeRequest()

      DeclarationViewModel(form, appDetails, regDetails, nino, mockIhtConnector, None).executors must be (executors)
    }

    "create isMultipleExecutor with true value when there are more than one executors " in {

      val executors = Seq(buildCoExecutor, buildCoExecutor)
      val regDetails = buildRegistrationDetails.copy(coExecutors = executors)
      implicit val fakeRequest = createFakeRequest()

      DeclarationViewModel(form, appDetails, regDetails, nino, mockIhtConnector, None).isMultipleExecutor must be (true)
    }

    "create isMultipleExecutor with false value when there is one executor " in {

      val executors = Nil
      val regDetails = buildRegistrationDetails.copy(coExecutors = executors)
      implicit val fakeRequest = createFakeRequest()

      DeclarationViewModel(form, appDetails, regDetails, nino, mockIhtConnector, None).isMultipleExecutor must be (false)
    }

    "create isMultipleExecutor with true value when there is only one coExecutor " in {

      val executors = Seq(buildCoExecutor)
      val regDetails = buildRegistrationDetails.copy(coExecutors = executors)
      implicit val fakeRequest = createFakeRequest()

      DeclarationViewModel(form, appDetails, regDetails, nino, mockIhtConnector, None).isMultipleExecutor must be (true)
    }

    "create registrationDetails " in {

      val regDetails = buildRegistrationDetails
      implicit val fakeRequest = createFakeRequest()

      DeclarationViewModel(form, appDetails, regDetails, nino, mockIhtConnector, None).registrationDetails must be (regDetails)
    }
  }
}

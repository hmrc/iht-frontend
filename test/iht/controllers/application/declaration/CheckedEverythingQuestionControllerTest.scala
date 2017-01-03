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

package iht.controllers.application.declaration

import iht.connector.{CachingConnector, IhtConnector}
import iht.controllers.application.ApplicationControllerTest
import iht.forms.ApplicationForms._
import iht.models.RegistrationDetails
import iht.testhelpers.CommonBuilder
import iht.testhelpers.MockObjectBuilder._
import iht.utils.CommonHelper
import play.api.i18n.Messages
import play.api.mvc.Result
import play.api.test.Helpers._

import scala.concurrent.Future

/**
 * Created by jennygj on 12/07/16.
 */

class CheckedEverythingQuestionControllerTest extends ApplicationControllerTest{

  val mockCachingConnector = mock[CachingConnector]
  val mockIhtConnector = mock[IhtConnector]

  def checkedEverythingQuestionController = new CheckedEverythingQuestionController {
    override val authConnector = createFakeAuthConnector(isAuthorised=true)
    override val cachingConnector = mockCachingConnector
    override val ihtConnector = mockIhtConnector
  }

  def checkedEverythingQuestionNotAuthorised = new CheckedEverythingQuestionController {
    override val authConnector = createFakeAuthConnector(isAuthorised=false)
    override val cachingConnector = mockCachingConnector
    override val ihtConnector = mockIhtConnector
  }

  "CheckedEverythingQuestionController" must {
    "redirect to login page on PageLoad if the user is not logged in" in {
      val result = checkedEverythingQuestionNotAuthorised.onPageLoad(createFakeRequest())
      status(result) should be(SEE_OTHER)
      redirectLocation(result) should be (Some(loginUrl))
    }

    "redirect to login page on Submit if the user is not logged in" in {
      val result = checkedEverythingQuestionNotAuthorised.onSubmit(createFakeRequest())
      status(result) should be(SEE_OTHER)
      redirectLocation(result) should be (Some(loginUrl))
    }

    "respond with OK on page load" in {
      createMockForRegistration(mockCachingConnector,
        regDetails = Option(CommonBuilder.buildRegistrationDetails1),
        getRegDetailsFromCache = true)
      val result = checkedEverythingQuestionController.onPageLoad (createFakeRequest())
      status(result) shouldBe OK
    }

    def answerAndSubmit(booleanValue: Boolean, rd: RegistrationDetails): Future[Result] = {
      createMockForRegistration(mockCachingConnector,
        regDetails = Option(rd),
        getRegDetailsFromCache = true)

      val filledForm = checkedEverythingQuestionForm.fill(Some(booleanValue))

      implicit val request = createFakeRequest().withFormUrlEncodedBody(filledForm.data
        .toSeq: _*)

      checkedEverythingQuestionController.onSubmit(request)
    }

    "save application and go to declaration page on submit when yes is chosen" in {
      val result = answerAndSubmit(booleanValue=true, CommonBuilder.buildRegistrationDetails1)
      status(result) shouldBe SEE_OTHER
      redirectLocation(result) shouldBe Some(iht.controllers.application.declaration.routes.DeclarationController.onPageLoad().url)
    }

    "save application and go to declaration page on submit when no is chosen" in {
      val rd = CommonBuilder.buildRegistrationDetails1
      val result = answerAndSubmit(booleanValue=false, rd)
      status(result) shouldBe SEE_OTHER
      redirectLocation(result) shouldBe Some(iht.controllers.application.routes.EstateOverviewController.onPageLoadWithIhtRef(
        CommonHelper.getOrExceptionNoIHTRef(rd.ihtReference)).url)
    }

    "display validation message when incomplete form is submitted" in {
      implicit val request = createFakeRequest()

      createMockForRegistration(mockCachingConnector,
        regDetails = Option(CommonBuilder.buildRegistrationDetails1),
        getRegDetailsFromCache = true)

      val result = checkedEverythingQuestionController.onSubmit()(request)
      status(result) should be (BAD_REQUEST)
      contentAsString(result) should include (Messages("error.problem"))
    }

  }
}

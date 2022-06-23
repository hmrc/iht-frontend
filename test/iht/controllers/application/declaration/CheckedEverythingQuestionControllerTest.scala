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

package iht.controllers.application.declaration

import iht.config.AppConfig
import iht.controllers.application.ApplicationControllerTest
import iht.forms.ApplicationForms._
import iht.models.RegistrationDetails
import iht.testhelpers.CommonBuilder
import iht.utils.CommonHelper
import iht.views.html.application.declaration.checked_everything_question
import play.api.i18n.MessagesApi
import play.api.mvc.{MessagesControllerComponents, Result}
import play.api.test.Helpers._
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController

import scala.concurrent.Future

/**
 * Created by jennygj on 12/07/16.
 */

class CheckedEverythingQuestionControllerTest extends ApplicationControllerTest{

  protected abstract class TestController extends FrontendController(mockControllerComponents) with CheckedEverythingQuestionController {
    override val cc: MessagesControllerComponents = mockControllerComponents
    override implicit val appConfig: AppConfig = mockAppConfig
    override val checkedEverythingQuestionView: checked_everything_question = app.injector.instanceOf[checked_everything_question]
  }

  lazy val checkedEverythingQuestionController = new TestController {
    override val authConnector = mockAuthConnector
    override val cachingConnector = mockCachingConnector
    override val ihtConnector = mockIhtConnector
  }

  lazy val checkedEverythingQuestionNotAuthorised = new TestController {
    override val authConnector = mockAuthConnector
    override val cachingConnector = mockCachingConnector
    override val ihtConnector = mockIhtConnector
  }

  "CheckedEverythingQuestionController" must {
    "redirect to login page on PageLoad if the user is not logged in" in {
      val result = checkedEverythingQuestionNotAuthorised.onPageLoad(createFakeRequest(isAuthorised = false, authRetrieveNino = false))
      status(result) must be(SEE_OTHER)
      redirectLocation(result) must be (Some(loginUrl))
    }

    "redirect to login page on Submit if the user is not logged in" in {
      val result = checkedEverythingQuestionNotAuthorised.onSubmit(createFakeRequest(isAuthorised = false, authRetrieveNino = false))
      status(result) must be(SEE_OTHER)
      redirectLocation(result) must be (Some(loginUrl))
    }

    "respond with OK on page load" in {
      createMockForRegistration(mockCachingConnector,
        regDetails = Option(CommonBuilder.buildRegistrationDetails1),
        getRegDetailsFromCache = true)
      val result = checkedEverythingQuestionController.onPageLoad (createFakeRequest(authRetrieveNino = false))
      status(result) mustBe OK
    }

    def answerAndSubmit(booleanValue: Boolean, rd: RegistrationDetails): Future[Result] = {
      createMockForRegistration(mockCachingConnector,
        regDetails = Option(rd),
        getRegDetailsFromCache = true)

      val filledForm = checkedEverythingQuestionForm.fill(Some(booleanValue))

      implicit val request = createFakeRequest(authRetrieveNino = false).withFormUrlEncodedBody(filledForm.data
        .toSeq: _*).withMethod("POST")

      checkedEverythingQuestionController.onSubmit(request)
    }

    "save application and go to declaration page on submit when yes is chosen" in {
      val result = answerAndSubmit(booleanValue = true, CommonBuilder.buildRegistrationDetails1)
      status(result) mustBe SEE_OTHER
      redirectLocation(result) mustBe Some(iht.controllers.application.declaration.routes.DeclarationController.onPageLoad.url)
    }

    "save application and go to declaration page on submit when no is chosen" in {
      val rd = CommonBuilder.buildRegistrationDetails1
      val result = answerAndSubmit(booleanValue = false, rd)
      status(result) mustBe SEE_OTHER
      redirectLocation(result) mustBe Some(iht.controllers.application.routes.EstateOverviewController.onPageLoadWithIhtRef(
        CommonHelper.getOrExceptionNoIHTRef(rd.ihtReference)).url)
    }

    "display validation message when incomplete form is submitted" in {
      implicit val request = createFakeRequest(authRetrieveNino = false)
      implicit val messagesApi = app.injector.instanceOf[MessagesApi]
      createMockForRegistration(mockCachingConnector,
        regDetails = Option(CommonBuilder.buildRegistrationDetails1),
        getRegDetailsFromCache = true)

      val result = checkedEverythingQuestionController.onSubmit()(request)
      status(result) must be(BAD_REQUEST)
      val resultAsString = contentAsString(result)
      resultAsString must include(messagesApi("error.problem"))
      resultAsString must include(messagesApi("error.hasCheckedEverything.select"))
    }
  }
}

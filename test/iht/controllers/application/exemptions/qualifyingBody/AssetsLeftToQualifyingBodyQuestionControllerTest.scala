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

package iht.controllers.application.exemptions.qualifyingBody

import iht.config.AppConfig
import iht.controllers.application.ApplicationControllerTest
import iht.forms.ApplicationForms.assetsLeftToQualifyingBodyQuestionForm
import iht.models.application.exemptions.BasicExemptionElement
import iht.testhelpers.{CommonBuilder, ContentChecker}
import iht.views.html.application.exemption.qualifyingBody.assets_left_to_qualifying_body_question
import play.api.mvc.MessagesControllerComponents
import play.api.test.Helpers._
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController

/**
 * Created by james on 16/08/16.
 */
class AssetsLeftToQualifyingBodyQuestionControllerTest extends ApplicationControllerTest {

  protected abstract class TestController extends FrontendController(mockControllerComponents) with AssetsLeftToQualifyingBodyQuestionController {
    override val cc: MessagesControllerComponents = mockControllerComponents
    override implicit val appConfig: AppConfig = mockAppConfig
    override val assetsLeftToQualifyingBodyQuestionView: assets_left_to_qualifying_body_question = app.injector.instanceOf[assets_left_to_qualifying_body_question]
  }

  def assetsLeftToQualifyingBodyQuestionController = new TestController {
    override val authConnector = mockAuthConnector
    override val cachingConnector = mockCachingConnector
    override val ihtConnector = mockIhtConnector
  }

  def assetsLeftToQualifyingBodyQuestionControllerNotAuthorised = new TestController {
    override val authConnector = mockAuthConnector
    override val cachingConnector = mockCachingConnector
    override val ihtConnector = mockIhtConnector
  }

  "AssetsLeftToQualifyingBodyQuestionControllerTest" must {


    "redirect to login page on page load if the user is not logged in" in {
      val result = assetsLeftToQualifyingBodyQuestionControllerNotAuthorised.onPageLoad(createFakeRequest(isAuthorised = false))
      status(result) must be (SEE_OTHER)
      redirectLocation(result) must be(Some(loginUrl))
    }

    "redirect to login page on submit if the user is not logged in" in {
      val result = assetsLeftToQualifyingBodyQuestionControllerNotAuthorised.onPageLoad(createFakeRequest(isAuthorised = false))
      status(result) must be (SEE_OTHER)
      redirectLocation(result) must be (Some(loginUrl))
    }

    "respond with OK on page load" in {
      val applicationDetails = CommonBuilder.buildApplicationDetails

      createMocksForApplication(mockCachingConnector,
        mockIhtConnector,
        appDetails = Some(applicationDetails),
        getAppDetails = true,
        saveAppDetails = true,
        storeAppDetailsInCache = true)

      val result = assetsLeftToQualifyingBodyQuestionController.onPageLoad(createFakeRequest())
      status(result) must be(OK)
    }

    "display the correct content on the page on page load" in {
      val applicationDetails = CommonBuilder.buildApplicationDetails
      val regDetails = CommonBuilder.buildRegistrationDetails1
      val deceasedName = regDetails.deceasedDetails.map(_.name).fold("")(identity)

      createMocksForApplication(mockCachingConnector,
        mockIhtConnector,
        regDetails = regDetails,
        appDetails = Some(applicationDetails),
        getAppDetails = true,
        saveAppDetails = true,
        storeAppDetailsInCache = true)

      val result = assetsLeftToQualifyingBodyQuestionController.onPageLoad(createFakeRequest())
      val resultAsString = ContentChecker.stripLineBreaks(contentAsString(result))
      resultAsString must include (messagesApi("iht.saveAndContinue"))
      resultAsString must include (messagesApi("page.iht.application.exemptions.assetsLeftToQualifyingBody.sectionTitle", deceasedName))
    }

    "save application and go to Exemptions Overview page on submit" in {
      val applicationDetails = CommonBuilder.buildApplicationDetails.copy(
        allExemptions = Some(CommonBuilder.buildAllExemptions.copy(qualifyingBody = Some(BasicExemptionElement(isSelected = Some(true))))))

      createMocksForApplication(mockCachingConnector,
        mockIhtConnector,
        appDetails = Some(applicationDetails),
        getAppDetails = true,
        saveAppDetails = true,
        storeAppDetailsInCache = true)

      val assetsLeftToQualifyingBody = BasicExemptionElement(isSelected = Some(true))

      val filledAssetLeftToQualifyingBodyQuestionForm = assetsLeftToQualifyingBodyQuestionForm.fill(assetsLeftToQualifyingBody)
      implicit val request = createFakeRequest().withFormUrlEncodedBody(filledAssetLeftToQualifyingBodyQuestionForm.data.toSeq: _*).withMethod("POST")

      val result = assetsLeftToQualifyingBodyQuestionController.onSubmit(request)
      status(result) must be (SEE_OTHER)
    }

    "display validation message when incomplete form is submitted" in {
      val applicationDetails = CommonBuilder.buildApplicationDetails.copy(
        allExemptions = Some(CommonBuilder.buildAllExemptions.copy(qualifyingBody = Some(BasicExemptionElement(isSelected = None)))))

      val filledAssetsLeftToQualifyingBodyQuestionForm = assetsLeftToQualifyingBodyQuestionForm.fill(BasicExemptionElement(isSelected = None))

      implicit val request = createFakeRequest().withFormUrlEncodedBody(filledAssetsLeftToQualifyingBodyQuestionForm.data.toSeq: _*).withMethod("POST")

      createMocksForApplication(mockCachingConnector,
        mockIhtConnector,
        appDetails = Some(applicationDetails),
        getAppDetails = true,
        saveAppDetails = true,
        storeAppDetailsInCache = true)

      val result = assetsLeftToQualifyingBodyQuestionController.onSubmit()(request)
      status(result) must be (BAD_REQUEST)
      contentAsString(result) must include(messagesApi("error.problem"))

    }

    "updating application details with No chosen, blanks the qualifying body list and sets the value to No" in {
      val applicationDetails = CommonBuilder.buildApplicationDetails.copy(qualifyingBodies = Seq(CommonBuilder.qualifyingBody))
      val qualifyingBody = BasicExemptionElement(Some(false))
      val result = assetsLeftToQualifyingBodyQuestionController.updateApplicationDetails(applicationDetails, None, qualifyingBody)
      result._1.qualifyingBodies must be (Nil)
      result._1.allExemptions.flatMap(_.qualifyingBody.flatMap(_.isSelected)) must be (Some(false))
    }

    "update application details with Yes chosen, keeps the qualifying body list and sets value to Yes" in {
      val applicationDetails = CommonBuilder.buildApplicationDetails.copy(qualifyingBodies = Seq(CommonBuilder.qualifyingBody))
      val qualifyingBody = BasicExemptionElement(Some(true))
      val result = assetsLeftToQualifyingBodyQuestionController.updateApplicationDetails(applicationDetails, None, qualifyingBody)
      result._1.qualifyingBodies must be (Seq(CommonBuilder.qualifyingBody))
      result._1.allExemptions.flatMap(_.qualifyingBody.flatMap(_.isSelected)) must be (Some(true))
    }

    behave like controllerOnPageLoadWithNoExistingRegistrationDetails(mockCachingConnector,
      assetsLeftToQualifyingBodyQuestionController.onPageLoad(createFakeRequest()))
  }

}

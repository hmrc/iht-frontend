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

package iht.controllers.application.tnrb

import iht.connector.{CachingConnector, IhtConnector}
import iht.controllers.application.ApplicationControllerTest
import iht.forms.TnrbForms._
import iht.models.application.tnrb.WidowCheck
import iht.testhelpers.CommonBuilder
import iht.testhelpers.MockObjectBuilder._
import iht.utils.CommonHelper
import iht.utils.tnrb.TnrbHelper
import iht.views.HtmlSpec
import org.jsoup.select.Elements
import play.api.i18n.Messages
import play.api.test.Helpers._

/**
  *
  * Created by Vineet Tyagi on 14/01/16.
  * l
  */
class DeceasedWidowCheckQuestionControllerTest extends ApplicationControllerTest with HtmlSpec {

  val mockCachingConnector = mock[CachingConnector]
  var mockIhtConnector = mock[IhtConnector]

  def deceasedWidowCheckQuestionController = new DeceasedWidowCheckQuestionController {
    override val authConnector = createFakeAuthConnector(isAuthorised = true)
    override val cachingConnector = mockCachingConnector
    override val ihtConnector = mockIhtConnector
  }

  def deceasedWidowCheckQuestionControllerNotAuthorised = new DeceasedWidowCheckQuestionController {
    override val authConnector = createFakeAuthConnector(isAuthorised = false)
    override val cachingConnector = mockCachingConnector
    override val ihtConnector = mockIhtConnector
  }

  before {
    mockIhtConnector = mock[IhtConnector]
  }

  "DeceasedWidowCheckQuestionController" must {

    "redirect to login page onPageLoad if the user is not logged in" in {
      val result = deceasedWidowCheckQuestionController.onPageLoad(createFakeRequest(isAuthorised = false))
      status(result) should be(SEE_OTHER)
      redirectLocation(result) should be(Some(loginUrl))
    }

    "redirect to ida login page on Submit if the user is not logged in" in {
      val result = deceasedWidowCheckQuestionController.onSubmit(createFakeRequest(isAuthorised = false))
      status(result) should be(SEE_OTHER)
      redirectLocation(result) should be(Some(loginUrl))
    }
  }

  "onSubmit" must {
    "save application and go to date when the deceased's partner died page on submit if the deceased is widowed" in {
      val applicationDetails = CommonBuilder.buildApplicationDetails.copy(widowCheck =
        Some(CommonBuilder.buildWidowedCheck.copy(dateOfPreDeceased = None)))

      createMocksForApplication(mockCachingConnector,
        mockIhtConnector,
        appDetails = Some(applicationDetails),
        getAppDetails = true,
        saveAppDetails = true)

      val withWidowedValue = CommonBuilder.buildWidowedCheck

      val filledDeceasedWidowCheckQuestionForm = deceasedWidowCheckQuestionForm.fill(withWidowedValue)
      implicit val request = createFakeRequest().withFormUrlEncodedBody(filledDeceasedWidowCheckQuestionForm.data.toSeq: _*)

      val result = deceasedWidowCheckQuestionController.onSubmit(request)
      status(result) shouldBe SEE_OTHER
      redirectLocation(result) should be(Some(routes.DeceasedWidowCheckDateController.onPageLoad().url))
    }

    "save application and go to Widow check date page on submit if WidowCheck section is not completed" in {
      val applicationDetails = CommonBuilder.buildApplicationDetails.copy(
        widowCheck = Some(CommonBuilder.buildWidowedCheck copy (dateOfPreDeceased = None)))

      createMocksForApplication(mockCachingConnector,
        mockIhtConnector,
        appDetails = Some(applicationDetails),
        getAppDetails = true,
        saveAppDetails = true)

      val withWidowedValue = CommonBuilder.buildWidowedCheck

      val filledDeceasedWidowCheckQuestionForm = deceasedWidowCheckQuestionForm.fill(withWidowedValue)
      implicit val request = createFakeRequest().withFormUrlEncodedBody(filledDeceasedWidowCheckQuestionForm.data.toSeq: _*)

      val result = deceasedWidowCheckQuestionController.onSubmit(request)
      status(result) shouldBe SEE_OTHER
      redirectLocation(result) should be(Some(routes.DeceasedWidowCheckDateController.onPageLoad().url))
    }

    "go to estate overview page on submit when the deceased is not widowed" in {
      val ihtRef = "IhtRef"
      val applicationDetails = CommonBuilder.buildApplicationDetails copy (ihtRef = Some(ihtRef))

      createMocksForApplication(mockCachingConnector,
        mockIhtConnector,
        appDetails = Some(applicationDetails),
        getAppDetails = true,
        saveAppDetails = true)

      val withWidowedValue = WidowCheck(Some(false), None)

      val filledDeceasedWidowCheckQuestionForm = deceasedWidowCheckQuestionForm.fill(withWidowedValue)
      implicit val request = createFakeRequest().withFormUrlEncodedBody(filledDeceasedWidowCheckQuestionForm.data.toSeq: _*)

      val result = deceasedWidowCheckQuestionController.onSubmit(request)
      status(result) shouldBe SEE_OTHER
      redirectLocation(result) should be(Some(iht.controllers.application.routes.EstateOverviewController
        .onPageLoadWithIhtRef(ihtRef).url))
    }

    "go to Tnrb overview page on submit when the deceased selects Yes in Edit mode" in {
      val ihtRef = "IhtRef"
      lazy val tnrbModel = CommonBuilder.buildTnrbEligibility.copy(None, None,None,None,None,None,None,None,None,None,None)
      val applicationDetails = CommonBuilder.buildApplicationDetails copy (ihtRef = Some(ihtRef),
        widowCheck = Some(CommonBuilder.buildWidowedCheck),
        increaseIhtThreshold = Some(tnrbModel))

      createMocksForApplication(mockCachingConnector,
        mockIhtConnector,
        appDetails = Some(applicationDetails),
        getAppDetails = true,
        saveAppDetails = true)

      val widowedCheckComplete = CommonBuilder.buildWidowedCheck

      val filledDeceasedWidowCheckQuestionForm = deceasedWidowCheckQuestionForm.fill(widowedCheckComplete)
      implicit val request = createFakeRequest().withFormUrlEncodedBody(filledDeceasedWidowCheckQuestionForm.data.toSeq: _*)

      val result = deceasedWidowCheckQuestionController.onSubmit(request)
      status(result) shouldBe SEE_OTHER
      redirectLocation(result) should be(Some(iht.controllers.application.tnrb.routes.TnrbOverviewController.onPageLoad()url))
    }

    "wipe out the WidowCheck date and tnrb eligibility data, go to estate overview page on submit " +
      "when user selects no to Deceased widowed question" in {

      val ihtRef = "IhtRef"
      val withWidowedValue = CommonBuilder.buildWidowedCheck.copy(widowed = Some(false),
        dateOfPreDeceased = Some(CommonBuilder.DefaultDateOfPreDeceased))
      val tnrbEligibilty = CommonBuilder.buildTnrbEligibility.copy(dateOfPreDeceased = Some(CommonBuilder.DefaultDateOfPreDeceased))
      val applicationDetails = CommonBuilder.buildApplicationDetails copy(ihtRef = Some(ihtRef),
        widowCheck = Some(withWidowedValue),
        increaseIhtThreshold = Some(tnrbEligibilty))

      createMocksForApplication(mockCachingConnector,
        mockIhtConnector,
        appDetails = Some(applicationDetails),
        getAppDetails = true,
        saveAppDetails = true)

      val filledDeceasedWidowCheckQuestionForm = deceasedWidowCheckQuestionForm.fill(withWidowedValue)
      implicit val request = createFakeRequest().withFormUrlEncodedBody(filledDeceasedWidowCheckQuestionForm.data.toSeq: _*)

      val result = deceasedWidowCheckQuestionController.onSubmit(request)
      status(result) shouldBe SEE_OTHER
      redirectLocation(result) should be(Some(iht.controllers.application.routes.EstateOverviewController
        .onPageLoadWithIhtRef(ihtRef).url))

      val capturedValue = verifyAndReturnSavedApplicationDetails(mockIhtConnector)

      val expectedAppDetails = applicationDetails.copy(
        widowCheck = Some(withWidowedValue.copy(dateOfPreDeceased = None)),
        increaseIhtThreshold = None
      )

      capturedValue shouldBe expectedAppDetails
    }
  }

  "onPageLoad" must {

    "respond with OK" in {
      val applicationDetails = CommonBuilder.buildApplicationDetails.copy(widowCheck = Some(CommonBuilder.buildWidowedCheck))

      createMocksForApplication(mockCachingConnector,
        mockIhtConnector,
        appDetails = Some(applicationDetails),
        getAppDetails = true,
        saveAppDetails = true)

      val result = deceasedWidowCheckQuestionController.onPageLoad(createFakeRequest())
      status(result) shouldBe OK
    }

    def setupMocksForTitleTests = {
      val applicationDetails = CommonBuilder.buildApplicationDetails.copy(widowCheck = Some(CommonBuilder.buildWidowedCheck),
        increaseIhtThreshold = Some(CommonBuilder.buildTnrbEligibility.copy(firstName = Some(CommonBuilder.firstNameGenerator),
          lastName = Some(CommonBuilder.surnameGenerator))))

      createMocksForApplication(mockCachingConnector,
        mockIhtConnector,
        appDetails = Some(applicationDetails),
        getAppDetails = true,
        saveAppDetails = true)

      applicationDetails
    }

    "return html containing link which points to estate overview when widow check date is empty" in {
      val ihtRef = "ihtRef"
      val applicationDetails = CommonBuilder.buildApplicationDetails.copy(ihtRef = Some(ihtRef),
        widowCheck = Some(CommonBuilder.buildWidowedCheck copy (dateOfPreDeceased = None)),
        increaseIhtThreshold = Some(CommonBuilder.buildTnrbEligibility.copy(firstName = Some(CommonBuilder.firstNameGenerator),
          lastName = Some(CommonBuilder.surnameGenerator))))

      createMocksForApplication(mockCachingConnector,
        mockIhtConnector,
        appDetails = Some(applicationDetails),
        getAppDetails = true,
        saveAppDetails = true)

      val expectedUrl = iht.controllers.application.routes.EstateOverviewController.onPageLoadWithIhtRef(ihtRef).url

      val result = deceasedWidowCheckQuestionController.onPageLoad(createFakeRequest())
      status(result) shouldBe OK
      val doc = asDocument(contentAsString(result))
      assertRenderedById(doc, "cancel-button")

      val link = doc.getElementById("cancel-button")
      link.text() shouldBe Messages("iht.estateReport.returnToEstateOverview")
      link.attr("href") shouldBe expectedUrl
    }

    "return html containing link which points to tnrb overview when widow check date is not empty" in {
      val applicationDetails = CommonBuilder.buildApplicationDetails.copy(widowCheck = Some(CommonBuilder.buildWidowedCheck),
        increaseIhtThreshold = Some(CommonBuilder.buildTnrbEligibility.copy(firstName = Some(CommonBuilder.firstNameGenerator),
          lastName = Some(CommonBuilder.surnameGenerator))))

      createMocksForApplication(mockCachingConnector,
        mockIhtConnector,
        appDetails = Some(applicationDetails),
        getAppDetails = true,
        saveAppDetails = true)

      val expectedUrl = iht.controllers.application.tnrb.routes.TnrbOverviewController.onPageLoad.url

      val result = deceasedWidowCheckQuestionController.onPageLoad(createFakeRequest())
      status(result) shouldBe OK
      val doc = asDocument(contentAsString(result))
      assertRenderedById(doc, "cancel-button")

      val link = doc.getElementById("cancel-button")
      link.text() shouldBe Messages("page.iht.application.tnrb.returnToIncreasingThreshold")
      link.attr("href") shouldBe expectedUrl
    }
  }
}

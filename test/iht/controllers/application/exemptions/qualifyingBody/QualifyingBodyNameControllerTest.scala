/*
 * Copyright 2016 HM Revenue & Customs
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

import iht.connector.{CachingConnector, IhtConnector}
import iht.controllers.application.ApplicationControllerTest
import iht.models.application.exemptions.QualifyingBody
import iht.testhelpers.CommonBuilder
import iht.testhelpers.MockObjectBuilder._
import org.scalatest.BeforeAndAfter
import play.api.data.Form
import play.api.i18n.Messages
import play.api.mvc.{AnyContentAsFormUrlEncoded, Request}
import play.api.test.Helpers._

/**
 * Copied by yasar and grant from jennygj on 01/09/16.
 */

class QualifyingBodyNameControllerTest extends ApplicationControllerTest with BeforeAndAfter {

  val mockCachingConnector = mock[CachingConnector]
  var mockIhtConnector = mock[IhtConnector]
  val QualifyingBody1Name = "Qualifying Body 1"
  val QualifyingBody2Name = "Qualifying Body 2"
  val QualifyingBodyNameAtLengthBoundary = "a" * 35
  val QualifyingBody1Value = BigDecimal(324)
  val QualifyingBody2Value = BigDecimal(65454)
  val qualifyingBody1 = QualifyingBody(Some("1"), Some(QualifyingBody1Name), Some(QualifyingBody1Value))
  val qualifyingBody2 = QualifyingBody(Some("2"), Some(QualifyingBody2Name), Some(QualifyingBody2Value))
  val referrerURL = "localhost:9070"

  before {
    mockIhtConnector = mock[IhtConnector]
  }

  def qualifyingBodyNameController = new QualifyingBodyNameController {
    override val authConnector = createFakeAuthConnector(isAuthorised = true)
    override val cachingConnector = mockCachingConnector
    override val ihtConnector = mockIhtConnector
  }

  def qualifyingBodyNameControllerNotAuthorised = new QualifyingBodyNameController {
    override val authConnector = createFakeAuthConnector(isAuthorised = false)
    override val cachingConnector = mockCachingConnector
    override val ihtConnector = mockIhtConnector
  }

  def createMocksForQualifyingBodyName = {
    val ad = CommonBuilder.buildApplicationDetails copy (qualifyingBodies = Seq(qualifyingBody1))
    createMocksForApplication(mockCachingConnector,
      mockIhtConnector,
      appDetails = Some(ad),
      getAppDetails = true,
      saveAppDetails = true,
      storeAppDetailsInCache = true)
  }

  def createMocksForQualifyingBodyNameWithTwoItems = {
    val ad = CommonBuilder.buildApplicationDetails copy (qualifyingBodies = Seq(qualifyingBody1, qualifyingBody2))
    createMocksForApplication(mockCachingConnector,
      mockIhtConnector,
      appDetails = Some(ad),
      getAppDetails = true,
      saveAppDetails = true,
      storeAppDetailsInCache = true)
  }

  lazy val resultOnPageLoadNotAuthorised =
    qualifyingBodyNameControllerNotAuthorised.onPageLoad(createFakeRequest())
  def resultOnEditPageLoadNotAuthorised(id: String) =
    qualifyingBodyNameControllerNotAuthorised.onEditPageLoad(id)(createFakeRequest())
  def resultOnSubmitNotAuthorised(request: Request[AnyContentAsFormUrlEncoded])=
    qualifyingBodyNameControllerNotAuthorised.onSubmit(request)
  def resultOnEditSubmitNotAuthorised(id: String) =
    qualifyingBodyNameControllerNotAuthorised.onEditSubmit(id)(createFakeRequest())

  lazy val resultOnPageLoad =
    qualifyingBodyNameController.onPageLoad(createFakeRequest())
  def resultOnEditPageLoad(id: String) =
    qualifyingBodyNameController.onEditPageLoad(id)(createFakeRequest())
  def resultOnSubmit(request: Request[AnyContentAsFormUrlEncoded]) =
    qualifyingBodyNameController.onSubmit(request)
  def resultOnEditSubmit(id: String)(request: Request[AnyContentAsFormUrlEncoded]) =
    qualifyingBodyNameController.onEditSubmit(id)(request)

  def request(form: Form[_]) =
    createFakeRequestWithReferrerWithBody(referrerURL = referrerURL, host = "localhost:9070", data = form.data.toSeq)

  "QualifyingBodyNameControllerTest" must {

    "redirect to log in page onPageLoad if user is not logged in" in {
      status(resultOnPageLoadNotAuthorised) should be(SEE_OTHER)
      redirectLocation(resultOnPageLoadNotAuthorised) should be(Some(loginUrl))
    }

    "redirect to log in page onEditPageLoad if user is not logged in" in {
      status(resultOnEditPageLoadNotAuthorised("1")) should be(SEE_OTHER)
      redirectLocation(resultOnEditPageLoadNotAuthorised("1")) should be(Some(loginUrl))
    }

    "redirect to log in page onSubmit if user is not logged in" in {
      status(resultOnSubmitNotAuthorised(createFakeRequest().withFormUrlEncodedBody((
        "totalValue", "101.00")))) should be(SEE_OTHER)
      redirectLocation(resultOnSubmitNotAuthorised(createFakeRequest().withFormUrlEncodedBody((
        "totalValue", "101.00")))) should be(Some(loginUrl))
    }

    "redirect to log in page onEditSubmit if user is not logged in" in {
      status(resultOnEditSubmitNotAuthorised("1")) should be(SEE_OTHER)
      redirectLocation(resultOnEditSubmitNotAuthorised("1")) should be(Some(loginUrl))
    }

    "return OK onPageLoad" in {
      createMocksForQualifyingBodyName
      status(resultOnPageLoad) should be(OK)
    }

    "display the correct content onPageLoad" in {
      createMocksForQualifyingBodyName
      val resultAsString = contentAsString(resultOnPageLoad)
      resultAsString should include(Messages("page.iht.application.exemptions.qualifyingBody.name.sectionTitle"))
      resultAsString should include(Messages("page.iht.application.exemptions.qualifyingBody.name.browserTitle"))
      resultAsString should include(Messages("iht.estateReport.exemptions.qualifyingBodies.returnToAssetsLeftToQualifyingBody"))
      resultAsString should include(Messages("iht.saveAndContinue"))
    }

    "display the correct content onEditPageLoad" in {
      createMocksForQualifyingBodyName
      val result = resultOnEditPageLoad("1")

      val resultAsString = contentAsString(result)
      resultAsString should include(Messages("page.iht.application.exemptions.qualifyingBody.name.sectionTitle"))
      resultAsString should include(Messages("page.iht.application.exemptions.qualifyingBody.name.browserTitle"))
      resultAsString should include(Messages("iht.estateReport.exemptions.qualifyingBodies.returnToAssetsLeftToQualifyingBody"))
      resultAsString should include(Messages("iht.saveAndContinue"))
      resultAsString should include(QualifyingBody1Name)
    }

    "display errors when a blank value is submitted" in {
      createMocksForQualifyingBodyName
      implicit val fakePostRequest = createFakeRequest().withFormUrlEncodedBody(("name", ""))
      val result = resultOnSubmit(fakePostRequest)

      status(result) shouldBe BAD_REQUEST
      contentAsString(result) should include("a problem")
    }

    "save new value with new ID to application details onSubmit where exactly 36 characters in length and redirect to QB detail overview" in {
      createMocksForQualifyingBodyName
      implicit val fakePostRequest = createFakeRequest().withFormUrlEncodedBody(("name", QualifyingBodyNameAtLengthBoundary))
      val result = resultOnSubmit(fakePostRequest)

      status(result) shouldBe SEE_OTHER
      // TODO: redirectLocation(result) shouldBe
      val appDetails = verifyAndReturnSavedApplicationDetails(mockIhtConnector)
      appDetails.qualifyingBodies.size shouldBe 2
      appDetails.qualifyingBodies.tail.head shouldBe QualifyingBody(Some("2"), Some(QualifyingBodyNameAtLengthBoundary), None)
    }

    "save new value with new ID to application details onSubmit and redirect to QB detail overview" in {
      createMocksForQualifyingBodyName
      implicit val fakePostRequest = createFakeRequest().withFormUrlEncodedBody(("name", QualifyingBody1Name))
      val result = resultOnSubmit(fakePostRequest)

      status(result) shouldBe SEE_OTHER
      // TODO: redirectLocation(result) shouldBe
      val appDetails = verifyAndReturnSavedApplicationDetails(mockIhtConnector)
      appDetails.qualifyingBodies.size shouldBe 2
      appDetails.qualifyingBodies.tail.head shouldBe QualifyingBody(Some("2"), Some(QualifyingBody1Name), None)
    }

    "display previously entered value on EditPageLoad" in {
      createMocksForQualifyingBodyName
      contentAsString(resultOnEditPageLoad("1")) should include(QualifyingBody1Name)
    }

    "amend existing value with ID 1 in application details onEditSubmit and redirect to QB detail overview" in {
      createMocksForQualifyingBodyNameWithTwoItems
      implicit val fakePostRequest = createFakeRequest().withFormUrlEncodedBody(("name", QualifyingBody2Name))
      val result = resultOnEditSubmit("1")(fakePostRequest)

      status(result) shouldBe SEE_OTHER
      // TODO: redirectLocation(result) shouldBe
      val appDetails = verifyAndReturnSavedApplicationDetails(mockIhtConnector)
      appDetails.qualifyingBodies.size shouldBe 2
      appDetails.qualifyingBodies.head shouldBe QualifyingBody(Some("1"), Some(QualifyingBody2Name), Some(QualifyingBody1Value))
    }

    "amend existing value with ID 2 in application details onEditSubmit and redirect to QB detail overview" in {
      createMocksForQualifyingBodyNameWithTwoItems
      implicit val fakePostRequest = createFakeRequest().withFormUrlEncodedBody(("name", QualifyingBody1Name))
      val result = resultOnEditSubmit("2")(fakePostRequest)

      status(result) shouldBe SEE_OTHER
      // TODO: redirectLocation(result) shouldBe
      val appDetails = verifyAndReturnSavedApplicationDetails(mockIhtConnector)
      appDetails.qualifyingBodies.size shouldBe 2
      appDetails.qualifyingBodies.tail.head shouldBe QualifyingBody(Some("2"), Some(QualifyingBody1Name), Some(QualifyingBody2Value))
    }

    "return an internal server error if onPageLoad for invalid ID is entered" in {
      createMocksForQualifyingBodyName
      a[RuntimeException] shouldBe thrownBy {
       await(resultOnEditPageLoad("10"))
      }
    }
  }
}

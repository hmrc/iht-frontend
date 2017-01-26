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

package iht.controllers.pdf

import iht.connector.{CachingConnector, IhtConnector}
import iht.controllers.application.ApplicationControllerTest
import iht.controllers.application.pdf.PDFController
import iht.models.RegistrationDetails
import iht.testhelpers.CommonBuilder
import iht.testhelpers.MockObjectBuilder._
import iht.utils.pdf.XmlFoToPDF
import play.api.test.Helpers._

/**
  * Created by david-beer on 21/11/16.
  */
class PDFControllerTest extends ApplicationControllerTest {

  val mockCachingConnector = mock[CachingConnector]
  var mockIhtConnector = mock[IhtConnector]

  val ihtRef = "1A1A1A"

  def pdfController = new PDFController {
    val authConnector = createFakeAuthConnector(isAuthorised = true)
    override val cachingConnector = mockCachingConnector
    override val ihtConnector = mockIhtConnector
    lazy val xmlFoToPDF = XmlFoToPDF
  }

  private def setUpMocks() {
    val regDetails: RegistrationDetails = CommonBuilder.buildRegistrationDetails1.copy(ihtReference = Some(ihtRef),
      returns = Seq(CommonBuilder.buildReturnDetails))

    createMockToGetExistingRegDetailsFromCache(mockCachingConnector, regDetails)
    createMockToGetCaseDetails(mockIhtConnector, regDetails)
    createMockToGetSubmittedApplicationDetails(mockIhtConnector)
    createMockToGetApplicationDetails(mockIhtConnector)
  }

  before {
    mockIhtConnector = mock[IhtConnector]
  }

  "onClearancePDF" must {
    "have correct contents for the certificate" in {
      setUpMocks
      val result = pdfController.onClearancePDF(ihtRef)(createFakeRequest())
      contentAsBytes(result).length should be > 0
    }
  }

  "onPreSubmissionPDF" must {
    "generate correct contents" in {
      setUpMocks
      val result = pdfController.onPreSubmissionPDF(createFakeRequest())
      contentAsBytes(result).length should be > 0
    }
  }

  "onPostSubmissionPDF" must {
    "generate correct contents" in {
      setUpMocks
      val result = pdfController.onPostSubmissionPDF(ihtRef)(createFakeRequest())
      contentAsBytes(result).length should be > 0
    }
  }

}

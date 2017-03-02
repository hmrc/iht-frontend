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

package iht.utils.pdf

import iht.FakeIhtApp
import iht.testhelpers.CommonBuilder
import org.joda.time.LocalDate
import org.scalatest.mock.MockitoSugar
import uk.gov.hmrc.play.test.UnitSpec
import play.api.i18n.MessagesApi

/**
  * Created by david-beer on 21/11/16.
  */
class XmlFoToPDFTest extends UnitSpec with FakeIhtApp with MockitoSugar {
  implicit val messagesApi: MessagesApi = app.injector.instanceOf[MessagesApi]

  lazy val regDetails = CommonBuilder.buildRegistrationDetails1
  lazy val appDetails = CommonBuilder.buildApplicationDetails

  "formatForDisplay" must {
    "transform the marital status" in {
      val rd = XmlFoToPDF.formatForDisplay(regDetails)
      val result = rd.deceasedDetails.flatMap(_.maritalStatus).fold("")(identity)
      result shouldBe messagesApi("page.iht.registration.deceasedDetails.maritalStatus.single.label")
    }
  }

  "XmlFoToPDF.createClearancePDF" must {
    "have correct contents for the certificate" in {
      val declarationDate = new LocalDate(2015, 10, 10)

      val result: Array[Byte] = XmlFoToPDF.createClearancePDF(regDetails, declarationDate)
      result.length should be >0
    }

    "have correct contents for the Pre Submission PDF" in {
      val result: Array[Byte] = XmlFoToPDF.createPreSubmissionPDF(regDetails, appDetails, "declaration_type")
      result.length should be >0
    }

    "have correct contents for the Post Submission PDF" in {
      lazy val ihtReturn = CommonBuilder.buildIHTReturn

      val result: Array[Byte] = XmlFoToPDF.createPostSubmissionPDF(regDetails, ihtReturn)
      result.length should be >0
    }
  }

}

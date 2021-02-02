/*
 * Copyright 2021 HM Revenue & Customs
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

import iht.config.AppConfig
import iht.forms.FormTestHelper
import iht.testhelpers.CommonBuilder
import org.joda.time.LocalDate

class XmlFoToPDFTest extends FormTestHelper {

  lazy val regDetails = CommonBuilder.buildRegistrationDetails1
  lazy val appDetails = CommonBuilder.buildApplicationDetails

  class Setup {
    val xmlFoToPDF: XmlFoToPDF = new XmlFoToPDF {
      override val resourceStreamResolver: BaseResourceStreamResolver = app.injector.instanceOf[BaseResourceStreamResolver]
      override val stylesheetResourceStreamResolver: StylesheetResourceStreamResolver = app.injector.instanceOf[StylesheetResourceStreamResolver]
      override val fopURIResolver: FopURIResolver = app.injector.instanceOf[FopURIResolver]
      override implicit val appConfig: AppConfig = mockAppConfig
    }
  }

  "XmlFoToPDF.createClearancePDF" must {
    "have correct contents for the certificate" in new Setup {
      val declarationDate = new LocalDate(2015, 10, 10)

      val result: Array[Byte] = xmlFoToPDF.createClearancePDF(regDetails, declarationDate, messages)
      result.length must be >0
    }

    "have correct contents for the Pre Submission PDF" in new Setup {
      val result: Array[Byte] = xmlFoToPDF.createPreSubmissionPDF(regDetails, appDetails, "declaration_type", messages)
      result.length must be >0
    }

    "have correct contents for the Post Submission PDF" in new Setup {
      lazy val ihtReturn = CommonBuilder.buildIHTReturn

      val result: Array[Byte] = xmlFoToPDF.createPostSubmissionPDF(regDetails, ihtReturn, messages)
      result.length must be >0
    }
  }

}

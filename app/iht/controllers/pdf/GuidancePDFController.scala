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

import javax.inject.Singleton

import akka.stream.scaladsl.{FileIO, Source}
import akka.util.ByteString
import iht.constants.Constants
import iht.controllers.auth.CustomPasscodeAuthentication
import play.api.http.HttpEntity
import play.api.mvc.{ResponseHeader, Result}
import uk.gov.hmrc.play.frontend.controller.{FrontendController, UnauthorisedAction}

@Singleton
class GuidancePDFController extends FrontendController with CustomPasscodeAuthentication {

  def loadPDF = UnauthorisedAction {
   implicit request => {
     val source: Source[ByteString, _] = FileIO.fromPath(Constants.pDFHMRCGuidance)
     Result(
       header = ResponseHeader(200, Map.empty),
       body = HttpEntity.Streamed(source, None, Some("application/pdf"))
     )
   }
  }
}

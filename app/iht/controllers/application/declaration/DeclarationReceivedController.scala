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


import iht.connector.CachingConnector
import iht.constants.Constants
import iht.controllers.IhtConnectors
import iht.controllers.application.ApplicationController
import iht.models.application.ProbateDetails
import iht.utils.CommonHelper

import scala.concurrent.duration.Duration
import scala.concurrent.{Await, Future}

/**
  * Created by vineet on 01/12/16.
  */

object DeclarationReceivedController extends DeclarationReceivedController with IhtConnectors

trait DeclarationReceivedController extends ApplicationController {
  def cachingConnector: CachingConnector

  def onPageLoad = authorisedForIht {
    implicit user => implicit request => {
      val rd = cachingConnector.getExistingRegistrationDetails
      val ihtReference = CommonHelper.getOrException(rd.ihtReference)
      val probateDetails: Option[ProbateDetails] = Await.result(cachingConnector.getProbateDetails, Duration.Inf)

      cachingConnector.storeSingleValue(Constants.PDFIHTReference, ihtReference).flatMap { _ =>
        Future.successful(Ok(iht.views.html.application.declaration.declaration_received(probateDetails,
          rd)))
      }
    }
  }
}

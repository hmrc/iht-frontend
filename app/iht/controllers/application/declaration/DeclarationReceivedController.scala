/*
 * Copyright 2019 HM Revenue & Customs
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


import iht.config.{AppConfig, FrontendAuthConnector}
import iht.connector.{CachingConnector, IhtConnectors}
import iht.constants.Constants
import iht.controllers.application.ApplicationController
import iht.utils.CommonHelper
import javax.inject.Inject
import play.api.Play.current
import play.api.i18n.Messages.Implicits._
import uk.gov.hmrc.auth.core.AuthConnector
import uk.gov.hmrc.auth.core.PlayAuthConnector

import scala.concurrent.Future

/**
  * Created by vineet on 01/12/16.
  */

class DeclarationReceivedControllerImpl @Inject()() extends DeclarationReceivedController with IhtConnectors

trait DeclarationReceivedController extends ApplicationController {


  def cachingConnector: CachingConnector

  def onPageLoad = authorisedForIht {

      implicit request => {
        withRegistrationDetails { rd =>
          val ihtReference = CommonHelper.getOrException(rd.ihtReference)
          cachingConnector.storeSingleValue(Constants.PDFIHTReference, ihtReference).flatMap { _ =>
            Future.successful(Ok(iht.views.html.application.declaration.declaration_received(rd)))
          }
        }
      }
  }
}

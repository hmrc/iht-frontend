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

import javax.inject.{Inject, Singleton}

import iht.controllers.application.EstateController
import iht.metrics.Metrics
import iht.utils._
import iht.utils.tnrb.TnrbHelper
import iht.views.html.application.tnrb.tnrb_guidance
import play.api.i18n.MessagesApi
import play.api.mvc.{Action, AnyContent}
import play.api.i18n.Messages.Implicits._
import play.api.Play.current
import scala.concurrent.Future

@Singleton
class TnrbGuidanceController @Inject() (implicit val messagesApi: MessagesApi) extends EstateController{
  def onPageLoad: Action[AnyContent] = authorisedForIht {
    implicit user => implicit request => {
      withRegistrationDetails { rd =>
          val ihtReference = CommonHelper.getOrException(rd.ihtReference)
          val deceasedName = CommonHelper.getDeceasedNameOrDefaultString(rd)
          Future.successful(Ok(tnrb_guidance(ihtReference,
            TnrbHelper.urlForIncreasingThreshold(CommonHelper.getOrException(rd.deceasedDetails.flatMap(_.maritalStatus))).url,
            deceasedName))
          )
      }
    }
  }
}

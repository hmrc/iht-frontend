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

import iht.controllers.IhtConnectors
import iht.controllers.application.ApplicationController
import iht.utils.CommonHelper

import scala.concurrent.Future

object QualifyingBodiesOverviewController extends QualifyingBodiesOverviewController with IhtConnectors

trait QualifyingBodiesOverviewController extends ApplicationController {

  def onPageLoad() = authorisedForIht {
    implicit user => implicit request => {

      withApplicationDetails {
        rd => ad => {

          val isAssetLeftToQualifyingBody = ad.allExemptions.flatMap(_.qualifyingBody).flatMap(_.isSelected)

          // TODO: Move error message to common place
          // TODO: Check that an exception is appropriate here.  Should it be refactored as a guard condition?
          Future.successful(Ok(iht.views.html.application.exemption.qualifyingBody.qualifying_bodies_overview(ad.qualifyingBodies,
            rd,
            CommonHelper.getOrException(isAssetLeftToQualifyingBody, "Illegal page navigation"))))
        }
      }
    }
  }
}

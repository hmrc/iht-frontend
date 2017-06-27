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

package iht.controllers.application.exemptions.charity

import javax.inject.Inject

import iht.controllers.application.EstateController
import iht.utils.CommonHelper
import play.api.Logger
import play.api.i18n.MessagesApi

import scala.concurrent.Future


class CharityDetailsOverviewController @Inject()(val messagesApi: MessagesApi) extends EstateController {
  def onPageLoad() = authorisedForIht {
    implicit user =>
      implicit request => {
        withApplicationDetails { rd =>
          ad =>
            Future.successful(Ok(iht.views.html.application.exemption.charity.charity_details_overview()))
        }
      }
  }

  def onEditPageLoad(id: String) = authorisedForIht {
    implicit user =>
      implicit request => {

        withRegistrationDetails { registrationDetails =>
          for {
            applicationDetails <- ihtConnector.getApplication(CommonHelper.getNino(user),
              CommonHelper.getOrExceptionNoIHTRef(registrationDetails.ihtReference),
              registrationDetails.acknowledgmentReference)
          } yield {
            applicationDetails match {
              case Some(applicationDetails) =>
                applicationDetails.charities.find(charity => charity.id.contains(id)).fold {
                  throw new RuntimeException("No charity found for the id")
                } {
                  (matchedCharity) =>
                    Ok(iht.views.html.application.exemption.charity.charity_details_overview(Some(matchedCharity)
                    ))
                }
              case _ =>
                Logger.warn("Problem retrieving Application Details. Redirecting to Internal Server Error")
                InternalServerError("No Application Details found")
            }
          }
        }
      }
  }
}

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

package iht.controllers.application.exemptions.charity

import iht.config.AppConfig
import iht.connector.{CachingConnector, IhtConnector}
import iht.controllers.application.EstateController
import iht.utils.StringHelper
import iht.views.html.application.exemption.charity.charity_delete_confirm
import javax.inject.Inject
import play.api.Logging
import play.api.mvc.MessagesControllerComponents
import uk.gov.hmrc.auth.core.AuthConnector
import uk.gov.hmrc.auth.core.retrieve.v2.Retrievals.{nino => ninoRetrieval}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController
import uk.gov.hmrc.play.partials.FormPartialRetriever

import scala.concurrent.Future

class CharityDeleteConfirmControllerImpl @Inject()(val ihtConnector: IhtConnector,
                                                   val cachingConnector: CachingConnector,
                                                   val authConnector: AuthConnector,
                                                   val formPartialRetriever: FormPartialRetriever,
                                                   implicit val appConfig: AppConfig,
                                                   val cc: MessagesControllerComponents) extends FrontendController(cc) with CharityDeleteConfirmController

trait CharityDeleteConfirmController extends EstateController with StringHelper with Logging {


  def onPageLoad(id: String) = authorisedForIhtWithRetrievals(ninoRetrieval) { userNino =>
    implicit request => {
      withApplicationDetails(userNino) {
        rd => ad => {
          Future.successful(ad.charities.find(_.id.contains(id)).fold {
            logger.warn("Charity with id = " + id + " not found during onLoad of delete confirmation")
            InternalServerError("Charity with id = " + id + " not found during onLoad of delete confirmation")
          } { c =>
            Ok(charity_delete_confirm(c, routes.CharityDeleteConfirmController.onSubmit(id)))
          })
        }
      }
    }
  }

  def onSubmit(id: String) = authorisedForIhtWithRetrievals(ninoRetrieval) { userNino =>
    implicit request => {
      withApplicationDetails(userNino) {
        rd => ad => {
          val index = ad.charities.indexWhere(_.id.contains(id))

          if (index == -1) {
             logger.warn("Charity with id = " + id + " not found during onSubmit of delete confirmation")
            Future.successful(InternalServerError("Charity with id = " + id
              + " not found during onSubmit of delete confirmation"))
          } else {
            val nino = getNino(userNino)
            val newCharities = ad.charities.patch(index, Nil, 1)
            val newAppDetails = ad copy (charities = newCharities)

            ihtConnector.saveApplication(nino, newAppDetails, rd.acknowledgmentReference).map {
              case Some(_) =>
                Redirect(iht.controllers.application.exemptions.charity.routes.CharitiesOverviewController.onPageLoad())
              case _ => {
                logger.warn("Save of app details fails with id = " + id
                  + " during save of app details during onSubmit of delete confirmation")
                InternalServerError("Save of app details fails with id = " + id
                  + " during save of app details during onSubmit of delete confirmation")
              }
            }
          }
        }
      }
    }
  }
}

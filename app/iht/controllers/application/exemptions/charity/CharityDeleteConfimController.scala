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

package iht.controllers.application.exemptions.charity

import iht.config.{AppConfig, FrontendAuthConnector}
import iht.connector.IhtConnectors
import iht.controllers.application.EstateController
import iht.metrics.Metrics
import iht.utils.StringHelper
import iht.views.html.application.exemption.charity.charity_delete_confirm
import javax.inject.Inject
import play.api.Logger
import play.api.Play.current
import play.api.i18n.Messages.Implicits._
import uk.gov.hmrc.auth.core.AuthConnector
import uk.gov.hmrc.auth.core.PlayAuthConnector

import scala.concurrent.Future
import uk.gov.hmrc.auth.core.retrieve.v2.Retrievals.{nino => ninoRetrieval}

class CharityDeleteConfirmControllerImpl @Inject()() extends CharityDeleteConfirmController with IhtConnectors {
  def metrics: Metrics = Metrics
}

trait CharityDeleteConfirmController extends EstateController {


  def onPageLoad(id: String) = authorisedForIhtWithRetrievals(ninoRetrieval) { userNino =>
    implicit request => {
      withApplicationDetails(userNino) {
        rd => ad => {
          Future.successful(ad.charities.find(_.id.contains(id)).fold {
            Logger.warn("Charity with id = " + id + " not found during onLoad of delete confirmation")
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
             Logger.warn("Charity with id = " + id + " not found during onSubmit of delete confirmation")
            Future.successful(InternalServerError("Charity with id = " + id
              + " not found during onSubmit of delete confirmation"))
          } else {
            val nino = StringHelper.getNino(userNino)
            val newCharities = ad.charities.patch(index, Nil, 1)
            val newAppDetails = ad copy (charities = newCharities)

            ihtConnector.saveApplication(nino, newAppDetails, rd.acknowledgmentReference).map {
              case Some(_) =>
                Redirect(iht.controllers.application.exemptions.charity.routes.CharitiesOverviewController.onPageLoad())
              case _ => {
                Logger.warn("Save of app details fails with id = " + id
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

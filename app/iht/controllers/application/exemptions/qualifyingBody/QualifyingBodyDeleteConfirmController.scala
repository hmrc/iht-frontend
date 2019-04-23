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

package iht.controllers.application.exemptions.qualifyingBody

import iht.config.AppConfig
import iht.connector.{CachingConnector, IhtConnector}
import iht.controllers.application.EstateController
import iht.utils.{CommonHelper, StringHelper}
import iht.views.html.application.exemption.qualifyingBody.qualifying_body_delete_confirm
import javax.inject.Inject
import play.api.Logger
import play.api.mvc.MessagesControllerComponents
import uk.gov.hmrc.auth.core.AuthConnector
import uk.gov.hmrc.auth.core.retrieve.v2.Retrievals.{nino => ninoRetrieval}
import uk.gov.hmrc.play.bootstrap.controller.FrontendController
import uk.gov.hmrc.play.partials.FormPartialRetriever

import scala.concurrent.Future

class QualifyingBodyDeleteConfirmControllerImpl @Inject()(val ihtConnector: IhtConnector,
                                                          val cachingConnector: CachingConnector,
                                                          val authConnector: AuthConnector,
                                                          val formPartialRetriever: FormPartialRetriever,
                                                          implicit val appConfig: AppConfig,
                                                          val cc: MessagesControllerComponents)
  extends FrontendController(cc) with QualifyingBodyDeleteConfirmController {

}

trait QualifyingBodyDeleteConfirmController extends EstateController with StringHelper {


  def onPageLoad(id: String) = authorisedForIhtWithRetrievals(ninoRetrieval) { userNino =>
    implicit request => {
      withApplicationDetails(userNino) {
        rd => ad => {
          Future.successful(ad.qualifyingBodies.find(_.id.contains(id)).fold {
            Logger.warn("QualifyingBody with id = " + id + " not found during onLoad of delete confirmation")
            InternalServerError("QualifyingBody with id = " + id + " not found during onLoad of delete confirmation")
          } { c =>
            Ok(qualifying_body_delete_confirm(c, routes.QualifyingBodyDeleteConfirmController.onSubmit(id)))
          })
        }
      }
    }
  }

  def onSubmit(id: String) = authorisedForIhtWithRetrievals(ninoRetrieval) { userNino =>
    implicit request => {
      withApplicationDetails(userNino) {
        rd => ad => {
          val index = ad.qualifyingBodies.indexWhere(_.id.contains(id))

          if (index == -1) {
             Logger.warn("QualifyingBody with id = " + id + " not found during onSubmit of delete confirmation")
            Future.successful(InternalServerError("QualifyingBody with id = " + id
              + " not found during onSubmit of delete confirmation"))
          } else {
            val nino = getNino(userNino)
            val newQualifyingBodies = ad.qualifyingBodies.patch(index, Nil, 1)
            val newAppDetails = ad copy (qualifyingBodies = newQualifyingBodies)

            ihtConnector.saveApplication(nino, newAppDetails, rd.acknowledgmentReference).map {
              case Some(_) =>
                Redirect(CommonHelper.addFragmentIdentifier(
                  iht.controllers.application.exemptions.qualifyingBody.routes.QualifyingBodiesOverviewController.onPageLoad(),
                  Some(appConfig.ExemptionsOtherAddID)))
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

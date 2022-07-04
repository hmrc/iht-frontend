/*
 * Copyright 2022 HM Revenue & Customs
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

package iht.controllers.application.debts

import iht.config.AppConfig
import iht.connector.{CachingConnector, IhtConnector}
import iht.constants.FieldMappings
import iht.controllers.application.ApplicationController
import iht.models.application.assets.Properties
import iht.utils.{CommonHelper, PropertyAndMortgageHelper}
import javax.inject.Inject
import play.api.mvc.{Call, MessagesControllerComponents, Request}
import uk.gov.hmrc.auth.core.AuthConnector
import uk.gov.hmrc.auth.core.retrieve.v2.Retrievals.{nino => ninoRetrieval}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController
import iht.views.html.application.debts.mortgages_overview

import scala.concurrent.Future

class MortgagesOverviewControllerImpl @Inject()(val cachingConnector: CachingConnector,
                                                val ihtConnector: IhtConnector,
                                                val authConnector: AuthConnector,
                                                val mortgagesOverviewView: mortgages_overview,
implicit val appConfig: AppConfig,
val cc: MessagesControllerComponents) extends FrontendController(cc) with MortgagesOverviewController

trait MortgagesOverviewController extends ApplicationController with PropertyAndMortgageHelper {


  private val MessageKeyReturnToDebts = "site.link.return.debts"

  def cachingConnector: CachingConnector

  def ihtConnector: IhtConnector
  val mortgagesOverviewView: mortgages_overview
  def onPageLoad = authorisedForIhtWithRetrievals(ninoRetrieval) { userNino =>
    implicit request => {
      doPageLoad(
        onCancel=iht.controllers.application.debts.routes.DebtsOverviewController.onPageLoad,
        onCancelMessageKey=MessageKeyReturnToDebts,
        isVisiblePropertyWarningAndLink=true,
        userNino)
    }
  }

  private def doPageLoad(onCancel: Call,
                         onCancelMessageKey: String,
                         isVisiblePropertyWarningAndLink: Boolean,
                         userNino: Option[String])(implicit request: Request[_]) = {
    withRegistrationDetails { regDetails =>
      ihtConnector.getApplication(getNino(userNino),
        CommonHelper.getOrExceptionNoIHTRef(regDetails.ihtReference),
        regDetails.acknowledgmentReference) flatMap {
        case Some(applicationDetails) => {
          val propertyList = applicationDetails.propertyList
          val properties = applicationDetails.allAssets.flatMap(_.properties).getOrElse(Properties(None))
          val updatedMortgages = updateMortgages(properties, applicationDetails)
          val mortgageList = if(updatedMortgages.getOrElse(None) == None) Nil else { updatedMortgages.get.mortgageList }

          Future.successful(Ok(mortgagesOverviewView(propertyList,
            mortgageList,
            FieldMappings.typesOfOwnership(regDetails.deceasedDetails.fold("")(_.name)),
            regDetails,
            onCancel,
            onCancelMessageKey)))

        }
        case _ => {
          Future.successful(Ok(mortgagesOverviewView(Nil,
            Nil,
            FieldMappings.typesOfOwnership(regDetails.deceasedDetails.fold("")(_.name)),
            regDetails,
            onCancel,
            onCancelMessageKey)))
        }
      }
    }
  }

}

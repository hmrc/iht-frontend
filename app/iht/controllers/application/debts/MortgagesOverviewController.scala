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

package iht.controllers.application.debts

import iht.connector.{CachingConnector, IhtConnector, IhtConnectors}
import iht.constants.FieldMappings
import iht.controllers.application.ApplicationController
import iht.models.application.assets.Properties
import iht.utils.{CommonHelper, PropertyAndMortgageHelper, StringHelper}
import play.api.Play.current
import play.api.i18n.Messages.Implicits._
import play.api.mvc.{Call, Request}
import uk.gov.hmrc.play.frontend.auth.AuthContext

import scala.concurrent.Future

object MortgagesOverviewController extends MortgagesOverviewController with IhtConnectors

trait MortgagesOverviewController extends ApplicationController {

  private val MessageKeyReturnToDebts = "site.link.return.debts"

  def cachingConnector: CachingConnector

  def ihtConnector: IhtConnector

  def onPageLoad = authorisedForIht {
    implicit user => implicit request => {
      doPageLoad(
        onCancel=iht.controllers.application.debts.routes.DebtsOverviewController.onPageLoad(),
        onCancelMessageKey=MessageKeyReturnToDebts,
        isVisiblePropertyWarningAndLink=true)
    }
  }

  private def doPageLoad(onCancel: Call,
                         onCancelMessageKey: String,
                         isVisiblePropertyWarningAndLink: Boolean)(implicit user: AuthContext, request: Request[_]) = {
    withRegistrationDetails { regDetails =>
      ihtConnector.getApplication(StringHelper.getNino(user),
        CommonHelper.getOrExceptionNoIHTRef(regDetails.ihtReference),
        regDetails.acknowledgmentReference) flatMap {
        case Some(applicationDetails) => {
          val propertyList = applicationDetails.propertyList
          val properties = applicationDetails.allAssets.flatMap(_.properties).getOrElse(Properties(None))
          val updatedMortgages = PropertyAndMortgageHelper.updateMortgages(properties, applicationDetails)
          val mortgageList = if(updatedMortgages.getOrElse(None) == None) Nil else { updatedMortgages.get.mortgageList }

          Future.successful(Ok(iht.views.html.application.debts.mortgages_overview(propertyList,
            mortgageList,
            FieldMappings.typesOfOwnership(regDetails.deceasedDetails.fold("")(_.name)),
            regDetails,
            onCancel,
            onCancelMessageKey)))

        }
        case _ => {
          Future.successful(Ok(iht.views.html.application.debts.mortgages_overview(Nil,
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

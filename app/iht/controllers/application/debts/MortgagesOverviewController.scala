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

import javax.inject.Inject

import iht.constants.FieldMappings
import iht.controllers.application.ApplicationController
import iht.models.application.assets.Property
import iht.models.application.debts.Mortgage
import iht.utils.CommonHelper
import play.api.i18n.MessagesApi
import play.api.mvc.{Call, Request}
import uk.gov.hmrc.play.frontend.auth.AuthContext

import scala.concurrent.Future

class MortgagesOverviewController @Inject()(val messagesApi: MessagesApi) extends ApplicationController {

  private val MessageKeyReturnToDebts = "site.link.return.debts"

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
      ihtConnector.getApplication(CommonHelper.getNino(user),
        CommonHelper.getOrExceptionNoIHTRef(regDetails.ihtReference),
        regDetails.acknowledgmentReference) flatMap {
        case Some(applicationDetails) => {
          val propertyList = applicationDetails.propertyList
          val mortgageList = applicationDetails.allLiabilities.flatMap(_.mortgages.map(_.mortgageList)).
            getOrElse(List.empty[Mortgage])

          val updatedMortgageList = updateMortgageListFromPropertyList(propertyList, mortgageList)

          Future.successful(Ok(iht.views.html.application.debts.mortgages_overview(propertyList,
            updatedMortgageList,
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

  /**
   *
   * @param propertyList
   * @param mortgageList
   * @return copy of the mortgage list updated from properties
   */
  def updateMortgageListFromPropertyList(propertyList: List[Property], mortgageList: List[Mortgage]): List[Mortgage] = {
    val propertyIdList: Set[String] = propertyList.map(p => p.id.getOrElse("")).toSet
    val mortgageIdList: Set[String] = mortgageList.map(m => m.id).toSet
    if (propertyIdList.union(mortgageIdList) != propertyIdList) throw new RuntimeException("MortgageList>PropertyList")
    val a: List[Mortgage] = propertyIdList.diff(mortgageIdList).map(id => Mortgage(id, None)).toList
    mortgageList ::: a
  }
}

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

package iht.controllers.application.exemptions.charity

import iht.config.AppConfig
import iht.connector.{CachingConnector, IhtConnector}
import iht.controllers.application.EstateController
import iht.forms.ApplicationForms._
import iht.models._
import iht.models.application.ApplicationDetails
import iht.models.application.exemptions._
import iht.utils.CommonHelper
import iht.views.html.application.exemption.charity.assets_left_to_charity_value
import javax.inject.Inject
import play.api.mvc.{Call, MessagesControllerComponents, Request}
import uk.gov.hmrc.auth.core.AuthConnector
import uk.gov.hmrc.auth.core.retrieve.v2.Retrievals.{nino => ninoRetrieval}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController

import scala.concurrent.Future

class CharityValueControllerImpl @Inject()(val ihtConnector: IhtConnector,
                                           val cachingConnector: CachingConnector,
                                           val authConnector: AuthConnector,
                                           val assetsLeftToCharityValueView: assets_left_to_charity_value,
                                           implicit val appConfig: AppConfig,
                                           val cc: MessagesControllerComponents) extends FrontendController(cc) with CharityValueController {

}

trait CharityValueController extends EstateController {


  lazy val submitUrl = CommonHelper.addFragmentIdentifier(routes.CharityValueController.onSubmit, Some(appConfig.ExemptionsCharitiesValueID))
  def cancelUrl: Call = routes.CharityDetailsOverviewController.onPageLoad

  private def editCancelUrl(id: String) = routes.CharityDetailsOverviewController.onEditPageLoad(id)

  private def editSubmitUrl(id: String) = CommonHelper.addFragmentIdentifier(routes.CharityValueController.onEditSubmit(id), Some(appConfig.ExemptionsCharitiesValueID))

  def locationAfterSuccessfulSave(optionID: Option[String]) = CommonHelper.getOrException(
    optionID.map(id => routes.CharityDetailsOverviewController.onEditPageLoad(id)))

  val updateApplicationDetails: (ApplicationDetails, Option[String], Charity) => (ApplicationDetails, Option[String]) =
    (appDetails, id, charity) => {
      val seekID = id.getOrElse("")
      val charityList = appDetails.charities

      val updatedCharitiesTuple: (Seq[Charity], String) = charityList.find(_.id.getOrElse("") equals seekID) match {
        case None =>
          id.fold {
            val nextID = nextId(charityList)
            (charityList :+ charity.copy(id = Some(nextID)), nextID)
          } { reqId => throw new RuntimeException("Id " + reqId + " can not be found") }
        case Some(matchedCharity) =>
          val updatedCharity: Charity = matchedCharity.copy(totalValue = charity.totalValue)
          (charityList.updated(charityList.indexOf(matchedCharity), updatedCharity), seekID)
      }
      (appDetails.copy(charities = updatedCharitiesTuple._1), Some(updatedCharitiesTuple._2))
    }

  val assetsLeftToCharityValueView: assets_left_to_charity_value
  def onPageLoad = authorisedForIht {
    implicit request => {
      withRegistrationDetails { regDetails =>
        Future.successful(Ok(assetsLeftToCharityValueView(assetsLeftToCharityValueForm,
          regDetails,
          submitUrl,
          cancelUrl)))
      }
    }
  }

  def onEditPageLoad(id: String) = authorisedForIhtWithRetrievals(ninoRetrieval) { userNino =>
    implicit request => {
      estateElementOnEditPageLoadWithNavigation[Charity](assetsLeftToCharityValueForm,
        assetsLeftToCharityValueView.apply,
        retrieveSectionDetailsOrExceptionIfInvalidID(id),
        editSubmitUrl(id),
        editCancelUrl(id),
        userNino)
    }
  }

  def onSubmit = authorisedForIhtWithRetrievals(ninoRetrieval) { userNino =>
    implicit request => {
      doSubmit(
        submitUrl = submitUrl,
        cancelUrl = cancelUrl,
        None,
        userNino)
    }
  }

  def onEditSubmit(id: String) = authorisedForIhtWithRetrievals(ninoRetrieval) { userNino =>
    implicit request => {
      doSubmit(
        submitUrl = editSubmitUrl(id),
        cancelUrl = editCancelUrl(id),
        Some(id),
        userNino)
    }
  }

  private def doSubmit(submitUrl: Call,
                       cancelUrl: Call,
                       charityId: Option[String],
                       userNino: Option[String])(
                        implicit request: Request[_]) = {
    estateElementOnSubmitWithIdAndNavigation[Charity](
      assetsLeftToCharityValueForm,
      assetsLeftToCharityValueView.apply,
      updateApplicationDetails,
      (_, updatedCharityID) => locationAfterSuccessfulSave(updatedCharityID),
      None,
      charityId,
      submitUrl,
      cancelUrl,
      userNino
    )
  }
}

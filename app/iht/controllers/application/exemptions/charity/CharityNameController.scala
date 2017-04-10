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

import iht.connector.IhtConnectors
import iht.controllers.application.EstateController
import iht.forms.ApplicationForms._
import iht.models.application.ApplicationDetails
import iht.models.application.exemptions._
import iht.models.{_}
import iht.utils.CommonHelper
import iht.views.html.application.exemption.charity.charity_name
import play.api.mvc.{Call, Request}
import uk.gov.hmrc.play.frontend.auth.AuthContext
import play.api.i18n.Messages.Implicits._
import play.api.Play.current
import scala.concurrent.Future
import iht.constants.IhtProperties._

object CharityNameController extends CharityNameController with IhtConnectors

trait CharityNameController extends EstateController {

  val submitUrl = CommonHelper.addFragmentIdentifier(routes.CharityNameController.onSubmit(), Some(ExemptionsCharitiesNameID))
  val cancelUrl = routes.CharityDetailsOverviewController.onPageLoad()

  def editCancelUrl(id: String) = routes.CharityDetailsOverviewController.onEditPageLoad(id)
  def editSubmitUrl(id: String) = CommonHelper.addFragmentIdentifier(routes.CharityNameController.onEditSubmit(id), Some(ExemptionsCharitiesNameID))

  def locationAfterSuccessfulSave(optionID: Option[String]) = CommonHelper.getOrException(
    optionID.map(id=>routes.CharityDetailsOverviewController.onEditPageLoad(id)))

  val updateApplicationDetails: (ApplicationDetails, Option[String], Charity) => (ApplicationDetails, Option[String]) =
    (appDetails, id, charity) => {
      val seekID = id.getOrElse("")
      val charityList = appDetails.charities
      val updatedCharitiesTuple: (Seq[Charity], String) = charityList.find(_.id.getOrElse("") equals seekID) match {
        case None =>
          id.fold {
            val nextID = nextId(charityList)
            (charityList :+ charity.copy(id = Some(nextID)), nextID)
          } {reqId => throw new RuntimeException("Id " + reqId + " can not be found")}
        case Some(matchedCharity) =>
          val updatedCharity: Charity = matchedCharity.copy(name = charity.name)
          (charityList.updated(charityList.indexOf(matchedCharity), updatedCharity), seekID)
      }
      (appDetails.copy(charities = updatedCharitiesTuple._1), Some(updatedCharitiesTuple._2))
    }


  def onPageLoad = authorisedForIht {
    implicit user => implicit request => {
      val regDetails = cachingConnector.getExistingRegistrationDetails
      Future.successful(Ok(iht.views.html.application.exemption.charity.charity_name(charityNameForm,
        regDetails,
        submitUrl,
        cancelUrl)))
    }
  }

  def onEditPageLoad(id: String) = authorisedForIht {
    implicit user => implicit request => {
      estateElementOnEditPageLoadWithNavigation[Charity](charityNameForm,
            charity_name.apply,
            retrieveSectionDetailsOrExceptionIfInvalidID(id),
            editSubmitUrl(id),
            editCancelUrl(id))
    }
  }

  def onSubmit = authorisedForIht {
    implicit user => implicit request => {
      doSubmit(
        submitUrl = submitUrl,
        cancelUrl = cancelUrl)
    }
  }

  def onEditSubmit(id: String) = authorisedForIht {
    implicit user => implicit request => {
      doSubmit(
        submitUrl = editSubmitUrl(id),
        cancelUrl = editCancelUrl(id),
        Some(id))
    }
  }

  private def doSubmit(submitUrl: Call,
                       cancelUrl: Call,
                       charityId: Option[String] = None)(
                        implicit user: AuthContext, request: Request[_]) = {
    estateElementOnSubmitWithIdAndNavigation[Charity](
      charityNameForm,
      charity_name.apply,
      updateApplicationDetails,
      (_, updatedCharityID) => locationAfterSuccessfulSave(updatedCharityID),
      None,
      charityId,
      submitUrl,
      cancelUrl
    )
  }

}

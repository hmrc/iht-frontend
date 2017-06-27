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

package iht.controllers.application.exemptions.qualifyingBody

import javax.inject.{Inject, Singleton}

import iht.connector.IhtConnectors
import iht.controllers.application.EstateController
import iht.forms.ApplicationForms.qualifyingBodyNameForm
import iht.metrics.Metrics
import iht.models.application.ApplicationDetails
import iht.models._
import iht.models.application.exemptions._
import iht.utils.CommonHelper
import iht.views.html.application.exemption.qualifyingBody.qualifying_body_name
import play.api.mvc.{Call, Request}
import uk.gov.hmrc.play.frontend.auth.AuthContext

import scala.concurrent.Future
import iht.constants.IhtProperties._
import play.api.i18n.MessagesApi

@Singleton
class QualifyingBodyNameController @Inject()(val messagesApi: MessagesApi) extends EstateController {

  val submitUrl = CommonHelper.addFragmentIdentifier(routes.QualifyingBodyNameController.onSubmit(), Some(ExemptionsOtherNameID))
  val cancelUrl = routes.QualifyingBodyDetailsOverviewController.onPageLoad()

  private def editCancelUrl(id: String) = routes.QualifyingBodyDetailsOverviewController.onEditPageLoad(id)
  private def editSubmitUrl(id: String) = CommonHelper.addFragmentIdentifier(routes.QualifyingBodyNameController.onEditSubmit(id), Some(ExemptionsOtherNameID))

  def locationAfterSuccessfulSave(optionID: Option[String]) = CommonHelper.getOrException(
    optionID.map(id => routes.QualifyingBodyDetailsOverviewController.onEditPageLoad(id)))

  val updateApplicationDetails: (ApplicationDetails, Option[String], QualifyingBody) => (ApplicationDetails, Option[String]) =
    (appDetails, id, qualifyingBody) => {
      val seekID = id.getOrElse("")
      val qbList = appDetails.qualifyingBodies

      val updatedQBTuple: (Seq[QualifyingBody], String) = qbList.find(_.id.getOrElse("") equals seekID) match {
        case None =>
          id.fold {
            val nextID = nextId(qbList)
            (qbList :+ qualifyingBody.copy(id = Some(nextID)), nextID)
          } { reqId => throw new RuntimeException("Id " + reqId + " can not be found") }
        case Some(matchedQualifyingBody) =>
          val updatedQualifyingBody: QualifyingBody = matchedQualifyingBody.copy(name = qualifyingBody.name)
          (qbList.updated(qbList.indexOf(matchedQualifyingBody), updatedQualifyingBody), seekID)
      }
      (appDetails.copy(qualifyingBodies = updatedQBTuple._1), Some(updatedQBTuple._2))
    }

  def onPageLoad = authorisedForIht {
    implicit user =>
      implicit request => {
        withRegistrationDetails { regDetails =>
          Future.successful(Ok(
            iht.views.html.application.exemption.qualifyingBody.qualifying_body_name(qualifyingBodyNameForm,
              regDetails,
              submitUrl,
              cancelUrl)))
        }
      }
  }

  def onEditPageLoad(id: String) = authorisedForIht {
    implicit user =>
      implicit request => {
        estateElementOnEditPageLoadWithNavigation[QualifyingBody](qualifyingBodyNameForm,
          qualifying_body_name.apply,
          retrieveQualifyingBodyDetailsOrExceptionIfInvalidID(id),
          editSubmitUrl(id),
          editCancelUrl(id))
      }
  }

  def onSubmit = authorisedForIht {
    implicit user =>
      implicit request => {
        doSubmit(
          submitUrl = submitUrl,
          cancelUrl = cancelUrl)
      }
  }

  def onEditSubmit(id: String) = authorisedForIht {
    implicit user =>
      implicit request => {
        doSubmit(
          submitUrl = editSubmitUrl(id),
          cancelUrl = editCancelUrl(id),
          charityId = Some(id))
      }
  }

  private def doSubmit(submitUrl: Call,
                       cancelUrl: Call,
                       charityId: Option[String] = None)(
                        implicit user: AuthContext, request: Request[_]) = {
    estateElementOnSubmitWithIdAndNavigation[QualifyingBody](
      qualifyingBodyNameForm,
      qualifying_body_name.apply,
      updateApplicationDetails,
      (_, updatedQualifyingBodyID) => locationAfterSuccessfulSave(updatedQualifyingBodyID),
      None,
      charityId,
      submitUrl,
      cancelUrl
    )
  }
}

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

package iht.controllers.application.exemptions.qualifyingBody

import iht.config.AppConfig
import iht.connector.{CachingConnector, IhtConnector}
import iht.controllers.application.EstateController
import iht.forms.ApplicationForms.qualifyingBodyNameForm
import iht.models._
import iht.models.application.ApplicationDetails
import iht.models.application.exemptions._
import iht.utils.CommonHelper
import iht.views.html.application.exemption.qualifyingBody.qualifying_body_name
import javax.inject.Inject
import play.api.mvc.{Call, MessagesControllerComponents, Request}
import uk.gov.hmrc.auth.core.AuthConnector
import uk.gov.hmrc.auth.core.retrieve.v2.Retrievals.{nino => ninoRetrieval}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController

import scala.concurrent.Future

class QualifyingBodyNameControllerImpl @Inject()(val ihtConnector: IhtConnector,
                                                 val cachingConnector: CachingConnector,
                                                 val authConnector: AuthConnector,
                                                 val qualifyingBodyNameView: qualifying_body_name,
                                                 implicit val appConfig: AppConfig,
                                                 val cc: MessagesControllerComponents) extends FrontendController(cc) with QualifyingBodyNameController

trait QualifyingBodyNameController extends EstateController {


  lazy val submitUrl = CommonHelper.addFragmentIdentifier(routes.QualifyingBodyNameController.onSubmit(), Some(appConfig.ExemptionsOtherNameID))

  def cancelUrl = routes.QualifyingBodyDetailsOverviewController.onPageLoad()

  private def editCancelUrl(id: String) = routes.QualifyingBodyDetailsOverviewController.onEditPageLoad(id)

  private def editSubmitUrl(id: String) = {
    CommonHelper.addFragmentIdentifier(routes.QualifyingBodyNameController.onEditSubmit(id),
      Some(appConfig.ExemptionsOtherNameID))
  }

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
  val qualifyingBodyNameView: qualifying_body_name
  def onPageLoad = authorisedForIht {
    implicit request => {
      withRegistrationDetails { regDetails =>
        Future.successful(Ok(
          qualifyingBodyNameView(qualifyingBodyNameForm,
            regDetails,
            submitUrl,
            cancelUrl)))
      }
    }
  }

  def onEditPageLoad(id: String) = authorisedForIhtWithRetrievals(ninoRetrieval) { userNino =>
    implicit request => {
      estateElementOnEditPageLoadWithNavigation[QualifyingBody](qualifyingBodyNameForm,
        qualifyingBodyNameView.apply,
        retrieveQualifyingBodyDetailsOrExceptionIfInvalidID(id),
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

  private def doSubmit(submitUrl: Call,
                       cancelUrl: Call,
                       charityId: Option[String],
                       userNino: Option[String])
                      (implicit request: Request[_]) = {
    estateElementOnSubmitWithIdAndNavigation[QualifyingBody](
      qualifyingBodyNameForm,
      qualifyingBodyNameView.apply,
      updateApplicationDetails,
      (_, updatedQualifyingBodyID) => locationAfterSuccessfulSave(updatedQualifyingBodyID),
      None,
      charityId,
      submitUrl,
      cancelUrl,
      userNino
    )
  }

  def onEditSubmit(id: String) = authorisedForIhtWithRetrievals(ninoRetrieval) { userNino =>
    implicit request => {
      doSubmit(
        submitUrl = editSubmitUrl(id),
        cancelUrl = editCancelUrl(id),
        charityId = Some(id),
        userNino)
    }
  }
}

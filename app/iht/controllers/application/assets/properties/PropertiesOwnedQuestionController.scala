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

package iht.controllers.application.assets.properties

import javax.inject.{Inject, Singleton}

import iht.constants.IhtProperties
import iht.controllers.application.EstateController
import iht.forms.ApplicationForms._
import iht.models._
import iht.models.application.ApplicationDetails
import iht.models.application.assets._
import iht.utils.CommonHelper
import iht.views.html.application.asset.properties.properties_owned_question
import play.api.Logger
import play.api.i18n.MessagesApi
import play.api.mvc.{Request, Result}
import uk.gov.hmrc.play.frontend.auth.AuthContext
import uk.gov.hmrc.play.http.HeaderCarrier

import scala.concurrent.Future

@Singleton
class PropertiesOwnedQuestionController @Inject()(val messagesApi: MessagesApi, val ihtProperties: IhtProperties) extends EstateController {

  def onPageLoad = authorisedForIht {
    implicit user =>
      implicit request =>
        estateElementOnPageLoad[Properties](propertiesForm, properties_owned_question.apply, _.allAssets.flatMap(_.properties))
  }

  def onSubmit = authorisedForIht {
    implicit user =>
      implicit request => {
        withRegistrationDetails { regDetails =>
          val deceasedName = CommonHelper.getOrException(regDetails.deceasedDetails).name

          val applicationDetailsFuture = ihtConnector.getApplication(CommonHelper.getNino(user),
            CommonHelper.getOrExceptionNoIHTRef(regDetails.ihtReference),
            regDetails.acknowledgmentReference)

          val boundForm = propertiesForm.bindFromRequest

          applicationDetailsFuture.flatMap {
            case Some(appDetails) => {
              boundForm.fold(
                formWithErrors => {
                  Future.successful(BadRequest(properties_owned_question(formWithErrors, regDetails)))
                },
                propertiesModel => {
                  saveApplication(CommonHelper.getNino(user), propertiesModel, appDetails, regDetails)
                }
              )
            }
            case _ => Future.successful(InternalServerError("Application details not found"))
          }
        }
      }
  }

  private def saveApplication(nino: String,
                              properties: Properties,
                              appDetails: ApplicationDetails,
                              regDetails: RegistrationDetails)(implicit request: Request[_],
                                                               hc: HeaderCarrier,
                                                               user: AuthContext): Future[Result] = {

    val updatedPropertyList = properties.isOwned match {
      case Some(false) => Nil
      case _ => appDetails.propertyList
    }

    val updatedAppDetails = appDetails.copy(allAssets = Some(appDetails.allAssets.fold
    (new AllAssets(properties = Some(properties)))(_.copy(
      properties = Some(properties)))), propertyList = updatedPropertyList)

    val previousIsOwnedValue: Option[Boolean] = appDetails.allAssets.flatMap(_.properties.flatMap(_.isOwned))
    val preexistingProperty: Boolean = appDetails.propertyList.nonEmpty

    val adAfterUpdatedForKickout = updateKickout(registrationDetails = regDetails, applicationDetails = updatedAppDetails)
    ihtConnector.saveApplication(nino, adAfterUpdatedForKickout, regDetails.acknowledgmentReference)
      .map { savedApplicationDetails =>
        savedApplicationDetails.fold[Result] {
          Logger.warn("Problem storing Application details. Redirecting to InternalServerError")
          InternalServerError
        } { _ =>
          adAfterUpdatedForKickout.kickoutReason match {
            case Some(reason) => Redirect(iht.controllers.application.routes.KickoutController.onPageLoad())
            case _ =>
            (properties.isOwned, previousIsOwnedValue, preexistingProperty) match {
              case (Some(false), _, _) => Redirect(CommonHelper.addFragmentIdentifier(assetsRedirectLocation, Some(ihtProperties.AppSectionPropertiesID)))
              case (Some(true), Some(true), _) =>
                Redirect(CommonHelper
                  .addFragmentIdentifier(iht.controllers.application.assets.properties.routes.PropertiesOverviewController.onPageLoad(),
                    Some(ihtProperties.AssetsPropertiesOwnedID)))
              case (Some(true), _, false) =>
                Redirect(iht.controllers.application.assets.properties.routes.PropertyDetailsOverviewController.onPageLoad())
              case (_, _, true) =>
                Redirect(CommonHelper
                  .addFragmentIdentifier(iht.controllers.application.assets.properties.routes.PropertiesOverviewController.onPageLoad(),
                    Some(ihtProperties.AssetsPropertiesOwnedID)))
              case _ =>
                Logger.warn("Problem storing Application details. Redirecting to InternalServerError")
                InternalServerError
            }
          }
        }
      }
  }
}

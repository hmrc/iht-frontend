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

package iht.controllers.application.assets.properties

import iht.config.AppConfig
import iht.connector.{CachingConnector, IhtConnector}
import iht.controllers.application.EstateController
import iht.forms.ApplicationForms._
import iht.metrics.IhtMetrics
import iht.models._
import iht.models.application.ApplicationDetails
import iht.models.application.assets._
import iht.models.application.debts.AllLiabilities
import iht.utils.{CommonHelper, PropertyAndMortgageHelper}
import iht.views.html.application.asset.properties.properties_owned_question
import javax.inject.Inject
import play.api.Logging
import play.api.mvc.{MessagesControllerComponents, Request, Result}
import uk.gov.hmrc.auth.core.AuthConnector
import uk.gov.hmrc.auth.core.retrieve.v2.Retrievals.{nino => ninoRetrieval}
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController

import scala.concurrent.Future


class PropertiesOwnedQuestionControllerImpl @Inject()(val metrics: IhtMetrics,
                                                      val ihtConnector: IhtConnector,
                                                      val cachingConnector: CachingConnector,
                                                      val authConnector: AuthConnector,
                                                      val propertiesOwnedQuestionView: properties_owned_question,
                                                      implicit val appConfig: AppConfig,
val cc: MessagesControllerComponents) extends FrontendController(cc) with PropertiesOwnedQuestionController

trait PropertiesOwnedQuestionController extends EstateController with PropertyAndMortgageHelper with Logging {

  val propertiesOwnedQuestionView: properties_owned_question
  def onPageLoad = authorisedForIhtWithRetrievals(ninoRetrieval) { userNino =>
    implicit request =>
      estateElementOnPageLoad[Properties](propertiesForm, propertiesOwnedQuestionView.apply, _.allAssets.flatMap(_.properties), userNino)
  }

  def onSubmit = authorisedForIhtWithRetrievals(ninoRetrieval) { userNino =>
    implicit request => {
      withRegistrationDetails { regDetails =>

        val applicationDetailsFuture = ihtConnector.getApplication(getNino(userNino),
          CommonHelper.getOrExceptionNoIHTRef(regDetails.ihtReference),
          regDetails.acknowledgmentReference)

        val boundForm = propertiesForm.bindFromRequest

        applicationDetailsFuture.flatMap {
          case Some(appDetails) =>
            boundForm.fold(
              formWithErrors => {
                Future.successful(BadRequest(propertiesOwnedQuestionView(formWithErrors, regDetails)))
              },
              propertiesModel => {
                saveApplication(getNino(userNino), propertiesModel, appDetails, regDetails)
              }
            )
          case _ => Future.successful(InternalServerError("Application details not found"))
        }
      }
    }
  }

  private def saveApplication(nino: String,
                              properties: Properties,
                              appDetails: ApplicationDetails,
                              regDetails: RegistrationDetails)(implicit request: Request[_],
                                                               hc: HeaderCarrier): Future[Result] = {


    val updatedAppDetails = appDetails.copy(
      allAssets = Some(appDetails.allAssets.fold(new AllAssets(properties = Some(properties)))(_.copy(
        properties = Some(properties)))),
      propertyList = updatePropertyList(properties, appDetails),
      allLiabilities = Some(appDetails.allLiabilities.fold(new AllLiabilities())(_.copy(
        mortgages = updateMortgages(properties, appDetails))))
    )

    val adAfterUpdatedForKickout = appKickoutUpdateKickout(registrationDetails = regDetails, applicationDetails = updatedAppDetails)
    ihtConnector.saveApplication(nino, adAfterUpdatedForKickout, regDetails.acknowledgmentReference)
      .map { savedApplicationDetails =>
        savedApplicationDetails.fold[Result] {
          logger.warn("Problem storing Application details. Redirecting to InternalServerError")
          InternalServerError
        } { _ =>
          adAfterUpdatedForKickout.kickoutReason match {
            case Some(_) => Redirect(iht.controllers.application.routes.KickoutAppController.onPageLoad())
            case _ => determineRedirectLocationForPropertiesOwnedQuestion(properties, appDetails)
          }
        }
      }
  }
}

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

package iht.controllers.application.gifts

import iht.config.AppConfig
import iht.connector.{CachingConnector, IhtConnector}
import iht.controllers.application.EstateController
import iht.forms.ApplicationForms._
import iht.models.application.ApplicationDetails
import iht.models.application.gifts.AllGifts
import iht.utils.GiftsHelper.createPreviousYearsGiftsLists
import iht.utils.{CommonHelper, ApplicationStatus => AppStatus}
import iht.views.html.application.gift.given_away
import javax.inject.Inject
import play.api.mvc.MessagesControllerComponents
import uk.gov.hmrc.auth.core.AuthConnector
import uk.gov.hmrc.auth.core.retrieve.v2.Retrievals.{nino => ninoRetrieval}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController

import scala.concurrent.Future

class GivenAwayControllerImpl @Inject()(val ihtConnector: IhtConnector,
                                        val cachingConnector: CachingConnector,
                                        val authConnector: AuthConnector,
                                        val givenAwayView: given_away,
                                        implicit val appConfig: AppConfig,
                                        val cc: MessagesControllerComponents) extends FrontendController(cc) with GivenAwayController

trait GivenAwayController extends EstateController {


  val givenAwayView: given_away
  def onPageLoad = authorisedForIhtWithRetrievals(ninoRetrieval) { userNino =>
    implicit request =>
      withApplicationDetails(userNino) { regDetails =>
        appDetails =>
          val fm = appDetails.allGifts.fold(giftsGivenAwayForm)(giftsGivenAwayForm.fill)

          CommonHelper.getOrException(regDetails.deceasedDateOfDeath.map { ddod =>
            val giftsList = appDetails.giftsList
              .fold(createPreviousYearsGiftsLists(ddod.dateOfDeath))(identity)

            Future.successful(Ok(givenAwayView(fm, regDetails, giftsList)))
          })
      }
  }


  def onSubmit = authorisedForIhtWithRetrievals(ninoRetrieval) { userNino =>
    implicit request => {
      withApplicationDetails(userNino) { regDetails =>
        appDetails =>
          val updateApplicationDetails: (ApplicationDetails, Option[String], AllGifts) =>
            (ApplicationDetails, Option[String]) =
            (appDetails, _, gifts) => {
              val updatedAllDetails = appDetails.copy(status = AppStatus.InProgress, allGifts = Some(appDetails.allGifts.fold
              (new AllGifts(isGivenAway = gifts.isGivenAway, None, None, None, None))
              (_.copy(isGivenAway = gifts.isGivenAway))))
              (updateApplicationDetailsWithUpdatedAllGifts(updatedAllDetails), None)
            }
          val boundForm = giftsGivenAwayForm.bindFromRequest
          boundForm.fold(
            formWithErrors => {
              CommonHelper.getOrException(regDetails.deceasedDateOfDeath.map { ddod =>
                val giftsList = appDetails.giftsList
                  .fold(createPreviousYearsGiftsLists(ddod.dateOfDeath))(identity)
                  .reverse
                Future.successful(BadRequest(givenAwayView(formWithErrors, regDetails, giftsList)))
              })
            },
            estateElementModel => {
              estatesSaveApplication(getNino(userNino),
                estateElementModel,
                regDetails,
                updateApplicationDetails,
                redirectLocation = (_, _) => CommonHelper.addFragmentIdentifier(giftsRedirectLocation, Some(appConfig.GiftsGivenAwayQuestionID)),
                None
              )
            }
          )
      }
    }
  }

  /**
    * Resets all the AllGifts values to None when GivenAway question's answer is no
    */
  private def updateApplicationDetailsWithUpdatedAllGifts(appDetails: ApplicationDetails) = {
    val isGivenAway = appDetails.allGifts.flatMap(_.isGivenAway)
    val isReservation = appDetails.allGifts.flatMap(_.isReservation)
    val isToTrust = appDetails.allGifts.flatMap(_.isToTrust)
    val isGivenInLast7Years = appDetails.allGifts.flatMap(_.isGivenInLast7Years)
    val action = appDetails.allGifts.flatMap(_.action)

    isGivenAway.fold(appDetails) {
      givenAwayValue =>
        if (!givenAwayValue) {
          appDetails copy(
            allGifts = Some(AllGifts(isGivenAway = Some(false), isReservation = isReservation, isToTrust = isToTrust,
              isGivenInLast7Years = isGivenInLast7Years, action = action)),
            giftsList = None)
        } else {
          appDetails
        }
    }
  }
}

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

package iht.controllers.application.gifts

/**
 * Created by xavierzanatta on 8/5/15.
 */

import iht.connector.{CachingConnector, IhtConnector}
import iht.controllers.IhtConnectors
import iht.controllers.application.EstateController
import iht.forms.ApplicationForms._
import iht.metrics.Metrics
import iht.models.application.ApplicationDetails
import iht.models.application.gifts.PreviousYearsGifts
import iht.models.RegistrationDetails
import iht.utils.{ApplicationKickOutHelper, CommonHelper, LogHelper}
import play.api.data.{Form, FormError}
import play.api.i18n.Messages
import play.api.mvc.{Call, Request, Result}
import uk.gov.hmrc.play.frontend.auth.AuthContext
import uk.gov.hmrc.play.http.HeaderCarrier

import scala.concurrent.Future

object GiftsDetailsController extends GiftsDetailsController with IhtConnectors {
  def metrics: Metrics = Metrics
}

trait GiftsDetailsController extends EstateController {
  override val applicationSection: Option[String] = Some(ApplicationKickOutHelper.ApplicationSectionGiftDetails)
  private lazy val cancelLabelKey = "GiftsDetailsCancelLabel"
  private lazy val cancelRedirectLocation = iht.controllers.application.gifts.routes.SevenYearsGiftsValuesController.onPageLoad()
  private lazy val cancelLabelKeyValueCancel = "iht.estateReport.gifts.returnToGiftsGivenAwayInThe7YearsBeforeDeath"
  private lazy val cancelLabelKeyValueReturnToGifts = "iht.estateReport.gifts.returnToGiftsGivenAway"

  def cachingConnector: CachingConnector

  def ihtConnector: IhtConnector

  def onPageLoad(id: String) = authorisedForIht {
    implicit user => implicit request => {
      cachingConnector.storeSingleValueSync(cancelLabelKey, cancelLabelKeyValueCancel)
      doPageLoad(id, Some(cancelRedirectLocation), Some(Messages(cancelLabelKeyValueCancel)))
    }
  }

  def onPageLoadForKickout(id: String) = authorisedForIht {
    implicit user => implicit request => {
      cachingConnector.storeSingleValueSync(cancelLabelKey, cancelLabelKeyValueReturnToGifts)
      doPageLoad(id, Some(cancelRedirectLocation), Some(Messages(cancelLabelKeyValueReturnToGifts)))
    }
  }

  private def doPageLoad(id: String, cancelUrl: Option[Call], cancelLabel: => Option[String])(implicit request:
  Request[_], user: AuthContext) = {
    val registrationDetails: RegistrationDetails = cachingConnector.getExistingRegistrationDetails

    val applicationDetails_future = ihtConnector.getApplication(CommonHelper.getNino(user),
      registrationDetails.ihtReference.getOrElse(throw new NoSuchElementException("No IHT Reference Present")),
      registrationDetails.acknowledgmentReference)

    applicationDetails_future flatMap {
      case Some(applicationDetails) => {
        CommonHelper.getOrException(applicationDetails.giftsList).find(pastGift => pastGift.yearId equals Some(id)) match {
          case Some(matchedGift) => {
            Future.successful(Ok(iht.views.html.application.gift.gifts_details(previousYearsGiftsForm.fill
              (matchedGift), registrationDetails, cancelUrl, cancelLabel)))
          }
          case _ => {
            Future.successful(Ok(iht.views.html.application.gift.gifts_details(previousYearsGiftsForm,
              registrationDetails, cancelUrl, cancelLabel)))
          }
        }
      }
      case _ => {
        Future.successful(Ok(iht.views.html.application.gift.gifts_details(previousYearsGiftsForm,
          registrationDetails, cancelUrl, cancelLabel)))
      }
    }
  }

  def onSubmit = authorisedForIht {
    implicit user => implicit request => {
      val regDetails: RegistrationDetails = cachingConnector.getExistingRegistrationDetails
      val boundForm = previousYearsGiftsForm.bindFromRequest
      implicit val applicationDetailsFuture = ihtConnector.getApplication(CommonHelper.getNino(user),
        regDetails.ihtReference.getOrElse(throw new NoSuchElementException("No IHT Reference Present")),
        regDetails.acknowledgmentReference)

      lazy val cancelLabelKeyValue = cachingConnector
        .getSingleValueSync(cancelLabelKey).fold(cancelLabelKeyValueReturnToGifts)(identity)

      boundForm.fold(
        formWithErrors => {
          LogHelper.logFormError(formWithErrors)
          Future.successful(BadRequest(iht.views.html.application.gift.gifts_details(formWithErrors, regDetails, Some
            (cancelRedirectLocation), Some(Messages(cancelLabelKeyValue)))))
        },
        previousYearsGifts => {

          processSubmit(CommonHelper.getNino(user), previousYearsGifts, regDetails)
        }
      )
    }
  }

  private def processSubmit(nino: String, previousYearsGifts: PreviousYearsGifts,
                            registrationDetails: RegistrationDetails)
                           (implicit request: Request[_],
                            hc: HeaderCarrier,
                            applicationDetailsFuture: Future[Option[ApplicationDetails]]): Future[Result] = {

    for {
      applicationDetails <- applicationDetailsFuture
      newApplicationDetails: ApplicationDetails = CommonHelper.getOrExceptionNoApplication(applicationDetails)
      giftsList = CommonHelper.getOrException(newApplicationDetails.giftsList, "No gifts list found")
    } yield {
      giftsList.find(pastGifts => pastGifts.yearId equals previousYearsGifts.yearId) match {
        case Some(matchedGift) => {
          val giftsList = newApplicationDetails.giftsList.get updated(matchedGift.yearId.get.toInt - 1, previousYearsGifts)
          val adtemp = newApplicationDetails.copy(giftsList = Some(giftsList))
          val ad = updateKickout(registrationDetails=registrationDetails, applicationDetails=adtemp,
            applicationID=previousYearsGifts.yearId)
          ihtConnector.saveApplication(nino, ad, registrationDetails.acknowledgmentReference)

          Redirect(ad.kickoutReason.fold(routes.SevenYearsGiftsValuesController.onPageLoad()){
            _=>{
              cachingConnector.storeSingleValueSync(ApplicationKickOutHelper.applicationLastSectionKey, applicationSection.fold("")(identity))
              cachingConnector.storeSingleValueSync(ApplicationKickOutHelper.applicationLastIDKey, previousYearsGifts.yearId.getOrElse(""))
              kickoutRedirectLocation
            }
          })
        }
        case _ => {
          Redirect(
            routes.SevenYearsGiftsValuesController.onPageLoad()
          )
        }
      }
    }
  }
}

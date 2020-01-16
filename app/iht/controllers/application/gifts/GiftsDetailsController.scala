/*
 * Copyright 2020 HM Revenue & Customs
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
import iht.models.RegistrationDetails
import iht.models.application.ApplicationDetails
import iht.models.application.gifts.PreviousYearsGifts
import iht.utils.CommonHelper._
import iht.utils.GiftsHelper._
import iht.utils.{ApplicationKickOutHelper, CommonHelper, LogHelper}
import javax.inject.Inject
import play.api.i18n.{Lang, Messages}
import play.api.mvc.{Call, MessagesControllerComponents, Request, Result}
import uk.gov.hmrc.auth.core.AuthConnector
import uk.gov.hmrc.auth.core.retrieve.v2.Retrievals.{nino => ninoRetrieval}
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.bootstrap.controller.FrontendController
import uk.gov.hmrc.play.partials.FormPartialRetriever

import scala.concurrent.Future

class GiftsDetailsControllerImpl @Inject()(val ihtConnector: IhtConnector,
                                           val cachingConnector: CachingConnector,
                                           val authConnector: AuthConnector,
                                           val formPartialRetriever: FormPartialRetriever,
                                           implicit val appConfig: AppConfig,
val cc: MessagesControllerComponents) extends FrontendController(cc) with GiftsDetailsController {

}

trait GiftsDetailsController extends EstateController {

  override val applicationSection: Option[String] = Some(ApplicationKickOutHelper.ApplicationSectionGiftDetails)
  private lazy val cancelLabelKey = "GiftsDetailsCancelLabel"
  private lazy val sevenYearsGiftsRedirectLocation = iht.controllers.application.gifts.routes.SevenYearsGiftsValuesController.onPageLoad()
  private lazy val cancelLabelKeyValueCancel = "iht.estateReport.gifts.returnToGiftsGivenAwayInThe7YearsBeforeDeath"
  private lazy val cancelLabelKeyValueReturnToGifts = "iht.estateReport.gifts.returnToGiftsGivenAway"

  def cachingConnector: CachingConnector

  def ihtConnector: IhtConnector

  def onPageLoad(id: String) = authorisedForIhtWithRetrievals(ninoRetrieval) { userNino =>
    implicit request => {
      cachingConnector.storeSingleValue(cancelLabelKey, cancelLabelKeyValueCancel)
      doPageLoad(id, Some(sevenYearsGiftsRedirectLocation), Some(Messages(cancelLabelKeyValueCancel)), userNino)
    }
  }

  def onPageLoadForKickout(id: String) = authorisedForIhtWithRetrievals(ninoRetrieval) { userNino =>
    implicit request => {
      cachingConnector.storeSingleValue(cancelLabelKey, cancelLabelKeyValueReturnToGifts)
      doPageLoad(id, Some(sevenYearsGiftsRedirectLocation), Some(Messages(cancelLabelKeyValueReturnToGifts)), userNino)
    }
  }

  private def doPageLoad(id: String, cancelUrl: Option[Call], cancelLabel: => Option[String], userNino: Option[String])(implicit request: Request[_]) = {
    withApplicationDetails(userNino) { rd => ad =>
      implicit val lang: Lang = messagesApi.preferred(request).lang
      val result = getOrException(rd.deceasedDateOfDeath.map { ddod =>
        withValue {
          val prevYearsGifts = ad.giftsList.fold(createPreviousYearsGiftsLists(ddod.dateOfDeath))(identity)

          prevYearsGifts.find(_.yearId.contains(id))
            .fold(previousYearsGiftsForm)(matchedGift => {
              previousYearsGiftsForm.fill(matchedGift)
            })
        }(form =>
          Ok(
            iht.views.html.application.gift.gifts_details(
              form,
              rd,
              Some(CommonHelper.addFragmentIdentifier(cancelUrl.get, Some(appConfig.GiftsValueDetailID + id.toString))),
              cancelLabel
            )
          )
        )
      })
      Future.successful(result)
    }
  }

  def onSubmit = authorisedForIhtWithRetrievals(ninoRetrieval) { userNino =>
    implicit request => {
      implicit val lang: Lang = messagesApi.preferred(request).lang
      withApplicationDetails(userNino) { rd => ad =>
        val boundForm = previousYearsGiftsForm.bindFromRequest
        boundForm.fold(
          formWithErrors => {
            for {
              cancelLabelKeyValue <- cachingConnector.getSingleValue(cancelLabelKey).map(_.fold(cancelLabelKeyValueReturnToGifts)(identity))
            } yield {
              LogHelper.logFormError(formWithErrors)
                BadRequest(
                  iht.views.html.application.gift.gifts_details(
                    formWithErrors,
                    rd,
                    Some(sevenYearsGiftsRedirectLocation),
                    Some(Messages(cancelLabelKeyValue))
                  )
                )
            }
          },
          previousYearsGifts => {
            processSubmit(getNino(userNino), previousYearsGifts, rd, ad)
          }
        )
      }
    }
  }

  private def processSubmit(nino: String, previousYearsGifts: PreviousYearsGifts,
                            rd: RegistrationDetails,
                            ad: ApplicationDetails)
                           (implicit request: Request[_],
                            hc: HeaderCarrier): Future[Result] = {
    CommonHelper.getOrException(rd.deceasedDateOfDeath.map { ddod =>
      val existingSeqPrevYearsGifts = ad.giftsList.fold(createPreviousYearsGiftsLists(ddod.dateOfDeath))(identity)
      val idToUpdate = existingSeqPrevYearsGifts.indexWhere(_.yearId == previousYearsGifts.yearId)
      if (idToUpdate < 0) {
        Future.successful(
          Redirect(
            sevenYearsGiftsRedirectLocation
          )
        )
      } else {
        withValue {
          val updatedSeqPrevYearsGifts = existingSeqPrevYearsGifts.updated(idToUpdate, previousYearsGifts)
          updateKickout(registrationDetails = rd,
            applicationDetails = ad.copy(giftsList = Some(updatedSeqPrevYearsGifts)),
            applicationID = previousYearsGifts.yearId)
        }{newAD =>
          ihtConnector.saveApplication(nino, newAD, rd.acknowledgmentReference).map(_ =>
            Redirect(newAD.kickoutReason.fold(CommonHelper.addFragmentIdentifier(
              sevenYearsGiftsRedirectLocation, Some(appConfig.GiftsValueDetailID + (idToUpdate + 1).toString))) {
              _ => {
                cachingConnector.storeSingleValue(
                  ApplicationKickOutHelper.applicationLastSectionKey, applicationSection.fold("")(identity))
                cachingConnector.storeSingleValue(
                  ApplicationKickOutHelper.applicationLastIDKey, previousYearsGifts.yearId.getOrElse(""))
                kickoutRedirectLocation
              }
            })
          )
        }
      }
    })
  }
}

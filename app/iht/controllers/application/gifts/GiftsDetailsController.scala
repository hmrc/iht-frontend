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

import javax.inject.{Inject, Singleton}

import iht.constants.IhtProperties
import iht.controllers.application.EstateController
import iht.forms.ApplicationForms._
import iht.models.RegistrationDetails
import iht.models.application.ApplicationDetails
import iht.models.application.gifts.PreviousYearsGifts
import iht.utils.CommonHelper._
import iht.utils.GiftsHelper._
import iht.utils.{ApplicationKickOutHelper, CommonHelper, LogHelper}
import play.api.i18n.{Messages, MessagesApi}
import play.api.mvc.{Call, Request, Result}
import uk.gov.hmrc.play.frontend.auth.AuthContext
import uk.gov.hmrc.play.http.HeaderCarrier

import scala.concurrent.Future

@Singleton
class GiftsDetailsController @Inject()(val messagesApi: MessagesApi, val ihtProperties: IhtProperties) extends EstateController {
  override val applicationSection: Option[String] = Some(ApplicationKickOutHelper.ApplicationSectionGiftDetails)
  private lazy val cancelLabelKey = "GiftsDetailsCancelLabel"
  private lazy val sevenYearsGiftsRedirectLocation = iht.controllers.application.gifts.routes.SevenYearsGiftsValuesController.onPageLoad()
  private lazy val cancelLabelKeyValueCancel = "iht.estateReport.gifts.returnToGiftsGivenAwayInThe7YearsBeforeDeath"
  private lazy val cancelLabelKeyValueReturnToGifts = "iht.estateReport.gifts.returnToGiftsGivenAway"

  def onPageLoad(id: String) = authorisedForIht {
    implicit user =>
      implicit request => {
        cachingConnector.storeSingleValueSync(cancelLabelKey, cancelLabelKeyValueCancel)
        doPageLoad(id, Some(sevenYearsGiftsRedirectLocation), Some(Messages(cancelLabelKeyValueCancel)))
      }
  }

  def onPageLoadForKickout(id: String) = authorisedForIht {
    implicit user =>
      implicit request => {
        cachingConnector.storeSingleValueSync(cancelLabelKey, cancelLabelKeyValueReturnToGifts)
        doPageLoad(id, Some(sevenYearsGiftsRedirectLocation), Some(Messages(cancelLabelKeyValueReturnToGifts)))
      }
  }

  private def doPageLoad(id: String, cancelUrl: Option[Call], cancelLabel: => Option[String])(implicit request:
  Request[_], user: AuthContext) = {
    withApplicationDetails { rd => ad =>
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
              Some(CommonHelper.addFragmentIdentifier(cancelUrl.get, Some(ihtProperties.GiftsValueDetailID + id.toString))),
              cancelLabel
            )
          )
        )
      })
      Future.successful(result)
    }
  }

  def onSubmit = authorisedForIht {
    implicit user =>
      implicit request => {
        withApplicationDetails { rd => ad =>
          val boundForm = previousYearsGiftsForm.bindFromRequest
          lazy val cancelLabelKeyValue = cachingConnector
            .getSingleValueSync(cancelLabelKey).fold(cancelLabelKeyValueReturnToGifts)(identity)
          boundForm.fold(
            formWithErrors => {
              LogHelper.logFormError(formWithErrors)
              Future.successful(
                BadRequest(
                  iht.views.html.application.gift.gifts_details(
                    formWithErrors,
                    rd,
                    Some(sevenYearsGiftsRedirectLocation),
                    Some(Messages(cancelLabelKeyValue))
                  )
                )
              )
            },
            previousYearsGifts => {
              processSubmit(CommonHelper.getNino(user), previousYearsGifts, rd, ad)
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
            Redirect(newAD.kickoutReason.fold(CommonHelper.addFragmentIdentifier(sevenYearsGiftsRedirectLocation,
              Some(ihtProperties.GiftsValueDetailID + (idToUpdate + 1).toString))) {
              _ => {
                cachingConnector.storeSingleValueSync(ApplicationKickOutHelper.applicationLastSectionKey, applicationSection.fold("")(identity))
                cachingConnector.storeSingleValueSync(ApplicationKickOutHelper.applicationLastIDKey, previousYearsGifts.yearId.getOrElse(""))
                kickoutRedirectLocation
              }
            })
          )
        }
      }
    })
  }
}

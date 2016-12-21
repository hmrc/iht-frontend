/*
 * Copyright 2016 HM Revenue & Customs
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

package iht.controllers.application

import iht.connector.{CachingConnector, IhtConnector}
import iht.constants.IhtProperties
import iht.controllers.IhtConnectors
import iht.metrics.Metrics
import iht.models.application.ApplicationDetails
import iht.models.enums.KickOutSource
import iht.models.RegistrationDetails
import iht.utils.tnrb._
import iht.utils.{ApplicationKickOutHelper, CommonHelper, ApplicationStatus => AppStatus}
import play.api.Logger
import play.api.i18n.Messages
import play.api.mvc.Request
import uk.gov.hmrc.play.frontend.auth.AuthContext

import scala.concurrent.Future

object KickoutController extends KickoutController with IhtConnectors {
  def metrics: Metrics = Metrics
}

trait KickoutController extends ApplicationController{
  val storageFailureMessage = "Failed to successfully store kickout flag"
  def cachingConnector: CachingConnector
  def ihtConnector: IhtConnector
  def metrics: Metrics

  def onPageLoad = authorisedForIht {
    implicit user => implicit request => {
      val regDetails:RegistrationDetails = cachingConnector.getExistingRegistrationDetails
      Logger.info("Retrieving kickout reason")

      for {
        applicationDetailsOpt: Option[ApplicationDetails] <- ihtConnector.getApplication(CommonHelper.getNino(user),
          CommonHelper.getOrExceptionNoIHTRef(regDetails.ihtReference),
          regDetails.acknowledgmentReference)
      } yield {
        val applicationDetails = CommonHelper.getOrExceptionNoApplication(applicationDetailsOpt)
        (applicationDetails.status, applicationDetails.kickoutReason) match {
          case (AppStatus.KickOut, Some(kickoutReason)) =>
            Logger.info(s"Kickout reason: $kickoutReason")
            val applicationLastSection = cachingConnector.getSingleValueSync(ApplicationKickOutHelper.applicationLastSectionKey)
            val applicationLastID = cachingConnector.getSingleValueSync(ApplicationKickOutHelper.applicationLastIDKey)

            cachingConnector.deleteSingleValueSync(ApplicationKickOutHelper.SeenFirstKickoutPageCacheKey)

            lazy val summaryParameter1 = ApplicationKickOutHelper.sources
              .find( source => source._1 == kickoutReason && source._2 == KickOutSource.TNRB).map( _ =>
                TnrbHelper.spouseOrCivilPartnerLabelWithOptions(
                  applicationDetails.increaseIhtThreshold,
                  applicationDetails.widowCheck,
                  Some(Messages("page.iht.application.tnrbEligibilty.partner.additional.label.the.deceased.previous")))
            )
            Ok(iht.views.html.application.iht_kickout_application(kickoutReason, applicationDetails,
              applicationLastSection, applicationLastID, summaryParameter1))
          case _ =>
            val ihtRef = CommonHelper.getOrExceptionNoIHTRef(regDetails.ihtReference)
            Redirect(iht.controllers.application.routes.EstateOverviewController.onPageLoadWithIhtRef(ihtRef))
        }
      }
    }
  }

  def onSubmit = authorisedForIht {
    implicit user => implicit request =>
      val futureOptionSeen = cachingConnector.getSingleValue(ApplicationKickOutHelper.SeenFirstKickoutPageCacheKey)
      futureOptionSeen.flatMap{ optionSeen =>
        optionSeen.fold{
          val storedOptionSeen = cachingConnector.storeSingleValue(ApplicationKickOutHelper.SeenFirstKickoutPageCacheKey, "true")
          storedOptionSeen.map{
            case Some(_) =>
              Redirect(iht.controllers.application.routes.KickoutController.onPageLoadDeleting())
            case _ =>
              Logger.warn(storageFailureMessage)
              InternalServerError(storageFailureMessage)
          }
        }{_=>
            emptyCache()
            updateMetrics().map( isUpdated => {
              cachingConnector.deleteSingleValueSync(ApplicationKickOutHelper.SeenFirstKickoutPageCacheKey)
              val regDetails:RegistrationDetails = cachingConnector.getExistingRegistrationDetails
              ihtConnector.deleteApplication(CommonHelper.getNino(user),CommonHelper.getOrExceptionNoIHTRef(regDetails.ihtReference))
              if(!isUpdated) {
                Logger.info("Application deleted after a kickout but unable to update metrics")
              }
              Redirect(IhtProperties.linkEstateReportKickOut)
            })
        }
      }
  }

  private def updateMetrics()(implicit user:AuthContext, request: Request[_]) = {
    val regDetails:RegistrationDetails = cachingConnector.getExistingRegistrationDetails
    val futureOptionAD = ihtConnector.getApplication(CommonHelper.getNino(user),
        CommonHelper.getOrExceptionNoIHTRef(regDetails.ihtReference),
        regDetails.acknowledgmentReference)
    futureOptionAD map { optionAD =>
      val optionIsUpdated = optionAD flatMap { ad =>
        ad.kickoutReason flatMap { kickoutReason =>
          ApplicationKickOutHelper.sources.find( sourceMapping => sourceMapping._1 == kickoutReason ) map { sourceMapping =>
            metrics.kickOutCounter(sourceMapping._2)
            true
          }
        }
      }
      optionIsUpdated.fold(false)(identity)
    }
  }

  private def emptyCache()(implicit user:AuthContext, request: Request[_]) = {
    cachingConnector.getSingleValueSync(ApplicationKickOutHelper.applicationLastSectionKey)
      .foreach(_=> cachingConnector.deleteSingleValueSync(ApplicationKickOutHelper.applicationLastSectionKey))
    cachingConnector.getSingleValueSync(ApplicationKickOutHelper.applicationLastIDKey)
      .foreach(_=>cachingConnector.deleteSingleValueSync(ApplicationKickOutHelper.applicationLastIDKey))
  }

  def onPageLoadDeleting = authorisedForIht {
    implicit user => implicit request =>
      Future.successful(Ok(iht.views.html.application.iht_kickout_final_application(request)))
  }
}

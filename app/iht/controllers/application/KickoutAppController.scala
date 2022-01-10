/*
 * Copyright 2022 HM Revenue & Customs
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

import iht.config.AppConfig
import iht.connector.{CachingConnector, IhtConnector}
import iht.metrics.IhtMetrics
import iht.models.RegistrationDetails
import iht.models.application.ApplicationDetails
import iht.models.enums.KickOutSource
import iht.utils.tnrb._
import iht.utils.{ApplicationKickOutHelper, CommonHelper, DeceasedInfoHelper, StringHelper, ApplicationStatus => AppStatus}
import javax.inject.Inject
import play.api.Logging
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents, Request}
import uk.gov.hmrc.auth.core.AuthConnector
import uk.gov.hmrc.auth.core.retrieve.v2.Retrievals.{nino => ninoRetrieval}
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController
import iht.views.html.application.iht_kickout_application
import iht.views.html.application.iht_kickout_final_application

import scala.concurrent.Future

class KickoutAppControllerImpl @Inject()(val metrics: IhtMetrics,
                                         val ihtConnector: IhtConnector,
                                         val cachingConnector: CachingConnector,
                                         val authConnector: AuthConnector,
                                         val ihtKickoutApplicationView: iht_kickout_application,
                                         val ihtKickoutFinalApplicationView: iht_kickout_final_application,
                                         implicit val appConfig: AppConfig,
                                         val cc: MessagesControllerComponents) extends FrontendController(cc) with KickoutAppController with Logging
trait KickoutAppController extends ApplicationController with StringHelper with TnrbHelper {
  val storageFailureMessage = "Failed to successfully store kick out flag"

  def cachingConnector: CachingConnector

  def ihtConnector: IhtConnector

  def metrics: IhtMetrics
  val ihtKickoutApplicationView: iht_kickout_application
  val ihtKickoutFinalApplicationView: iht_kickout_final_application
  def onPageLoad: Action[AnyContent] = authorisedForIhtWithRetrievals(ninoRetrieval) { userNino =>
    implicit request => {
      withRegistrationDetails { regDetails =>
        logger.info("Retrieving kickout reason")
        fetchAppDetailsFromIHT(userNino,regDetails).flatMap { applicationDetailsOpt =>
          val applicationDetails = CommonHelper.getOrExceptionNoApplication(applicationDetailsOpt)

          (applicationDetails.status, applicationDetails.kickoutReason) match {
            case (AppStatus.KickOut, Some(kickoutReason)) =>
              logger.info(s"Kickout reason: $kickoutReason")
              cachingConnector.deleteSingleValue(ApplicationKickOutHelper.SeenFirstKickoutPageCacheKey)
              val deceasedName = DeceasedInfoHelper.getDeceasedNameOrDefaultString(regDetails)
              lazy val summaryParameter1 = getKickoutDetails(kickoutReason, deceasedName, applicationDetails)

              for {
                applicationLastSection <- cachingConnector.getSingleValue(ApplicationKickOutHelper.applicationLastSectionKey)
                applicationLastID <- cachingConnector.getSingleValue(ApplicationKickOutHelper.applicationLastIDKey)
              } yield {
                Ok(ihtKickoutApplicationView(kickoutReason, applicationDetails,
                  applicationLastSection, applicationLastID, summaryParameter1, deceasedName))
              }

            case _ =>
              val ihtRef = CommonHelper.getOrExceptionNoIHTRef(regDetails.ihtReference)
              Future.successful(Redirect(iht.controllers.application.routes.EstateOverviewController.onPageLoadWithIhtRef(ihtRef)))
          }
        }
      }
    }
  }

  def fetchAppDetailsFromIHT(userNino: Option[String], regDetails: RegistrationDetails)
                            (implicit headerCarrier: HeaderCarrier): Future[Option[ApplicationDetails]] = {
    ihtConnector.getApplication(getNino(userNino),
      CommonHelper.getOrExceptionNoIHTRef(regDetails.ihtReference),
      regDetails.acknowledgmentReference)
  }

  def getKickoutDetails(kickoutReason: String, deceasedName: String, applicationDetails: ApplicationDetails)(implicit request: Request[_]): String ={
    ApplicationKickOutHelper.sources
      .find(source => source._1 == kickoutReason && source._2 == KickOutSource.TNRB).map(_ =>
      previousSpouseOrCivilPartner(
        applicationDetails.increaseIhtThreshold,
        applicationDetails.widowCheck,
        deceasedName
      )
    ).fold(deceasedName)(identity)
  }

  def onSubmit = authorisedForIhtWithRetrievals(ninoRetrieval) { userNino =>
    implicit request =>
      val futureOptionSeen = cachingConnector.getSingleValue(ApplicationKickOutHelper.SeenFirstKickoutPageCacheKey)
      futureOptionSeen.flatMap { optionSeen =>
        optionSeen.fold {
          val storedOptionSeen = cachingConnector.storeSingleValue(ApplicationKickOutHelper.SeenFirstKickoutPageCacheKey, "true")
          storedOptionSeen.map {
            case Some(_) =>
              Redirect(iht.controllers.application.routes.KickoutAppController.onPageLoadDeleting())
            case _ =>
              logger.warn(storageFailureMessage)
              InternalServerError(storageFailureMessage)
          }
        } { _ =>
          emptyCache()
          withRegistrationDetails { regDetails =>
            updateMetrics(regDetails, userNino).flatMap(isUpdated => {
              cachingConnector.deleteSingleValue(ApplicationKickOutHelper.SeenFirstKickoutPageCacheKey)
              withRegistrationDetails { regDetails =>
                ihtConnector.deleteApplication(getNino(userNino), CommonHelper.getOrExceptionNoIHTRef(regDetails.ihtReference))
                if (!isUpdated) {
                  logger.info("Application deleted after a kickout but unable to update metrics")
                }
                Future.successful(Redirect(iht.controllers.routes.DeadlinesController.onPageLoadApplication))
              }
            })
          }
        }
      }
  }

  private def updateMetrics(regDetails:RegistrationDetails, userNino: Option[String])(implicit request: Request[_]): Future[Boolean] = {
    val futureOptionAD = ihtConnector.getApplication(getNino(userNino),
      CommonHelper.getOrExceptionNoIHTRef(regDetails.ihtReference),
      regDetails.acknowledgmentReference)
    futureOptionAD map { optionAD =>
      val optionIsUpdated = optionAD flatMap { ad =>
        ad.kickoutReason flatMap { kickoutReason =>
          ApplicationKickOutHelper.sources.find(sourceMapping => sourceMapping._1 == kickoutReason) map { sourceMapping =>
            metrics.kickOutCounter(sourceMapping._2)
            true
          }
        }
      }
      optionIsUpdated.fold(false)(identity)
    }
  }

  private def emptyCache()(implicit request: Request[_]) = {
    cachingConnector.getSingleValue(ApplicationKickOutHelper.applicationLastSectionKey)
      .map(_ => cachingConnector.deleteSingleValue(ApplicationKickOutHelper.applicationLastSectionKey))
    cachingConnector.getSingleValue(ApplicationKickOutHelper.applicationLastIDKey)
      .map(_ => cachingConnector.deleteSingleValue(ApplicationKickOutHelper.applicationLastIDKey))
  }

  def onPageLoadDeleting = authorisedForIhtWithRetrievals(ninoRetrieval) { userNino =>
    implicit request => {
      withApplicationDetails(userNino) { rd =>
        ad =>
          lazy val ihtReference = CommonHelper.getOrExceptionNoIHTRef(rd.ihtReference)
          Future.successful(Ok(ihtKickoutFinalApplicationView(ihtReference)))
      }
    }
  }
}

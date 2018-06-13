/*
 * Copyright 2018 HM Revenue & Customs
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

import iht.connector.{CachingConnector, IhtConnector, IhtConnectors}
import iht.metrics.Metrics
import iht.models.RegistrationDetails
import iht.models.application.ApplicationDetails
import iht.models.enums.KickOutSource
import iht.utils.tnrb._
import iht.utils.{ApplicationKickOutHelper, CommonHelper, DeceasedInfoHelper, StringHelper, ApplicationStatus => AppStatus}
import play.api.Logger
import play.api.Play.current
import play.api.i18n.Messages.Implicits._
import play.api.mvc.{Action, AnyContent, Request}
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.frontend.auth.AuthContext

import scala.concurrent.Future

object KickoutController extends KickoutController with IhtConnectors {
  def metrics: Metrics = Metrics
}

trait KickoutController extends ApplicationController {
  val storageFailureMessage = "Failed to successfully store kickout flag"

  def cachingConnector: CachingConnector

  def ihtConnector: IhtConnector

  def metrics: Metrics

  def onPageLoad: Action[AnyContent] = authorisedForIht {
    implicit user =>
      implicit request => {
        withRegistrationDetails { regDetails =>
          Logger.info("Retrieving kickout reason")
            fetchAppDetailsFromIHT(user,regDetails)(hc).flatMap { applicationDetailsOpt =>
              val applicationDetails = CommonHelper.getOrExceptionNoApplication(applicationDetailsOpt)

              (applicationDetails.status, applicationDetails.kickoutReason) match {
                case (AppStatus.KickOut, Some(kickoutReason)) =>
                  Logger.info(s"Kickout reason: $kickoutReason")
                  cachingConnector.deleteSingleValue(ApplicationKickOutHelper.SeenFirstKickoutPageCacheKey)
                  val deceasedName = DeceasedInfoHelper.getDeceasedNameOrDefaultString(regDetails)
                  lazy val summaryParameter1 = getKickoutDetails(kickoutReason, deceasedName, applicationDetails)

                  for {
                    applicationLastSection <- cachingConnector.getSingleValue(ApplicationKickOutHelper.applicationLastSectionKey)
                    applicationLastID <- cachingConnector.getSingleValue(ApplicationKickOutHelper.applicationLastIDKey)
                  } yield {
                    Ok(iht.views.html.application.iht_kickout_application(kickoutReason, applicationDetails,
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

  def fetchAppDetailsFromIHT(user: AuthContext, regDetails: RegistrationDetails)(implicit headerCarrier: HeaderCarrier): Future[Option[ApplicationDetails]] = {
    ihtConnector.getApplication(StringHelper.getNino(user),
      CommonHelper.getOrExceptionNoIHTRef(regDetails.ihtReference),
      regDetails.acknowledgmentReference)
  }

  def getKickoutDetails(kickoutReason: String, deceasedName: String, applicationDetails: ApplicationDetails): String ={
    ApplicationKickOutHelper.sources
      .find(source => source._1 == kickoutReason && source._2 == KickOutSource.TNRB).map(_ =>
      TnrbHelper.previousSpouseOrCivilPartner(
        applicationDetails.increaseIhtThreshold,
        applicationDetails.widowCheck,
        deceasedName
      )
    ).fold(deceasedName)(identity)
  }

  def onSubmit = authorisedForIht {
    implicit user =>
      implicit request =>
        val futureOptionSeen = cachingConnector.getSingleValue(ApplicationKickOutHelper.SeenFirstKickoutPageCacheKey)
        futureOptionSeen.flatMap { optionSeen =>
          optionSeen.fold {
            val storedOptionSeen = cachingConnector.storeSingleValue(ApplicationKickOutHelper.SeenFirstKickoutPageCacheKey, "true")
            storedOptionSeen.map {
              case Some(_) =>
                Redirect(iht.controllers.application.routes.KickoutController.onPageLoadDeleting())
              case _ =>
                Logger.warn(storageFailureMessage)
                InternalServerError(storageFailureMessage)
            }
          } { _ =>
            emptyCache()
            withRegistrationDetails { regDetails =>
              updateMetrics(regDetails).flatMap(isUpdated => {
                cachingConnector.deleteSingleValue(ApplicationKickOutHelper.SeenFirstKickoutPageCacheKey)
                withRegistrationDetails { regDetails =>
                  ihtConnector.deleteApplication(StringHelper.getNino(user), CommonHelper.getOrExceptionNoIHTRef(regDetails.ihtReference))
                  if (!isUpdated) {
                    Logger.info("Application deleted after a kickout but unable to update metrics")
                  }
                  Future.successful(Redirect(iht.controllers.routes.DeadlinesController.onPageLoadApplication))
                }
              })
            }
          }
        }
  }

  private def updateMetrics(regDetails:RegistrationDetails)(implicit user: AuthContext, request: Request[_]): Future[Boolean] = {
    val futureOptionAD = ihtConnector.getApplication(StringHelper.getNino(user),
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

  private def emptyCache()(implicit user: AuthContext, request: Request[_]) = {
    cachingConnector.getSingleValue(ApplicationKickOutHelper.applicationLastSectionKey)
      .map(_ => cachingConnector.deleteSingleValue(ApplicationKickOutHelper.applicationLastSectionKey))
    cachingConnector.getSingleValue(ApplicationKickOutHelper.applicationLastIDKey)
      .map(_ => cachingConnector.deleteSingleValue(ApplicationKickOutHelper.applicationLastIDKey))
  }

  def onPageLoadDeleting = authorisedForIht {
    implicit user =>
      implicit request => {
        withApplicationDetails { rd =>
          ad =>
            lazy val ihtReference = CommonHelper.getOrExceptionNoIHTRef(rd.ihtReference)
            Future.successful(Ok(iht.views.html.application.iht_kickout_final_application(ihtReference)))
        }
      }
  }
}

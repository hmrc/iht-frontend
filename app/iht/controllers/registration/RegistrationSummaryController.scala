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

package iht.controllers.registration

import iht.config.AppConfig
import iht.connector.{CachingConnector, IhtConnector}
import iht.controllers.ControllerHelper
import iht.metrics.IhtMetrics
import iht.models._
import iht.models.application.ApplicationDetails
import iht.models.enums.StatsSource
import iht.utils._
import javax.inject.Inject
import play.api.i18n.Lang
import play.api.mvc.{MessagesControllerComponents, Request, Result}
import uk.gov.hmrc.auth.core.AuthConnector
import uk.gov.hmrc.http._
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController
import iht.views.html.registration.registration_summary
import iht.views.html.registration.registration_error
import iht.views.html.registration.registration_error_serviceUnavailable

import scala.concurrent.Future

class RegistrationSummaryControllerImpl @Inject()(val ihtConnector: IhtConnector,
                                                  val cachingConnector: CachingConnector,
                                                  val metrics: IhtMetrics,
                                                  val authConnector: AuthConnector,
                                                  val registrationSummaryView: registration_summary,
                                                  val registrationErrorView: registration_error,
                                                  val registrationErrorServiceUnavailableView: registration_error_serviceUnavailable,
                                                  implicit val appConfig: AppConfig,
val cc: MessagesControllerComponents) extends FrontendController(cc) with RegistrationSummaryController

trait RegistrationSummaryController extends RegistrationController with StringHelper with RegistrationDetailsHelper {
  override def guardConditions: Set[Predicate] = guardConditionsRegistrationSummary
  def cachingConnector: CachingConnector
  def metrics: IhtMetrics
  def ihtConnector: IhtConnector
  val registrationSummaryView: registration_summary
  val registrationErrorView: registration_error
  val registrationErrorServiceUnavailableView: registration_error_serviceUnavailable
  def onPageLoad = authorisedForIht {
    implicit request => {
      implicit val request2Lang: Lang = messagesApi.preferred(request).lang

      withRegistrationDetailsRedirectOnGuardCondition { rd =>
        Future.successful(Ok(registrationSummaryView(rd, additionalApplicantType(rd.applicantDetails.get.role))))
      }
    }
  }

  private def saveApplicationDetails(rd: RegistrationDetails, ihtRef: String, ackRef: String)(implicit hc: HeaderCarrier, request: Request[_]) = {
    logger.info("Create initial (empty) Application Details")
    val savedFutureOptionApplicationDetails = ihtConnector.saveApplication(
      CommonHelper.getOrException(rd.applicantDetails).nino,
      ApplicationDetails(status = ApplicationStatus.NotStarted, ihtRef = Some(ihtRef)),
      ackRef)
    savedFutureOptionApplicationDetails.map {
      case Some(_) =>
        fillMetrics(rd)
        Redirect(routes.CompletedRegistrationController.onPageLoad())
      case None =>
        logger.warn("Failed to save application details during registration summary")
        InternalServerError("Failed to save application details during registration summary")
    }
  }

  def onSubmit = authorisedForIht {
    implicit request => {
      def errorHandler: PartialFunction[Throwable, Result] = {
        case ex: GatewayTimeoutException =>
          logger.warn("Request has been timed out while submitting registration", ex)
          InternalServerError(registrationErrorView(ControllerHelper.errorRequestTimeOut))
        case ex: UpstreamErrorResponse if ex.statusCode == 502 &&
          ex.message.contains("Service Unavailable") =>
          logger.warn("Service Unavailable while submitting registration", ex)
          InternalServerError(registrationErrorServiceUnavailableView())
        case ex: UpstreamErrorResponse if ex.statusCode == 502 &&
          ex.message.contains("500 response returned from DES") =>
          throw ex
        case ex: Exception =>
          if (ex.getMessage.contains("Request timed out")) {
            logger.warn("Request has been timed out while submitting registration", ex)
            InternalServerError(registrationErrorView(ControllerHelper.errorRequestTimeOut))
          } else {
            logger.warn("System error while submitting registration", ex)
            InternalServerError(registrationErrorView(ControllerHelper.errorSystem))
          }
      }

      // In order to set up stub data correctly,
      // need to know what the acknowledgement reference will be
      val ackRef = generateAcknowledgeReference
      val futureRegistrationDetails = cachingConnector.getRegistrationDetails
        .map(optRegDetails => getOrExceptionNoRegistration(optRegDetails))
      futureRegistrationDetails.flatMap(registrationDetails => {
        val ihtReference = ihtConnector.submitRegistration(
          CommonHelper.getOrException(registrationDetails.applicantDetails).nino,
          registrationDetails.copy(acknowledgmentReference = ackRef)
        )
        ihtReference.flatMap { ihtReference => {
          if (ihtReference.isEmpty) {
            Future.successful(Redirect(routes.DuplicateRegistrationController.onPageLoad("IHT Reference")))
          } else {
            val storedFutureOptionRegistrationDetails = cachingConnector.storeRegistrationDetails {
              registrationDetails.copy(ihtReference = Some(ihtReference), acknowledgmentReference = ackRef)
            }
            storedFutureOptionRegistrationDetails.flatMap { storedResult => {
              storedResult match {
                case Some(_) => saveApplicationDetails(registrationDetails, ihtReference, ackRef)
                case None =>
                  logger.warn("Storage of registration details fails during registration summary")
                  Future.successful(InternalServerError)
              }
            }
            }
          }
        }
        } recover errorHandler
      })
    }
  }

  /**
    * Fill the metrics input
    *
    * @param regDetails
    */
  private def fillMetrics(regDetails: RegistrationDetails) = {

    Future(metrics.generalStatsCounter(StatsSource.COMPLETED_REG)).onComplete {
      case _ => logger.info("Unable to write to StatsSource metrics repository")
    }

    if (regDetails.coExecutors.nonEmpty) {
      Future(metrics.generalStatsCounter(StatsSource.COMPLETED_REG_ADDITIONAL_EXECUTORS)).onComplete {
        case _ => logger.info("Unable to write to StatsSource metrics repository")
      }
    }
  }
}

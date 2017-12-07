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

package iht.controllers.registration

import iht.connector.{CachingConnector, IhtConnector, IhtConnectors}
import iht.controllers.ControllerHelper
import iht.metrics.Metrics
import iht.models._
import iht.models.application.ApplicationDetails
import iht.models.enums.StatsSource
import iht.utils._
import play.api.Logger
import play.api.Play.current
import play.api.i18n.Messages.Implicits._
import play.api.mvc.{Request, Result}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import uk.gov.hmrc.http.{ConflictException, GatewayTimeoutException, HeaderCarrier}

object RegistrationSummaryController extends RegistrationSummaryController with IhtConnectors {
  def metrics: Metrics = Metrics
}

trait RegistrationSummaryController extends RegistrationController {
  override def guardConditions: Set[Predicate] = guardConditionsRegistrationSummary

  def cachingConnector: CachingConnector

  def metrics: Metrics

  def ihtConnector: IhtConnector

  def onPageLoad = authorisedForIht {
    implicit user => implicit request => {
      withRegistrationDetailsRedirectOnGuardCondition { rd =>
        Future.successful(Ok(iht.views.html.registration.registration_summary(rd, additionalApplicantType(rd.applicantDetails.get.role))))
      }
    }
  }

  private def saveApplicationDetails(rd: RegistrationDetails, ihtRef: String, ackRef: String)(implicit hc: HeaderCarrier, request: Request[_]) = {
    Logger.info("Create initial (empty) Application Details")
    val savedFutureOptionApplicationDetails = ihtConnector.saveApplication(
      CommonHelper.getOrException(rd.applicantDetails).nino,
      ApplicationDetails(status = ApplicationStatus.NotStarted, ihtRef = Some(ihtRef)),
      ackRef)
    savedFutureOptionApplicationDetails.map {
      case Some(_) =>
        fillMetrics(rd)
        Redirect(routes.CompletedRegistrationController.onPageLoad())
      case None =>
        Logger.warn("Failed to save application details during registration summary")
        InternalServerError("Failed to save application details during registration summary")
    }
  }

  def onSubmit = authorisedForIht {
    implicit user => implicit request => {
      def errorHandler: PartialFunction[Throwable, Result] = {
        case ex: GatewayTimeoutException => {
          Logger.debug("Request has been timed out while submitting registration")
          Ok(iht.views.html.registration.registration_error(ControllerHelper.errorRequestTimeOut))
        }
        case ex: Exception => {
          if (ex.getMessage.contains("Request timed out")) {
            Logger.debug("Request has been timed out while submitting registration")
            Ok(iht.views.html.registration.registration_error(ControllerHelper.errorRequestTimeOut))
          } else {
            Logger.debug("System error while submitting registration")
            Ok(iht.views.html.registration.registration_error(ControllerHelper.errorSystem))
          }
        }
      }

      // In order to set up stub data correctly,
      // need to know what the acknowledgement reference will be
      val ackRef = StringHelper.generateAcknowledgeReference
      val futureRegistrationDetails = cachingConnector.getRegistrationDetails
        .map(optRegDetails => RegistrationDetailsHelper.getOrExceptionNoRegistration(optRegDetails))
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
                  Logger.warn("Storage of registration details fails during registration summary")
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

    Future(metrics.generalStatsCounter(StatsSource.COMPLETED_REG)).onFailure {
      case _ => Logger.info("Unable to write to StatsSource metrics repository")
    }

    if (regDetails.coExecutors.nonEmpty) {
      Future(metrics.generalStatsCounter(StatsSource.COMPLETED_REG_ADDITIONAL_EXECUTORS)).onFailure {
        case _ => Logger.info("Unable to write to StatsSource metrics repository")
      }
    }
  }
}

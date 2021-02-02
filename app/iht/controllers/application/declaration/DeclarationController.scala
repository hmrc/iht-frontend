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

package iht.controllers.application.declaration

import iht.config.AppConfig
import iht.connector.{CachingConnector, IhtConnector}
import iht.controllers.ControllerHelper
import iht.controllers.application.ApplicationController
import iht.forms.ApplicationForms
import iht.metrics.IhtMetrics
import iht.models._
import iht.models.application.{ApplicationDetails, ProbateDetails}
import iht.models.enums.StatsSource
import iht.utils.CommonHelper._
import iht.utils.{CommonHelper, _}
import iht.viewmodels.application.DeclarationViewModel
import javax.inject.Inject
import play.api.Logging
import play.api.i18n.Lang
import play.api.mvc._
import uk.gov.hmrc.auth.core.AuthConnector
import uk.gov.hmrc.auth.core.retrieve.v2.Retrievals.{nino => ninoRetrieval}
import uk.gov.hmrc.http.{GatewayTimeoutException, HeaderCarrier, UpstreamErrorResponse}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController
import uk.gov.hmrc.play.partials.FormPartialRetriever

import scala.concurrent.Future

class DeclarationControllerImpl @Inject()(val metrics: IhtMetrics,
                                          val ihtConnector: IhtConnector,
                                          val cachingConnector: CachingConnector,
                                          val authConnector: AuthConnector,
                                          val formPartialRetriever: FormPartialRetriever,
                                          implicit val appConfig: AppConfig,
                                          val cc: MessagesControllerComponents) extends FrontendController(cc) with DeclarationController with Logging

trait DeclarationController extends ApplicationController with StringHelper with Logging {
  def cachingConnector: CachingConnector
  def ihtConnector: IhtConnector
  val metrics: IhtMetrics

  def onPageLoad: Action[AnyContent] = authorisedForIhtWithRetrievals(ninoRetrieval) { userNino =>
    implicit request => {
      withRegistrationDetails { regDetails =>
        getApplicationDetails(getOrException(regDetails.ihtReference), regDetails.acknowledgmentReference, userNino).flatMap { appDetails =>
          realTimeRiskingMessage(appDetails, CommonHelper.getOrException(regDetails.ihtReference), getNino(userNino), ihtConnector).map { optRiskMsg =>
            val englishMessages = cc.messagesApi.preferred(Seq(Lang("en")))

            Ok(iht.views.html.application.declaration.declaration(
              DeclarationViewModel(ApplicationForms.declarationForm,
                appDetails,
                regDetails,
                getNino(userNino),
                ihtConnector,
                optRiskMsg
              ), englishMessages
            ))
          }
        }
      }
    }
  }

  private def withErrors(userNino: Option[String], rd: RegistrationDetails)
                        (implicit request: Request[_]) =
    getApplicationDetails(getOrException(rd.ihtReference), rd.acknowledgmentReference, userNino) flatMap { appDetails =>
      realTimeRiskingMessage(
        appDetails,
        CommonHelper.getOrException(rd.ihtReference),
        getNino(userNino),
        ihtConnector
      ) map { optRiskMsg =>
        val englishMessages = cc.messagesApi.preferred(Seq(Lang("en")))

        BadRequest(iht.views.html.application.declaration.declaration(
          DeclarationViewModel(ApplicationForms.declarationForm,
            appDetails,
            rd,
            getNino(userNino),
            ihtConnector,
            optRiskMsg
          ), englishMessages
        ))
      }
    }

  def onSubmit = authorisedForIhtWithRetrievals(ninoRetrieval) { userNino =>
    implicit request => {
      withRegistrationDetails { rd =>
        if (rd.coExecutors.nonEmpty) {
          val boundForm = ApplicationForms.declarationForm.bindFromRequest
          boundForm.fold(
            formWithErrors => {
              LogHelper.logFormError(formWithErrors)
              withErrors(userNino, rd)
            }, {
              case true =>
                processApplicationOrRedirect(userNino)
              case _ =>
                logger.warn("isDeclared is false. Redirecting to InternalServerError")
                Future.successful(InternalServerError)
            }
          )
        } else {
          processApplicationOrRedirect(userNino)
        }
      } recover {
        case ex: UpstreamErrorResponse if ex.statusCode == 502 &&
          ex.message.contains("Service Unavailable") => {
          logger.warn("Service Unavailable while submitting application", ex)
          InternalServerError(iht.views.html.estateReports.estateReports_error_serviceUnavailable())
        }
      }
    }
  }

  private[controllers] def realTimeRiskingMessage(ad: ApplicationDetails,
                                                  ihtAppReference: String,
                                                  nino: String,
                                                  ihtConnector: IhtConnector)
                                                 (implicit request: Request[_]): Future[Option[String]] = {
    val moneyValue = for {
      assets <- ad.allAssets
      money <- assets.money
    } yield {
      money.value.getOrElse(BigDecimal(0)) + money.shareValue.getOrElse(BigDecimal(0))
    }

    val riskMessage = moneyValue.fold(getRealTimeRiskMessage(ihtConnector, ihtAppReference, nino)) {
      result =>
        if (result == 0) {
          getRealTimeRiskMessage(ihtConnector, ihtAppReference, nino)
        } else {
          logger.debug("Money has a value, hence no need to check for real-time risking message")
          Future.successful(None)
        }
    }

    riskMessage
  }

  private[controllers] def getRealTimeRiskMessage(ihtConnector: IhtConnector, ihtAppReference: String, nino: String)
                                                 (implicit hc: HeaderCarrier) = {
    logger.debug("Money has no value, hence need to check for real-time risking message")
    ihtConnector.getRealtimeRiskingMessage(ihtAppReference, nino).recover {
      case e: Exception =>
        logger.warn(s"Problem getting realtime risking message: ${e.getMessage}")
        None
    }
  }


  private def processApplicationOrRedirect(userNino: Option[String])(implicit request: Request[_],
                                                                     hc: HeaderCarrier): Future[Result] = {
    withRegistrationDetails { rd =>
      val ihtReference = CommonHelper.getOrException(rd.ihtReference)
      ihtConnector.getCaseDetails(getNino(userNino), ihtReference) flatMap { rd =>
        if (rd.status == ApplicationStatus.AwaitingReturn) {
          processApplication(getNino(userNino))
        } else {
          Future.successful(Redirect(
            iht.controllers.estateReports.routes.YourEstateReportsController.onPageLoad()))
        }
      }
    }
  }

  private def processApplication(nino: String)(implicit request: Request[_],
                                               hc: HeaderCarrier): Future[Result] = {
    withRegistrationDetails { regDetails =>
      val ihtAppReference = regDetails.ihtReference
      val acknowledgement = regDetails.acknowledgmentReference

      ihtConnector.getApplication(nino, ihtAppReference, acknowledgement).flatMap{ applicationDetails =>

        val ad1 = CommonHelper.getOrExceptionNoApplication(applicationDetails)

        fillMetricsData(ad1, regDetails)

        val updatedAppDetails: ApplicationDetails = ad1.copy(reasonForBeingBelowLimit = calculateReasonForBeingBelowLimit(ad1))

        ihtConnector.saveApplication(nino, updatedAppDetails, acknowledgement).flatMap(optionSavedApplication => {
          if (optionSavedApplication.isEmpty) {
            logger.debug("Unable to save application details: reasonForBeingBelowLimit not saved")
          }
          submitApplication(nino, updatedAppDetails, ihtAppReference)
        }).flatMap {
          case None =>
            Future.successful(Redirect(iht.controllers.routes.NonLeadExecutorController.onPageLoad()))
          case returnId@Some(_) =>
            processToGetProbateDetails(nino, ihtAppReference, returnId).flatMap(probateDetails => storeProbateDetails(probateDetails))
              .map { _ =>
                Redirect(iht.controllers.application.declaration.routes.DeclarationReceivedController.onPageLoad())
              }
        }
      }
    }
  }

  private def submitApplication(nino: String,
                                updatedAppDetails: ApplicationDetails,
                                ihtAppReference: Option[String])
                               (implicit request: Request[_]): Future[Option[String]] = {

    logger.debug("Processing submission of application with IHT reference " + ihtAppReference + ":-\n" + updatedAppDetails.toString)
    ihtConnector.submitApplication(ihtAppReference, nino, updatedAppDetails)
  }

  private def processToGetProbateDetails(nino: String, ihtAppReference: Option[String], returnId: Option[String])
                                        (implicit request: Request[_]): Future[Option[ProbateDetails]] = {

    logger.debug("Submission completed successfully with return id ::: " + returnId)
    Future(metrics.generalStatsCounter(StatsSource.COMPLETED_APP)).onComplete {
      case _ => logger.info("Unable to write to StatsSource metrics repository")
    }
    logger.info("Processing to get Probate details")
    returnId.fold[Future[Option[ProbateDetails]]](throw new RuntimeException("Unable to submit application")) { idVal =>
      ihtConnector.deleteApplication(nino, ihtAppReference)
      getProbateDetails(nino, ihtAppReference, idVal.trim)
    }
  }

  private def storeProbateDetails(probateDetails: Option[ProbateDetails])
                                 (implicit request: Request[_]): Future[Option[ProbateDetails]]= {
    probateDetails match {
      case Some(probateObject) =>
        logger.info("Saving probate details in session")
        cachingConnector.storeProbateDetails(probateObject)
      case _ =>
        logger.warn("Probate details could not be retrieved")
        Future.successful(None)
    }
  }

  private def getProbateDetails(nino: String, ihtReference: String, ihtReturnId: String)
                               (implicit hc: HeaderCarrier): Future[Option[ProbateDetails]] = {

    ihtConnector.getProbateDetails(nino, ihtReference, ihtReturnId.trim) map {
      case Some(probateObject) =>
        logger.info("Probate details received successfully")
        Some(probateObject)
      case _ =>
        logger.warn("Problem occured while retrieving Probate details ")
        None
    } recover {
      case e: Exception =>
        logger.warn(s"Problem getting probate details: ${e.getMessage}")
        None
    }
  }

  def submissionException(exception: Throwable): String = {
    exception match {
      case _: GatewayTimeoutException =>
        logger.debug("Request has been timed out while submitting application")
        ControllerHelper.errorServiceUnavailable
      case ex: Exception =>
        if (ex.getMessage.contains("Request timed out") || ex.getMessage.contains("Connection refused")
          || ex.getMessage.contains("Service Unavailable") || ex.getMessage.contains(ControllerHelper.desErrorCode503)) {
          logger.debug("Request has been timed out while submitting application")
          ControllerHelper.errorServiceUnavailable
        } else if (ex.getMessage.contains(ControllerHelper.desErrorCode502) || ex.getMessage.contains(ControllerHelper.desErrorCode504)) {
          logger.debug("System error while submitting application")
          ControllerHelper.errorRequestTimeOut
        } else {
          logger.debug("System error while submitting application")
          ControllerHelper.errorSystem
        }
      case _ =>
        logger.debug("System error while submitting application")
        ControllerHelper.errorSystem
    }
  }

  /*
   * Calculate the reason for declaration
   */
  def calculateReasonForBeingBelowLimit(appDetails: ApplicationDetails): Option[String] = {

    val grossEstateValue = appDetails.totalValue
    val grossEstateValueMinusExemptionsAndLiabilities = if (appDetails.totalExemptionsValue > 0) {
      grossEstateValue - appDetails.totalExemptionsValue - appDetails.totalLiabilitiesValue
    } else {
      grossEstateValue
    }

    if (grossEstateValue <= appConfig.taxThreshold) {
      Some(ControllerHelper.ReasonForBeingBelowLimitExceptedEstate)
    } else if (grossEstateValueMinusExemptionsAndLiabilities <= appConfig.taxThreshold &&
      grossEstateValue <= appConfig.grossEstateLimit &&
      grossEstateValue > appConfig.taxThreshold) {
      Some(ControllerHelper.ReasonForBeingBelowLimitSpouseCivilPartnerOrCharityExemption)
    } else if (grossEstateValueMinusExemptionsAndLiabilities <= appConfig.transferredNilRateBand) {
      Some(ControllerHelper.ReasonForBeingBelowLimitTNRB)
    } else {
      None
    }
  }

  /**
    * Creates various metric data from ApplicationDetails object
    */
  private def fillMetricsData(appDetails: ApplicationDetails, regDetails: RegistrationDetails): Unit = {
    val assetValue = appDetails.totalAssetsValue
    val giftValue = CommonHelper.getOrZero(appDetails.totalPastYearsGiftsOption)
    val debtsValue = appDetails.totalLiabilitiesValue
    val exemptionsValue = appDetails.totalExemptionsValue
    val totalAssets = assetValue + giftValue

    //Getting stats for Application that has additional executors
    if (regDetails.coExecutors.nonEmpty) {
      Future(metrics.generalStatsCounter(StatsSource.ADDITIONAL_EXECUTOR_APP)).onComplete {
        case _ => logger.info("Unable to write to StatsSource metrics repository")
      }
    }

    statsSource(appDetails, giftValue, debtsValue, exemptionsValue, totalAssets) foreach { stats =>
      Future(metrics.generalStatsCounter(stats)).onComplete {
        case _ => logger.info("Unable to write to StatsSource metrics repository")
      }
    }
  }

  def statsSource(appDetails: ApplicationDetails,
                  giftValue: BigDecimal,
                  debtsValue: BigDecimal,
                  exemptionsValue: BigDecimal,
                  totalAssets: BigDecimal): Option[StatsSource.Value] = {
    val total = totalAssets + giftValue + debtsValue + exemptionsValue
    if (total == 0) {
      Some(StatsSource.NO_ASSETS_DEBTS_EXEMPTIONS_APP)
    } else if (totalAssets > 0 && debtsValue > 0 && exemptionsValue > 0 && appDetails.increaseIhtThreshold.isDefined) {
      Some(StatsSource.ASSET_DEBTS_EXEMPTIONS_TNRB_APP)
    } else if (totalAssets > 0 && debtsValue > 0 && exemptionsValue == 0) {
      Some(StatsSource.ASSETS_AND_DEBTS_ONLY_APP)
    } else if (totalAssets > 0) {
      Some(StatsSource.ASSETS_ONLY_APP)
    } else {
      None
    }
  }
}

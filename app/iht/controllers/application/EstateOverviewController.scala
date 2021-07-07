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

package iht.controllers.application

import iht.config.AppConfig
import iht.connector.{CachingConnector, IhtConnector}
import iht.models.RegistrationDetails
import iht.models.application.ApplicationDetails
import iht.utils.CommonHelper._
import iht.utils.tnrb.TnrbHelper
import iht.utils.{ApplicationKickOutNonSummaryHelper, ApplicationStatus, EstateNotDeclarableHelper, ExemptionsGuidanceHelper, RegistrationDetailsHelper, StringHelper, SubmissionDeadlineHelper}
import iht.viewmodels.application.overview.EstateOverviewViewModel
import iht.views.html.application.overview.estate_overview_json_error
import iht.views.html.application.overview.estate_overview
import javax.inject.Inject
import org.joda.time.LocalDate
import play.api.mvc.{MessagesControllerComponents, _}
import uk.gov.hmrc.auth.core.AuthConnector
import uk.gov.hmrc.auth.core.retrieve.v2.Retrievals.{nino => ninoRetrieval}
import uk.gov.hmrc.http._
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController

import scala.concurrent.Future

class EstateOverviewControllerImpl @Inject()(val ihtConnector: IhtConnector,
                                             val cachingConnector: CachingConnector,
                                             val authConnector: AuthConnector,
                                             val estateOverviewJsonErrorView: estate_overview_json_error,
                                             val estateOverviewView: estate_overview,
                                             implicit val appConfig: AppConfig,
                                             val cc: MessagesControllerComponents) extends FrontendController(cc) with EstateOverviewController

trait EstateOverviewController extends ApplicationController with ExemptionsGuidanceHelper
  with EstateNotDeclarableHelper with ApplicationKickOutNonSummaryHelper with TnrbHelper with StringHelper with RegistrationDetailsHelper {

  lazy val checkedEverythingQuestionPage = iht.controllers.application.declaration.routes.CheckedEverythingQuestionController.onPageLoad()

  lazy val kickOutPage = iht.controllers.application.routes.KickoutAppController.onPageLoad()
  lazy val exemptionsOverviewPage= iht.controllers.application.exemptions.routes.ExemptionsOverviewController.onPageLoad()
  lazy val tnrbGuidancePage = iht.controllers.application.tnrb.routes.TnrbGuidanceController.onSystemPageLoad()

  def cachingConnector: CachingConnector

  def ihtConnector: IhtConnector
  val estateOverviewJsonErrorView: estate_overview_json_error
  val estateOverviewView: estate_overview
  def onPageLoadWithIhtRef(ihtReference: String) = authorisedForIhtWithRetrievals(ninoRetrieval) { userNino =>
    implicit request => {
      def errorHandler: PartialFunction[Throwable, Result] = {
        case ex: UpstreamErrorResponse if ex.statusCode == 500 &&
          ex.getMessage.contains("JSON validation against schema failed") => {
          logger.warn("JSON validation against schema failed. Redirecting to error page", ex)
          InternalServerError(estateOverviewJsonErrorView())
        }
      }
      val nino = getNino(userNino)

      ihtConnector.getCaseDetails(nino, ihtReference).flatMap {
        caseDetails =>
          caseDetails.status match {
            case ApplicationStatus.AwaitingReturn => {
              for {
                Some(registrationDetails) <- cachingConnector.storeRegistrationDetails(caseDetails)
                applicationDetails <- getApplicationDetails(ihtReference, registrationDetails.acknowledgmentReference, userNino)
                deadlineDate <- getDeadline(applicationDetails, userNino)
                redirectCall: Option[Call] <- guidanceRedirect(
                  routes.EstateOverviewController.onPageLoadWithIhtRef(ihtReference), applicationDetails, cachingConnector
                )
              } yield {
                redirectCall match {
                  case Some(call) => Redirect(call)
                  case None => {
                    val viewModel = EstateOverviewViewModel(registrationDetails, applicationDetails, deadlineDate)
                    Ok(estateOverviewView(viewModel))
                  }
                }
              }
            }
            case _ => Future.successful(Redirect(iht.controllers.estateReports.routes.YourEstateReportsController.onPageLoad()))
          }
      } recover errorHandler
    }
  }


  /**
    * This method has been mapped to GET type in taxreturn.routes file because it has been called
    * from the links in estate_overview.scala.html that does not contain any form.
    *
    * @param ihtReference
    */
  def onContinueOrDeclarationRedirect(ihtReference: String): Action[AnyContent] = authorisedForIhtWithRetrievals(ninoRetrieval) { userNino =>
    implicit request => {
      val nino = getNino(userNino)
      withRegistrationDetails { regDetails =>
        for {
          appDetails <- getApplicationDetails(ihtReference, regDetails.acknowledgmentReference, userNino)
          appDetailsWithKickOutUpdatedOpt <- ihtConnector.saveApplication(nino, appKickoutUpdateKickout(
            checks = checksBackend,
            registrationDetails = regDetails,
            applicationDetails = appDetails), regDetails.acknowledgmentReference)

          appDetailWithKickOutUpdated = getOrException(appDetailsWithKickOutUpdatedOpt)
          result <- getRedirect(regDetails, appDetailWithKickOutUpdated)

        } yield {
          result
        }
      }
    }
  }

  private def getDeadline(ad: ApplicationDetails, userNino: Option[String])
                         (implicit headerCarrier: HeaderCarrier): Future[LocalDate] = {
    SubmissionDeadlineHelper(getNino(userNino), ad.ihtRef.getOrElse(""), ihtConnector, headerCarrier)
  }

  private def getRedirect(regDetails: RegistrationDetails, appDetails: ApplicationDetails)
                         (implicit hc: HeaderCarrier) = {
    val netEstateValue = appDetails.netValueAfterExemptionAndDebtsForPositiveExemption
    val tnrb = appDetails.increaseIhtThreshold
    val maritalStatus = getMaritalStatus(regDetails)
    val kickOutReason = appDetails.kickoutReason

    val declarableCondition1 = netEstateValue <= appConfig.exemptionsThresholdValue.toInt &&
                                tnrb.isEmpty && kickOutReason.isEmpty

    val declarableCondition2 = netEstateValue <= appConfig.transferredNilRateBand.toInt &&
      tnrb.isDefined && !maritalStatus.equals(appConfig.statusSingle) && kickOutReason.isEmpty

    if (declarableCondition1 || declarableCondition2) {
      Future.successful(Redirect(checkedEverythingQuestionPage))
    } else {
      getNotDeclarableRedirect(regDetails, appDetails)
    }

  }

  private def getNotDeclarableRedirect(regDetails: RegistrationDetails,
                                        appDetails: ApplicationDetails)
                                      (implicit headerCarrier: HeaderCarrier): Future[Result] ={

    if (isEstateOverGrossEstateLimit(appDetails)) {
      Future.successful(Redirect(kickOutPage))
    } else if (isEstateValueMoreThanTaxThresholdBeforeExemptionsStarted(appDetails)) {
      getExemptionsGuidanceRedirect(getOrException(regDetails.ihtReference), appDetails, cachingConnector).map(opCall =>
        Redirect(getExemptionsLinkUrlForContinueButton(opCall))
      )
    } else if (isEstateValueMoreThanTaxThresholdBeforeTnrbStarted(appDetails, regDetails)) {
      Future.successful(Redirect(getTnrbLinkUrlForContinueButton(regDetails, appDetails)))
    } else if (isEstateValueMoreThanTaxThresholdBeforeTnrbFinished(appDetails, regDetails)) {
      Future.successful(Redirect(getEntryPointForTnrb(regDetails, appDetails)))
    } else {
      Future.successful(Redirect(kickOutPage))
    }
  }

  /**
    * Returns ExemptionsOverview page if guidance has been seen
    *
    * @param url
    * @return
    */
  private def getExemptionsLinkUrlForContinueButton(url: Option[Call]) =  url.fold(exemptionsOverviewPage)(identity)

  private def getTnrbLinkUrlForContinueButton(regDetails: RegistrationDetails, appDetails: ApplicationDetails) = {
    if (appDetails.isWidowCheckQuestionAnswered) {
      getEntryPointForTnrb(regDetails, appDetails)
    } else {
      tnrbGuidancePage
    }
  }

  /**
    * Gets the ExemptionsGuidance Url based on whether it has been seen
    *
    * @param ihtReference
    * @param appDetails
    * @param cachingConnector
    * @param headerCarrier
    * @return
    */
  private def getExemptionsGuidanceRedirect(ihtReference: String, appDetails: ApplicationDetails, cachingConnector: CachingConnector)
                                           (implicit headerCarrier: HeaderCarrier): Future[Option[Call]] = {
   guidanceRedirect(routes.EstateOverviewController.onPageLoadWithIhtRef(ihtReference), appDetails, cachingConnector)
  }
}

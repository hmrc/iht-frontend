/*
 * Copyright 2019 HM Revenue & Customs
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

import iht.config.FrontendAuthConnector
import iht.connector.{CachingConnector, IhtConnector, IhtConnectors}
import iht.constants.IhtProperties._
import iht.models.RegistrationDetails
import iht.models.application.ApplicationDetails
import iht.utils.CommonHelper._
import iht.utils.RegistrationDetailsHelper._
import iht.utils.tnrb.TnrbHelper
import iht.utils.{ApplicationKickOutNonSummaryHelper, ApplicationStatus, EstateNotDeclarableHelper, ExemptionsGuidanceHelper, StringHelper, SubmissionDeadlineHelper}
import iht.viewmodels.application.overview.EstateOverviewViewModel
import javax.inject.Inject
import org.joda.time.LocalDate
import play.api.Logger
import play.api.Play.current
import play.api.i18n.Messages.Implicits._
import play.api.mvc._
import uk.gov.hmrc.auth.core.AuthConnector
import uk.gov.hmrc.auth.core.PlayAuthConnector
import uk.gov.hmrc.auth.core.retrieve.v2.Retrievals.{nino => ninoRetrieval}
import uk.gov.hmrc.http._

import scala.concurrent.Future

class EstateOverviewControllerImpl @Inject()() extends EstateOverviewController with IhtConnectors

trait EstateOverviewController extends ApplicationController {

  lazy val checkedEverythingQuestionPage = iht.controllers.application.declaration.routes.CheckedEverythingQuestionController.onPageLoad()

  lazy val kickOutPage = iht.controllers.application.routes.KickoutAppController.onPageLoad()
  lazy val exemptionsOverviewPage= iht.controllers.application.exemptions.routes.ExemptionsOverviewController.onPageLoad()
  lazy val tnrbGuidancePage = iht.controllers.application.tnrb.routes.TnrbGuidanceController.onSystemPageLoad()

  def cachingConnector: CachingConnector

  def ihtConnector: IhtConnector

  def onPageLoadWithIhtRef(ihtReference: String) = authorisedForIhtWithRetrievals(ninoRetrieval) { userNino =>
    implicit request => {
      def errorHandler: PartialFunction[Throwable, Result] = {
        case ex: Upstream5xxResponse if ex.upstreamResponseCode == 500 &&
          ex.getMessage.contains("JSON validation against schema failed") => {
          Logger.warn("JSON validation against schema failed. Redirecting to error page", ex)
          InternalServerError(iht.views.html.application.overview.estate_overview_json_error())
        }
      }
      val nino = StringHelper.getNino(userNino)

      ihtConnector.getCaseDetails(nino, ihtReference).flatMap {
        caseDetails =>
          caseDetails.status match {
            case ApplicationStatus.AwaitingReturn => {
              for {
                Some(registrationDetails) <- cachingConnector.storeRegistrationDetails(caseDetails)
                applicationDetails <- getApplicationDetails(ihtReference, registrationDetails.acknowledgmentReference, userNino)
                deadlineDate <- getDeadline(applicationDetails, userNino)
                redirectCall: Option[Call] <- ExemptionsGuidanceHelper.guidanceRedirect(
                  routes.EstateOverviewController.onPageLoadWithIhtRef(ihtReference), applicationDetails, cachingConnector)
              } yield {
                redirectCall match {
                  case Some(call) => Redirect(call)
                  case None => {
                    val viewModel = EstateOverviewViewModel(registrationDetails, applicationDetails, deadlineDate)
                    Ok(iht.views.html.application.overview.estate_overview(viewModel))
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
      val nino = StringHelper.getNino(userNino)
      withRegistrationDetails { regDetails =>
        for {
          appDetails <- getApplicationDetails(ihtReference, regDetails.acknowledgmentReference, userNino)
          appDetailsWithKickOutUpdatedOpt <- ihtConnector.saveApplication(nino, ApplicationKickOutNonSummaryHelper.updateKickout(
            checks = ApplicationKickOutNonSummaryHelper.checksBackend,
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

  private def getDeadline(ad: ApplicationDetails, userNino: Option[String])(
    implicit headerCarrier: HeaderCarrier): Future[LocalDate] = {
    SubmissionDeadlineHelper(StringHelper.getNino(userNino), ad.ihtRef.getOrElse(""), ihtConnector, headerCarrier)
  }

  private def getRedirect(regDetails: RegistrationDetails, appDetails: ApplicationDetails)(implicit hc: HeaderCarrier) = {
    val netEstateValue = appDetails.netValueAfterExemptionAndDebtsForPositiveExemption
    val tnrb = appDetails.increaseIhtThreshold
    val maritalStatus = getMaritalStatus(regDetails)
    val kickOutReason = appDetails.kickoutReason

    val declarableCondition1 = netEstateValue <= exemptionsThresholdValue.toInt &&
                                tnrb.isEmpty && kickOutReason.isEmpty

    val declarableCondition2 = netEstateValue <= transferredNilRateBand.toInt &&
      tnrb.isDefined && !maritalStatus.equals(statusSingle) && kickOutReason.isEmpty

    if (declarableCondition1 || declarableCondition2) {
      Future.successful(Redirect(checkedEverythingQuestionPage))
    } else {
      getNotDeclarableRedirect(regDetails, appDetails)
    }

  }

  private def getNotDeclarableRedirect(regDetails: RegistrationDetails,
                                        appDetails: ApplicationDetails)
                                      (implicit headerCarrier: HeaderCarrier): Future[Result] ={

    if (EstateNotDeclarableHelper.isEstateOverGrossEstateLimit(appDetails)) {
      Future.successful(Redirect(kickOutPage))
    } else if (EstateNotDeclarableHelper.isEstateValueMoreThanTaxThresholdBeforeExemptionsStarted(appDetails)) {
      getExemptionsGuidanceRedirect(getOrException(regDetails.ihtReference), appDetails, cachingConnector).map(opCall =>
        Redirect(getExemptionsLinkUrlForContinueButton(opCall))
      )
    } else if (EstateNotDeclarableHelper.isEstateValueMoreThanTaxThresholdBeforeTnrbStarted(appDetails, regDetails)) {
      Future.successful(Redirect(getTnrbLinkUrlForContinueButton(regDetails, appDetails)))
    } else if (EstateNotDeclarableHelper.isEstateValueMoreThanTaxThresholdBeforeTnrbFinished(appDetails, regDetails)) {
      Future.successful(Redirect(TnrbHelper.getEntryPointForTnrb(regDetails, appDetails)))
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
      TnrbHelper.getEntryPointForTnrb(regDetails, appDetails)
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
  private def getExemptionsGuidanceRedirect(ihtReference: String,
                                 appDetails: ApplicationDetails,
                                 cachingConnector: CachingConnector)
                                (implicit headerCarrier: HeaderCarrier): Future[Option[Call]] = {
   ExemptionsGuidanceHelper.guidanceRedirect(routes.EstateOverviewController.onPageLoadWithIhtRef(ihtReference), appDetails, cachingConnector)
  }
}

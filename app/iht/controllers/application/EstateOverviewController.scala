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

package iht.controllers.application

import iht.connector.{CachingConnector, IhtConnector, IhtConnectors}
import iht.constants.IhtProperties._
import iht.models.RegistrationDetails
import iht.models.application.ApplicationDetails
import iht.utils.CommonHelper._
import iht.utils.RegistrationDetailsHelper._
import iht.utils.tnrb.TnrbHelper
import iht.utils.{ApplicationKickOutNonSummaryHelper, ApplicationStatus, EstateNotDeclarableHelper, ExemptionsGuidanceHelper, StringHelper, SubmissionDeadlineHelper}
import iht.viewmodels.application.overview.EstateOverviewViewModel
import org.joda.time.LocalDate
import play.api.Play.current
import play.api.i18n.Messages.Implicits._
import play.api.mvc._
import uk.gov.hmrc.play.frontend.auth.AuthContext
import uk.gov.hmrc.play.http.HeaderCarrier

import scala.concurrent.duration.Duration
import scala.concurrent.{Await, Future}

object EstateOverviewController extends EstateOverviewController with IhtConnectors

trait EstateOverviewController extends ApplicationController {

val checkedEverythingQuestionPage = iht.controllers.application.declaration.routes.CheckedEverythingQuestionController.onPageLoad()

  val kickOutPage = iht.controllers.application.routes.KickoutController.onPageLoad()
  val exemptionsOverviewPage= iht.controllers.application.exemptions.routes.ExemptionsOverviewController.onPageLoad()
  val tnrbGuidancePage = iht.controllers.application.tnrb.routes.TnrbGuidanceController.onSystemPageLoad()

  def cachingConnector: CachingConnector

  def ihtConnector: IhtConnector

  def onPageLoadWithIhtRef(ihtReference: String) = authorisedForIht {
    implicit user =>
      implicit request => {
        val nino = StringHelper.getNino(user)

        ihtConnector.getCaseDetails(nino, ihtReference).flatMap {
          caseDetails =>
            caseDetails.status match {
              case ApplicationStatus.AwaitingReturn => {
                for {
                  Some(registrationDetails) <- cachingConnector.storeRegistrationDetails(caseDetails)
                  applicationDetails <- getApplicationDetails(ihtReference, registrationDetails.acknowledgmentReference)
                  deadlineDate <- getDeadline(applicationDetails, user)
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
              case _ => Future.successful(Redirect(iht.controllers.home.routes.YourEstateReportsController.onPageLoad()))
            }
        }
      }
  }

  /**
    * This method has been mapped to GET type in taxreturn.routes file because it has been called
    * from the links in estate_overview.scala.html that does not contain any form.
    *
    * @param ihtReference
    */
  def onContinueOrDeclarationRedirect(ihtReference: String) = authorisedForIht {
    implicit user => implicit request => {

      val nino = StringHelper.getNino(user)
      withRegistrationDetails { regDetails =>
        for {
          appDetails <- getApplicationDetails(ihtReference, regDetails.acknowledgmentReference)
          appDetailsWithKickOutUpdatedOpt <- ihtConnector.saveApplication(nino, ApplicationKickOutNonSummaryHelper.updateKickout(
            checks = ApplicationKickOutNonSummaryHelper.checksBackend,
            registrationDetails = regDetails,
            applicationDetails = appDetails), regDetails.acknowledgmentReference)

          appDetailWithKickOutUpdated = getOrException(appDetailsWithKickOutUpdatedOpt)

        } yield {
          getRedirect(regDetails, appDetailWithKickOutUpdated)
        }
      }
    }
  }

  private def getDeadline(ad: ApplicationDetails, user: AuthContext)(
    implicit headerCarrier: HeaderCarrier): Future[LocalDate] = {
    SubmissionDeadlineHelper(StringHelper.getNino(user), ad.ihtRef.getOrElse(""), ihtConnector, headerCarrier)
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
      Redirect(checkedEverythingQuestionPage)
    } else {
      getNotDeclarableRedirect(regDetails, appDetails)
    }

  }

  private def getNotDeclarableRedirect(regDetails: RegistrationDetails,
                                        appDetails: ApplicationDetails)
                                      (implicit headerCarrier: HeaderCarrier): Result ={

    if (EstateNotDeclarableHelper.isEstateOverGrossEstateLimit(appDetails)) {
      Redirect(kickOutPage)
    } else if (EstateNotDeclarableHelper.isEstateValueMoreThanTaxThresholdBeforeExemptionsStarted(appDetails)) {
      Redirect(getExemptionsLinkUrlForContinueButton(
        getExemptionsGuidanceRedirect(getOrException(regDetails.ihtReference), appDetails, cachingConnector)))
    } else if (EstateNotDeclarableHelper.isEstateValueMoreThanTaxThresholdBeforeTnrbStarted(appDetails, regDetails)) {
      Redirect(getTnrbLinkUrlForContinueButton(regDetails, appDetails))
    } else if (EstateNotDeclarableHelper.isEstateValueMoreThanTaxThresholdBeforeTnrbFinished(appDetails, regDetails)) {
      Redirect(TnrbHelper.getEntryPointForTnrb(regDetails, appDetails))
    } else {
      Redirect(kickOutPage)
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
                                (implicit headerCarrier: HeaderCarrier): Option[Call] = {
    val result = ExemptionsGuidanceHelper.guidanceRedirect(
      routes.EstateOverviewController.onPageLoadWithIhtRef(ihtReference), appDetails, cachingConnector)
    Await.result(result, Duration.Inf)
  }

}

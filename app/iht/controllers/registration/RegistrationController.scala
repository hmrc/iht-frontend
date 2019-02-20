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

package iht.controllers.registration

import iht.connector.CachingConnector
import iht.controllers.auth.IhtBaseController
import iht.models.RegistrationDetails
import iht.utils.AddressHelper._
import iht.utils.ApplicantHelper._
import iht.utils.CommonHelper._
import iht.utils.DeceasedInfoHelper._
import iht.utils.{IhtSection, RegistrationKickOutHelper}
import play.api.Logger
import play.api.i18n.Lang
import play.api.mvc.{AnyContent, Call, Request, Result}
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.bootstrap.controller.FrontendController
import uk.gov.hmrc.play.partials.FormPartialRetriever

import scala.concurrent.Future


trait RegistrationController extends FrontendController with IhtBaseController {
  type Predicate = (RegistrationDetails, String) => Boolean
  override lazy val ihtSection = IhtSection.Registration

  def cachingConnector: CachingConnector

  protected def language(implicit request: Request[AnyContent]) = request.acceptLanguages.foldRight(Lang.apply("en"))((_, lang) => lang)

  implicit val formPartialRetriever: FormPartialRetriever

  lazy val guardConditionsDeceasedPermanentHome = Set(isThereADateOfDeath)

  lazy val guardConditionsAboutDeceased = Set(isThereADeceasedDomicile)

  lazy val guardConditionsDeceasedLastContactAddressQuestion = Set(isThereADeceasedFirstName)

  lazy val guardConditionsDeceasedLastContactAddress = Set(isDeceasedAddressQuestionAnswered)

  lazy val guardConditionsApplicantApplyingForProbateQuestion = Set(isThereADeceasedAddress)

  lazy val guardConditionsApplicantProbateLocation = Set(isApplicantApplyingForProbateQuestionAnswered)

  lazy val guardConditionsApplicantContactDetails = Set(isThereAnApplicantProbateLocation)

  lazy val guardConditionsApplicantAddress = Set(isThereAnApplicantPhoneNo)

  lazy val guardConditionsCoExecutorOthersApplyingForProbateQuestion = Set(isThereAnApplicantAddress)

  lazy val guardConditionsCoExecutorPersonalDetails = Set(isApplicantOthersApplyingForProbateQuestionAnsweredYes)

  lazy val guardConditionsCoExecutorAddress = Set(isThereACoExecutorWithId, isThereACoExecutorFirstName)

  lazy val guardConditionsRegistrationSummary = Set(isThereADateOfDeath,
    isThereADeceasedFirstName,
    isDeceasedAddressQuestionAnswered,
    isThereADeceasedAddress,
    isApplicantApplyingForProbateQuestionAnswered,
    isThereAnApplicantProbateLocation,
    isThereAnApplicantPhoneNo,
    isThereAnApplicantAddress,
    isApplicantOthersApplyingForProbateQuestionAnswered)

  lazy val regSummaryRoute = routes.RegistrationSummaryController.onPageLoad

  lazy val cancelToRegSummary = Some(regSummaryRoute)

  def guardConditions: Set[Predicate]

  def storeKickoutReasonAndRedirect(kickoutReason: String)(implicit request: Request[_], hc: HeaderCarrier): Future[Result] =
    cachingConnector.storeSingleValue(RegistrationKickOutHelper.RegistrationKickoutReasonCachingKey, kickoutReason) map { _ =>
      Redirect(routes.KickoutRegController.onPageLoad())
    }

  def checkGuardCondition(registrationDetails: RegistrationDetails, id: String): Boolean = {
    guardConditions.takeWhile(func => func.apply(registrationDetails, id)).size == guardConditions.size
  }

  def withRegistrationDetailsRedirectOnGuardCondition(body: RegistrationDetails => Future[Result])
                                                     (implicit request: Request[_], hc: HeaderCarrier): Future[Result] = {
    withRegistrationDetails { rd =>
      val uri = request.uri.split("/")
      val id = if (uri.isEmpty) "" else uri.last
      if (checkGuardCondition(rd, id)) {
        body(rd)
      } else if(!checkGuardCondition(rd, id) && rd.deceasedDateOfDeath.isDefined) {
        Logger.info(s"Registration guard condition not met when ${request.uri} requested so re-directing to estate reports page")
        Future.successful(Redirect(iht.controllers.estateReports.routes.YourEstateReportsController.onPageLoad()))
      } else {
        Logger.info(s"Registration details not found in cache when $uri requested so re-directing to estate reports page")
        Future.successful(Redirect(iht.controllers.estateReports.routes.YourEstateReportsController.onPageLoad()))
      }
    }
  }

  def withRegistrationDetails(body: RegistrationDetails => Future[Result])
                             (implicit request: Request[_], hc: HeaderCarrier): Future[Result] = {
    val futureOptionRD: Future[Option[RegistrationDetails]] = cachingConnector.getRegistrationDetails
    futureOptionRD.flatMap(optionRD => {
      val registrationDetails = optionRD.fold(new RegistrationDetails(None, None, None, Nil, None, "", ""))(identity)
      body(registrationDetails)
    })
  }

  def withRegistrationDetailsOrRedirect(url: String)(body: RegistrationDetails => Future[Result])
                                       (implicit request: Request[_], hc: HeaderCarrier): Future[Result] = {
    cachingConnector.getRegistrationDetails flatMap {
      case None =>
        Logger.info(s"Registration details not found in cache when $url requested so re-directing to application overview page")
        Future.successful(Redirect(iht.controllers.estateReports.routes.YourEstateReportsController.onPageLoad()))
      case Some(rd) =>
        if (rd.ihtReference.isEmpty) {
          Logger.info(s"IHT reference number not found in cache when $url requested so re-directing to application overview page")
          Future.successful(Redirect(iht.controllers.estateReports.routes.YourEstateReportsController.onPageLoad()))
        } else {
          body(rd)
        }
    }
  }

  def storeRegistrationDetails(rd: RegistrationDetails,
                               successRoute: Call,
                               failMessage: String)
                              (implicit hc: HeaderCarrier): Future[Result] = {
    cachingConnector.storeRegistrationDetails(rd).flatMap {
      case Some(_) => Future.successful(Redirect(successRoute))

      case None => {
        Logger.warn(failMessage)
        Future.successful(InternalServerError(failMessage))
      }
    }
  }
}

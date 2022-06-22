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

import iht.connector.CachingConnector
import iht.controllers.auth.IhtBaseController
import iht.models.RegistrationDetails
import iht.utils.AddressHelper
import iht.utils.ApplicantHelper._
import iht.utils.CommonHelper._
import iht.utils.{DeceasedInfoHelper, IhtSection, RegistrationKickOutHelper}
import play.api.i18n.Lang
import play.api.mvc.{AnyContent, Call, Request, Result}
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController

import scala.concurrent.Future


trait RegistrationController extends FrontendController with IhtBaseController with RegistrationKickOutHelper {
  type Predicate = (RegistrationDetails, String) => Boolean
  override lazy val ihtSection = IhtSection.Registration

  def cachingConnector: CachingConnector
  protected def language(implicit request: Request[AnyContent]) = request.acceptLanguages.foldRight(Lang.apply("en"))((_, lang) => lang)
  lazy val guardConditionsDeceasedPermanentHome = Set(DeceasedInfoHelper.isThereADateOfDeath)

  lazy val guardConditionsAboutDeceased = Set(DeceasedInfoHelper.isThereADeceasedDomicile)

  lazy val guardConditionsDeceasedLastContactAddressQuestion = Set(DeceasedInfoHelper.isThereADeceasedFirstName)

  lazy val guardConditionsDeceasedLastContactAddress = Set(DeceasedInfoHelper.isDeceasedAddressQuestionAnswered)

  lazy val guardConditionsApplicantApplyingForProbateQuestion = Set(DeceasedInfoHelper.isThereADeceasedAddress)

  lazy val guardConditionsApplicantExecutorOfEstateQuestion = Set(isApplicantApplyingForProbateQuestionAnswered)

  lazy val guardConditionsApplicantProbateLocation = Set(isApplicantAnExecutorAnswered)

  lazy val guardConditionsApplicantContactDetails = Set(isThereAnApplicantProbateLocation)

  lazy val guardConditionsApplicantAddress = Set(isThereAnApplicantPhoneNo)

  lazy val guardConditionsCoExecutorOthersApplyingForProbateQuestion = Set(AddressHelper.isThereAnApplicantAddress)

  lazy val guardConditionsCoExecutorPersonalDetails = Set(isApplicantOthersApplyingForProbateQuestionAnsweredYes)

  lazy val guardConditionsCoExecutorAddress = Set(isThereACoExecutorWithId, isThereACoExecutorFirstName)

  lazy val guardConditionsRegistrationSummary = Set(DeceasedInfoHelper.isThereADateOfDeath,
    DeceasedInfoHelper.isThereADeceasedFirstName,
    DeceasedInfoHelper.isDeceasedAddressQuestionAnswered,
    DeceasedInfoHelper.isThereADeceasedAddress,
    isApplicantApplyingForProbateQuestionAnswered,
    isThereAnApplicantProbateLocation,
    isThereAnApplicantPhoneNo,
    AddressHelper.isThereAnApplicantAddress,
    isApplicantOthersApplyingForProbateQuestionAnswered)

  lazy val regSummaryRoute = routes.RegistrationSummaryController.onPageLoad

  lazy val cancelToRegSummary = Some(regSummaryRoute)

  def guardConditions: Set[Predicate]

  def storeKickoutReasonAndRedirect(kickoutReason: String)(implicit hc: HeaderCarrier): Future[Result] =
    cachingConnector.storeSingleValue(RegistrationKickoutReasonCachingKey, kickoutReason) map { _ =>
      Redirect(routes.KickoutRegController.onPageLoad)
    }

  def checkGuardCondition(registrationDetails: RegistrationDetails, id: String): Boolean = {
    guardConditions.takeWhile(func => func.apply(registrationDetails, id)).size == guardConditions.size
  }

  def withRegistrationDetailsRedirectOnGuardCondition(body: RegistrationDetails => Future[Result])
                                                     (implicit request: Request[_]): Future[Result] = {
    withRegistrationDetails { rd =>
      val uri = request.uri.split("/")
      val id = if (uri.isEmpty) "" else uri.last
      if (checkGuardCondition(rd, id)) {
        body(rd)
      } else if(!checkGuardCondition(rd, id) && rd.deceasedDateOfDeath.isDefined) {
        logger.info(s"Registration guard condition not met when ${request.uri} requested so re-directing to estate reports page")
        Future.successful(Redirect(iht.controllers.estateReports.routes.YourEstateReportsController.onPageLoad))
      } else {
        logger.info(s"Registration details not found in cache when $uri requested so re-directing to estate reports page")
        Future.successful(Redirect(iht.controllers.estateReports.routes.YourEstateReportsController.onPageLoad))
      }
    }
  }

  def withRegistrationDetails(body: RegistrationDetails => Future[Result])
                             (implicit request: Request[_]): Future[Result] = {
    val futureOptionRD: Future[Option[RegistrationDetails]] = cachingConnector.getRegistrationDetails
    futureOptionRD.flatMap(optionRD => {
      val registrationDetails = optionRD.fold(new RegistrationDetails(None, None, None, Nil, None, "", ""))(identity)
      body(registrationDetails)
    })
  }

  def withRegistrationDetailsOrRedirect(url: String)(body: RegistrationDetails => Future[Result])
                                       (implicit request: Request[_]): Future[Result] = {
    cachingConnector.getRegistrationDetails flatMap {
      case None =>
        logger.info(s"Registration details not found in cache when $url requested so re-directing to application overview page")
        Future.successful(Redirect(iht.controllers.estateReports.routes.YourEstateReportsController.onPageLoad))
      case Some(rd) =>
        if (rd.ihtReference.isEmpty) {
          logger.info(s"IHT reference number not found in cache when $url requested so re-directing to application overview page")
          Future.successful(Redirect(iht.controllers.estateReports.routes.YourEstateReportsController.onPageLoad))
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
        logger.warn(failMessage)
        Future.successful(InternalServerError(failMessage))
      }
    }
  }
}

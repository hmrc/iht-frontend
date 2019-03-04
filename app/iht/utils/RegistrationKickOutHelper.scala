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

package iht.utils

import iht.config.{IhtPropertiesReader => Property}
import iht.connector.CachingConnector
import iht.constants.IhtProperties
import iht.models.{DeceasedDateOfDeath, RegistrationDetails}
import play.api.Logger
import play.api.mvc.Results._
import play.api.mvc.{Call, Request, Result}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import uk.gov.hmrc.http.HeaderCarrier

object RegistrationKickOutHelper {
  lazy val RegistrationKickoutReasonCachingKey = "RegistrationKickoutReason"

  lazy val KickoutDeceasedDateOfDeathDateCapitalTax = "KickoutDeceasedDateOfDeathDateCapitalTax"
  lazy val KickoutDeceasedDateOfDeathDateOther = "KickoutDeceasedDateOfDeathDateOther"

  lazy val KickoutDeceasedDetailsLocationScotland = "KickoutDeceasedDetailsLocationScotland"
  lazy val KickoutDeceasedDetailsLocationNI = "KickoutDeceasedDetailsLocationNI"
  lazy val KickoutDeceasedDetailsLocationOther = "KickoutDeceasedDetailsLocationOther"

  lazy val KickoutApplicantDetailsProbateScotland = "KickoutApplicantDetailsProbateScotland"
  lazy val KickoutApplicantDetailsProbateNi = "KickoutApplicantDetailsProbateNi"

  lazy val KickoutNotApplyingForProbate = "KickoutNotApplyingForProbate"
  lazy val KickoutNotAnExecutor = "KickoutNotAnExecutor"

  def kickoutReasonDeceasedDateOfDeathInternal(deceasedDateOfDeath:DeceasedDateOfDeath): Option[String] =
    deceasedDateOfDeath.dateOfDeath match {
    case x if IhtProperties.dateOfDeathMinValidationDate.compareTo(deceasedDateOfDeath.dateOfDeath) > 0 => Some(KickoutDeceasedDateOfDeathDateCapitalTax)
    case x if IhtProperties.dateOfDeathMaxValidationDate.compareTo(deceasedDateOfDeath.dateOfDeath) > 0 => Some(KickoutDeceasedDateOfDeathDateOther)
    case _ => None
  }

  def kickoutReasonDeceasedDateOfDeath(rd: RegistrationDetails): Option[String] =
    kickoutReasonDeceasedDateOfDeathInternal(rd.deceasedDateOfDeath.get)

  def kickoutReasonDeceasedDetails(rd: RegistrationDetails): Option[String] =
    rd.deceasedDetails.flatMap(_.domicile).flatMap{
      case IhtProperties.domicileEnglandOrWales => None
      case IhtProperties.domicileScotland => Some(KickoutDeceasedDetailsLocationScotland)
      case IhtProperties.domicileNorthernIreland => Some(KickoutDeceasedDetailsLocationNI)
      case _ => Some(KickoutDeceasedDetailsLocationOther)
  }

  def kickoutReasonApplicantDetails(rd: RegistrationDetails): Option[String] = {
    rd.applicantDetails.flatMap(_.country).flatMap{
      case IhtProperties.applicantCountryScotland => Some(RegistrationKickOutHelper.KickoutApplicantDetailsProbateScotland)
      case IhtProperties.applicantCountryNorthernIreland => Some(RegistrationKickOutHelper.KickoutApplicantDetailsProbateNi)
      case _ => None
    }
  }

  def checkNotApplyingForProbateKickout(rd: RegistrationDetails): Option[String] = {
    rd.applicantDetails.flatMap(_.isApplyingForProbate).flatMap{
      case true => None
      case _ => Some(RegistrationKickOutHelper.KickoutNotApplyingForProbate)
    }
  }

  def checkNotAnExecutorKickout(rd: RegistrationDetails): Option[String] = {
    rd.applicantDetails.flatMap(_.isAnExecutor).flatMap{
      case true => None
      case _ => Some(RegistrationKickOutHelper.KickoutNotAnExecutor)
    }
  }

  def noKickoutCheck(rd: RegistrationDetails): Option[String] = None

  def storeAndRedirectWithKickoutCheck(cachingConnector: CachingConnector, rd: RegistrationDetails,
                                       getKickoutReason: RegistrationDetails => Option[String], nextPage: Call,
                                       failMessage: String = "Failed to successfully store registration details")
                                      (implicit request: Request[_], hc: HeaderCarrier): Future[Result] =
    cachingConnector.storeRegistrationDetails(rd).flatMap{
      case Some(_) =>
        getKickoutReason.apply(rd).fold(Future.successful(Redirect(nextPage))) { kickoutReason =>
          cachingConnector.storeSingleValue(RegistrationKickoutReasonCachingKey, kickoutReason).flatMap(_ =>
            Future.successful(Redirect(iht.controllers.registration.routes.KickoutRegController.onPageLoad())))}

        case None =>
          Logger.warn(failMessage)
          Future.successful(InternalServerError(failMessage))
      }

}

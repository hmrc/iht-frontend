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

package utils

import iht.constants.Constants
import iht.models.RegistrationDetails
import iht.models.application.{ApplicationDetails, ProbateDetails}
import iht.utils.ApplicationStatus
import play.api.http.HeaderNames
import play.api.mvc.AnyContentAsEmpty
import play.api.test.FakeRequest
import uk.gov.hmrc.auth.core.authorise.Predicate
import uk.gov.hmrc.auth.core.retrieve.Retrieval
import uk.gov.hmrc.auth.core.retrieve.v2.Retrievals
import uk.gov.hmrc.auth.core.{AuthConnector, PlayAuthConnector}
import uk.gov.hmrc.http.{HeaderCarrier, SessionKeys}

import scala.concurrent.{ExecutionContext, Future}

trait TestDataUtil {

  def createFakeAuthConnector(isAuthorised: Boolean = true, defaultAuthNino: String = "AA123456A"): AuthConnector = new PlayAuthConnector {
    override val serviceUrl: String = null
    override lazy val http = null

    override def authorise[A](predicate: Predicate, retrieval: Retrieval[A])(implicit hc: HeaderCarrier, ec: ExecutionContext): Future[A] = {
      retrieval match {
        case Retrievals.nino => Future.successful(Some(defaultAuthNino).asInstanceOf[A])
        case _ => super.authorise(predicate, retrieval)
      }
    }
  }

  val testApplicationDetails = ApplicationDetails(allAssets = None,
    propertyList = Nil,
    allLiabilities = None,
    allExemptions = None,
    allGifts = None,
    giftsList = None,
    charities = Seq(),
    qualifyingBodies = Seq(),
    widowCheck = None,
    increaseIhtThreshold = None,
    status = ApplicationStatus.InProgress,
    kickoutReason = None,
    ihtRef = None,
    reasonForBeingBelowLimit = None)

  val testSaveApplicationDetails = ApplicationDetails(allAssets = None,
    propertyList = Nil,
    allLiabilities = None,
    allExemptions = None,
    allGifts = None,
    giftsList = None,
    charities = Seq(),
    qualifyingBodies = Seq(),
    widowCheck = None,
    increaseIhtThreshold = None,
    status = ApplicationStatus.InProgress,
    kickoutReason = None,
    ihtRef = None,
    reasonForBeingBelowLimit = Some("Excepted Estate"))

  val testRegistrationDetails = RegistrationDetails(
    deceasedDateOfDeath = None,
    applicantDetails = None,
    deceasedDetails = None,
    coExecutors = Seq(),
    ihtReference = Some("ABC1234567890"),
    acknowledgmentReference = "AAABBBCCC",
    returns = Seq(),
    areOthersApplyingForProbate = None
  )

  val testProbateDetails = ProbateDetails(
    grossEstateforIHTPurposes = 0,
    grossEstateforProbatePurposes = 0,
    totalDeductionsForProbatePurposes = 0,
    netEstateForProbatePurposes = 0,
    valueOfEstateOutsideOfTheUK = 0,
    valueOfTaxPaid = 0,
    probateReference = "XX123456789X"
  )

  def createFakeRequest(isAuthorised: Boolean = true, referer: Option[String] = None): FakeRequest[AnyContentAsEmpty.type] = {
    val userId = "ID-" + "AA123456A"
    if (isAuthorised) {
      FakeRequest().withSession(
        Constants.NINO -> "AA123456A",
        SessionKeys.sessionId -> s"session-$userId",
        "token" -> "some-gg-token").withHeaders(
        "Accept-Language" -> "en-GB"
      )
    } else {
      FakeRequest().withHeaders(
        "Accept-Language" -> "en-GB",
        HeaderNames.REFERER -> referer.getOrElse("")
      )
    }
  }
}

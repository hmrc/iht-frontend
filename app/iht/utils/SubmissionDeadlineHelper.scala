/*
 * Copyright 2020 HM Revenue & Customs
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

import iht.connector.IhtConnector
import iht.models.application.IhtApplication
import org.joda.time.LocalDate

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import uk.gov.hmrc.http.HeaderCarrier

object SubmissionDeadlineHelper {
  def apply(nino: String, ihtReference: String, ihtConnector: IhtConnector, headerCarrier: HeaderCarrier): Future[LocalDate] = {

    for{
      cases <- ihtConnector.getCaseList(nino)(headerCarrier)
    }yield{
      val theCase: Seq[IhtApplication] = cases.filter(_.ihtRefNo == ihtReference)
      if (theCase.isEmpty) {
        throw new RuntimeException(s"Reference $ihtReference does not exist.")
      }
      val regDate: LocalDate = theCase.head.registrationDate
      regDate.plusYears(1).plusMonths(1).minusDays(1)
    }
  }
}

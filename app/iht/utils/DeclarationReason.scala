/*
 * Copyright 2018 HM Revenue & Customs
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

/**
 *
 * Created by Vineet Tyagi on 22/09/15.
 *
 * Contains the Declaration reasons
 */
object DeclarationReason {

  // Declarations
  val ValueLessThanNilRateBand = "valueLessThanNilRateBand"
  val ValueLessThanNilRateBandAfterExemption = "valueLessThanNilRateBandAfterExemption"
  val ValueLessThanTransferredNilRateBand = "valueLessThanTransferredNilRateBand"
  val ValueLessThanTransferredNilRateBandAfterExemption = "valueLessThanTransferredNilRateBandAfterExemption"

  val ExemptionEstateLessThanThreshold = "Estate value is less than Threshold value"
  val TnrbEstateLessThanThreshold = "Estate value is more than Threshold value"
  val TnrbEstateLessThanThresholdWithNilExemption = "Estate value is less than threshold with nil Exemption"
}

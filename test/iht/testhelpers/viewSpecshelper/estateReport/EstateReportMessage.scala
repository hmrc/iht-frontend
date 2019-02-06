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

package iht.testhelpers.viewSpecshelper.estateReport

trait EstateReportMessage {

  val estateReportMessageTitle = "Help with your estate reports"

  val estateReportPersonalDetailsErrorTitle = "If you made errors in personal details"

  val estateReportPersonalDetailsSubmittedErrorP1 = "You do not need to inform HMRC of any changes or errors in names, numbers, addresses or dates, or the removal or addition of executors."
  val estateReportPersonalDetailsSubmittedErrorP2 = "You must ensure the executors are listed correctly on the probate application."
  val estateReportPersonalDetailsSubmittedErrorP3 = "If you personally need an estate report with amended personal details you will need to register the estate again."

  val estateReportPersonalDetailsErrorP1 = "Once your report is in review or closed, you cannot re-open it or start a new one."
  val estateReportPersonalDetailsErrorP2 = "You do not need to inform HMRC of any changes or errors in names, numbers, addresses or dates, or the removal or addition of executors."
  val estateReportPersonalDetailsErrorP3 = "You must ensure the executors are listed correctly on the probate application."

  val estateReportEstateValueErrorTitle = "If you made errors in estate value"
  val estateReportEstateValueErrorP1 = "You do not need to report any changes in estate value to HMRC unless you think you might need to submit a full return (IHT400). If a full return is needed and you do not make one, you may get a penalty."
  val estateReportEstateValueErrorLink = "Check if the estate needs a full return."
  val estateReportEstateValueErrorP2 = "You must in all cases report the amended estate value (gross and net) to the Probate Service until the time you get the Grant of Probate."

}

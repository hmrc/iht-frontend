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

/**
 *
 * Created by Vineet Tyagi on 22/09/15.
 *
 * Contains the various Application status values. This status is actually two
 * different types of status: the etmp status and the front end status.
 */
object ApplicationStatus {

  val AwaitingReturn="Awaiting Return"
  val NotStarted="Not Started"
  val InProgress="In Progress"
  val InReview="In Review"
  val Closed="Closed"
  val KickOut="Kick Out"
  val ClearanceGranted = "Clearance Granted"
  val UnderEnquiry = "Under Enquiry"
  val IneligibleApplication = "Ineligible Application"
}

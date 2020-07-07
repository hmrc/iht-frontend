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

import iht.config.AppConfig
import iht.models._
import iht.models.application.ApplicationDetails

import scala.collection.immutable.ListMap

case class RegistrationDetailsHelperFixture(x: Any = None)(implicit val appConfig: AppConfig) extends RegistrationDetailsHelper

trait RegistrationDetailsHelper {
  implicit val appConfig: AppConfig

  /**
    * Iterates through ListMap of ApplicationDetails->Boolean functions, executing each one in turn, passing in the
    * specified ApplicationDetails object, until one yields true, at which point the String key associated with
    * the function is returned as an Option. If all functions yield false then a None is returned.
    */
  def findFirstTrue(registrationDetails: RegistrationDetails,
                    applicationDetails: ApplicationDetails,
                    sectionTotal: => Seq[BigDecimal],
                    keysToFunctions: ListMap[String, (RegistrationDetails,
                      ApplicationDetails, Seq[BigDecimal]) => Boolean]): Option[String] = {

    val passedItems = keysToFunctions.keys.iterator.takeWhile(key =>
      !keysToFunctions(key)(registrationDetails, applicationDetails, sectionTotal))

    val passedItemsSize = passedItems.size

    if (passedItemsSize >= keysToFunctions.keys.iterator.size) {
      // If they've all yielded false
      None
    } else {
      val remainingItems = keysToFunctions.keys.iterator.drop(passedItemsSize)
      Some(remainingItems.next())
    }
  }

  def getOrExceptionNoRegistration(option: Option[RegistrationDetails]): RegistrationDetails = CommonHelper.getOrException(option, "No registration details")

  /**
    * Checks the Exemptions Completion based on marital status
    *
    * @param rd : RegistrationDetails
    * @param ad : ApplicationDetails
    * @return
    */
  def isExemptionsCompleted(rd: RegistrationDetails, ad: ApplicationDetails): Boolean = {
    def isExemptionsCompleted(ad:ApplicationDetails) = ad.isCompleteCharities.getOrElse(false) &&
      ad.isCompleteQualifyingBodies.getOrElse(false) &&
      ad.allExemptions.flatMap(_.partner.flatMap(_.isComplete)).getOrElse(false)

    def isExemptionsCompletedWithoutPartnerExemption(ad:ApplicationDetails) =
      (ad.isCompleteCharities, ad.isCompleteQualifyingBodies) match {
        case (Some(true), Some(true)) => true
        case _ => false
      }

    if (!rd.deceasedDetails.flatMap(_.maritalStatus).contains(appConfig.statusMarried)) {
      isExemptionsCompletedWithoutPartnerExemption(ad)
    } else {
      isExemptionsCompleted(ad)
    }
  }

  /**
    * Fetch the deceased Marital status if DeceasedDetails and MaritalStatus exists else throws exception
    *
    * @param regDetails
    * @return
    */
  def getMaritalStatus(regDetails: RegistrationDetails) =
    CommonHelper.getOrException(CommonHelper.getOrException(regDetails.deceasedDetails).maritalStatus)
}
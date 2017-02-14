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

package iht.views.application

import iht.models.application.basicElements.ShareableBasicEstateElement

/**
  * Created by vineet on 14/02/17.
  */
trait SharableOverviewData {

  val dataWithQuestionsAnsweredNo =
    Some(ShareableBasicEstateElement(value = None, shareValue = None, isOwned = Some(false), isOwnedShare = Some(false)))

  val dataWithQuestionsAnsweredYes =
    Some(ShareableBasicEstateElement(value = None, shareValue = None, isOwned = Some(true), isOwnedShare = Some(true)))

  val ownedAmount = 1234.0
  val ownedAmountDisplay = "£1,234.00"
  val jointAmount = 2345.0
  val jointAmountDisplay = "£2,345.00"

  val dataWithValues =
    Some(ShareableBasicEstateElement(value = Some(ownedAmount), shareValue = Some(jointAmount),
      isOwned = Some(true), isOwnedShare = Some(true)))

}

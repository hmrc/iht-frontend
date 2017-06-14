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

package iht.utils

import iht.FakeIhtApp
import iht.constants.FieldMappings
import org.scalatest.mock.MockitoSugar
import play.api.i18n.{MessagesApi, Messages}
import uk.gov.hmrc.play.test.UnitSpec

class FieldMappingsTest extends UnitSpec with FakeIhtApp with MockitoSugar {

  "FieldMappings" must {
    "create type of ownership with correct data" in {
      implicit val request = createFakeRequest()
      implicit val messagesApi: MessagesApi = app.injector.instanceOf[MessagesApi]
      def messages = messagesApi.preferred(request)

      val deceasedName = "John"

      val ownership = FieldMappings.typesOfOwnership(deceasedName)(messages)
      val deceasedOnlyLabel = ownership.get("Deceased only").fold("")( _._1)
      val deceasedOnlyLabelHint = ownership.get("Deceased only").fold(Some(""))( _._2)
      val jointOwnershipLabel = ownership.get("Joint").fold("")( _._1)
      val jointOwnershipHint = ownership.get("Joint").fold(Some(""))( _._2)
      val commonOwnershipLabel = ownership.get("In common").fold("")( _._1)
      val commonOwnershipHint = ownership.get("In common").fold(Some(""))( _._2)

      deceasedOnlyLabel shouldBe messagesApi("page.iht.application.assets.typeOfOwnership.deceasedOnly.label", deceasedName)
      jointOwnershipLabel shouldBe messagesApi("page.iht.application.assets.typeOfOwnership.joint.label")
      commonOwnershipLabel shouldBe messagesApi("page.iht.application.assets.typeOfOwnership.inCommon.label")
      deceasedOnlyLabelHint shouldBe Some(messagesApi("page.iht.application.assets.typeOfOwnership.deceasedOnly.hint", deceasedName))
      jointOwnershipHint shouldBe Some(messagesApi("page.iht.application.assets.typeOfOwnership.joint.hint", deceasedName))
      commonOwnershipHint shouldBe Some(messagesApi("page.iht.application.assets.typeOfOwnership.inCommon.hint", deceasedName))
    }

    "create tenures with correct data" in {
      implicit val request = createFakeRequest()
      implicit val messagesApi: MessagesApi = app.injector.instanceOf[MessagesApi]
      def messages = messagesApi.preferred(request)

      val deceasedName = "John"

      val tenures = FieldMappings.tenures(deceasedName)(messages)
      val freeHoldLabelHint = tenures.get("Freehold").fold(Some(""))( _._2)
      val leaseHoldLabelHint = tenures.get("Leasehold").fold(Some(""))( _._2)

      freeHoldLabelHint shouldBe Some(messagesApi("page.iht.application.assets.tenure.freehold.hint", deceasedName))
      leaseHoldLabelHint shouldBe Some(messagesApi("page.iht.application.assets.tenure.leasehold.hint", deceasedName))
    }
  }
}

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

import iht.FakeIhtApp
import iht.config.AppConfig
import iht.constants.FieldMappings
import org.scalatestplus.mockito.MockitoSugar
import play.api.i18n.{Lang, MessagesApi}

class FieldMappingsTest extends FakeIhtApp with MockitoSugar {

  implicit val lang: Lang = Lang.defaultLang
  val mockAppConfig: AppConfig = app.injector.instanceOf[AppConfig]

  "FieldMappings" must {
    "create type of ownership with correct data" in {
      implicit val request = createFakeRequest()
      implicit val messagesApi: MessagesApi = app.injector.instanceOf[MessagesApi]
      def messages = messagesApi.preferred(request)

      val deceasedName = "John"

      val ownership = FieldMappings.typesOfOwnership(deceasedName)(messages, mockAppConfig)
      val deceasedOnlyLabel = ownership.get("Deceased only").fold("")( _._1)
      val deceasedOnlyLabelHint = ownership.get("Deceased only").fold(Some(""))( _._2)
      val jointOwnershipLabel = ownership.get("Joint").fold("")( _._1)
      val jointOwnershipHint = ownership.get("Joint").fold(Some(""))( _._2)
      val commonOwnershipLabel = ownership.get("In common").fold("")( _._1)
      val commonOwnershipHint = ownership.get("In common").fold(Some(""))( _._2)

      deceasedOnlyLabel mustBe messagesApi("page.iht.application.assets.typeOfOwnership.deceasedOnly.label", deceasedName)
      jointOwnershipLabel mustBe messagesApi("page.iht.application.assets.typeOfOwnership.joint.label")
      commonOwnershipLabel mustBe messagesApi("page.iht.application.assets.typeOfOwnership.inCommon.label")
      deceasedOnlyLabelHint mustBe Some(messagesApi("page.iht.application.assets.typeOfOwnership.deceasedOnly.hint", deceasedName))
      jointOwnershipHint mustBe Some(messagesApi("page.iht.application.assets.typeOfOwnership.joint.hint", deceasedName))
      commonOwnershipHint mustBe Some(messagesApi("page.iht.application.assets.typeOfOwnership.inCommon.hint", deceasedName))
    }

    "create tenures with correct data" in {
      implicit val request = createFakeRequest()
      implicit val messagesApi: MessagesApi = app.injector.instanceOf[MessagesApi]
      def messages = messagesApi.preferred(request)

      val deceasedName = "John"

      val tenures = FieldMappings.tenures(deceasedName)(messages, mockAppConfig)
      val freeHoldLabelHint = tenures.get("Freehold").fold(Some(""))( _._2)
      val leaseHoldLabelHint = tenures.get("Leasehold").fold(Some(""))( _._2)

      freeHoldLabelHint mustBe Some(messagesApi("page.iht.application.assets.tenure.freehold.hint", deceasedName))
      leaseHoldLabelHint mustBe Some(messagesApi("page.iht.application.assets.tenure.leasehold.hint", deceasedName))
    }
  }
}

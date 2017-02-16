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

package iht.views.application.assets.properties

import iht.models.UkAddress
import iht.models.application.assets.Properties
import iht.testhelpers.CommonBuilder
import iht.views.html.application.asset.properties.{properties_overview, property_details_overview}
import iht.views.{ExitComponent, GenericNonSubmittablePageBehaviour}
import play.api.i18n.Messages
import play.api.mvc.AnyContentAsEmpty
import play.api.test.FakeRequest

class PropertyDetailsOverviewViewTest extends GenericNonSubmittablePageBehaviour {
  implicit def request: FakeRequest[AnyContentAsEmpty.type] = createFakeRequest()

  def registrationDetails = CommonBuilder.buildRegistrationDetails1

  def deceasedName = registrationDetails.deceasedDetails.map(_.name).fold("")(identity)

  override def guidanceParagraphs = Set(
    Messages("page.iht.application.assets.property.detailsOverview.mortgage.text")
  )

  override def pageTitle = Messages("iht.estateReport.assets.propertyAdd")

  override def browserTitle = Messages("iht.estateReport.assets.propertyAdd")

  override val exitId: String = "return-to-properties-link"

  override def exitComponent = Some(
    ExitComponent(
      iht.controllers.application.assets.properties.routes.PropertiesOverviewController.onPageLoad(),
      Messages("page.iht.application.assets.property.detailsOverview.returnLink")
    )
  )

  override def view =
    property_details_overview(deceasedName, Some(CommonBuilder.property)).toString()

  "Property details overview view" must {
    behave like nonSubmittablePage()
  }
}

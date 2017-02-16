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

import iht.models.application.assets.Properties
import iht.testhelpers.CommonBuilder
import iht.views.{ExitComponent, GenericNonSubmittablePageBehaviour}
import iht.views.html.application.asset.properties.{properties_overview, property_details_overview}
import play.api.i18n.Messages
import play.api.mvc.AnyContentAsEmpty
import play.api.test.FakeRequest

class PropertiesOverviewViewTest extends GenericNonSubmittablePageBehaviour {
  implicit def request: FakeRequest[AnyContentAsEmpty.type] = createFakeRequest()

  def registrationDetails = CommonBuilder.buildRegistrationDetails1

  def deceasedName = registrationDetails.deceasedDetails.map(_.name).fold("")(identity)

  override def guidanceParagraphs = Set(
    Messages("page.iht.application.assets.deceased-permanent-home.description.p1", deceasedName),
    Messages("page.iht.application.assets.deceased-permanent-home.description.p2",deceasedName)
  )

  override def pageTitle = Messages("page.iht.application.assets.deceased-permanent-home.sectionTitle")

  override def browserTitle = Messages("iht.estateReport.assets.propertiesBuildingsAndLand")

  override def exitComponent = Some(
    ExitComponent(
      iht.controllers.application.assets.routes.AssetsOverviewController.onPageLoad(),
      Messages("site.link.return.assets")
    )
  )

  override val exitId: String = "return-button"

  override def view =
    properties_overview(List(CommonBuilder.property, CommonBuilder.property2),
      Some(Properties(isOwned = Some(true))),
      registrationDetails).toString()

  "Property Details overview view" must {

    behave like nonSubmittablePage()

    //    "have correct questions" in {
    //      val view = propertyDetailsOverviewView()
    //      messagesShouldBePresent(view.toString, Messages("iht.estateReport.exemptions.propertyDetails.assetLeftToCharity.question",
    //                                                      CommonHelper.getDeceasedNameOrDefaultString(registrationDetails)))
    //    }
    //
    //    "have Add a charity link with correct target" in {
    //      val view = propertyDetailsOverviewView()
    //
    //      val returnLink = view.getElementById("add-charity")
    //      returnLink.attr("href") shouldBe charityDetailsPageUrl.url
    //      returnLink.text() shouldBe Messages("page.iht.application.exemptions.assetLeftToCharity.addCharity")
    //    }
    //
    //    "show no propertyDetails added message when the is no charity present" in {
    //      val view = propertyDetailsOverviewView()
    //
    //      messagesShouldBePresent(view.toString, Messages("page.iht.application.exemptions.charityOverview.noCharities.text"))
    //    }
    //
    //    "have the return link with correct text" in {
    //      val view = propertyDetailsOverviewView()
    //
    //      val returnLink = view.getElementById("return-button")
    //      returnLink.attr("href") shouldBe exemptionsOverviewPageUrl.url
    //      returnLink.text() shouldBe Messages("page.iht.application.return.to.exemptionsOf",
    //                                          CommonHelper.getOrException(registrationDetails.deceasedDetails.map(_.name)))
    //    }

  }

}

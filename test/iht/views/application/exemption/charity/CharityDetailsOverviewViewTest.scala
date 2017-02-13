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

package iht.views.application.exemption.charity

import iht.controllers.application.exemptions.charity.routes
import iht.views.HtmlSpec
import iht.views.html.application.exemption.charity.charity_details_overview
import iht.{FakeIhtApp, TestUtils}
import org.jsoup.nodes.Element
import org.scalatest.BeforeAndAfter
import org.scalatest.mock.MockitoSugar
import play.api.i18n.Messages
import uk.gov.hmrc.play.test.UnitSpec

/**
 * Created by jennygj on 13/10/16.
 */
class CharityDetailsOverviewViewTest extends UnitSpec with FakeIhtApp with MockitoSugar with TestUtils with HtmlSpec with BeforeAndAfter{

  "CharityDetailsOverviewView" must {

    "contain correct links and show correct link texts for Charity name, number and value " +
      "when charily details has not been entered" in {
      implicit val request = createFakeRequest()

      val charity = iht.testhelpers.CommonBuilder.charity.copy(number = None, name = None, totalValue = None)
      val view = charity_details_overview(Some(charity)).toString
      val doc = asDocument(view)

      val nameLink: Element = doc.getElementById("charity-name-link")
      val expectedNameUrl =  routes.CharityNameController.onEditPageLoad(charity.id.getOrElse(""))
      nameLink.attr("href") shouldBe expectedNameUrl.url
      assertEqualsValue(doc, "a#charity-name-link span", Messages("site.link.giveName"))

      val numberLink: Element = doc.getElementById("charity-number-link")
      val expectedNumberUrl =  routes.CharityNumberController.onEditPageLoad(charity.id.getOrElse(""))
      numberLink.attr("href") shouldBe expectedNumberUrl.url
      assertEqualsValue(doc, "a#charity-number-link span", Messages("site.link.giveNumber"))

      val valueLink: Element = doc.getElementById("charity-value-link")
      val expectedValueURl = routes.CharityValueController.onEditPageLoad(charity.id.getOrElse(""))
      valueLink.attr("href") shouldBe expectedValueURl.url
      assertEqualsValue(doc, "a#charity-value-link span", Messages("site.link.giveValue"))
    }

    "contain correct links and show correct link texts for Charity name, number and value " +
      "when charily details have been entered" in {
      implicit val request = createFakeRequest()

      val charity = iht.testhelpers.CommonBuilder.charity
      val view = charity_details_overview(Some(charity)).toString
      val doc = asDocument(view)

      val nameLink: Element = doc.getElementById("charity-name-link")
      val expectedNameUrl =  routes.CharityNameController.onEditPageLoad(charity.id.getOrElse(""))
      nameLink.attr("href") shouldBe expectedNameUrl.url
      assertEqualsValue(doc, "a#charity-name-link span", Messages("iht.change"))

      val numberLink: Element = doc.getElementById("charity-number-link")
      val expectedNumberUrl =  routes.CharityNumberController.onEditPageLoad(charity.id.getOrElse(""))
      numberLink.attr("href") shouldBe expectedNumberUrl.url
      assertEqualsValue(doc, "a#charity-number-link span", Messages("iht.change"))

      val valueLink: Element = doc.getElementById("charity-value-link")
      val expectedValueURl = routes.CharityValueController.onEditPageLoad(charity.id.getOrElse(""))
      valueLink.attr("href") shouldBe expectedValueURl.url
      assertEqualsValue(doc, "a#charity-value-link span", Messages("iht.change"))
    }

    "contain links with charity ID #1 when charity name is completed, but charity number and value are empty" +
      "and page is visited to amend charity ID 1" in {
      implicit val request = createFakeRequest()

      val charity = iht.testhelpers.CommonBuilder.charity.copy(number=None,totalValue=None)
      val view = charity_details_overview(Some(charity)).toString
      val doc = asDocument(view)
      val numberLink: Element = doc.getElementById("charity-number-link")
      val valueLink: Element = doc.getElementById("charity-value-link")
      val expectedNumberUrl =  "/inheritance-tax/estate-report/charity-number/1"
      val expectedValueURl = "/inheritance-tax/estate-report/assets-value-left-to-charity/1"

      numberLink.attr("href") shouldBe expectedNumberUrl
      valueLink.attr("href") shouldBe expectedValueURl
    }

    "contain links with charity ID 2 when charity number is completed, but charity name and value are empty " +
      "and page is visited to amend charity ID 2" in {
      implicit val request = createFakeRequest()

      val charity = iht.testhelpers.CommonBuilder.charity.copy(id=Some("2"), name=None, totalValue=None)
      val view = charity_details_overview(Some(charity)).toString
      val doc = asDocument(view)
      val nameLink: Element = doc.getElementById("charity-name-link")
      val valueLink: Element = doc.getElementById("charity-value-link")
      val expectedNameUrl =  "/inheritance-tax/estate-report/charity-name/2"
      val expectedValueURl = "/inheritance-tax/estate-report/assets-value-left-to-charity/2"

      nameLink.attr("href") shouldBe expectedNameUrl
      valueLink.attr("href") shouldBe expectedValueURl
    }

    "contain links with charity ID 3 when all charity values have been completed" +
      "and page is visited to amend charity ID 3" in {
      implicit val request = createFakeRequest()

      val charity = iht.testhelpers.CommonBuilder.charity.copy(id=Some("3"))
      val view = charity_details_overview(Some(charity)).toString
      val doc = asDocument(view)
      val nameLink: Element = doc.getElementById("charity-name-link")
      val numberLink: Element = doc.getElementById("charity-number-link")
      val valueLink: Element = doc.getElementById("charity-value-link")
      val expectedNameUrl =  "/inheritance-tax/estate-report/charity-name/3"
      val expectedNumberUrl =  "/inheritance-tax/estate-report/charity-number/3"
      val expectedValueURl = "/inheritance-tax/estate-report/assets-value-left-to-charity/3"

      nameLink.attr("href") shouldBe expectedNameUrl
      numberLink.attr("href") shouldBe expectedNumberUrl
      valueLink.attr("href") shouldBe expectedValueURl
    }


    "contain a links without charity ID when the page is visited to add a completely new charity" in {
      implicit val request = createFakeRequest()

      val view = charity_details_overview().toString
      val doc = asDocument(view)
      val numberLink: Element = doc.getElementById("charity-number-link")
      val nameLink: Element = doc.getElementById("charity-name-link")
      val valueLink: Element = doc.getElementById("charity-value-link")
      val expectedNumberUrl =  "/inheritance-tax/estate-report/charity-number"
      val expectedNameUrl =  "/inheritance-tax/estate-report/charity-name"
      val expectedValueURl = "/inheritance-tax/estate-report/assets-value-left-to-charity"

      nameLink.attr("href") shouldBe expectedNameUrl
      numberLink.attr("href") shouldBe expectedNumberUrl
      valueLink.attr("href") shouldBe expectedValueURl
    }
  }

}

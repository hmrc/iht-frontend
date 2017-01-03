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

package iht.views.application.exemption.qualifyingBody

import iht.forms.ApplicationForms._
import iht.testhelpers.CommonBuilder
import iht.views.HtmlSpec
import iht.views.html.application.exemption.qualifyingBody.assets_left_to_qualifying_body_question
import iht.{FakeIhtApp, TestUtils}
import org.scalatest.BeforeAndAfter
import org.scalatest.mock.MockitoSugar
import play.api.i18n.Messages
import uk.gov.hmrc.play.test.UnitSpec

/**
 * Created by vineet on 29/11/16.
 */
class AssetsLeftToQualifyingBodyQuestionViewTest extends UnitSpec with FakeIhtApp with MockitoSugar with TestUtils with HtmlSpec with BeforeAndAfter{

  val regDetails = CommonBuilder.buildRegistrationDetails1

  "AssetsLeftToQualifyingBodyQuestionView" must {

    "contain correct question, guidance and links with correct text  " in {
      implicit val request = createFakeRequest()

      val view = assets_left_to_qualifying_body_question(assetsLeftToQualifyingBodyQuestionForm, regDetails).toString
      val doc = asDocument(view)

      view should include (Messages("page.iht.application.exemptions.assetsLeftToQualifyingBody.sectionTitle"))
      view should include (Messages("page.iht.application.exemptions.assetsLeftToQualifyingBody.p1"))
      view should include (Messages("page.iht.application.exemptions.assetsLeftToQualifyingBody.p2"))
      view should include (Messages("iht.estateReport.exemptions.qualifyingBodies.assetsLeftToQualifyingBody.p3"))

      assertRenderedById(doc, "save-continue")
      assertEqualsValue(doc, "button#save-continue", Messages("iht.saveAndContinue"))

      val cancelLink = doc.getElementById("cancel-button")
      cancelLink.text shouldBe
        Messages("page.iht.application.return.to.exemptionsOf", regDetails.deceasedDetails.map(_.name).fold("")(identity))
      cancelLink.attr("href") shouldBe
        iht.controllers.application.exemptions.routes.ExemptionsOverviewController.onPageLoad.url

    }
  }

}

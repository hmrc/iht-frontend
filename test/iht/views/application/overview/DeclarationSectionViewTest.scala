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

package iht.views.application.overview

import iht.viewmodels.application.overview._
import iht.views.HtmlSpec
import iht.views.html.application.overview.declaration_section
import iht.{FakeIhtApp, TestUtils}
import org.scalatest.mock.MockitoSugar
import play.api.i18n.{I18nSupport, Messages, MessagesApi}
import play.api.i18n.Messages.Implicits._
import play.api.Play.current
import uk.gov.hmrc.play.test.UnitSpec

class DeclarationSectionViewTest extends UnitSpec with FakeIhtApp with MockitoSugar with TestUtils with HtmlSpec {
  implicit val messagesApi: MessagesApi = app.injector.instanceOf[MessagesApi]
  val ihtRef = "ABC123"

  val declarationSectionViewModel = DeclarationSectionViewModel(
    ihtReference = ihtRef,
    declarationSectionStatus = Declarable)


  "declarationSection " must {

    "contain the incomplete guidance text when Declaration status is InComplete" in {
      implicit val request = createFakeRequest()

      val view = declaration_section(declarationSectionViewModel.copy(declarationSectionStatus = InComplete)).toString
      val doc = asDocument(view)

      assertEqualsValue(doc, "p#all-sections-not-complete-declaration-guidance-text1 strong",
        Messages("page.iht.application.estateOverview.declaration.allSectionsNotComplete.guidance.text1"))

      assertEqualsValue(doc, "p#all-sections-not-complete-declaration-guidance-text2",
        Messages("page.iht.application.estateOverview.declaration.allSectionsNotComplete.guidance.text2"))
    }

    "contain the NotDeclarable guidance text when Declaration status is NotDeclarable" in {
      implicit val request = createFakeRequest()

      val view = declaration_section(declarationSectionViewModel.copy(declarationSectionStatus = NotDeclarable)).toString
      val doc = asDocument(view)

      assertEqualsValue(doc, "p#not-declarable-guidance",
        Messages("page.iht.application.estateOverview.declaration.continue.guidance.text"))

    }

    "contain the Declarable guidance text when Declaration status is Declarable" in {
      implicit val request = createFakeRequest()

      val view = declaration_section(declarationSectionViewModel).toString
      val doc = asDocument(view)

      assertEqualsValue(doc, "p#declarable-guidance",
        Messages("page.iht.application.estateOverview.declaration.allSectionsComplete.guidance.text"))

    }

    "contain the Continue button when Declaration status is NotDeclarable" in {
      implicit val request = createFakeRequest()

      val view = declaration_section(declarationSectionViewModel.copy(declarationSectionStatus = NotDeclarable)).toString
      val doc = asDocument(view)

      val link = doc.getElementById("continue")
      link.text shouldBe Messages("iht.continue")
      link.attr("href") shouldBe
        iht.controllers.application.routes.EstateOverviewController.onContinueOrDeclarationRedirect(ihtRef).url
    }

    "contain the Continue to declaration button when Declaration status is Declarable" in {
      implicit val request = createFakeRequest()

      val view = declaration_section(declarationSectionViewModel.copy(declarationSectionStatus = Declarable)).toString
      val doc = asDocument(view)

      val link = doc.getElementById("continue-to-declaration")
      link.text shouldBe Messages("iht.continue")
      link.attr("href") shouldBe
        iht.controllers.application.routes.EstateOverviewController.onContinueOrDeclarationRedirect(ihtRef).url
    }
  }
}

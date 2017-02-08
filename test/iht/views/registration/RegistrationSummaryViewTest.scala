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

package iht.views.registration

import iht.models.RegistrationDetails
import iht.testhelpers.{CommonBuilder, TestHelper}
import iht.views.ViewTestHelper
import iht.views.html.registration.registration_summary
import org.jsoup.nodes.Element
import org.jsoup.select.Elements
import play.api.i18n.Messages
import play.api.mvc.AnyContentAsEmpty
import play.api.test.FakeRequest
import scala.collection.JavaConversions._
import scala.util.{Failure, Success, Try}

case class SharableOverviewRow(rowText: String = "", value: String = "", linkText: String = "")

object SharableOverviewRow {

  def apply(element: Element): SharableOverviewRow = {
    val cells = element.select("div:not(.visually-hidden)")
    val row = cells.size match {
      case 2 => SharableOverviewRow(
        rowText = cells.get(0).text,
        linkText = cells.get(1).text
      )
      case 3 => SharableOverviewRow(
        rowText = cells.get(0).text,
        value = cells.get(1).text,
        linkText = getLinkText(cells.get(2).getElementsByTag("a").first)
      )
      case 4 => SharableOverviewRow(
        rowText = cells.get(0).text,
        value = cells.get(1).text,
        linkText = getLinkText(cells.get(3).getElementsByTag("a").first)
      )
    }

    row
  }

  def getLinkText(link: Element) = {
    val internalSpansTry = Try(link.getElementsByTag("span"))
    internalSpansTry match {
      case Success(internalSpans) =>
        internalSpans.get(0).text
      case Failure(_) =>
        Try(link.text) match {
          case Success(lt) => lt
          case Failure(_) => ""
        }

    }
  }
}

class RegistrationSummaryViewTest extends ViewTestHelper {
  implicit def request: FakeRequest[AnyContentAsEmpty.type] = createFakeRequest()

  def registrationDetails = {
    val coExecutor1 = CommonBuilder.buildCoExecutor.copy(firstName = "Coexec1firstname", lastName = "Coexec1lastname")
    val coExecutor2 = CommonBuilder.buildCoExecutor.copy(firstName = "Coexec2firstname", lastName = "Coexec2lastname")
    val coExecutor3 = CommonBuilder.buildCoExecutor.copy(firstName = "Coexec3firstname", lastName = "Coexec3lastname")

    RegistrationDetails(
      deceasedDateOfDeath = Some(CommonBuilder.buildDeceasedDateOfDeath),
      applicantDetails = Some(CommonBuilder.buildApplicantDetails copy (
        country = Some(TestHelper.ApplicantCountryEnglandOrWales),
        firstName = Some("ApplicantFirstname"),
        middleName = None,
        lastName = Some("ApplicantLastname")
        )
      ),
      deceasedDetails = Some(CommonBuilder.buildDeceasedDetails copy(
          domicile = Some(TestHelper.DomicileEnglandOrWales),
          maritalStatus = Some(TestHelper.MaritalStatusSingle),
        firstName = Some("DeceasedFirstname"),
        middleName = None,
        lastName = Some("DeceasedLastname")
        )
      ),
      coExecutors = Seq(coExecutor1, coExecutor2, coExecutor3),
      ihtReference = Some("ABC"),
      acknowledgmentReference = CommonBuilder.DefaultAcknowledgmentReference
    )
  }

  def viewAsString: String = registration_summary(registrationDetails, "").toString

  def doc = asDocument(viewAsString)

  "Registration summary view" must {
    "have the correct title" in {
      titleShouldBeCorrect(viewAsString, Messages("iht.registration.checkYourAnswers"))
    }

    "have the correct browser title" in {
      browserTitleShouldBeCorrect(viewAsString, Messages("iht.registration.checkYourAnswers"))
    }

    "have a Confirm details button" in {
      doc.getElementsByClass("button").first.attr("value") shouldBe Messages("page.iht.registration.registrationSummary.button")
    }

    "display the correct values in the table of entered details" in {
      val expectedSetRows = Set(
        SharableOverviewRow("Date of death", "12 December 2011", "Change"),
        SharableOverviewRow("Contact address", "addr1 addr2 addr3 addr4 AA1 1AA United Kingdom", "Change"),
        SharableOverviewRow("Location of deceased’s permanent home", "England or Wales", "Change"),
        SharableOverviewRow("Address", "addr1 addr2 addr3 addr4 AA1 1AA United Kingdom", "Change"),
        SharableOverviewRow("Date of birth", "12 December 1998", "Change"),
        SharableOverviewRow("Name", "Mary Smith", "Change"),
        SharableOverviewRow("National Insurance number", "KR131911A", ""),
        SharableOverviewRow("National Insurance number", "KR131911A", "Change"),
        SharableOverviewRow("Are you applying for probate for the deceased’s estate?", "Yes", "Change"),
        SharableOverviewRow("Where are you going to apply for probate?", "England or Wales", "Change"),
        SharableOverviewRow("Phone number", "02079460093", "Change"),
        SharableOverviewRow("Name", "Mary Taylor", "Change"),
        SharableOverviewRow("Relationship status", "Never married or in a civil partnership", "Change"),
        SharableOverviewRow("Name", "Patricia Taylor", "Change"),
        SharableOverviewRow("Date of birth", "12 December 1998", ""),
        SharableOverviewRow("Name", "Mary Smith", ""),
        SharableOverviewRow("Name", "Robert Davies", "Change"))

      val tableHTMLElements: Elements = doc.select("li.tabular-data__entry")
      val setRows = tableHTMLElements.map(element => SharableOverviewRow.apply(element)).toSet

      setRows shouldBe expectedSetRows

    }
  }
}

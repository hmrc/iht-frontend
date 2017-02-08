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

import iht.models.{RegistrationDetails, UkAddress}
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

private case class SharableOverviewRow(rowText: String = "", value: String = "", linkText: String = "")

private object SharableOverviewRow {
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
    Try(link.getElementsByTag("span")) match {
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
    val coExecutor1 = CommonBuilder.buildCoExecutor.copy(firstName = "Coexec1firstname",
      lastName = "Coexec1lastname", nino = "XX121212E")
    val coExecutor2 = CommonBuilder.buildCoExecutor.copy(firstName = "Coexec2firstname",
      lastName = "Coexec2lastname", nino = "XX121212F")
    val coExecutor3 = CommonBuilder.buildCoExecutor.copy(firstName = "Coexec3firstname",
      lastName = "Coexec3lastname", nino = "XX121212G")

    val deceasedUkAddress = new UkAddress("deceasedaddr1", "deceasedaddr2", Some("deceasedaddr3"),
      Some("deceasedaddr4"), CommonBuilder.DefaultPostCode)
    val applicantUkAddress = new UkAddress("applicantaddr1", "applicantaddr2", Some("applicantaddr3"),
      Some("applicantaddr4"), CommonBuilder.DefaultPostCode)

    RegistrationDetails(
      deceasedDateOfDeath = Some(CommonBuilder.buildDeceasedDateOfDeath),
      applicantDetails = Some(CommonBuilder.buildApplicantDetails copy(
        country = Some(TestHelper.ApplicantCountryEnglandOrWales),
        firstName = Some("ApplicantFirstname"),
        middleName = None,
        lastName = Some("ApplicantLastname"),
        ukAddress = Some(applicantUkAddress),
        nino = Some("XX121212C")
      )),
      deceasedDetails = Some(CommonBuilder.buildDeceasedDetails copy(
        domicile = Some(TestHelper.DomicileEnglandOrWales),
        maritalStatus = Some(TestHelper.MaritalStatusSingle),
        firstName = Some("DeceasedFirstname"),
        middleName = None,
        lastName = Some("DeceasedLastname"),
        ukAddress = Some(deceasedUkAddress),
        nino = Some("XX121212D")
      )),
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
        // Deceased
        SharableOverviewRow(Messages("iht.dateOfDeath"), "12 December 2011", "Change"),
        SharableOverviewRow(Messages("iht.name.upperCaseInitial"), "DeceasedFirstname DeceasedLastname", "Change"),
        SharableOverviewRow(Messages("iht.registration.deceased.locationOfPermanentHome"), "England or Wales", "Change"),
        SharableOverviewRow(Messages("iht.registration.contactAddress"),
          "deceasedaddr1 deceasedaddr2 deceasedaddr3 deceasedaddr4 AA1 1AA United Kingdom", "Change"),
        SharableOverviewRow(Messages("iht.dateofbirth"), "12 December 1998", "Change"),
        SharableOverviewRow(Messages("iht.nationalInsuranceNo"), "XX121212D", "Change"),

        // Applicant
        SharableOverviewRow(Messages("iht.registration.applicant.applyingForProbate"), "Yes", "Change"),
        SharableOverviewRow(Messages("iht.name.upperCaseInitial"), "ApplicantFirstname ApplicantLastname"),
        SharableOverviewRow(Messages("page.iht.registration.applicant.probateLocation.title"), "England or Wales", "Change"),
        SharableOverviewRow(Messages("iht.registration.checklist.phoneNo.upperCaseInitial"), "02079460093", "Change"),
        SharableOverviewRow(Messages("iht.address.upperCaseInitial"),
          "applicantaddr1 applicantaddr2 applicantaddr3 applicantaddr4 AA1 1AA United Kingdom", "Change"),
        SharableOverviewRow(Messages("page.iht.registration.registrationSummary.deceasedInfo.maritalStatus.label"),
          "Never married or in a civil partnership", "Change"),
        SharableOverviewRow(Messages("iht.nationalInsuranceNo"), "XX121212C"),
        SharableOverviewRow(Messages("iht.dateofbirth"), "12 December 1998"),

        // Co-executors
        SharableOverviewRow(Messages("iht.name.upperCaseInitial"), "Coexec1firstname Coexec1lastname", "Change"),
        SharableOverviewRow(Messages("iht.nationalInsuranceNo"), "XX121212E", "Change"),
        SharableOverviewRow(Messages("iht.name.upperCaseInitial"), "Coexec2firstname Coexec2lastname", "Change"),
        SharableOverviewRow(Messages("iht.nationalInsuranceNo"), "XX121212F", "Change"),
        SharableOverviewRow(Messages("iht.name.upperCaseInitial"), "Coexec3firstname Coexec3lastname", "Change"),
        SharableOverviewRow(Messages("iht.nationalInsuranceNo"), "XX121212G", "Change"),
        SharableOverviewRow(Messages("iht.address.upperCaseInitial"),
          "addr1 addr2 addr3 addr4 AA1 1AA United Kingdom", "Change")
      )

      val tableHTMLElements: Elements = doc.select("li.tabular-data__entry")
      val setRows = tableHTMLElements.map(element => SharableOverviewRow.apply(element)).toSet

      setRows shouldBe expectedSetRows
    }
  }
}

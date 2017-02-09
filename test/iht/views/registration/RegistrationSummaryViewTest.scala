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
import iht.testhelpers.{CommonBuilder, NinoBuilder, TestHelper}
import iht.utils.CommonHelper
import iht.views.ViewTestHelper
import iht.views.html.registration.registration_summary
import org.jsoup.nodes.Element
import org.jsoup.select.Elements
import play.api.i18n.Messages
import play.api.mvc.AnyContentAsEmpty
import play.api.test.FakeRequest
import iht.views.html._

import scala.collection.JavaConversions._
import scala.util.{Failure, Success, Try}
import iht.controllers.registration.applicant.{routes => applicantRoutes}
import iht.controllers.registration.deceased.{routes => deceasedRoutes}
import iht.controllers.registration.executor.{routes => executorRoutes}

case class SharableOverviewRow(rowText: String = "", value: String = "", linkText: String = "", linkHref: String = "")

object SharableOverviewRow {
  def apply(element: Element): SharableOverviewRow = {
    val cells = element.select("div:not(.visually-hidden)")
    val row = cells.size match {
      case 2 => SharableOverviewRow(
        rowText = cells.get(0).text,
        linkText = cells.get(1).text
      )
      case 3 =>
        SharableOverviewRow(
          rowText = cells.get(0).text,
          value = cells.get(1).text,
          linkText = getLinkText(cells.get(2).getElementsByTag("a").first),
          linkHref = getLinkHref(cells.get(2).getElementsByTag("a").first)
        )
      case 4 =>
        SharableOverviewRow(
          rowText = cells.get(0).text,
          value = cells.get(1).text,
          linkText = getLinkText(cells.get(3).getElementsByTag("a").first),
          linkHref = getLinkHref(cells.get(2).getElementsByTag("a").first)
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

  def getLinkHref(link: Element) = {
    Try(link.attr("href")) match {
      case Success(href) =>
        href
      case Failure(_) =>
        ""
    }
  }
}

class RegistrationSummaryViewTest extends ViewTestHelper {
  implicit def request: FakeRequest[AnyContentAsEmpty.type] = createFakeRequest()

  val applicantNino = NinoBuilder.defaultNino
  val deceasedNino = NinoBuilder.defaultNino
  val coExecutorNino1 = NinoBuilder.defaultNino
  val coExecutorNino2 = NinoBuilder.defaultNino
  val coExecutorNino3 = NinoBuilder.defaultNino
  val coExecutorPhoneNo1 = "02079460091"
  val coExecutorPhoneNo2 = "02079460092"
  val coExecutorPhoneNo3 = "02079460093"

  def deceasedName = CommonHelper.getDeceasedNameOrDefaultString(registrationDetailsAllUKAddresses)

  def registrationDetails(deceasedUkAddress: UkAddress, applicantUkAddress: UkAddress) = {
    val coExecutorAddress1 = new UkAddress("addr11", "addr12", Some("addr13"), Some("addr14"), "AA1 1AA")
    val coExecutorAddress2 = new UkAddress("addr21", "addr22", Some("addr23"), Some("addr24"), "AA2 1AA")
    val coExecutorAddress3 = new UkAddress("addr31", "addr32", Some("addr33"), Some("addr34"), "AA3 1AA")

    val coExecutorContactDetails1 = new iht.models.ContactDetails(coExecutorPhoneNo1, Some("a@example.com"))
    val coExecutorContactDetails2 = new iht.models.ContactDetails(coExecutorPhoneNo2, Some("a@example.com"))
    val coExecutorContactDetails3 = new iht.models.ContactDetails(coExecutorPhoneNo3, Some("a@example.com"))

    val coExecutor1 = CommonBuilder.buildCoExecutor.copy(id = Some("1"), firstName = "Coexec1firstname",
      lastName = "Coexec1lastname", nino = coExecutorNino1, ukAddress = Some(coExecutorAddress1),
      contactDetails = coExecutorContactDetails1
    )
    val coExecutor2 = CommonBuilder.buildCoExecutor.copy(id = Some("2"), firstName = "Coexec2firstname",
      lastName = "Coexec2lastname", nino = coExecutorNino2, ukAddress = Some(coExecutorAddress2),
      contactDetails = coExecutorContactDetails2
    )
    val coExecutor3 = CommonBuilder.buildCoExecutor.copy(id = Some("3"), firstName = "Coexec3firstname",
      lastName = "Coexec3lastname", nino = coExecutorNino3, ukAddress = Some(coExecutorAddress3),
      contactDetails = coExecutorContactDetails3
    )

    RegistrationDetails(
      deceasedDateOfDeath = Some(CommonBuilder.buildDeceasedDateOfDeath),
      applicantDetails = Some(CommonBuilder.buildApplicantDetails copy(
        country = Some(TestHelper.ApplicantCountryEnglandOrWales),
        firstName = Some("ApplicantFirstname"),
        middleName = None,
        lastName = Some("ApplicantLastname"),
        ukAddress = Some(applicantUkAddress),
        nino = Some(applicantNino)
      )),
      deceasedDetails = Some(CommonBuilder.buildDeceasedDetails copy(
        domicile = Some(TestHelper.DomicileEnglandOrWales),
        maritalStatus = Some(TestHelper.MaritalStatusSingle),
        firstName = Some("DeceasedFirstname"),
        middleName = None,
        lastName = Some("DeceasedLastname"),
        ukAddress = Some(deceasedUkAddress),
        nino = Some(deceasedNino)
      )),
      coExecutors = Seq(coExecutor1, coExecutor2, coExecutor3),
      ihtReference = Some("ABC"),
      acknowledgmentReference = CommonBuilder.DefaultAcknowledgmentReference
    )
  }

  def expectedExecutor(id: String, name: String, nino: String, addr: String, phone: String, dob: String) = {
    Set(
      SharableOverviewRow(Messages("iht.name.upperCaseInitial"), name, Messages("iht.change"),
        executorRoutes.CoExecutorPersonalDetailsController.onEditPageLoad(id).url + "#firstName"),
      SharableOverviewRow(Messages("iht.nationalInsuranceNo"), nino, Messages("iht.change"),
        executorRoutes.CoExecutorPersonalDetailsController.onEditPageLoad(id).url + "#nino"),
      SharableOverviewRow(Messages("iht.address.upperCaseInitial"),
        addr, Messages("iht.change"),
        executorRoutes.OtherPersonsAddressController.onEditPageLoadUK(id).url + "#details"),
      SharableOverviewRow(Messages("iht.registration.checklist.phoneNo.upperCaseInitial"),
        phone, Messages("iht.change"),
        executorRoutes.CoExecutorPersonalDetailsController.onEditPageLoad(id).url + "#phoneNo"),
      SharableOverviewRow(Messages("iht.dateofbirth"), dob, Messages("iht.change"),
        executorRoutes.CoExecutorPersonalDetailsController.onEditPageLoad(id).url + "#date-of-birth"))
  }

  def expectedSetRows(deceasedAddress: String, applicantAddress: String) = Set(
    SharableOverviewRow(Messages("iht.dateOfDeath"), "12 December 2011", Messages("iht.change"),
      deceasedRoutes.DeceasedDateOfDeathController.onEditPageLoad().url + "#date-of-death"),
    SharableOverviewRow(Messages("iht.name.upperCaseInitial"), "DeceasedFirstname DeceasedLastname", Messages("iht.change"),
      deceasedRoutes.AboutDeceasedController.onEditPageLoad().url + "#firstName"),
    SharableOverviewRow(Messages("iht.registration.deceased.locationOfPermanentHome"), "England or Wales", Messages("iht.change"),
      deceasedRoutes.DeceasedPermanentHomeController.onEditPageLoad().url + "#country"),
    SharableOverviewRow(Messages("iht.registration.contactAddress"), deceasedAddress, Messages("iht.change"),
      deceasedRoutes.DeceasedAddressDetailsUKController.onEditPageLoad().url + "#details"),
    SharableOverviewRow(Messages("iht.dateofbirth"), "12 December 1998", Messages("iht.change"),
      deceasedRoutes.AboutDeceasedController.onEditPageLoad().toString + "#date-of-birth"),
    SharableOverviewRow(Messages("iht.nationalInsuranceNo"), deceasedNino, Messages("iht.change"),
      deceasedRoutes.AboutDeceasedController.onEditPageLoad().url + "#nino"),
    SharableOverviewRow(Messages("page.iht.registration.registrationSummary.deceasedInfo.maritalStatus.label"),
      "Never married or in a civil partnership", Messages("iht.change"),
      deceasedRoutes.AboutDeceasedController.onEditPageLoad().url + "#relationship-status"),

    SharableOverviewRow(Messages("iht.registration.applicant.applyingForProbate"), "Yes", Messages("iht.change"),
      applicantRoutes.ApplyingForProbateController.onEditPageLoad().url + "#applying-for-probate"),
    SharableOverviewRow(Messages("iht.name.upperCaseInitial"), "ApplicantFirstname ApplicantLastname"),
    SharableOverviewRow(Messages("page.iht.registration.applicant.probateLocation.title"), "England or Wales", Messages("iht.change"),
      applicantRoutes.ProbateLocationController.onEditPageLoad().url + "#country"),
    SharableOverviewRow(Messages("iht.registration.checklist.phoneNo.upperCaseInitial"), coExecutorPhoneNo3, Messages("iht.change"),
      applicantRoutes.ApplicantTellUsAboutYourselfController.onEditPageLoad().url + "#phoneNo"),
    SharableOverviewRow(Messages("iht.address.upperCaseInitial"), applicantAddress, Messages("iht.change"),
      applicantRoutes.ApplicantAddressController.onEditPageLoadUk().url + "#details"),
    SharableOverviewRow(Messages("iht.nationalInsuranceNo"), applicantNino),
    SharableOverviewRow(Messages("iht.dateofbirth"), "12 December 1998")
  ) ++ expectedExecutor("1", "Coexec1firstname Coexec1lastname", coExecutorNino1,
    "addr11 addr12 addr13 addr14 AA1 1AA United Kingdom", coExecutorPhoneNo1, "12 December 1998") ++
    expectedExecutor("2", "Coexec2firstname Coexec2lastname", coExecutorNino2,
      "addr21 addr22 addr23 addr24 AA2 1AA United Kingdom", coExecutorPhoneNo2, "12 December 1998") ++
    expectedExecutor("3", "Coexec3firstname Coexec3lastname", coExecutorNino3,
      "addr31 addr32 addr33 addr34 AA3 1AA United Kingdom", coExecutorPhoneNo3, "12 December 1998")

  def registrationDetailsAllUKAddresses = {
    val deceasedUkAddress = new UkAddress("deceasedaddr1", "deceasedaddr2", Some("deceasedaddr3"),
      Some("deceasedaddr4"), CommonBuilder.DefaultPostCode)
    val applicantUkAddress = new UkAddress("applicantaddr1", "applicantaddr2", Some("applicantaddr3"),
      Some("applicantaddr4"), CommonBuilder.DefaultPostCode)
    registrationDetails(deceasedUkAddress, applicantUkAddress)
  }

  def registrationDetailsAllForeignAddresses = {
    val deceasedUkAddress = new UkAddress("deceasedaddr1", "deceasedaddr2", Some("deceasedaddr3"),
      Some("deceasedaddr4"), "", "AF")
    val applicantUkAddress = new UkAddress("applicantaddr1", "applicantaddr2", Some("applicantaddr3"),
      Some("applicantaddr4"), "", "AF")
    registrationDetails(deceasedUkAddress, applicantUkAddress)
  }

  def expectedSetRowsAllUKAddresses = expectedSetRows(
    "deceasedaddr1 deceasedaddr2 deceasedaddr3 deceasedaddr4 AA1 1AA United Kingdom",
    "applicantaddr1 applicantaddr2 applicantaddr3 applicantaddr4 AA1 1AA United Kingdom")

  def expectedSetRowsAllForeignAddresses = expectedSetRows(
    "deceasedaddr1 deceasedaddr2 deceasedaddr3 deceasedaddr4 Afghanistan",
    "applicantaddr1 applicantaddr2 applicantaddr3 applicantaddr4 Afghanistan")

  def viewAsString: String = registration_summary(registrationDetailsAllUKAddresses, "").toString

  def doc = asDocument(viewAsString)

  def viewAsStringForeign: String = registration_summary(registrationDetailsAllForeignAddresses, "").toString

  def docForeign = asDocument(viewAsStringForeign)

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

    "have text paragraphs" in {
      messagesShouldBePresent(viewAsString, Messages("page.iht.registration.registrationSummary.subTitle"),
        Messages("page.iht.registration.registrationSummary.applicantTable.title"),
        Messages("iht.registration.othersApplyingForProbate"))
    }

    "have section title for deceased" in {
      messagesShouldBePresent(viewAsString,
        Messages("site.nameDetails", ihtHelpers.name(deceasedName)).toString)
    }

    "have section title for co-executor 1" in {
      messagesShouldBePresent(viewAsString,
        Messages("site.nameDetails", ihtHelpers.name(registrationDetailsAllUKAddresses.coExecutors.head.name)).toString)
    }

    "have section title for co-executor 2" in {
      messagesShouldBePresent(viewAsString,
        Messages("site.nameDetails", ihtHelpers.name(registrationDetailsAllUKAddresses.coExecutors(1).name)).toString)
    }

    "have section title for co-executor 3" in {
      messagesShouldBePresent(viewAsString,
        Messages("site.nameDetails", ihtHelpers.name(registrationDetailsAllUKAddresses.coExecutors(2).name)).toString)
    }

    "display the correct values in the table of entered details where all UK addresses" in {
      val tableHTMLElements: Elements = doc.select("li.tabular-data__entry")
      val setRows = tableHTMLElements.map(element => SharableOverviewRow.apply(element)).toSet
      setRows shouldBe expectedSetRowsAllUKAddresses
    }

    //    "display the correct values in the table of entered details where all foreign addresses" in {
    //      val tableHTMLElements: Elements = docForeign.select("li.tabular-data__entry")
    //      val setRows = tableHTMLElements.map(element => SharableOverviewRow.apply(element)).toSet
    //      setRows shouldBe expectedSetRowsAllForeignAddresses
    //    }
  }
}

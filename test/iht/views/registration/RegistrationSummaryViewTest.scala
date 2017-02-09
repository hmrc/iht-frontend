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

  val coExecutor1Addr1 = "addr11"
  val coExecutor1Addr2 = "addr12"
  val coExecutor1Addr3 = "addr13"
  val coExecutor1Addr4 = "addr14"

  val coExecutor2Addr1 = "addr21"
  val coExecutor2Addr2 = "addr22"
  val coExecutor2Addr3 = "addr23"
  val coExecutor2Addr4 = "addr24"

  val coExecutor3Addr1 = "addr31"
  val coExecutor3Addr2 = "addr32"
  val coExecutor3Addr3 = "addr33"
  val coExecutor3Addr4 = "addr34"

  val coExecutorFirstName1 = "Coexec1firstname"
  val coExecutorFirstName2 = "Coexec2firstname"
  val coExecutorFirstName3 = "Coexec3firstname"

  val coExecutorLastName1 = "Coexec1lastname"
  val coExecutorLastName2 = "Coexec2lastname"
  val coExecutorLastName3 = "Coexec3lastname"

  val deceasedAddr1 = "deceasedaddr1"
  val deceasedAddr2 = "deceasedaddr2"
  val deceasedAddr3 = "deceasedaddr3"
  val deceasedAddr4 = "deceasedaddr4"

  val applicantAddr1 = "applicantaddr1"
  val applicantAddr2 = "applicantaddr2"
  val applicantAddr3 = "applicantaddr3"
  val applicantAddr4 = "applicantaddr4"

  val applicantFirstName = "applicantFirstName"
  val applicantLastName = "applicantLastName"

  val deceasedFirstName = "deceasedFirstName"
  val deceasedLastName = "deceasedLastName"

  val countryCodeForeign = "AF"
  val englandOrWales = "England or Wales"
  val unitedKingdom = "United Kingdom"
  val foreign = "Afghanistan"

  val postCode1 = "AA1 1AA"
  val postCode2 = "AA2 1AA"
  val postCode3 = "AA3 1AA"

  val email = "a@example.com"

  val dob = "12 December 1998"

  def deceasedName = CommonHelper.getDeceasedNameOrDefaultString(registrationDetailsAllUKAddresses)

  def registrationDetails(deceasedUkAddress: UkAddress, applicantUkAddress: UkAddress,
                          coExecutorAddress1: UkAddress, coExecutorAddress2: UkAddress, coExecutorAddress3: UkAddress) = {

    val coExecutorContactDetails1 = new iht.models.ContactDetails(coExecutorPhoneNo1, Some(email))
    val coExecutorContactDetails2 = new iht.models.ContactDetails(coExecutorPhoneNo2, Some(email))
    val coExecutorContactDetails3 = new iht.models.ContactDetails(coExecutorPhoneNo3, Some(email))

    val coExecutor1 = CommonBuilder.buildCoExecutor.copy(id = Some("1"), firstName = coExecutorFirstName1,
      lastName = coExecutorLastName1, nino = coExecutorNino1, ukAddress = Some(coExecutorAddress1),
      contactDetails = coExecutorContactDetails1
    )
    val coExecutor2 = CommonBuilder.buildCoExecutor.copy(id = Some("2"), firstName = coExecutorFirstName2,
      lastName = coExecutorLastName2, nino = coExecutorNino2, ukAddress = Some(coExecutorAddress2),
      contactDetails = coExecutorContactDetails2
    )
    val coExecutor3 = CommonBuilder.buildCoExecutor.copy(id = Some("3"), firstName = coExecutorFirstName3,
      lastName = coExecutorLastName3, nino = coExecutorNino3, ukAddress = Some(coExecutorAddress3),
      contactDetails = coExecutorContactDetails3
    )

    RegistrationDetails(
      deceasedDateOfDeath = Some(CommonBuilder.buildDeceasedDateOfDeath),
      applicantDetails = Some(CommonBuilder.buildApplicantDetails copy(
        country = Some(TestHelper.ApplicantCountryEnglandOrWales),
        firstName = Some(applicantFirstName),
        middleName = None,
        lastName = Some(applicantLastName),
        ukAddress = Some(applicantUkAddress),
        nino = Some(applicantNino)
      )),
      deceasedDetails = Some(CommonBuilder.buildDeceasedDetails copy(
        domicile = Some(TestHelper.DomicileEnglandOrWales),
        maritalStatus = Some(TestHelper.MaritalStatusSingle),
        firstName = Some(deceasedFirstName),
        middleName = None,
        lastName = Some(deceasedLastName),
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

  def expectedSetRows(deceasedAddress: String, applicantAddress: String,
                      coExecutor1Address: String, coExecutor2Address: String, coExecutor3Address: String) = Set(
    SharableOverviewRow(Messages("iht.dateOfDeath"), "12 December 2011", Messages("iht.change"),
      deceasedRoutes.DeceasedDateOfDeathController.onEditPageLoad().url + "#date-of-death"),
    SharableOverviewRow(Messages("iht.name.upperCaseInitial"), s"$deceasedFirstName $deceasedLastName", Messages("iht.change"),
      deceasedRoutes.AboutDeceasedController.onEditPageLoad().url + "#firstName"),
    SharableOverviewRow(Messages("iht.registration.deceased.locationOfPermanentHome"), englandOrWales, Messages("iht.change"),
      deceasedRoutes.DeceasedPermanentHomeController.onEditPageLoad().url + "#country"),
    SharableOverviewRow(Messages("iht.registration.contactAddress"), deceasedAddress, Messages("iht.change"),
      deceasedRoutes.DeceasedAddressDetailsUKController.onEditPageLoad().url + "#details"),
    SharableOverviewRow(Messages("iht.dateofbirth"), dob, Messages("iht.change"),
      deceasedRoutes.AboutDeceasedController.onEditPageLoad().toString + "#date-of-birth"),
    SharableOverviewRow(Messages("iht.nationalInsuranceNo"), deceasedNino, Messages("iht.change"),
      deceasedRoutes.AboutDeceasedController.onEditPageLoad().url + "#nino"),
    SharableOverviewRow(Messages("page.iht.registration.registrationSummary.deceasedInfo.maritalStatus.label"),
      "Never married or in a civil partnership", Messages("iht.change"),
      deceasedRoutes.AboutDeceasedController.onEditPageLoad().url + "#relationship-status"),

    SharableOverviewRow(Messages("iht.registration.applicant.applyingForProbate"), "Yes", Messages("iht.change"),
      applicantRoutes.ApplyingForProbateController.onEditPageLoad().url + "#applying-for-probate"),
    SharableOverviewRow(Messages("iht.name.upperCaseInitial"), s"$applicantFirstName $applicantLastName"),
    SharableOverviewRow(Messages("page.iht.registration.applicant.probateLocation.title"), englandOrWales, Messages("iht.change"),
      applicantRoutes.ProbateLocationController.onEditPageLoad().url + "#country"),
    SharableOverviewRow(Messages("iht.registration.checklist.phoneNo.upperCaseInitial"), coExecutorPhoneNo3, Messages("iht.change"),
      applicantRoutes.ApplicantTellUsAboutYourselfController.onEditPageLoad().url + "#phoneNo"),
    SharableOverviewRow(Messages("iht.address.upperCaseInitial"), applicantAddress, Messages("iht.change"),
      applicantRoutes.ApplicantAddressController.onEditPageLoadUk().url + "#details"),
    SharableOverviewRow(Messages("iht.nationalInsuranceNo"), applicantNino),
    SharableOverviewRow(Messages("iht.dateofbirth"), dob)
  ) ++ expectedExecutor("1", s"$coExecutorFirstName1 $coExecutorLastName1", coExecutorNino1,
    coExecutor1Address, coExecutorPhoneNo1, dob) ++
    expectedExecutor("2", s"$coExecutorFirstName2 $coExecutorLastName2", coExecutorNino2,
      coExecutor2Address, coExecutorPhoneNo2, dob) ++
    expectedExecutor("3", s"$coExecutorFirstName3 $coExecutorLastName3", coExecutorNino3,
      coExecutor3Address, coExecutorPhoneNo3, dob)

  def registrationDetailsAllUKAddresses = {
    val deceasedUkAddress = new UkAddress(deceasedAddr1, deceasedAddr2, Some(deceasedAddr3),
      Some(deceasedAddr4), CommonBuilder.DefaultPostCode)
    val applicantUkAddress = new UkAddress(applicantAddr1, applicantAddr2, Some(applicantAddr3),
      Some(applicantAddr4), CommonBuilder.DefaultPostCode)
    val coExecutorAddress1 = new UkAddress(coExecutor1Addr1, coExecutor1Addr2, Some(coExecutor1Addr3), Some(coExecutor1Addr4), postCode1)
    val coExecutorAddress2 = new UkAddress(coExecutor2Addr1, coExecutor2Addr2, Some(coExecutor2Addr3), Some(coExecutor2Addr4), postCode2)
    val coExecutorAddress3 = new UkAddress(coExecutor3Addr1, coExecutor3Addr2, Some(coExecutor3Addr3), Some(coExecutor3Addr4), postCode3)
    registrationDetails(deceasedUkAddress, applicantUkAddress, coExecutorAddress1, coExecutorAddress2, coExecutorAddress3)
  }

  def registrationDetailsAllForeignAddresses = {
    val deceasedUkAddress = new UkAddress(deceasedAddr1, deceasedAddr2, Some(deceasedAddr3),
      Some(deceasedAddr4), "", countryCodeForeign)
    val applicantUkAddress = new UkAddress(applicantAddr1, applicantAddr2, Some(applicantAddr3),
      Some(applicantAddr4), "", countryCodeForeign)
    val coExecutorAddress1 = new UkAddress(coExecutor1Addr1, coExecutor1Addr2, Some(coExecutor1Addr3), Some(coExecutor1Addr4), "", countryCodeForeign)
    val coExecutorAddress2 = new UkAddress(coExecutor2Addr1, coExecutor2Addr2, Some(coExecutor2Addr3), Some(coExecutor2Addr4), "", countryCodeForeign)
    val coExecutorAddress3 = new UkAddress(coExecutor3Addr1, coExecutor3Addr2, Some(coExecutor3Addr3), Some(coExecutor3Addr4), "", countryCodeForeign)
    registrationDetails(deceasedUkAddress, applicantUkAddress, coExecutorAddress1, coExecutorAddress2, coExecutorAddress3)
  }

  def expectedSetRowsAllUKAddresses = expectedSetRows(
    s"$deceasedAddr1 $deceasedAddr2 $deceasedAddr3 $deceasedAddr4 $postCode1 $unitedKingdom",
    s"$applicantAddr1 $applicantAddr2 $applicantAddr3 $applicantAddr4 $postCode1 $unitedKingdom",
    s"$coExecutor1Addr1 $coExecutor1Addr2 $coExecutor1Addr3 $coExecutor1Addr4 $postCode1 $unitedKingdom",
    s"$coExecutor2Addr1 $coExecutor2Addr2 $coExecutor2Addr3 $coExecutor2Addr4 $postCode2 $unitedKingdom",
    s"$coExecutor3Addr1 $coExecutor3Addr2 $coExecutor3Addr3 $coExecutor3Addr4 $postCode3 $unitedKingdom"
  )

  def expectedSetRowsAllForeignAddresses = expectedSetRows(
    s"$deceasedAddr1 $deceasedAddr2 $deceasedAddr3 $deceasedAddr4 $foreign",
    s"$applicantAddr1 $applicantAddr2 $applicantAddr3 $applicantAddr4 $foreign",
    s"$coExecutor1Addr1 $coExecutor1Addr2 $coExecutor1Addr3 $coExecutor1Addr4 $foreign",
    s"$coExecutor2Addr1 $coExecutor2Addr2 $coExecutor2Addr3 $coExecutor2Addr4 $foreign",
    s"$coExecutor3Addr1 $coExecutor3Addr2 $coExecutor3Addr3 $coExecutor3Addr4 $foreign"
  )

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

    "display the correct values in the table of entered details where all foreign addresses" in {
      val tableHTMLElements: Elements = docForeign.select("li.tabular-data__entry")
      val setRows = tableHTMLElements.map(element => SharableOverviewRow.apply(element)).toSet
      setRows shouldBe expectedSetRowsAllForeignAddresses
    }

    "have a link to add or delete an executor" in {
      val anchor = doc.getElementById("coexecutors-summary")
      anchor.text shouldBe Messages("page.iht.registration.registrationSummary.coExecutorTable.changeOthersApplying.link")
      anchor.attr("href") shouldBe iht.controllers.registration.executor.routes.ExecutorOverviewController.onPageLoad().url
    }
  }
}

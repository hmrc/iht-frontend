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

package iht.utils

import iht.FakeIhtApp
import iht.constants.Constants
import iht.models.application.ApplicationDetails
import iht.models.application.exemptions.BasicExemptionElement
import iht.models.{DeceasedDateOfDeath, RegistrationDetails}
import iht.testhelpers._
import org.joda.time.LocalDate
import org.scalatest.mock.MockitoSugar
import play.api.i18n.{I18nSupport, Messages, MessagesApi}
import play.api.i18n.Messages.Implicits._
import play.api.Play.current
import play.api.mvc.Session
import play.api.test.FakeRequest
import uk.gov.hmrc.domain.Nino
import uk.gov.hmrc.play.frontend.auth.connectors.domain.{Accounts, ConfidenceLevel, CredentialStrength}
import uk.gov.hmrc.play.frontend.auth.{AuthContext, LoggedInUser, Principal}
import uk.gov.hmrc.play.http.HeaderCarrier
import uk.gov.hmrc.play.http.logging.SessionId
import uk.gov.hmrc.play.test.UnitSpec
import uk.gov.hmrc.play.frontend.auth.connectors.domain.IhtAccount
import uk.gov.hmrc.play.frontend.auth.Principal

import scala.collection.immutable.ListMap

/**
 *
 * Created by Vineet Tyagi on 28/05/15.
 *
 * This Class contains the Unit Tests for iht.utils.CommonHelper
 */
class CommonHelperTest extends UnitSpec with FakeIhtApp with MockitoSugar with I18nSupport {

  implicit val messagesApi: MessagesApi = app.injector.instanceOf[MessagesApi]
  val referrerURL="http://localhost:9070/inheritance-tax/registration/addExecutor"
  val host="localhost:9070"

  "Must provide the Referrer URL excluding the host" in {

    val request = FakeRequest().withHeaders(("referer", referrerURL), ("host", host))
    val result = CommonHelper.getReferrerPathExcludingHost(request)

    assert(result.equals(
      "/inheritance-tax/registration/addExecutor"),"URL without host is /inheritance-tax/registration/addExecutor")

  }

  /*
   * Test the input status to proper status format
   * e.g.  - Input In review
   *         Output In review
   */
  "Must convert the application status in proper format" in {

    val inputStatus = "Awaiting Return"
    val result = CommonHelper.formatStatus(inputStatus)

    assert(result.equals("Awaiting return"), "Reformatted status is Awaiting return")

  }

  "verify the input date is within range" in {
    val date = LocalDate.now.plusMonths(12)
    assert(CommonHelper.isDateWithInRange(date) == true, "Given date must be with in next 24 months from last " +
      "day of the month of the given date")
  }

  "verify the input date is not within range" in {
    val date = LocalDate.now.minusMonths(27)
    assert(CommonHelper.isDateWithInRange(date) == false, "Given date must be out of next 24 months from last" +
      "day of the month of the given date")
  }

  "format status must format a status" in {
    val formattedStatus = CommonHelper.formatStatus("All gOod")
    assert(formattedStatus == "All good")
  }

  "format status must replace kickout with in progress" in {
    val formattedStatus = CommonHelper.formatStatus(ApplicationStatus.KickOut)
    val formattedInProgress = CommonHelper.formatStatus(ApplicationStatus.InProgress)
    assert(formattedStatus == formattedInProgress)
  }

  "format status must capitalise the first letter of the first word" in {
    val formattedStatus = CommonHelper.formatStatus("lower CASES")
    assert(formattedStatus == "Lower cases")
  }

  "findFirstTrue should return correct item subscript when last of three is true" in {
    val a: (RegistrationDetails, ApplicationDetails, Seq[BigDecimal]) => Boolean = (rd, ad, st) => false
    val b: (RegistrationDetails, ApplicationDetails, Seq[BigDecimal]) => Boolean = (rd, ad, st) => false
    val c: (RegistrationDetails, ApplicationDetails, Seq[BigDecimal]) => Boolean = (rd, ad, st) => true
    val aa = CommonHelper.findFirstTrue(CommonBuilder.buildRegistrationDetails, CommonBuilder.buildApplicationDetails, Seq(BigDecimal(0)),
      ListMap("1" -> a, "2" -> b, "3" -> c))
    aa shouldBe Some("3")
  }

  "findFirstTrue should return correct item subscript when second of three is true" in {
    val a: (RegistrationDetails, ApplicationDetails, Seq[BigDecimal]) => Boolean = (rd, ad, st) => false
    val b: (RegistrationDetails, ApplicationDetails, Seq[BigDecimal]) => Boolean = (rd, ad, st) => true
    val c: (RegistrationDetails, ApplicationDetails, Seq[BigDecimal]) => Boolean = (rd, ad, st) => false
    val aa = CommonHelper.findFirstTrue(CommonBuilder.buildRegistrationDetails, CommonBuilder.buildApplicationDetails, Seq(BigDecimal(0)),
      ListMap("1" -> a, "2" -> b, "3" -> c))
    aa shouldBe Some("2")
  }

  "findFirstTrue should return correct item subscript when first of three is true" in {
    val a: (RegistrationDetails, ApplicationDetails, Seq[BigDecimal]) => Boolean = (rd, ad, st) => true
    val b: (RegistrationDetails, ApplicationDetails, Seq[BigDecimal]) => Boolean = (rd, ad, st) => false
    val c: (RegistrationDetails, ApplicationDetails, Seq[BigDecimal]) => Boolean = (rd, ad, st) => false
    val aa = CommonHelper.findFirstTrue(CommonBuilder.buildRegistrationDetails, CommonBuilder.buildApplicationDetails, Seq(BigDecimal(0)),
      ListMap("1" -> a, "2" -> b, "3" -> c))
    aa shouldBe Some("1")
  }

  "findFirstTrue should return None when none of three is true" in {
    val a: (RegistrationDetails, ApplicationDetails, Seq[BigDecimal]) => Boolean = (rd, ad, st) => false
    val b: (RegistrationDetails, ApplicationDetails, Seq[BigDecimal]) => Boolean = (rd, ad, st) => false
    val c: (RegistrationDetails, ApplicationDetails, Seq[BigDecimal]) => Boolean = (rd, ad, st) => false
    val aa = CommonHelper.findFirstTrue(CommonBuilder.buildRegistrationDetails, CommonBuilder.buildApplicationDetails, Seq(BigDecimal(0)),
      ListMap("1" -> a, "2" -> b, "3" -> c))
    aa shouldBe None
  }

  "findFirstTrue should return correct item subscript when more than one item is true" in {

    val a: (RegistrationDetails, ApplicationDetails, Seq[BigDecimal]) => Boolean = (rd, ad, st) => false
    val b: (RegistrationDetails, ApplicationDetails, Seq[BigDecimal]) => Boolean = (rd, ad, st) => false
    val c: (RegistrationDetails, ApplicationDetails, Seq[BigDecimal]) => Boolean = (rd, ad, st) => false
    val d: (RegistrationDetails, ApplicationDetails, Seq[BigDecimal]) => Boolean = (rd, ad, st) => true
    val e: (RegistrationDetails, ApplicationDetails, Seq[BigDecimal]) => Boolean = (rd, ad, st) => false
    val f: (RegistrationDetails, ApplicationDetails, Seq[BigDecimal]) => Boolean = (rd, ad, st) => true
    val g: (RegistrationDetails, ApplicationDetails, Seq[BigDecimal]) => Boolean = (rd, ad, st) => false
    val h: (RegistrationDetails, ApplicationDetails, Seq[BigDecimal]) => Boolean = (rd, ad, st) => false

    val aa = CommonHelper.findFirstTrue(
      CommonBuilder.buildRegistrationDetails, CommonBuilder.buildApplicationDetails, Seq(BigDecimal(0)),
      ListMap("1" -> a, "2" -> b, "3" -> c, "4" -> d, "5" -> e, "6" -> f, "7" -> g, "8" -> h))
    aa shouldBe Some("4")
  }


  "findFirstTrue should return None when sequence is empty" in {
    val aa = CommonHelper.findFirstTrue(
      CommonBuilder.buildRegistrationDetails, CommonBuilder.buildApplicationDetails, Seq(BigDecimal(0)), ListMap())
    aa shouldBe None
  }

  "trimAndUpperCaseNino should return correctly formatted nino" in {
    val nino = CommonBuilder.DefaultNino
   val result = CommonHelper.trimAndUpperCaseNino(" " + nino.toLowerCase + " ")
   result shouldBe nino
  }


  "generateAcknowledgeReference should not contain a dash" in {
    val result = CommonHelper.generateAcknowledgeReference
    result shouldNot contain("-")
  }

  "getSessionId should return a string when given SessionId" in {
    val hc = new HeaderCarrier(sessionId = Some(SessionId("1")))
    val result = CommonHelper.getSessionId(hc)
    result.length shouldNot be(0)
  }


  "getSessionId should throw a RuntimeException when no SessionId found" in {
    val hc = new HeaderCarrier()
    a [RuntimeException] shouldBe thrownBy {
      CommonHelper.getSessionId(hc)
    }
  }

  "getNino should throw a RuntimeException when user account could not be retrieved" in {

    val loggedInUser = new LoggedInUser(CommonBuilder.firstNameGenerator, None, None, None, CredentialStrength.Strong, ConfidenceLevel.L300, "")
    val ac = new AuthContext(loggedInUser, Principal(None, Accounts()), None, None, None, None)
    a [RuntimeException] shouldBe thrownBy {
      CommonHelper.getNino(ac)
    }
  }

  "booleanToYesNo should return Yes as a String" in {
    val result = CommonHelper.booleanToYesNo(boolean = true)
    result should be("Yes")
  }

  "booleanToYesNo should return No as a String" in {
    val result = CommonHelper.booleanToYesNo(boolean = false)
    result should be("No")
  }

  "createDate should return None" in {
   val result = CommonHelper.createDate(Some(""), Some("01"), Some("10"))
   result should be(None)
  }

  "preDeceasedDiedEligible should return true if date is later than eligibility date" in {
   CommonHelper.preDeceasedDiedEligible(LocalDate.now) should be(true)
  }

  "preDeceasedDiedEligible should return true if date is equal to eligibility date" in {
    CommonHelper.preDeceasedDiedEligible(new LocalDate(1974, 11, 13)) should be(true)
  }

  "preDeceasedDiedEligible should return false if date is earlier than eligibility date" in {
    CommonHelper.preDeceasedDiedEligible(new LocalDate(1974, 11, 12)) should be(false)
  }

  "getOrException throws exception if String None passed in with suitable message" in {
    val aa:Option[String] = None
    intercept[RuntimeException] {
      CommonHelper.getOrException(aa)
    }.getMessage should include ("No element found")
  }

  "getOrException throws exception if Application Details None passed in with suitable message" in {
    val aa:Option[ApplicationDetails] = None
    intercept[RuntimeException] {
      CommonHelper.getOrExceptionNoApplication(aa)
    }.getMessage should include ("No application details")
  }

  "getOrException throws exception if Application Details saved None passed in with suitable message" in {
    val aa:Option[ApplicationDetails] = None
    intercept[RuntimeException] {
      CommonHelper.getOrExceptionApplicationNotSaved(aa)
    }.getMessage should include ("Unable to save application")
  }

  "getOrException throws exception if Registration Details None passed in with suitable message" in {
    val aa:Option[RegistrationDetails] = None
    intercept[RuntimeException] {
      CommonHelper.getOrExceptionNoRegistration(aa)
    }.getMessage should include ("No registration details")
  }

  "getOrException throws exception if IHT Ref None passed in with suitable message" in {
    val aa:Option[String] = None
    intercept[RuntimeException] {
      CommonHelper.getOrExceptionNoIHTRef(aa)
    }.getMessage should include ("No IHT Reference")
  }

  "Predicate isThereADateOfDeath returns true when there is a date of death" in {
    CommonHelper.isThereADateOfDeath(CommonBuilder.buildRegistrationDetails copy(
        deceasedDateOfDeath = Some(DeceasedDateOfDeath(new LocalDate(2000,10,10)))), ""
    ) shouldBe true
  }

  "Predicate isThereADateOfDeath returns false when there is no date of death" in {
    CommonHelper.isThereADateOfDeath(CommonBuilder.buildRegistrationDetails copy(
      deceasedDateOfDeath = None), "") shouldBe false
  }

  "Predicate isThereADeceasedDomicile returns true when there's a deceased domicile" in {
    CommonHelper.isThereADeceasedDomicile(CommonBuilder.buildRegistrationDetails copy(
        deceasedDetails = Some(CommonBuilder.buildDeceasedDetails)
      ), "") shouldBe true
  }

  "Predicate isThereADeceasedDomicile returns false when there's no deceased domicile" in {
    CommonHelper.isThereADeceasedDomicile(CommonBuilder.buildRegistrationDetails copy(
      deceasedDetails = Some(CommonBuilder.buildDeceasedDetails.copy(domicile = None))
      ), "") shouldBe false
  }

  "Predicate isThereADeceasedFirstName returns true when there's a deceased first name" in {
    CommonHelper.isThereADeceasedFirstName(CommonBuilder.buildRegistrationDetails copy(
        deceasedDetails = Some(CommonBuilder.buildDeceasedDetails)
      ), "") shouldBe true
  }

  "Predicate isThereADeceasedFirstName returns false when there's no deceased first name" in {
    CommonHelper.isThereADeceasedFirstName(CommonBuilder.buildRegistrationDetails copy(
      deceasedDetails = Some(CommonBuilder.buildDeceasedDetails.copy(firstName = None))
      ), "") shouldBe false
  }

  "Predicate isDeceasedAddressQuestionAnswered returns true when deceased address " +
    "question answered " in {
    CommonHelper.isDeceasedAddressQuestionAnswered(CommonBuilder.buildRegistrationDetails copy(
        deceasedDetails = Some(CommonBuilder.buildDeceasedDetails)
      ), "") shouldBe true
  }

  "Predicate isDeceasedAddressQuestionAnswered returns false when deceased address question " +
    "not answered " in {
    CommonHelper.isDeceasedAddressQuestionAnswered(CommonBuilder.buildRegistrationDetails copy(
      deceasedDetails = Some(CommonBuilder.buildDeceasedDetails.copy(isAddressInUK = None))
      ), "") shouldBe false
  }

  "Predicate isThereADeceasedAddress returns true when there is a deceased address" in {
    CommonHelper.isThereADeceasedAddress(CommonBuilder.buildRegistrationDetails copy(
        deceasedDetails = Some(CommonBuilder.buildDeceasedDetails)
      ), "") shouldBe true
  }

  "Predicate isThereADeceasedAddress returns false when there is no deceased address" in {
    CommonHelper.isThereADeceasedAddress(CommonBuilder.buildRegistrationDetails copy(
      deceasedDetails = Some(CommonBuilder.buildDeceasedDetails.copy(ukAddress = None))
      ), "") shouldBe false
  }

  "Predicate isApplicantApplyingForProbateQuestionAnswered returns true when applicant probate " +
    "question answered" in {
    CommonHelper.isApplicantApplyingForProbateQuestionAnswered(CommonBuilder.buildRegistrationDetails copy(
        applicantDetails = Some(CommonBuilder.buildApplicantDetails)
      ), "") shouldBe true
  }

  "Predicate isApplicantApplyingForProbateQuestionAnswered returns false when applicant probate " +
    "question is not answered" in {
    CommonHelper.isApplicantApplyingForProbateQuestionAnswered(CommonBuilder.buildRegistrationDetails copy(
      applicantDetails = Some(CommonBuilder.buildApplicantDetails.copy(isApplyingForProbate = None))
      ), "") shouldBe false
  }

  "Predicate isThereAnApplicantProbateLocation returns true when there is an applicant probate location " in {
    CommonHelper.isThereAnApplicantProbateLocation(CommonBuilder.buildRegistrationDetails copy(
      applicantDetails = Some(CommonBuilder.buildApplicantDetails)
      ), "") shouldBe true
  }

  "Predicate isThereAnApplicantProbateLocation returns false when there is no applicant probate" +
    " location selected " in {
    CommonHelper.isThereAnApplicantProbateLocation(CommonBuilder.buildRegistrationDetails copy(
      applicantDetails = Some(CommonBuilder.buildApplicantDetails.copy(country = None))
      ), "") shouldBe false
  }

  "Predicate isThereAnApplicantPhoneNo returns true when there is an applicant phone no " in {
    CommonHelper.isThereAnApplicantPhoneNo(CommonBuilder.buildRegistrationDetails copy(
      applicantDetails = Some(CommonBuilder.buildApplicantDetails)
      ), "") shouldBe true
  }

  "Predicate isThereAnApplicantPhoneNo returns false when there is no applicant phone no " in {
    CommonHelper.isThereAnApplicantPhoneNo(CommonBuilder.buildRegistrationDetails copy(
      applicantDetails = Some(CommonBuilder.buildApplicantDetails.copy(phoneNo = None))
      ), "") shouldBe false
  }

  "Predicate isThereAnApplicantAddress returns true when there is an applicant address " in {
    CommonHelper.isThereAnApplicantAddress(CommonBuilder.buildRegistrationDetails copy(
      applicantDetails = Some(CommonBuilder.buildApplicantDetails)
      ), "") shouldBe true
  }

  "Predicate isThereAnApplicantAddress returns false when there is no applicant address " in {
    CommonHelper.isThereAnApplicantAddress(CommonBuilder.buildRegistrationDetails copy(
      applicantDetails = Some(CommonBuilder.buildApplicantDetails.copy(ukAddress = None))
      ), "") shouldBe false
  }

  "Predicate isApplicantOthersApplyingForProbateQuestionAnsweredYes returns true when" +
    " applicant others applying for probate question answered" in {
    CommonHelper.isApplicantOthersApplyingForProbateQuestionAnsweredYes(
      CommonBuilder.buildRegistrationDetails copy(
      areOthersApplyingForProbate = Some(true)
      ), "") shouldBe true
  }

  "Predicate isApplicantOthersApplyingForProbateQuestionAnsweredYes returns false when" +
    " applicant others applying for probate question is not answered" in {
    CommonHelper.isApplicantOthersApplyingForProbateQuestionAnsweredYes(
      CommonBuilder.buildRegistrationDetails copy(
      areOthersApplyingForProbate = None
      ), "") shouldBe false
  }

  "Predicate isApplicantOthersApplyingForProbateQuestionAnsweredYes returns false when" +
    " applicant others applying for probate question is value is selected as No" in {
    CommonHelper.isApplicantOthersApplyingForProbateQuestionAnsweredYes(
      CommonBuilder.buildRegistrationDetails copy(
      areOthersApplyingForProbate = Some(false)
      ), "") shouldBe false
  }

 "aggregateOfSeqOfOption returns Some(false) where at least one element is Some(false)" in {
    val seqList = Seq(Some(true),Some(false),None)
    val result = CommonHelper.aggregateOfSeqOfOption(seqList)
    result shouldBe Some(false)
  }

  "aggregateOfSeqOfOption returns Some(false) where at least one element is None" in {
    val seqList = Seq(Some(true),None,None)
    val result = CommonHelper.aggregateOfSeqOfOption(seqList)
    result shouldBe Some(false)
  }

  "aggregateOfSeqOfOption returns Some(true) where all element are Some(true)" in {
    val seqList = Seq(Some(true),Some(true),Some(true))
    val result = CommonHelper.aggregateOfSeqOfOption(seqList)
    result shouldBe Some(true)
  }

  "aggregateOfSeqOfOption returns None where all element are None" in {
    val seqList = Seq(None, None, None)
    val result = CommonHelper.aggregateOfSeqOfOption(seqList)
    result shouldBe None
  }

  "aggregateOfSeqOfOptionDecimal returns the correct optional value" in {
    val seqList = Seq(Some(BigDecimal(12)), Some(BigDecimal(10)), None)
    val result = CommonHelper.aggregateOfSeqOfOptionDecimal(seqList)
    result shouldBe Some(BigDecimal(22))
  }

  "aggregateOfSeqOfOptionDecimal returns None when all values are None" in {
    val seqList = Seq(None, None, None)
    val result = CommonHelper.aggregateOfSeqOfOptionDecimal(seqList)
    result shouldBe None
  }

  "isExemptionsCompleted"  should {
    val regDetailsMarried = CommonBuilder.buildRegistrationDetails4
    val regDetailsWidowed = CommonBuilder.buildRegistrationDetails5


    "return true when Deceased is Married and all exemptions have been completed" in {
      val appDetails = CommonBuilder.buildApplicationDetails.copy(
        allExemptions = Some(CommonBuilder.buildAllExemptions.copy(
          partner = Some(CommonBuilder.buildPartnerExemption),
          charity = Some(BasicExemptionElement(Some(true))),
          qualifyingBody = Some(BasicExemptionElement(Some(false))))),
        charities = Seq(CommonBuilder.buildCharity.copy(
          Some("1"),Some("testCharity"),Some("123456"), Some(BigDecimal(80000))))
      )

      CommonHelper.isExemptionsCompleted(regDetailsMarried, appDetails) shouldBe true
    }

    "return false when Deceased is Married but all exemptions have not been completed" in {
      val appDetails = CommonBuilder.buildApplicationDetails.copy(
        allExemptions = Some(CommonBuilder.buildAllExemptions.copy(
          partner = Some(CommonBuilder.buildPartnerExemption),
          charity = Some(BasicExemptionElement(Some(true))),
          qualifyingBody = Some(BasicExemptionElement(Some(true))))),
        charities = Seq(CommonBuilder.buildCharity.copy(
          Some("1"),Some("testCharity"),Some("123456"), Some(BigDecimal(80000))))
      )

      CommonHelper.isExemptionsCompleted(regDetailsMarried, appDetails) shouldBe false
    }

    "return true when Deceased's marital status is other than Married and all exemptions have been completed" in {

      val appDetails = CommonBuilder.buildApplicationDetails.copy(
        allExemptions = Some(CommonBuilder.buildAllExemptions.copy(
          partner = None,
          charity = Some(BasicExemptionElement(Some(true))),
          qualifyingBody = Some(BasicExemptionElement(Some(false))))),
        charities = Seq(CommonBuilder.buildCharity.copy(
          Some("1"),Some("testCharity"),Some("123456"), Some(BigDecimal(80000))))
      )

      CommonHelper.isExemptionsCompleted(regDetailsWidowed, appDetails) shouldBe true
    }

    "return false when Deceased's marital status is other than Married and all exemptions have not been completed" in {
      val appDetails = CommonBuilder.buildApplicationDetails.copy(
        allExemptions = Some(CommonBuilder.buildAllExemptions.copy(
          partner = None,
          charity = Some(BasicExemptionElement(Some(true))),
          qualifyingBody = Some(BasicExemptionElement(Some(true))))),
        charities = Seq(CommonBuilder.buildCharity.copy(
          Some("1"),Some("testCharity"),Some("123456"), Some(BigDecimal(80000))))
      )

      CommonHelper.isExemptionsCompleted(regDetailsWidowed, appDetails) shouldBe false
    }
  }

"getOrZero" should {
    "return BigDecimal(0) if None is given as input" in {
      CommonHelper.getOrZero(None) shouldBe BigDecimal(0)
    }

    "return the correct value if input is BigDecimal value other than 0" in {
      CommonHelper.getOrZero(Some(BigDecimal(100000))) shouldBe BigDecimal(100000)
    }
  }

  "escapeSpace" must {
    "replace space with &nbsp;" in {
      CommonHelper.escapeSpace("first last") shouldBe "first&nbsp;last"
    }
  }

  "numericElements" must {
    "return the blank key if first element is blank" in {
      CommonHelper.convertToNumbers(Seq("", "2", "3"), "1", "2") shouldBe Left("1")
    }

    "return the blank key if second element is blank" in {
      CommonHelper.convertToNumbers(Seq("1", "", "3"), "1", "2") shouldBe Left("1")
    }

    "return the blank key if third element is blank" in {
      CommonHelper.convertToNumbers(Seq("1", "2", " "), "1", "2") shouldBe Left("1")
    }

    "return the invalid characters key if first element has not numeric characters" in {
      CommonHelper.convertToNumbers(Seq("&^", "2", "3"), "1", "2") shouldBe Left("2")
    }

    "return the invalid characters key if second element has not numeric characters" in {
      CommonHelper.convertToNumbers(Seq("1", "£$", "3"), "1", "2") shouldBe Left("2")
    }

    "return the invalid characters key if third element has not numeric characters" in {
      CommonHelper.convertToNumbers(Seq("1", "2", "three"), "1", "2") shouldBe Left("2")
    }

    "return the an integer seq if all numeric elements passed in" in {
      CommonHelper.convertToNumbers(Seq("1", "2", "5"), "1", "2") shouldBe Right(Seq(1,2,5))
    }
  }

  "getDeceasedNameOrDefaultString" must {
    val firstName = "first"
    val lastName = "last"
    val deceasedDetails = CommonBuilder.buildDeceasedDetails.copy(firstName = Some(firstName), lastName = Some(lastName))
    val regDetails = CommonBuilder.buildRegistrationDetails.copy(deceasedDetails = Some(deceasedDetails))

    "return Deceased name where deceased details exists " in {
      CommonHelper.getDeceasedNameOrDefaultString(regDetails) shouldBe firstName+" "+lastName
    }

    "return default string where deceased details does not exists " in {
      val regDetailsWithNODeceasedDetails = regDetails.copy(deceasedDetails = None)
      CommonHelper.getDeceasedNameOrDefaultString(regDetailsWithNODeceasedDetails) shouldBe messagesApi("iht.the.deceased")
    }
  }

  def buildAuthContext(nino:Nino): AuthContext = {
    new AuthContext(
      LoggedInUser(CommonBuilder.firstNameGenerator, None, None, None, CredentialStrength.Strong, ConfidenceLevel.L300 ,""),
      Principal(None, Accounts(iht = Some(IhtAccount("", nino)) )),
      None, None, None, None
    )
  }

  "ensureSessionHasNino" must {
    "throw an exception if the authcontext contains no nino" in {
      val nino = NinoBuilder.randomNino
      a[RuntimeException] shouldBe thrownBy {
        CommonHelper.ensureSessionHasNino(new Session(), CommonBuilder.buildAuthContext())
      }
    }

    "add the nino if it is not present in the session" in {
      val nino = NinoBuilder.randomNino
      val result = CommonHelper.ensureSessionHasNino(new Session(), buildAuthContext(nino))
      result.get(Constants.NINO) shouldBe Some(nino.nino)
    }

    "retrieve the nino if the same nino is present in the session" in {
      val nino = NinoBuilder.randomNino
      val session = new Session() + (Constants.NINO -> nino.name)
      val result = CommonHelper.ensureSessionHasNino(session, buildAuthContext(nino))
      result.get(Constants.NINO) shouldBe Some(nino.nino)
    }

    "replace the nino if a different nino is present in the session" in {
      val nino = NinoBuilder.randomNino
      val newNino = NinoBuilder.randomNino
      val session = new Session() + (Constants.NINO -> nino.name)
      val result = CommonHelper.ensureSessionHasNino(session, buildAuthContext(newNino))
      result.get(Constants.NINO) shouldBe Some(newNino.nino)
    }
  }


  "formatCurrencyForInput" must {
    "return a properly formatted number if no trailing zero provided" in {
      val number = "5000.5"
      CommonHelper.formatCurrencyForInput(number) shouldBe "5000.50"
    }

    "return a properly formatted number if two decimal places provided" in {
      val number = "5000.55"
      CommonHelper.formatCurrencyForInput(number) shouldBe "5000.55"
    }

    "return a properly formatted number if no decimal places provided" in {
      val number = "5000"
      CommonHelper.formatCurrencyForInput(number) shouldBe "5000.00"
    }

    "return a properly formatted number if no decimal point provided" in {
      val number = "5000."
      CommonHelper.formatCurrencyForInput(number) shouldBe "5000.00"
    }

    "return a blank string if no number provided" in {
      val number = ""
      CommonHelper.formatCurrencyForInput(number) shouldBe ""
    }

    "return the initial value if number not provided" in {
      val number = "thisIsNotANumber"
      CommonHelper.formatCurrencyForInput(number) shouldBe "thisIsNotANumber"
    }
  }

  "getNinoFromSession" must {
    "return the nino when it is present in the session" in {
      val nino = "CSXXXXX"
      val request = FakeRequest().withSession(Constants.NINO -> nino)
      CommonHelper.getNinoFromSession(request) shouldBe nino
    }

    "return the empty string when nino is not present in the session" in {
      val request = FakeRequest().withSession()
      CommonHelper.getNinoFromSession(request) shouldBe ""
    }
  }
}

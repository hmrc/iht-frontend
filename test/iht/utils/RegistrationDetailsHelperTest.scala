/*
 * Copyright 2019 HM Revenue & Customs
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
import iht.models.RegistrationDetails
import iht.models.application.ApplicationDetails
import iht.models.application.exemptions.BasicExemptionElement
import iht.testhelpers._
import org.scalatest.mock.MockitoSugar
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.test.FakeRequest
import uk.gov.hmrc.play.test.UnitSpec

import scala.collection.immutable.ListMap

class RegistrationDetailsHelperTest extends FakeIhtApp with MockitoSugar {

  val referrerURL = "http://localhost:9070/inheritance-tax/registration/addExecutor"
  val host = "localhost:9070"

  "Must provide the Referrer URL excluding the host" in {

    val request = FakeRequest().withHeaders(("referer", referrerURL), ("host", host))
    val result = CommonHelper.getReferrerPathExcludingHost(request)

    assert(result.equals(
      "/inheritance-tax/registration/addExecutor"), "URL without host is /inheritance-tax/registration/addExecutor")

  }

  "findFirstTrue should return correct item subscript when last of three is true" in {
    val a: (RegistrationDetails, ApplicationDetails, Seq[BigDecimal]) => Boolean = (rd, ad, st) => false
    val b: (RegistrationDetails, ApplicationDetails, Seq[BigDecimal]) => Boolean = (rd, ad, st) => false
    val c: (RegistrationDetails, ApplicationDetails, Seq[BigDecimal]) => Boolean = (rd, ad, st) => true
    val aa = RegistrationDetailsHelper.findFirstTrue(CommonBuilder.buildRegistrationDetails, CommonBuilder.buildApplicationDetails, Seq(BigDecimal(0)),
      ListMap("1" -> a, "2" -> b, "3" -> c))
    aa mustBe Some("3")
  }

  "findFirstTrue should return correct item subscript when second of three is true" in {
    val a: (RegistrationDetails, ApplicationDetails, Seq[BigDecimal]) => Boolean = (rd, ad, st) => false
    val b: (RegistrationDetails, ApplicationDetails, Seq[BigDecimal]) => Boolean = (rd, ad, st) => true
    val c: (RegistrationDetails, ApplicationDetails, Seq[BigDecimal]) => Boolean = (rd, ad, st) => false
    val aa = RegistrationDetailsHelper.findFirstTrue(CommonBuilder.buildRegistrationDetails, CommonBuilder.buildApplicationDetails, Seq(BigDecimal(0)),
      ListMap("1" -> a, "2" -> b, "3" -> c))
    aa mustBe Some("2")
  }

  "findFirstTrue should return correct item subscript when first of three is true" in {
    val a: (RegistrationDetails, ApplicationDetails, Seq[BigDecimal]) => Boolean = (rd, ad, st) => true
    val b: (RegistrationDetails, ApplicationDetails, Seq[BigDecimal]) => Boolean = (rd, ad, st) => false
    val c: (RegistrationDetails, ApplicationDetails, Seq[BigDecimal]) => Boolean = (rd, ad, st) => false
    val aa = RegistrationDetailsHelper.findFirstTrue(CommonBuilder.buildRegistrationDetails, CommonBuilder.buildApplicationDetails, Seq(BigDecimal(0)),
      ListMap("1" -> a, "2" -> b, "3" -> c))
    aa mustBe Some("1")
  }

  "findFirstTrue should return None when none of three is true" in {
    val a: (RegistrationDetails, ApplicationDetails, Seq[BigDecimal]) => Boolean = (rd, ad, st) => false
    val b: (RegistrationDetails, ApplicationDetails, Seq[BigDecimal]) => Boolean = (rd, ad, st) => false
    val c: (RegistrationDetails, ApplicationDetails, Seq[BigDecimal]) => Boolean = (rd, ad, st) => false
    val aa = RegistrationDetailsHelper.findFirstTrue(CommonBuilder.buildRegistrationDetails, CommonBuilder.buildApplicationDetails, Seq(BigDecimal(0)),
      ListMap("1" -> a, "2" -> b, "3" -> c))
    aa mustBe None
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

    val aa = RegistrationDetailsHelper.findFirstTrue(
      CommonBuilder.buildRegistrationDetails, CommonBuilder.buildApplicationDetails, Seq(BigDecimal(0)),
      ListMap("1" -> a, "2" -> b, "3" -> c, "4" -> d, "5" -> e, "6" -> f, "7" -> g, "8" -> h))
    aa mustBe Some("4")
  }


  "findFirstTrue should return None when sequence is empty" in {
    val aa = RegistrationDetailsHelper.findFirstTrue(
      CommonBuilder.buildRegistrationDetails, CommonBuilder.buildApplicationDetails, Seq(BigDecimal(0)), ListMap())
    aa mustBe None
  }

  "getOrException throws exception if Registration Details None passed in with suitable message" in {
    val aa: Option[RegistrationDetails] = None
    intercept[RuntimeException] {
      RegistrationDetailsHelper.getOrExceptionNoRegistration(aa)
    }.getMessage must include("No registration details")
  }

  "isExemptionsCompleted" should {
    val regDetailsMarried = CommonBuilder.buildRegistrationDetails4
    val regDetailsWidowed = CommonBuilder.buildRegistrationDetails5


    "return true when Deceased is Married and all exemptions have been completed" in {
      val appDetails = CommonBuilder.buildApplicationDetails.copy(
        allExemptions = Some(CommonBuilder.buildAllExemptions.copy(
          partner = Some(CommonBuilder.buildPartnerExemption),
          charity = Some(BasicExemptionElement(Some(true))),
          qualifyingBody = Some(BasicExemptionElement(Some(false))))),
        charities = Seq(CommonBuilder.buildCharity.copy(
          Some("1"), Some("testCharity"), Some("123456"), Some(BigDecimal(80000))))
      )

      RegistrationDetailsHelper.isExemptionsCompleted(regDetailsMarried, appDetails) mustBe true
    }

    "return false when Deceased is Married but all exemptions have not been completed" in {
      val appDetails = CommonBuilder.buildApplicationDetails.copy(
        allExemptions = Some(CommonBuilder.buildAllExemptions.copy(
          partner = Some(CommonBuilder.buildPartnerExemption),
          charity = Some(BasicExemptionElement(Some(true))),
          qualifyingBody = Some(BasicExemptionElement(Some(true))))),
        charities = Seq(CommonBuilder.buildCharity.copy(
          Some("1"), Some("testCharity"), Some("123456"), Some(BigDecimal(80000))))
      )

      RegistrationDetailsHelper.isExemptionsCompleted(regDetailsMarried, appDetails) mustBe false
    }

    "return true when Deceased's marital status is other than Married and all exemptions have been completed" in {

      val appDetails = CommonBuilder.buildApplicationDetails.copy(
        allExemptions = Some(CommonBuilder.buildAllExemptions.copy(
          partner = None,
          charity = Some(BasicExemptionElement(Some(true))),
          qualifyingBody = Some(BasicExemptionElement(Some(false))))),
        charities = Seq(CommonBuilder.buildCharity.copy(
          Some("1"), Some("testCharity"), Some("123456"), Some(BigDecimal(80000))))
      )

      RegistrationDetailsHelper.isExemptionsCompleted(regDetailsWidowed, appDetails) mustBe true
    }

    "return false when Deceased's marital status is other than Married and all exemptions have not been completed" in {
      val appDetails = CommonBuilder.buildApplicationDetails.copy(
        allExemptions = Some(CommonBuilder.buildAllExemptions.copy(
          partner = None,
          charity = Some(BasicExemptionElement(Some(true))),
          qualifyingBody = Some(BasicExemptionElement(Some(true))))),
        charities = Seq(CommonBuilder.buildCharity.copy(
          Some("1"), Some("testCharity"), Some("123456"), Some(BigDecimal(80000))))
      )

      RegistrationDetailsHelper.isExemptionsCompleted(regDetailsWidowed, appDetails) mustBe false
    }
  }
}

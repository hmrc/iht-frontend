/*
 * Copyright 2020 HM Revenue & Customs
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
import iht.config.AppConfig
import iht.models.application.ApplicationDetails
import iht.models.{DeceasedDetails, RegistrationDetails}
import iht.testhelpers.CommonBuilder
import org.scalatestplus.mockito.MockitoSugar
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.test.FakeRequest

/**
 *
 * This Class contains the Unit Tests for iht.utils.CommonHelper
 */
class CommonHelperTest extends FakeIhtApp with MockitoSugar with I18nSupport {

  implicit val messagesApi: MessagesApi = app.injector.instanceOf[MessagesApi]
  implicit val mockAppConfig: AppConfig = app.injector.instanceOf[AppConfig]

  val referrerURL="http://localhost:9070/inheritance-tax/registration/addExecutor"
  val host="localhost:9070"

  "Must provide the Referrer URL excluding the host" in {

    val request = FakeRequest().withHeaders(("referer", referrerURL), ("host", host))
    val result = CommonHelper.getReferrerPathExcludingHost(request)

    assert(result.equals(
      "/inheritance-tax/registration/addExecutor"),"URL without host is /inheritance-tax/registration/addExecutor")

  }

  "getOrException throws exception if String None passed in with suitable message" in {
    val aa:Option[String] = None
    intercept[RuntimeException] {
      CommonHelper.getOrException(aa)
    }.getMessage must include ("No element found")
  }

  "getOrException throws exception if Application Details None passed in with suitable message" in {
    val aa:Option[ApplicationDetails] = None
    intercept[RuntimeException] {
      CommonHelper.getOrExceptionNoApplication(aa)
    }.getMessage must include ("No application details")
  }

  "getOrException throws exception if Application Details saved None passed in with suitable message" in {
    val aa:Option[ApplicationDetails] = None
    intercept[RuntimeException] {
      CommonHelper.getOrExceptionApplicationNotSaved(aa)
    }.getMessage must include ("Unable to save application")
  }

  "getOrException throws exception if IHT Ref None passed in with suitable message" in {
    val aa:Option[String] = None
    intercept[RuntimeException] {
      CommonHelper.getOrExceptionNoIHTRef(aa)
    }.getMessage must include ("No IHT Reference")
  }

 "aggregateOfSeqOfOption returns Some(false) where at least one element is Some(false)" in {
    val seqList = Seq(Some(true),Some(false),None)
    val result = CommonHelper.aggregateOfSeqOfOption(seqList)
    result mustBe Some(false)
  }

  "aggregateOfSeqOfOption returns Some(false) where at least one element is None" in {
    val seqList = Seq(Some(true),None,None)
    val result = CommonHelper.aggregateOfSeqOfOption(seqList)
    result mustBe Some(false)
  }

  "aggregateOfSeqOfOption returns Some(true) where all element are Some(true)" in {
    val seqList = Seq(Some(true),Some(true),Some(true))
    val result = CommonHelper.aggregateOfSeqOfOption(seqList)
    result mustBe Some(true)
  }

  "aggregateOfSeqOfOption returns None where all element are None" in {
    val seqList = Seq(None, None, None)
    val result = CommonHelper.aggregateOfSeqOfOption(seqList)
    result mustBe None
  }

  "aggregateOfSeqOfOptionDecimal returns the correct optional value" in {
    val seqList = Seq(Some(BigDecimal(12)), Some(BigDecimal(10)), None)
    val result = CommonHelper.aggregateOfSeqOfOptionDecimal(seqList)
    result mustBe Some(BigDecimal(22))
  }

  "aggregateOfSeqOfOptionDecimal returns None when all values are None" in {
    val seqList = Seq(None, None, None)
    val result = CommonHelper.aggregateOfSeqOfOptionDecimal(seqList)
    result mustBe None
  }

"getOrZero" must {
    "return BigDecimal(0) if None is given as input" in {
      CommonHelper.getOrZero(None) mustBe BigDecimal(0)
    }

    "return the correct value if input is BigDecimal value other than 0" in {
      CommonHelper.getOrZero(Some(BigDecimal(100000))) mustBe BigDecimal(100000)
    }
  }

  "numericElements" must {
    "return the blank key if first element is blank" in {
      CommonHelper.convertToNumbers(Seq("", "2", "3"), "1", "2") mustBe Left("1")
    }

    "return the blank key if second element is blank" in {
      CommonHelper.convertToNumbers(Seq("1", "", "3"), "1", "2") mustBe Left("1")
    }

    "return the blank key if third element is blank" in {
      CommonHelper.convertToNumbers(Seq("1", "2", " "), "1", "2") mustBe Left("1")
    }

    "return the invalid characters key if first element has not numeric characters" in {
      CommonHelper.convertToNumbers(Seq("&^", "2", "3"), "1", "2") mustBe Left("2")
    }

    "return the invalid characters key if second element has not numeric characters" in {
      CommonHelper.convertToNumbers(Seq("1", "£$", "3"), "1", "2") mustBe Left("2")
    }

    "return the invalid characters key if third element has not numeric characters" in {
      CommonHelper.convertToNumbers(Seq("1", "2", "three"), "1", "2") mustBe Left("2")
    }

    "return the an integer seq if all numeric elements passed in" in {
      CommonHelper.convertToNumbers(Seq("1", "2", "5"), "1", "2") mustBe Right(Seq(1,2,5))
    }
  }

  "formatCurrencyForInput" must {
    "return a properly formatted number if no trailing zero provided" in {
      val number = "5000.5"
      CommonHelper.formatCurrencyForInput(number) mustBe "5000.50"
    }

    "return a properly formatted number if two decimal places provided" in {
      val number = "5000.55"
      CommonHelper.formatCurrencyForInput(number) mustBe "5000.55"
    }

    "return a properly formatted number if no decimal places provided" in {
      val number = "5000"
      CommonHelper.formatCurrencyForInput(number) mustBe "5000.00"
    }

    "return a properly formatted number if no decimal point provided" in {
      val number = "5000."
      CommonHelper.formatCurrencyForInput(number) mustBe "5000.00"
    }

    "return a blank string if no number provided" in {
      val number = ""
      CommonHelper.formatCurrencyForInput(number) mustBe ""
    }

    "return the initial value if number not provided" in {
      val number = "thisIsNotANumber"
      CommonHelper.formatCurrencyForInput(number) mustBe "thisIsNotANumber"
    }
  }

    "" must {
      val deceasedDetails = CommonBuilder.buildDeceasedDetails
      val singleDeceasedDetails = CommonBuilder.buildDeceasedDetailsSingle
      val applicantDetails = CommonBuilder.buildApplicantDetails

      val buildDeceasedDetailsNone = DeceasedDetails(None)

      "have no registration details" in {
        val registrationDetails = RegistrationDetails(None, None, None)
        the[RuntimeException] thrownBy CommonHelper.mapMaritalStatus(registrationDetails) must have message "No element found"
      }

      "have no marital status" in {
        val registrationDetails = RegistrationDetails(None, Some(applicantDetails), Some(buildDeceasedDetailsNone))
        CommonHelper.mapMaritalStatus(registrationDetails) mustBe "notMarried"
      }

      "have married status" in {
        val registrationDetails = RegistrationDetails(None, Some(applicantDetails), Some(deceasedDetails))
        CommonHelper.mapMaritalStatus(registrationDetails) mustBe "married"
      }

      "have not married status" in {
        val registrationDetails = RegistrationDetails(None, Some(applicantDetails), Some(singleDeceasedDetails))
        CommonHelper.mapMaritalStatus(registrationDetails) mustBe "notMarried"
      }

    }

}

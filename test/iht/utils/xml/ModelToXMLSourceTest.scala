/*
 * Copyright 2016 HM Revenue & Customs
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

package iht.utils.xml

import iht.resources.{IhtReturn, RegistrationDetailsReturn}
import iht.testhelpers.CommonBuilder
import iht.testhelpers.IHTReturnTestHelper._
import org.joda.time.LocalDate
import org.scalatest.mock.MockitoSugar
import uk.gov.hmrc.play.test.UnitSpec

import scala.xml.XML

class ModelToXMLSourceTest extends UnitSpec with MockitoSugar with iht.FakeIhtApp {
  "getIHTReturnXMLSource" must {

    "return correct XML corresponding to a fully completed IHT Return object" in {
      val ihtReturn = buildIHTReturnCorrespondingToApplicationDetailsAllFields(new LocalDate(2016, 6, 13), "111222333444")
      val result = ModelToXMLSource.getXMLSource(ihtReturn)
      val printer = new scala.xml.PrettyPrinter(80, 2)
      val xmlActual = printer.format(XML.loadString(result))

      val survivingSpouse = ihtReturn.deceased.get.survivingSpouse.get
      val deceasedSpouse = ihtReturn.deceased.get.transferOfNilRateBand.get.deceasedSpouses.toList.head.spouse.get
      val xmlExpected = printer.format(XML.loadString(IhtReturn(survivingSpouse.firstName.get, survivingSpouse.lastName.get,
        survivingSpouse.mainAddress.get.postalCode, survivingSpouse.nino.get, deceasedSpouse.firstName.get,
        deceasedSpouse.lastName.get, deceasedSpouse.mainAddress.get.postalCode, deceasedSpouse.nino.get).data))

      xmlActual shouldBe xmlExpected
    }

    "return correct XML corresponding to a fully completed RegistrationDetails object" in {
      val regDetails = CommonBuilder.buildRegistrationDetails1
      val result: String = ModelToXMLSource.getXMLSource(regDetails)
      val printer = new scala.xml.PrettyPrinter(80, 2)
      val xmlActual = printer.format(XML.loadString(result))

      val xmlExpected = printer.format(XML.loadString(RegistrationDetailsReturn(regDetails.applicantDetails.get,
        regDetails.deceasedDetails.get, regDetails.coExecutors, CommonBuilder.DefaultAcknowledgmentReference).data))

      xmlActual shouldBe xmlExpected
    }

    "return correct XML corresponding to a fully completed RegistrationDetails object concatenated to iht return object" in {
      val regDetails = CommonBuilder.buildRegistrationDetails1
      val ihtReturn = buildIHTReturnCorrespondingToApplicationDetailsAllFields(new LocalDate(2016, 6, 13), "111222333444")
      val result: Array[Byte] = ModelToXMLSource.getPostSubmissionDetailsXMLSource(regDetails, ihtReturn)
      result.length > 13000 shouldBe true
    }

  }
}

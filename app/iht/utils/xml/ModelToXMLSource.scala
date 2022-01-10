/*
 * Copyright 2022 HM Revenue & Customs
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

import iht.models.RegistrationDetails
import iht.models.application.ApplicationDetails
import iht.models.des.ihtReturn.IHTReturn
import IHTReturn._
import org.json.{JSONObject, XML}
import play.api.libs.json.Json

/**
 * Created by david-beer on 03/06/16.
 */

object ModelToXMLSource extends ModelToXMLSource

trait ModelToXMLSource {
  private val XMLRootIhtReturnSummary = "IHTReturnSummary"
  private val XMLRootIhtReturn = "IHTReturn"
  private val XMLRootRegistrationDetails = "RegistrationDetails"
  private val XMLRootPostSubmission = "PostSubmissionXML"
  private val XMLRootClearanceCertificate = "ClearanceCertificateXML"
  private val XMLRootPreSubmission = "PreSubmissionXML"
  private val XMLRootApplicationDetails = "ApplicationDetails"

  def getClearanceCertificateXMLSource(registrationDetails: RegistrationDetails): Array[Byte] = {
    val regDetailsXMLString = getXMLSource(registrationDetails)
    val clearanceXML = s"<$XMLRootClearanceCertificate>" + regDetailsXMLString + s"</$XMLRootClearanceCertificate>"
    clearanceXML.getBytes
  }

  def getPostSubmissionDetailsXMLSource(registrationDetails: RegistrationDetails, ihtReturn: IHTReturn, applicationDetails: ApplicationDetails): Array[Byte] = {
    val regDetailsXMLString = getXMLSource(registrationDetails)
    val ihtReturnSummaryXMLString = getXMLSource(ihtReturn, XMLRootIhtReturnSummary)
    val ihtReturnXMLString = getXMLSource(ihtReturn, XMLRootIhtReturn)
    val applicationDetailsXML = getXMLSource(applicationDetails)
    val postSubmissionXML = s"<$XMLRootPostSubmission>" + regDetailsXMLString + ihtReturnSummaryXMLString + applicationDetailsXML +
      ihtReturnXMLString + s"</$XMLRootPostSubmission>"
    postSubmissionXML.getBytes
  }

  def getPreSubmissionXMLSource(registrationDetails: RegistrationDetails, applicationDetails: ApplicationDetails): Array[Byte] = {
    val regDetailsXML = getXMLSource(registrationDetails)
    val applicationDetailsXML = getXMLSource(applicationDetails)
    val preSubmissionXML = s"<$XMLRootPreSubmission>" + regDetailsXML + applicationDetailsXML + s"</$XMLRootPreSubmission>"
    preSubmissionXML.getBytes
  }

  def getXMLSource(applicationDetails: ApplicationDetails): String =
    s"<$XMLRootApplicationDetails>" + XML.toString(new JSONObject(Json.toJson(applicationDetails).toString())) + s"</$XMLRootApplicationDetails>"

  def getXMLSource(ihtReturn: IHTReturn, section: String): String = {
    val x: String = Json.toJson(sortByGiftDate(ihtReturn)).toString()
    s"<$section>" + XML.toString(new JSONObject(x)) + s"</$section>"
  }

  def getXMLSource(registrationDetails: RegistrationDetails): String =
    s"<$XMLRootRegistrationDetails>" + XML.toString(new JSONObject(Json.toJson(registrationDetails).toString())) + s"</$XMLRootRegistrationDetails>"
}

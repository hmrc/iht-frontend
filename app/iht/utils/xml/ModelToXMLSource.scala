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

package iht.utils.xml

import iht.models.RegistrationDetails
import iht.models.application.ApplicationDetails
import models.des.iht_return.IHTReturn
import models.des.iht_return.IHTReturn._
import org.json.{JSONObject, XML}
import play.api.libs.json.Json

/**
 * Created by david-beer on 03/06/16.
 */

object ModelToXMLSource extends ModelToXMLSource

trait ModelToXMLSource {
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

  def getPostSubmissionDetailsXMLSource(registrationDetails: RegistrationDetails, ihtReturn: IHTReturn): Array[Byte] = {
    val regDetailsXMLString = getXMLSource(registrationDetails)
    val ihtReturnXMLString = getXMLSource(ihtReturn)
    val postSubmissionXML = s"<$XMLRootPostSubmission>" + regDetailsXMLString + ihtReturnXMLString + s"</$XMLRootPostSubmission>"

//    val pp = new scala.xml.PrettyPrinter(80, 2)
//    Logger.debug("Post-submission generated XML: " + pp.format(scala.xml.XML.loadString(postSubmissionXML)))
    postSubmissionXML.getBytes
  }

  def getPreSubmissionXMLSource(registrationDetails: RegistrationDetails, applicationDetails: ApplicationDetails): Array[Byte] = {
    val regDetailsXML = getXMLSource(registrationDetails)
    val applicationDetailsXML = getXMLSource(applicationDetails)
    println( "\n***********" + applicationDetailsXML)
    val preSubmissionXML = s"<$XMLRootPreSubmission>" + regDetailsXML + applicationDetailsXML + s"</$XMLRootPreSubmission>"

    preSubmissionXML.getBytes
  }

  def getXMLSource(applicationDetails: ApplicationDetails): String =
    s"<$XMLRootApplicationDetails>" + XML.toString(new JSONObject(Json.toJson(applicationDetails).toString())) + s"</$XMLRootApplicationDetails>"

  def getXMLSource(ihtReturn: IHTReturn): String =
    s"<$XMLRootIhtReturn>" + XML.toString(new JSONObject(Json.toJson(sortByGiftDate(ihtReturn)).toString())) + s"</$XMLRootIhtReturn>"


  def getXMLSource(registrationDetails: RegistrationDetails): String =
    s"<$XMLRootRegistrationDetails>" + XML.toString(new JSONObject(Json.toJson(registrationDetails).toString())) + s"</$XMLRootRegistrationDetails>"
}

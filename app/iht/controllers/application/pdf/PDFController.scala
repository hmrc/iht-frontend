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

package iht.controllers.application.pdf

import iht.config.AppConfig
import iht.connector.IhtConnectors
import iht.constants.{Constants, IhtProperties}
import iht.controllers.application.ApplicationController
import iht.models.RegistrationDetails
import iht.models.des.ihtReturn.IHTReturn
import iht.utils.pdf._
import iht.utils.{CommonHelper, DeclarationHelper, StringHelper}
import javax.inject.{Inject, Singleton}
import play.api.Logger
import play.api.i18n.{I18nSupport, Messages, MessagesApi}
import uk.gov.hmrc.auth.core.AuthConnector
import uk.gov.hmrc.auth.core.PlayAuthConnector
import uk.gov.hmrc.auth.core.retrieve.v2.Retrievals.{nino => ninoRetrieval}
import uk.gov.hmrc.http.HeaderCarrier

import scala.concurrent.Future

/**
  * Created by dbeer on 14/08/15.
  */

class PDFControllerImpl @Inject()(val messagesApi: MessagesApi) extends PDFController with IhtConnectors

trait PDFController extends ApplicationController with I18nSupport {

  private def pdfHeaders(fileName: String): Seq[(String, String)] =
    IhtProperties.pdfStaticHeaders :+ Tuple2(CONTENT_DISPOSITION, "inline; filename=\"" + fileName + "\"")

  def onPreSubmissionPDF = authorisedForIhtWithRetrievals(ninoRetrieval) { userNino =>
    implicit request => {
      Logger.info("Generating Summary PDF")
      val messages = messagesApi.preferred(request)
      val fileName = s"${messages("iht.inheritanceTaxEstateReport")}.pdf"
      withApplicationDetails(userNino) { regDetails =>
        applicationDetails =>
          val pdfByteArray = XmlFoToPDF.createPreSubmissionPDF(
            regDetails,
            applicationDetails,
            DeclarationHelper.getDeclarationType(applicationDetails), messages)
          Future.successful(Ok(pdfByteArray).withHeaders(pdfHeaders(fileName): _*))
      }
    }
  }

  def onClearancePDF = authorisedForIhtWithRetrievals(ninoRetrieval) { userNino =>
    implicit request => {
      Logger.info("Generating Clearance PDF")
      val messages = messagesApi.preferred(request)
      cachingConnector.getSingleValue(Constants.PDFIHTReference).flatMap { optionIHTReference =>
        val ihtReference = CommonHelper.getOrException(optionIHTReference)
        val fileName = s"${messages("pdf.clearanceCertificate.title")}.pdf"
        val nino = StringHelper.getNino(userNino)
        ihtConnector.getCaseDetails(nino, ihtReference).flatMap(registrationDetails =>
          getSubmittedApplicationDetails(nino,
            registrationDetails, messages) map {
            case Some(ihtReturn) =>
              val pdfByteArray = XmlFoToPDF.createClearancePDF(registrationDetails, CommonHelper.getOrException(
                ihtReturn.declaration, "No declaration found").declarationDate.getOrElse(
                throw new RuntimeException("Declaration Date not available")), messages)
              Ok(pdfByteArray).withHeaders(pdfHeaders(fileName): _*)
            case _ =>
              internalServerError
          }
        )
      }
    }
  }

  def onPostSubmissionPDF = authorisedForIhtWithRetrievals(ninoRetrieval) { userNino =>
    implicit request => {
      Logger.info("Generating Application PDF")
      val messages = messagesApi.preferred(request)
      cachingConnector.getSingleValue(Constants.PDFIHTReference).flatMap {
        case Some(ihtReference) => {
          val fileName = s"${messages("iht.inheritanceTaxEstateReport")}.pdf"
          val nino = StringHelper.getNino(userNino)
          ihtConnector.getCaseDetails(nino, ihtReference).flatMap(regDetails =>
            getSubmittedApplicationDetails(nino, regDetails, messages) map {
              case Some(ihtReturn) =>
                val pdfByteArray = XmlFoToPDF.createPostSubmissionPDF(regDetails, ihtReturn, messages)
                Ok(pdfByteArray).withHeaders(pdfHeaders(fileName): _*)
              case _ =>
                internalServerError
            }
          )
        }
        case _ => Future.successful(Redirect(iht.controllers.estateReports.routes.YourEstateReportsController.onPageLoad()))
      }
    }
  }

  private def internalServerError = {
    Logger.warn("There has been a problem retrieving the details for the Application PDF. Redirecting" +
      " to internalServerError")
    InternalServerError("There has been a problem retrieving the details for the Application PDF")
  }

  /**
    * Retrieves IHTReturn for given ihtRef and returnId
    */
  private def getSubmittedApplicationDetails(nino: String,
                                             registrationDetails:RegistrationDetails,
                                             messages: Messages)
                                            (implicit headerCarrier: HeaderCarrier): Future[Option[IHTReturn]] = {
    val ihtReference = CommonHelper.getOrExceptionNoIHTRef(registrationDetails.ihtReference)
    val returnId = registrationDetails.updatedReturnId
    ihtConnector.getSubmittedApplicationDetails(nino, ihtReference, returnId) map {
      case None =>
        Logger.warn("IhtReturn details not found")
        None
      case Some(ihtReturn) =>
        Logger.info("IhtReturn details have been successfully retrieved ")
        Some(PdfFormatter.transform(ihtReturn, registrationDetails, messages))
    }
  }
}

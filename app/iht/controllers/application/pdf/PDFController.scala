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

package iht.controllers.application.pdf

import iht.config.FrontendAuthConnector
import iht.connector.{CachingConnector, IhtConnector}
import iht.constants.{Constants, IhtProperties}
import iht.controllers.application.ApplicationController
import iht.controllers.auth.IhtActions
import iht.models.RegistrationDetails
import iht.utils.pdf._
import iht.utils.{CommonHelper, DeclarationHelper}
import models.des.iht_return.IHTReturn
import play.api.Logger
import play.api.i18n.Messages
import play.api.i18n.Messages.Implicits._
import play.api.Play.current
import uk.gov.hmrc.play.frontend.auth.connectors.AuthConnector
import uk.gov.hmrc.play.http.HeaderCarrier

import scala.concurrent.Future

/**
  * Created by dbeer on 14/08/15.
  */
object PDFController extends PDFController {
  lazy val cachingConnector = CachingConnector
  lazy val authConnector: AuthConnector = FrontendAuthConnector
  lazy val ihtConnector = IhtConnector
  lazy val xmlFoToPDF = XmlFoToPDF
}

trait PDFController extends ApplicationController with IhtActions {

  def cachingConnector: CachingConnector

  def ihtConnector: IhtConnector

  def xmlFoToPDF: XmlFoToPDF

  private def pdfHeaders(fileName: String): Seq[(String, String)] =
    IhtProperties.pdfStaticHeaders :+ Tuple2(CONTENT_DISPOSITION, "inline; filename=\"" + fileName + "\"")

  def onPreSubmissionPDF = authorisedForIht {
    implicit user =>
      implicit request => {
        Logger.info("Generating Summary PDF")
        val fileName = s"${Messages("iht.inheritanceTaxEstateReport")}.pdf"
        withApplicationDetails { regDetails =>
          applicationDetails =>
            val pdfByteArray = xmlFoToPDF.createPreSubmissionPDF(
              regDetails,
              applicationDetails,
              DeclarationHelper.getDeclarationType(applicationDetails)
            )
            Future.successful(Ok(pdfByteArray).withHeaders(pdfHeaders(fileName): _*))
        }
      }
  }

  def onClearancePDF = authorisedForIht {
    implicit user =>
      implicit request => {
        Logger.info("Generating Clearance PDF")

        cachingConnector.getSingleValue(Constants.PDFIHTReference).flatMap { optionIHTReference =>
          val ihtReference = CommonHelper.getOrException(optionIHTReference)
          val fileName = s"${Messages("pdf.clearanceCertificate.title")}.pdf"
          val nino = CommonHelper.getNino(user)
          ihtConnector.getCaseDetails(nino, ihtReference).flatMap(registrationDetails =>
            getSubmittedApplicationDetails(nino,
              registrationDetails) map {
              case Some(ihtReturn) =>
                val pdfByteArray = xmlFoToPDF.createClearancePDF(registrationDetails, CommonHelper.getOrException(
                  ihtReturn.declaration, "No declaration found").declarationDate.getOrElse(
                  throw new RuntimeException("Declaration Date not available")))
                Ok(pdfByteArray).withHeaders(pdfHeaders(fileName): _*)
              case _ =>
                internalServerError
            }
          )
        }
      }
  }

  def onPostSubmissionPDF = authorisedForIht {
    implicit user =>
      implicit request => {
        Logger.info("Generating Application PDF")
        cachingConnector.getSingleValue(Constants.PDFIHTReference).flatMap { optionIHTReference =>
          val ihtReference = CommonHelper.getOrException(optionIHTReference)
          val fileName = s"${Messages("iht.inheritanceTaxEstateReport")}.pdf"
          val nino = CommonHelper.getNino(user)
          ihtConnector.getCaseDetails(nino, ihtReference).flatMap(regDetails =>
            getSubmittedApplicationDetails(nino, regDetails) map {
              case Some(ihtReturn) =>
                val pdfByteArray = xmlFoToPDF.createPostSubmissionPDF(regDetails, ihtReturn)
                Ok(pdfByteArray).withHeaders(pdfHeaders(fileName): _*)
              case _ =>
                internalServerError
            }
          )
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
  private def getSubmittedApplicationDetails(nino: String, registrationDetails:RegistrationDetails)
                                            (implicit headerCarrier: HeaderCarrier): Future[Option[IHTReturn]] = {
    val ihtReference = CommonHelper.getOrExceptionNoIHTRef(registrationDetails.ihtReference)
    val returnId = registrationDetails.updatedReturnId
    ihtConnector.getSubmittedApplicationDetails(nino, ihtReference, returnId) map {
      case None =>
        Logger.warn("IhtReturn details not found")
        None
      case Some(ihtReturn) =>
        Logger.info("IhtReturn details have been successfully retrieved ")
        Some(PdfFormatter.transform(ihtReturn, registrationDetails.deceasedDetails.fold("")(_.name)))
    }
  }
}

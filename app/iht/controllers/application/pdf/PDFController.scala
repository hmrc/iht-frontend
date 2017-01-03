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
import iht.controllers.application.ApplicationController
import iht.controllers.auth.IhtActions
import iht.utils.pdf._
import iht.utils.{CommonHelper, DeclarationHelper}
import models.des.iht_return.IHTReturn
import play.api.Logger
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

  def onPDFSummary = authorisedForIht {
    implicit user => implicit request => {
      Logger.info("Generating Summary PDF")
      withApplicationDetails { regDetails => applicationDetails =>
        val pdfByteArray = xmlFoToPDF.generateSummaryPDF(
          regDetails,
          applicationDetails,
          DeclarationHelper.getDeclarationType(applicationDetails)
        )
        Future.successful(Ok(pdfByteArray).withHeaders(("Content-type", "application/pdf")))
      }
    }
  }

  def onPDFClearance(ihtReference: String) = authorisedForIht {
    implicit user => implicit request => {
      Logger.info("Generating Clearance PDF")
      val nino = CommonHelper.getNino(user)
      ihtConnector.getCaseDetails(nino, ihtReference).flatMap(registrationDetails =>
        getSubmittedApplicationDetails(nino,
          CommonHelper.getOrExceptionNoIHTRef(registrationDetails.ihtReference),
          registrationDetails.updatedReturnId) map {
          case Some(ihtReturn) =>
            val pdfByteArray = xmlFoToPDF.createClearancePDF(registrationDetails, CommonHelper.getOrException(
              ihtReturn.declaration, "No declaration found").declarationDate.getOrElse(
              throw new RuntimeException("Declaration Date not available")))
            Ok(pdfByteArray).withHeaders(("Content-type", "application/pdf"))
          case _ =>
            Logger.warn("There has been a problem retrieving the details for the Application PDF. Redirecting" +
              " to internalServerError")
            InternalServerError("There has been a problem retrieving the details for the " +
              "Application PDF")
        }
      )
    }
  }

  def onApplicationPDF(ihtReference: String) = authorisedForIht {
    implicit user => implicit request => {
      val nino = CommonHelper.getNino(user)

      ihtConnector.getCaseDetails(nino, ihtReference).flatMap(regDetails =>
        getSubmittedApplicationDetails(nino, ihtReference, regDetails.updatedReturnId) map {
          case Some(ihtReturn) =>
            val pdfByteArray = xmlFoToPDF.createApplicationReturnPDF(regDetails, ihtReturn)
            Ok(pdfByteArray).withHeaders(("Content-type", "application/pdf"))
          case _ =>
            Logger.warn("There has been a problem retrieving the details for the Application PDF. Redirecting" +
              " to internalServerError")
            InternalServerError("There has been a problem retrieving the details for the " +
              "Application PDF")
        }
      )
    }
  }

  /**
    * Retrieves IHTReturn for given ihtRef and returnId
    */
  private def getSubmittedApplicationDetails(nino: String, ihtReference: String, returnId: String)
                                            (implicit headerCarrier: HeaderCarrier): Future[Option[IHTReturn]] = {
    ihtConnector.getSubmittedApplicationDetails(nino, ihtReference, returnId) map { optionIHTReturn =>
      if(optionIHTReturn.isDefined) {
        Logger.info("IhtReturn details have been successfully retrieved ")
      } else {
        Logger.warn("IhtReturn details not found")
      }
      optionIHTReturn
    }
  }
}

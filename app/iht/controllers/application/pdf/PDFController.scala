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
import iht.models.RegistrationDetails
import iht.models.application.ApplicationDetails
import iht.utils.pdf._
import iht.utils.{CommonHelper, DeclarationHelper}
import models.des.iht_return.IHTReturn
import play.api.Logger
import uk.gov.hmrc.play.frontend.auth.connectors.AuthConnector
import uk.gov.hmrc.play.http.HeaderCarrier

import scala.concurrent.duration.Duration
import scala.concurrent.{Await, Future}

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
      val regDetails:RegistrationDetails = cachingConnector.getExistingRegistrationDetails
      val applicationDetails: Option[ApplicationDetails] = Await.result(ihtConnector.getApplication(CommonHelper.getNino(user),
        CommonHelper.getOrExceptionNoIHTRef(regDetails.ihtReference),
        regDetails.acknowledgmentReference),
        Duration.Inf)
      val declarationType: String = DeclarationHelper.getDeclarationType(applicationDetails.fold(throw new RuntimeException)(identity))
      val declaration = declarationType match {
        case "" => false
        case _ => {
          Logger.info("Declaration Type = " + declarationType)
          true
        }
      }

      val kickout = CommonHelper.getOrExceptionNoApplication(applicationDetails).kickoutReason.isEmpty

      val pdfByteArray = xmlFoToPDF.generateSummaryPDF(regDetails, applicationDetails, declaration, declarationType, kickout)
      Future.successful(Ok(pdfByteArray).withHeaders(("Content-type", "application/pdf")))
    }
  }

  def onPDFClearance(ihtReference: String) = authorisedForIht {
    implicit user => implicit request => {
      Logger.info("Generating Clearance PDF")
      val nino = CommonHelper.getNino(user)
      ihtConnector.getCaseDetails(nino, ihtReference).map(registrationDetails =>
        getSubmittedApplicationDetails(nino,
          CommonHelper.getOrExceptionNoIHTRef(registrationDetails.ihtReference),
          registrationDetails.updatedReturnId) match {
          case Some(ihtReturn) => {
              val pdfByteArray = xmlFoToPDF.createClearancePDF(registrationDetails, CommonHelper.getOrException(
                ihtReturn.declaration, "No declaration found").declarationDate.getOrElse(
                throw new RuntimeException("Declaration Date not available")))

            Ok(pdfByteArray).withHeaders(("Content-type", "application/pdf"))
          }
          case _ => {
            Logger.warn("There has been a problem retrieving the details for the Application PDF. Redirecting" +
              " to internalServerError")
            InternalServerError("There has been a problem retrieving the details for the " +
              "Application PDF")
          }
        }
      )
    }
  }

  def onApplicationPDF(ihtReference: String) = authorisedForIht {
    implicit user => implicit request => {
      val nino = CommonHelper.getNino(user)

      ihtConnector.getCaseDetails(nino, ihtReference).map(regDetails =>
        getSubmittedApplicationDetails(nino, ihtReference, regDetails.updatedReturnId) match {
          case Some(ihtReturn) => {
            val pdfByteArray = xmlFoToPDF.createApplicationReturnPDF(regDetails, ihtReturn)
            Ok(pdfByteArray).withHeaders(("Content-type", "application/pdf"))
          }
          case _ => {
            Logger.warn("There has been a problem retrieving the details for the Application PDF. Redirecting" +
              " to internalServerError")
            InternalServerError("There has been a problem retrieving the details for the " +
              "Application PDF")
          }
        }
      )
    }
  }

  /**
    *
    * Retrieves IHTReturn for given ihtRef and returnId
    *
    * @param nino
    * @param ihtReference
    * @param returnId
    * @return
    */
  private def getSubmittedApplicationDetails(nino: String, ihtReference: String, returnId: String)
                                            (implicit headerCarrier: HeaderCarrier): Option[IHTReturn] = {
    Await.result(ihtConnector.getSubmittedApplicationDetails(nino, ihtReference, returnId),
      Duration.Inf) match {
      case Some(ihtReturn) => {
        Logger.info("IhtReturn details have been successfully retrieved ")
        Some(ihtReturn)
      }
      case _ => {
        Logger.warn("IhtReturn details not found")
        None
      }
    }
  }
}

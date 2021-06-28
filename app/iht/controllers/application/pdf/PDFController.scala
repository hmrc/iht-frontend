/*
 * Copyright 2021 HM Revenue & Customs
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
import iht.connector.{CachingConnector, IhtConnector}
import iht.constants.Constants
import iht.controllers.application.ApplicationController
import iht.models.RegistrationDetails
import iht.models.des.ihtReturn.IHTReturn
import iht.utils.pdf._
import iht.utils.{CommonHelper, DeclarationHelper, StringHelper}
import javax.inject.Inject
import play.api.Logging
import play.api.i18n.{I18nSupport, Messages}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.auth.core.AuthConnector
import uk.gov.hmrc.auth.core.retrieve.v2.Retrievals.{nino => ninoRetrieval}
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController

import scala.concurrent.Future

class PDFControllerImpl @Inject()(val cachingConnector: CachingConnector,
                                  val ihtConnector: IhtConnector,
                                  val authConnector: AuthConnector,
                                  val xmlFoToPDF: XmlFoToPDF,
                                  implicit val appConfig: AppConfig,
                                  val cc: MessagesControllerComponents) extends FrontendController(cc) with PDFController

trait PDFController extends ApplicationController with I18nSupport with StringHelper with PdfHelper with Logging {

  val xmlFoToPDF: XmlFoToPDF

  private def pdfHeaders(fileName: String): Seq[(String, String)] =
    appConfig.pdfStaticHeaders :+ Tuple2(CONTENT_DISPOSITION, "inline; filename=\"" + fileName + "\"")

  def onPreSubmissionPDF: Action[AnyContent] = authorisedForIhtWithRetrievals(ninoRetrieval) { userNino =>
    implicit request => {
      logger.info("Generating Summary PDF")
      val messages = messagesApi.preferred(request)
      val fileName = s"${messages("iht.inheritanceTaxEstateReport")}.pdf"
      withApplicationDetails(userNino) { regDetails =>
        applicationDetails =>
          val pdfByteArray = xmlFoToPDF.createPreSubmissionPDF(
            regDetails,
            applicationDetails,
            DeclarationHelper.getDeclarationType(applicationDetails), messages)
          Future.successful(Ok(pdfByteArray).withHeaders(pdfHeaders(fileName): _*))
      }
    }
  }

  def onClearancePDF: Action[AnyContent] = authorisedForIhtWithRetrievals(ninoRetrieval) { userNino =>
    implicit request => {
      logger.info("Generating Clearance PDF")
      val messages = messagesApi.preferred(request)
      cachingConnector.getSingleValue(Constants.PDFIHTReference).flatMap { optionIHTReference =>
        val ihtReference = CommonHelper.getOrException(optionIHTReference)
        val fileName = s"${messages("pdf.clearanceCertificate.title")}.pdf"
        val nino = getNino(userNino)
        ihtConnector.getCaseDetails(nino, ihtReference).flatMap(registrationDetails =>
          getSubmittedApplicationDetails(nino,
            registrationDetails, messages) map {
            case Some(ihtReturn) =>
              val pdfByteArray = xmlFoToPDF.createClearancePDF(registrationDetails, CommonHelper.getOrException(
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

  def onPostSubmissionPDF: Action[AnyContent] = authorisedForIhtWithRetrievals(ninoRetrieval) { userNino =>
    implicit request => {
      logger.info("Generating Application PDF")
      val messages = messagesApi.preferred(request)
      cachingConnector.getSingleValue(Constants.PDFIHTReference).flatMap {
        case Some(ihtReference) =>
          val fileName = s"${messages("iht.inheritanceTaxEstateReport")}.pdf"
          val nino = getNino(userNino)
          ihtConnector.getCaseDetails(nino, ihtReference).flatMap(regDetails =>
            getSubmittedApplicationDetails(nino, regDetails, messages) map {
              case Some(ihtReturn) =>
                val pdfByteArray = xmlFoToPDF.createPostSubmissionPDF(regDetails, ihtReturn, messages)
                Ok(pdfByteArray).withHeaders(pdfHeaders(fileName): _*)
              case _ =>
                internalServerError
            }
          )
        case _ => Future.successful(Redirect(iht.controllers.estateReports.routes.YourEstateReportsController.onPageLoad()))
      }
    }
  }

  private def internalServerError = {
    logger.warn("There has been a problem retrieving the details for the Application PDF. Redirecting" +
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
        logger.warn("IhtReturn details not found")
        None
      case Some(ihtReturn) =>
        logger.info("IhtReturn details have been successfully retrieved ")
        Some(transform(ihtReturn, registrationDetails, messages))
    }
  }
}

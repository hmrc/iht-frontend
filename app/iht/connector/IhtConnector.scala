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

package iht.connector

import iht.config.AppConfig
import iht.controllers.ControllerHelper
import iht.models._
import iht.models.application.{ApplicationDetails, IhtApplication, ProbateDetails}
import iht.models.des.ihtReturn.IHTReturn
import iht.utils.{GiftsHelper, RegistrationDetailsHelperFixture, StringHelper}
import javax.inject.Inject
import models.des.EventRegistration
import play.api.Logger
import play.api.http.Status._
import play.api.libs.json.{JsError, JsValue, Json}
import play.api.mvc.Request
import uk.gov.hmrc.http._
import uk.gov.hmrc.play.bootstrap.config.ServicesConfig
import uk.gov.hmrc.play.bootstrap.http.DefaultHttpClient

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

trait IhtConnector {

  def http: HttpGet with HttpPost with HttpPut with HttpDelete

  def serviceUrl: String

  def url(path: String) = s"$serviceUrl$path"

  def submitRegistration(nino: String, rd: RegistrationDetails)(implicit headerCarrier: HeaderCarrier, request: Request[_]): Future[String]

  def getCaseList(nino: String)(implicit headerCarrier: HeaderCarrier): Future[Seq[IhtApplication]]

  def getCaseDetails(nino: String, ihtReference: String)(implicit headerCarrier: HeaderCarrier): Future[RegistrationDetails]

  def getApplication(nino: String,
                     ihtReference: String,
                     acknowledgmentReference: String)(implicit headerCarrier: HeaderCarrier): Future[Option[ApplicationDetails]]

  def saveApplication(nino: String,
                      data: ApplicationDetails,
                      acknowledgmentReference: String)(implicit headerCarrier: HeaderCarrier, request: Request[_]): Future[Option[ApplicationDetails]]

  def getRealtimeRiskingMessage(ihtAppReference: String, nino: String)(implicit headerCarrier: HeaderCarrier): Future[Option[String]]

  def submitApplication(ihtAppReference: String,
                        nino: String, applicationDetails: ApplicationDetails)
                       (implicit headerCarrier: HeaderCarrier, request: Request[_]): Future[Option[String]]

  def requestClearance(nino: String,
                       ihtReference: String)
                      (implicit headerCarrier: HeaderCarrier): Future[Boolean]

  def getProbateDetails(nino: String,
                        ihtReference: String,
                        ihtReturnId: String)
                       (implicit headerCarrier: HeaderCarrier): Future[Option[ProbateDetails]]

  def deleteApplication(nino: String, ihtReference: String)(implicit headerCarrier: HeaderCarrier): Unit

  def getSubmittedApplicationDetails(nino: String, ihtReference: String, returnId: String)(implicit headerCarrier: HeaderCarrier): Future[Option[IHTReturn]]
}

class IhtConnectorImpl @Inject()(val http: DefaultHttpClient,
                                 val config: ServicesConfig,
                                 implicit val appConfig: AppConfig) extends IhtConnector with StringHelper {
  lazy val serviceUrl: String = config.baseUrl("iht")

  override def deleteApplication(nino: String, ihtReference: String)(implicit headerCarrier: HeaderCarrier): Unit = {
    Logger.info("Calling IHT micro-service to delete application")
    http.GET(s"$serviceUrl/iht/$nino/application/delete/$ihtReference")
  }

  private def ihtHeaders(implicit request: Request[_]) = Seq("path"->request.headers.get("Referer").getOrElse(""))

  override def submitRegistration(nino: String, rd: RegistrationDetails)(implicit headerCarrier: HeaderCarrier, request: Request[_]): Future[String] = {
    val er = EventRegistration.fromRegistrationDetails(rd)
    val ninoFormatted = trimAndUpperCaseNino(nino)
    Logger.info("Calling IHT micro-service to submit registration")
    http.POST(s"$serviceUrl/iht/$ninoFormatted/registration/submit", er, ihtHeaders) map {
      response => {
        if (response.status == ACCEPTED) {
          ""
        } else {
          Logger.info("Successful return from registration submit")
          response.body
        }
      }
    } recoverWith {
      case Upstream4xxResponse(message, CONFLICT, _, _) =>
        Logger.warn(s"CONFLICT Failure response for duplicate case. Error message: $message")
        Future.failed(new ConflictException(message))
      case ex: GatewayTimeoutException =>
        Logger.warn("5xx Response returned : " + ex.getMessage)
        Future.failed(new GatewayTimeoutException(ex.getMessage))
      case ex : Upstream5xxResponse =>
        Logger.warn("5xx Response returned : " + ex.getMessage)
        Future.failed(ex)
      case ex =>
        Logger.warn("5xx Response returned : " + ex.getMessage)
        Future.failed(Upstream5xxResponse(ex.getMessage, INTERNAL_SERVER_ERROR, INTERNAL_SERVER_ERROR))
    }
  }

  override def saveApplication(nino: String,
                               data: ApplicationDetails,
                               acknowledgmentReference: String)
                              (implicit headerCarrier: HeaderCarrier, request: Request[_]): Future[Option[ApplicationDetails]] =
    exceptionCheckForResponses {
    Logger.info("Saving application in Secure Storage")
    val future_response = http.POST(s"$serviceUrl/iht/$nino/application/save/$acknowledgmentReference", data, ihtHeaders)
    for {
      response <- future_response
    } yield {
      response.status match {
        case OK =>
          Logger.info("Successful return from right for save application")
          Some(data)
        case _ =>
          Logger.warn("Problem saving application details")
          throw new RuntimeException("Problem saving application details")
      }
    }
  }

  override def getApplication(nino: String, ihtRef: String, acknowledgmentReference: String)
                             (implicit headerCarrier: HeaderCarrier): Future[Option[ApplicationDetails]] = exceptionCheckForResponses {
    Logger.info("Getting application from Secure Storage")
    val future_response = http.GET(s"$serviceUrl/iht/$nino/application/get/$ihtRef/$acknowledgmentReference")
    for {
      response <- future_response
    } yield {
      response.status match {
        case OK =>
          Logger.info("Successfully received data from SecStore")
          Some(Json.fromJson[ApplicationDetails](Json.parse(response.body)).get)
            .map(ad=> GiftsHelper.correctGiftDateFormats(ad))
        case NO_CONTENT =>
          Logger.info("Empty return from Secure Storage")
          None
        case _ =>
          Logger.warn("Problem retrieving application details")
          throw new RuntimeException("Problem retrieving application details")
      }
    }
  }

  override def getCaseList(nino: String)(implicit headerCarrier: HeaderCarrier): Future[Seq[IhtApplication]] = exceptionCheckForResponses {
    Logger.info("Getting Case List")
    http.GET(s"$serviceUrl/iht/$nino/home/listCases").map {
      response => {
        response.status match {
          case OK =>
            Logger.info("Successfully return from Get Case List")
            Json.fromJson[Seq[IhtApplication]](Json.parse(response.body)).getOrElse(Nil)
          case NO_CONTENT =>
            Logger.info("Empty return from Get Case List")
            Nil
          case _ =>
            Logger.warn("Problem retrieving Case List")
            throw new RuntimeException("Problem retrieving Case List")
        }
      }
    }
  }

  override def getCaseDetails(nino: String, ihtReference: String)
                             (implicit headerCarrier: HeaderCarrier): Future[RegistrationDetails] = exceptionCheckForResponses {
    Logger.info("Getting Case Details")
    http.GET(s"$serviceUrl/iht/$nino/home/caseDetails/$ihtReference").map {
      response => {
        response.status match {
          case OK =>
            Logger.info("Returned Case Details")
            val js: JsValue = Json.parse(response.body)
            Json.fromJson[RegistrationDetails](js) match {
              case JsError(_) =>
                Logger.warn("JSON parse error. Although returned - Failure to create Registration Details")
                throw new RuntimeException("JSON parse error. Although returned - Failure to create Registration Details")
              case x =>
                Logger.info("Correctly returned for registration details")
                RegistrationDetailsHelperFixture().getOrExceptionNoRegistration(x.asOpt)
            }
          case _ =>
            Logger.warn("Problem retrieving Case Details")
            throw new RuntimeException("Problem retrieving Case Details")
        }
      }
    }
  }

  def connectionRecoveryPF[A]: PartialFunction[Throwable, scala.concurrent.Future[A]]  = {
    case e: GatewayTimeoutException =>
      Logger.warn("Gateway Timeout Response Returned ::: " + e.getMessage)
      Future.failed(new GatewayTimeoutException(e.message))
    case e: BadRequestException =>
      Logger.warn("BadRequest Response Returned ::: " + e.getMessage)
      Future.failed(new BadRequestException(e.message))
    case e: Upstream4xxResponse =>
      Logger.warn("Upstream4xxResponse Returned ::: " + e.getMessage)
      Future.failed(Upstream4xxResponse(e.message, e.upstreamResponseCode, e.reportAs))
    case e: Upstream5xxResponse =>
      Logger.warn("Upstream5xxResponse Returned ::: " + e.getMessage)
      Future.failed(Upstream5xxResponse(e.message, e.upstreamResponseCode, e.reportAs))
    case e: NotFoundException =>
      Logger.warn("Upstream4xxResponse Returned ::: " + e.getMessage)
      Future.failed(Upstream4xxResponse(e.message, ControllerHelper.notFoundExceptionCode, ControllerHelper.notFoundExceptionCode))
    case e: Exception =>
      Logger.warn("Exception Returned ::: " + e.getMessage)
      Future.failed(new Exception(e.getMessage))
  }

  private def connectorRecovery[A]: PartialFunction[Throwable, Future[A]] = connectionRecoveryPF

  override def submitApplication(ihtAppReference: String, nino: String, applicationDetails: ApplicationDetails)
                                (implicit headerCarrier: HeaderCarrier, request: Request[_]): Future[Option[String]] = {
    val formattedNino = trimAndUpperCaseNino(nino)
    Logger.info("Submitting application")
    http.POST(s"$serviceUrl/iht/$formattedNino/$ihtAppReference/application/submit", applicationDetails, ihtHeaders).map(
      response =>
        response.status match {
        case OK =>
          Logger.info("Response received from Right for application submit")
          Some(response.body.split(":").last.trim)
        case _ =>
          Logger.warn("Problem with the submission of the application details")
          throw new RuntimeException("Problem with the submission of the application details")
      }
    ) recoverWith {
      case e: Upstream4xxResponse if e.upstreamResponseCode == FORBIDDEN => Future.successful(None)
    } recoverWith connectorRecovery
  }

  def realtimeRiskingMessageResponseMatch(response: HttpResponse): Option[String] = {
    response.status match {
      case status if status == OK =>
        if (response.body.nonEmpty) {
          Some(response.body)
        } else {
          None
        }
      case NO_CONTENT => None
    }
  }

  /**
   * Get the real-time risking information from the IHT service. If none found then
   * returns None, else returns a message indicating the problem.
   */
  override def getRealtimeRiskingMessage(ihtAppReference: String, nino: String)
                                        (implicit headerCarrier: HeaderCarrier): Future[Option[String]] = {
    Logger.info("Getting realtime risking message")
    http.GET(s"$serviceUrl/iht/$nino/application/getRealtimeRiskingMessage/$ihtAppReference") map {
      response => realtimeRiskingMessageResponseMatch(response)
    } recoverWith connectorRecovery
  }

  override def requestClearance(nino: String, ihtReference: String)
                               (implicit headerCarrier: HeaderCarrier): Future[Boolean] = {
    Logger.info("Requesting clearance")
    val future_response = http.GET(s"$serviceUrl/iht/$nino/$ihtReference/application/requestClearance")
    for {
      response <- future_response
    } yield {
      response.status match {
        case OK =>
          Logger.info("Received response from DES")
          true
        case _ =>
          Logger.warn("Problem requesting clearance")
          throw new RuntimeException("Problem requesting clearance")
      }
    }
  }

  def returnProbateDetails(js: JsValue): Some[ProbateDetails] = {
    Json.fromJson[ProbateDetails](js) match {
      case JsError(_) =>
        Logger.warn("JSON parse error. Although returned - Failure to create Probate Details")
        throw new RuntimeException("JSON parse error. Although returned - Failure to create Probate Details")
      case x =>
        Logger.info("Correctly returned for Probate Details")
        Some(x.get)
    }
  }

  def retrieveProbateDetails(response: HttpResponse): Some[ProbateDetails] = {
    response.status match {
      case OK =>
        Logger.info("Returned Probate Details")
        val js: JsValue = Json.parse(response.body)
        returnProbateDetails(js)
      case _ =>
        Logger.warn("Problem retrieving Probate Details")
        throw new RuntimeException("Problem retrieving Probate Details")
    }
  }

  override def getProbateDetails(nino: String, ihtReference: String, ihtReturnId: String)
                                (implicit headerCarrier: HeaderCarrier): Future[Option[ProbateDetails]] = {
    Logger.info("Getting Probate Details")
    http.GET(s"$serviceUrl/iht/$nino/application/probateDetails/$ihtReference/$ihtReturnId") map { response =>
      retrieveProbateDetails(response)
    } recoverWith connectorRecovery
  }

  override def getSubmittedApplicationDetails(nino: String, ihtReference: String, returnId: String)
                                             (implicit headerCarrier: HeaderCarrier): Future[Option[IHTReturn]] =
    exceptionCheckForResponses {
      Logger.info("Getting the submitted IHT return details")
      http.GET(s"$serviceUrl/iht/$nino/$ihtReference/$returnId/application/getSubmittedApplicationDetails") map { response =>
        response.status match {
          case OK =>
            Logger.info("getSubmittedApplicationDetails response OK")
            val js: JsValue = Json.parse(response.body)
            Json.fromJson[IHTReturn](js) match {
              case JsError(_) =>
                Logger.warn("JSON parse error. Although returned - Failure to create an IHTReturn")
                throw new RuntimeException("JSON parse error. Although returned - Failure to create an IHTReturn")
              case iht_ret =>
                Logger.info("Correctly retrieved the IHT return details")
                Some(iht_ret.get)
            }
          case _ =>
            Logger.warn("Problem retrieving the IHT return details")
            throw new RuntimeException("Problem retrieving the IHT return details")
        }
      }
    }

  /**
   * Checks the relevant response exceptions for the code to be executed
   */
  private def exceptionCheckForResponses[A](x: Future[A]): Future[A] = x recoverWith connectorRecovery
}
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

package iht.connector

import iht.config.AppConfig
import iht.controllers.ControllerHelper
import iht.models._
import iht.models.application.{ApplicationDetails, IhtApplication, ProbateDetails}
import iht.models.des.ihtReturn.IHTReturn
import iht.utils.{GiftsHelper, RegistrationDetailsHelperFixture, StringHelper}
import javax.inject.Inject
import models.des.EventRegistration
import play.api.Logging
import play.api.http.Status._
import play.api.libs.json.{JsError, JsValue, Json}
import play.api.mvc.{MessagesControllerComponents, Request}
import uk.gov.hmrc.http._
import uk.gov.hmrc.play.bootstrap.config.ServicesConfig
import uk.gov.hmrc.play.bootstrap.http.DefaultHttpClient
import uk.gov.hmrc.http.HttpReads.Implicits._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

trait IhtConnector extends Logging {

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
                                 val cc: MessagesControllerComponents,
                                 implicit val appConfig: AppConfig) extends IhtConnector with StringHelper {
  lazy val serviceUrl: String = config.baseUrl("iht")

  override def deleteApplication(nino: String, ihtReference: String)(implicit headerCarrier: HeaderCarrier): Unit = {
    logger.info("Calling IHT micro-service to delete application")
    http.GET(s"$serviceUrl/iht/$nino/application/delete/$ihtReference")(rds = readRaw, hc = headerCarrier, ec = cc.executionContext)
  }

  private def ihtHeaders(implicit request: Request[_]) = Seq("path"->request.headers.get("Referer").getOrElse(""))

  override def submitRegistration(nino: String, rd: RegistrationDetails)(implicit headerCarrier: HeaderCarrier, request: Request[_]): Future[String] = {
    val er = EventRegistration.fromRegistrationDetails(rd)
    val ninoFormatted = trimAndUpperCaseNino(nino)
    logger.info("Calling IHT micro-service to submit registration")
    http.POST(
      s"$serviceUrl/iht/$ninoFormatted/registration/submit", er, ihtHeaders
    )(rds = readRaw, hc = headerCarrier, ec = cc.executionContext, wts = Json.format[models.des.EventRegistration]) map {
      response => {
        if (response.status == ACCEPTED) {
          ""
        } else {
          logger.info("Successful return from registration submit")
          response.body
        }
      }
    } recoverWith {
      case UpstreamErrorResponse(message, CONFLICT, _, _) =>
        logger.warn(s"CONFLICT Failure response for duplicate case. Error message: $message")
        Future.failed(new ConflictException(message))
      case ex: GatewayTimeoutException =>
        logger.warn("5xx Response returned : " + ex.getMessage)
        Future.failed(new GatewayTimeoutException(ex.getMessage))
      case ex : UpstreamErrorResponse =>
        logger.warn("5xx Response returned : " + ex.getMessage)
        Future.failed(ex)
      case ex =>
        logger.warn("5xx Response returned : " + ex.getMessage)
        Future.failed(UpstreamErrorResponse(ex.getMessage, INTERNAL_SERVER_ERROR, INTERNAL_SERVER_ERROR))
    }
  }

  override def saveApplication(nino: String,
                               data: ApplicationDetails,
                               acknowledgmentReference: String)
                              (implicit headerCarrier: HeaderCarrier, request: Request[_]): Future[Option[ApplicationDetails]] =
    exceptionCheckForResponses {
      logger.info("Saving application in Secure Storage")
      val future_response = http.POST(
        s"$serviceUrl/iht/$nino/application/save/$acknowledgmentReference", data, ihtHeaders
      )(rds = readRaw, hc = headerCarrier, ec = cc.executionContext, wts = Json.format[ApplicationDetails])
      for {
        response <- future_response
      } yield {
        response.status match {
          case OK =>
            logger.info("Successful return from right for save application")
            Some(data)
          case _ =>
            logger.warn("Problem saving application details")
            throw new RuntimeException("Problem saving application details")
        }
      }
    }

  override def getApplication(nino: String, ihtRef: String, acknowledgmentReference: String)
                             (implicit headerCarrier: HeaderCarrier): Future[Option[ApplicationDetails]] = exceptionCheckForResponses {
    logger.info("Getting application from Secure Storage")
    val future_response = http.GET(
      s"$serviceUrl/iht/$nino/application/get/$ihtRef/$acknowledgmentReference"
    )(rds = readRaw, hc = headerCarrier, ec = cc.executionContext)
    for {
      response <- future_response
    } yield {
      response.status match {
        case OK =>
          logger.info("Successfully received data from SecStore")
          Some(Json.fromJson[ApplicationDetails](Json.parse(response.body)).get)
            .map(ad=> GiftsHelper.correctGiftDateFormats(ad))
        case NO_CONTENT =>
          logger.info("Empty return from Secure Storage")
          None
        case _ =>
          logger.warn("Problem retrieving application details")
          throw new RuntimeException("Problem retrieving application details")
      }
    }
  }

  override def getCaseList(nino: String)(implicit headerCarrier: HeaderCarrier): Future[Seq[IhtApplication]] = exceptionCheckForResponses {
    logger.info("Getting Case List")
    http.GET(
      s"$serviceUrl/iht/$nino/home/listCases"
      )(rds = readRaw, hc = headerCarrier, ec = cc.executionContext).map {
      response => {
        response.status match {
          case OK =>
            logger.info("Successfully return from Get Case List")
            Json.fromJson[Seq[IhtApplication]](Json.parse(response.body)).getOrElse(Nil)
          case NO_CONTENT =>
            logger.info("Empty return from Get Case List")
            Nil
          case _ =>
            logger.warn("Problem retrieving Case List")
            throw new RuntimeException("Problem retrieving Case List")
        }
      }
    }
  }

  override def getCaseDetails(nino: String, ihtReference: String)
                             (implicit headerCarrier: HeaderCarrier): Future[RegistrationDetails] = exceptionCheckForResponses {
    logger.info("Getting Case Details")
    http.GET(
      s"$serviceUrl/iht/$nino/home/caseDetails/$ihtReference"
      )(rds = readRaw, hc = headerCarrier, ec = cc.executionContext).map {
      response => {
        response.status match {
          case OK =>
            logger.info("Returned Case Details")
            val js: JsValue = Json.parse(response.body)
            Json.fromJson[RegistrationDetails](js) match {
              case JsError(_) =>
                logger.warn("JSON parse error. Although returned - Failure to create Registration Details")
                throw new RuntimeException("JSON parse error. Although returned - Failure to create Registration Details")
              case x =>
                logger.info("Correctly returned for registration details")
                RegistrationDetailsHelperFixture().getOrExceptionNoRegistration(x.asOpt)
            }
          case _ =>
            logger.warn("Problem retrieving Case Details")
            throw new RuntimeException("Problem retrieving Case Details")
        }
      }
    }
  }

  def connectionRecoveryPF[A]: PartialFunction[Throwable, scala.concurrent.Future[A]]  = {
    case e: GatewayTimeoutException =>
      logger.warn("Gateway Timeout Response Returned ::: " + e.getMessage)
      Future.failed(new GatewayTimeoutException(e.message))
    case e: BadRequestException =>
      logger.warn("BadRequest Response Returned ::: " + e.getMessage)
      Future.failed(new BadRequestException(e.message))
    case e: UpstreamErrorResponse =>
      logger.warn("UpstreamErrorResponse Returned ::: " + e.getMessage)
      Future.failed(UpstreamErrorResponse.apply(e.message, e.statusCode, e.reportAs))
    case e: NotFoundException =>
      logger.warn("Upstream4xxResponse Returned ::: " + e.getMessage)
      Future.failed(UpstreamErrorResponse.apply(e.message, ControllerHelper.notFoundExceptionCode))
    case e: InternalServerException =>
      logger.warn("InternalServerException Returned ::: " + e.getMessage)
      Future.failed(UpstreamErrorResponse.apply(e.message, ControllerHelper.internalExceptionCode))
    case e: Exception =>
      logger.warn("Exception Returned ::: " + e.getMessage)
      Future.failed(new Exception(e.getMessage))
  }

  private def connectorRecovery[A]: PartialFunction[Throwable, Future[A]] = connectionRecoveryPF

  override def submitApplication(ihtAppReference: String, nino: String, applicationDetails: ApplicationDetails)
                                (implicit headerCarrier: HeaderCarrier, request: Request[_]): Future[Option[String]] = {
    val formattedNino = trimAndUpperCaseNino(nino)
    logger.info("Submitting application")
    http.POST(
      s"$serviceUrl/iht/$formattedNino/$ihtAppReference/application/submit", applicationDetails, ihtHeaders
    )(rds = readRaw, hc = headerCarrier, ec = cc.executionContext, wts = Json.format[ApplicationDetails]).map(
      response =>
        response.status match {
          case OK =>
            logger.info("Response received from Right for application submit")
            Some(response.body.split(":").last.trim)
          case FORBIDDEN => None
          case INTERNAL_SERVER_ERROR =>
            logger.warn("Problem with the submission of the application details")
            throw new InternalServerException(response.body)
        }
    ) recoverWith {
      case e: UpstreamErrorResponse if e.statusCode == FORBIDDEN => {
        Future.successful(None)
      }
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
    logger.info("Getting realtime risking message")
    http.GET(
      s"$serviceUrl/iht/$nino/application/getRealtimeRiskingMessage/$ihtAppReference"
    )(rds = readRaw, hc = headerCarrier, ec = cc.executionContext).map {
      response => realtimeRiskingMessageResponseMatch(response)
    } recoverWith connectorRecovery
  }

  override def requestClearance(nino: String, ihtReference: String)
                               (implicit headerCarrier: HeaderCarrier): Future[Boolean] = {
    logger.info("Requesting clearance")
    val future_response = http.GET(
      s"$serviceUrl/iht/$nino/$ihtReference/application/requestClearance"
    )(rds = readRaw, hc = headerCarrier, ec = cc.executionContext)
    for {
      response <- future_response
    } yield {
      response.status match {
        case OK =>
          logger.info("Received response from DES")
          true
        case _ =>
          logger.warn("Problem requesting clearance")
          throw new RuntimeException("Problem requesting clearance")
      }
    }
  }

  def returnProbateDetails(js: JsValue): Some[ProbateDetails] = {
    Json.fromJson[ProbateDetails](js) match {
      case JsError(_) =>
        logger.warn("JSON parse error. Although returned - Failure to create Probate Details")
        throw new RuntimeException("JSON parse error. Although returned - Failure to create Probate Details")
      case x =>
        logger.info("Correctly returned for Probate Details")
        Some(x.get)
    }
  }

  def retrieveProbateDetails(response: HttpResponse): Some[ProbateDetails] = {
    response.status match {
      case OK =>
        logger.info("Returned Probate Details")
        val js: JsValue = Json.parse(response.body)
        returnProbateDetails(js)
      case _ =>
        logger.warn("Problem retrieving Probate Details")
        throw new RuntimeException("Problem retrieving Probate Details")
    }
  }

  override def getProbateDetails(nino: String, ihtReference: String, ihtReturnId: String)
                                (implicit headerCarrier: HeaderCarrier): Future[Option[ProbateDetails]] = {
    logger.info("Getting Probate Details")
    http.GET(
      s"$serviceUrl/iht/$nino/application/probateDetails/$ihtReference/$ihtReturnId")(rds = readRaw, hc = headerCarrier, ec = cc.executionContext).map { response =>
      retrieveProbateDetails(response)
    } recoverWith connectorRecovery
  }

  override def getSubmittedApplicationDetails(nino: String, ihtReference: String, returnId: String)
                                             (implicit headerCarrier: HeaderCarrier): Future[Option[IHTReturn]] =
    exceptionCheckForResponses {
      logger.info("Getting the submitted IHT return details")

      http.GET(
        s"$serviceUrl/iht/$nino/$ihtReference/$returnId/application/getSubmittedApplicationDetails"
      )(rds = readRaw, hc = headerCarrier, ec = cc.executionContext).map { response =>
        response.status match {
          case OK =>
            logger.info("getSubmittedApplicationDetails response OK")
            val js: JsValue = Json.parse(response.body)
            Json.fromJson[IHTReturn](js) match {
              case JsError(_) =>
                logger.warn("JSON parse error. Although returned - Failure to create an IHTReturn")
                throw new RuntimeException("JSON parse error. Although returned - Failure to create an IHTReturn")
              case iht_ret =>
                logger.info("Correctly retrieved the IHT return details")
                Some(iht_ret.get)
            }
          case _ =>
            logger.warn("Problem retrieving the IHT return details")
            throw new RuntimeException("Problem retrieving the IHT return details")
        }
      }
    }

  /**
   * Checks the relevant response exceptions for the code to be executed
   */
  private def exceptionCheckForResponses[A](x: Future[A]): Future[A] = x recoverWith connectorRecovery
}
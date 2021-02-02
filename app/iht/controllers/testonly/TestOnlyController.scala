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

package iht.controllers.testonly

import iht.config.{AppConfig, IhtFormPartialRetriever}
import iht.connector.{CachingConnector, IhtConnector}
import iht.controllers.application.ApplicationController
import iht.forms.testonly.TestOnlyForms.{storeRegistrationDetailsForm, _}
import iht.models.RegistrationDetails
import iht.models.application.ApplicationDetails
import javax.inject.Inject
import play.api.Logging
import play.api.libs.json.Json
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.auth.core.AuthConnector
import uk.gov.hmrc.auth.core.retrieve.v2.Retrievals.{nino => ninoRetrieval}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController

import scala.concurrent.Future

class TestOnlyControllerImpl @Inject()(val ihtConnector: IhtConnector,
                                       val cachingConnector: CachingConnector,
                                       val authConnector: AuthConnector,
                                       implicit val formPartialRetriever: IhtFormPartialRetriever,
                                       implicit val appConfig: AppConfig,
val cc: MessagesControllerComponents) extends FrontendController(cc) with TestOnlyController

trait TestOnlyController extends ApplicationController with Logging {
  def ihtConnector: IhtConnector
  def cachingConnector: CachingConnector

  def deleteFirstCase: Action[AnyContent] = authorisedForIhtWithRetrievals(ninoRetrieval) { userNino =>
    implicit request => {
      val nino = getNino(userNino)
      ihtConnector.getCaseList(nino).map {
        list => {
          list.foreach {
            c => ihtConnector.deleteApplication(nino, c.ihtRefNo)
          }
          Ok("Removed data")
        }
      }
    }
  }

  def fillApplication: Action[AnyContent] = authorisedForIhtWithRetrievals(ninoRetrieval) { userNino =>
    implicit request => {
      withApplicationDetails(userNino) { registrationDetails => applicationDetails =>
        ihtConnector.saveApplication(getNino(userNino),
          TestOnlyDataGenerator.buildApplicationDetails(registrationDetails.ihtReference),
          registrationDetails.acknowledgmentReference)
          .map {
            case Some(_) => Ok("Filled application with No answers")
            case _ =>
              logger.warn("Unable to save application details. Redirecting to InternalServerError")
              InternalServerError("Unable to save application details")
          }
      }
    }
  }

  def storeRegistrationDetailsPageLoad: Action[AnyContent] = authorisedForIht {
    implicit request => {
      Future.successful(Ok(iht.views.html.testOnly.store_registration_details()))
    }
  }

  def storeRegistrationDetailsSubmit: Action[AnyContent] = authorisedForIht {
    implicit request => {
      val boundForm = storeRegistrationDetailsForm.bindFromRequest

      boundForm.fold(formWithErrors => {
        Future.successful(Ok(iht.views.html.testOnly.store_registration_details_result("There was a problem with the data you submitted.")))
      },

        details => {
          try {
            val rd = Json.parse(details).as[RegistrationDetails]
            cachingConnector.storeRegistrationDetails(rd)

            Future.successful(Ok(iht.views.html.testOnly.store_registration_details_result("Registration details stored.")))
          }
          catch {
            case _: Throwable => Future.successful(
              Ok(iht.views.html.testOnly.store_registration_details_result("Could not store these registration details.")))
          }
        }
      )
    }
  }

  def storeApplicationDetailsPageLoad = authorisedForIht {
    implicit request => {
      Future.successful(Ok(iht.views.html.testOnly.store_application_details()))
    }
  }

  def storeApplicationDetailsSubmit = authorisedForIht {
    implicit request => {
      val boundForm = storeApplicationDetailsForm.bindFromRequest

      boundForm.fold(formWithErrors => {
        Future.successful(Ok(iht.views.html.testOnly.store_application_details_result("There was a problem with the data you submitted.")))
      },
        details => {
          try {
            val ihtRef = s"${details.nino}${details.fileReference}"
            val ad = Json.parse(details.applicationDetails).as[ApplicationDetails] copy
              (ihtRef = Some(ihtRef))

            val ackRef = s"${ihtRef}${ihtRef}AA"

            ihtConnector.saveApplication(details.nino, ad, ackRef)
              .map(optionApplicationDetailsSaved => {
                optionApplicationDetailsSaved.fold {
                  InternalServerError("Application Details not stored.")
                }(_ => {
                  Ok(iht.views.html.testOnly.store_application_details_result("Application details stored."))
                })
              })
            Future.successful(Ok(iht.views.html.testOnly.store_application_details_result("Application details stored.")))
          }
          catch {
            case x: Throwable => {
              Future.successful(
                Ok(iht.views.html.testOnly.store_application_details_result("Could not store these application details.")))
            }
          }
        }
      )
    }
  }
}

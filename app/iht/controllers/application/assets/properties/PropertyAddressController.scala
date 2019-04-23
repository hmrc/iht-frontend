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

package iht.controllers.application.assets.properties

import iht.config.AppConfig
import iht.connector.{CachingConnector, IhtConnector}
import iht.controllers.application.EstateController
import iht.forms.ApplicationForms.propertyAddressForm
import iht.metrics.IhtMetrics
import iht.models._
import iht.models.application.ApplicationDetails
import iht.models.application.assets.Property
import iht.utils.{ApplicationKickOutHelper, CommonHelper, LogHelper, StringHelper}
import javax.inject.Inject
import play.api.Logger
import play.api.i18n.Messages
import play.api.mvc._
import uk.gov.hmrc.auth.core.AuthConnector
import uk.gov.hmrc.auth.core.retrieve.v2.Retrievals.{nino => ninoRetrieval}
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.bootstrap.controller.{FrontendController, FrontendHeaderCarrierProvider}
import uk.gov.hmrc.play.partials.FormPartialRetriever

import scala.concurrent.Future

class PropertyAddressControllerImpl @Inject()(val metrics: IhtMetrics,
                                              val ihtConnector: IhtConnector,
                                              val cachingConnector: CachingConnector,
                                              val authConnector: AuthConnector,
                                              val formPartialRetriever: FormPartialRetriever,
                                              implicit val appConfig: AppConfig,
                                              val cc: MessagesControllerComponents) extends FrontendController(cc) with PropertyAddressController

trait PropertyAddressController extends EstateController with FrontendHeaderCarrierProvider with StringHelper {


  def ihtConnector: IhtConnector

  def cachingConnector: CachingConnector

  def editCancelUrl(id: String) = routes.PropertyDetailsOverviewController.onEditPageLoad(id)

  def editSubmitUrl(id: String) = routes.PropertyAddressController.onEditSubmit(id)

  def locationAfterSuccessfulSave(id: String) = CommonHelper.addFragmentIdentifier(
    routes.PropertyDetailsOverviewController.onEditPageLoad(id), Some(appConfig.AssetsPropertiesPropertyAddressID))

  def cancelUrl = routes.PropertyDetailsOverviewController.onPageLoad()

  lazy val submitUrl = routes.PropertyAddressController.onSubmit()
  def cancelLabel(implicit request: Request[_]) = Messages("iht.estateReport.assets.properties.returnToAddAProperty")
  override val applicationSection = Some(ApplicationKickOutHelper.ApplicationSectionProperties)

  def onPageLoad = authorisedForIht {
    implicit request => {
      Future.successful(Ok(iht.views.html.application.asset.properties.property_address(
        propertyAddressForm,
        cancelUrl,
        submitUrl)))
    }
  }

  def onEditPageLoad(id: String): Action[AnyContent] = authorisedForIhtWithRetrievals(ninoRetrieval) { userNino =>
    implicit request =>
      withRegistrationDetails { registrationData =>
        for {
          applicationDetails <- ihtConnector.getApplication(getNino(userNino),
            CommonHelper.getOrExceptionNoIHTRef(registrationData.ihtReference),
            registrationData.acknowledgmentReference)(hc)
        } yield {
          applicationDetails match {
            case Some(appDetails) =>
              appDetails.propertyList.find(property => property.id.getOrElse("") equals id).fold {
                throw new RuntimeException("No Property found for the id")
              } { matchedProperty =>
                Ok(iht.views.html.application.asset.properties.property_address(
                  propertyAddressForm.fill(matchedProperty),
                  editCancelUrl(id),
                  editSubmitUrl(id)
                ))
              }
            case _ =>
              Logger.warn("Problem retrieving Application Details. Redirecting to Internal Server Error")
              InternalServerError("No Application Details found")
          }
        }
      }
  }

  def onSubmit = authorisedForIhtWithRetrievals(ninoRetrieval) { userNino =>
    implicit request => {
      doSubmit(
        redirectLocationIfErrors = routes.PropertyAddressController.onSubmit(),
        submitUrl = submitUrl,
        cancelUrl = cancelUrl,
        userNino)
    }
  }

  def onEditSubmit(id: String) = authorisedForIhtWithRetrievals(ninoRetrieval) { userNino =>
    implicit request => {
      doSubmit(
        redirectLocationIfErrors = routes.PropertyAddressController.onEditSubmit(id),
        submitUrl = editSubmitUrl(id),
        cancelUrl = editCancelUrl(id),
        userNino,
        Some(id))
    }
  }

  private def doSubmit(redirectLocationIfErrors: Call,
                       submitUrl: Call,
                       cancelUrl: Call,
                       userNino: Option[String],
                       propertyId: Option[String] = None)(
                        implicit request: Request[_]) = {
    val boundForm = propertyAddressForm.bindFromRequest
    boundForm.fold(
      formWithErrors => {
        LogHelper.logFormError(formWithErrors)
        Future.successful(BadRequest(iht.views.html.application.asset.properties.property_address(
          formWithErrors,
          cancelUrl,
          submitUrl)))
      },
      property => {
        processSubmit(getNino(userNino), property, propertyId)
      }
    )
  }

  def processSubmit(nino: String, property: Property,
                    propertyId: Option[String] = None)(
                     implicit request: Request[_], hc: HeaderCarrier): Future[Result] = {

    withRegistrationDetails { registrationData =>
      val ihtReference = CommonHelper.getOrExceptionNoIHTRef(registrationData.ihtReference)
      val applicationDetailsFuture: Future[Option[ApplicationDetails]] =
        ihtConnector.getApplication(nino, ihtReference, registrationData.acknowledgmentReference)

      applicationDetailsFuture.flatMap { optionApplicationDetails =>
        val tuplePropertiesAndID: (List[Property], String) = addPropertyToPropertyList(property.copy(id = propertyId),
          optionApplicationDetails.fold[List[Property]](Nil)(ad => ad.propertyList))

        val updatedProperties: List[Property] = tuplePropertiesAndID._1

        val propertyID: String = tuplePropertiesAndID._2

        val ad = updateKickout(registrationDetails = registrationData,
          applicationDetails = optionApplicationDetails.map(ad => ad.copy(propertyList = updatedProperties)).fold
          (ApplicationDetails(propertyList = updatedProperties))(identity),
          applicationID = Some(propertyID))

        ihtConnector.saveApplication(nino, ad, registrationData.acknowledgmentReference) map {
          case Some(_) =>
            Redirect(ad.kickoutReason.fold(locationAfterSuccessfulSave(propertyID)) {
              _ => {
                cachingConnector.storeSingleValue(ApplicationKickOutHelper.applicationLastSectionKey, applicationSection.fold("")(identity))
                cachingConnector.storeSingleValue(ApplicationKickOutHelper.applicationLastIDKey, propertyID)
                kickoutRedirectLocation
              }
            })
          case _ =>
            Logger.warn("Problem saving Application details. Redirecting to InternalServerError")
            InternalServerError
        }
      }
    }
  }

  def addPropertyToPropertyList(property: Property, propertyList: List[Property]): (List[Property], String) = {

    val seekID = property.id.getOrElse("")

    propertyList.find(x => x.id.getOrElse("") equals seekID) match {
      case None =>
        val nextID = nextId(propertyList)
        val updatedList = propertyList :+ property.copy(id = Some(nextID))

        (updatedList, nextID)
      case Some(matchedProperty) =>
        val updatedProperty = matchedProperty.copy(id = property.id, address = property.address)
        val updatedList: List[Property] = propertyList.updated(propertyList.indexOf(matchedProperty), updatedProperty)

        (updatedList, seekID)
    }
  }
}

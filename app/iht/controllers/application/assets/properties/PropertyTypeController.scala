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

package iht.controllers.application.assets.properties

import iht.config.AppConfig
import iht.connector.{CachingConnector, IhtConnector}
import iht.controllers.application.EstateController
import iht.forms.ApplicationForms._
import iht.metrics.IhtMetrics
import iht.models._
import iht.models.application.ApplicationDetails
import iht.models.application.assets.Property
import iht.utils._
import javax.inject.Inject
import play.api.mvc.{Call, MessagesControllerComponents, Request, Result}
import uk.gov.hmrc.auth.core.AuthConnector
import uk.gov.hmrc.auth.core.retrieve.v2.Retrievals.{nino => ninoRetrieval}
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController
import iht.views.html.application.asset.properties.property_type

import scala.concurrent.Future

class PropertyTypeControllerImpl @Inject()(val metrics: IhtMetrics,
                                           val ihtConnector: IhtConnector,
                                           val cachingConnector: CachingConnector,
                                           val authConnector: AuthConnector,
                                           val propertyTypeView: property_type,
                                           implicit val appConfig: AppConfig,
                                           val cc: MessagesControllerComponents) extends FrontendController(cc) with PropertyTypeController

trait PropertyTypeController extends EstateController with StringHelper {

  override val applicationSection = Some(ApplicationKickOutHelper.ApplicationSectionProperties)

  def cancelUrl = routes.PropertyDetailsOverviewController.onPageLoad

  lazy val submitUrl = routes.PropertyTypeController.onSubmit

  def editCancelUrl(id: String) = routes.PropertyDetailsOverviewController.onEditPageLoad(id)

  def editSubmitUrl(id: String) = routes.PropertyTypeController.onEditSubmit(id)

  def ihtConnector: IhtConnector

  def cachingConnector: CachingConnector

  def locationAfterSuccessfulSave(id: String) = CommonHelper.addFragmentIdentifier(
    routes.PropertyDetailsOverviewController.onEditPageLoad(id), Some(appConfig.AssetsPropertiesPropertyKindID))
  val propertyTypeView: property_type
  def onPageLoad = authorisedForIht {
    implicit request => {
      withRegistrationDetails { regDetails =>
        val deceasedName = DeceasedInfoHelper.getDeceasedNameOrDefaultString(regDetails)
        Future.successful(Ok(propertyTypeView(
          propertyTypeForm,
          cancelUrl,
          submitUrl,
          deceasedName))
        )
      }
    }
  }

  def onEditPageLoad(id: String) = authorisedForIhtWithRetrievals(ninoRetrieval) { userNino =>
    implicit request => {
      withRegistrationDetails { registrationData =>
        val deceasedName = DeceasedInfoHelper.getDeceasedNameOrDefaultString(registrationData)
        for {
          applicationDetails <- ihtConnector.getApplication(getNino(userNino),
            CommonHelper.getOrExceptionNoIHTRef(registrationData.ihtReference),
            registrationData.acknowledgmentReference)
        } yield {
          applicationDetails match {
            case Some(applicationDetails) => {
              applicationDetails.propertyList.find(property => property.id.getOrElse("") equals id).fold {
                throw new RuntimeException("No Property found for the id")
              } {
                (matchedProperty) =>
                  Ok(propertyTypeView(
                    propertyTypeForm.fill(matchedProperty),
                    editCancelUrl(id),
                    editSubmitUrl(id),
                    deceasedName))
              }
            }
            case _ => {
              logger.warn("Problem retrieving Application Details. Redirecting to Internal Server Error")
              InternalServerError("No Application Details found")
            }
          }
        }
      }
    }
  }

  private def doSubmit(redirectLocationIfErrors: Call,
                       submitUrl: Call,
                       cancelUrl: Call,
                       userNino: Option[String],
                       propertyId: Option[String] = None)(
                        implicit request: Request[_]) = {

    withRegistrationDetails { regDetails =>
      val deceasedName = DeceasedInfoHelper.getDeceasedNameOrDefaultString(regDetails)

      val boundForm = propertyTypeForm.bindFromRequest
      boundForm.fold(
        formWithErrors => {
          LogHelper.logFormError(formWithErrors)
          Future.successful(BadRequest(propertyTypeView(formWithErrors,
            cancelUrl,
            submitUrl,
            deceasedName)))
        },
        property => {
          processSubmit(getNino(userNino), property, propertyId)
        }
      )
    }
  }

  def processSubmit(nino: String,
                    property: Property,
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
          case Some(_) => {
            Redirect(ad.kickoutReason.fold(locationAfterSuccessfulSave(propertyID)) {
              _ => {
                cachingConnector.storeSingleValue(ApplicationKickOutHelper.applicationLastSectionKey, applicationSection.fold("")(identity))
                cachingConnector.storeSingleValue(ApplicationKickOutHelper.applicationLastIDKey, propertyID)
                kickoutRedirectLocation
              }
            })
          }
          case _ => {
            logger.warn("Problem saving Application details. Redirecting to InternalServerError")
            InternalServerError
          }
        }
      }
    }
  }

  def onSubmit = authorisedForIhtWithRetrievals(ninoRetrieval) { userNino =>
    implicit request => {
      doSubmit(
        redirectLocationIfErrors = routes.PropertyTypeController.onSubmit,
        submitUrl = submitUrl,
        cancelUrl = cancelUrl,
        userNino)
    }
  }

  def onEditSubmit(id: String) = authorisedForIhtWithRetrievals(ninoRetrieval) { userNino =>
    implicit request => {
      doSubmit(
        redirectLocationIfErrors = routes.PropertyTypeController.onEditSubmit(id),
        submitUrl = editSubmitUrl(id),
        cancelUrl = editCancelUrl(id),
        userNino,
        Some(id))
    }
  }

  def addPropertyToPropertyList(property: Property, propertyList: List[Property]): (List[Property], String) = {

    val seekID = property.id.getOrElse("")
    propertyList.find(property => property.id.getOrElse("") equals seekID) match {
      case None => {
        val nextID = nextId(propertyList)
        val updatedList = propertyList :+ property.copy(id = Some(nextID))
        (updatedList, nextID)
      }
      case Some(matchedProperty) => {
        val updatedProperty = matchedProperty.copy(id = property.id, propertyType = property.propertyType)
        val updatedList: List[Property] = propertyList.updated(propertyList.indexOf(matchedProperty), updatedProperty)
        (updatedList, seekID)
      }
    }
  }
}

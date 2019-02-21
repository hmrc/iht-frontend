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

package iht.controllers.application.debts

import iht.connector.{CachingConnector, IhtConnector}
import iht.constants.IhtProperties._
import iht.controllers.application.ApplicationController
import iht.forms.ApplicationForms._
import iht.models._
import iht.models.application.ApplicationDetails
import iht.models.application.assets.Property
import iht.models.application.debts._
import iht.utils.{ApplicationStatus, CommonHelper, StringHelper}
import javax.inject.Inject
import play.api.Logger
import play.api.Play.current
import play.api.i18n.Messages.Implicits._
import play.api.mvc.{Request, Result}
import uk.gov.hmrc.auth.core.AuthConnector
import uk.gov.hmrc.auth.core.retrieve.v2.Retrievals.{nino => ninoRetrieval}
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.partials.FormPartialRetriever

import scala.concurrent.Future

class MortgageValueControllerImpl @Inject()(val cachingConnector: CachingConnector,
                                            val ihtConnector: IhtConnector,
                                            val authConnector: AuthConnector,
                                            override implicit val formPartialRetriever: FormPartialRetriever) extends MortgageValueController

trait MortgageValueController extends ApplicationController {


  def cachingConnector: CachingConnector

  def ihtConnector: IhtConnector

  def onSubmitUrl(id: String) = iht.controllers.application.debts.routes.MortgageValueController.onSubmit(id)

  def onPageLoad(id: String) = authorisedForIhtWithRetrievals(ninoRetrieval) { userNino =>

      implicit request => {
        withRegistrationDetails { regDetails =>
          for {
            applicationDetails <- ihtConnector.getApplication(StringHelper.getNino(userNino),
              CommonHelper.getOrExceptionNoIHTRef(regDetails.ihtReference),
              regDetails.acknowledgmentReference)
          } yield {
            applicationDetails match {
              case Some(appDetails) =>
                val propertyList = appDetails.propertyList
                val matchedProperty: Property = appDetails.propertyList.find(property => property.id.getOrElse("") equals id).fold {
                  throw new RuntimeException("No Property found for the id")
                }(identity)
                val allLiabilities = appDetails.allLiabilities.getOrElse(new AllLiabilities())
                val mortgageList = allLiabilities.mortgages.map(x => x.mortgageList).getOrElse(List.empty[Mortgage])
                val updatedMortgageList = updateMortgageListFromPropertyList(propertyList, mortgageList)

                resultOnUpdateMortgageList(updatedMortgageList, id, matchedProperty, regDetails)
              case _ =>
                Logger.warn("Problem retrieving Application Details. Redirecting to Internal Server Error")
                InternalServerError("No Application Details found")
            }
          }
        }
      }
  }

  def resultOnUpdateMortgageList(updatedMortgageList: Option[List[Mortgage]], id: String,
                                 matchedProperty: Property, regDetails: RegistrationDetails)(implicit request: Request[_]): Result = {
    updatedMortgageList match {
      case Some(mortList) =>
        mortList.find(mortgage => mortgage.id equals id).fold {
          throw new RuntimeException("No Mortgage found for the id")
        }((matchedMortgage) => Ok(iht.views.html.application.debts.mortgage_value(mortgagesForm.fill(matchedMortgage),
          matchedProperty, onSubmitUrl(id), regDetails)))
      case _ => Ok(iht.views.html.application.debts.mortgage_value(
        mortgagesForm.fill(Mortgage(id, None, None)),
        matchedProperty,
        onSubmitUrl(id),
        regDetails))
    }
  }

  def onSubmit(id: String) = authorisedForIhtWithRetrievals(ninoRetrieval) { userNino =>

      implicit request => {
        withRegistrationDetails { regDetails =>
          val applicationDetailsFuture = ihtConnector.getApplication(StringHelper.getNino(userNino),
            CommonHelper.getOrExceptionNoIHTRef(regDetails.ihtReference),
            regDetails.acknowledgmentReference)

          val boundForm = mortgagesForm.bindFromRequest

          applicationDetailsFuture.flatMap {
            case Some(appDetails) => {

              val matchedProperty = appDetails.propertyList.find(property => property.id.getOrElse("") equals id).fold {
                throw new RuntimeException("No Property found for the id")
              }(identity)

              boundForm.fold(
                formWithErrors => {
                  Future.successful(BadRequest(iht.views.html.application.debts.mortgage_value(formWithErrors,
                    matchedProperty,
                    onSubmitUrl(id),
                    regDetails)))
                },
                mortgage => {
                  val newMort = mortgage.copy(id = id)
                  saveApplication(StringHelper.getNino(userNino), id, newMort, appDetails, regDetails)
                }
              )
            }
            case _ => Future.successful(InternalServerError("Application details not found"))
          }
        }
      }
  }

  private def saveApplication(nino: String,
                              id: String,
                              mortgage: Mortgage,
                              appDetails: ApplicationDetails,
                              regDetails: RegistrationDetails)(implicit request: Request[_],
                                                               hc: HeaderCarrier): Future[Result] = {

    val updatedLiabilities: AllLiabilities = appDetails.allLiabilities.fold(new AllLiabilities(mortgages = Some(MortgageEstateElement(None, List(mortgage))))) {
      x =>
        x.copy(mortgages = Some(x.mortgages.fold(MortgageEstateElement(None, List(mortgage))) {
          mortgageEstateModel =>
            mortgageEstateModel.mortgageList.size match {
              case x if x > 0 => {
                val mortList = mortgageEstateModel.mortgageList
                val updatedMortgageList = updateMortgageList(mortgage, mortList)
                mortgageEstateModel.copy(mortgageList = updatedMortgageList)
              }
              case _ => mortgageEstateModel.copy(mortgageList = List(mortgage))
            }
        }))
    }

    val applicationDetailsFuture = ihtConnector.getApplication(nino,
      CommonHelper.getOrExceptionNoIHTRef(regDetails.ihtReference),
      regDetails.acknowledgmentReference)

    applicationDetailsFuture.flatMap {
      case Some(x) => {
        val updatedAppDetails = x.copy(status = ApplicationStatus.InProgress, allLiabilities = Some(updatedLiabilities))

        ihtConnector.saveApplication(nino, updatedAppDetails, regDetails.acknowledgmentReference) map (_ =>
          Redirect(CommonHelper.addFragmentIdentifier(routes.MortgagesOverviewController.onPageLoad, Some(DebtsMortgagesPropertyID + id))))
      }
      case _ => {
        Logger.warn("Application Details not found")
        Future.successful(InternalServerError)
      }
    }

  }


  /**
    * Updates the Mortgage List
    */
  private def updateMortgageList(mortgage: Mortgage, mortgageList: List[Mortgage]): List[Mortgage] = {

    mortgageList.find(_.id equals mortgage.id) match {
      case Some(matchedMortgage) => {
        val updatedProperty = matchedMortgage.copy(id = mortgage.id, value = mortgage.value, isOwned = mortgage.isOwned)
        mortgageList.updated(mortgageList.indexOf(matchedMortgage), updatedProperty)
      }
      case None => {
        Logger.error("No Mortgage exists for the property")
        mortgageList :+ mortgage
      }
    }
  }


  /**
    * Copy of the mortgage list updated from properties
    */
  private def updateMortgageListFromPropertyList(propertyList: List[Property], mortgageList: List[Mortgage]): Option[List[Mortgage]] = {
    val propertyIdList: Set[String] = propertyList.map(p => p.id.getOrElse("")).toSet
    val mortgageIdList: Set[String] = mortgageList.map(m => m.id).toSet
    if (propertyIdList.union(mortgageIdList) != propertyIdList) throw new RuntimeException("MortgageList>PropertyList")
    val a: List[Mortgage] = propertyIdList.diff(mortgageIdList).map(id => Mortgage(id, None)).toList
    Some(mortgageList ::: a)
  }
}

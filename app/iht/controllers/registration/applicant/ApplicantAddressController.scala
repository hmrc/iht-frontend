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

package iht.controllers.registration.applicant

import iht.config.AppConfig
import iht.connector.{CachingConnector, IhtConnector}
import iht.controllers.registration.RegistrationController
import iht.controllers.registration.executor.{routes => executorRoutes}
import iht.forms.registration.ApplicantForms._
import iht.utils.CommonHelper
import iht.views.html.registration.applicant.applicant_address
import javax.inject.Inject
import play.api.mvc.{Call, MessagesControllerComponents}
import uk.gov.hmrc.auth.core.AuthConnector
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController

import scala.concurrent.Future

class ApplicantAddressControllerImpl @Inject()(val ihtConnector: IhtConnector,
                                               val cachingConnector: CachingConnector,
                                               val authConnector: AuthConnector,
                                               val applicantAddressView: applicant_address,
                                               implicit val appConfig: AppConfig,
                                               val cc: MessagesControllerComponents) extends FrontendController(cc) with ApplicantAddressController {

}

trait ApplicantAddressController extends RegistrationController {
  def cachingConnector: CachingConnector
  override def guardConditions: Set[Predicate] = guardConditionsApplicantAddress

  lazy val loadRouteUk = routes.ApplicantAddressController.onPageLoadUk
  lazy val loadRouteAbroad = routes.ApplicantAddressController.onPageLoadAbroad
  lazy val editLoadRouteUk = routes.ApplicantAddressController.onEditPageLoadUk
  lazy val editLoadRouteAbroad = routes.ApplicantAddressController.onEditPageLoadAbroad
  lazy val submitRouteUk = routes.ApplicantAddressController.onSubmitUk
  lazy val submitRouteAbroad = routes.ApplicantAddressController.onSubmitAbroad
  lazy val editSubmitRouteUk = routes.ApplicantAddressController.onEditSubmitUk
  lazy val editSubmitRouteAbroad = routes.ApplicantAddressController.onEditSubmitAbroad
  lazy val nextPageRoute = executorRoutes.OthersApplyingForProbateController.onPageLoad

  def onPageLoadUk = pageLoad(isInternational = false, submitRouteUk, loadRouteAbroad)
  def onPageLoadAbroad = pageLoad(isInternational = true, submitRouteAbroad, loadRouteUk)
  def onEditPageLoadUk = pageLoad(isInternational = false, editSubmitRouteUk, editLoadRouteAbroad, cancelToRegSummary)
  def onEditPageLoadAbroad = pageLoad(isInternational = true, editSubmitRouteAbroad, editLoadRouteUk, cancelToRegSummary)
  val applicantAddressView: applicant_address
  private def pageLoad(isInternational: Boolean, actionCall: Call, changeNationalityCall: Call,
                       cancelCall: Option[Call] = None) = authorisedForIht {
    implicit request => {
      withRegistrationDetailsRedirectOnGuardCondition { rd =>
        val formType = if(isInternational) applicantAddressAbroadForm else applicantAddressUkForm
        val ad = CommonHelper.getOrException(rd.applicantDetails)

        val form = if (CommonHelper.getOrException(ad.doesLiveInUK) == isInternational) {
          formType
        } else {
          ad.ukAddress.fold(formType)(address => formType.fill(address))
        }

        Future.successful(Ok(applicantAddressView(form, isInternational, actionCall, changeNationalityCall, cancelCall)))
      }
    }
  }

  def onSubmitUk = onSubmit(isInternational = false, nextPageRoute, submitRouteUk, loadRouteAbroad)
  def onSubmitAbroad = onSubmit(isInternational = true, nextPageRoute, submitRouteAbroad, loadRouteUk)
  def onEditSubmitUk = onSubmit(isInternational = false, regSummaryRoute, editSubmitRouteUk, editLoadRouteAbroad,
                                cancelToRegSummary)
  def onEditSubmitAbroad = onSubmit(isInternational = true, regSummaryRoute, editSubmitRouteAbroad, editLoadRouteUk,
                                    cancelToRegSummary)

  private def onSubmit(isInternational: Boolean, actionCall: Call, onFailureActionCall: Call, changeNationalityCall: Call,
                       cancelCall: Option[Call] = None) = authorisedForIht {
    implicit request => {
      withRegistrationDetailsRedirectOnGuardCondition { rd =>
        val boundForm =
          if (isInternational) applicantAddressAbroadForm.bindFromRequest else applicantAddressUkForm.bindFromRequest

        boundForm.fold(formWithErrors => {
          Future.successful(BadRequest(applicantAddressView(formWithErrors,
            isInternational, onFailureActionCall, changeNationalityCall, cancelCall)))
        },

          details => {
            val ad = CommonHelper.getOrException(rd.applicantDetails) copy (ukAddress = Some(details),
              doesLiveInUK = Some(!isInternational))
            val copyOfRd = rd copy (applicantDetails = Some(ad))

            storeRegistrationDetails(copyOfRd, actionCall, "Storing registration details fails during application address submit")
          }
        )
      }
    }
  }
}

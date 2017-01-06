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

package iht.controllers.registration

import iht.connector.CachingConnector
import iht.controllers.{ControllerHelper, IhtConnectors}
import iht.utils.CommonHelper
import play.api.Logger

import scala.concurrent.Future


object CompletedRegistrationController extends CompletedRegistrationController with IhtConnectors

trait CompletedRegistrationController extends RegistrationController{
  def cachingConnector: CachingConnector
  override def guardConditions: Set[Predicate] = Set.empty

  def onPageLoad() = authorisedForIht {
    implicit user =>implicit request => {
      for {
        registrationDetailsResponse <- cachingConnector.getRegistrationDetails
        ihtReference = CommonHelper.getOrExceptionNoRegistration(registrationDetailsResponse).ihtReference
      } yield {
        val ihtReferenceForPage = ihtReference.getOrElse(ControllerHelper.ihtReferenceErrorString)
        if (ihtReferenceForPage.equals(ControllerHelper.ihtReferenceErrorString)) {
          Logger.warn("Iht Reference not found")
        }
        Ok(iht.views.html.registration.completed_registration(ihtReferenceForPage))
      }
    }
  }

  def onSubmit = authorisedForIht {
    implicit user =>implicit request => {
      Future(Redirect(iht.controllers.home.routes.IhtHomeController.onPageLoad()))
    }
  }
}

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

package iht.controllers.registration

import iht.connector.CachingConnector
import iht.controllers.ControllerHelper.Mode
import iht.controllers.registration.{routes => registrationRoutes}
import iht.models.RegistrationDetails
import iht.utils.DeceasedInfoHelper
import iht.utils.RegistrationKickOutHelper._
import play.api.data.Form
import play.api.mvc.{AnyContent, Call, Request, Result}

import scala.concurrent.Future

trait RegistrationBaseController[T] extends RegistrationController {
  def getLang(request: Request[AnyContent]) = language(request)

  def cachingConnector: CachingConnector

  def form: Form[T]

  def okForPageLoad(form: Form[T], name: Option[String] = None)(implicit request: Request[AnyContent]): Result

  def badRequestForSubmit(form: Form[T], name: Option[String] = None)(implicit request: Request[AnyContent]): Result

  def applyChangesToRegistrationDetails(original: RegistrationDetails, details: T, mode: Mode.Value): RegistrationDetails

  def onwardRoute(rd: RegistrationDetails): Call

  def onwardRouteInEditMode(rd: RegistrationDetails): Call = registrationRoutes.RegistrationSummaryController.onPageLoad

  def onPageLoad = pageLoad(Mode.Standard)

  def onSubmit = submit(Mode.Standard)

  def fillForm(rd: RegistrationDetails): Form[T]

  def getKickoutReason: RegistrationDetails => Option[String] = noKickoutCheck

  val storageFailureMessage: String = "Failed to successfully store registration details"

  // This method provides a hook to allow controllers to apply extra form validation if needed.  By default
  // it simply passes back the unmodified form
  def performAdditionalValidation(form: Form[T], rd: RegistrationDetails, mode: Mode.Value): Form[T] = form

  def pageLoad(mode: Mode.Value) = authorisedForIht {
    implicit request =>
      withRegistrationDetailsRedirectOnGuardCondition { rd =>
        val deceasedName = DeceasedInfoHelper.getDeceasedNameOrDefaultString(rd)
        Future.successful(okForPageLoad(fillForm(rd), Some(deceasedName)))
      }
  }

  def submit(mode: Mode.Value) =
    authorisedForIht {
      implicit request => {
        withRegistrationDetailsRedirectOnGuardCondition { rd =>
          val boundForm = performAdditionalValidation(form.bindFromRequest, rd, mode)
          val deceasedName = DeceasedInfoHelper.getDeceasedNameOrDefaultString(rd)
          boundForm.fold(
            formWithErrors => Future.successful(badRequestForSubmit(formWithErrors, Some(deceasedName)))
            ,
            details => {
              val copyOfRd = applyChangesToRegistrationDetails(rd, details, mode)
              storeAndRedirectWithKickoutCheck(cachingConnector, copyOfRd, getKickoutReason, onwardRoute(copyOfRd), storageFailureMessage)
            }
          )
        }
      }
    }
}

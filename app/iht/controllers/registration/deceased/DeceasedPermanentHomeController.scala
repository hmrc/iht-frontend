/*
 * Copyright 2020 HM Revenue & Customs
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

package iht.controllers.registration.deceased

import iht.config.AppConfig
import iht.connector.{CachingConnector, IhtConnector}
import iht.controllers.ControllerHelper.Mode
import iht.forms.registration.DeceasedForms._
import iht.models.{DeceasedDetails, RegistrationDetails}
import iht.views.html.registration.{deceased => views}
import javax.inject.Inject
import play.api.data.Form
import play.api.i18n.Messages
import play.api.mvc.{MessagesControllerComponents, _}
import uk.gov.hmrc.auth.core.AuthConnector
import uk.gov.hmrc.play.bootstrap.controller.FrontendController
import uk.gov.hmrc.play.partials.FormPartialRetriever

class DeceasedPermanentHomeControllerImpl @Inject()(val ihtConnector: IhtConnector,
                                                    val cachingConnector: CachingConnector,
                                                    val authConnector: AuthConnector,
                                                    val formPartialRetriever: FormPartialRetriever,
                                                    implicit val appConfig: AppConfig,
                                                    val cc: MessagesControllerComponents) extends FrontendController(cc) with DeceasedPermanentHomeController

trait DeceasedPermanentHomeController extends RegistrationDeceasedControllerWithEditMode {
  def form(implicit messages: Messages) = deceasedPermanentHomeForm

  override def guardConditions: Set[Predicate] = guardConditionsDeceasedPermanentHome

  override def getKickoutReason = kickoutReasonDeceasedDetails

  override val storageFailureMessage = "Storage of registration details fails during deceased permanent home submission"


  lazy val submitRoute = routes.DeceasedPermanentHomeController.onSubmit
  lazy val editSubmitRoute = routes.DeceasedPermanentHomeController.onEditSubmit

  def okForPageLoad(form: Form[DeceasedDetails], name: Option[String])(implicit request: Request[AnyContent]) = {
    Ok(views.deceased_permanent_home(form, submitRoute))
  }

  def okForEditPageLoad(form: Form[DeceasedDetails], name: Option[String])(implicit request: Request[AnyContent]) =
    Ok(views.deceased_permanent_home(form, editSubmitRoute, cancelToRegSummary))

  def badRequestForSubmit(form: Form[DeceasedDetails], name: Option[String])(implicit request: Request[AnyContent]) =
    BadRequest(views.deceased_permanent_home(form, submitRoute))

  def badRequestForEditSubmit(form: Form[DeceasedDetails], name: Option[String])(implicit request: Request[AnyContent]) =
    BadRequest(views.deceased_permanent_home(form, editSubmitRoute, cancelToRegSummary))

  def onwardRoute(rd: RegistrationDetails) = routes.AboutDeceasedController.onPageLoad

  def applyChangesToRegistrationDetails(rd: RegistrationDetails, dd: DeceasedDetails, mode: Mode.Value) = {
    val x = rd.deceasedDetails.getOrElse(new DeceasedDetails) copy (domicile = dd.domicile)
    rd copy (deceasedDetails = Some(x))
  }
}

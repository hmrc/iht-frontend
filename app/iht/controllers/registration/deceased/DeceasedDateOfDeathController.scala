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

package iht.controllers.registration.deceased

import iht.config.AppConfig
import iht.connector.{CachingConnector, IhtConnector}
import iht.controllers.ControllerHelper.Mode
import iht.controllers.registration.RegistrationBaseControllerWithEditMode
import iht.forms.registration.DeceasedForms._
import iht.models.{DeceasedDateOfDeath, RegistrationDetails}
import iht.views.html.registration.{deceased => views}
import javax.inject.Inject
import org.joda.time.LocalDate
import play.api.data.{Form, FormError}
import play.api.i18n.Messages
import play.api.mvc.{MessagesControllerComponents, _}
import uk.gov.hmrc.auth.core.AuthConnector
import uk.gov.hmrc.play.bootstrap.controller.FrontendController
import uk.gov.hmrc.play.partials.FormPartialRetriever

import scala.util.{Failure, Success, Try}

class DeceasedDateOfDeathControllerImpl @Inject()(val ihtConnector: IhtConnector,
                                                  val cachingConnector: CachingConnector,
                                                  val authConnector: AuthConnector,
                                                  val formPartialRetriever: FormPartialRetriever,
                                                  implicit val appConfig: AppConfig,
                                                  val cc: MessagesControllerComponents) extends FrontendController(cc) with DeceasedDateOfDeathController {

}

trait DeceasedDateOfDeathController extends RegistrationBaseControllerWithEditMode[DeceasedDateOfDeath] {
  def fillForm(rd: RegistrationDetails)(implicit request: Request[_]) = rd.deceasedDateOfDeath.fold(form)(dd => form.fill(dd))

  def form(implicit messages: Messages) = deceasedDateOfDeathForm

  override def guardConditions: Set[Predicate] = Set((_, _) => true)

  override def getKickoutReason = kickoutReasonDeceasedDateOfDeath

  override val storageFailureMessage = "Storage of registration details fails during deceased date of death submission"

  lazy val submitRoute = routes.DeceasedDateOfDeathController.onSubmit
  lazy val editSubmitRoute = routes.DeceasedDateOfDeathController.onEditSubmit

  def okForPageLoad(form: Form[DeceasedDateOfDeath], name: Option[String])(implicit request: Request[AnyContent]) =
    Ok(views.deceased_date_of_death(form, submitRoute))

  def okForEditPageLoad(form: Form[DeceasedDateOfDeath], name: Option[String])(implicit request: Request[AnyContent]) =
    Ok(views.deceased_date_of_death(form, editSubmitRoute, cancelToRegSummary))

  def badRequestForSubmit(form: Form[DeceasedDateOfDeath], name: Option[String])(implicit request: Request[AnyContent]) =
    BadRequest(views.deceased_date_of_death(form, submitRoute))

  def badRequestForEditSubmit(form: Form[DeceasedDateOfDeath], name: Option[String])(implicit request: Request[AnyContent]) =
    BadRequest(views.deceased_date_of_death(form, editSubmitRoute, cancelToRegSummary))

  def onwardRoute(rd: RegistrationDetails) = routes.DeceasedPermanentHomeController.onPageLoad

  def applyChangesToRegistrationDetails(rd: RegistrationDetails, dd: DeceasedDateOfDeath, mode: Mode.Value) =
    rd copy (deceasedDateOfDeath = Some(dd))

  override def performAdditionalValidation(form: Form[DeceasedDateOfDeath], rd: RegistrationDetails, mode: Mode.Value) = mode match {
    case Mode.Standard => form
    case _ => {
      val dob = rd.deceasedDetails.flatMap(_.dateOfBirth).fold(
        throw new RuntimeException("Date of birth not found"))(identity)
      compareDateOfBirthToDateOfDeath(form, dob)
    }
  }

  def compareDateOfBirthToDateOfDeath(boundForm: Form[DeceasedDateOfDeath], dateOfBirth: LocalDate): Form[DeceasedDateOfDeath] = {
    val dateOfDeathDay = boundForm("dateOfDeath.day").value
    val dateOfDeathMonth = boundForm("dateOfDeath.month").value
    val dateOfDeathYear = boundForm("dateOfDeath.year").value

    val dateOfDeath = Try(new LocalDate(dateOfDeathYear.getOrElse("").toInt,
      dateOfDeathMonth.getOrElse("").toInt,
      dateOfDeathDay.getOrElse("").toInt))

    dateOfDeath match {
      case Success(v) => {
        if (!dateOfBirth.isBefore(v) && !dateOfBirth.isEqual(v)) {
          boundForm.copy(errors = boundForm.errors.+:(FormError("dateOfDeath",
            "error.dateOfDeath.giveAfterDateOfBirth")))
        } else {
          boundForm
        }
      }
      case Failure(e) => {
        boundForm
      }
    }
  }
}

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

package iht.controllers.application

import iht.connector.{CachingConnector, IhtConnector}
import iht.models._
import iht.models.application.ApplicationDetails
import iht.models.application.assets._
import iht.models.application.exemptions._
import iht.utils.ApplicationKickOutHelper.FunctionListMap
import iht.utils.{ApplicationKickOutHelper, CommonHelper}
import play.api.Logger
import play.api.data.{Form, FormError}
import play.api.mvc.{Call, Request, Result}
import play.twirl.api.HtmlFormat._
import uk.gov.hmrc.play.frontend.auth.AuthContext
import uk.gov.hmrc.play.http.HeaderCarrier

import scala.concurrent.Future
import scala.util.{Failure, Success, Try}

trait EstateController extends ApplicationController {

  val applicationSection: Option[String] = None

  def cachingConnector: CachingConnector

  def ihtConnector: IhtConnector

  val assetsRedirectLocation =
    iht.controllers.application.assets.routes.AssetsOverviewController.onPageLoad()
  val insurancePoliciesRedirectLocation =
    iht.controllers.application.assets.insurancePolicy.routes.InsurancePolicyOverviewController.onPageLoad()
  val debtsRedirectLocation = iht.controllers.application.debts.routes.DebtsOverviewController.onPageLoad()
  val giftsRedirectLocation = iht.controllers.application.gifts.routes.GiftsOverviewController.onPageLoad()
  val kickoutRedirectLocation = iht.controllers.application.routes.KickoutController.onPageLoad()

  val retrieveSectionDetailsOrExceptionIfInvalidID: String => ApplicationDetails => Option[Charity] = id => ad => {
    val foundCharity = ad.charities.find(_.id.contains(id))
    Some(foundCharity.fold(throw new RuntimeException(""))(identity))
  }

  val retrieveQualifyingBodyDetailsOrExceptionIfInvalidID:
    String => ApplicationDetails => Option[QualifyingBody] = id => ad => {
    val foundID = ad.qualifyingBodies.find(_.id.contains(id))
    Some(foundID.fold(throw new RuntimeException(""))(identity))
  }

  val updateAllAssetsWithInsurancePolicy: (AllAssets,
    InsurancePolicy,
    InsurancePolicy => InsurancePolicy) => AllAssets =
    (allAssets, insurancePolicy, filterFunction: InsurancePolicy => InsurancePolicy) => {
      def booleanValue: (Option[Boolean], Option[BigDecimal], Option[BigDecimal]) => Option[BigDecimal] =
        (optBoolean, optBigDecimal, defaultBigDecimal) => {
          optBoolean match {
            case None => defaultBigDecimal
            case Some(false) => None
            case Some(true) => optBigDecimal
          }
        }

      def updateInsurancePolicy(insurancePolicy: InsurancePolicy, ip: InsurancePolicy,
                                filterFunction: InsurancePolicy => InsurancePolicy) = {

        val tempInsurancePolicy = insurancePolicy.copy(
          isAnnuitiesBought = ip.isAnnuitiesBought.fold[Option[Boolean]](insurancePolicy.isAnnuitiesBought)(xx => Some(xx)),
          isInsurancePremiumsPayedForSomeoneElse = ip.isInsurancePremiumsPayedForSomeoneElse.
            fold[Option[Boolean]](insurancePolicy.isInsurancePremiumsPayedForSomeoneElse)(xx => Some(xx)),
          value = booleanValue(ip.policyInDeceasedName, ip.value, insurancePolicy.value),
          shareValue = booleanValue(ip.isJointlyOwned, ip.shareValue, insurancePolicy.shareValue),
          policyInDeceasedName = ip.policyInDeceasedName.fold[Option[Boolean]](insurancePolicy.policyInDeceasedName)(xx => Some(xx)),
          isJointlyOwned = ip.isJointlyOwned.fold[Option[Boolean]](insurancePolicy.isJointlyOwned)(xx => Some(xx)),
          isInTrust = ip.isInTrust.fold[Option[Boolean]](insurancePolicy.isInTrust)(xx => Some(xx)),
          coveredByExemption = ip.coveredByExemption.fold[Option[Boolean]](insurancePolicy.coveredByExemption)(xx => Some(xx)),
          sevenYearsBefore = ip.sevenYearsBefore.fold[Option[Boolean]](insurancePolicy.sevenYearsBefore)(xx => Some(xx)),
          moreThanMaxValue = ip.moreThanMaxValue.fold[Option[Boolean]](insurancePolicy.moreThanMaxValue)(xx => Some(xx))
        )

        filterFunction(tempInsurancePolicy)
      }

      allAssets.copy(insurancePolicy = Some(
        updateInsurancePolicy(allAssets.insurancePolicy
          .fold(new InsurancePolicy(None, None, None, None, None, None, None, None, None, None))(identity),
          insurancePolicy,
          filterFunction)
      ))
    }


  def estateElementOnPageLoad[A](form: Form[A],
                                 retrievePageToDisplay: (Form[A], RegistrationDetails) => Appendable,
                                 retrieveSectionDetails: ApplicationDetails => Option[A])
                                (implicit request: Request[_], user: AuthContext) = {
    withRegistrationDetails { regDetails =>
      val applicationDetailsFuture = ihtConnector.getApplication(CommonHelper.getNino(user),
        CommonHelper.getOrExceptionNoIHTRef(regDetails.ihtReference),
        regDetails.acknowledgmentReference)

      applicationDetailsFuture.map {
        applicationDetails =>
          applicationDetails.fold(InternalServerError("Application details not found")) {
            (appDetails: ApplicationDetails) => {
              val fm = retrieveSectionDetails(appDetails).fold(form)(form.fill)
              Ok(retrievePageToDisplay(fm, regDetails))
            }
          }
      }
    }
  }

  /**
    * Loads the page in Edit mode with Id. Can be used for properties, charities and qualifying bodies.
    */
  def estateElementOnEditPageLoadWithNavigation[A](form: Form[A],
                                                   retrievePageToDisplay: (Form[A],
                                                     RegistrationDetails, Call, Call) => Appendable,
                                                   retrieveSectionDetails: ApplicationDetails => Option[A],
                                                   submit: Call,
                                                   cancel: Call)(implicit request: Request[_], user: AuthContext) = {

    withRegistrationDetails { regDetails =>
      val applicationDetailsFuture = ihtConnector.getApplication(CommonHelper.getNino(user),
        CommonHelper.getOrExceptionNoIHTRef(regDetails.ihtReference),
        regDetails.acknowledgmentReference)

      applicationDetailsFuture.map {
        applicationDetails =>
          applicationDetails.fold(InternalServerError("Application details not found")) {
            (appDetails: ApplicationDetails) => {
              val fm = retrieveSectionDetails(appDetails).fold(form)(form.fill)
              Ok(retrievePageToDisplay(fm, regDetails, submit, cancel))
            }
          }
      }
    }
  }

  /**
    * id: Is being used in case of properties, charities and qualifying bodies. Can be kept as _ where you don't need it.
    */
  def estateElementOnSubmit[A](form: Form[A],
                               retrievePageToDisplay: (Form[A], RegistrationDetails) => Appendable,
                               updateApplicationDetails: (ApplicationDetails, Option[String], A) => (ApplicationDetails, Option[String]),
                               redirectLocation: Call,
                               formValidation: Option[Form[A] => Option[FormError]] = None,
                               id: Option[String] = None)
                              (implicit request: Request[_], user: AuthContext): Future[Result] = {

    val conditionalRedirect: (ApplicationDetails, Option[String]) => Call = (_, _) => redirectLocation
    estateElementOnSubmitConditionalRedirect[A](form, retrievePageToDisplay,
      updateApplicationDetails,
      conditionalRedirect,
      formValidation, id)
  }

  /**
    * id: Is being used in case of properties, charities and qualifying bodies. Can be kept as _ where you don't need it.
    */
  def estateElementOnSubmitConditionalRedirect[A](form: Form[A],
                                                  retrievePageToDisplay: (Form[A], RegistrationDetails) => Appendable,
                                                  updateApplicationDetails: (ApplicationDetails,
                                                    Option[String], A) => (ApplicationDetails, Option[String]),
                                                  redirectLocation: (ApplicationDetails, Option[String]) => Call,
                                                  formValidation: Option[Form[A] => Option[FormError]] = None,
                                                  id: Option[String] = None)
                                                 (implicit request: Request[_], user: AuthContext): Future[Result] = {
    withRegistrationDetails { regDetails =>
      val boundFormBeforeValidation = form.bindFromRequest

      val boundForm = formValidation.flatMap(_ (boundFormBeforeValidation)) match {
        case None => boundFormBeforeValidation
        case Some(formError) => Form(
          boundFormBeforeValidation.mapping,
          boundFormBeforeValidation.data,
          Seq(formError),
          boundFormBeforeValidation.value)
      }

      boundForm.fold(
        formWithErrors => {
          Future.successful(BadRequest(retrievePageToDisplay(formWithErrors, regDetails)))
        },
        estateElementModel => {
          estatesSaveApplication(CommonHelper.getNino(user),
            estateElementModel,
            regDetails,
            updateApplicationDetails,
            redirectLocation = redirectLocation,
            id
          )
        }
      )
    }
  }

  /**
    * id: Is being used in case of properties, charities and qualifying bodies.
    */
  protected def estatesSaveApplication[A](nino: String,
                                        estateElementModel: A,
                                        regDetails: RegistrationDetails,
                                        updateApplicationDetails: (ApplicationDetails, Option[String], A) => (ApplicationDetails, Option[String]),
                                        redirectLocation: (ApplicationDetails, Option[String]) => Call,
                                        id: Option[String])
                                       (implicit request: Request[_], hc: HeaderCarrier): Future[Result] = {

    val futureOptionApplicationDetails = ihtConnector.getApplication(nino,
      CommonHelper.getOrExceptionNoIHTRef(regDetails.ihtReference),
      regDetails.acknowledgmentReference)

    futureOptionApplicationDetails.flatMap {
      case Some(appDetails) =>
        val tryResult = Try {
          val updateTuple = updateApplicationDetails(appDetails, id, estateElementModel)
          (updateKickout(registrationDetails = regDetails, applicationDetails = updateTuple._1), updateTuple._2)
        }

        tryResult match {
          case Success(updateTuple) =>
            val applicationDetails = updateTuple._1
            val updatedID = updateTuple._2
            ihtConnector.saveApplication(nino, applicationDetails, regDetails.acknowledgmentReference)
              .map(optionApplicationDetailsSaved => {
                optionApplicationDetailsSaved.fold {
                  Logger.warn("Application Details not found")
                  InternalServerError("Application Details not found")
                }(_ => {
                  cachingConnector.storeSingleValueSync(ApplicationKickOutHelper.applicationLastSectionKey,
                    applicationSection.fold("")(identity))
                  Redirect(applicationDetails.kickoutReason.fold(redirectLocation(applicationDetails, updatedID))(_ =>
                    kickoutRedirectLocation))
                })
              })
          case Failure(ex) =>
            Logger.warn("Id " + id + " is unrecognized")
            Future.successful(InternalServerError("Id " + id + " is unrecognized"))

        }


      case _ => {
        Logger.warn("Application Details not found")
        Future.successful(InternalServerError("Application Details not found"))
      }
    }
  }

  /**
    * Updates the Kick out if applicable
    */
  def updateKickout(checks: FunctionListMap = ApplicationKickOutHelper.checksEstate,
                    registrationDetails: RegistrationDetails,
                    applicationDetails: ApplicationDetails,
                    applicationID: Option[String] = None)
                   (implicit request: Request[_], hc: HeaderCarrier): ApplicationDetails =
    ApplicationKickOutHelper.updateKickout(checks = checks,
      prioritySection = applicationSection,
      registrationDetails = registrationDetails,
      applicationDetails = applicationDetails,
      idForSectionTotal = applicationID)

  /**
    * Submits the page with Id and navigation urls.Can be used in properties, charities and qualifying bodies
    */

  def estateElementOnSubmitWithIdAndNavigation[A](
                                                   form: Form[A],
                                                   retrievePageToDisplay: (Form[A], RegistrationDetails, Call, Call) => Appendable,
                                                   updateApplicationDetails: (ApplicationDetails, Option[String], A) => (ApplicationDetails, Option[String]),
                                                   redirectLocation: (ApplicationDetails, Option[String]) => Call,
                                                   formValidation: Option[Form[A] => Option[FormError]] = None,
                                                   id: Option[String] = None,
                                                   submit: Call,
                                                   cancel: Call)(implicit request: Request[_], user: AuthContext): Future[Result] = {

    withRegistrationDetails { regDetails =>
      val boundFormBeforeValidation = form.bindFromRequest
      val boundForm = formValidation.flatMap(_ (boundFormBeforeValidation)) match {
        case None => boundFormBeforeValidation
        case Some(formError) => Form(
          boundFormBeforeValidation.mapping,
          boundFormBeforeValidation.data,
          Seq(formError),
          boundFormBeforeValidation.value)
      }

      boundForm.fold(
        formWithErrors => {
          Future.successful(BadRequest(retrievePageToDisplay(formWithErrors, regDetails, submit, cancel)))
        },
        estateElementModel => {
          estatesSaveApplication(CommonHelper.getNino(user),
            estateElementModel,
            regDetails,
            updateApplicationDetails,
            redirectLocation = redirectLocation,
            id
          )
        }
      )
    }
  }
}

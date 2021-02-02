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

package iht.controllers.application.exemptions.partner

import iht.config.AppConfig
import iht.connector.{CachingConnector, IhtConnector}
import iht.controllers.application.EstateController
import iht.forms.ApplicationForms._
import iht.models._
import iht.models.application.ApplicationDetails
import iht.models.application.exemptions._
import iht.utils.CommonHelper._
import iht.utils.{CommonHelper, IhtFormValidator}
import iht.views.html._
import iht.views.html.application.exemption.partner.partner_permanent_home_question
import javax.inject.Inject
import play.api.Logging
import play.api.i18n.Messages
import play.api.mvc.{Call, MessagesControllerComponents, Request, Result}
import uk.gov.hmrc.auth.core.AuthConnector
import uk.gov.hmrc.auth.core.retrieve.v2.Retrievals.{nino => ninoRetrieval}
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController
import uk.gov.hmrc.play.partials.FormPartialRetriever

import scala.concurrent.Future

class PartnerPermanentHomeQuestionControllerImpl @Inject()(val ihtConnector: IhtConnector,
                                                           val cachingConnector: CachingConnector,
                                                           val authConnector: AuthConnector,
                                                           val formPartialRetriever: FormPartialRetriever,
implicit val appConfig: AppConfig,
val cc: MessagesControllerComponents) extends FrontendController(cc) with PartnerPermanentHomeQuestionController {

}

trait PartnerPermanentHomeQuestionController extends EstateController with Logging {


  lazy val partnerPermanentHomePage = routes.PartnerPermanentHomeQuestionController.onPageLoad()
  lazy val exemptionsOverviewPage = addFragmentIdentifier(
    iht.controllers.application.exemptions.routes.ExemptionsOverviewController.onPageLoad(), Some(appConfig.ExemptionsPartnerHomeID))
  lazy val partnerOverviewPage = addFragmentIdentifier(routes.PartnerOverviewController.onPageLoad(), Some(appConfig.ExemptionsPartnerHomeID))

  def onPageLoad = authorisedForIhtWithRetrievals(ninoRetrieval) { userNino =>

      implicit request =>

        withRegistrationDetails { registrationDetails =>
          for {
            applicationDetails <- ihtConnector.getApplication(getNino(userNino),
              CommonHelper.getOrExceptionNoIHTRef(registrationDetails.ihtReference),
              registrationDetails.acknowledgmentReference)
          } yield {
            applicationDetails match {
              case Some(appDetails) => {

                val filledForm = appDetails.allExemptions.flatMap(_.partner)
                  .fold(partnerPermanentHomeQuestionForm)(partnerPermanentHomeQuestionForm.fill)

                Ok(partner_permanent_home_question(filledForm,
                  registrationDetails,
                  returnLabel(registrationDetails, appDetails),
                  returnUrl(registrationDetails, appDetails)
                ))
              }
              case _ => {
                logger.warn("Application Details not found")
                InternalServerError("Application details not found")
              }
            }
          }
        }
  }

  def onSubmit = authorisedForIhtWithRetrievals(ninoRetrieval) { userNino =>

      implicit request => {

        withRegistrationDetails { regDetails =>
          val boundForm = IhtFormValidator.addDeceasedNameToAllFormErrors(partnerPermanentHomeQuestionForm
            .bindFromRequest, regDetails.deceasedDetails.fold("")(_.name))

          val applicationDetailsFuture = ihtConnector.getApplication(getNino(userNino),
            CommonHelper.getOrExceptionNoIHTRef(regDetails.ihtReference),
            regDetails.acknowledgmentReference)

          applicationDetailsFuture.flatMap {
            case Some(appDetails) => {
              IhtFormValidator.addDeceasedNameToAllFormErrors(boundForm, regDetails.deceasedDetails.fold("")(_.name))
                .fold(
                formWithErrors => {
                  Future.successful(BadRequest(partner_permanent_home_question(formWithErrors,
                    regDetails,
                    returnLabel(regDetails, appDetails),
                    returnUrl(regDetails, appDetails))))
                },
                partnerExemption => {
                  saveApplication(getNino(userNino), partnerExemption, regDetails, appDetails)
                }
              )
            }
            case None => {
              logger.warn("Application Details not found")
              Future.successful(InternalServerError("Application details not found"))
            }
          }
        }
      }
  }

  def saveApplication(nino: String,
                      pe: PartnerExemption,
                      regDetails: RegistrationDetails,
                      appDetails: ApplicationDetails)(implicit request: Request[_],
                                                      hc: HeaderCarrier): Future[Result] = {

    val existingIsAssetForDeceasedPartner = appDetails.allExemptions.
      flatMap(_.partner.flatMap(_.isAssetForDeceasedPartner))
    val existingFirstName = appDetails.allExemptions.flatMap(_.partner.flatMap(_.firstName))
    val existingLastName = appDetails.allExemptions.flatMap(_.partner.flatMap(_.lastName))
    val existingDateOfBirth = appDetails.allExemptions.flatMap(_.partner.flatMap(_.dateOfBirth))
    val existingNino = appDetails.allExemptions.flatMap(_.partner.flatMap(_.nino))
    val existingTotalAssets = appDetails.allExemptions.flatMap(_.partner.flatMap(_.totalAssets))

    val updatedPartnerExemption = pe.copy(isAssetForDeceasedPartner = existingIsAssetForDeceasedPartner,
      firstName = existingFirstName,
      lastName = existingLastName,
      dateOfBirth = existingDateOfBirth,
      nino = existingNino,
      totalAssets = existingTotalAssets)

    val applicationDetails = appKickoutUpdateKickout(checks = checksEstate,
      prioritySection = applicationSection,
      registrationDetails = regDetails,
      applicationDetails = appDetails.copy(allExemptions = Some(appDetails.allExemptions.fold(new
          AllExemptions(partner = Some(updatedPartnerExemption)))(_.copy(partner = Some(updatedPartnerExemption))))))
    ihtConnector.saveApplication(nino, applicationDetails, regDetails.acknowledgmentReference).map(_ =>
      Redirect(applicationDetails.kickoutReason.fold(partnerOverviewPage)
      (_ => kickoutRedirectLocation)))
  }

  private def returnLabel(regDetails: RegistrationDetails,
                          appDetails: ApplicationDetails)(implicit messages: Messages): String = {
    val deceasedName = ihtHelpers.custom.name(regDetails.deceasedDetails.map(_.name).getOrElse(""))
    val partner = appDetails.allExemptions.flatMap(_.partner)
    partner match {
      case Some(x) => {
        if (x.isPartnerHomeInUK.isDefined) {
          messages("iht.estateReport.exemptions.partner.returnToAssetsLeftToSpouse")
        } else {
          messages("page.iht.application.return.to.exemptionsOf", deceasedName)
        }
      }
      case None => {
        messages("page.iht.application.return.to.exemptionsOf", deceasedName)
      }
    }
  }

  private def returnUrl(regDetails: RegistrationDetails, appDetails: ApplicationDetails): Call = {
    val partner = appDetails.allExemptions.flatMap(_.partner)
    partner match {
      case Some(x) => {
        if (x.isPartnerHomeInUK.isDefined) {
          routes.PartnerOverviewController.onPageLoad()
        } else {
          exemptionsOverviewPage
        }
      }
      case None => exemptionsOverviewPage
    }
  }
}

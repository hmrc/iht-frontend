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

package iht.utils

import java.util.UUID._

import iht.connector.CachingConnector
import iht.constants.IhtProperties.statusMarried
import iht.constants.{Constants, IhtProperties}
import iht.models._
import iht.models.application.ApplicationDetails
import iht.models.application.assets.InsurancePolicy
import iht.views.html._
import play.api.Logger
import play.api.Play.current
import play.api.data.{Form, FormError}
import play.api.i18n.Messages
import play.api.i18n.Messages.Implicits._
import play.api.mvc.{Call, Request, Session}
import play.twirl.api.Html
import uk.gov.hmrc.play.frontend.auth.AuthContext

import scala.collection.immutable.ListMap
import scala.util.{Failure, Success, Try}

/**
  *
  * This object contains all the common functionalities that can be reused
  */
object CommonHelper {
  private val DateRangeMonths = 24
  def cachingConnector: CachingConnector = CachingConnector

  import uk.gov.hmrc.play.http.HeaderCarrier

  /**
    * Capture the Referrer URL excluding the Host URL
    * e.g - Input  - http://localhost:9070/inheritance-tax/registration/addExecutor
    * Output - /inheritance-tax/registration/addExecutor
    */
  def getReferrerPathExcludingHost(request: Request[_]): String = {

    val refererFullPath = request.headers("referer")
    val pathOrigin = request.headers("host")

    //Capture the URL string excluding the http://Host
    refererFullPath.substring(refererFullPath.indexOf(pathOrigin) + pathOrigin.length)
  }

  def numberWithCommas(n: BigDecimal): String = {
    val fmt = new java.text.DecimalFormat("##,###.##")
    fmt.setDecimalSeparatorAlwaysShown(true)
    fmt.setMinimumFractionDigits(2)
    fmt.format(n)
  }

  def formatCurrencyForInput(n: String): Any = {
      Try(BigDecimal(n)) match {
        case Success (b) =>
          val fmt = new java.text.DecimalFormat("#####.##")
          fmt.setDecimalSeparatorAlwaysShown(false)
          fmt.setMinimumFractionDigits(2)
          fmt.format(b)
        case Failure(exception) =>
          n
      }
  }

  def getOrExceptionIfNegative(i: Int): Int = if (i < 0) throw new RuntimeException("Unexpected negative value") else i

  def getOrException[A](option: Option[A], errorMessage: String = "No element found"): A =
    option.fold(throw new RuntimeException(errorMessage))(identity)

  def getOrZero(option: Option[BigDecimal]): BigDecimal = option.fold(BigDecimal(0))(identity)

  def getOrExceptionNoIHTRef(option: Option[String]): String = getOrException(option, "No IHT Reference")

  def getOrExceptionNoApplication(option: Option[ApplicationDetails],
                                  errorMessage: String = "No application details"): ApplicationDetails = getOrException(option, errorMessage)

  def getOrExceptionApplicationNotSaved(option: Option[ApplicationDetails],
                                        errorMessage: String = "Unable to save application"): ApplicationDetails = getOrException(option, errorMessage)

  def getEmptyStringOrElse[A](option: Option[A], noneValue: String): String = option.fold(noneValue)(_ => "")

  def mapMaritalStatus(rd: RegistrationDetails, newValueMarried: String = "married", newValueNotMarried: String = "notMarried") =
    if (getOrException(rd.deceasedDetails.map(_.maritalStatus)) == statusMarried) newValueMarried else newValueNotMarried

  def mapBigDecimalPair(first: Option[BigDecimal],
                        second: Option[BigDecimal],
                        bothNone: String,
                        bothHaveValue: String,
                        firstHasValue: String,
                        secondHasValue: String
                       ) = {
    (first, second) match {
      case (None, None) => bothNone
      case (Some(_), Some(_)) => bothHaveValue
      case (Some(_), None) => firstHasValue
      case _ => secondHasValue
    }
  }

  def getOrMinus1(value: Option[BigDecimal]): BigDecimal = value.fold(BigDecimal(-1))(identity)


  def isSectionComplete[T](inputSection: Seq[Option[T]]) = inputSection.forall(_.isDefined)

  def getMessageKeyValueOrBlank(key: String) = if (key.length == 0) key else Messages(key)

  def withValue[A, B](value: A)(func: A => B) = func(value)

  def spy[A]: (String, A) => A = (msg, value) => {
    Logger.debug(s"\n************:$msg " + value)
    value
  }

  def escapeSpace(s: String) = s.replaceAll(" ", "&nbsp;")

  def escapeApostrophes(s: String): String = s.replaceAll("'", "&#x27;")

  def escapePound(s: String): String = s.replaceAll("£", "&pound;")

  def addApostrophe(name: String): String = name + "'" + (if (name.endsWith("s")) "" else "s")

  def addEscapedApostrophe(name: String): String = escapeApostrophes(addApostrophe(name))

  /**
    * returns Some(true) if all the values are true, Some(false) if any false or None.
    * None if all are None
    */
  def aggregateOfSeqOfOption(seqOfOptionValues: Seq[Option[Boolean]]): Option[Boolean] =
    if (seqOfOptionValues.isEmpty) {
      Some(false)
    } else {
      seqOfOptionValues.reduce { (a, b) => if (a == b) a else Some(false) }
    }

  /**
    * returns Some(BigDecimal) or None
    * ex - Input - Seq(Some(BigDecimal(12)), Some(Bigdecimal(10)))
    * Output - Some(BigDecimal(22))
    */
  def aggregateOfSeqOfOptionDecimal(seqOfOptionBigDecimal: Seq[Option[BigDecimal]]): Option[BigDecimal] =
    seqOfOptionBigDecimal collect { case Some(n) => n } reduceLeftOption {
      _ + _
    }

  val isApplicantApplyingForProbateQuestionAnswered: Predicate = (rd, _) => rd.applicantDetails.flatMap(_.isApplyingForProbate).isDefined
  val isThereAnApplicantProbateLocation: Predicate = (rd, _) => rd.applicantDetails.flatMap(_.country).isDefined
  val isThereAnApplicantPhoneNo: Predicate = (rd, _) => rd.applicantDetails.flatMap(_.phoneNo).isDefined
  val isThereAnApplicantAddress: Predicate = (rd, _) => rd.applicantDetails.flatMap(_.ukAddress).isDefined
  val isApplicantOthersApplyingForProbateQuestionAnsweredYes: Predicate = (rd, _) => rd.areOthersApplyingForProbate.fold(false)(identity)
  val isApplicantOthersApplyingForProbateQuestionAnswered: Predicate = (rd, _) => rd.areOthersApplyingForProbate.isDefined

  /**
    * Finds the Coexecutor corresponding to the ID.
    */
  val findExecutor: (String, Seq[CoExecutor]) => Option[CoExecutor] = (id, coExecutors) => {
    coExecutors.filter(_.id.contains(id)) match {
      case x :: Nil => Some(x)
      case _ => None
    }
  }

  val isThereACoExecutorWithId: Predicate = (rd, id) => findExecutor(id, rd.coExecutors).isDefined
  val isThereACoExecutorFirstName: Predicate = (rd, id) => findExecutor(id, rd.coExecutors).fold(false) {
    _.firstName.trim.nonEmpty
  }

  def addressFormater(applicantAddress: UkAddress): String = {
    var address: String = ihtHelpers.custom.name(applicantAddress.ukAddressLine1.toString) +
      " \n" + ihtHelpers.custom.name(applicantAddress.ukAddressLine2.toString).toString.replace("\n", "")

    if (applicantAddress.ukAddressLine3.isDefined) {
      address += " \n" + ihtHelpers.custom.name(applicantAddress.ukAddressLine3.getOrElse("").toString).toString.replace("\n", "")
    }

    if (applicantAddress.ukAddressLine4.isDefined) {
      address += " \n" + ihtHelpers.custom.name(applicantAddress.ukAddressLine4.getOrElse("").toString).toString.replace("\n", "")
    }

    if (applicantAddress.postCode.toString != "") {
      address += " \n" + applicantAddress.postCode.toString
    }

    if (countryName(applicantAddress.countryCode) != "" && applicantAddress.countryCode != "GB") {
      address += " \n" + countryName(applicantAddress.countryCode)
    }

    address.toString().trim()
  }

  def addressLayout(address: UkAddress): Html = {
    Html(CommonHelper.addressFormater(address).replace("\n", "<br/>"))
  }

  def insurancePoliciesEndLineMessageKey(form: Form[InsurancePolicy]): Option[String] = {
    if (form.errors.exists(error => Constants.insurancePolicyFormFieldsWithExtraContentLineInErrorSummary
      .contains(error.key))) {
      Some("page.iht.application.insurance.policies.validation.summary.endLine")
    } else {
      None
    }
  }

  /**
    * In some areas, wording on summary messages is slightly inconsistent. This allows us to overide
    * the standard behavior of message+.summary, with alternative text.
    */
  def overrideSummaryMessages(optionalErrorMap: Option[Map[String, String]], error: FormError)(implicit messages: Messages): String = {
    val defaultMap: Map[String, String] = optionalErrorMap.fold[Map[String, String]](Map.empty)(identity)
    val messageKey = s"${error.message}"
    val messageKeySummary = s"$messageKey.summary"
    val overriddenMap: Map[String, String] = defaultMap.get(error.message) match {
      case None => defaultMap + (error.message -> messageKeySummary)
      case Some(msg) => defaultMap
    }
    val messageKeyValue = messages(overriddenMap(error.message), error.args: _*)
    if (messageKeyValue == messageKeySummary) {
      messages(messageKey, error.args: _*)
    } else {
      messageKeyValue
    }
  }

  /**
    * Converts each element in a string seq into a number. If any of the elements are blank then
    * a Left of anyBlankValue is returned. If any of the elements are non-numeric then a Left of
    * anyNonNumericValue is returned. Otherwise a Right of an integer seq is returned.
    */
  def convertToNumbers(dateElements: Seq[String],
                       anyBlankValue: String,
                       anyNonNumericValue: String): Either[String, Seq[Int]] = {
    if (dateElements.exists(x => x.trim().isEmpty)) {
      Left(anyBlankValue)
    } else {
      val attemptedConvertedElements = dateElements.map { x =>
        Try(x.toInt)
      }
      if (attemptedConvertedElements.exists(_.isFailure)) {
        Left(anyNonNumericValue)
      } else {
        Right(attemptedConvertedElements.map(_.get))
      }
    }
  }

  def ensureSessionHasNino(session: Session, user: AuthContext): Session =
    withValue(StringHelper.getNino(user)) { currentNino =>
      val optionSession = session.get(Constants.NINO).fold[Option[Session]](
        None
      ) { foundNino =>
        if (foundNino == currentNino) {
          Option(session)
        } else {
          None
        }
      }
      optionSession.fold(session + (Constants.NINO -> currentNino))(identity)
    }


  def addFragmentIdentifier(call: Call, identifier: Option[String] = None) = {
    identifier match {
      case None => call
      case Some(id) => Call(call.method, addFragmentIdentifierToUrl(call.url, id))
    }
  }

  def addFragmentIdentifierToUrl(url: String, identifier: String): String = {
    if (identifier.nonEmpty) {
      url + "#" + identifier
    } else {
      url
    }
  }

  def getNinoFromSession(request:Request[_]): Option[String] = request.session.get(Constants.NINO)

}

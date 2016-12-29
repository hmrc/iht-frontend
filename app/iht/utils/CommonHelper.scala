/*
 * Copyright 2016 HM Revenue & Customs
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

import iht.constants.IhtProperties.statusMarried
import iht.constants.{Constants, IhtProperties}
import iht.models._
import iht.models.application.ApplicationDetails
import iht.models.application.assets.InsurancePolicy
import iht.models.application.gifts.PreviousYearsGifts
import org.joda.time.{DateTime, LocalDate}
import play.api.data.{Form, FormError}
import play.api.i18n.Messages
import play.api.mvc.Request
import play.api.{Logger, Play}
import uk.gov.hmrc.play.frontend.auth.AuthContext

import scala.collection.immutable.ListMap
import scala.util.Try

/**
 *
 * Created by Vineet Tyagi on 28/05/15.
 *
 * This object contains all the common functionalities that can be reused
 */
object CommonHelper {
  private val DateRangeMonths = 24

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

  def trimAndUpperCaseNino(nino: String) = {
    nino.trim.replace(" ", "").toUpperCase
  }

  def generateAcknowledgeReference: String = {
    randomUUID.toString().replaceAll("-", "")
  }

  /**
   * Convert the second element of array (Array created by input string) to Lowercase
   */
  def formatStatus(inputStatus: String) = {

    val arrayStatus = inputStatus match {
      case ApplicationStatus.KickOut => ApplicationStatus.InProgress.split(" ")
      case ApplicationStatus.ClearanceGranted => ApplicationStatus.Closed.split(" ")
      case _ => inputStatus.split(" ")
    }

    val firstPhase = arrayStatus.head

    if (arrayStatus.length > 1) {
      (firstPhase.replace(firstPhase.charAt(0), firstPhase.charAt(0).toUpper) + " " + arrayStatus.last.toLowerCase).trim
    } else {
      firstPhase.trim
    }
  }

  def getSessionId(hc: HeaderCarrier) = {
    val sessionId = hc.sessionId.getOrElse(throw new RuntimeException("No session id found in header carrier"))
    sessionId.value
  }

  def determineStatusToUse(desStatus: String, secureStorageStatus: String): String = {
    desStatus match {
      case (ApplicationStatus.AwaitingReturn) => secureStorageStatus
      case (_) => desStatus
    }
  }

  /**
   * Check the current date against input date plus range (thats add 24 months in the last day of the month
   * of input date)
   */
  def isDateWithInRange(date: LocalDate): Boolean = {
    val dateString = date.toString
    val dateTime = new DateTime(dateString)
    val dateRange = dateTime.dayOfMonth.withMaximumValue.plusMonths(24).toLocalDate
    LocalDate.now().compareTo(dateRange) < 0
  }

  def getNino(user: AuthContext):String = {
    user.principal.accounts.iht.getOrElse(throw new RuntimeException("User account could not be retrieved!")).nino.value
  }

  // scalastyle:off magic.number
  def formatNino(s: String) = {
    if (s.length >= 9) {
      val str = s.replace(" ", "")
      (str.substring(0, 2) + " " + str.substring(2, 4) + " " +
        str.substring(4, 6) + " " + str.substring(6, 8) + " " + str.substring(8)).toUpperCase
    } else {
      s
    }
  }



  def booleanToYesNo(boolean: Boolean): String = {
    boolean match {
      case true => "Yes"
      case false => "No"
    }
  }

  /*
   * Creates the valid date
   */

  def createDate(y: Option[String], m: Option[String], d: Option[String]): Option[LocalDate] = {
    val year: String = if (y.getOrElse("").replaceAll(" ", "").length > 4) {
      ""
    } else {
      y.getOrElse("")
    }

    try {
      Some(
        new LocalDate(
          year.replaceAll(" ", "").toInt,
          m.getOrElse("").replaceAll(" ", "").toInt,
          d.getOrElse("").replaceAll(" ", "").toInt
        )
      )
    } catch {
      case e: Exception => None
    }
  }

  /*
   * Checks the future Date
   */
  def isNotFutureDate = {
    date: LocalDate => !date.isAfter(LocalDate.now())
  }

  /**
   * Check the Predeceased Date Of Death Tnrb Eligibility
   */

  def preDeceasedDiedEligible(x: LocalDate) =
    x.isAfter(IhtProperties.dateOfPredeceasedForTnrbEligibility) ||
      x.isEqual(IhtProperties.dateOfPredeceasedForTnrbEligibility)

  /**
   * Fetches the key value from application.conf file
   */
  def getFromConfig(key: String) = {
    getOrException(Play.current.configuration.getString(key), "Configuration value not found for " + key)
  }

  /**
    * Iterates through ListMap of ApplicationDetails->Boolean functions, executing each one in turn, passing in the
    * specified ApplicationDetails object, until one yields true, at which point the String key associated with
    * the function is returned as an Option. If all functions yield false then a None is returned.
    */
  def findFirstTrue(registrationDetails: RegistrationDetails,
                    applicationDetails: ApplicationDetails,
                    sectionTotal: => Seq[BigDecimal],
                    keysToFunctions: ListMap[String, (RegistrationDetails,
                    ApplicationDetails, Seq[BigDecimal]) => Boolean]): Option[String] = {

    val passedItems = keysToFunctions.keys.iterator.takeWhile(key =>
      !keysToFunctions(key)(registrationDetails, applicationDetails, sectionTotal))

    val passedItemsSize = passedItems.size

    if (passedItemsSize >= keysToFunctions.keys.iterator.size) { // If they've all yielded false
      None
    } else {
      val remainingItems = keysToFunctions.keys.iterator.drop(passedItemsSize)
      Some(remainingItems.next())
    }
  }

  def getOrException[A](option: Option[A], errorMessage:String = "No element found"):A =
    option.fold(throw new RuntimeException(errorMessage))(identity)

  def getOrBigDecimalZero(option: Option[BigDecimal]): BigDecimal = option.fold(BigDecimal(0))(identity)

  def perhaps[A](thing: Option[A]): A = thing.fold(throw new RuntimeException("No element found"))(identity)

  def getOrExceptionNoIHTRef(option: Option[String]):String = getOrException(option, "No IHT Reference")

  def getOrExceptionNoApplication(option: Option[ApplicationDetails],
                                  errorMessage:String = "No application details"):ApplicationDetails = getOrException(option, errorMessage)

  def getOrExceptionApplicationNotSaved(option: Option[ApplicationDetails],
                                        errorMessage:String = "Unable to save application"):ApplicationDetails = getOrException(option, errorMessage)

  def getOrExceptionNoRegistration(option: Option[RegistrationDetails]):RegistrationDetails = getOrException(option, "No registration details")

  def getEmptyStringOrElse[A](option: Option[A], noneValue: String): String = option.fold(noneValue)(_ => "")

  def mapMaritalStatus(rd:RegistrationDetails, newValueMarried: String = "married", newValueNotMarried: String = "notMarried") =
    if(getOrException(rd.deceasedDetails.map(_.maritalStatus)) == statusMarried) newValueMarried else newValueNotMarried

  def getDateBeforeSevenYears (date: LocalDate) = {
    date.minusYears(IhtProperties.giftsYears.toInt).plusDays(1)
  }

  def mapBigDecimalPair( first: Option[BigDecimal],
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

  def previousYearsGiftsAccessibility(element: PreviousYearsGifts) = {
    val messageFileSectionKey = "page.iht.application.gifts.sevenYears.values.valueOfGiftsAndExemptions.link.screenReader"
    val startDate = getOrException(element.startDate)
    val endDate = getOrException(element.endDate)
    val totalGifts = element.value.fold(BigDecimal(0))(identity)
    val totalExemptions = element.exemptions.fold(BigDecimal(0))(identity)
    val amountAddedToEstate = totalGifts - totalExemptions

    mapBigDecimalPair(element.value, element.exemptions,
      Messages(s"$messageFileSectionKey.bothBlank", startDate, endDate, totalGifts, totalExemptions, amountAddedToEstate),
      Messages(s"$messageFileSectionKey.bothHaveValue", startDate, endDate, totalGifts, totalExemptions, amountAddedToEstate),
      Messages(s"$messageFileSectionKey.valueHasValue", startDate, endDate, totalGifts, totalExemptions, amountAddedToEstate),
      Messages(s"$messageFileSectionKey.exemptionHasValue", startDate, endDate, totalGifts, totalExemptions, amountAddedToEstate))
  }

  def previousYearsGiftsAccessibilityTotals(totalPastYearsGifts:BigDecimal,
                                            totalExemptionsValue: BigDecimal,
                                            totalPastYearsGiftsValueExcludingExemptions: BigDecimal,
         elements: Seq[PreviousYearsGifts]): Option[(String, String, String)] = {
    val messageFileSectionKey = "page.iht.application.gifts.sevenYears.values.valueOfGiftsAndExemptions.total"
    val sortedGifts = elements.sortWith( (a,b) => getOrException(a.startDate) < getOrException(b.startDate))
    val earliestDate = getOrException(sortedGifts.head.startDate)
    val latestDate = getOrException(sortedGifts.reverse.head.endDate)

    Some(
      (Messages(s"$messageFileSectionKey.gifts.screenReader", earliestDate, latestDate, totalPastYearsGifts),
      Messages(s"$messageFileSectionKey.exemptions.screenReader", earliestDate, latestDate, totalExemptionsValue),
      Messages(s"$messageFileSectionKey.estate.screenReader", earliestDate, latestDate, totalPastYearsGiftsValueExcludingExemptions))
    )
  }

  def getOrZero(optionBigDecimal:Option[BigDecimal]) = optionBigDecimal.fold(BigDecimal(0))(identity)

  def isSectionComplete[T](inputSection: Seq[Option[T]]) = inputSection.forall(_.isDefined)

  def getMessageKeyValueOrBlank(key:String) = if(key.length ==0) key else Messages(key)

  def withValue[A,B](value:A)(func:A => B) = func(value)

  def spy[A]: (String, A) => A = (msg, value) => {
    Logger.debug(s"\n************:$msg " + value)
    value
  }

  def escapeSpace(s: String) = s.replaceAll(" ", "&nbsp;")

  def escapeApostrophes(s: String): String = s.replaceAll("'", "&#x27;")

  def escapePound(s: String): String = s.replaceAll("£", "&pound;")

  def addApostrophe(name: String): String = name + "'" + (if (name.endsWith("s")) "" else "s")

  def specialiseError[T](form: Form[T], key: String, error: String): Form[T] =
    form.globalErrors.find(ge => ge.messages == Seq(error)) match {
      case Some(formError) => form.withError(formError copy (key = key))
      case _ => form
    }

  def addEscapedApostrophe(name: String): String = escapeApostrophes(addApostrophe(name))

   /**
    * returns Some(true) if all the values are true, Some(false) if any false or None.
    * None if all are None
    */
  def aggregateOfSeqOfOption(seqOfOptionValues: Seq[Option[Boolean]]): Option[Boolean] =
     if(seqOfOptionValues.isEmpty) {
       Some(false)
     } else {
       seqOfOptionValues.reduce { (a, b) => if (a == b) a else Some(false) }
     }

  /**
    * returns Some(BigDecimal) or None
    * ex - Input - Seq(Some(BigDecimal(12)), Some(Bigdecimal(10)))
    *      Output - Some(BigDecimal(22))
    */
  def aggregateOfSeqOfOptionDecimal(seqOfOptionBigDecimal: Seq[Option[BigDecimal]]): Option[BigDecimal] =
    seqOfOptionBigDecimal collect { case Some(n) => n } reduceLeftOption { _ + _ }

  val isThereADateOfDeath: Predicate = (rd, _) => rd.deceasedDateOfDeath.isDefined
  val isThereADeceasedDomicile: Predicate = (rd, _) => rd.deceasedDetails.flatMap(_.domicile).isDefined
  val isThereADeceasedFirstName: Predicate = (rd, _) => rd.deceasedDetails.flatMap(_.firstName).isDefined
  val isDeceasedAddressQuestionAnswered: Predicate = (rd, _) => rd.deceasedDetails.flatMap(_.isAddressInUK).isDefined
  val isThereADeceasedAddress: Predicate = (rd, _) => rd.deceasedDetails.flatMap(_.ukAddress).isDefined
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
  val isThereACoExecutorFirstName: Predicate = (rd, id) => findExecutor(id, rd.coExecutors).fold(false) { _.firstName.trim.nonEmpty }

  /**
    * Checks the Exemptions Completion based on marital status
    *
    * @param rd: RegistrationDetails
    * @param ad: ApplicationDetails
    * @return
    */
  def isExemptionsCompleted(rd: RegistrationDetails,
                            ad: ApplicationDetails) = {
    !rd.deceasedDetails.flatMap(_.maritalStatus).contains(IhtProperties.statusMarried) match {
      case true => ad.isExemptionsCompletedWithoutPartnerExemption
      case false => ad.isExemptionsCompleted
    }
  }

  def addressFormater(applicantAddress: UkAddress): String ={
    var address: String = applicantAddress.ukAddressLine1.toString +
      " \n" + applicantAddress.ukAddressLine2.toString

    if(applicantAddress.ukAddressLine3.isDefined) {
      address += " \n" + applicantAddress.ukAddressLine3.getOrElse("").toString
    }

    if(applicantAddress.ukAddressLine4.isDefined) {
      address += " \n" + applicantAddress.ukAddressLine4.getOrElse("").toString
    }

    if (applicantAddress.postCode.toString != "" ) {
      address += " \n" + applicantAddress.postCode.toString
    }

    if (countryName(applicantAddress.countryCode) != "") {
      address += " \n" + countryName(applicantAddress.countryCode)
    }

    address.toString
  }

  /**
    * Fetch the deceased Marital status if DeceasedDetails and MaritalStatus exists else throws exception
    *
    * @param regDetails
    * @return
    */
  def getMaritalStatus(regDetails: RegistrationDetails) =
    getOrException(getOrException(regDetails.deceasedDetails).maritalStatus)

  def insurancePoliciesEndLineMessageKey(form:Form[InsurancePolicy]):Option[String] = {
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
  def overrideSummaryMessages(optionalErrorMap: Option[Map[String, String]], error: FormError): String = {
    val defaultMap: Map[String, String] = optionalErrorMap.fold[Map[String, String]](Map.empty)(identity)
    val messageKey = s"${error.message}"
    val messageKeySummary = s"$messageKey.summary"
    val overriddenMap: Map[String, String] = defaultMap.get(error.message) match {
      case None => defaultMap + (error.message -> messageKeySummary)
      case Some(msg) => defaultMap
    }
    val messageKeyValue = Messages(overriddenMap(error.message), error.args: _*)
    if (messageKeyValue == messageKeySummary) {
      Messages(messageKey, error.args: _*)
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
                      anyNonNumericValue: String):Either[String, Seq[Int]] = {
    if (dateElements.exists(x => x.trim().isEmpty)) {
      Left(anyBlankValue)
    } else {
      val attemptedConvertedElements = dateElements.map{ x =>
        Try(x.toInt)
      }
      if (attemptedConvertedElements.exists(_.isFailure)) {
        Left(anyNonNumericValue)
      } else {
        Right(attemptedConvertedElements.map(_.get))
      }
    }
  }

  def getDeceasedNameOrDefaultString(regDetails: RegistrationDetails):Option[String] = {

    None
  }

}

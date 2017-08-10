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

package iht.utils.pdf

import java.io.{ByteArrayInputStream, ByteArrayOutputStream, File}
import javax.xml.transform.sax.SAXResult
import javax.xml.transform.stream.StreamSource
import javax.xml.transform.{ErrorListener, Transformer, TransformerException, TransformerFactory}

import iht.models.RegistrationDetails
import iht.models.application.ApplicationDetails
import iht.models.des.ihtReturn.{Asset, IHTReturn}
import iht.utils.tnrb.TnrbHelper
import iht.utils.xml.ModelToXMLSource
import iht.utils.{CommonHelper, _}
import org.apache.fop.apps._
import org.apache.fop.events.model.EventSeverity
import org.apache.fop.events.{Event, EventFormatter, EventListener}
import org.apache.xmlgraphics.util.MimeConstants
import org.joda.time.LocalDate
import play.api.Play.current
import play.api.i18n.Messages
import play.api.{Logger, Play}
import uk.gov.hmrc.play.language.LanguageUtils.Dates

/**
  * Created by david-beer on 07/06/16.
  */
object XmlFoToPDF extends XmlFoToPDF

trait XmlFoToPDF {
  private val filePathForFOPConfig = "pdf/fop.xconf"
  private val folderForPDFTemplates = "pdf/templates"
  private val filePathForClearanceXSL = s"$folderForPDFTemplates/clearance/main.xsl"
  private val filePathForPreSubmissionXSL = s"$folderForPDFTemplates/presubmission/main.xsl"
  private val filePathForPostSubmissionXSL = s"$folderForPDFTemplates/postsubmission/main.xsl"

  def createPreSubmissionPDF(registrationDetails: RegistrationDetails, applicationDetails: ApplicationDetails,
                             declarationType: String, messages: Messages): Array[Byte] = {
    val rd = PdfFormatter.transform(registrationDetails, messages)
    val ad = PdfFormatter.transform(applicationDetails, registrationDetails, messages)
    val declaration = if (declarationType.isEmpty) false else true
    Logger.debug(s"Declaration value = $declaration and declaration type = $declarationType")

    val modelAsXMLStream: StreamSource = new StreamSource(new ByteArrayInputStream(
      ModelToXMLSource.getPreSubmissionXMLSource(rd, ad)))

    val pdfoutStream = new ByteArrayOutputStream()

    createPreSubmissionTransformer(rd, ad, messages)
      .transform(modelAsXMLStream, new SAXResult(fop(pdfoutStream).getDefaultHandler))
    pdfoutStream.toByteArray
  }

  def createPostSubmissionPDF(registrationDetails: RegistrationDetails,
                              ihtReturn: IHTReturn, messages: Messages): Array[Byte] = {

    val rd = PdfFormatter.transform(registrationDetails, messages)
    val modelAsXMLStream: StreamSource = new StreamSource(new ByteArrayInputStream(ModelToXMLSource.
      getPostSubmissionDetailsXMLSource(rd, ihtReturn)))

    val pdfoutStream = new ByteArrayOutputStream()

    createPostSubmissionTransformer(rd, ihtReturn, messages)
      .transform(modelAsXMLStream, new SAXResult(fop(pdfoutStream).getDefaultHandler))

    pdfoutStream.toByteArray
  }

  def createClearancePDF(registrationDetails: RegistrationDetails,
                         declarationDate: LocalDate, messages: Messages): Array[Byte] = {
    val modelAsXMLStream: StreamSource = new StreamSource(
      new ByteArrayInputStream(ModelToXMLSource.getClearanceCertificateXMLSource(registrationDetails)))

    val pdfoutStream = new ByteArrayOutputStream()

    createClearanceTransformer(registrationDetails, declarationDate, messages)
      .transform(modelAsXMLStream, new SAXResult(fop(pdfoutStream).getDefaultHandler))

    pdfoutStream.write(pdfoutStream.toByteArray)
    pdfoutStream.toByteArray
  }

  private def createClearanceTransformer(registrationDetails: RegistrationDetails,
                                         declarationDate: LocalDate,
                                         messages: Messages): Transformer = {
    val template: StreamSource = new StreamSource(
      Play.classloader.getResourceAsStream(filePathForClearanceXSL))
    val transformerFactory: TransformerFactory = TransformerFactory.newInstance
    transformerFactory.setURIResolver(new StylesheetResolver)
    val transformer: Transformer = transformerFactory.newTransformer(template)
    setupTransformerEventHandling(transformer)

    setupCommonTransformerParameters(transformer, messages)
    transformer.setParameter("declaration-date", Dates.formatDate(declarationDate)(messages.lang))
    transformer
  }

  private def createPreSubmissionTransformer(registrationDetails: RegistrationDetails,
                                             applicationDetails: ApplicationDetails,
                                             messages: Messages): Transformer = {
    val template: StreamSource = new StreamSource(Play.classloader
      .getResourceAsStream(filePathForPreSubmissionXSL))
    val transformerFactory: TransformerFactory = TransformerFactory.newInstance
    transformerFactory.setURIResolver(new StylesheetResolver)
    val transformer: Transformer = transformerFactory.newTransformer(template)
    setupTransformerEventHandling(transformer)
    val preDeceasedName = applicationDetails.increaseIhtThreshold.map(
      xx => xx.firstName.fold("")(identity) + " " + xx.lastName.fold("")(identity)).fold("")(identity)

    val dateOfMarriage = applicationDetails.increaseIhtThreshold.map(xx => xx.dateOfMarriage.fold(new LocalDate)(identity))
    val dateOfPredeceased = applicationDetails.widowCheck.flatMap { x => x.dateOfPreDeceased }

    setupCommonTransformerParametersPreAndPost(transformer, registrationDetails, preDeceasedName, dateOfMarriage,
      applicationDetails.totalAssetsValue, applicationDetails.totalLiabilitiesValue, applicationDetails.totalExemptionsValue,
      CommonHelper.getOrZero(applicationDetails.totalPastYearsGiftsOption), messages)

    transformer.setParameter("giftsTotalExclExemptions", CommonHelper.getOrMinus1(applicationDetails.totalPastYearsGiftsValueExcludingExemptionsOption))
    transformer.setParameter("giftsExemptionsTotal", CommonHelper.getOrMinus1(applicationDetails.totalPastYearsGiftsExemptionsOption))
    transformer.setParameter("applicantName", registrationDetails.applicantDetails.map(_.name).fold("")(identity))
    transformer.setParameter("estateValue", applicationDetails.totalNetValue)
    transformer.setParameter("thresholdValue", applicationDetails.currentThreshold)
    transformer.setParameter("marriedOrCivilPartnershipLabel",
      TnrbHelper.preDeceasedMaritalStatusSubLabel(dateOfPredeceased))
    transformer.setParameter("kickout", applicationDetails.kickoutReason.isEmpty)
    transformer
  }

  private def createPostSubmissionTransformer(registrationDetails: RegistrationDetails,
                                              ihtReturn: IHTReturn,
                                              messages: Messages): Transformer = {
    val templateSource: StreamSource = new StreamSource(Play.classloader.getResourceAsStream(filePathForPostSubmissionXSL))
    val transformerFactory: TransformerFactory = TransformerFactory.newInstance
    transformerFactory.setURIResolver(new StylesheetResolver)
    val transformer: Transformer = transformerFactory.newTransformer(templateSource)
    setupTransformerEventHandling(transformer)
    val preDeceasedName = ihtReturn.deceased.flatMap(_.transferOfNilRateBand.flatMap(_.deceasedSpouses.head
      .spouse.map(xx => xx.firstName.fold("")(identity) + " " + xx.lastName.fold("")(identity)))).fold("")(identity)
    val dateOfMarriage: LocalDate = ihtReturn.deceased.flatMap(_.transferOfNilRateBand.flatMap(_.deceasedSpouses.head.spouse.
      flatMap(_.dateOfMarriage))).fold(new LocalDate)(identity)
    val declarationDate: LocalDate = CommonHelper.getOrException(ihtReturn.declaration, "No declaration found").declarationDate.
      getOrElse(throw new RuntimeException("Declaration Date not available"))

    setupCommonTransformerParametersPreAndPost(transformer, registrationDetails, preDeceasedName, Option(dateOfMarriage),
      ihtReturn.totalAssetsValue + ihtReturn.totalTrustsValue, ihtReturn.totalDebtsValue, ihtReturn.totalExemptionsValue,
      ihtReturn.totalGiftsValue, messages)

    transformer.setParameter("sumHouseholdAssets", ihtReturn.totalForAssetIDs(Set("0016")))

    CommonHelper.withValue(ihtReturn.exemptionTotalsByExemptionType) { totals =>
      transformer.setParameter(s"exemptionTotalsSpouse",totals.find(_._1 == "Spouse").fold(BigDecimal(0))(_._2))
      transformer.setParameter(s"exemptionTotalsCharity",totals.find(_._1 == "Charity").fold(BigDecimal(0))(_._2))
      transformer.setParameter(s"exemptionTotalsGNCP",totals.find(_._1 == "GNCP").fold(BigDecimal(0))(_._2))
    }

    transformer.setParameter("declarationDate", Dates.formatDate(declarationDate)(messages.lang))
    transformer.setParameter("giftsExemptionsTotal", ihtReturn.giftsExemptionsTotal)
    transformer.setParameter("giftsTotalExclExemptions", ihtReturn.giftsTotalExclExemptions)
    transformer.setParameter("estateValue", ihtReturn.totalNetValue)
    transformer.setParameter("thresholdValue", ihtReturn.currentThreshold)
    transformer
  }

  private def setupCommonTransformerParameters(transformer: Transformer, messages: Messages): Unit = {

    transformer.setParameter("versionParam", "2.0")
    transformer.setParameter("translator", XSLScalaBridge(messages))
    transformer.setParameter("pdfFormatter", PdfFormatter)
  }

  private def setupCommonTransformerParametersPreAndPost(transformer: Transformer,
                                                         registrationDetails: RegistrationDetails,
                                                         preDeceasedName: String,
                                                         dateOfMarriage: Option[LocalDate],
                                                         totalAssetsValue: BigDecimal,
                                                         totalLiabilitiesValue: BigDecimal,
                                                         totalExemptionsValue: BigDecimal,
                                                         totalPastYearsGiftsValue: BigDecimal,
                                                         messages: Messages) = {
    setupCommonTransformerParameters(transformer, messages)
    transformer.setParameter("ihtReference", formattedIHTReference(registrationDetails.ihtReference.fold("")(identity)))
    transformer.setParameter("assetsTotal", totalAssetsValue)
    transformer.setParameter("debtsTotal", totalLiabilitiesValue)
    transformer.setParameter("exemptionsTotal", totalExemptionsValue)
    transformer.setParameter("giftsTotal", totalPastYearsGiftsValue)
    transformer.setParameter("deceasedName", registrationDetails.deceasedDetails.fold("")(_.name))
    transformer.setParameter("preDeceasedName", preDeceasedName)
    transformer.setParameter("marriageLabel", TnrbHelper.marriageOrCivilPartnerShipLabelForPdf(dateOfMarriage))
  }

  private def fop(pdfoutStream: ByteArrayOutputStream): Fop = {
    val BASEURI = new File(".").toURI
    val fopURIResolver = new FopURIResolver
    val confBuilder = new FopConfParser(Play.classloader.getResourceAsStream(filePathForFOPConfig),
      EnvironmentalProfileFactory.createRestrictedIO(BASEURI, fopURIResolver)).getFopFactoryBuilder
    val fopFactory: FopFactory = confBuilder.build
    val foUserAgent: FOUserAgent = fopFactory.newFOUserAgent

    setupFOPEventHandling(foUserAgent)

    fopFactory.newFop(MimeConstants.MIME_PDF, foUserAgent, pdfoutStream)
  }

  private def setupTransformerEventHandling(transformer: Transformer) = {
    val errorListener = new ErrorListener {
      override def warning(exception: TransformerException): Unit =
        Logger.debug(exception.getMessageAndLocation)

      override def error(exception: TransformerException): Unit = {
        Logger.error(exception.getMessage, exception)
        throw exception
      }

      override def fatalError(exception: TransformerException): Unit = {
        Logger.error(exception.getMessage, exception)
        throw exception
      }
    }
    transformer.setErrorListener(errorListener)
  }

  private def setupFOPEventHandling(foUserAgent: FOUserAgent) = {
    val eventListener = new EventListener {
      override def processEvent(event: Event): Unit = {
        val messages = EventFormatter.format(event)

        val optionErrorMsg = event.getSeverity match {
          case EventSeverity.INFO =>
            Logger.info(messages)
            None
          case EventSeverity.WARN =>
            Logger.debug(messages)
            None
          case EventSeverity.ERROR =>
            Logger.error(messages)
            Some(messages)
          case EventSeverity.FATAL =>
            Logger.error(messages)
            Some(messages)
          case _ => None
        }
        optionErrorMsg.foreach(errorMsg => throw new RuntimeException(errorMsg))
      }
    }
    foUserAgent.getEventBroadcaster.addEventListener(eventListener)
  }
}
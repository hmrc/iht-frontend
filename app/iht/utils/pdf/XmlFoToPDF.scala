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

import iht.utils._
import iht.constants.{FieldMappings, IhtProperties}
import iht.models.RegistrationDetails
import iht.models.application.ApplicationDetails
import iht.utils.CommonHelper
import iht.utils.tnrb.TnrbHelper
import iht.utils.xml.ModelToXMLSource
import models.des.iht_return.IHTReturn
import org.apache.fop.apps._
import org.apache.xmlgraphics.util.MimeConstants
import org.joda.time.LocalDate
import play.api.Play.current
import play.api.{Logger, Play}
import org.apache.fop.events.Event
import org.apache.fop.events.EventFormatter
import org.apache.fop.events.EventListener
import org.apache.fop.events.model.EventSeverity
import FieldMappings._

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
                             declarationType: String): Array[Byte] = {
    val rd = PdfFormatter.transform(registrationDetails)
    val declaration = if (declarationType.isEmpty) false else true
    Logger.debug(s"Declaration value = $declaration and declaration type = $declarationType")

    val modelAsXMLStream: StreamSource = new StreamSource(new ByteArrayInputStream(
      ModelToXMLSource.getPreSubmissionXMLSource(rd, applicationDetails)))

    val pdfoutStream = new ByteArrayOutputStream()

    createPreSubmissionTransformer(rd, applicationDetails)
      .transform(modelAsXMLStream, new SAXResult(fop(pdfoutStream).getDefaultHandler))
    pdfoutStream.toByteArray
  }

  def createPostSubmissionPDF(registrationDetails: RegistrationDetails, ihtReturn: IHTReturn): Array[Byte] = {
    val rd = PdfFormatter.transform(registrationDetails)
    val modelAsXMLStream: StreamSource = new StreamSource(new ByteArrayInputStream(ModelToXMLSource.
      getPostSubmissionDetailsXMLSource(rd, ihtReturn)))

    val pdfoutStream = new ByteArrayOutputStream()

    createPostSubmissionTransformer(rd, ihtReturn)
      .transform(modelAsXMLStream, new SAXResult(fop(pdfoutStream).getDefaultHandler))

    pdfoutStream.toByteArray
  }

  def createClearancePDF(registrationDetails: RegistrationDetails, declarationDate: LocalDate): Array[Byte] = {
    val modelAsXMLStream: StreamSource = new StreamSource(
      new ByteArrayInputStream(ModelToXMLSource.getClearanceCertificateXMLSource(registrationDetails)))

    val pdfoutStream = new ByteArrayOutputStream()

    createClearanceTransformer(registrationDetails, declarationDate)
      .transform(modelAsXMLStream, new SAXResult(fop(pdfoutStream).getDefaultHandler))

    pdfoutStream.write(pdfoutStream.toByteArray)
    pdfoutStream.toByteArray
  }

  private def createClearanceTransformer(registrationDetails: RegistrationDetails,
                                         declarationDate: LocalDate): Transformer = {
    val template: StreamSource = new StreamSource(
      Play.classloader.getResourceAsStream(filePathForClearanceXSL))
    val transformerFactory: TransformerFactory = TransformerFactory.newInstance
    transformerFactory.setURIResolver(new StylesheetResolver)
    val transformer: Transformer = transformerFactory.newTransformer(template)
    setupTransformerEventHandling(transformer)

    setupCommonTransformerParameters(transformer)
    transformer.setParameter("declaration-date", declarationDate.toString(IhtProperties.dateFormatForDisplay))
    transformer
  }

  private def createPreSubmissionTransformer(registrationDetails: RegistrationDetails,
                                             applicationDetails: ApplicationDetails): Transformer = {
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
      applicationDetails.totalGiftsValue)

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
                                              ihtReturn: IHTReturn): Transformer = {
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
      ihtReturn.totalGiftsValue)

    transformer.setParameter("declarationDate", declarationDate.toString(IhtProperties.dateFormatForDisplay))
    transformer
  }

  private def setupCommonTransformerParameters(transformer: Transformer): Unit = {
    transformer.setParameter("versionParam", "2.0")
    transformer.setParameter("translator", MessagesTranslator)
    transformer.setParameter("pdfFormatter", PdfFormatter)
  }

  private def setupCommonTransformerParametersPreAndPost(transformer: Transformer,
                                                         registrationDetails: RegistrationDetails,
                                                         preDeceasedName: String,
                                                         dateOfMarriage: Option[LocalDate],
                                                         totalAssetsValue: BigDecimal,
                                                         totalLiabilitiesValue: BigDecimal,
                                                         totalExemptionsValue: BigDecimal,
                                                         totalPastYearsGiftsValue: BigDecimal) = {
    setupCommonTransformerParameters(transformer)
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
        val msg = EventFormatter.format(event)

        val optionErrorMsg = event.getSeverity match {
          case EventSeverity.INFO =>
            Logger.info(msg)
            None
          case EventSeverity.WARN =>
            Logger.debug(msg)
            None
          case EventSeverity.ERROR =>
            Logger.error(msg)
            Some(msg)
          case EventSeverity.FATAL =>
            Logger.error(msg)
            Some(msg)
          case _ => None
        }
        optionErrorMsg.foreach(errorMsg => throw new RuntimeException(errorMsg))
      }
    }
    foUserAgent.getEventBroadcaster.addEventListener(eventListener)
  }
}
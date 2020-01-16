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

package iht.utils.pdf

import java.io.{ByteArrayInputStream, ByteArrayOutputStream, File}

import iht.config.AppConfig
import iht.models.RegistrationDetails
import iht.models.application.ApplicationDetails
import iht.models.des.ihtReturn.IHTReturn
import iht.utils.tnrb.TnrbHelper
import iht.utils.xml.ModelToXMLSource
import iht.utils.{CommonHelper, _}
import javax.inject.{Inject, Singleton}
import javax.xml.transform.sax.SAXResult
import javax.xml.transform.stream.StreamSource
import javax.xml.transform.{ErrorListener, Transformer, TransformerException, TransformerFactory}
import org.apache.fop.apps._
import org.apache.fop.events.model.EventSeverity
import org.apache.fop.events.{Event, EventFormatter, EventListener}
import org.apache.xmlgraphics.util.MimeConstants
import org.joda.time.LocalDate
import play.api.Logger
import play.api.i18n.Messages
import iht.utils.CustomLanguageUtils.Dates

@Singleton
class DefaultXmlFoToPDF @Inject()(val stylesheetResourceStreamResolver: StylesheetResourceStreamResolver,
                                  val resourceStreamResolver: BaseResourceStreamResolver,
                                  val fopURIResolver: FopURIResolver,
                                  implicit val appConfig: AppConfig) extends XmlFoToPDF

trait XmlFoToPDF extends PdfHelper with TnrbHelper {
  val resourceStreamResolver: BaseResourceStreamResolver
  val stylesheetResourceStreamResolver: StylesheetResourceStreamResolver
  val fopURIResolver: FopURIResolver

  private val filePathForFOPConfig = "pdf/fop.xconf"
  private val folderForPDFTemplates = "pdf/templates"
  private val filePathForClearanceXSL = s"$folderForPDFTemplates/clearance/main.xsl"
  private val filePathForPreSubmissionXSL = s"$folderForPDFTemplates/presubmission/main.xsl"
  private val filePathForPostSubmissionXSL = s"$folderForPDFTemplates/postsubmission/main.xsl"

  def createPreSubmissionPDF(registrationDetails: RegistrationDetails, applicationDetails: ApplicationDetails,
                             declarationType: String, messages: Messages): Array[Byte] = {
    val rd = PdfFormatter.transform(registrationDetails, messages)
    val ad = PdfFormatter.transformWithApplicationDetails(applicationDetails, registrationDetails, messages)
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
    val ad = createApplicationDetails(
      ihtReturn.freeEstate.flatMap(_.estateAssets),
      ihtReturn.trusts)
    val modelAsXMLStream: StreamSource = new StreamSource(new ByteArrayInputStream(ModelToXMLSource.
      getPostSubmissionDetailsXMLSource(rd, ihtReturn, ad)))

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
    val template: StreamSource = resourceStreamResolver.resolvePath(filePathForClearanceXSL)
    val transformerFactory: TransformerFactory = TransformerFactory.newInstance
    transformerFactory.setURIResolver(stylesheetResourceStreamResolver)

    val transformer: Transformer = transformerFactory.newTransformer(template)
    setupTransformerEventHandling(transformer)

    setupCommonTransformerParameters(transformer, messages)
    transformer.setParameter("declaration-date", Dates.formatDate(declarationDate)(messages))
    transformer
  }

  private def createPreSubmissionTransformer(registrationDetails: RegistrationDetails,
                                             applicationDetails: ApplicationDetails,
                                             messages: Messages): Transformer = {
    val template: StreamSource = resourceStreamResolver.resolvePath(filePathForPreSubmissionXSL)
    val transformerFactory: TransformerFactory = TransformerFactory.newInstance
    transformerFactory.setURIResolver(stylesheetResourceStreamResolver)

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

    transformer.setParameter("estateValue", assetsNetValue(applicationDetails))
    transformer.setParameter("thresholdValue", applicationDetails.currentThreshold)
    transformer.setParameter("marriedOrCivilPartnershipLabel", preDeceasedMaritalStatusSubLabel(dateOfPredeceased)(messages))
    transformer.setParameter("kickout", applicationDetails.kickoutReason.isEmpty)
    transformer.setParameter("estateOverviewDisplayMode",PdfFormatter.estateOverviewDisplayMode(applicationDetails))
    transformer
  }

  private def createPostSubmissionTransformer(registrationDetails: RegistrationDetails,
                                              ihtReturn: IHTReturn,
                                              messages: Messages): Transformer = {
    val templateSource: StreamSource = resourceStreamResolver.resolvePath(filePathForPostSubmissionXSL)
    val transformerFactory: TransformerFactory = TransformerFactory.newInstance
    transformerFactory.setURIResolver(stylesheetResourceStreamResolver)

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

    transformer.setParameter("sumHouseholdAssets", ihtReturn.totalForAssetIDs(Set("0016","0017","0018")))

    CommonHelper.withValue(ihtReturn.exemptionTotalsByExemptionType) { totals =>
      transformer.setParameter(s"exemptionTotalsSpouse",totals.find(_._1 == "Spouse or civil partner").fold(BigDecimal(0))(_._2))
      transformer.setParameter(s"exemptionTotalsCharity",totals.find(_._1 == "Charity").fold(BigDecimal(0))(_._2))
      transformer.setParameter(s"exemptionTotalsGNCP",totals.find(_._1 == "Other qualifying bodies").fold(BigDecimal(0))(_._2))
    }

    transformer.setParameter("declarationDate", Dates.formatDate(declarationDate)(messages))
    transformer.setParameter("giftsExemptionsTotal", ihtReturn.giftsExemptionsTotal)
    transformer.setParameter("giftsTotalExclExemptions", ihtReturn.giftsTotalExclExemptions)
    transformer.setParameter("estateValue", ihtReturn.totalNetValue)
    transformer.setParameter("thresholdValue", ihtReturn.currentThreshold)
    transformer.setParameter("tnrbEligibility", ihtReturn.isTnrbApplicable)
    transformer.setParameter("estateOverviewDisplayMode", PdfFormatter.estateOverviewDisplayModeForPostPdf(ihtReturn))
    transformer
  }

  private def setupCommonTransformerParameters(transformer: Transformer, messages: Messages): Unit = {
    transformer.setParameter("versionParam", "2.0")
    transformer.setParameter("translator", XSLScalaBridge(messages))
    transformer.setParameter("pdfFormatter", PdfFormatter)
    val hmrcLogoFile = if (messages.lang.code == "en") {
      "pdf/logo/hmrc_logo_en.jpg"
    } else {
      "pdf/logo/hmrc_logo_cy.jpg"
    }
    transformer.setParameter("hmrcLogo", hmrcLogoFile)
  }

  private def setupCommonTransformerParametersPreAndPost(transformer: Transformer,
                                                         registrationDetails: RegistrationDetails,
                                                         preDeceasedName: String,
                                                         dateOfMarriage: Option[LocalDate],
                                                         totalAssetsValue: BigDecimal,
                                                         totalLiabilitiesValue: BigDecimal,
                                                         totalExemptionsValue: BigDecimal,
                                                         totalPastYearsGiftsValue: BigDecimal,
                                                         messages: Messages): Unit = {
    setupCommonTransformerParameters(transformer, messages)
    transformer.setParameter("ihtReference", formattedIHTReference(registrationDetails.ihtReference.fold("")(identity)))
    transformer.setParameter("assetsTotal", totalAssetsValue)
    transformer.setParameter("debtsTotal", totalLiabilitiesValue)
    transformer.setParameter("exemptionsTotal", totalExemptionsValue)
    transformer.setParameter("giftsTotal", totalPastYearsGiftsValue)
    transformer.setParameter("deceasedName", registrationDetails.deceasedDetails.fold("")(_.name))
    transformer.setParameter("preDeceasedName", preDeceasedName)
    transformer.setParameter("marriageLabel", marriageOrCivilPartnerShipLabelForPdf(dateOfMarriage)(messages))
  }

  private def fop(pdfOutStream: ByteArrayOutputStream): Fop = {
    val restrictedIO: EnvironmentProfile = EnvironmentalProfileFactory.createRestrictedIO(new File(".").toURI, fopURIResolver)

    val confBuilder = new FopConfParser(resourceStreamResolver.resolvePath(filePathForFOPConfig).getInputStream, restrictedIO).getFopFactoryBuilder
    val fopFactory: FopFactory = confBuilder.build
    val foUserAgent: FOUserAgent = fopFactory.newFOUserAgent

    setupFOPEventHandling(foUserAgent)

    fopFactory.newFop(MimeConstants.MIME_PDF, foUserAgent, pdfOutStream)
  }

  private def setupTransformerEventHandling(transformer: Transformer): Unit = {
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

  private def setupFOPEventHandling(foUserAgent: FOUserAgent): Unit = {
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

  private def assetsNetValue( applicationDetails: ApplicationDetails) = {

    applicationDetails.totalExemptionsValue match {
      case x if x>0 => applicationDetails.totalNetValue
      case _ => applicationDetails.totalValue
    }

  }
}

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
import javax.xml.transform.{Transformer, TransformerFactory}
import iht.utils._
import iht.constants.IhtProperties
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

/**
  * Created by david-beer on 07/06/16.
  */
object XmlFoToPDF extends XmlFoToPDF

trait XmlFoToPDF {
  private val filePathForFOPConfig = "pdf/fop.xconf"
  private val folderForPDFTemplates = "pdf/templates"

  def generateSummaryPDF(regDetails: RegistrationDetails, applicationDetails: ApplicationDetails,
                         declarationType: String) = {
    val declaration = if (declarationType.isEmpty) false else true
    Logger.debug(s"Declaration value = $declaration and declaration type = $declarationType")

    val template: StreamSource = new StreamSource(Play.classloader
      .getResourceAsStream(s"$folderForPDFTemplates/pre-submission-estate-report.xsl"))
    val streamSource: StreamSource = new StreamSource(new ByteArrayInputStream(
      ModelToXMLSource.getPreSubmissionXMLSource(regDetails, applicationDetails)))

    createPreSubmissionPDF(template,
      streamSource,
      regDetails,
      applicationDetails,
      declaration,
      declarationType,
      applicationDetails.kickoutReason.isEmpty
    )
  }

  private def createPreSubmissionPDF(template: StreamSource, modelSource: StreamSource, registrationDetails: RegistrationDetails,
                                     applicationDetails: ApplicationDetails, declaration: Boolean, declarationType: String,
                                     kickout: Boolean): Array[Byte] = {
    val BASEURI = new File(".").toURI
    val fopURIResolver = new FopURIResolver
    val confBuilder = new FopConfParser(Play.classloader.getResourceAsStream(filePathForFOPConfig),
      EnvironmentalProfileFactory.createRestrictedIO(BASEURI, fopURIResolver)).getFopFactoryBuilder
    val fopFactory: FopFactory = confBuilder.build

    val foUserAgent: FOUserAgent = fopFactory.newFOUserAgent

    val pdfoutStream = new ByteArrayOutputStream()
    val transformerFactory: TransformerFactory = TransformerFactory.newInstance
    transformerFactory.setURIResolver(new StylesheetResolver)

    val transformer: Transformer = transformerFactory.newTransformer(template)

    val fop: Fop = fopFactory.newFop(MimeConstants.MIME_PDF, foUserAgent, pdfoutStream)
    val res = new SAXResult(fop.getDefaultHandler)

    val preDeceasedName = applicationDetails.increaseIhtThreshold.map(
      xx => xx.firstName.fold("")(identity) + " " + xx.lastName.fold("")(identity)).fold("")(identity)

    val dateOfMarriage = applicationDetails.increaseIhtThreshold.map(xx => xx.dateOfMarriage.fold(new LocalDate)(identity))
    val dateOfPredeceased = applicationDetails.widowCheck.flatMap{ x=> x.dateOfPreDeceased}

    transformer.setParameter("versionParam", "2.0")
    transformer.setParameter("translator", MessagesTranslator)
    transformer.setParameter("ihtReference", formattedIHTReference(registrationDetails.ihtReference.fold("")(identity)))
    transformer.setParameter("pdfFormatter", PdfFormatter)
    transformer.setParameter("assetsTotal", applicationDetails.totalAssetsValue)
    transformer.setParameter("debtsTotal", applicationDetails.totalLiabilitiesValue)
    transformer.setParameter("exemptionsTotal", applicationDetails.totalExemptionsValue)
    transformer.setParameter("giftsTotalExclExemptions",
      CommonHelper.getOrBigDecimalZero(applicationDetails.totalPastYearsGiftsValueExcludingExemptionsOption))
    transformer.setParameter("giftsTotal", applicationDetails.totalGiftsValue)
    transformer.setParameter("deceasedName", registrationDetails.deceasedDetails.fold("")(_.name))
    transformer.setParameter("applicantName", registrationDetails.applicantDetails.map(_.name).fold("")(identity))
    transformer.setParameter("estateValue", applicationDetails.totalNetValue)
    transformer.setParameter("thresholdValue", applicationDetails.currentThreshold)
    transformer.setParameter("preDeceasedName", preDeceasedName)
    transformer.setParameter("marriageLabel", TnrbHelper.marriageOrCivilPartnerShipLabelForPdf(dateOfMarriage))
    transformer.setParameter("marriedOrCivilPartnershipLabel",
                              TnrbHelper.preDeceasedMaritalStatusSubLabel(dateOfPredeceased))
    transformer.setParameter("kickout", kickout)

    transformer.transform(modelSource, res)
    pdfoutStream.toByteArray
  }

  def createClearancePDF(registrationDetails: RegistrationDetails, declarationDate: LocalDate): Array[Byte] = {

    val templateFile: StreamSource = new StreamSource(
      Play.classloader.getResourceAsStream(s"$folderForPDFTemplates/clearance-certificate.xsl"))
    val streamSource: StreamSource = new StreamSource(
      new ByteArrayInputStream(ModelToXMLSource.getClearanceCertificateXMLSource(registrationDetails)))
    createClearancePDFFile(streamSource, templateFile, declarationDate)
  }

  private def createClearancePDFFile(streamSource: StreamSource,
                                     templateFileSource: StreamSource,
                                     declarationDate: LocalDate): Array[Byte] = {
    val BASEURI = new File(".").toURI
    val fopURIResolver = new FopURIResolver
    val confBuilder = new FopConfParser(Play.classloader.getResourceAsStream(filePathForFOPConfig),
      EnvironmentalProfileFactory.createRestrictedIO(BASEURI, fopURIResolver)).getFopFactoryBuilder
    val fopFactory: FopFactory = confBuilder.build

    val foUserAgent: FOUserAgent = fopFactory.newFOUserAgent

    val pdfoutStream = new ByteArrayOutputStream()
    val transformerFactory: TransformerFactory = TransformerFactory.newInstance

    val transformer: Transformer = transformerFactory.newTransformer(templateFileSource)

    val fop: Fop = fopFactory.newFop(MimeConstants.MIME_PDF, foUserAgent, pdfoutStream)
    val res = new SAXResult(fop.getDefaultHandler)

    transformer.setParameter("pdfFormatter", PdfFormatter)
    transformer.setParameter("versionParam", "2.0")
    transformer.setParameter("translator", MessagesTranslator)
    transformer.setParameter("declaration-date", declarationDate.toString(IhtProperties.dateFormatForDisplay))

    transformer.transform(streamSource, res)
    pdfoutStream.write(pdfoutStream.toByteArray)
    pdfoutStream.toByteArray
  }

  //TODO  - Modify below implementatin once template is ready
  def createApplicationReturnPDF(registrationDetails: RegistrationDetails, ihtReturn: IHTReturn): Array[Byte] = {
    val templateFile: StreamSource = new StreamSource(Play.classloader.getResourceAsStream(
      s"$folderForPDFTemplates/post-submission-estate-report.xsl"))

    val streamSource: StreamSource = new StreamSource(new ByteArrayInputStream(ModelToXMLSource.
      getPostSubmissionDetailsXMLSource(registrationDetails, ihtReturn)))
    generateApplicationReturnPDFData(streamSource, templateFile, ihtReturn, registrationDetails)
  }

  private def generateApplicationReturnPDFData(streamSource: StreamSource, templateFileSource: StreamSource, ihtReturn: IHTReturn,
                                               registrationDetails: RegistrationDetails): Array[Byte] = {
    val BASEURI = new File(".").toURI
    val fopURIResolver = new FopURIResolver
    val confBuilder = new FopConfParser(Play.classloader.getResourceAsStream(filePathForFOPConfig),
      EnvironmentalProfileFactory.createRestrictedIO(BASEURI, fopURIResolver)).getFopFactoryBuilder
    val fopFactory: FopFactory = confBuilder.build

    val foUserAgent: FOUserAgent = fopFactory.newFOUserAgent

    val templateSrc: StreamSource = new StreamSource(Play.classloader.getResourceAsStream(
      s"$folderForPDFTemplates/post-submission-estate-report.xsl"))
    val pdfoutStream = new ByteArrayOutputStream()
    val transformerFactory: TransformerFactory = TransformerFactory.newInstance
    transformerFactory.setURIResolver(new StylesheetResolver)

    val transformer: Transformer = transformerFactory.newTransformer(templateSrc)

    val fop: Fop = fopFactory.newFop(MimeConstants.MIME_PDF, foUserAgent, pdfoutStream)
    val res = new SAXResult(fop.getDefaultHandler)

    val preDeceasedName = ihtReturn.deceased.flatMap(_.transferOfNilRateBand.flatMap(_.deceasedSpouses.head
      .spouse.map(xx => xx.firstName.fold("")(identity) + " " + xx.lastName.fold("")(identity)))).fold("")(identity)
    val dateOfMarriage = ihtReturn.deceased.flatMap(_.transferOfNilRateBand.flatMap(_.deceasedSpouses.head.spouse.
      flatMap(_.dateOfMarriage))).fold(new LocalDate)(identity)
    val declarationDate: LocalDate = CommonHelper.getOrException(ihtReturn.declaration, "No declaration found").declarationDate.
      getOrElse(throw new RuntimeException("Declaration Date not available"))

    transformer.setParameter("versionParam", "2.0")
    transformer.setParameter("translator", MessagesTranslator)
    transformer.setParameter("ihtReference", formattedIHTReference(registrationDetails.ihtReference.fold("")(identity)))
    transformer.setParameter("declarationDate", declarationDate.toString(IhtProperties.dateFormatForDisplay))
    transformer.setParameter("pdfFormatter", PdfFormatter)
    transformer.setParameter("assetsTotal", ihtReturn.totalAssetsValue + ihtReturn.totalTrustsValue)
    transformer.setParameter("debtsTotal", ihtReturn.totalDebtsValue)
    transformer.setParameter("exemptionsTotal", ihtReturn.totalExemptionsValue)
    transformer.setParameter("giftsTotal", ihtReturn.totalGiftsValue)
    transformer.setParameter("deceasedName", registrationDetails.deceasedDetails.fold("")(_.name))
    transformer.setParameter("preDeceasedName", preDeceasedName)
    transformer.setParameter("marriageLabel", TnrbHelper.marriageOrCivilPartnerShipLabelForPdf(Some(dateOfMarriage)))

    transformer.transform(streamSource, res)
    pdfoutStream.toByteArray
  }
}

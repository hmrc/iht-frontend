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

  def createPreSubmissionPDF(regDetails: RegistrationDetails, applicationDetails: ApplicationDetails,
                             declarationType: String): Array[Byte] = {
    val declaration = if (declarationType.isEmpty) false else true
    Logger.debug(s"Declaration value = $declaration and declaration type = $declarationType")

    val modelAsXMLStream: StreamSource = new StreamSource(new ByteArrayInputStream(
      ModelToXMLSource.getPreSubmissionXMLSource(regDetails, applicationDetails)))

    val pdfoutStream = new ByteArrayOutputStream()

    preSubmissionTransformer(regDetails, applicationDetails)
      .transform(modelAsXMLStream, new SAXResult(fop(pdfoutStream).getDefaultHandler))
    pdfoutStream.toByteArray
  }

  def createPostSubmissionPDF(registrationDetails: RegistrationDetails, ihtReturn: IHTReturn): Array[Byte] = {
    val modelAsXMLStream: StreamSource = new StreamSource(new ByteArrayInputStream(ModelToXMLSource.
      getPostSubmissionDetailsXMLSource(registrationDetails, ihtReturn)))

    val pdfoutStream = new ByteArrayOutputStream()

    postSubmissionTransformer(registrationDetails, ihtReturn)
      .transform(modelAsXMLStream, new SAXResult(fop(pdfoutStream).getDefaultHandler))

    pdfoutStream.toByteArray
  }

  def createClearancePDF(registrationDetails: RegistrationDetails, declarationDate: LocalDate): Array[Byte] = {
    val modelAsXMLStream: StreamSource = new StreamSource(
      new ByteArrayInputStream(ModelToXMLSource.getClearanceCertificateXMLSource(registrationDetails)))

    val pdfoutStream = new ByteArrayOutputStream()

    clearanceTransformer(registrationDetails, declarationDate)
      .transform(modelAsXMLStream, new SAXResult(fop(pdfoutStream).getDefaultHandler))

    pdfoutStream.write(pdfoutStream.toByteArray)
    pdfoutStream.toByteArray
  }

  private def clearanceTransformer(registrationDetails:RegistrationDetails,
                                   declarationDate: LocalDate): Transformer = {
    val template: StreamSource = new StreamSource(
      Play.classloader.getResourceAsStream(s"$folderForPDFTemplates/clearance-certificate.xsl"))
    val transformerFactory: TransformerFactory = TransformerFactory.newInstance
    transformerFactory.setURIResolver(new StylesheetResolver)
    val transformer: Transformer = transformerFactory.newTransformer(template)
    transformer.setErrorListener(errorListener)

    transformer.setParameter("pdfFormatter", PdfFormatter)
    transformer.setParameter("versionParam", "2.0")
    transformer.setParameter("translator", MessagesTranslator)
    transformer.setParameter("declaration-date", declarationDate.toString(IhtProperties.dateFormatForDisplay))
    transformer
  }

  private def preSubmissionTransformer(registrationDetails:RegistrationDetails,
                                       applicationDetails: ApplicationDetails): Transformer = {
    val template: StreamSource = new StreamSource(Play.classloader
      .getResourceAsStream(s"$folderForPDFTemplates/pre-submission-estate-report.xsl"))
    val transformerFactory: TransformerFactory = TransformerFactory.newInstance
    transformerFactory.setURIResolver(new StylesheetResolver)
    val transformer: Transformer = transformerFactory.newTransformer(template)
    transformer.setErrorListener(errorListener)
    val preDeceasedName = applicationDetails.increaseIhtThreshold.map(
      xx => xx.firstName.fold("")(identity) + " " + xx.lastName.fold("")(identity)).fold("")(identity)

    val dateOfMarriage = applicationDetails.increaseIhtThreshold.map(xx => xx.dateOfMarriage.fold(new LocalDate)(identity))
    val dateOfPredeceased = applicationDetails.widowCheck.flatMap { x => x.dateOfPreDeceased }

    transformer.setParameter("versionParam", "2.0")
    transformer.setParameter("translator", MessagesTranslator)
    transformer.setParameter("ihtReference", formattedIHTReference(registrationDetails.ihtReference.fold("")(identity)))
    transformer.setParameter("pdfFormatter", PdfFormatter)
    transformer.setParameter("assetsTotal", applicationDetails.totalAssetsValue)
    transformer.setParameter("debtsTotal", applicationDetails.totalLiabilitiesValue)
    transformer.setParameter("exemptionsTotal", applicationDetails.totalExemptionsValue)
    transformer.setParameter("giftsTotal", applicationDetails.totalGiftsValue)
    transformer.setParameter("deceasedName", registrationDetails.deceasedDetails.fold("")(_.name))
    transformer.setParameter("applicantName", registrationDetails.applicantDetails.map(_.name).fold("")(identity))
    transformer.setParameter("estateValue", applicationDetails.totalNetValue)
    transformer.setParameter("thresholdValue", applicationDetails.currentThreshold)
    transformer.setParameter("preDeceasedName", preDeceasedName)
    transformer.setParameter("marriageLabel", TnrbHelper.marriageOrCivilPartnerShipLabelForPdf(dateOfMarriage))
    transformer.setParameter("marriedOrCivilPartnershipLabel",
      TnrbHelper.preDeceasedMaritalStatusSubLabel(dateOfPredeceased))
    transformer.setParameter("kickout", applicationDetails.kickoutReason.isEmpty)
    transformer
  }

  private def postSubmissionTransformer(registrationDetails:RegistrationDetails,
                                             ihtReturn: IHTReturn): Transformer = {
    val templateSource: StreamSource = new StreamSource(Play.classloader.getResourceAsStream(
      s"$folderForPDFTemplates/post-submission-estate-report.xsl"))
    val transformerFactory: TransformerFactory = TransformerFactory.newInstance
    transformerFactory.setURIResolver(new StylesheetResolver)
    val transformer: Transformer = transformerFactory.newTransformer(templateSource)
    transformer.setErrorListener(errorListener)
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
    transformer
  }

  private def errorListener = new ErrorListener {
      override def warning(exception: TransformerException): Unit =
        Logger.warn(exception.getMessageAndLocation)

      override def error(exception: TransformerException): Unit = {
        throw exception
      }

      override def fatalError(exception: TransformerException): Unit = {
        throw exception
      }
    }

  private def fop(pdfoutStream: ByteArrayOutputStream): Fop ={
    val BASEURI = new File(".").toURI
    val fopURIResolver = new FopURIResolver
    val confBuilder = new FopConfParser(Play.classloader.getResourceAsStream(filePathForFOPConfig),
      EnvironmentalProfileFactory.createRestrictedIO(BASEURI, fopURIResolver)).getFopFactoryBuilder
    val fopFactory: FopFactory = confBuilder.build
    val foUserAgent: FOUserAgent = fopFactory.newFOUserAgent

    fopFactory.newFop(MimeConstants.MIME_PDF, foUserAgent, pdfoutStream)
  }
}

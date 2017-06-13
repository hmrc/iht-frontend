<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="2.0"
                xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:fo="http://www.w3.org/1999/XSL/Format"
                xmlns:common="http://exslt.org/common"
                xmlns:xalan="http://xml.apache.org" exclude-result-prefixes="common xalan"
                xmlns:scala="java:iht.utils.pdf.XSLScalaBridge"
                xmlns:formatter="java:iht.utils.pdf.PdfFormatter">

    <xsl:param name="translator" />
    <xsl:param name="pdfFormatter"/>
    <xsl:param name="versionParam" select="'1.0'"/>
    <xsl:param name="declaration-date"/>

    <xsl:template match="RegistrationDetails">
        <fo:root font-family="OpenSans">
            <fo:layout-master-set>
                <fo:simple-page-master master-name="simple"
                                       page-height="29.7cm" page-width="21.0cm"
                                       margin-top="0.2cm" margin-bottom="0.2cm"
                                       margin-left="2cm" margin-right="2cm">
                    <fo:region-body margin-top="2.5cm" margin-bottom="2.5cm"/>
                    <fo:region-before extent="2.4cm"/>
                    <fo:region-after extent="2.4cm"/>
                </fo:simple-page-master>
            </fo:layout-master-set>
            <fo:declarations>
                <x:xmpmeta xmlns:x="adobe:ns:meta/">
                    <rdf:RDF xmlns:rdf="http://www.w3.org/1999/02/22-rdf-syntax-ns#">
                        <rdf:Description rdf:about=""
                                         xmlns:dc="http://purl.org/dc/elements/1.1/">
                            <dc:title><xsl:value-of select="scala:getMessagesText($translator, 'pdf.clearanceCertificate.title')"/></dc:title>
                            <dc:creator><xsl:value-of select="scala:getMessagesText($translator, 'pdf.meta.author')"/></dc:creator>
                            <dc:description><xsl:value-of select="scala:getMessagesText($translator, 'pdf.clearanceCertificate.title')"/></dc:description>
                        </rdf:Description>
                    </rdf:RDF>
                </x:xmpmeta>
            </fo:declarations>
            <fo:page-sequence master-reference="simple">
                <fo:static-content flow-name="xsl-region-before">
                    <fo:block border-bottom-style="solid">
                        <fo:external-graphic src="url('pdf/logo/hmrc_logo.jpg')" height="50px" content-width="scale-to-fit"/>
                    </fo:block>
                </fo:static-content>

                <fo:flow flow-name="xsl-region-body">
                    <fo:block font-family="OpenSans-Bold" font-size="24pt" font-weight="bold">
                        <xsl:value-of select="scala:getMessagesText($translator, 'pdf.clearanceCertificate.title')"/>
                        <fo:block font-family="OpenSans" font-size="12pt" font-weight="normal" space-before="1cm">
                            <xsl:value-of select="scala:getMessagesText($translator, 'pdf.clearanceCertificate.summary')"/>
                        </fo:block>
                    </fo:block>

                    <fo:block font-family="OpenSans" font-size="12pt" font-weight="normal" space-before="1cm">
                        <fo:block font-family="OpenSans-Bold" font-size="16pt" font-weight="bold">
                            <xsl:value-of select="scala:getMessagesText($translator, 'page.iht.registration.registrationSummary.deceasedTable.title')" />
                        </fo:block>
                        <fo:block>
                            <fo:table space-before="0.5cm">
                                <fo:table-column column-number="1" />
                                <fo:table-column column-number="2" />
                                <fo:table-body font-size="12pt" >
                                    <fo:table-row line-height="30pt">
                                        <fo:table-cell text-align="left" border-top ="solid 0.3mm gray" border-bottom ="solid 0.1mm gray" padding-left="4pt">
                                            <fo:block><xsl:value-of select="scala:getMessagesText($translator, 'iht.name.upperCaseInitial')" /></fo:block>
                                        </fo:table-cell>
                                        <fo:table-cell text-align="left" border-top ="solid 0.3mm gray" border-bottom ="solid 0.1mm gray" padding-left="4pt">
                                            <fo:block>
                                                <xsl:value-of select="concat(deceasedDetails/firstName,' ', deceasedDetails/lastName)" />
                                            </fo:block>
                                        </fo:table-cell>
                                    </fo:table-row>
                                    <fo:table-row line-height="30pt">
                                        <fo:table-cell text-align="left" border-top ="solid 0.0mm gray" border-bottom ="solid 0.1mm gray" padding-left="4pt">
                                            <fo:block><xsl:value-of select="scala:getMessagesText($translator, 'iht.dateOfDeath')" /></fo:block>
                                        </fo:table-cell>
                                        <fo:table-cell text-align="left" border-top ="solid 0.0mm gray" border-bottom ="solid 0.1mm gray" padding-left="4pt">
                                            <fo:block>
                                                <xsl:value-of select="scala:getDateForDisplay($translator,./deceasedDateOfDeath/dateOfDeath)" />
                                            </fo:block>
                                        </fo:table-cell>
                                    </fo:table-row>
                                    <fo:table-row line-height="30pt">
                                        <fo:table-cell text-align="left" border-top ="solid 0.1mm gray" border-bottom ="solid 0.3mm gray" padding-left="4pt">
                                            <fo:block><xsl:value-of select="scala:getMessagesText($translator, 'pdf.clearanceCertificate.estateReport.date.label')" /></fo:block>
                                        </fo:table-cell>
                                        <fo:table-cell text-align="left" border-top ="solid 0.1mm gray" border-bottom ="solid 0.3mm gray" padding-left="4pt">
                                            <fo:block>
                                                <xsl:value-of select="$declaration-date"/>
                                            </fo:block>
                                        </fo:table-cell>
                                    </fo:table-row>
                                </fo:table-body>
                            </fo:table>
                        </fo:block>
                    </fo:block>

                    <fo:block font-family="OpenSans" font-size="12pt" font-weight="normal" space-before="1cm">
                        <xsl:value-of select="scala:getMessagesText($translator, 'pdf.clearanceCertificate.firstParagraph')" />
                    </fo:block>

                    <fo:block font-family="OpenSans" font-size="12pt" font-weight="normal" space-before="1cm">
                        <xsl:value-of select="scala:getMessagesText($translator, 'pdf.clearanceCertificate.coExecutor.paragraph1')" />
                        <fo:list-block space-before="0.25em" space-after="0.25em">
                            <fo:list-item space-after="0.5em">
                                <fo:list-item-label start-indent="1em">
                                    <fo:block>
                                        &#x2022;
                                    </fo:block>
                                </fo:list-item-label>
                                <fo:list-item-body start-indent="2em">
                                    <fo:block>
                                        <xsl:value-of select="concat(applicantDetails/firstName,' ', applicantDetails/lastName)" />
                                    </fo:block>
                                </fo:list-item-body>
                            </fo:list-item>
                            <xsl:for-each select="./coExecutors">
                                <fo:list-item space-after="0.5em">
                                    <fo:list-item-label start-indent="1em">
                                        <fo:block>
                                            &#x2022;
                                        </fo:block>
                                    </fo:list-item-label>
                                    <fo:list-item-body start-indent="2em">
                                        <fo:block>
                                            <xsl:value-of select="concat(firstName,' ', lastName)" />
                                        </fo:block>
                                    </fo:list-item-body>
                                </fo:list-item>
                            </xsl:for-each>
                        </fo:list-block>
                        <xsl:value-of select="scala:getMessagesText($translator,'pdf.clearanceCertificate.coExecutor.paragraph2')"/>
                    </fo:block>

                    <fo:block font-family="OpenSans" font-size="12pt" font-weight="normal" space-before="1cm">
                        <xsl:value-of select="scala:getMessagesText($translator, 'pdf.clearanceCertificate.lastParagraph.line1')"/>
                        <xsl:value-of select="scala:getMessagesText($translator, 'pdf.clearanceCertificate.lastParagraph.line2')"/>
                        <xsl:value-of select="scala:getMessagesText($translator, 'pdf.clearanceCertificate.lastParagraph.line3')"/>
                    </fo:block>

                </fo:flow>
            </fo:page-sequence>
        </fo:root>
    </xsl:template>
</xsl:stylesheet>

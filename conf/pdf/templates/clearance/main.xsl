<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="2.0"
                xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:fo="http://www.w3.org/1999/XSL/Format"
                xmlns:common="http://exslt.org/common"
                xmlns:xalan="http://xml.apache.org" exclude-result-prefixes="common xalan"
                xmlns:scala="java:iht.utils.pdf.XSLScalaBridge"
                xmlns:formatter="java:iht.utils.pdf.PdfFormatter">

    <xsl:include href="pdf/templates/common/table-row.xsl"/>
    <xsl:include href="pdf/templates/common/styles.xsl"/>

    <xsl:param name="translator" />
    <xsl:param name="pdfFormatter"/>
    <xsl:param name="versionParam" select="'1.0'"/>
    <xsl:param name="declaration-date"/>
    <xsl:param name="hmrcLogo"/>


    <xsl:template match="RegistrationDetails">
        <fo:root xsl:use-attribute-sets="root">
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
                        <fo:external-graphic height="50px" content-width="scale-to-fit">
                            <xsl:attribute name="src">
                                <xsl:value-of select="$hmrcLogo" />
                            </xsl:attribute>
                        </fo:external-graphic>
                    </fo:block>
                </fo:static-content>

                <fo:flow flow-name="xsl-region-body">
                    <fo:block role="H1" xsl:use-attribute-sets="h1">
                        <xsl:value-of select="scala:getMessagesText($translator, 'pdf.clearanceCertificate.title')"/>
                    </fo:block>
                    <fo:block xsl:use-attribute-sets="copy">
                        <xsl:value-of select="scala:getMessagesText($translator, 'pdf.clearanceCertificate.summary')"/>
                    </fo:block>


                    <fo:block>
                        <fo:block role="H2" xsl:use-attribute-sets="h2">
                            <xsl:value-of select="scala:getMessagesText($translator, 'page.iht.registration.registrationSummary.deceasedTable.title')" />
                        </fo:block>
                        <fo:block>
                            <fo:table>
                                <fo:table-column column-number="1" />
                                <fo:table-column column-number="2" />
                                <fo:table-body>
                                    <xsl:call-template name="table-row">
                                        <xsl:with-param name="label" select="scala:getMessagesText($translator, 'iht.name.upperCaseInitial')"/>
                                        <xsl:with-param name="value" select="concat(deceasedDetails/firstName,' ', deceasedDetails/lastName)"/>
                                    </xsl:call-template>
                                    <xsl:call-template name="table-row">
                                        <xsl:with-param name="label" select="scala:getMessagesText($translator, 'iht.dateOfDeath')"/>
                                        <xsl:with-param name="value" select="scala:getDateForDisplay($translator,./deceasedDateOfDeath/dateOfDeath)"/>
                                    </xsl:call-template>
                                    <xsl:call-template name="table-row">
                                        <xsl:with-param name="label" select="scala:getMessagesText($translator, 'pdf.clearanceCertificate.estateReport.date.label')"/>
                                        <xsl:with-param name="value" select="$declaration-date"/>
                                    </xsl:call-template>
                                </fo:table-body>
                            </fo:table>
                        </fo:block>
                    </fo:block>

                    <fo:block xsl:use-attribute-sets="copy">
                        <xsl:value-of select="scala:getMessagesText($translator, 'pdf.clearanceCertificate.firstParagraph.part1')"/>
                        <xsl:text> </xsl:text>
                        <xsl:value-of select="concat(' ', deceasedDetails/firstName,' ', deceasedDetails/lastName)" />
                        <xsl:text> </xsl:text>
                        <xsl:value-of select="scala:getMessagesText($translator, 'pdf.clearanceCertificate.firstParagraph.part2')" />
                    </fo:block>

                    <fo:block xsl:use-attribute-sets="copy">
                        <xsl:value-of select="scala:getMessagesText($translator, 'pdf.clearanceCertificate.coExecutor.paragraph1')" />
                    </fo:block>

                    <fo:list-block xsl:use-attribute-sets="copy list">
                        <fo:list-item xsl:use-attribute-sets="list__item">
                            <fo:list-item-label end-indent="1em">
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
                            <fo:list-item xsl:use-attribute-sets="list__item">
                                <fo:list-item-label end-indent="1em">
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

                    <fo:block xsl:use-attribute-sets="copy">
                        <xsl:value-of select="scala:getMessagesText($translator,'pdf.clearanceCertificate.coExecutor.paragraph2')"/>
                    </fo:block>

                    <fo:block xsl:use-attribute-sets="copy">
                        <xsl:value-of select="scala:getMessagesText($translator, 'pdf.clearanceCertificate.lastParagraph.line1')"/>
                    </fo:block>
                    <fo:block xsl:use-attribute-sets="copy">
                        <xsl:value-of select="scala:getMessagesText($translator, 'pdf.clearanceCertificate.lastParagraph.line2')"/>
                    </fo:block>
                    <fo:block xsl:use-attribute-sets="copy">
                        <xsl:value-of select="scala:getMessagesText($translator, 'pdf.clearanceCertificate.lastParagraph.line3')"/>
                    </fo:block>


                </fo:flow>
            </fo:page-sequence>
        </fo:root>
    </xsl:template>
</xsl:stylesheet>

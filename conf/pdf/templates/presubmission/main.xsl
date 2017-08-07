<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="2.0"
                xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:fo="http://www.w3.org/1999/XSL/Format"
                xmlns:common="http://exslt.org/common"
                xmlns:scala="java:iht.utils.pdf.XSLScalaBridge"
                xmlns:xalan="http://xml.apache.org" exclude-result-prefixes="common xalan">

    <xsl:param name="translator"/>
    <xsl:param name="versionParam" select="'1.0'"/>
    <xsl:param name="applicantName"/>

    <xsl:include href="pdf/templates/common/table-row.xsl"/>
    <xsl:include href="pdf/templates/presubmission/table-row.xsl"/>

    <xsl:include href="pdf/templates/common/registration/case-details.xsl"/>
    <xsl:include href="pdf/templates/presubmission/application-details.xsl"/>

    <xsl:attribute-set name="root">
        <xsl:attribute name="font-family">OpenSans</xsl:attribute>
    </xsl:attribute-set>

    <xsl:attribute-set name="copy">
        <xsl:attribute name="font-family">OpenSans</xsl:attribute>
        <xsl:attribute name="font-size">12pt</xsl:attribute>
        <xsl:attribute name="font-weight">normal</xsl:attribute>
        <xsl:attribute name="space-before">0.5cm</xsl:attribute>
        <xsl:attribute name="color">blue</xsl:attribute>
    </xsl:attribute-set>
    <xsl:attribute-set name="copy--heavy">
        <xsl:attribute name="font-family">OpenSans-Bold</xsl:attribute>
        <xsl:attribute name="font-weight">bold</xsl:attribute>
        <xsl:attribute name="color">plum</xsl:attribute>
    </xsl:attribute-set>

    <xsl:attribute-set name="h1">
        <xsl:attribute name="font-family">OpenSans-Bold</xsl:attribute>
        <xsl:attribute name="font-size">24pt</xsl:attribute>
        <xsl:attribute name="font-weight">bold</xsl:attribute>
        <xsl:attribute name="space-before">0.5cm</xsl:attribute>
        <xsl:attribute name="color">red</xsl:attribute>
    </xsl:attribute-set>

    <xsl:attribute-set name="h2">
        <xsl:attribute name="font-family">OpenSans-Bold</xsl:attribute>
        <xsl:attribute name="font-size">18pt</xsl:attribute>
        <xsl:attribute name="font-weight">bold</xsl:attribute>
        <xsl:attribute name="space-before">0.5cm</xsl:attribute>
        <xsl:attribute name="space-after">0.5cm</xsl:attribute>
        <xsl:attribute name="color">green</xsl:attribute>
    </xsl:attribute-set>

    <xsl:attribute-set name="h3">
        <xsl:attribute name="font-family">OpenSans</xsl:attribute>
        <xsl:attribute name="font-size">16pt</xsl:attribute>
        <xsl:attribute name="font-weight">bold</xsl:attribute>
        <xsl:attribute name="space-before">0.5cm</xsl:attribute>
        <xsl:attribute name="space-after">0.25cm</xsl:attribute>
        <xsl:attribute name="color">orange</xsl:attribute>
    </xsl:attribute-set>

    <xsl:attribute-set name="h4">
        <xsl:attribute name="font-family">OpenSans-Bold</xsl:attribute>
        <xsl:attribute name="font-size">14pt</xsl:attribute>
        <xsl:attribute name="font-weight">bold</xsl:attribute>
        <xsl:attribute name="color">saddlebrown</xsl:attribute>
    </xsl:attribute-set>

    <xsl:attribute-set name="row">
        <xsl:attribute name="border-top">1px dashed maroon</xsl:attribute>
    </xsl:attribute-set>
    <xsl:attribute-set name="row--heavy">
        <xsl:attribute name="border-top">3px dotted blueviolet</xsl:attribute>
    </xsl:attribute-set>
    <xsl:attribute-set name="row--total">
        <xsl:attribute name="border-bottom">3px solid darkcyan</xsl:attribute>
    </xsl:attribute-set>

    <xsl:attribute-set name="cell">
        <xsl:attribute name="padding-top">5px</xsl:attribute>
        <xsl:attribute name="padding-bottom">5px</xsl:attribute>
        <xsl:attribute name="padding-left">5px</xsl:attribute>
        <xsl:attribute name="padding-right">5px</xsl:attribute>
        <xsl:attribute name="font-size">12pt</xsl:attribute>
        <xsl:attribute name="color">mediumslateblue</xsl:attribute>
    </xsl:attribute-set>

    <xsl:attribute-set name="set-right">
        <xsl:attribute name="text-align">right</xsl:attribute>
    </xsl:attribute-set>

    <xsl:attribute-set name="page-header">
        <xsl:attribute name="border-bottom">1px solid red</xsl:attribute>
    </xsl:attribute-set>

    <xsl:attribute-set name="page-footer--title">
        <xsl:attribute name="font-family">OpenSans</xsl:attribute>
        <xsl:attribute name="font-size">8pt</xsl:attribute>
        <xsl:attribute name="font-weight">normal</xsl:attribute>
        <xsl:attribute name="text-align">left</xsl:attribute>
        <xsl:attribute name="space-before">0.5cm</xsl:attribute>
        <xsl:attribute name="color">blue</xsl:attribute>
    </xsl:attribute-set>

    <xsl:attribute-set name="page-footer--page-number">
        <xsl:attribute name="font-family">OpenSans</xsl:attribute>
        <xsl:attribute name="font-size">8pt</xsl:attribute>
        <xsl:attribute name="font-weight">normal</xsl:attribute>
        <xsl:attribute name="text-align">right</xsl:attribute>
        <xsl:attribute name="space-before">0.5cm</xsl:attribute>
        <xsl:attribute name="color">blue</xsl:attribute>
    </xsl:attribute-set>

    <xsl:template match="/">
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
            <x:xmpmeta xmlns:x="adobe:ns:meta/">
                <rdf:RDF xmlns:rdf="http://www.w3.org/1999/02/22-rdf-syntax-ns#">
                    <rdf:Description rdf:about=""
                                     xmlns:dc="http://purl.org/dc/elements/1.1/">
                        <dc:title><xsl:value-of select="scala:getMessagesText($translator, 'iht.inheritanceTaxEstateReport')"/></dc:title>
                        <dc:creator><xsl:value-of select="scala:getMessagesText($translator, 'pdf.meta.author')"/></dc:creator>
                        <dc:description><xsl:value-of select="scala:getMessagesText($translator, 'iht.inheritanceTaxEstateReport')"/></dc:description>
                    </rdf:Description>
                </rdf:RDF>
            </x:xmpmeta>

            <fo:page-sequence master-reference="simple">
                <fo:static-content flow-name="xsl-region-before">
                    <fo:block xsl:use-attribute-sets="page-header">
                        <fo:external-graphic src="url('pdf/logo/hmrc_logo.jpg')" height="50px" content-width="scale-to-fit"/>
                    </fo:block>
                </fo:static-content>

                <fo:static-content flow-name="xsl-region-after">
                    <fo:block>
                        <fo:table>
                            <fo:table-body>
                                <fo:table-row>
                                    <fo:table-cell xsl:use-attribute-sets="page-footer--title">
                                        <fo:block><xsl:value-of select="scala:getMessagesText($translator, 'iht.inheritanceTaxEstateReport')"/></fo:block>
                                    </fo:table-cell>
                                    <fo:table-cell xsl:use-attribute-sets="page-footer--page-number">
                                        <fo:block>
                                                <xsl:value-of select="scala:getMessagesText($translator, 'pdf.page.number')" />
                                                <xsl:text>&#160;</xsl:text>
                                                <fo:page-number format="1" />
                                        </fo:block>
                                    </fo:table-cell>
                                </fo:table-row>
                            </fo:table-body>
                        </fo:table>
                    </fo:block>
                </fo:static-content>

                <fo:flow flow-name="xsl-region-body">
                    <fo:block role="H1" xsl:use-attribute-sets="h1">
                        A: <xsl:value-of select="scala:getMessagesText($translator, 'iht.inheritanceTaxEstateReport')"/>
                    </fo:block>
                    <fo:block xsl:use-attribute-sets="copy">
                        <xsl:value-of
                                select="concat(scala:getMessagesText($translator, 'pdf.inheritance.tax.reference'), ' ', $ihtReference)"/>
                    </fo:block>

                    <fo:block xsl:use-attribute-sets="copy">
                        <xsl:value-of select="scala:getMessagesTextWithParameters($translator, 'pdf.inheritance.tax.application.summary.p1', $deceasedName, $applicantName)"/>
                    </fo:block>

                    <fo:block xsl:use-attribute-sets="copy">
                        <xsl:value-of select="scala:getMessagesText($translator, 'pdf.inheritance.tax.application.summary.p2')"/>
                    </fo:block>
                    <xsl:apply-templates/>
                </fo:flow>
            </fo:page-sequence>
        </fo:root>

    </xsl:template>
</xsl:stylesheet>

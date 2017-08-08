<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="2.0"
                xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:fo="http://www.w3.org/1999/XSL/Format"
                xmlns:common="http://exslt.org/common"
                xmlns:scala="java:iht.utils.pdf.XSLScalaBridge"
                xmlns:xalan="http://xml.apache.org" exclude-result-prefixes="common xalan">

    <xsl:param name="translator"/>
    <xsl:param name="versionParam" select="'1.0'"/>
    <xsl:param name="declarationDate"/>
    <xsl:param name="deceasedName"/>

    <xsl:include href="pdf/templates/common/table-row.xsl"/>
    <xsl:include href="pdf/templates/common/registration/case-details.xsl"/>
    <xsl:include href="pdf/templates/postsubmission/iht-return.xsl"/>

    <xsl:include href="pdf/templates/common/styles.xsl"/>

    <xsl:template match="/">
        <fo:root xsl:use-attribute-sets="root">
            <fo:layout-master-set>
                <fo:simple-page-master master-name="main-other"
                                       page-height="29.7cm" page-width="21.0cm"
                                       margin-top="0.2cm" margin-bottom="0.2cm"
                                       margin-left="2cm" margin-right="2cm">
                    <fo:region-body margin-top="2.5cm" margin-bottom="2.5cm"/>
                    <fo:region-before extent="2.4cm"/>
                    <fo:region-after extent="1.4cm"/>

                </fo:simple-page-master>

                <fo:simple-page-master master-name="main-last"
                                       page-height="29.7cm" page-width="21.0cm"
                                       margin-top="0.2cm" margin-bottom="0.2cm"
                                       margin-left="2cm" margin-right="2cm">
                    <fo:region-body margin-top="2.5cm" margin-bottom="2.5cm"/>
                    <fo:region-before extent="2.4cm"/>
                    <fo:region-after extent="2.4cm" region-name="xsl-region-end-of-report"/>
                </fo:simple-page-master>

                <fo:page-sequence-master master-name="main">
                    <fo:repeatable-page-master-alternatives>
                        <fo:conditional-page-master-reference page-position="last" master-reference="main-last"/>
                        <fo:conditional-page-master-reference master-reference="main-other"/>
                    </fo:repeatable-page-master-alternatives>
                </fo:page-sequence-master>

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

            <fo:page-sequence master-reference="main">
                <fo:static-content flow-name="xsl-region-before">
                    <fo:block xsl:use-attribute-sets="page-header">
                        <fo:external-graphic src="url('pdf/logo/hmrc_logo.jpg')" height="50px" content-width="scale-to-fit"/>
                    </fo:block>
                </fo:static-content>

                <fo:static-content flow-name="xsl-region-end-of-report">
                    <fo:block xsl:use-attribute-sets="section copy--heavy">
                        <xsl:value-of
                                select="scala:getMessagesTextWithParameter($translator, 'iht.pdf.endOfTheEstateReport', $deceasedName)"/>
                    </fo:block>

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
                       <xsl:value-of select="scala:getMessagesText($translator, 'iht.inheritanceTaxEstateReport')"/>
                    </fo:block>
                    <fo:block xsl:use-attribute-sets="copy--lede">
                        <fo:block xsl:use-attribute-sets="copy">
                            <xsl:value-of
                                    select="concat(scala:getMessagesText($translator, 'pdf.inheritance.tax.reference'),' ', $ihtReference)"/>
                        </fo:block>
                        <fo:block xsl:use-attribute-sets="copy">
                            <xsl:value-of select="concat(scala:getMessagesText($translator, 'pdf.inheritance.tax.declaration.date.text'),' ', $declarationDate)"/>
                        </fo:block>
                    </fo:block>
                    <xsl:apply-templates/>
                </fo:flow>
            </fo:page-sequence>

        </fo:root>

    </xsl:template>
</xsl:stylesheet>

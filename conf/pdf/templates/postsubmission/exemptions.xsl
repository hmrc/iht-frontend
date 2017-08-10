<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="2.0"
                xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:fo="http://www.w3.org/1999/XSL/Format"
                xmlns:common="http://exslt.org/common"
                xmlns:xalan="http://xml.apache.org" exclude-result-prefixes="common xalan"
                xmlns:scala="java:iht.utils.pdf.XSLScalaBridge">

    <xsl:param name="translator"/>
    <xsl:param name="pdfFormatter"/>
    <xsl:param name="versionParam" select="'1.0'"/>
    <xsl:param name="exemptionsTotal"/>

    <xsl:template name="exemptions">
        <xsl:choose>
            <xsl:when test="freeEstate/estateExemptions != ''">

                <fo:block page-break-inside="avoid">
                    <fo:block role="H2" xsl:use-attribute-sets="h2">
                        <xsl:value-of select="scala:getMessagesText($translator, 'iht.estateReport.exemptions.title')"/>
                    </fo:block>



                        </xsl:for-each>
                        </fo:table-body>
                    </fo:table>
                </fo:block>
            </xsl:when>
        </xsl:choose>

    </xsl:template>
</xsl:stylesheet>

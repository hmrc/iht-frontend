<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="2.0"
                xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:fo="http://www.w3.org/1999/XSL/Format"
                xmlns:common="http://exslt.org/common"
                xmlns:xalan="http://xml.apache.org" exclude-result-prefixes="common xalan"
                xmlns:scala="java:iht.utils.pdf.XSLScalaBridge"
                xmlns:formatter="java:iht.utils.pdf.PdfFormatter">

    <xsl:param name="translator"/>
    <xsl:param name="pdfFormatter"/>
    <xsl:param name="versionParam" select="'1.0'"/>
    <xsl:param name="giftsTotal"/>
    <xsl:param name="giftsExemptionsTotal"/>
    <xsl:param name="giftsTotalExclExemptions"/>

    <xsl:template name="gifts">
        <xsl:param name="value"/>

        <xsl:choose>
            <xsl:when test="gifts/array != ''">
                <fo:block role="H2" xsl:use-attribute-sets="h2" page-break-before="always">
                    <xsl:value-of
                            select="scala:getMessagesText($translator, 'iht.estateReport.gifts.valueOfGiftsGivenAway')"/>
                </fo:block>

                <!-- Gifts table  -->
                <fo:block>
                    <fo:block>
                        <fo:table>
                            <fo:table-header>
                                <fo:table-row>
                                    <fo:table-cell xsl:use-attribute-sets="tabular-cell">
                                        <fo:block/>
                                    </fo:table-cell>
                                    <fo:table-cell xsl:use-attribute-sets="tabular-cell tabular-cell--header set-right">
                                        <fo:block>
                                            <xsl:value-of
                                                    select="scala:getMessagesText($translator, 'page.iht.application.gifts.lastYears.tableTitle1')"/>
                                        </fo:block>
                                    </fo:table-cell>
                                    <fo:table-cell xsl:use-attribute-sets="tabular-cell tabular-cell--header set-right">
                                        <fo:block>
                                            <xsl:value-of
                                                    select="scala:getMessagesText($translator, 'page.iht.exemptions.title')"/>
                                        </fo:block>
                                    </fo:table-cell>
                                    <fo:table-cell xsl:use-attribute-sets="tabular-cell tabular-cell--no-right-border tabular-cell--header set-right">
                                        <fo:block>
                                            <xsl:value-of
                                                    select="scala:getMessagesText($translator, 'page.iht.application.gifts.lastYears.tableTitle3')"/>
                                        </fo:block>
                                    </fo:table-cell>
                                </fo:table-row>
                            </fo:table-header>

                            <fo:table-body>
                                <xsl:for-each select="gifts/array">
                                    <fo:table-row>
                                        <fo:table-cell xsl:use-attribute-sets="tabular-cell">
                                            <fo:block>
                                                <xsl:value-of
                                                        select="scala:getDateForDisplay($translator,dateOfGift)"/>
                                            </fo:block>
                                        </fo:table-cell>

                                        <xsl:variable name="giftsValue">
                                            <xsl:value-of select="assetTotalValue"/>
                                        </xsl:variable>
                                        <xsl:variable name="exemptionsValue">
                                            <xsl:choose>
                                                <xsl:when test="string-length(assetDescription)>0 and contains(assetDescription, '&#xA3;')">
                                                    <xsl:value-of select="number(substring-after(assetDescription, '&#xA3;'))"/>
                                                </xsl:when>
                                                <xsl:otherwise>
                                                    0.00
                                                </xsl:otherwise>
                                            </xsl:choose>
                                        </xsl:variable>

                                        <fo:table-cell xsl:use-attribute-sets="tabular-cell set-right">
                                            <fo:block>
                                                <xsl:if test="$giftsValue">
                                                     &#xA3;<xsl:value-of select='format-number(number(valuePrevOwned), "##,##0.00")'/>
                                                </xsl:if>
                                            </fo:block>
                                        </fo:table-cell>

                                        <fo:table-cell xsl:use-attribute-sets="tabular-cell set-right">
                                            <fo:block>
                                                &#xA3;<xsl:value-of select='format-number($exemptionsValue, "##,##0.00")'/>
                                            </fo:block>
                                        </fo:table-cell>

                                        <fo:table-cell xsl:use-attribute-sets="tabular-cell tabular-cell--no-right-border set-right">
                                            <fo:block>
                                                <xsl:choose>
                                                    <xsl:when test="$giftsValue &gt; 0">
                                                        &#xA3;<xsl:value-of
                                                            select='format-number(($giftsValue), "##,##0.00")'/>
                                                    </xsl:when>
                                                    <xsl:otherwise>
                                                        &#xA3;0.00
                                                    </xsl:otherwise>
                                                </xsl:choose>
                                            </fo:block>
                                        </fo:table-cell>
                                    </fo:table-row>
                                </xsl:for-each>


                            </fo:table-body>
                        </fo:table>
                    </fo:block>


                    <!-- Total Gifts row-->
                    <fo:block>
                        <fo:table>
                            <fo:table-column column-number="1" column-width="25%"/>
                            <fo:table-column column-number="2" column-width="25%"/>
                            <fo:table-column column-number="3" column-width="25%"/>
                            <fo:table-column column-number="4" column-width="25%"/>

                            <fo:table-body>
                                <xsl:call-template name="table-row--currency-3-col">
                                    <xsl:with-param name="label"
                                                    select="scala:getMessagesText($translator, 'iht.estateReport.gifts.totalOverSevenYears')"/>
                                    <xsl:with-param name="value1">
                                        <xsl:value-of select='format-number($giftsTotalExclExemptions, "##,##0.00")'/>
                                    </xsl:with-param>
                                    <xsl:with-param name="value2">
                                        <xsl:value-of select='format-number($giftsExemptionsTotal, "##,##0.00")'/>
                                    </xsl:with-param>
                                    <xsl:with-param name="value3">
                                        <xsl:value-of select='format-number($giftsTotal, "##,##0.00")'/>
                                    </xsl:with-param>
                                </xsl:call-template>

                            </fo:table-body>

                        </fo:table>
                     </fo:block>
                </fo:block>
            </xsl:when>
            <xsl:otherwise>
                <xsl:value-of select="scala:getMessagesText($translator, 'site.noneInEstate')"/>
            </xsl:otherwise>
        </xsl:choose>

    </xsl:template>
</xsl:stylesheet>

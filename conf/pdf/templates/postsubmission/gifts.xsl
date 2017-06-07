<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="2.0"
                xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:fo="http://www.w3.org/1999/XSL/Format"
                xmlns:common="http://exslt.org/common"
                xmlns:xalan="http://xml.apache.org" exclude-result-prefixes="common xalan"
                xmlns:i18n="java:iht.utils.pdf.MessagesTranslator"
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
                <fo:block font-family="OpenSans-Bold" font-size="16" font-weight="bold" space-before="1.0cm">
                    <xsl:value-of
                            select="i18n:getMessagesText($translator, 'iht.estateReport.gifts.valueOfGiftsGivenAway')"/>
                </fo:block>

                <!-- Gifts table  -->
                <fo:block font-family="OpenSans" font-size="12pt" font-weight="normal" space-before="0.5cm">
                    <fo:block>
                        <fo:table space-before="0.5cm">
                            <fo:table-header>
                                <fo:table-row line-height="18pt">
                                    <fo:table-cell text-align="left" padding-left="4pt">
                                        <fo:block/>
                                    </fo:table-cell>
                                    <fo:table-cell text-align="right" padding-left="4pt">
                                        <fo:block>
                                            <xsl:value-of
                                                    select="i18n:getMessagesText($translator, 'page.iht.application.gifts.lastYears.tableTitle1')"/>
                                        </fo:block>
                                    </fo:table-cell>
                                    <fo:table-cell text-align="right" padding-left="4pt">
                                        <fo:block>
                                            <xsl:value-of
                                                    select="i18n:getMessagesText($translator, 'page.iht.exemptions.title')"/>
                                        </fo:block>
                                    </fo:table-cell>
                                    <fo:table-cell text-align="right" padding-left="4pt">
                                        <fo:block>
                                            <xsl:value-of
                                                    select="i18n:getMessagesText($translator, 'page.iht.application.gifts.lastYears.tableTitle3')"/>
                                        </fo:block>
                                    </fo:table-cell>
                                </fo:table-row>
                            </fo:table-header>
                            <fo:table-body font-size="12pt">
                                <xsl:for-each select="gifts/array">
                                    <fo:table-row border-top="solid 0.1mm gray" line-height="30pt">
                                        <fo:table-cell text-align="left" padding-left="4pt">
                                            <fo:block>
                                                <xsl:value-of
                                                        select="formatter:getDateForDisplay($pdfFormatter,dateOfGift)"/>
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

                                        <fo:table-cell text-align="right" padding-left="4pt">
                                            <fo:block>
                                                <xsl:if test="$giftsValue">
                                                    <xsl:choose>
                                                        <xsl:when test="valuePrevOwned &gt; 0">
                                                            &#xA3;<xsl:value-of select='format-number(number(valuePrevOwned), "##,##0.00")'/>
                                                        </xsl:when>
                                                    </xsl:choose>
                                                </xsl:if>
                                            </fo:block>
                                        </fo:table-cell>

                                        <fo:table-cell text-align="right" padding-left="4pt">
                                            <fo:block>
                                                <xsl:choose>
                                                    <xsl:when test="$exemptionsValue &gt; 0">
                                                        <xsl:value-of select='format-number($exemptionsValue, "##,##0.00")'/>
                                                     </xsl:when>
                                                    <xsl:otherwise>
                                                    </xsl:otherwise>
                                                </xsl:choose>
                                            </fo:block>
                                        </fo:table-cell>

                                        <fo:table-cell text-align="right" padding-left="4pt">
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
                                <xsl:comment>Blank row to display line at end of section</xsl:comment>

                            </fo:table-body>
                        </fo:table>
                    </fo:block>
                    <!-- Total Gifts row-->
                    <fo:table space-before="0.5cm">
                        <fo:table-column column-number="1" column-width="25%"/>
                        <fo:table-column column-number="2" column-width="25%"/>
                        <fo:table-column column-number="3" column-width="25%"/>
                        <fo:table-column column-number="4" column-width="25%"/>

                        <fo:table-body font-size="12pt">
                            <xsl:call-template name="table-row-money-3-values-border-top-black">
                                <xsl:with-param name="label"
                                                select="i18n:getMessagesText($translator, 'iht.estateReport.gifts.totalOverSevenYears')"/>
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
                            <xsl:call-template name="table-row-blank-short-vpad-border-top-grey-thin"/>
                        </fo:table-body>

                    </fo:table>
                </fo:block>
            </xsl:when>
            <xsl:otherwise>
                <xsl:value-of select="i18n:getMessagesText($translator, 'site.noneInEstate')"/>
            </xsl:otherwise>
        </xsl:choose>

    </xsl:template>
</xsl:stylesheet>

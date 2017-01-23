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

    <xsl:template name="gifts">
        <xsl:param name="value"/>

        <xsl:choose>
            <xsl:when test="gifts/array != ''">
                <fo:block font-family="OpenSans-Bold" font-size="16" font-weight="bold" space-before="1.5cm">
                    <xsl:value-of
                            select="i18n:getMessagesText($translator, 'iht.estateReport.gifts.givenAway.title')"/>
                </fo:block>

                <fo:block font-family="OpenSans-Bold" font-size="16" font-weight="bold" space-before="0.5cm">
                    <xsl:value-of
                            select="i18n:getMessagesText($translator, 'iht.estateReport.gifts.givenAwayIn7YearsBeforeDeath')"/>
                </fo:block>

                <!-- Gifts table  -->
                <fo:block font-family="OpenSans" font-size="12pt" font-weight="normal" space-before="0.5cm">
                    <fo:block>
                        <fo:table space-before="0.5cm">
                            <fo:table-header>
                                <fo:table-row>
                                    <fo:table-cell>
                                        <fo:block>
                                            <xsl:text/>
                                        </fo:block>
                                    </fo:table-cell>
                                    <fo:table-cell>
                                        <fo:block>
                                            <xsl:value-of
                                                    select="i18n:getMessagesText($translator, 'page.iht.application.gifts.lastYears.tableTitle1')"/>
                                        </fo:block>
                                    </fo:table-cell>
                                    <fo:table-cell>
                                        <fo:block>
                                            <xsl:value-of
                                                    select="i18n:getMessagesText($translator, 'page.iht.application.gifts.lastYears.tableTitle2')"/>
                                        </fo:block>
                                    </fo:table-cell>
                                </fo:table-row>
                            </fo:table-header>
                            <fo:table-body font-size="12pt">
                                <xsl:for-each select="gifts/array">
                                    <fo:table-row border-top="solid 0.3mm black" line-height="30pt">
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
                                                <xsl:when
                                                        test="string-length(assetDescription)>0 and contains(assetDescription, '&#xA3;')">
                                                    &#xA3;<xsl:value-of
                                                        select="format-number(number(substring-after(assetDescription, '&#xA3;')), '##,###.##')"/>
                                                </xsl:when>
                                                <xsl:otherwise>

                                                </xsl:otherwise>
                                            </xsl:choose>
                                        </xsl:variable>
                                        <fo:table-cell text-align="left" padding-left="4pt">
                                            <fo:block>
                                                <xsl:if test="$giftsValue">
                                                    <xsl:choose>
                                                        <xsl:when test="$giftsValue &gt; 1">
                                                            &#xA3;<xsl:value-of select='format-number(number($giftsValue), "##,###.00")'/>
                                                        </xsl:when>
                                                        <xsl:otherwise>
                                                            &#xA3;<xsl:value-of select="$giftsValue"/>
                                                        </xsl:otherwise>
                                                    </xsl:choose>
                                                </xsl:if>
                                            </fo:block>
                                        </fo:table-cell>
                                        <fo:table-cell text-align="left" padding-left="4pt">
                                            <fo:block>
                                                <xsl:value-of select='$exemptionsValue'/>
                                            </fo:block>
                                        </fo:table-cell>
                                    </fo:table-row>
                                </xsl:for-each>
                                <xsl:comment>Blank row to display line at end of section</xsl:comment>
                                <xsl:call-template name="table-row-blank-tall-border-both-black-thick"/>
                            </fo:table-body>
                        </fo:table>
                    </fo:block>
                    <!-- Total Gifts row-->
                    <fo:block font-family="OpenSans-Bold" font-size="16" font-weight="bold" space-before="0.5cm">
                        <xsl:value-of select="i18n:getMessagesText($translator, 'pdf.gifts.total.title')"/>
                    </fo:block>

                    <fo:table space-before="0.5cm">
                        <fo:table-column column-number="1" column-width="60%"/>
                        <fo:table-column column-number="2" column-width="40%"/>
                        <fo:table-body font-size="12pt">

                            <xsl:call-template name="table-row-money-tall-border-top-black">
                                <xsl:with-param name="label"
                                                select="i18n:getMessagesText($translator, 'pdf.total.text')"/>
                                <xsl:with-param name="value" select='format-number(number($giftsTotal), "##,###.00")'/>
                            </xsl:call-template>

                            <xsl:comment>Blank row to display line at end of section</xsl:comment>
                            <xsl:call-template name="table-row-blank-tall-border-both-grey-thin"/>

                        </fo:table-body>
                    </fo:table>
                </fo:block>
            </xsl:when>
        </xsl:choose>

    </xsl:template>
</xsl:stylesheet>

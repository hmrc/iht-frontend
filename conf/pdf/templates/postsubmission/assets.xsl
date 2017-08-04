<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="2.0"
                xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:fo="http://www.w3.org/1999/XSL/Format"
                xmlns:common="http://exslt.org/common"
                xmlns:xalan="http://xml.apache.org" exclude-result-prefixes="common xalan"
                xmlns:scala="java:iht.utils.pdf.XSLScalaBridge">

    <xsl:param name="translator"/>
    <xsl:param name="pdfFormatter"/>
    <xsl:param name="versionParam" select="'1.0'"/>
    <xsl:param name="assetsTotal"/>
    <xsl:include href="pdf/templates/postsubmission/trusts.xsl"/>
    <xsl:template name="assets">
        <xsl:param name="value"/>
        <xsl:choose>
            <xsl:when test="freeEstate/estateAssets != ''">
                <fo:block font-family="OpenSans-Bold" font-size="16" font-weight="bold" page-break-before="always">
                    <xsl:value-of select="scala:getMessagesText($translator, 'iht.estateReport.assets.inEstate')"/>
                </fo:block>
                <fo:block font-family="OpenSans" font-size="12pt" font-weight="normal" space-before="0.5cm">

                    <xsl:for-each select="freeEstate/estateAssets">
                        <xsl:if test="assetCode='0016'">
                            <fo:table>
                                <fo:table-column column-number="1" column-width="60%"/>
                                <fo:table-column column-number="2" column-width="40%"/>
                                <fo:table-body font-size="12pt">

                                    <xsl:call-template name="table-row-short-vpad-border-top-black">
                                        <xsl:with-param name="label"
                                                        select="scala:getMessagesText($translator, 'pdf.assetDescription.text')"/>
                                        <xsl:with-param name="value" select="assetDescription"/>
                                    </xsl:call-template>

                                    <xsl:call-template name="table-row-money-tall">
                                        <xsl:with-param name="label"
                                                        select="scala:getMessagesText($translator, 'iht.value')"/>
                                        <xsl:with-param name="value" select='format-number(number(assetTotalValue), "##,###.00")'/>
                                    </xsl:call-template>
                                    <xsl:comment>Blank row to display line at end of section</xsl:comment>
                                    <xsl:call-template name="table-row-blank-tall-border-both-grey-thin"/>
                                </fo:table-body>

                            </fo:table>
                        </xsl:if>
                    </xsl:for-each>

                    <xsl:for-each select="freeEstate/estateAssets">
                        <xsl:if test="assetCode='0017'">
                            <fo:table>
                                <fo:table-column column-number="1" column-width="60%"/>
                                <fo:table-column column-number="2" column-width="40%"/>
                                <fo:table-body font-size="12pt">

                                    <xsl:call-template name="table-row-short-vpad-border-top-black">
                                        <xsl:with-param name="label"
                                                        select="scala:getMessagesText($translator, 'pdf.assetDescription.text')"/>
                                        <xsl:with-param name="value" select="scala:getMessagesText($translator, 'pdf.assets.property.otherResidential')"/>
                                    </xsl:call-template>

                                    <xsl:call-template name="table-row-money-tall">
                                        <xsl:with-param name="label"
                                                        select="scala:getMessagesText($translator, 'iht.value')"/>
                                        <xsl:with-param name="value" select='format-number(number(assetTotalValue), "##,###.00")'/>
                                    </xsl:call-template>
                                    <xsl:comment>Blank row to display line at end of section</xsl:comment>
                                    <xsl:call-template name="table-row-blank-tall-border-both-grey-thin"/>
                                </fo:table-body>

                            </fo:table>
                        </xsl:if>
                    </xsl:for-each>

                    <xsl:for-each select="freeEstate/estateAssets">
                        <xsl:if test="assetCode='0018'">
                            <fo:table>
                                <fo:table-column column-number="1" column-width="60%"/>
                                <fo:table-column column-number="2" column-width="40%"/>
                                <fo:table-body font-size="12pt">

                                    <xsl:call-template name="table-row-short-vpad-border-top-black">
                                        <xsl:with-param name="label"
                                                        select="scala:getMessagesText($translator, 'pdf.assetDescription.text')"/>
                                        <xsl:with-param name="value" select="scala:getMessagesText($translator, 'pdf.assets.property.otherLandAndBuildings')"/>
                                    </xsl:call-template>

                                    <xsl:call-template name="table-row-money-tall">
                                        <xsl:with-param name="label"
                                                        select="scala:getMessagesText($translator, 'iht.value')"/>
                                        <xsl:with-param name="value" select='format-number(number(assetTotalValue), "##,###.00")'/>
                                    </xsl:call-template>
                                    <xsl:comment>Blank row to display line at end of section</xsl:comment>
                                    <xsl:call-template name="table-row-blank-tall-border-both-grey-thin"/>
                                </fo:table-body>

                            </fo:table>
                        </xsl:if>
                    </xsl:for-each>

                    <xsl:for-each select="freeEstate/estateAssets">
                        <xsl:if test="assetCode='9001'">
                            <fo:table>
                                <fo:table-column column-number="1" column-width="60%"/>
                                <fo:table-column column-number="2" column-width="40%"/>
                                <fo:table-body font-size="12pt">

                                    <xsl:call-template name="table-row-short-vpad-border-top-black">
                                        <xsl:with-param name="label"
                                                        select="scala:getMessagesText($translator, 'pdf.assetDescription.text')"/>
                                        <xsl:with-param name="value" select="assetDescription"/>
                                    </xsl:call-template>

                                    <xsl:call-template name="table-row-money-tall">
                                        <xsl:with-param name="label"
                                                        select="scala:getMessagesText($translator, 'iht.value')"/>
                                        <xsl:with-param name="value"
                                                        select='format-number(number(assetTotalValue), "##,###.00")'/>
                                    </xsl:call-template>
                                    <xsl:comment>Blank row to display line at end of section</xsl:comment>
                                    <xsl:call-template name="table-row-blank-tall-border-both-grey-thin"/>
                                </fo:table-body>
                            </fo:table>
                        </xsl:if>
                    </xsl:for-each>

                    <xsl:for-each select="freeEstate/estateAssets">
                        <xsl:if test="assetCode='9004'">
                            <fo:table>
                                <fo:table-column column-number="1" column-width="60%"/>
                                <fo:table-column column-number="2" column-width="40%"/>
                                <fo:table-body font-size="12pt">

                                    <xsl:call-template name="table-row-short-vpad-border-top-black">
                                        <xsl:with-param name="label"
                                                        select="scala:getMessagesText($translator, 'pdf.assetDescription.text')"/>
                                        <xsl:with-param name="value" select="assetDescription"/>
                                    </xsl:call-template>

                                    <xsl:call-template name="table-row-money-tall">
                                        <xsl:with-param name="label"
                                                        select="scala:getMessagesText($translator, 'iht.value')"/>
                                        <xsl:with-param name="value"
                                                        select='format-number(number(assetTotalValue), "##,###.00")'/>
                                    </xsl:call-template>
                                    <xsl:comment>Blank row to display line at end of section</xsl:comment>
                                    <xsl:call-template name="table-row-blank-tall-border-both-grey-thin"/>
                                </fo:table-body>
                            </fo:table>
                        </xsl:if>
                    </xsl:for-each>


                    <xsl:for-each select="freeEstate/estateAssets">
                        <xsl:if test="assetCode='9005'">
                            <fo:table>
                                <fo:table-column column-number="1" column-width="60%"/>
                                <fo:table-column column-number="2" column-width="40%"/>
                                <fo:table-body font-size="12pt">

                                    <xsl:call-template name="table-row-short-vpad-border-top-black">
                                        <xsl:with-param name="label"
                                                        select="scala:getMessagesText($translator, 'pdf.assetDescription.text')"/>
                                        <xsl:with-param name="value" select="assetDescription"/>
                                    </xsl:call-template>

                                    <xsl:call-template name="table-row-money-tall">
                                        <xsl:with-param name="label"
                                                        select="scala:getMessagesText($translator, 'iht.value')"/>
                                        <xsl:with-param name="value"
                                                        select='format-number(number(assetTotalValue), "##,###.00")'/>
                                    </xsl:call-template>
                                    <xsl:comment>Blank row to display line at end of section</xsl:comment>
                                    <xsl:call-template name="table-row-blank-tall-border-both-grey-thin"/>
                                </fo:table-body>
                            </fo:table>
                        </xsl:if>
                    </xsl:for-each>



                    <xsl:for-each select="freeEstate/estateAssets">
                        <xsl:if test="assetCode='9008'">
                            <fo:table>
                                <fo:table-column column-number="1" column-width="60%"/>
                                <fo:table-column column-number="2" column-width="40%"/>
                                <fo:table-body font-size="12pt">

                                    <xsl:call-template name="table-row-short-vpad-border-top-black">
                                        <xsl:with-param name="label"
                                                        select="scala:getMessagesText($translator, 'pdf.assetDescription.text')"/>
                                        <xsl:with-param name="value" select="assetDescription"/>
                                    </xsl:call-template>

                                    <xsl:call-template name="table-row-money-tall">
                                        <xsl:with-param name="label"
                                                        select="scala:getMessagesText($translator, 'iht.value')"/>
                                        <xsl:with-param name="value"
                                                        select='format-number(number(assetTotalValue), "##,###.00")'/>
                                    </xsl:call-template>
                                    <xsl:comment>Blank row to display line at end of section</xsl:comment>
                                    <xsl:call-template name="table-row-blank-tall-border-both-grey-thin"/>
                                </fo:table-body>
                            </fo:table>
                        </xsl:if>
                    </xsl:for-each>

                    <xsl:for-each select="freeEstate/estateAssets">
                        <xsl:if test="assetCode='9010'">
                            <fo:table>
                                <fo:table-column column-number="1" column-width="60%"/>
                                <fo:table-column column-number="2" column-width="40%"/>
                                <fo:table-body font-size="12pt">

                                    <xsl:call-template name="table-row-short-vpad-border-top-black">
                                        <xsl:with-param name="label"
                                                        select="scala:getMessagesText($translator, 'pdf.assetDescription.text')"/>
                                        <xsl:with-param name="value" select="assetDescription"/>
                                    </xsl:call-template>

                                    <xsl:call-template name="table-row-money-tall">
                                        <xsl:with-param name="label"
                                                        select="scala:getMessagesText($translator, 'iht.value')"/>
                                        <xsl:with-param name="value"
                                                        select='format-number(number(assetTotalValue), "##,###.00")'/>
                                    </xsl:call-template>
                                    <xsl:comment>Blank row to display line at end of section</xsl:comment>
                                    <xsl:call-template name="table-row-blank-tall-border-both-grey-thin"/>
                                </fo:table-body>
                            </fo:table>
                        </xsl:if>
                    </xsl:for-each>

                    <xsl:for-each select="freeEstate/estateAssets">
                        <xsl:if test="assetCode='9006'">
                            <fo:table>
                                <fo:table-column column-number="1" column-width="60%"/>
                                <fo:table-column column-number="2" column-width="40%"/>
                                <fo:table-body font-size="12pt">

                                    <xsl:call-template name="table-row-short-vpad-border-top-black">
                                        <xsl:with-param name="label"
                                                        select="scala:getMessagesText($translator, 'pdf.assetDescription.text')"/>
                                        <xsl:with-param name="value" select="assetDescription"/>
                                    </xsl:call-template>

                                    <xsl:call-template name="table-row-money-tall">
                                        <xsl:with-param name="label"
                                                        select="scala:getMessagesText($translator, 'iht.value')"/>
                                        <xsl:with-param name="value"
                                                        select='format-number(number(assetTotalValue), "##,###.00")'/>
                                    </xsl:call-template>
                                    <xsl:comment>Blank row to display line at end of section</xsl:comment>
                                    <xsl:call-template name="table-row-blank-tall-border-both-grey-thin"/>
                                </fo:table-body>
                            </fo:table>
                        </xsl:if>
                    </xsl:for-each>


                    <xsl:for-each select="freeEstate/estateAssets">
                        <xsl:if test="assetCode='9021'">
                            <fo:table>
                                <fo:table-column column-number="1" column-width="60%"/>
                                <fo:table-column column-number="2" column-width="40%"/>
                                <fo:table-body font-size="12pt">

                                    <xsl:call-template name="table-row-short-vpad-border-top-black">
                                        <xsl:with-param name="label"
                                                        select="scala:getMessagesText($translator, 'pdf.assetDescription.text')"/>
                                        <xsl:with-param name="value" select="assetDescription"/>
                                    </xsl:call-template>

                                    <xsl:call-template name="table-row-money-tall">
                                        <xsl:with-param name="label"
                                                        select="scala:getMessagesText($translator, 'iht.value')"/>
                                        <xsl:with-param name="value"
                                                        select='format-number(number(assetTotalValue), "##,###.00")'/>
                                    </xsl:call-template>
                                    <xsl:comment>Blank row to display line at end of section</xsl:comment>
                                    <xsl:call-template name="table-row-blank-tall-border-both-grey-thin"/>
                                </fo:table-body>
                            </fo:table>
                        </xsl:if>
                    </xsl:for-each>

                    <xsl:comment>Trust section starts</xsl:comment>
                    <xsl:call-template name="trusts">
                        <xsl:with-param name="value" select="trusts"></xsl:with-param>
                    </xsl:call-template>
                    <xsl:comment>Trust section ends</xsl:comment>

                    <xsl:for-each select="freeEstate/estateAssets">
                        <xsl:if test="assetCode='9098'">
                            <fo:table>
                                <fo:table-column column-number="1" column-width="60%"/>
                                <fo:table-column column-number="2" column-width="40%"/>
                                <fo:table-body font-size="12pt">

                                    <xsl:call-template name="table-row-short-vpad-border-top-black">
                                        <xsl:with-param name="label"
                                                        select="scala:getMessagesText($translator, 'pdf.assetDescription.text')"/>
                                        <xsl:with-param name="value" select="assetDescription"/>
                                    </xsl:call-template>

                                    <xsl:call-template name="table-row-money-tall">
                                        <xsl:with-param name="label"
                                                        select="scala:getMessagesText($translator, 'iht.value')"/>
                                        <xsl:with-param name="value"
                                                        select='format-number(number(assetTotalValue), "##,###.00")'/>
                                    </xsl:call-template>
                                    <xsl:comment>Blank row to display line at end of section</xsl:comment>
                                    <xsl:call-template name="table-row-blank-tall-border-both-grey-thin"/>
                                </fo:table-body>
                            </fo:table>
                        </xsl:if>
                    </xsl:for-each>

                    <xsl:for-each select="freeEstate/estateAssets">
                        <xsl:if test="assetCode='9099'">
                            <fo:table>
                                <fo:table-column column-number="1" column-width="60%"/>
                                <fo:table-column column-number="2" column-width="40%"/>
                                <fo:table-body font-size="12pt">

                                    <xsl:call-template name="table-row-short-vpad-border-top-black">
                                        <xsl:with-param name="label"
                                                        select="scala:getMessagesText($translator, 'pdf.assetDescription.text')"/>
                                        <xsl:with-param name="value" select="assetDescription"/>
                                    </xsl:call-template>

                                    <xsl:call-template name="table-row-money-tall">
                                        <xsl:with-param name="label"
                                                        select="scala:getMessagesText($translator, 'iht.value')"/>
                                        <xsl:with-param name="value"
                                                        select='format-number(number(assetTotalValue), "##,###.00")'/>
                                    </xsl:call-template>
                                    <xsl:comment>Blank row to display line at end of section</xsl:comment>
                                    <xsl:call-template name="table-row-blank-tall-border-both-grey-thin"/>
                                </fo:table-body>
                            </fo:table>
                        </xsl:if>
                    </xsl:for-each>

                    <xsl:for-each select="freeEstate/estateAssets">
                        <xsl:if test="assetCode='9013'">
                            <fo:table>
                                <fo:table-column column-number="1" column-width="60%"/>
                                <fo:table-column column-number="2" column-width="40%"/>
                                <fo:table-body font-size="12pt">

                                    <xsl:call-template name="table-row-short-vpad-border-top-black">
                                        <xsl:with-param name="label"
                                                        select="scala:getMessagesText($translator, 'pdf.assetDescription.text')"/>
                                        <xsl:with-param name="value" select="assetDescription"/>
                                    </xsl:call-template>

                                    <xsl:call-template name="table-row-money-tall">
                                        <xsl:with-param name="label"
                                                        select="scala:getMessagesText($translator, 'iht.value')"/>
                                        <xsl:with-param name="value"
                                                        select='format-number(number(assetTotalValue), "##,###.00")'/>
                                    </xsl:call-template>
                                    <xsl:comment>Blank row to display line at end of section</xsl:comment>
                                    <xsl:call-template name="table-row-blank-tall-border-both-grey-thin"/>
                                </fo:table-body>
                            </fo:table>
                        </xsl:if>
                    </xsl:for-each>

                    <xsl:for-each select="freeEstate/estateAssets">
                        <xsl:if test="assetCode='9015'">
                            <fo:table>
                                <fo:table-column column-number="1" column-width="60%"/>
                                <fo:table-column column-number="2" column-width="40%"/>
                                <fo:table-body font-size="12pt">

                                    <xsl:call-template name="table-row-short-vpad-border-top-black">
                                        <xsl:with-param name="label"
                                                        select="scala:getMessagesText($translator, 'pdf.assetDescription.text')"/>
                                        <xsl:with-param name="value" select="assetDescription"/>
                                    </xsl:call-template>

                                    <xsl:call-template name="table-row-money-tall">
                                        <xsl:with-param name="label"
                                                        select="scala:getMessagesText($translator, 'iht.value')"/>
                                        <xsl:with-param name="value"
                                                        select='format-number(number(assetTotalValue), "##,###.00")'/>
                                    </xsl:call-template>
                                    <xsl:comment>Blank row to display line at end of section</xsl:comment>
                                    <xsl:call-template name="table-row-blank-tall-border-both-grey-thin"/>
                                </fo:table-body>
                            </fo:table>
                        </xsl:if>
                    </xsl:for-each>

                    <fo:block font-family="OpenSans-Bold" font-size="16" font-weight="bold">
                        <fo:table space-before="0.5cm">
                            <fo:table-column column-number="1" column-width="60%"/>
                            <fo:table-column column-number="2" column-width="40%"/>
                            <fo:table-body font-size="12pt">

                                <xsl:call-template name="table-row-money-short-vpad-no-border">
                                    <xsl:with-param name="label"
                                                    select="scala:getMessagesText($translator, 'page.iht.application.assets.overview.total')"/>
                                    <xsl:with-param name="value"
                                                    select='format-number(number($assetsTotal), "##,###.00")'/>
                                </xsl:call-template>

                                <xsl:comment>Blank row to display line at end of section</xsl:comment>
                                <xsl:call-template name="table-row-blank-tall-border-both-grey-thin"/>
                            </fo:table-body>
                        </fo:table>
                    </fo:block>

                </fo:block>
            </xsl:when>
        </xsl:choose>

    </xsl:template>
</xsl:stylesheet>
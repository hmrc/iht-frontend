<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="2.0"
                xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:fo="http://www.w3.org/1999/XSL/Format"
                xmlns:common="http://exslt.org/common"
                xmlns:xalan="http://xml.apache.org" exclude-result-prefixes="common xalan">


<xsl:attribute-set name="root">
    <xsl:attribute name="font-family">OpenSans</xsl:attribute>
    <xsl:attribute name="font-size">12pt</xsl:attribute>
    <xsl:attribute name="font-weight">normal</xsl:attribute>
    <xsl:attribute name="line-height">1.2</xsl:attribute>
    <xsl:attribute name="color">black</xsl:attribute>
</xsl:attribute-set>

<xsl:attribute-set name="copy">
    <xsl:attribute name="font-family">OpenSans</xsl:attribute>
    <xsl:attribute name="space-before">0.5cm</xsl:attribute>
</xsl:attribute-set>

<xsl:attribute-set name="copy--heavy">
    <xsl:attribute name="font-family">OpenSans-Bold</xsl:attribute>
    <xsl:attribute name="font-weight">bold</xsl:attribute>
</xsl:attribute-set>

<xsl:attribute-set name="copy--large">
    <xsl:attribute name="font-family">OpenSans</xsl:attribute>
    <xsl:attribute name="font-size">16pt</xsl:attribute>
    <xsl:attribute name="font-weight">normal</xsl:attribute>
</xsl:attribute-set>

<xsl:attribute-set name="copy--lede">
    <xsl:attribute name="font-family">OpenSans</xsl:attribute>
    <xsl:attribute name="font-size">12pt</xsl:attribute>
    <xsl:attribute name="font-weight">normal</xsl:attribute>
    <xsl:attribute name="line-height">1.4</xsl:attribute>
    <xsl:attribute name="padding-bottom">0.5cm</xsl:attribute>
</xsl:attribute-set>

<xsl:attribute-set name="list">
    <xsl:attribute name="space-before">0.5cm</xsl:attribute>
    <xsl:attribute name="line-height">1.6</xsl:attribute>
</xsl:attribute-set>

<xsl:attribute-set name="list__item">
    <xsl:attribute name="space-before">0.25em</xsl:attribute>
    <xsl:attribute name="space-after">0.25em</xsl:attribute>
    <xsl:attribute name="line-height">1.2</xsl:attribute>
</xsl:attribute-set>

<xsl:attribute-set name="h1">
    <xsl:attribute name="font-family">OpenSans-Bold</xsl:attribute>
    <xsl:attribute name="font-size">24pt</xsl:attribute>
    <xsl:attribute name="font-weight">bold</xsl:attribute>
    <xsl:attribute name="space-before">0.5cm</xsl:attribute>
</xsl:attribute-set>

<xsl:attribute-set name="h2">
    <xsl:attribute name="font-family">OpenSans-Bold</xsl:attribute>
    <xsl:attribute name="font-size">18pt</xsl:attribute>
    <xsl:attribute name="font-weight">bold</xsl:attribute>
    <xsl:attribute name="space-before">1cm</xsl:attribute>
    <xsl:attribute name="space-after">0.5cm</xsl:attribute>
</xsl:attribute-set>

<xsl:attribute-set name="h3">
    <xsl:attribute name="font-family">OpenSans-Bold</xsl:attribute>
    <xsl:attribute name="font-size">14pt</xsl:attribute>
    <xsl:attribute name="font-weight">bold</xsl:attribute>
    <xsl:attribute name="space-before">0.5cm</xsl:attribute>
    <xsl:attribute name="space-after">0.25cm</xsl:attribute>
</xsl:attribute-set>

<xsl:attribute-set name="h4">
    <xsl:attribute name="font-family">OpenSans-Bold</xsl:attribute>
    <xsl:attribute name="font-size">12pt</xsl:attribute>
    <xsl:attribute name="font-weight">bold</xsl:attribute>
    <xsl:attribute name="space-before">0.25cm</xsl:attribute>
    <xsl:attribute name="space-after">0.25cm</xsl:attribute>
</xsl:attribute-set>

<xsl:attribute-set name="row">
    <xsl:attribute name="border-top">1px solid gray</xsl:attribute>
</xsl:attribute-set>
<xsl:attribute-set name="row--heavy">
    <xsl:attribute name="border-top">3px solid black</xsl:attribute>
</xsl:attribute-set>
<xsl:attribute-set name="row--total">
    <xsl:attribute name="border-bottom">3px solid black</xsl:attribute>
</xsl:attribute-set>

<xsl:attribute-set name="cell">
    <xsl:attribute name="padding-top">8px</xsl:attribute>
    <xsl:attribute name="padding-bottom">8px</xsl:attribute>
    <xsl:attribute name="padding-left">0</xsl:attribute>
    <xsl:attribute name="padding-right">10px</xsl:attribute>
    <xsl:attribute name="font-size">12pt</xsl:attribute>
</xsl:attribute-set>

<xsl:attribute-set name="declaration">
    <xsl:attribute name="padding-top">0.25cm</xsl:attribute>
    <xsl:attribute name="padding-bottom">0.5cm</xsl:attribute>
</xsl:attribute-set>

<xsl:attribute-set name="section">
    <xsl:attribute name="padding-bottom">1cm</xsl:attribute>
</xsl:attribute-set>

<xsl:attribute-set name="set-right">
    <xsl:attribute name="text-align">right</xsl:attribute>
</xsl:attribute-set>

<xsl:attribute-set name="page-header">
    <xsl:attribute name="border-bottom">1px solid black</xsl:attribute>
</xsl:attribute-set>

<xsl:attribute-set name="page-footer--title">
    <xsl:attribute name="font-family">OpenSans</xsl:attribute>
    <xsl:attribute name="font-size">8pt</xsl:attribute>
    <xsl:attribute name="font-weight">normal</xsl:attribute>
    <xsl:attribute name="text-align">left</xsl:attribute>
    <xsl:attribute name="space-before">0.5cm</xsl:attribute>
</xsl:attribute-set>

<xsl:attribute-set name="page-footer--page-number">
    <xsl:attribute name="font-family">OpenSans</xsl:attribute>
    <xsl:attribute name="font-size">8pt</xsl:attribute>
    <xsl:attribute name="font-weight">normal</xsl:attribute>
    <xsl:attribute name="text-align">right</xsl:attribute>
    <xsl:attribute name="space-before">0.5cm</xsl:attribute>
</xsl:attribute-set>

    <xsl:attribute-set name="tabular-cell">
        <xsl:attribute name="font-family">OpenSans</xsl:attribute>
        <xsl:attribute name="font-weight">normal</xsl:attribute>
        <xsl:attribute name="padding-top">5px</xsl:attribute>
        <xsl:attribute name="padding-bottom">5px</xsl:attribute>
        <xsl:attribute name="padding-left">5px</xsl:attribute>
        <xsl:attribute name="padding-right">5px</xsl:attribute>
        <xsl:attribute name="font-size">12pt</xsl:attribute>
        <xsl:attribute name="border-top">1px solid lightgray</xsl:attribute>
        <xsl:attribute name="border-right">1px solid lightgray</xsl:attribute>
    </xsl:attribute-set>

    <xsl:attribute-set name="tabular-cell--no-right-border">
        <xsl:attribute name="border-right">none</xsl:attribute>
    </xsl:attribute-set>

    <xsl:attribute-set name="tabular-cell--header">
        <xsl:attribute name="font-family">OpenSans-Bold</xsl:attribute>
        <xsl:attribute name="font-weight">bold</xsl:attribute>
    </xsl:attribute-set>

</xsl:stylesheet>
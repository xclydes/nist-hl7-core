<?xml version="1.0"?>
<!--
    An xsl transformation for reading a Version 2.5 HL7 profile and
    generating a message based on the profile description.

    Author:   Len Gebase, NIST
    Author: Sydney Henrard, NIST

    Profiles look like this:
          ...
      <StaticDef MsgStructId="ADT_A01" ...>
        <Metadata ... />
        <Segment Name="PID" ...>
            <Field dataType="XPN" ...>
              <Component dataType="FN" ...>
                <SubComponent dataType="ST" ... />
              </Component>
              <Component dataType="ST" ... />
                  ...
             </Field>
                ...
         </Segment>
              ....

    The output message for the above might look like:

      <ADT_01>
        <PID>
          <PID.5>
            <XPN.1>
              <FN.1>Smith</FN.1>
            </XPN.1>
            <XPN.2>Joe</XPN.2>
            ...
          </PID.5>
          ...
        <PID>
        ...
      </ADT_01>

    Loosely, the mapping looks like:

    <StaticDef>   ==>   <ADT_A01>     (MsgStructID)
    <Segment>     ==>   <PID>         (Segment Name)
    <Field>       ==>   <PID.5>       (where 5 is the sequence number
                                    obtained from looking up the
                                    ItemNo in the Data Element table)
    <Component>   ==>   <XPN.1>       (Field dataType, nested
                                    elements are numbered sequentially)
    <SubComponent> ==>  <FN.1>        (Component dataType.  When
                                    a primitive dataType is encountered
                                    a value may be generated - usually
                                    looked up in some table we've
                                    populated with data for use when
                                    generating test cases)
    <Component>   ==>   <XPN.2>       (Field dataType, but this time
                                    the Component dataType is
                                    primitive and we generate a
                                    value for it)

    The output msg should include values (that we generate or lookup)
    for elements in the profile whose data type is primitive (dataType =
    ST, NM, ID, DT, SI, TN, or IS).

    Optionally, Segment Group elements (<SegGroup>), which contain
    Segments and may also include nested Segment Groups, may also be
    included in the profile along with Segments.  They appear at the same
    level as Segments.  Segment Group elements are not reflected in the
    output message, except that the cardinality of the group will impact
    the number of times the enclosed Segments are repeated.

    Global Paramters.

Option                   Allowed Values           Default Value
================================================================
valid                    true/false                true
segmentCardinality       0 <= number <= 1           0.5
fieldCardinality         0 <= number <= 1           0.5
RE-Usage                 0 <= number <= 1           0.5
cardErrorMinSeg"        -1 <= number <= 0            0
cardErrorMinField"      -1 <= number <= 0            0
cardErrorMaxSeg"        -1 <= number <= 0            0
cardErrorMaxField"      -1 <= number <= 0            0
RusageError             -1 <= number <= 0            0
XUsageError             -1 <= number <= 0            0
namedSegment							  String									empty
namedField								  String									empty
namedComponent              String									empty
namedSubComponent           String									empty
elementCardinality         number >= 0							empty
elementREinclude           true/false								empty
=================================================================

Note that all R-Usage elements are included unless an error is being generated
and no X-Usage elements are used unless an error is being generated.  C-Usage
and CE-Usage are not currently supported.

- segmentCardinality
- fieldCardinality
- RE-Usage
Parameters that take a non-negative value range generate valid elements in the
output message.  0 = minimum cardinality or don't include element.
1 = maximum cardinality or include all elements.  0.5 means a value roughly
mid-way between the end points, etc.  Only 0, 1, & 0.5 are currently supported.

- cardErrorMinSeg
- cardErrorMinField
- cardErrorMaxSeg
- cardErrorMaxField
- RusageError
- XUsageError
Parameters that take a non-positive value range generate invalid elements in the
output message.  0 = no error, -1 = all errors, and -0.1 = 1 error.  Only
zero or 1 error is currently supported.

valid

  If true, then no invalid elements will be created.
  If false, an invalid msg may be generated.

namedSegment
namedField
namedComponent
namedSubComponent
elementCardinality
elementREinclude

	Identify a specific element, the element's cardinality or if the
  element should be included.

    -->
<xsl:stylesheet version="2.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns="urn:hl7-org:v2xml" xmlns:generation="http://www.nist.gov/healthcare/generation/message">
	<!-- Use this file to get sequence number for fields -->
	<xsl:param name="sequencenumbers"/>
	<xsl:key name="generation:sequenceNumberKey" match="generation:FieldElement" use="@FieldName"/>
	<!-- options: -->
	<xsl:param name="valid" select="'false'"/>
	<xsl:param name="starValue" select="3"/>
	<xsl:param name="segmentCardinality" select="0.5"/>
	<xsl:param name="fieldCardinality" select="0.5"/>
	<xsl:param name="RE_Usage" select="0.5"/>
	<xsl:param name="minSegCardError" select="'false'"/>
	<xsl:param name="maxSegCardError" select="'false'"/>
	<xsl:param name="minFieldCardError" select="'false'"/>
	<xsl:param name="maxFieldCardError" select="'false'"/>
	<xsl:param name="RUsageError" select="'false'"/>
	<xsl:param name="XUsageError" select="'false'"/>
	<xsl:param name="namedSegment" select="''"/>
	<xsl:param name="namedField" select="''"/>
	<xsl:param name="namedComponent" select="''"/>
	<xsl:param name="namedSubComponent" select="''"/>
	<!--	<xsl:param name="positionField" select="''"/>
	<xsl:param name="positionComponent" select="''"/>
	<xsl:param name="positionSubComponent" select="''"/> -->
	<xsl:param name="elementCardinality" select="''"/>
	<xsl:param name="elementREinclude" select="''"/>
	<xsl:output method="xml" indent="yes"/>
	<xsl:strip-space elements="*"/>
	<xsl:variable name="newline">
		<xsl:text>
  </xsl:text>
	</xsl:variable>
	<!-- ............................................................. -->
	<!-- ...................  ROOT  .................................. -->
	<xsl:template match="/">
		<xsl:apply-templates select="HL7v2xConformanceProfile"/>
		<xsl:apply-templates select="Specification"/>
	</xsl:template>
	<!-- hl7 uses 'HL7v2xConformanceProfile', but the VA uses 'Specification'  -->
	<xsl:template match="HL7v2xConformanceProfile | Specification">
		<xsl:apply-templates select="HL7v2xStaticDef"/>
		<xsl:apply-templates select="Message"/>
	</xsl:template>
	<!-- ......................................................
      processing of static def (or Message) starts here.
    ..................................................... -->
	<xsl:template match="HL7v2xStaticDef | Message">
		<xsl:variable name="msgStructID" select="@MsgStructID"/>
		<!-- Along with meta data that's being ignored (at least for now),
        the static def is a sequence of Segment and SegGroup elements.
        These can be intermixed in any order.  We invoke apply-templates
        without the select attribute so that the specific template that gets
        invoked will depend on what's seen in the input stream.
        The msgStructId defines the root element of the output msg. -->
		<xsl:element name="{$msgStructID}">
			<xsl:apply-templates>
				<xsl:with-param name="msgStructID" select="$msgStructID"/>
				<xsl:with-param name="loopCounter" select="1"/>
			</xsl:apply-templates>
		</xsl:element>
		<xsl:value-of select="$newline"/>
	</xsl:template>
	<!-- ............................................................. -->
	<xsl:template match="SegGroup" name="segGroup">
		<xsl:param name="msgStructID"/>
		<xsl:param name="loopCounter" select="1"/>
		<xsl:param name="init" select="true()"/>
		<xsl:param name="reps" select="0"/>
		<xsl:param name="segmentGroups"/>
		<xsl:variable name="grpName" select="@Name"/>
		<xsl:variable name="grpUsage" select="@Usage"/>
		<!-- there are no options for controlling groups, so this has to be special caseed -->
		<xsl:variable name="includeGrp">
			<xsl:choose>
				<xsl:when test="$grpUsage = 'R'">
					<xsl:value-of select="'true'"/>
				</xsl:when>
				<xsl:when test="$grpUsage = 'RE'">
					<xsl:choose>
						<xsl:when test="position() mod 3 = 0">
							<xsl:value-of select="'false'"/>
						</xsl:when>
						<xsl:otherwise>
							<xsl:value-of select="'true'"/>
						</xsl:otherwise>
					</xsl:choose>
				</xsl:when>
				<xsl:otherwise>
					<xsl:value-of select="'false'"/>
				</xsl:otherwise>
			</xsl:choose>
		</xsl:variable>
		<!-- if min isn't a number (should be though), set = 0 -->
		<xsl:variable name="grpMin">
			<xsl:choose>
				<xsl:when test="number(@Min)">
					<xsl:value-of select="@Min"/>
				</xsl:when>
				<xsl:otherwise>
					<xsl:value-of select="0"/>
				</xsl:otherwise>
			</xsl:choose>
		</xsl:variable>
		<!-- max might = *, we'll set to min+3 in this case -->
		<xsl:variable name="grpMax">
			<xsl:choose>
				<xsl:when test="number(@Max)">
					<xsl:value-of select="@Max"/>
				</xsl:when>
				<xsl:otherwise>
					<xsl:value-of select="$grpMin + 3"/>
				</xsl:otherwise>
			</xsl:choose>
		</xsl:variable>
		<!-- set the number of times the group will repeat.
      (there's no option for controlling group repetition.) -->
		<xsl:variable name="grpReps">
			<xsl:choose>
				<xsl:when test="$init">
					<!-- not recursing  -->
					<xsl:choose>
						<xsl:when test="number($grpMin) > number($grpMax)">
							<xsl:value-of select="0"/>
							<!-- max or min is set incorrectly -->
						</xsl:when>
						<xsl:when test="$grpMax = $grpMin">
							<xsl:value-of select="$grpMin"/>
						</xsl:when>
						<xsl:otherwise>
							<!-- grpMax > grpMin -->
							<xsl:choose>
								<xsl:when test="position() mod 7 = 0">
									<xsl:value-of select="$grpMin"/>
								</xsl:when>
								<xsl:when test="position() mod 3 = 0">
									<xsl:value-of select="$grpMax"/>
								</xsl:when>
								<xsl:otherwise>
									<xsl:value-of select="$grpMin + floor(($grpMax - $grpMin) div 2)"/>
								</xsl:otherwise>
							</xsl:choose>
						</xsl:otherwise>
					</xsl:choose>
				</xsl:when>
				<xsl:otherwise>
					<!-- value's already set if recursing -->
					<xsl:value-of select="$reps"/>
				</xsl:otherwise>
			</xsl:choose>
		</xsl:variable>
		<!-- process Segments or nested Segment Groups -->
		<xsl:if test="$includeGrp = 'true'">
			<xsl:if test="$grpReps &gt; 0">
				<xsl:variable name="rootName" select="//HL7v2xStaticDef/@MsgStructID"/>
				<xsl:element name="{$rootName}.{$grpName}">
					<!-- Annotate for the generation -->
					<xsl:attribute name="Usage"><xsl:value-of select="@OriginalUsage"/></xsl:attribute>
					<xsl:attribute name="Min"><xsl:value-of select="@OriginalMin"/></xsl:attribute>
					<xsl:apply-templates>
						<xsl:with-param name="loopCounter" select="1"/>
						<xsl:with-param name="segmentGroups" select="concat($segmentGroups, '/', $grpName)"/>
					</xsl:apply-templates>
				</xsl:element>
			</xsl:if>
			<!-- recurse through this template if counter < reps -->
			<xsl:if test="$loopCounter &lt; $grpReps">
				<xsl:call-template name="segGroup">
					<xsl:with-param name="loopCounter" select="$loopCounter+1"/>
					<xsl:with-param name="msgStructID" select="$msgStructID"/>
					<xsl:with-param name="init" select="false()"/>
					<xsl:with-param name="reps" select="$grpReps"/>
					<xsl:with-param name="segmentGroups" select="$segmentGroups"/>
				</xsl:call-template>
			</xsl:if>
		</xsl:if>
	</xsl:template>
	<!-- ............................................................. -->
	<xsl:template match="Segment" name="segment">
		<xsl:param name="loopCounter" select="1"/>
		<xsl:param name="init" select="true()"/>
		<xsl:param name="reps" select="0"/>
		<xsl:param name="segmentGroups"/>
		<xsl:variable name="segName" select="@Name"/>
		<!-- VA uses 'Optionality' in place of 'Usage' attribute -->
		<xsl:variable name="segUsage">
			<xsl:choose>
				<xsl:when test="@Usage!=''">
					<xsl:value-of select="@Usage"/>
				</xsl:when>
				<xsl:otherwise>
					<xsl:value-of select="@Optionality"/>
				</xsl:otherwise>
			</xsl:choose>
		</xsl:variable>
		<!-- see if the element will be included -->
		<xsl:variable name="includeSegment">
			<xsl:choose>
				<xsl:when test="$init">
					<xsl:call-template name="includeElement">
						<xsl:with-param name="usage" select="$segUsage"/>
						<xsl:with-param name="segmentName" select="$segName"/>
					</xsl:call-template>
				</xsl:when>
				<xsl:otherwise>
					<!-- recursing, just set to true -->
					<xsl:value-of select="'true'"/>
				</xsl:otherwise>
			</xsl:choose>
		</xsl:variable>
		<!-- set cardinality -->
		<xsl:variable name="cardinality">
			<xsl:call-template name="setCardinalityOption">
				<xsl:with-param name="segName" select="$segName"/>
				<xsl:with-param name="cardinality" select="$segmentCardinality"/>
				<xsl:with-param name="errorMax" select="$maxSegCardError"/>
				<xsl:with-param name="errorMin" select="$minSegCardError"/>
			</xsl:call-template>
		</xsl:variable>
		<!-- if min isn't a number (should be though), set = 0 -->
		<xsl:variable name="segMin">
			<xsl:choose>
				<xsl:when test="number(@Min)">
					<xsl:value-of select="@Min"/>
				</xsl:when>
				<xsl:otherwise>
					<xsl:value-of select="0"/>
				</xsl:otherwise>
			</xsl:choose>
		</xsl:variable>
		<!-- max might = *, we'll set to min+3 in this case -->
		<xsl:variable name="segMax">
			<xsl:choose>
				<xsl:when test="number(@Max)">
					<xsl:value-of select="@Max"/>
				</xsl:when>
				<xsl:otherwise>
					<xsl:value-of select="$segMin + $starValue"/>
				</xsl:otherwise>
			</xsl:choose>
		</xsl:variable>
		<!-- set the number of times this seg will repeat -->
		<xsl:variable name="segReps">
			<xsl:choose>
				<xsl:when test="$init">
					<!-- not recursing  -->
					<xsl:choose>
						<xsl:when test="number($segMin) > number($segMax)">
							<xsl:value-of select="0"/>
							<!-- max or min is set incorrectly -->
						</xsl:when>
						<!-- set to specific number of repetitions (see below) -->
						<xsl:when test="$cardinality &gt; 10 or $cardinality = 10">
							<xsl:choose>
								<xsl:when test="$cardinality - 10 &gt; $segMax">
									<xsl:value-of select="$segMax"/>
								</xsl:when>
								<xsl:when test="$cardinality - 10 &lt; $segMin ">
									<xsl:value-of select="$segMin"/>
								</xsl:when>
								<xsl:otherwise>
									<xsl:value-of select="$cardinality - 10"/>
								</xsl:otherwise>
							</xsl:choose>
						</xsl:when>
						<xsl:when test="$cardinality=1">
							<xsl:value-of select="$segMax"/>
						</xsl:when>
						<xsl:when test="$cardinality=0">
							<xsl:value-of select="$segMin"/>
						</xsl:when>
						<xsl:when test="$cardinality &gt; 0 and $cardinality &lt; 1">
							<xsl:value-of select="$segMax"/>
						</xsl:when>
						<!-- generate invalid msg if card < 0 -->
						<xsl:when test="$cardinality = -1">
							<xsl:value-of select="$segMax + 1"/>
						</xsl:when>
						<xsl:when test="$cardinality = -0.1">
							<xsl:choose>
								<xsl:when test="$segMin > 0">
									<xsl:value-of select="$segMin - 1"/>
								</xsl:when>
								<xsl:otherwise>
									<xsl:value-of select="$segMin"/>
								</xsl:otherwise>
							</xsl:choose>
						</xsl:when>
						<xsl:otherwise>
							<!-- generate a mix of valid msgs -->
							<xsl:choose>
								<xsl:when test="((position() + (0 mod 3)) mod 3) = 0">
									<xsl:value-of select="$segMax"/>
								</xsl:when>
								<xsl:when test="((position() + (0 mod 3)) mod 2) = 0">
									<xsl:value-of select="$segMin"/>
								</xsl:when>
								<xsl:otherwise>
									<xsl:value-of select="$segMin + floor(($segMax - $segMin) div 2)"/>
								</xsl:otherwise>
							</xsl:choose>
						</xsl:otherwise>
					</xsl:choose>
				</xsl:when>
				<xsl:otherwise>
					<!-- value's already set if recursing -->
					<xsl:value-of select="$reps"/>
				</xsl:otherwise>
			</xsl:choose>
		</xsl:variable>
		<!--  each segment is made up of a sequence of fields.
      the names of the children of the output msg's root element are
      taken from the seg names. -->
		<xsl:if test="$includeSegment = 'true'">
			<xsl:if test="$segReps &gt; 0">
				<xsl:element name="{$segName}">
					<!-- Annotate for the generation -->
					<xsl:attribute name="Usage"><xsl:value-of select="@OriginalUsage"/></xsl:attribute>
					<xsl:attribute name="Min"><xsl:value-of select="@OriginalMin"/></xsl:attribute>
					<xsl:apply-templates select="Field">
						<xsl:with-param name="segName" select="$segName"/>
						<xsl:with-param name="loopCounter" select="1"/>
						<xsl:with-param name="setIDnumber" select="$loopCounter"/>
						<xsl:with-param name="segmentGroups" select="$segmentGroups"/>
						<!-- value of SET ID field -->
					</xsl:apply-templates>
				</xsl:element>
			</xsl:if>
			<!-- call this template again if the Segment's being repeated -->
			<xsl:if test="$loopCounter &lt; $segReps">
				<xsl:call-template name="segment">
					<xsl:with-param name="loopCounter" select="$loopCounter+1"/>
					<xsl:with-param name="init" select="false()"/>
					<xsl:with-param name="reps" select="$segReps"/>
					<xsl:with-param name="segmentGroups" select="$segmentGroups"/>
				</xsl:call-template>
			</xsl:if>
		</xsl:if>
	</xsl:template>
	<!-- ............................................................. -->
	<xsl:template match="Field" name="field">
		<xsl:param name="loopCounter" select="1"/>
		<xsl:param name="segName"/>
		<xsl:param name="init" select="true()"/>
		<xsl:param name="reps" select="0"/>
		<xsl:param name="segmentGroups"/>
		<!-- field reps -->
		<xsl:variable name="fieldName" select="@Name"/>
		<xsl:variable name="fieldDataType" select="@Datatype"/>
		<xsl:variable name="tableId" select="@Table"/>
		<xsl:variable name="constantValue" select="@ConstantValue"/>
		<!-- VA uses 'Optionality' in place of 'Usage' attribute -->
		<xsl:variable name="fieldUsage">
			<xsl:choose>
				<xsl:when test="@Usage!=''">
					<xsl:value-of select="@Usage"/>
				</xsl:when>
				<xsl:otherwise>
					<xsl:value-of select="@Optionality"/>
				</xsl:otherwise>
			</xsl:choose>
		</xsl:variable>
		<!-- if we're not recursing, call includeElement to determine if this
      field will be included based on the field's usage setting and the
      values of the usage options, otherwise include.
      -->
		<xsl:variable name="includeField">
			<xsl:choose>
				<xsl:when test="$init">
					<xsl:call-template name="includeElement">
						<xsl:with-param name="usage" select="$fieldUsage"/>
						<xsl:with-param name="segmentName" select="$segName"/>
						<xsl:with-param name="fieldName" select="$fieldName"/>
						<!--<xsl:with-param name="fieldPosition" select="$fieldSeqNum"/>-->
					</xsl:call-template>
				</xsl:when>
				<xsl:otherwise>
					<xsl:value-of select="'true'"/>
				</xsl:otherwise>
			</xsl:choose>
		</xsl:variable>
		<!-- set cardinality, same strategy as for segment -->
		<xsl:variable name="cardinality">
			<xsl:call-template name="setCardinalityOption">
				<xsl:with-param name="segName" select="$segName"/>
				<xsl:with-param name="fieldName" select="$fieldName"/>
				<xsl:with-param name="cardinality" select="$fieldCardinality"/>
				<xsl:with-param name="errorMax" select="$maxFieldCardError"/>
				<xsl:with-param name="errorMin" select="$minFieldCardError"/>
			</xsl:call-template>
		</xsl:variable>
		<!-- if min isn't a number (should be though), set = 0 -->
		<xsl:variable name="fieldMin">
			<xsl:choose>
				<xsl:when test="number(@Min) and $fieldUsage = 'X'">
					<xsl:value-of select="1"/>
				</xsl:when>
				<xsl:when test="number(@Min)">
					<xsl:value-of select="@Min"/>
				</xsl:when>
				<xsl:otherwise>
					<xsl:value-of select="0"/>
				</xsl:otherwise>
			</xsl:choose>
		</xsl:variable>
		<!-- max might = *, we'll set to min+3 in this case -->
		<xsl:variable name="fieldMax">
			<xsl:choose>
				<xsl:when test="number(@Max)">
					<xsl:value-of select="@Max"/>
				</xsl:when>
				<xsl:otherwise>
					<xsl:value-of select="number($fieldMin) + number($starValue)"/>
				</xsl:otherwise>
			</xsl:choose>
		</xsl:variable>
		<!-- set the number of times this field will repeat -->
		<xsl:variable name="fieldReps">
			<xsl:choose>
				<xsl:when test="$init">
					<!-- not recursing  -->
					<xsl:choose>
						<xsl:when test="number($fieldMin) > number($fieldMax)">
							<xsl:value-of select="0"/>
							<!-- max or min is set incorrectly -->
						</xsl:when>
						<!-- set to specific number of repetitions (see below) -->
						<xsl:when test="$cardinality &gt; 10 or $cardinality = 10">
							<xsl:choose>
								<xsl:when test="$cardinality - 10 &gt; $fieldMax">
									<xsl:value-of select="$fieldMax"/>
								</xsl:when>
								<xsl:when test="$cardinality - 10 &lt; $fieldMin ">
									<xsl:value-of select="$fieldMin"/>
								</xsl:when>
								<xsl:otherwise>
									<xsl:value-of select="$cardinality - 10"/>
								</xsl:otherwise>
							</xsl:choose>
						</xsl:when>
						<xsl:when test="$cardinality=1">
							<xsl:value-of select="$fieldMax"/>
						</xsl:when>
						<xsl:when test="$cardinality=0">
							<xsl:value-of select="$fieldMin"/>
						</xsl:when>
						<!-- generate invalid msg if card < 0 -->
						<xsl:when test="$cardinality = -1">
							<xsl:value-of select="$fieldMax + 1"/>
						</xsl:when>
						<xsl:when test="$cardinality = -0.1">
							<xsl:choose>
								<xsl:when test="$fieldMin > 0">
									<xsl:value-of select="$fieldMin - 1"/>
								</xsl:when>
								<xsl:otherwise>
									<xsl:value-of select="$fieldMin"/>
								</xsl:otherwise>
							</xsl:choose>
						</xsl:when>
						<xsl:otherwise>
							<!-- generate a mix of valid msgs -->
							<xsl:choose>
								<xsl:when test="((position() + (0 mod 4)) mod 4) = 0">
									<xsl:value-of select="$fieldMax"/>
								</xsl:when>
								<xsl:when test="((position() + (0 mod 4)) mod 3) = 0">
									<xsl:value-of select="$fieldMin"/>
								</xsl:when>
								<xsl:otherwise>
									<xsl:value-of select="$fieldMin + floor(($fieldMax - $fieldMin) div 2)"/>
								</xsl:otherwise>
							</xsl:choose>
						</xsl:otherwise>
					</xsl:choose>
				</xsl:when>
				<xsl:otherwise>
					<xsl:value-of select="$reps"/>
				</xsl:otherwise>
			</xsl:choose>
		</xsl:variable>
		<xsl:variable name="fieldSeqNum">
			<xsl:for-each select="document($sequencenumbers)">
				<xsl:for-each select="key('generation:sequenceNumberKey', $fieldName)">
					<xsl:if test="@SegmentName=substring($segName, 1, 3)">
						<xsl:value-of select="@Position"/>
					</xsl:if>
				</xsl:for-each>
			</xsl:for-each>
		</xsl:variable>
		<xsl:if test="$includeField='true'">
			<xsl:if test="$fieldReps &gt; 0">
				<xsl:variable name="primitive">
					<xsl:call-template name="isPrimitive">
						<xsl:with-param name="dataType" select="$fieldDataType"/>
					</xsl:call-template>
				</xsl:variable>
				<!--  the output msg elements built from the Fields are named based on
          the parent Segment name and the field's sequence number. -->
				<xsl:element name="{$segName}.{$fieldSeqNum}">
					<!-- Annotate for the generation -->
					<xsl:attribute name="Usage"><xsl:value-of select="@OriginalUsage"/></xsl:attribute>
					<xsl:attribute name="Min"><xsl:value-of select="@OriginalMin"/></xsl:attribute>
					<xsl:if test="$primitive = 'true'">
						<xsl:choose>
							<xsl:when test="$constantValue!=''">
								<xsl:value-of select="$constantValue"/>
							</xsl:when>
							<xsl:otherwise>
								<xsl:attribute name="Populate"/>
								<!-- <xsl:value-of select="$fieldDataType"/>:<xsl:value-of select="$segmentGroups"/>:<xsl:value-of select="$segName"/>:<xsl:value-of select="$fieldName"/>:::<xsl:value-of select="$tableId"/> -->
								<xsl:value-of select="$segmentGroups"/>:<xsl:value-of select="$segName"/>:<xsl:value-of select="$fieldName"/>::
							</xsl:otherwise>
						</xsl:choose>
					</xsl:if>
					<!-- Datatype varies -->
					<xsl:if test="$fieldDataType = 'varies'">
						<xsl:attribute name="Varies"/>
						<FakeComponent.1 Populate="">
							<xsl:value-of select="$segmentGroups"/>:<xsl:value-of select="$segName"/>:<xsl:value-of select="$fieldName"/>::
						</FakeComponent.1>
						<FakeComponent.2 Populate="">
							<xsl:value-of select="$segmentGroups"/>:<xsl:value-of select="$segName"/>:<xsl:value-of select="$fieldName"/>::
						</FakeComponent.2>
						<FakeComponent.3 Populate="">
							<xsl:value-of select="$segmentGroups"/>:<xsl:value-of select="$segName"/>:<xsl:value-of select="$fieldName"/>::
						</FakeComponent.3>
					</xsl:if>
					<xsl:apply-templates select="Component">
						<xsl:with-param name="segName" select="$segName"/>
						<xsl:with-param name="fieldDataType" select="$fieldDataType"/>
						<xsl:with-param name="fieldName" select="$fieldName"/>
						<xsl:with-param name="fieldPosition" select="$fieldSeqNum"/>
						<xsl:with-param name="segmentGroups" select="$segmentGroups"/>
					</xsl:apply-templates>
				</xsl:element>
			</xsl:if>
			<!-- call this template again if the Field's being repeated
      fieldReps is set when this template is initially invoked
      (not recursing) on subsequent calls (from here) fieldReps
      is set to the value of 'reps', so it doesn't change -->
			<xsl:if test="$loopCounter &lt; $fieldReps">
				<xsl:call-template name="field">
					<xsl:with-param name="loopCounter" select="$loopCounter+1"/>
					<xsl:with-param name="segName" select="$segName"/>
					<xsl:with-param name="init" select="false()"/>
					<xsl:with-param name="reps" select="$fieldReps"/>
					<xsl:with-param name="segmentGroups" select="$segmentGroups"/>
				</xsl:call-template>
			</xsl:if>
		</xsl:if>
	</xsl:template>
	<!-- ............................................................. -->
	<xsl:template match="Component">
		<xsl:param name="segName"/>
		<xsl:param name="fieldDataType"/>
		<xsl:param name="fieldName"/>
		<xsl:param name="segmentGroups"/>
		<!--<xsl:param name="fieldPosition"/>-->
		<xsl:variable name="compName" select="@Name"/>
		<xsl:variable name="compDataType" select="@Datatype"/>
		<xsl:variable name="tableId" select="@Table"/>
		<xsl:variable name="constantValue" select="@ConstantValue"/>
		<!-- VA uses 'Optionality' in place of 'Usage' attribute -->
		<xsl:variable name="compUsage">
			<xsl:choose>
				<xsl:when test="@Usage!=''">
					<xsl:value-of select="@Usage"/>
				</xsl:when>
				<xsl:otherwise>
					<xsl:value-of select="@Optionality"/>
				</xsl:otherwise>
			</xsl:choose>
		</xsl:variable>
		<xsl:variable name="compCount">
			<xsl:number count="Component" level="single"/>
		</xsl:variable>
		<!-- set includeComponent (we don't recurse thru this template) -->
		<xsl:variable name="includeComponent">
			<xsl:call-template name="includeElement">
				<xsl:with-param name="usage" select="$compUsage"/>
				<xsl:with-param name="segmentName" select="$segName"/>
				<xsl:with-param name="fieldName" select="$fieldName"/>
				<xsl:with-param name="componentName" select="$compName"/>
				<!--<xsl:with-param name="fieldPosition" select="$fieldPosition"/>
				<xsl:with-param name="componentPosition" select="$compCount"/> -->
			</xsl:call-template>
		</xsl:variable>
		<!-- the component elements in the output msg are numbered
      sequentially and named using the parent field data type. -->
		<xsl:if test="$includeComponent = 'true'">
			<xsl:element name="{$fieldDataType}.{$compCount}">
				<!-- Annotate for the generation -->
				<xsl:attribute name="Usage"><xsl:value-of select="@OriginalUsage"/></xsl:attribute>
				<xsl:attribute name="Min">1</xsl:attribute>
				<xsl:variable name="primitive">
					<xsl:call-template name="isPrimitive">
						<xsl:with-param name="dataType" select="$compDataType"/>
					</xsl:call-template>
				</xsl:variable>
				<xsl:if test="$primitive = 'true'">
					<xsl:choose>
						<xsl:when test="$constantValue!=''">
							<xsl:value-of select="$constantValue"/>
						</xsl:when>
						<xsl:otherwise>
							<xsl:attribute name="Populate"/>
							<!-- <xsl:value-of select="$compDataType"/>:<xsl:value-of select="$segName"/>:<xsl:value-of select="$fieldName"/>:<xsl:value-of select="$compName"/>::<xsl:value-of select="$tableId"/> -->
							<xsl:value-of select="$segmentGroups"/>:<xsl:value-of select="$segName"/>:<xsl:value-of select="$fieldName"/>:<xsl:value-of select="$compName"/>:						
						</xsl:otherwise>
					</xsl:choose>
				</xsl:if>
				<xsl:apply-templates select="SubComponent">
					<xsl:with-param name="compDataType" select="$compDataType"/>
					<xsl:with-param name="compName" select="$compName"/>
					<!--<xsl:with-param name="compPosition" select="$compCount"/>-->
					<xsl:with-param name="fieldName" select="$fieldName"/>
					<!--<xsl:with-param name="fieldPosition" select="$fieldPosition"/>-->
					<xsl:with-param name="segName" select="$segName"/>
					<xsl:with-param name="segmentGroups" select="$segmentGroups"/>
				</xsl:apply-templates>
			</xsl:element>
		</xsl:if>
	</xsl:template>
	<!-- ............................................................. -->
	<xsl:template match="SubComponent">
		<xsl:param name="compDataType"/>
		<xsl:param name="compName"/>
		<!--<xsl:param name="compPosition"/>-->
		<xsl:param name="fieldName"/>
		<!--<xsl:param name="fieldPosition"/>-->
		<xsl:param name="segName"/>
		<xsl:param name="segmentGroups"/>
		<xsl:variable name="subName" select="@Name"/>
		<xsl:variable name="subDataType" select="@Datatype"/>
		<xsl:variable name="tableId" select="@Table"/>
		<xsl:variable name="constantValue" select="@ConstantValue"/>
		<!-- VA uses 'Optionality' in place of 'Usage' attribute -->
		<xsl:variable name="subUsage">
			<xsl:choose>
				<xsl:when test="@Usage!=''">
					<xsl:value-of select="@Usage"/>
				</xsl:when>
				<xsl:otherwise>
					<xsl:value-of select="@Optionality"/>
				</xsl:otherwise>
			</xsl:choose>
		</xsl:variable>
		<xsl:variable name="subCount">
			<xsl:number count="SubComponent" level="single"/>
		</xsl:variable>
		<!-- set includeSubcomponent (we don't recurse thru this template) -->
		<xsl:variable name="includeSubcomponent">
			<xsl:call-template name="includeElement">
				<xsl:with-param name="usage" select="$subUsage"/>
				<xsl:with-param name="segmentName" select="$segName"/>
				<xsl:with-param name="fieldName" select="$fieldName"/>
				<xsl:with-param name="componentName" select="$compName"/>
				<xsl:with-param name="subComponentName" select="$subName"/>
				<!--<xsl:with-param name="fieldPosition" select="$fieldPosition"/>
				<xsl:with-param name="componentPosition" select="$compPosition"/>
				<xsl:with-param name="subComponentPosition" select="$subCount"/>-->
			</xsl:call-template>
		</xsl:variable>
		<!-- sub component elements in the output msg are named based upon
      the parent component's dataType and a sequential count -->
		<xsl:if test="$includeSubcomponent = 'true'">
			<xsl:element name="{$compDataType}.{$subCount}">
				<!-- Annotate for the generation -->
				<xsl:attribute name="Usage"><xsl:value-of select="@OriginalUsage"/></xsl:attribute>
				<xsl:attribute name="Min">1</xsl:attribute>
				<xsl:choose>
					<xsl:when test="$constantValue!=''">
						<xsl:value-of select="$constantValue"/>
					</xsl:when>
					<xsl:otherwise>
						<xsl:attribute name="Populate"/>
						<!--  <xsl:value-of select="$subDataType"/>:<xsl:value-of select="$segName"/>:<xsl:value-of select="$fieldName"/>:<xsl:value-of select="$compName"/>:<xsl:value-of select="$subName"/>:<xsl:value-of select="$tableId"/> -->
						<xsl:value-of select="$segmentGroups"/>:<xsl:value-of select="$segName"/>:<xsl:value-of select="$fieldName"/>:<xsl:value-of select="$compName"/>:<xsl:value-of select="$subName"/>
					</xsl:otherwise>
				</xsl:choose>
			</xsl:element>
		</xsl:if>
	</xsl:template>
	<!-- ............................................................. -->
	<!-- check to see if element should be included in output based on
    the element's Usage setting  -->
	<xsl:template name="includeElement">
		<xsl:param name="usage"/>
		<xsl:param name="segmentName" select="''"/>
		<xsl:param name="fieldName" select="''"/>
		<xsl:param name="componentName" select="''"/>
		<xsl:param name="subComponentName" select="''"/>
		
		<xsl:if test="$fieldName = 'Reserved for harmonization with V2.6 1'">
		  <xsl:element name="lol"><xsl:value-of select="$usage"/></xsl:element>
		</xsl:if>
		
		<!--<xsl:param name="fieldPosition" select="''"/>
		<xsl:param name="componentPosition" select="''"/>
		<xsl:param name="subComponentPosition" select="''"/>-->
		<xsl:choose>
			<!-- all R-Usage elements are included unless an error is being generated -->
			<xsl:when test="$usage = 'R'">
				<xsl:choose>
					<xsl:when test="number($RUsageError) &lt; 0">
						<!-- error -->
						<xsl:choose>
							<xsl:when test="$segmentName = $namedSegment
              		and $fieldName = $namedField
                  and $componentName = $namedComponent
                  and $subComponentName = $namedSubComponent">
								<xsl:value-of select="'false'"/>
							</xsl:when>
							<!--<xsl:when test="$segmentName = $namedSegment
              		and $fieldPosition = $positionField
                  and $componentPosition = $positionComponent
                  and $subComponentPosition = $positionSubComponent">
								<xsl:value-of select="'false'"/>
							</xsl:when> -->
							<xsl:otherwise>
								<!-- include element -->
								<xsl:value-of select="'true'"/>
							</xsl:otherwise>
						</xsl:choose>
					</xsl:when>
					<xsl:otherwise>
						<!-- valid -->
						<xsl:value-of select="'true'"/>
					</xsl:otherwise>
				</xsl:choose>
			</xsl:when>
			<!-- end usage=R -->
			<!-- no X-Usage elements are included unless an error is being generated -->
			<xsl:when test="$usage = 'X'">
				<xsl:choose>
					<xsl:when test="number($XUsageError) &lt; 0">
						<xsl:choose>
							<xsl:when test="$segmentName = $namedSegment
              		and $fieldName = $namedField
                  and $componentName = $namedComponent
                  and $subComponentName = $namedSubComponent">
								<xsl:value-of select="'true'"/>
							</xsl:when>
							<!--<xsl:when test="$segmentName = $namedSegment
              		and $fieldPosition = $positionField
                  and $componentPosition = $positionComponent
                  and $subComponentPosition = $positionSubComponent">
								<xsl:value-of select="'true'"/>
							</xsl:when> -->
							<xsl:otherwise>
								<!-- don't include element -->
								<xsl:value-of select="'false'"/>
							</xsl:otherwise>
						</xsl:choose>
					</xsl:when>
					<xsl:otherwise>
							<xsl:if test="$fieldName = 'Reserved for harmonization with V2.6 1'">
		  <xsl:element name="lol"><xsl:value-of select="$usage"/></xsl:element>
		</xsl:if>
					
						<xsl:value-of select="'false'"/>
					</xsl:otherwise>
				</xsl:choose>
			</xsl:when>
			<!-- end usage=X -->
			<xsl:when test="$usage = 'RE'">
				<xsl:choose>
					<!-- if elementREinclude isn't empty and this is the designated
          	element, then its value determines if the element is
            included or omitted -->
					<xsl:when test="$elementREinclude != ''
          			and $segmentName = $namedSegment
              	and $fieldName = $namedField
                and $componentName = $namedComponent
                and $subComponentName = $namedSubComponent">
						<xsl:value-of select="$elementREinclude"/>
					</xsl:when>
					<!--<xsl:when test="$elementREinclude != ''
          			and $segmentName = $namedSegment
              		and $fieldPosition = $positionField
                  and $componentPosition = $positionComponent
                  and $subComponentPosition = $positionSubComponent">
						<xsl:value-of select="$elementREinclude"/>
					</xsl:when> -->
					<xsl:when test="$elementREinclude = 'true'
          			and (
          			(name() = 'Segment' and $segmentName = $namedSegment and $fieldName = '' and $componentName = '' and $subComponentName = '')
                    or
          			(name() = 'Field' and $segmentName = $namedSegment and $fieldName = $namedField and $componentName = '' and $subComponentName = '')
                    or
          			(name() = 'Component' and $segmentName = $namedSegment and $fieldName = $namedField and $componentName = $namedComponent and $subComponentName = '')
                    or
          			(name() = 'SubComponent' and $segmentName = $namedSegment and $fieldName = $namedField and $componentName = $namedComponent and $subComponentName = $namedSubComponent)
          			)
          			">
						<xsl:value-of select="'true'"/>
					</xsl:when>
					<!--<xsl:when test="$elementREinclude = 'true'
          			and (
          			(name() = 'Segment' and $segmentName = $positionSegment and $fieldPosition = '' and $componentPosition = '' and $subComponentPosition = '')
                    or
          			(name() = 'Field' and $segmentName = $positionSegment and $fieldPosition = $positionField and $componentPosition = '' and $subComponentPosition = '')
                    or
          			(name() = 'Component' and $segmentName = $positionSegment and $fieldPosition = $positionField and $componentPosition = $positionComponent and $subComponentPosition = '')
                    or
          			(name() = 'SubComponent' and $segmentName = $positionSegment and $fieldPosition = $positionField and $componentPosition = $positionComponent and $subComponentPosition = $positionSubComponent)
          			)
          			">
						<xsl:value-of select="'true'"/>
					</xsl:when> -->
					<xsl:when test="$RE_Usage = 1">
						<!-- include all -->
						<xsl:value-of select="'true'"/>
					</xsl:when>
					<xsl:when test="$RE_Usage = 0">
						<!-- include none -->
						<xsl:value-of select="'false'"/>
					</xsl:when>
					<xsl:when test="$RE_Usage = 0.5">
						<!-- include 1/5 to 1/2 -->
						<xsl:variable name="modNum">
							<xsl:value-of select="(0 mod 4) + 2"/>
						</xsl:variable>
						<xsl:choose>
							<xsl:when test="((position() + (0 mod 6)) mod $modNum) = 0">
								<xsl:value-of select="'true'"/>
							</xsl:when>
							<xsl:otherwise>
								<xsl:value-of select="'false'"/>
							</xsl:otherwise>
						</xsl:choose>
					</xsl:when>
					<xsl:otherwise>
						<!-- should catch all cases above, but ... -->
						<xsl:value-of select="'false'"/>
					</xsl:otherwise>
				</xsl:choose>
			</xsl:when>
			<!-- end usage=RE -->
			<xsl:otherwise>
				<!-- not including C/CE usage segs right now -->
				<xsl:value-of select="'false'"/>
			</xsl:otherwise>
		</xsl:choose>
	</xsl:template>
	<!-- ............................................................. -->
	<xsl:template name="setCardinalityOption">
		<xsl:param name="segName" select="''"/>
		<xsl:param name="fieldName" select="''"/>
		<xsl:param name="cardinality" select="0"/>
		<!--  <xsl:param name="elementNumber" select="0"/> -->
		<xsl:param name="errorMax" select="0"/>
		<xsl:param name="errorMin" select="0"/>
		<!--  <xsl:param name="errorLocationMax" select="0"/>
  <xsl:param name="errorLocationMin" select="0"/> -->
		<!-- we either set card to elementCardinality (value for just 1 element),
    	an error value (only 1 error), or 'cardinality' (all but 1 element) -->
		<xsl:choose>
			<xsl:when test="$elementCardinality != ''
      		and $segName = $namedSegment
          and $fieldName = $namedField">
				<!-- segment and field cardinality options take on values from 0 to 1.
        	0 = set card to min for all elements,
          1 = set card to max for all elements.
          values between 0 & 1 are for setting a mix of card
          values between min and max.
          'elementCardinality' is for setting the exact number
          of repetitions for a specific element.  we're adding 10
          for a specific count, to distinguish between tbe 2 cases. -->
				<xsl:value-of select="$elementCardinality+10"/>
			</xsl:when>
			<xsl:when test="number($errorMax) &lt; 0">
				<xsl:choose>
					<xsl:when test="$segName = $namedSegment
        			and $fieldName = $namedField">
						<xsl:value-of select="-1"/>
						<!-- max card error -->
					</xsl:when>
					<!--         <xsl:when test="$elementNumber = $errorLocationMax">
          <xsl:value-of select="-1"/>
        </xsl:when> -->
					<xsl:otherwise>
						<xsl:value-of select="$cardinality"/>
					</xsl:otherwise>
				</xsl:choose>
			</xsl:when>
			<xsl:when test="number($errorMin) &lt; 0">
				<xsl:choose>
					<xsl:when test="$segName = $namedSegment
        			and $fieldName = $namedField">
						<xsl:value-of select="-0.1"/>
						<!-- min card error -->
					</xsl:when>
					<!--        <xsl:when test="$elementNumber = $errorLocationMin">
          <xsl:value-of select="-0.1"/>
        </xsl:when> -->
					<xsl:otherwise>
						<xsl:value-of select="$cardinality"/>
					</xsl:otherwise>
				</xsl:choose>
			</xsl:when>
			<xsl:otherwise>
				<xsl:value-of select="$cardinality"/>
			</xsl:otherwise>
		</xsl:choose>
	</xsl:template>
	<!-- ............................................................. -->
	<xsl:template name="isPrimitive">
		<xsl:param name="dataType"/>
		<xsl:choose>
			<xsl:when test="$dataType='ST'
          or $dataType='NM'
          or $dataType='ID'
          or $dataType='DT'
          or $dataType='SI'
          or $dataType='TN'
          or $dataType='TM'
          or $dataType='TX'
          or $dataType='IS'
          or $dataType='GTS'
          or $dataType='DTM'">
				<xsl:value-of select="'true'"/>
			</xsl:when>
			<xsl:otherwise>
				<xsl:value-of select="'false'"/>
			</xsl:otherwise>
		</xsl:choose>
	</xsl:template>
</xsl:stylesheet>

<%@ taglib uri="http://java.sun.com/jsf/html"        prefix="h"     %>
<%@ taglib uri="http://java.sun.com/jsf/core"        prefix="f"     %>
<%@ taglib uri="http://myfaces.apache.org/tomahawk"  prefix="t"     %>
<%@ taglib uri="http://struts.apache.org/tags-tiles" prefix="tiles" %>

<f:subview id="compoundViewer">

	<t:panelGrid rendered="#{! empty compoundViewer.compound}" columns="1">
		<t:aliasBean alias="#{nameValueTable}"
			value="#{compoundViewer.compoundNameValueTable}">
			<%@ include file="../nameValueTable.jspf" %>
		</t:aliasBean>

    <t:div style="margin-top: 15px; margin-left: 20px;">
      <t:panelGrid columns="1">
        <t:outputText
          styleClass="subsectionHeader"
          value="Wells in which this Compound is found"
        />
			  <t:aliasBean alias="#{wells}" value="#{compoundViewer.compound.wells}">
				  <%@ include file="wellTable.jspf" %>
		  	</t:aliasBean>
			</t:panelGrid>
		</t:div>

    <%@ include file="structureImageNotice.jspf" %>

	</t:panelGrid>

	<t:panelGroup rendered="#{empty compoundViewer.compound}">
		<t:outputText
			value="There are no compounds in well #{compoundViewer.parentWellOfInterest.wellKey}"
			styleClass="label" />
	</t:panelGroup>
</f:subview>



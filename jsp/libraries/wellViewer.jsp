<%-- The html taglib contains all the tags for dealing with forms and other HTML-specific goodies. --%>
<%@ taglib uri="http://java.sun.com/jsf/html" prefix="h"%>
<%-- The core taglib contains all the logic, validation, controller, and other tags specific to JSF. --%>
<%@ taglib uri="http://java.sun.com/jsf/core" prefix="f"%>
<%-- The core taglib for JSTL; commented out until we really need it (we'll try to get by without and instead use pure JSF componentry --%>
<%--@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" --%>
<%-- The Apache Tomahawk JSF components --%>
<%@ taglib uri="http://myfaces.apache.org/tomahawk" prefix="t"%>
<%-- Tiles --%>
<%@ taglib uri="http://struts.apache.org/tags-tiles" prefix="tiles"%>

<f:subview id="well">

	<t:aliasBean alias="#{navigator}" value="#{searchResultsRegistry.searchResults}" >
		<%@ include file="../searchResultsNavPanel.jspf"  %>
	</t:aliasBean>

  <h:form id="wellForm">

    <h:panelGroup rendered="#{wellViewer.displayDone}">
      <h:commandButton id="doneCommand" action="#{wellViewer.done}" value="Done" />
    </h:panelGroup>

    <t:div />

    <h:panelGrid columns="2" styleClass="standardTable">
      <h:outputText id="plate" value="#{wellViewer.well.plateNumber}" />
      <h:outputText id="well" value="#{wellViewer.well.wellName}" />
    </h:panelGrid>

    <t:div />
  </h:form>

</f:subview>

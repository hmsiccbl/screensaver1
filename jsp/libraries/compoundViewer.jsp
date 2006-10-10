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

<f:subview id="compoundViewer">

	<t:aliasBean alias="#{navigator}" value="#{searchResultsRegistry.searchResults}" >
		<%@ include file="../searchResultsNavPanel.jspf"  %>
	</t:aliasBean>

  <h:panelGrid columns="2" rendered="true">
    <h:outputText value="smiles:" />
    <h:outputText value="#{compoundViewer.compound.smiles}" />
  </h:panelGrid>

</f:subview>



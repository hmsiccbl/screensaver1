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


<f:subview id="login">
  <h:form id="loginErrorForm">
    <t:outputText value="Based upon the user ID and password you provided, we are not yet sure of who you are.  In keeping with our policy of protecting data from unauthorized users, we cannot give you the benefit of the doubt and let you into this system. Instead, we must ask that you try once again to tell us who you are." styleClass=""/>
    <t:panelGrid columns="1">
      <t:commandButton value="Try again" action="#{mainController.viewMain}" styleClass="command"/>
      <t:commandLink value="Forgot your user ID or password?" action="#{mainController.forgotIdOrPassword}"
        styleClass="command" />
    </t:panelGrid>
  </h:form>
</f:subview>

<%@ taglib uri="http://java.sun.com/jsf/html"        prefix="h"     %>
<%@ taglib uri="http://java.sun.com/jsf/core"        prefix="f"     %>
<%@ taglib uri="http://myfaces.apache.org/tomahawk"  prefix="t"     %>
<%@ taglib uri="http://struts.apache.org/tags-tiles" prefix="tiles" %>

<f:subview id="wellFinder">
  <h:form id="wellFinderForm">
    <t:panelGrid columns="2">
      <t:panelGrid columns="1">
        <t:commandButton
          action="#{wellFinder.findWells}"
          id="findWellsSubmit1"
          value="Find Wells"
          styleClass="command"
        />
        <t:inputTextarea
          id="plateWellList"
          value="#{wellFinder.plateWellList}"
          styleClass="inputText"
          cols="50"
          rows="40"
          forceId="true"
        ></t:inputTextarea>
        <t:commandButton
          action="#{wellFinder.findWells}"
          id="findWellsSubmit2"
          value="Find Wells"
          styleClass="command"
        />
      </t:panelGrid>
			<%@ include file="../help/libraries/wellFinderInputHelp.jsp" %>
    </t:panelGrid>
  </h:form>
</f:subview>

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

<f:subview id="wellFinder">

  <h:form id="wellFinderForm">
  
    <h:panelGrid columns="1" styleClass="standardTable">
    
      <t:outputText value="Find a Well:" styleClass="sectionHeader" />
      <t:panelGroup>
        <t:outputLabel
          id="plateNumberLabel"
          for="plateNumber"
          value="Plate:"
          styleClass="inputLabel"
        />
        <t:inputText
          id="plateNumber"
          value="#{wellFinder.plateNumber}"
          size="5"
          styleClass="input"
        />
        <t:outputLabel
          id="wellNameLabel"
          for="wellName"
          value="Well:"
          styleClass="inputLabel"
        />
        <t:inputText
          id="wellName"
          value="#{wellFinder.wellName}"
          size="3"
          styleClass="input"
        />
        <t:commandButton
	      action="#{wellFinder.findWell}"
          id="findAWellSubmit"
          value="Find Well"
          styleClass="command"
        />
      </t:panelGroup>
      
      <t:outputText value="Find Multiple Wells:" styleClass="sectionHeader" />
      <t:panelGrid columns="2" styleClass="standardTable">
        <t:inputTextarea
          id="plateWellList"
          value="#{wellFinder.plateWellList}"
          styleClass="input"
        ></t:inputTextarea>
        <t:outputText value="instructions placeholder" />
        <t:commandButton
	      action="#{wellFinder.findWells}"
          id="findMultipleWellsSubmit"
          value="Find Wells"
          styleClass="command"
        />
      </t:panelGrid>
      
    </h:panelGrid>
    
  </h:form>

</f:subview>

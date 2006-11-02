<%@ taglib uri="http://java.sun.com/jsf/html"        prefix="h"     %>
<%@ taglib uri="http://java.sun.com/jsf/core"        prefix="f"     %>
<%@ taglib uri="http://myfaces.apache.org/tomahawk"  prefix="t"     %>
<%@ taglib uri="http://struts.apache.org/tags-tiles" prefix="tiles" %>

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
      <t:panelGrid columns="2">
        <t:inputTextarea
          id="plateWellList"
          value="#{wellFinder.plateWellList}"
          styleClass="input"
          cols="50"
          rows="20"
        ></t:inputTextarea>
        <t:outputLabel
          value="Enter Plate/Well information over multiple lines. The first item on every line should be the plate number, and every subsequent item on the line should be a well name. Items can be separated by whitespace, commas, or semicolons. Plate numbers can be prefixed with \"PL\", \"PL-\", or \"PL_\". Try copying two columns, \"Plate\" and \"Well\", from a spreadsheet, and pasting them into the box."
          for="plateWellList"
        />
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

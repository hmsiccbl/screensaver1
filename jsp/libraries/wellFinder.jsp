<%@ taglib uri="http://java.sun.com/jsf/html"        prefix="h"     %>
<%@ taglib uri="http://java.sun.com/jsf/core"        prefix="f"     %>
<%@ taglib uri="http://myfaces.apache.org/tomahawk"  prefix="t"     %>
<%@ taglib uri="http://struts.apache.org/tags-tiles" prefix="tiles" %>

<f:subview id="wellFinder">

  <h:form id="wellFinderForm">
  
    <h:panelGrid columns="1" styleClass="standardTable">

      <t:outputText value="Find Wells:" styleClass="sectionHeader" />
      <t:panelGrid columns="2">
        <t:inputTextarea
          id="plateWellList"
          value="#{wellFinder.plateWellList}"
          styleClass="input"
          cols="50"
          rows="35"
        ></t:inputTextarea>
        <f:verbatim escape="false">
          <style type="text/css">
            .example { font-family: monospace; background-color: #eeeeee; }
          </style>
          Instructions:
          <ul>
            <li>One plate per line, followed by one or more wells.</li>
            <li>Try copy-and-pasting the Plate and Well columns from your spreadsheet.</li>
            <li>Examples:
              <ul>
                <li><pre class="example">50a6</pre></li>
                <li><pre class="example">pl50a06</pre></li>
                <li><pre class="example">PL-50A06</pre></li>
                <li><pre class="example">PL_50A06</pre></li>
                <li><pre class="example">50a6 a7 a8</pre></li>
                <li><pre class="example">50a6,a7,a8</pre></li>
                <li><pre class="example">50 a6 a7 a8 b6 b7 b8
51 a6 a7 a8 b6 b7 b8</pre></li>
                <li><pre class="example">Plate   Well
50      A06
50      B17
50      H11
51      C10
51      E03
51      F22
...</pre></li>
              </ul>
            </li>
            <li>
              <span style="font-size: smaller;">
                Separate every element on the line with whitespace, commas, or semicolons.
              </span>
            </li>
            <li>
              <span style="font-size: smaller;">
                The first well on the line does not need a separator from the plate number.
              </span>
            </li>
            <li>
              <span style="font-size: smaller;">
                Plate numbers can be prefixed with "PL", "PL-", or "PL_".
              </span>
            </li>
            <li>
              <span style="font-size: smaller;">
                Case (uppercase or lowercase) never matters.
              </span>
            </li>
          </ul>
        </f:verbatim>
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

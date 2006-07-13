<%@include file="header.jspf"%>

<h1>
  Screen Result Import Errors
</h1>

<f:view>

  <h:form id="commandForm">

    <h:panelGroup>
      <h:commandButton id="doneCommand" immediate="true" action="#{ScreenResultImporter.done}" value="Done" styleClass="command" />
      <h:commandButton id="downloadCommand" action="#{ScreenResultImporter.downloadErrorAnnotatedWorkbook}" value="Download Error-Annotated Workbook" styleClass="command" />
    </h:panelGroup>

  </h:form>

  <p />

    <h:messages id="allMessages" globalOnly="false" showDetail="true" styleClass="errorMessage" />

  <p />

    <h:panelGroup>
      <%@include file="screenResultImport.jspf"%>
    </h:panelGroup>

    <h:form id="errorsTableForm">

      <h:outputText styleClass="sectionHeader">
        Import errors for <h:outputText value="#{ScreenResultImporter.uploadedFile.name}"/>
      </h:outputText>

      <t:dataTable id="importErrorsTable" value="#{ScreenResultImporter.importErrors}" var="row" styleClass="standardTable" headerClass="" rowClasses="row1,row2" columnClasses="">
        <t:column>
          <f:facet name="header">
            <h:outputText value="Worksheet" />
          </f:facet>
          <h:outputText value="#{row.cell.sheetName}" />
        </t:column>
        <t:column>
          <f:facet name="header">
            <h:outputText value="Cell" />
          </f:facet>
          <h:outputText value="#{row.cell.column}" />
        </t:column>
        <t:column>
          <f:facet name="header">
            <h:outputText value="Error" />
          </f:facet>
          <h:outputText value="#{row.message}" />
        </t:column>
      </t:dataTable>

    </h:form>
</f:view>

<%@include file="footer.jspf"%>

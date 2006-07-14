<%@include file="header.jspf"%>

<h1>
  Screen Result Import Errors
</h1>

<f:view>

  <h:form id="commandForm">

    <h:panelGroup>
      <h:commandButton id="doneCommand" immediate="true" action="#{ScreenResultImporter.done}" value="Done" styleClass="command" />
      <h:commandButton id="downloadCommand" actionListener="#{ScreenResultImporter.downloadErrorAnnotatedWorkbookListener}" value="Download Error-Annotated Workbook" styleClass="command" />
    </h:panelGroup>

  </h:form>

  <p />

    <h:messages id="allMessages" globalOnly="false" showDetail="true" styleClass="errorMessage" />

  <p />

    <h:panelGroup>
      <%@include file="screenResultImport.jspf"%>
    </h:panelGroup>

    <h:form id="errorsTableForm">

      <h:outputText value="Import Errors for #{ScreenResultImporter.uploadedFile.name}" styleClass="sectionHeader"/>

      <t:dataTable id="importErrorsTable" value="#{ScreenResultImporter.importErrors}" var="row" styleClass="standardTable" headerClass="tableHeader" rowClasses="row1,row2" columnClasses="">
        <t:column styleClass="column">
          <f:facet name="header">
            <h:outputText value="Worksheet" />
          </f:facet>
          <h:outputText value="#{row.cell.sheetName}" />
        </t:column>
        <t:column styleClass="column">
          <f:facet name="header">
            <h:outputText value="Cell" />
          </f:facet>
          <h:outputText value="#{row.cell.formattedRowAndColumn}" />
        </t:column>
        <t:column styleClass="column">
          <f:facet name="header">
            <h:outputText value="Error" />
          </f:facet>
          <h:outputText value="#{row.message}" />
        </t:column>
      </t:dataTable>

    </h:form>
</f:view>

<%@include file="footer.jspf"%>

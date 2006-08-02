<%@include file="header.jspf"%>

<f:view>

  <t:panelGrid id="navAndContentPanel" columns="2" style="table-layout: fixed" columnClasses="navPanelColumn,contentColumn">
    <%@include file="navPanel.jspf"%>

    <t:outputText styleClass="title" value="Welcome to Screensaver 1.x!" />

  </t:panelGrid>

</f:view>

<%@include file="footer.jspf"%>
<%@include file="/headers.inc"%>

<f:subview id="librariesBrowser">

  <t:aliasBean
    alias="#{searchResults}"
    value="#{searchResultsRegistry.searchResults}">
    <%@include file="../searchResults.jspf"%>
  </t:aliasBean>

</f:subview>



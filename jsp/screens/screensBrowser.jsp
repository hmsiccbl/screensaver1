<%@include file="/headers.inc"%>

<f:subview id="screensBrowser">

  <t:aliasBean
    alias="#{searchResults}"
    value="#{screensBrowser.searchResults}">
    <%@include file="../searchResults.jspf"%>
  </t:aliasBean>
  
</f:subview>



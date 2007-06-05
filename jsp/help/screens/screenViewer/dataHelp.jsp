<%@ taglib uri="http://java.sun.com/jsf/html"        prefix="h"     %>
<%@ taglib uri="http://java.sun.com/jsf/core"        prefix="f"     %>
<%@ taglib uri="http://myfaces.apache.org/tomahawk"  prefix="t"     %>
<%@ taglib uri="http://struts.apache.org/tags-tiles" prefix="tiles" %>

<f:subview id="dataHelpText">
  <f:verbatim escape="false">
    <p>
      Data help is currently under construction. Check back soon!
    </p>
    <p>
      You can choose to select a subset of the data headers for viewing in both the Data Headers and
      Data sections by using the "Show selected data headers" controls that
      appear above the Data Headers section when either of the Data Headers and Data sections is
      expanded. Just uncheck the checkboxes next to the data header names that you don't want to
      see, and click the "Update" button. Click the "All" button to show all the columns.
    </p>
  </f:verbatim>
</f:subview>
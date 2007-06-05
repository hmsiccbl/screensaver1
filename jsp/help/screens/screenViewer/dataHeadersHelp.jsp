<%@ taglib uri="http://java.sun.com/jsf/html"        prefix="h"     %>
<%@ taglib uri="http://java.sun.com/jsf/core"        prefix="f"     %>
<%@ taglib uri="http://myfaces.apache.org/tomahawk"  prefix="t"     %>
<%@ taglib uri="http://struts.apache.org/tags-tiles" prefix="tiles" %>

<f:subview id="dataHeadersHelpText">
  <f:verbatim escape="false">
    <p>
      The Data Headers section of the Screen Viewer page summarizes the different data value types
      that occur for every well in the screen results. Some examples of a data value type are:
      a raw value from an assay readout; a P-score or a Z-score; an average of raw values for the
      replicate readouts; etc. They are called "data headers" because they are normally the column
      headers when the data is presented in a table, such as in a spreadsheet, or the Data section
      of the Screen Viewer page.
    </p>
    <p>
      The columns in the table contain the names of the data headers, and the rows contain the
      data header attributes. Position your mouse over any of the attribute names in the left-hand
      column for a description of what that attribute.
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
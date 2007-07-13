<%@ taglib uri="http://java.sun.com/jsf/html"        prefix="h"     %>
<%@ taglib uri="http://java.sun.com/jsf/core"        prefix="f"     %>
<%@ taglib uri="http://myfaces.apache.org/tomahawk"  prefix="t"     %>
<%@ taglib uri="http://struts.apache.org/tags-tiles" prefix="tiles" %>

<f:subview id="dataHelpText">
  <f:verbatim escape="false">
    <p>
      The Data section of the Screen Viewer page allows you to browse all the data in the screen
      results. There are three extra columns to go along with all the Data Headers columns: Plate,
      Well, and Well Type. The Well Type column indicates the type of well on the <i>assay plates</i>,
      and not just the library plates, so there may be some assay control wells that are just empty
      wells int the library. You can sort and browse the data in many different ways:
      <ul>
        <li>
          Navigate through the data using the first page, previous page, next
          page, and last page buttons.
        </li>
        <li>
          From the "Show:" menu list, select "All" to browse through all result values.
        </li>
        <li>
          From the "Show:" menu list, select "Positives" to view only
          result values that have been deemed "positive". If more than one Data
          Header is a Positive Indicator, a second menu list will appear,
          allowing you to select the Data Header that will be used to determine
          the set of positives.</li>
        <li>
          From the "Show:" menu list, select a plate number to browse all the result values for a 
          particular plate.
        </li>
        <li>
          From the "Rows per page", select the number of rows you
          would like to view on the page. <span class="helpTip">Tip: </span>When
          viewing by "Positives" or by plate number, you can choose to view
          "All" available rows.</li>
        <li>
          To the right of the blue navigation buttons, you can enter in a data row number and click
          "Go" to view that data.
        </li>
        <li>
          Change the sort order by clicking on any of the column headers. Clicking once will do a
          forward sort, and clicking a second time on the same header will change to a
          reverse sort.
        </li>
        <li>
          View information about a well, including the gene or compounds in the well, by clicking
          on the well name.
        </li>
      </ul>
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
<%@ taglib uri="http://java.sun.com/jsf/html"        prefix="h"     %>
<%@ taglib uri="http://java.sun.com/jsf/core"        prefix="f"     %>
<%@ taglib uri="http://myfaces.apache.org/tomahawk"  prefix="t"     %>
<%@ taglib uri="http://struts.apache.org/tags-tiles" prefix="tiles" %>

<f:subview id="wellVolumeSearchResultsHelpText">
  <f:verbatim escape="false">
    <p>
      After searching for multiple well volumes (using the Well Finder's "Find Well Volumes" 
      command), or selecting "View Well Volumes" from the
      Library Viewer page, you will be directed to the Well Volume Search Results page,
      which displays a list of the wells you searched for with their associated volume information 
      for each library plate copy. From here you can:
      <ul>
        <li>
          Navigate through the list using the first/prev/next/last buttons on the Search
          Results Navigation Bar.
        </li>
        <li>
          Change the number of wells displayed on a single page.
        </li>
        <li>
          Change the sort order, either by clicking on the column headers, or using the controls in
          the Navigation Bar.
        </li>
        <li>
          View information about a by clicking on the well name.
        </li>
      </ul>
    </p>
  </f:verbatim>
</f:subview>

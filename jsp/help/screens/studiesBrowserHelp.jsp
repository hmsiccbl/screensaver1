<%@ taglib uri="http://java.sun.com/jsf/html"        prefix="h"     %>
<%@ taglib uri="http://java.sun.com/jsf/core"        prefix="f"     %>
<%@ taglib uri="http://myfaces.apache.org/tomahawk"  prefix="t"     %>
<%@ taglib uri="http://struts.apache.org/tags-tiles" prefix="tiles" %>

<f:subview id="studiesBrowserHelpText">
  <f:verbatim escape="false">
    <p>
      Clicking on the "Browse Studies" link in the left menu page bring you to
      the Studies Browser page, which allows you to browse all public studies
      in the database. From here, you can:
      <ul>
        <li>
          Navigate through the list using the first/prev/next/last buttons on the Search
          Results Navigation Bar.
        </li>
        <li>
          Change the number of studies displayed on a single page.
        </li>
        <li>
          Change the sort order, either by clicking on the column headers, or using the controls in
          the Navigation Bar.
        </li>
        <li>
          View information about a study and its annotations data, by clicking on the
          Study Number.
        </li>
      </ul>
    </p>
  </f:verbatim>
</f:subview>
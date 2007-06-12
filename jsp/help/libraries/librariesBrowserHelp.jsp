<%@ taglib uri="http://java.sun.com/jsf/html"        prefix="h"     %>
<%@ taglib uri="http://java.sun.com/jsf/core"        prefix="f"     %>
<%@ taglib uri="http://myfaces.apache.org/tomahawk"  prefix="t"     %>
<%@ taglib uri="http://struts.apache.org/tags-tiles" prefix="tiles" %>

<f:subview id="librariesBrowserHelpText">
  <f:verbatim escape="false">
    <p>
      Clicking on the "Browse Libraries" link in the left menu page brings you to the Libraries
      Browser page, which contains a list of the full set of libraries. From here, you can:
      <ul>
        <li>
          Navigate through the list using the first/prev/next/last buttons on the Search
          Results Navigation Bar.
        </li>
        <li>
          Change the number of libraries displayed on a single page.
          <ul>
            <li>
              <span class="helpTip">Tip: </span>Try selecting "All per page" to view all the libraries at once.
            </li>
          </ul>
        </li>
        <li>
          Change the sort order, either by clicking on the column headers, or using the controls in
          the Navigation Bar.
          <ul>
            <li>
              <span class="helpTip">Tip: </span>It can be useful to do a reverse sort on Library Type to
              bring the RNAi libraries to the top of the search results.
            </li>
          </ul>
        </li>
        <li>
          View information about a library by clicking on the library's Short Name.
        </li>
      </ul>
    </p>
  </f:verbatim>
</f:subview>
<%@ taglib uri="http://java.sun.com/jsf/html"        prefix="h"     %>
<%@ taglib uri="http://java.sun.com/jsf/core"        prefix="f"     %>
<%@ taglib uri="http://myfaces.apache.org/tomahawk"  prefix="t"     %>
<%@ taglib uri="http://struts.apache.org/tags-tiles" prefix="tiles" %>

<f:subview id="screensBrowserHelpText">
  <f:verbatim escape="false">
    <p>
      Clicking on the "Browse Screens" and "My Screens" links in the left menu page bring you
      to the Screens Browser page, which allows you to browse all screens in the database, or
      just the screens you are associated with, respectively. From here, you can:
      <ul>
        <li>
          Navigate through the list using the first/prev/next/last buttons on the Search
          Results Navigation Bar.
        </li>
        <li>
          Change the number of screens displayed on a single page.
        </li>
        <li>
          Change the sort order, either by clicking on the column headers, or using the controls in
          the Navigation Bar.
        </li>
        <li>
          View information about a screen, along with the screen results, by clicking on the
          Screen Number.
        </li>
      </ul>
    </p>
    <p>
      The Screen Results column will display "available", "none", or "not shared" depending on
      the status of that screen. "None" means that no screen results have been loaded into the
      database. "Not shared" means the screen results have been loaded, but they are not
      accessible to you because they are not yet public. "Available" means they are loaded and
      accessible to you. It can be useful to do a sort on this column to bring the screens
      with available screen results to the top of the search results.
    </p>
    <p>
      <b><i>Note</i></b> that you can get to a specific screen quickly at any time by
      entering the screen number in the Screen Quick Finder in the left menu bar.
    </p>
  </f:verbatim>
</f:subview>
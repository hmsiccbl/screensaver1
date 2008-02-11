<%@ taglib uri="http://java.sun.com/jsf/html"        prefix="h"     %>
<%@ taglib uri="http://java.sun.com/jsf/core"        prefix="f"     %>
<%@ taglib uri="http://myfaces.apache.org/tomahawk"  prefix="t"     %>
<%@ taglib uri="http://struts.apache.org/tags-tiles" prefix="tiles" %>

<f:subview id="wellSearchResultsHelpText">
  <f:verbatim escape="false">
    <p>
      After searching for multiple wells, or selecting "View Library Contents" from the
      Library Viewer page, you will be directed to the Well Search Results page,
      which displays a list of the wells you searched for. From here you can:
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
          View information about a well, including the gene or compounds in the well, by clicking
          on the well name.
        </li>
        <li>
          View a gene or compound directly by clicking on the gene name or SMILES string in the
          "Contents" column of the search results.
        </li>
        <li>
          Download your search results to an Excel file or an SD file by selecting "Excel
          Spreadsheet" or "SDFile" in the pulldown menu in the bottom right, where it says
          "download search results to", and clicking the "Download" button. (Be aware that
          large search results may take a while to download.)
        </li>
      </ul>
    </p>
    <p>
      Once you navigate down to the Well, Gene, or Compound Viewer pages, you can scroll
      through the search results item by item while remaining in that view.
    </p>
  </f:verbatim>
</f:subview>

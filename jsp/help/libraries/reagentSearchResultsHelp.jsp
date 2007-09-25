<%@ taglib uri="http://java.sun.com/jsf/html"        prefix="h"     %>
<%@ taglib uri="http://java.sun.com/jsf/core"        prefix="f"     %>
<%@ taglib uri="http://myfaces.apache.org/tomahawk"  prefix="t"     %>
<%@ taglib uri="http://struts.apache.org/tags-tiles" prefix="tiles" %>

<f:subview id="reagentSearchResultsHelpText">
  <f:verbatim escape="false">
    <p>After searching for multiple reagents (from the "Reagent Finder"
		page), you will be directed to the Reagent Search Results page, which
		displays a list of the reagents you searched for. All available
		annotations will also be displayed for each reagent, one annotation
		type per column. From here you can:<ul>
        <li>
          Navigate through the list using the first/prev/next/last buttons on the Search
          Results Navigation Bar.
        </li>
        <li>
          Change the number of reagents displayed on a single page.
        </li>
        <li>
          Change the sort order, either by clicking on the column headers, or using the controls in
          the Navigation Bar.
        </li>
        <li>By clicking the reagent identifier, you can view information
			about a reagent, including the gene or compounds in the reagent, and
			any available annotations on the reagent.</li>
        <li>
          View a gene or compound directly by clicking on the gene name or SMILES string in the
          "Contents" column of the search results.
        </li>
      </ul>
    </p>
    <p>
      Once you navigate down to the Reagent, Gene, or Compound Viewer pages, you can scroll
      through the search results item by item while remaining in that view.
    </p>
  </f:verbatim>
</f:subview>

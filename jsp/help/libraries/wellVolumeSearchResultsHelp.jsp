<%@ taglib uri="http://java.sun.com/jsf/html" prefix="h"%>
<%@ taglib uri="http://java.sun.com/jsf/core" prefix="f"%>
<%@ taglib uri="http://myfaces.apache.org/tomahawk" prefix="t"%>
<%@ taglib uri="http://struts.apache.org/tags-tiles" prefix="tiles"%>

<f:subview id="wellCopyVolumeSearchResultsHelpText">
	<f:verbatim escape="false">
		<p>After searching for multiple well volumes (using the Well
		Finder's "Find Well Volumes" command), or selecting "View Well
		Volumes" from the Library Viewer or Cherry Pick Request Viewer page,
		you will be directed to the Well Volume Search Results page. This
		search results page displays a list of the wells you searched for with
		their associated volume information. The search results consists of
		two tabs:
		<dl>
			<dt>Well Volumes</dt>
			<dd>Displays volume information per well, summarizing the
			volumes in each copy of the well. In particular, the maximum
			volume(and less importantly, the minimum volume) of the well's copies
			are dislpayed, allowing the administrator to view whether a well is
			in need of new copies or whether the existing copies are sufficient
			to satisfy a particular need.</dd>
			<dt>Well/Copy Volumes</dt>
			<dd>Displays volume information per well copy. This tab allows
			the administrator to edit the remaining volume for any of the
			available well copies.</dd>
		</dl>
       
    From each tab you can:
    <ul>
			<li>Navigate through the list using the first/prev/next/last
			buttons on the Search Results Navigation Bar.</li>
			<li>Change the number of wells displayed on a single page.</li>
			<li>Change the sort order, either by clicking on the column
			headers, or using the controls in the Navigation Bar.</li>
			<li>View information about a well by clicking on the well name.</li>
		</ul>
		
		From the "Well/Copy Volumes" tab, you can edit the remaining volume for any of the available well copies.  To do so:
		
		<ol>
			<li>Click the "Edit" button below the search results table.
			<li>
			<li>Enter the new remaining volume for whichever well copies
			need to be adjusted. You may page through the search results,
			allowing you to edit well copy volumes on multiple pages.</li>
			<li>Enter a comment explaining why the well copy volumes need to
			be adjusted.
			<li>Click the "Save" button to commit your changes, or "Cancel"
			to discard them.</li>
		</ol>
		</p>
	</f:verbatim>
</f:subview>

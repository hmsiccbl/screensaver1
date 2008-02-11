<%@ taglib uri="http://java.sun.com/jsf/html" prefix="h"%>
<%@ taglib uri="http://java.sun.com/jsf/core" prefix="f"%>
<%@ taglib uri="http://myfaces.apache.org/tomahawk" prefix="t"%>
<%@ taglib uri="http://struts.apache.org/tags-tiles" prefix="tiles"%>

<f:subview id="reagentFinderHelp">
	<f:verbatim escape="false">
		<p>You can easily look up reagents (small molecules, siRNAs, etc.)
		in the ICCB-L libraries collection by clicking on the "Find Reagents"
		link in the left menu pane, which opens the "Reagent Finder" page.
		From the "Source/Vendor" list, select the source or vendor that
		provides the reagents for which you are searching. In the text field,
		type (or paste from the clipboard) the identifier for one or more
		reagents. The Reagent Finder page contains detailed instructions
		explaining how the reagent identifiers should be specified.</p>
	</f:verbatim>
</f:subview>

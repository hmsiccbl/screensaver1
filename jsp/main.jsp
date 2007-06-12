<%-- The html taglib contains all the tags for dealing with forms and other HTML-specific goodies. --%>
<%@ taglib uri="http://java.sun.com/jsf/html" prefix="h"%>
<%-- The core taglib contains all the logic, validation, controller, and other tags specific to JSF. --%>
<%@ taglib uri="http://java.sun.com/jsf/core" prefix="f"%>
<%-- The core taglib for JSTL; commented out until we really need it (we'll try to get by without and instead use pure JSF componentry --%>
<%--@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" --%>
<%-- The Apache Tomahawk JSF components --%>
<%@ taglib uri="http://myfaces.apache.org/tomahawk" prefix="t"%>
<%-- Tiles --%>
<%@ taglib uri="http://struts.apache.org/tags-tiles" prefix="tiles"%>

<f:subview id="main">
	<t:outputText
		value="Welcome to Screensaver, #{menu.screensaverUser.fullNameFirstLast}!"
		styleClass="title" />
	<t:div />
	<f:verbatim escape="false">
		<p>To get started, try clicking some of the items in the left menu
		bar. For a detailed description of what you can do, see our <a
			href="helpViewer.jsf">help</a> page.  Here are a few highlights:</p>
		<p>
		<ul>
			<li>Use "Browse Libraries" or "Find Wells" to view the contents
			of ICCB-L compound and RNAi libraries, including structures, SMILES
			strings, vendor information, and when possible, links to PubChem (or
			GenBank for RNAi libraries).</li>
			<li>"Find Wells" should be your first stop when searching for
			information on wells that score as positive (or potentially positive)
			in your screen.</li>
			<li>From "Data Downloads" you can download SD files for all
			ICCB-L libraries, either as a single file or as individual library
			files (note that natural product extracts do not have SD files or
			structural information).</li>
			<li>"My Screens" permits access to your personal screen result data,
			if it has been loaded into the database. We are endeavoring to load
			all available screen result data, so if you find that your screen's
			result data is not available for viewing, please contact <a
				href="mailto:david_wrobel@hms.harvard.edu">David Wrobel</a> and we
			will make it a priority. Among the more useful features are the
			ability to view each screening data plate with a heat map viewer and
			to download the screen result data as a spreadsheet. Note that you
			will not be able to view screen results from any screens other than
			your own unless they have been classified as "shared" by the
			screener, in which case they are available for viewing. ICCB-L is in
			the process of developing guidelines for data sharing. Please contact
			<a
				href="mailto:caroline_shamu@hms.harvard.edu">Caroline Shamu</a> or <a
				href="mailto:su_chiang@hms.harvard.edu">Su Chiang</a> if you have
			any questions.</li>
		</ul>
		</p>

			<p>The development team will be continuing to add features and
		enhancements, so your questions and feedback are welcome! Please use
		the feedback link at bottom of the page to contact the development
		team.</p>
	</f:verbatim>
</f:subview>

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
		<div id="ie_detection_message" style="display: none; color: fuchsia;">
		The Screensaver development team recommends the <a
			href="www.mozilla.com">Mozilla Firefox Browser</a> (version 1.5 or
		higher). While we work to support other browsers, it's not always
		possible for us to test every feature across all browser types.</div>
		<script type="text/javascript">
            if (! Array.every) { // if not firefox >=1.5
              document.getElementById("ie_detection_message").style.display = "block";
            }
          </script>
		<p>To get started, try clicking some of the items in the left menu
		bar. For a detailed description of what you can do, see our <a
			href="helpViewer.jsf">help</a> page. Here are a few highlights:</p>
		<p>
		<ul>
			<li>Use "Browse Libraries" to view the contents of ICCB-L
			compound and RNAi libraries, including structures, SMILES strings,
			vendor information, and when possible, links to PubChem (or GenBank
			for RNAi libraries).</li>
	</f:verbatim>
	<t:div rendered="#{menu.screener || menu.readAdmin}">
		<f:verbatim escape="false">
			<li>Use "Find Wells" to search for information on a particular
			set of wells, such as those that scored as positive (or potentially
			positive) in your screen.</li>
		</f:verbatim>
	</t:div>
	<t:div >
		<f:verbatim escape="false">
			<li>Use "Browse Studies" to view the studies that have been added to
			Screensaver. Studies associated biologically significant annotations with
			library reagents.</f:verbatim>
	</t:div>
	<t:div rendered="#{menu.screener || menu.readAdmin}">
		<f:verbatim escape="false">
			<li>"My Screens" permits access to your personal screen result
			data, if it has been loaded into the database. We are endeavoring to
			load all available screen result data, so if you find that your
			screen's result data is not available for viewing, please contact <a
				href="mailto:david_wrobel@hms.harvard.edu">David Wrobel</a> and we
			will make it a priority. Among the more useful features are the
			ability to view each screening data plate with a heat map viewer and
			to download the screen result data as a spreadsheet. Note that you
			will not be able to view screen results from any screens other than
			your own unless they have been classified as "shared" by the
			screener, in which case they are available for viewing. ICCB-L is in
			the process of developing guidelines for data sharing. Please contact
			<a href="mailto:caroline_shamu@hms.harvard.edu">Caroline Shamu</a> or
			<a href="mailto:su_chiang@hms.harvard.edu">Su Chiang</a> if you have
			any questions.</li>
		</f:verbatim>
	</t:div>
	<f:verbatim escape="false">
		<li>From "Data Downloads" you can download SD files for all
		ICCB-L libraries, either as a single file or as individual library
		files (note that natural product extracts do not have SD files or
		structural information).</li>
	</f:verbatim>
	<f:verbatim escape="false">
		</ul>
		</p>

		<p>The development team will be continuing to add features and
		enhancements, so your questions and feedback are welcome! Please use
		the feedback link at bottom of the page to contact the development
		team.</p>
	</f:verbatim>
</f:subview>

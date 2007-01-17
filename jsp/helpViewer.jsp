<%@ taglib uri="http://java.sun.com/jsf/html"        prefix="h"     %>
<%@ taglib uri="http://java.sun.com/jsf/core"        prefix="f"     %>
<%@ taglib uri="http://myfaces.apache.org/tomahawk"  prefix="t"     %>
<%@ taglib uri="http://struts.apache.org/tags-tiles" prefix="tiles" %>

<f:subview id="helpViewer">

  <f:verbatim escape="false">
	Thanks for trying out this early release of Screensaver! Here's a quick guide to how you can
	use Screensaver and what it can do for you.
    <ul>
      <li>
        <b>Finding Wells</b>
        <br/>
        You can easily look up the contents of wells in the ICCB-L libraries collection in various
        ways:
        <ul>
          <li>
            To look up the contents of a single well, enter in the plate number and well name in
            the Plate/Well text boxes in the left menu pane;
          </li>
          <li>
            Or, click on the "Find Wells" link in the left menu pane to get to the Well Finder
            page;
          </li>
          <li>
            You can enter in the coordinates for a single well at the top of the page;
          </li>
          <li>
            Or, multiple wells at the bottom;
          </li>
          <li>
            There are many formats you can enter in multiple wells. The first item on the line should
            be the plate number, and the remaining items should be the well name (e.g., "A05" or
            "a5"). You can enter in as many lines as you want, and use whitespace or punctuation to
            separate the plate number from the well names, and the individual well names;
          </li>
          <li>
            Try the following:
            <ul>
              <li>
                Open a Screen Results spreadsheet;
              </li>
              <li>
                Highlight the Plate and Well columns by control-clicking or apple-clicking) the
                column headers for those columns;
              </li>
              <li>
                Type control-C (or apple-C on Mac) to copy the Plate/Well list to the clipboard;
              </li>
              <li>
                Click in the "Find Multiple Wells" text box on the Well Finder page, and type
                control-V (or apple-V) to paste the Plate/Well list into the text box;
              </li>
              <li>
                Click the "Find Wells" button. This should bring you to a list of the wells in your
                Screen Results!
              </li>
            </ul>
          </li>
        </ul>
      </li>
      <li>
        <b>Viewing Wells</b>
        <br/>
        After searching for multiple wells, you will be on the Well Search Results page,
        which displays a list of the wells you searched for. You can:
        <ul>
          <li>
            Navigate through the list using the first/prev/next/last buttons on the Search
            Results Navigation Bar;
          </li>
          <li>
            Change the number of wells displayed on a single page;
          </li>
          <li>
            Change the sort order, either by clicking on the column headers, or using the controls in
            the Navigation Bar;
          </li>
          <li>
            View information about a well, including the gene or compounds in the well, by clicking
            on the well name;
          </li>
          <li>
            View a gene or compound directly by clicking on the gene name or SMILES string in the
            "Contents" column of the search results;
          </li>
          <li>
            Once you navigate down to the Well, Gene, or Compound Viewer pages, you can scroll
            through the search results item by item while remaining in that view;
          </li>
          <li>
            Download your search results to an Excel file or an SD file by selecting "Excel
            Spreadsheet" or "SDFile" in the pulldown menu in the bottom right, where it says
            "download search results to". (Be aware that large search results may take a while to
            download.)
          </li>
        </ul>
      </li>
      <li>
        <b>Browsing Libraries</b>
        <br/>
        Click on the "Browse Libraries" link in the left menu page to get to the Libraries Browser,
        which contains a "search results" of the full set of libraries.
        From there, you can:
        <ul>
          <li>
            Go to the first/prev/next/last page of the search results; change the number of
            libraries displayed on a single page; and change the sort order
            (HINT: it can be useful to do a reverse sort on Library Type to
            get the RNAi libraries to the top of the search results);
          </li>
          <li>
            View information about a library by clicking on the library name, which takes you to the
            Library Viewer page;
          </li>
          <li>
            From the Library Viewer page, you can view the library contents, which brings up a Well
            Search Results page with all the wells in that library. You can navigate the Well
            Search Results as described above in the section on Finding Wells;
          </li>
          <li>
            If you are a <i>libraries adminstrator</i>, you can import library contents from the
            Library Viewer page as well.
          </li>
        </ul>
      </li>
      <li>
        <b>Browsing Screens</b>
        <br/>
        Click on the "Browse Screens" link in the left menu page to get to the Screens Browser,
        which contains a "search results" of all the screens you are allowed to see. If you are
        an <i>administrator</i>, this will be the complete set of screens. If you are a
        <i>screener</i>, then it will be a smaller set of screens.
        From there, you can:
        <ul>
          <li>
            Go to the first/prev/next/last page of the search results; change the number of
            libraries displayed on a single page; and change the sort order;
          </li>
          <li>
            View information about a screen by clicking on the screen number, which takes you to the
            Screen Viewer page;
          </li>
          <li>
            If you are a <i>screener</i>, the Screen Viewer page will display some basic information about
            the screen.
          </li>
          <li>
            If you are an <i>administrator</i>, it will display administrative information about
            the screen that is not viewable by normal users;
          </li>
          <li>
            If you are a <i>screens administrator</i>, it will allow you to edit the information
            about a screen.
            (Keep in mind that any changes you make to the screen will not be permanent! You will want
            to change the information as it appears in ScreenDB instead. The
            information about the libraries, screens, and screeners will be reloaded from ScreenDB
            into Screensaver periodically);
          </li>
          <li>
            You can also view the screen results from the Screen Viewer page if they are currently
            loaded into the Screensaver database;
          </li>
          <li>
            You will only be able to view screen results you have permission to see. If you are a
            <i>screener</i>, this should include all your own screens, plus the screens you
            collaborated on;
          </li>
          <li>
            <i>Administrators</i> can see all the screen results;
          </li>
          <li>
            <i>Screen results administrators</i> can also load or reload screen results into the
            database.
          </li>
        </ul>
      </li>
      <li>
        <b>Viewing Screen Results</b>
        <br/>
        Clicking "View Screen Results" from the Screen Viewer page will take you to the Screen Result
        Viewer page. From there, you can:
        <ul>
          <li>
            Download the screen results to an Excel file by clicking the "Download" button;
          </li>
          <li>
            Go back to the Screen Viewer page by clicking the "View Screen" button;
          </li>
          <li>
            If you are a <i>screen results administrator</i>, you will see controls for deleting the
            screen results, and reloading them from a file;
          </li>
          <li>
            Show or hide some or all of the data headers for the screen results: use control-clicks
            (apple-clicks on Mac) to select the data headers you want to see in the list below the
            "Show selected data headers:" label;
          </li>
          <li>
            Open and close the four major sections of the Screen Results Viewer page (Summary; Data
            Headers; Data; and Heat Maps) by clicking on
            the triangles next to the section headers;
          </li>
          <li>
            Be sure to check out the Heat Map Viewer - it's pretty awesome!
          </li>
          <li>
            Be sure to try the "Add Heat Map" button to view two heat maps for different data headers
            side by side;
          </li>
          <li>
            You can exclude control wells and edge wells from Z-score normalization calculations and
            color scaling by checking the "Control wells" and "Edge wells" checkboxes to the left
            of the heat map.
          </li>
        </ul>
      </li>
      <li>
        <b>Give us feedback!</b>
        <br/>
        We want to know what you think!
        For now, you can give us feedback by sending email to
        <a href="mailto:andrew_tolopko@hms.harvard.edu">andrew_tolopko@hms.harvard.edu</a> and
        <a href="mailto:john_sullivan@hms.harvard.edu">john_sullivan@hms.harvard.edu</a>.
      </li>
    </ul>
  </f:verbatim>
  
</f:subview>

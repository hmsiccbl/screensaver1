// $HeadURL$
// $Id$
//
// Copyright © 2006, 2010 by the President and Fellows of Harvard College.
//
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.ui.libraries;

import java.math.BigDecimal;
import java.util.List;
import java.util.SortedSet;

import com.google.common.collect.Sets;
import org.apache.commons.lang.StringEscapeUtils;
import org.apache.log4j.Logger;

import edu.harvard.med.screensaver.db.GenericEntityDAO;
import edu.harvard.med.screensaver.model.MolarConcentration;
import edu.harvard.med.screensaver.model.Volume;
import edu.harvard.med.screensaver.model.libraries.Copy;
import edu.harvard.med.screensaver.model.libraries.Plate;
import edu.harvard.med.screensaver.model.libraries.PlateStatus;
import edu.harvard.med.screensaver.model.libraries.PlateType;
import edu.harvard.med.screensaver.model.users.ScreensaverUserRole;
import edu.harvard.med.screensaver.service.OperationRestrictedException;
import edu.harvard.med.screensaver.ui.arch.view.SearchResultContextEntityViewerBackingBean;

/**
 */
public class LibraryCopyViewer extends SearchResultContextEntityViewerBackingBean<Copy,Copy>
{
  private static Logger log = Logger.getLogger(LibraryCopyViewer.class);

  private LibraryCopyDetail _libraryCopyDetail;
  private LibraryCopyPlateSearchResults _libraryCopyPlateSearchResults;
  private LibraryCopyPlateCommentSearchResults _libraryCopyPlateCommentSearchResults;
  private PlateAggregateFields _plateAggregateFields = null;

  /**
   * @motivation for CGLIB2
   */
  protected LibraryCopyViewer()
  {
  }

  public LibraryCopyViewer(LibraryCopyViewer libraryCopyViewerProxy,
                           GenericEntityDAO dao,
                           LibraryCopySearchResults libraryCopySearchResults,
                           LibraryCopyDetail libraryCopyDetail,
                           LibraryCopyPlateSearchResults libraryCopyPlateSearchResults,
                           LibraryCopyPlateCommentSearchResults libraryCopyPlateCommentSearchResults)
  {
    super(libraryCopyViewerProxy,
          Copy.class,
          BROWSE_LIBRARY_COPIES,
          VIEW_LIBRARY_COPY,
          dao,
          libraryCopySearchResults);
    _libraryCopyDetail = libraryCopyDetail;
    _libraryCopyPlateSearchResults = libraryCopyPlateSearchResults;
    _libraryCopyPlateSearchResults.setNestedIn(this);
    _libraryCopyPlateCommentSearchResults = libraryCopyPlateCommentSearchResults;
    getIsPanelCollapsedMap().put("plateComments", true);
    getIsPanelCollapsedMap().put("plates", true);
  }

  public LibraryCopyPlateSearchResults getLibraryCopyPlateSearchResults()
  {
    return _libraryCopyPlateSearchResults;
  }

  public LibraryCopyPlateCommentSearchResults getLibraryCopyPlateCommentSearchResults()
  {
    return _libraryCopyPlateCommentSearchResults;
  }

  @Override
  protected void initializeViewer(Copy copy)
  {
    if (!!!getScreensaverUser().isUserInRole(ScreensaverUserRole.READ_EVERYTHING_ADMIN)) {
      throw new OperationRestrictedException("view Library Copy");
    }
    _libraryCopyDetail.setEntity(copy);
    getLibraryCopyPlateSearchResults().searchPlatesForCopy(copy);
    getLibraryCopyPlateCommentSearchResults().searchForCopy(copy);
    _plateAggregateFields = null;
  }

  @Override
  protected void initializeEntity(Copy copy)
  {
    getDao().needReadOnly(copy, Copy.library);
  }
  
  /**
   * Get whether user can view any data in the current view.
   * 
   * @return <code>true</code> iff user can view any data in the current view
   */
  public boolean isLibraryCopiesAdmin()
  {
    return isUserInRole(ScreensaverUserRole.LIBRARY_COPIES_ADMIN);
  }

  /*
   * For [#2571] copy plates info made prominent as copy viewer properties - display plate properties as (pseudo) copy
   * properties
   * in the libraryCopyDetail.xhtml
   */
  public PlateAggregateFields getPlateAggregateFields()
  {
    if (_plateAggregateFields == null) {
      _plateAggregateFields = new PlateAggregateFields((List<Plate>) getLibraryCopyPlateSearchResults().getDataTableModel().getWrappedData());
    }
    return _plateAggregateFields;
  }

  /*
   * For [#2571] copy plates info made prominent as copy viewer properties - display plate properties as (pseudo) copy
   * properties
   * in the libraryCopyDetail.xhtml
   */
  public class PlateAggregateFields
  {
    private SortedSet<String> _locations = Sets.newTreeSet();
    private SortedSet<Volume> _volumes = Sets.newTreeSet();
    private SortedSet<BigDecimal> _mgMlConcentrations = Sets.newTreeSet();
    private SortedSet<MolarConcentration> _molarConcentrations = Sets.newTreeSet();
    private SortedSet<PlateStatus> _secondaryStatuses = Sets.newTreeSet();
    private SortedSet<PlateType> _types = Sets.newTreeSet();

    private PlateAggregateFields(List<Plate> plates)
    {
      for (Plate plate : plates) {
        if (plate.getLocation() != null) {
          _locations.add(StringEscapeUtils.escapeHtml(plate.getLocation().toDisplayString()));
        }
        if (plate.getWellVolume() != null) {
          _volumes.add(plate.getWellVolume());
        }
        if(plate.getMgMlConcentration() != null) {
          _mgMlConcentrations.add(plate.getMgMlConcentration());
        }
        if(plate.getMolarConcentration() != null) {
          _molarConcentrations.add(plate.getMolarConcentration());
        }
        _secondaryStatuses.add(plate.getStatus());
        if (plate.getPlateType() != null) {
          _types.add(plate.getPlateType());
        }
      }
      _secondaryStatuses.remove(getEntity().getPrimaryPlateStatus());
    }

    public SortedSet<String> getLocations()
    {
      return _locations;
    }

    public SortedSet<Volume> getVolumes()
    {
      return _volumes;
    }
    
    public SortedSet<BigDecimal> getMgMlConcentrations()
    {
      return _mgMlConcentrations;
    }
    
    public SortedSet<MolarConcentration> getMolarConcentrations()
    {
      return _molarConcentrations;
    }

    public SortedSet<PlateStatus> getSecondaryStatuses()
    {
      return _secondaryStatuses;
    }

    public SortedSet<PlateType> getTypes()
    {
      return _types;
    }
  };

}


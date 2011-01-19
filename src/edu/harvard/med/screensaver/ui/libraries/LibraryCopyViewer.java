// $HeadURL$
// $Id$
//
// Copyright © 2006, 2010 by the President and Fellows of Harvard College.
//
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.ui.libraries;

import java.util.List;
import java.util.Set;

import com.google.common.base.Function;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import org.apache.commons.lang.StringEscapeUtils;
import org.apache.log4j.Logger;

import edu.harvard.med.screensaver.db.GenericEntityDAO;
import edu.harvard.med.screensaver.model.MolarConcentration;
import edu.harvard.med.screensaver.model.Volume;
import edu.harvard.med.screensaver.model.libraries.Copy;
import edu.harvard.med.screensaver.model.libraries.Plate;
import edu.harvard.med.screensaver.model.libraries.PlateLocation;
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
    getDao().needReadOnly(copy, Copy.library.getPath());
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
    private List<String> _locations = Lists.newArrayList();
    private List<String> _volumes = Lists.newArrayList();
    private List<String> _concentrations = Lists.newArrayList();
    private List<String> _statuses = Lists.newArrayList();
    private List<String> _types = Lists.newArrayList();

    private Function<PlateLocation,String> _plateLocationFunction = new Function<PlateLocation,String>() {
      @Override
      public String apply(PlateLocation from)
      {
        return from == null ? "" : StringEscapeUtils.escapeHtml(from.toDisplayString());
      }
    };

    private Function<PlateStatus,String> _plateStatusFunction = new Function<PlateStatus,String>() {
      @Override
      public String apply(PlateStatus from)
      {
        return from == null ? "" : from.toString();
      }
    };

    private Function<Volume,String> _plateVolumeFunction = new Function<Volume,String>() {
      @Override
      public String apply(Volume from)
      {
        return from == null ? "" : from.toString();
      }
    };

    private Function<MolarConcentration,String> _plateConcentrationFunction = new Function<MolarConcentration,String>() {
      @Override
      public String apply(MolarConcentration from)
      {
        return from == null ? "" : from.toString();
      }
    };

    private Function<PlateType,String> _plateTypeFunction = new Function<PlateType,String>() {
      @Override
      public String apply(PlateType from)
      {
        return from == null ? "" : from.toString();
      }
    };

    private PlateAggregateFields(List<Plate> plates)
    {
      Set<PlateLocation> locations = Sets.newHashSet();
      Set<Volume> volumes = Sets.newHashSet();
      //      Set<Concentration> concentrations = Sets.newHashSet();
      Set<PlateStatus> statuses = Sets.newHashSet();
      Set<PlateType> types = Sets.newHashSet();
      for (Plate plate : plates) {
        locations.add(plate.getLocation());
        volumes.add(plate.getWellVolume());
        statuses.add(plate.getStatus());
        types.add(plate.getPlateType());
      }
      _locations = Lists.newArrayList(Iterables.transform(locations, _plateLocationFunction));
      _volumes = Lists.newArrayList(Iterables.transform(volumes, _plateVolumeFunction));
      //      _concentrations = Lists.newArrayList(Iterables.transform(concentrations, _plateConcentrationFunction));
      _statuses = Lists.newArrayList(Iterables.transform(statuses, _plateStatusFunction));
      _types = Lists.newArrayList(Iterables.transform(types, _plateTypeFunction));
    }

    public List<String> getLocations()
    {
      return _locations;
    }

    public List<String> getVolumes()
    {
      return _volumes;
    }

    //TODO: verify that this will actually ever be used
    //    public List<String> getConcentrations()
    //    {
    //      return _concentrations;
    //    }

    public List<String> getStatuses()
    {
      return _statuses;
    }

    public List<String> getTypes()
    {
      return _types;
    }
  };

}


// $HeadURL: svn+ssh://js163@orchestra.med.harvard.edu/svn/iccb/screensaver/trunk/.eclipse.prefs/codetemplates.xml $
// $Id: codetemplates.xml 169 2006-06-14 21:57:49Z js163 $
//
// Copyright 2006 by the President and Fellows of Harvard College.
//
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.ui.searchresults;

import javax.faces.model.DataModel;

import org.apache.log4j.Logger;

/**
 * SearchResults subclass that add support for the display of a row's "detail"
 * in a subtable, where the detail data is packaged in SearchResult
 *
 * @param E the type of each row's data object
 * @param D the type of each row's detail data SearchResult object
 * @author <a mailto="andrew_tolopko@hms.harvard.edu">Andrew Tolopko</a>
 * @author <a mailto="john_sullivan@hms.harvard.edu">John Sullivan</a>
 */
public abstract class SearchResultsWithRowDetail<E,D extends SearchResults> extends SearchResults<E>
{
  // static members

  private static Logger log = Logger.getLogger(SearchResultsWithRowDetail.class);

  private static final String[] CAPABILITIES = { "viewRowDetail" };

  // instance data members

  private D _rowDetail;
  private boolean _isRowDetailVisible;


  // public constructors and methods

  public SearchResultsWithRowDetail()
  {
    super(CAPABILITIES);
  }

  protected void makeRowDetail(E entity)
  {
  }

  public D getRowDetail()
  {
    return _rowDetail;
  }

  public void setRowDetail(D rowDetail)
  {
    _rowDetail = rowDetail;
  }

  @SuppressWarnings("unchecked")
  public String showRowDetail()
  {
    if (getDataTable().getDataModel().isRowAvailable()) {
      E entity = getRowData();
      makeRowDetail(entity);
      _isRowDetailVisible = true;
    }
    return REDISPLAY_PAGE_ACTION_RESULT;
  }

  public String hideRowDetail()
  {
    _isRowDetailVisible = false;
    return REDISPLAY_PAGE_ACTION_RESULT;
  }

  public boolean isRowDetailVisible()
  {
    return _isRowDetailVisible;
  }

  // protected methods

  @Override
  protected DataModel buildDataModel()
  {
    hideRowDetail();
    return super.buildDataModel();
  }

  @Override
  protected void setEditMode(boolean isEditMode)
  {
    hideRowDetail();
    super.setEditMode(isEditMode);
  }


  // private methods

}


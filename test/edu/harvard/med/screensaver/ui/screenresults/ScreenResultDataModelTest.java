// $HeadURL: svn+ssh://js163@orchestra.med.harvard.edu/svn/iccb/screensaver/branches/schema-upgrade-2007/.eclipse.prefs/codetemplates.xml $
// $Id: codetemplates.xml 169 2006-06-14 21:57:49Z js163 $
//
// Copyright 2006 by the President and Fellows of Harvard College.
//
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.ui.screenresults;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import edu.harvard.med.screensaver.AbstractSpringPersistenceTest;
import edu.harvard.med.screensaver.db.ScreenResultsDAO;
import edu.harvard.med.screensaver.db.SortDirection;
import edu.harvard.med.screensaver.db.ScreenResultSortQuery.SortByWellProperty;
import edu.harvard.med.screensaver.model.MakeDummyEntities;
import edu.harvard.med.screensaver.model.libraries.Library;
import edu.harvard.med.screensaver.model.libraries.Well;
import edu.harvard.med.screensaver.model.screenresults.ScreenResult;
import edu.harvard.med.screensaver.model.screens.Screen;
import edu.harvard.med.screensaver.model.screens.ScreenType;
import edu.harvard.med.screensaver.ui.searchresults.WellColumn;
import edu.harvard.med.screensaver.ui.table.VirtualPagingDataModel;

import org.apache.log4j.Logger;
import org.springframework.orm.hibernate3.HibernateTemplate;

public class ScreenResultDataModelTest extends AbstractSpringPersistenceTest
{
  // static members

  private static Logger log = Logger.getLogger(ScreenResultDataModelTest.class);

  protected HibernateTemplate hibernateTemplate;
  protected ScreenResultsDAO screenResultsDao;

  private Screen _rnaiScreen;

  private Screen _smallMoleculeScreen;

  @Override
  protected void onSetUp() throws Exception
  {
    super.onSetUp();
    _smallMoleculeScreen = MakeDummyEntities.makeDummyScreen(1, ScreenType.SMALL_MOLECULE);
    Library smallMoleculeLibrary = MakeDummyEntities.makeDummyLibrary(_smallMoleculeScreen.getScreenNumber(), _smallMoleculeScreen.getScreenType(), 3);
    MakeDummyEntities.makeDummyScreenResult(_smallMoleculeScreen, smallMoleculeLibrary);
    genericEntityDao.saveOrUpdateEntity(smallMoleculeLibrary);
    genericEntityDao.saveOrUpdateEntity(_smallMoleculeScreen);

    _rnaiScreen = MakeDummyEntities.makeDummyScreen(2, ScreenType.RNAI);
    Library rnaiLibrary = MakeDummyEntities.makeDummyLibrary(_rnaiScreen.getScreenNumber(), _rnaiScreen.getScreenType(), 3);
    MakeDummyEntities.makeDummyScreenResult(_rnaiScreen, rnaiLibrary);
    genericEntityDao.saveOrUpdateEntity(rnaiLibrary);
    genericEntityDao.saveOrUpdateEntity(_rnaiScreen);
  }

  // TODO: test all ScreenResultDataModel subclasses
  // TODO: test all sort columns
  // TODO: sort asc & desc
  // TODO: test fetch efficiency by inspecting Hib statistics (e.g.)
  public void testFullScreenResultDataModel()
  {
    ScreenResult screenResult = _rnaiScreen.getScreenResult();
    WellColumn sortColumn =
      new WellColumn(SortByWellProperty.WELL_NAME,
                     "wellName",
                     "Well Name",
                     false) {
      @Override public Object getCellValue(Well entity) { return entity; }
    };
    FullScreenResultDataModel dataModel =
      new FullScreenResultDataModel(screenResult,
                                    screenResult.getResultValueTypesList(),
                                    screenResult.getWellCount(),
                                    10,
                                    sortColumn,
                                    SortDirection.ASCENDING,
                                    genericEntityDao);
    testSort(dataModel, sortColumn, SortDirection.ASCENDING);
  }

  @SuppressWarnings("unchecked")
  private void testSort(VirtualPagingDataModel<String,Well> dataModel, WellColumn sortColumn, SortDirection sortDirection)
  {
    dataModel.sort(sortColumn, sortDirection);
    List<Comparable> actualSortedValues = new ArrayList<Comparable>();
    List<Comparable> expectedSortedValues = new ArrayList<Comparable>();
    for (int i = 0; i < 10; i++) {
      dataModel.setRowIndex(i);
      Well rowData = dataModel.getRowData();
      actualSortedValues.add((Comparable) sortColumn.getCellValue(rowData));
    }
    assertEquals("row count", 10, actualSortedValues.size());
    expectedSortedValues.addAll(actualSortedValues);
    Collections.sort(expectedSortedValues);
    assertEquals("sorted values on " + sortColumn.getName() + ", " + sortDirection,
                 expectedSortedValues,
                 actualSortedValues);
  }

}

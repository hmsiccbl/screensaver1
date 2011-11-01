// $HeadURL: http://forge.abcd.harvard.edu/svn/screensaver/branches/atolopko/2189/core/src/main/java/edu/harvard/med/screensaver/ui/screenresults/ScreenResultImporter.java $
// $Id: ScreenResultImporter.java 5492 2011-03-11 20:39:01Z atolopko $
//
// Copyright © 2006, 2010 by the President and Fellows of Harvard College.
//
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.ui.screenresults;

import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;

import javax.faces.model.DataModel;
import javax.faces.model.ListDataModel;

import com.google.common.base.Function;

import com.google.common.collect.Iterables;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.apache.log4j.Logger;
import org.apache.myfaces.custom.fileupload.UploadedFile;

import edu.harvard.med.iccbl.platereader.parser.PlateOrdering;
import edu.harvard.med.screensaver.io.ParseError;
import edu.harvard.med.screensaver.io.workbook2.Workbook;
import edu.harvard.med.screensaver.model.libraries.WellName;
import edu.harvard.med.screensaver.model.screenresults.AssayWellControlType;
import edu.harvard.med.screensaver.model.screens.AssayReadoutType;
import edu.harvard.med.screensaver.model.screens.Screen;
import edu.harvard.med.screensaver.service.screenresult.PlateReaderRawDataTransformer.Result;
import edu.harvard.med.screensaver.ui.arch.util.JSFUtils;
import edu.harvard.med.screensaver.ui.arch.util.UISelectOneBean;
import edu.harvard.med.screensaver.ui.arch.view.AbstractBackingBean;
import edu.harvard.med.screensaver.ui.arch.view.aspects.UICommand;
import edu.harvard.med.screensaver.ui.screens.ScreenViewer;

public class PlateReaderRawDataTransformer extends AbstractBackingBean
{
  private static final Logger log = Logger.getLogger(PlateReaderRawDataTransformer.class);

  private ScreenViewer _screenViewer;
  private edu.harvard.med.screensaver.service.screenresult.PlateReaderRawDataTransformer _transformer;

  private UploadedFile _uploadedFile;
  private String _plates;
  private UISelectOneBean<AssayReadoutType> _readoutTypes = 
    new UISelectOneBean<AssayReadoutType>(Lists.newArrayList(AssayReadoutType.values()), true) {
      protected String getEmptyLabel()
      {
        return "<select>";
      }
    };
  private String _conditions;
  private Integer _replicates;
  private Result _result;
  private String _comments;
  private List<? extends ParseError> _lastParseErrors;


  /**
   * @motivation for CGLIB2
   */
  protected PlateReaderRawDataTransformer()
  {
  }

  public PlateReaderRawDataTransformer(ScreenViewer screenViewer,
                                       edu.harvard.med.screensaver.service.screenresult.PlateReaderRawDataTransformer transformer)
  {
    _screenViewer = screenViewer;
    _transformer = transformer;
  }

  public Screen getScreen()
  {
    return _screenViewer.getEntity();
  }

  public void setUploadedFile(UploadedFile uploadedFile)
  {
    _uploadedFile = uploadedFile;
  }

  public UploadedFile getUploadedFile()
  {
    return _uploadedFile;
  }

  public String getPlates()
  {
    return _plates;
  }

  public UISelectOneBean<AssayReadoutType> getReadoutTypes()
  {
    return _readoutTypes;
  }

  public void setPlates(String plates)
  {
    _plates = plates;
  }

  public String getConditions()
  {
    return _conditions;
  }

  public void setConditions(String conditions)
  {
    _conditions = conditions;
  }

  public Integer getReplicates()
  {
    return _replicates;
  }

  public void setReplicates(Integer replicates)
  {
    _replicates = replicates;
  }

  public String getComments()
  {
    return _comments;
  }

  public void setComments(String comments)
  {
    _comments = comments;
  }

  @UICommand
  public String cancel()
  {
    return _screenViewer.reload();
  }

  @UICommand
  public String view() throws IOException
  {
    reset();
    return TRANSFORM_PLATE_READER_RAW_DATA;
  }

  private void reset()
  {
    _plates = null;
    _readoutTypes.setValue(null);
    _replicates = null;
    _conditions = null;
    _comments = null;
    _result = null;
  }

  public Result getResult()
  {
    return _result;
  }

  @UICommand
  public String transform() throws IOException
  {
    File outputFile = File.createTempFile(getScreen().getFacilityId(), ".xls");
    PlateOrdering ordering = new PlateOrdering();
    List<String> plateNumbersTokens = Lists.newArrayList(getPlates().split("\\W+"));
    List plateNumbers = Lists.newArrayList(Iterables.transform(plateNumbersTokens, new Function<String,Integer>() {
      public Integer apply(String s)
      {
        return Integer.valueOf(s);
      }
    }));
    ordering.addPlates(plateNumbers);
    ordering.addReplicates(getReplicates());
    String[] conditions = getConditions().split("\\n");
    ordering.addConditions(Lists.newArrayList(conditions));
    List readoutTypes = Lists.newArrayList(AssayReadoutType.FP);
    ordering.addReadoutTypes(readoutTypes);
    _result = _transformer.transform(new InputStreamReader(_uploadedFile.getInputStream()),
                           outputFile,
                           getScreen(),
                           DEFAULT_PLATE_SIZE,
                           ordering,
                           Maps.<WellName,AssayWellControlType>newHashMap());
    JSFUtils.handleUserFileDownloadRequest(getFacesContext(), outputFile, Workbook.MIME_TYPE);
    return REDISPLAY_PAGE_ACTION_RESULT;
  }

  public boolean getHasErrors()
  {
    return _lastParseErrors != null && !_lastParseErrors.isEmpty();
  }

  public DataModel getImportErrors()
  {
    return new ListDataModel(_lastParseErrors);
  }
}

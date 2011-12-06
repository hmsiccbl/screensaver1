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
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.SequenceInputStream;
import java.io.StringReader;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.regex.Pattern;

import javax.faces.model.DataModel;
import javax.faces.model.ListDataModel;

import com.google.common.base.Joiner;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;
import org.apache.myfaces.custom.fileupload.UploadedFile;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.annotate.JsonFilter;
import org.codehaus.jackson.map.ser.FilterProvider;
import org.codehaus.jackson.map.ser.impl.SimpleBeanPropertyFilter;
import org.codehaus.jackson.map.ser.impl.SimpleFilterProvider;
import scala.util.parsing.combinator.Parsers.ParseResult;

import edu.harvard.med.iccbl.platereader.parser.CompositePlateOrdering;
import edu.harvard.med.iccbl.platereader.parser.SimplePlateOrdering;
import edu.harvard.med.screensaver.ScreensaverConstants;
import edu.harvard.med.screensaver.db.GenericEntityDAO;
import edu.harvard.med.screensaver.io.ParseError;
import edu.harvard.med.screensaver.io.libraries.PlateNumbersParser;
import edu.harvard.med.screensaver.io.libraries.PlateWellNamesParser;
import edu.harvard.med.screensaver.io.workbook2.Workbook;
import edu.harvard.med.screensaver.model.activities.AdministrativeActivityType;
import edu.harvard.med.screensaver.model.libraries.WellName;
import edu.harvard.med.screensaver.model.screenresults.AssayWellControlType;
import edu.harvard.med.screensaver.model.screens.AssayReadoutType;
import edu.harvard.med.screensaver.model.screens.Screen;
import edu.harvard.med.screensaver.model.users.AdministratorUser;
import edu.harvard.med.screensaver.service.screenresult.PlateReaderRawDataTransformer.Result;
import edu.harvard.med.screensaver.ui.arch.util.JSFUtils;
import edu.harvard.med.screensaver.ui.arch.util.UISelectOneBean;
import edu.harvard.med.screensaver.ui.arch.view.AbstractBackingBean;
import edu.harvard.med.screensaver.ui.arch.view.aspects.UICommand;
import edu.harvard.med.screensaver.ui.screens.ScreenViewer;
import edu.harvard.med.screensaver.util.DevelopmentException;
import edu.harvard.med.screensaver.util.NullSafeUtils;
import edu.harvard.med.screensaver.util.StringUtils;

public class PlateReaderRawDataTransformer extends AbstractBackingBean
{
  private static final Logger log = Logger.getLogger(PlateReaderRawDataTransformer.class);
  private static final String ACTIVITY_INPUT_PARAMS_DELIMITER = ";";

  public enum OutputFormat {
    PlatePerWorksheet("Plate per worksheet"),
    AllPlatesInSingleWorksheet("All plates in single worksheet");
    
    private String _displayText;
    
    public String getDisplayText()
    {
      return _displayText;
    }

    private OutputFormat(String displayText) { _displayText = displayText; }
  }

  @JsonFilter("savedForm1Properties")
  // TODO: rename to PlatesAndControlsInput
  public static class FormOne
  {
    private String _plates;
    private String _outputFileName;
    private String _negativeControls;
    private String _positiveControls;
    private String _otherControls;
    private transient DataModel _controlWellsModel;
    private transient PlateWellNamesParser _wellNamesParser = new PlateWellNamesParser(ScreensaverConstants.DEFAULT_PLATE_SIZE);
    private UISelectOneBean<OutputFormat> _outputFormat = new UISelectOneBean<OutputFormat>(Lists.newArrayList(OutputFormat.values())) {
      @Override
      protected String makeLabel(OutputFormat t)
      {
        return t.getDisplayText();
      }
    };

    public FormOne()
    {}

    public FormOne(String serialized) throws IOException
    {
      FormOne tmp = deserialize(serialized);
      _plates = tmp._plates;
      _outputFileName = tmp._outputFileName;
      _outputFormat.setSelection(tmp._outputFormat.getSelection());
      _negativeControls = tmp._negativeControls;
      _positiveControls = tmp._positiveControls;
      _otherControls = tmp._otherControls;
    }

    public String getPlates()
    {
      return _plates;
    }

    public void setPlates(String plates)
    {
      _plates = plates;
    }

    public String getOutputFileName()
    {
      return _outputFileName;
    }

    public void setOutputFileName(String outputFileName)
    {
      _outputFileName = outputFileName;
    }

    public UISelectOneBean<OutputFormat> getOutputFormat()
    {
      return _outputFormat;
    }

    public void setOutputFormat(UISelectOneBean<OutputFormat> outputFormat)
    {
      _outputFormat = outputFormat;
    }

    /**
     * @return
     * @motivation for serialization
     */
    public OutputFormat getOutputFormatSelection()
    {
      return _outputFormat.getSelection();
    }

    /** @motivation for serialization */
    public void setOutputFormatSelection(OutputFormat of)
    {
      _outputFormat.setSelection(of);
    }

    public String getNegativeControls()
    {
      return _negativeControls;
    }

    public void setNegativeControls(String negativeControls)
    {
      if (!NullSafeUtils.nullSafeEquals(_negativeControls, negativeControls)) {
        _controlWellsModel = null; // reset
      }
      _negativeControls = negativeControls;
    }

    public String getPositiveControls()
    {
      return _positiveControls;
    }

    public void setPositiveControls(String positiveControls)
    {
      if (!NullSafeUtils.nullSafeEquals(_positiveControls, positiveControls)) {
        _controlWellsModel = null; // reset
      }
      _positiveControls = positiveControls;
    }

    public void setOtherControls(String otherControls)
    {
      _otherControls = otherControls;
    }

    public String getOtherControls()
    {
      return _otherControls;
    }

    public DataModel getControlWellsModel()
    {
      if (_controlWellsModel == null) {
        HashSet<WellName> negControls = Sets.newHashSet(_wellNamesParser.parse_Java(NullSafeUtils.value(getNegativeControls(), "")));
        HashSet<WellName> posControls = Sets.newHashSet(_wellNamesParser.parse_Java(NullSafeUtils.value(getPositiveControls(), "")));
        HashSet<WellName> otherControls = Sets.newHashSet(_wellNamesParser.parse_Java(NullSafeUtils.value(getOtherControls(), "")));
        List<Map<String,AssayWellControlType>> model = Lists.newArrayList();
        for (int r = 0; r < ScreensaverConstants.DEFAULT_PLATE_SIZE.getRows(); ++r) {
          Map<String,AssayWellControlType> row = Maps.newHashMap();
          model.add(row);
          for (int c = 0; c < ScreensaverConstants.DEFAULT_PLATE_SIZE.getColumns(); ++c) {
            WellName wellName = new WellName(r, c);
            if (negControls.contains(wellName)) {
              row.put(wellName.getColumnLabel(), AssayWellControlType.ASSAY_CONTROL);
            }
            else if (posControls.contains(wellName)) {
              row.put(wellName.getColumnLabel(), AssayWellControlType.ASSAY_POSITIVE_CONTROL);
            }
            else if (otherControls.contains(wellName)) {
              row.put(wellName.getColumnLabel(), AssayWellControlType.OTHER_CONTROL);
            }
            else {
              row.put(wellName.getColumnLabel(), null);
            }
          }
        }
        _controlWellsModel = new ListDataModel(model);
      }
      return _controlWellsModel;
    }

    public void invalidateControlWellsModel()
    {
      _controlWellsModel = null;
    }

    private Map<WellName,AssayWellControlType> parseControls()
    {
      HashMap<WellName,AssayWellControlType> controls = Maps.<WellName,AssayWellControlType>newHashMap();
      _wellNamesParser = new PlateWellNamesParser(ScreensaverConstants.DEFAULT_PLATE_SIZE);
      for (WellName wellName : _wellNamesParser.parse_Java(getNegativeControls())) {
        controls.put(wellName, AssayWellControlType.ASSAY_CONTROL);
      }
      for (WellName wellName : _wellNamesParser.parse_Java(getPositiveControls())) {
        controls.put(wellName, AssayWellControlType.ASSAY_POSITIVE_CONTROL);
      }
      for (WellName wellName : _wellNamesParser.parse_Java(getOtherControls())) {
        controls.put(wellName, AssayWellControlType.OTHER_CONTROL);
      }
      return controls;
    }

    public boolean validate(PlateReaderRawDataTransformer parent)
    {
      boolean result = true;
      ParseResult plateParseResult = PlateNumbersParser.validate_Java(getPlates());
      if (!plateParseResult.successful()) {
        parent.showMessage("invalidUserInput", "Plates", plateParseResult);
        result = false;
      }
      ParseResult parseResult1 = _wellNamesParser.validate_Java(getNegativeControls());
      if (!parseResult1.successful()) {
        parent.showMessage("invalidUserInput", "Negative Control Wells", parseResult1);
        result = false;
      }
      ParseResult parseResult2 = _wellNamesParser.validate_Java(getPositiveControls());
      if (!parseResult2.successful()) {
        parent.showMessage("invalidUserInput", "Positive Control Wells", parseResult2);
        result = false;
      }
      ParseResult parseResult3 = _wellNamesParser.validate_Java(getOtherControls());
      if (!parseResult3.successful()) {
        parent.showMessage("invalidUserInput", "Other Control Wells", parseResult3);
        result = false;
      }
      HashSet<WellName> negControls = Sets.newHashSet(_wellNamesParser.parse_Java(getNegativeControls()));
      HashSet<WellName> posControls = Sets.newHashSet(_wellNamesParser.parse_Java(getPositiveControls()));
      HashSet<WellName> otherControls = Sets.newHashSet(_wellNamesParser.parse_Java(getOtherControls()));
      Iterable<WellName> nonExtantWellNames = Iterables.filter(Sets.union(negControls, posControls), WellName.makeIsNonExtantWellNamePredicate(ScreensaverConstants.DEFAULT_PLATE_SIZE));
      if (nonExtantWellNames.iterator().hasNext()) {
        parent.showMessage("invalidUserInput", "Control Wells", "invalid well names " + Joiner.on(", ").join(nonExtantWellNames));
        result = false;
      }
      if (Sets.union(Sets.union(negControls, posControls), otherControls).size() < negControls.size() + posControls.size() + otherControls.size()) {
        parent.showMessage("invalidUserInput", "Control Wells", "a well can only be used as one type of control");
        result = false;
      }

      return result;
    }

    /** @motivation for control wells plate map UI dataTable */
    public List<String> getPlateRowLabels()
    {
      return ScreensaverConstants.DEFAULT_PLATE_SIZE.getRowsLabels();
    }

    /** @motivation for control wells plate map UI dataTable */
    public DataModel getPlateColumns()
    {
      return new ListDataModel(ScreensaverConstants.DEFAULT_PLATE_SIZE.getColumnsLabels());
    }

    /**
     * Serialize to a JSON representation using {@link ObjectMapper#writeValue(java.io.Writer, Object)}
     * 
     * @return a JSON representation
     * @throws IOException
     */
    public String serialize() throws IOException
    {
      ObjectMapper mapper = new ObjectMapper();
      FilterProvider filters = new SimpleFilterProvider().addFilter("savedForm1Properties",
                                                                    SimpleBeanPropertyFilter.filterOutAllExcept("plates",
                                                                                                                "outputFileName",
                                                                                                                "outputFormatSelection",
                                                                                                                "positiveControls",
                                                                                                                "negativeControls",
                                                                                                                "otherControls"));
      return mapper.writer(filters).writeValueAsString(this);
    }

    /**
     * deSerialize from a JSON representation using {@link ObjectMapper}
     * 
     * @return the deserialized InputFileParams
     */
    private FormOne deserialize(String input) throws IOException
    {
      ObjectMapper mapper = new ObjectMapper();
      FormOne params = mapper.readValue(input.getBytes(), FormOne.class);
      return params;
    }
  }

  @JsonFilter("savedForm2Properties")
  public static class InputFileParams
  {
    private UploadedFile _uploadedFile;
    private UISelectOneBean<AssayReadoutType> _readoutType =
      new UISelectOneBean<AssayReadoutType>(Lists.newArrayList(AssayReadoutType.values()), true) {
      protected String getEmptyLabel()
      {
        return "<select>";
      }
    };

    private String _conditions;
    private Integer _replicates;
    private String _readouts;
    private UISelectOneBean<CollationOrder> _collationOrder =
      new UISelectOneBean<CollationOrder>(ImmutableList.of(new CollationOrder(ImmutableList.of(PlateOrderingGroup.Plates, PlateOrderingGroup.Conditions, PlateOrderingGroup.Replicates, PlateOrderingGroup.Readouts)),
                                                           new CollationOrder(ImmutableList.of(PlateOrderingGroup.Plates, PlateOrderingGroup.Replicates, PlateOrderingGroup.Conditions, PlateOrderingGroup.Readouts)),
                                                           new CollationOrder(ImmutableList.of(PlateOrderingGroup.Conditions, PlateOrderingGroup.Plates, PlateOrderingGroup.Replicates, PlateOrderingGroup.Readouts)),
                                                           new CollationOrder(ImmutableList.of(PlateOrderingGroup.Conditions, PlateOrderingGroup.Replicates, PlateOrderingGroup.Plates, PlateOrderingGroup.Readouts)),
                                                           new CollationOrder(ImmutableList.of(PlateOrderingGroup.Replicates, PlateOrderingGroup.Plates, PlateOrderingGroup.Conditions, PlateOrderingGroup.Readouts)),
                                                           new CollationOrder(ImmutableList.of(PlateOrderingGroup.Replicates, PlateOrderingGroup.Conditions, PlateOrderingGroup.Plates, PlateOrderingGroup.Readouts))));
    private Integer _expectedPlateMatrixCount;
    private Integer _uploadedPlateMatrixCount;

    public InputFileParams()
    {}

    public InputFileParams(InputFileParams from)
    {
      _readoutType.setSelection(from._readoutType.getSelection());
      _conditions = from._conditions;
      _replicates = from._replicates;
      _readouts = from._readouts;
      _collationOrder.setSelection(from._collationOrder.getSelection());
    }

    public void setUploadedFile(UploadedFile uploadedFile)
    {
      // note: only replace the previously uploaded file, if user specified a new file (since multiple page reloads may occur before the transformation operation is invoked)
      if (uploadedFile != null) {
        log.info("uploaded new file " + _uploadedFile);
        _uploadedFile = uploadedFile;
      }
    }

    public UploadedFile getUploadedFile()
    {
      return _uploadedFile;
    }

    /** @motivation for serialization */
    public String getUploadedFilename()
    {
      return getUploadedFile() == null ? "" : getUploadedFile().getName();
    }

    /** @motivation for serialization */
    public void setUploadedFilename(String name)
    {
      // no-op - user has to select the filename manually
      log.info("saved filename: " + name);
    }

    public boolean isFileUploaded()
    {
      return _uploadedFile != null && _uploadedFile.getSize() > 0;
    }

    public UISelectOneBean<AssayReadoutType> getReadoutType()
    {
      return _readoutType;
    }

    /** @motivation for serialization */
    public AssayReadoutType getReadoutTypeSelection()
    {
      return getReadoutType().getSelection();
    }

    /** @motivation for serialization */
    public void setReadoutTypeSelection(AssayReadoutType art)
    {
      getReadoutType().setSelection(art);
    }

    public String getConditions()
    {
      return _conditions;
    }

    public List<String> getParsedConditions() throws IOException
    {
      return parseStrings(getConditions());
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

    public String getReadouts()
    {
      return _readouts;
    }

    public List<String> getParsedReadouts() throws IOException
    {
      return parseStrings(getReadouts());
    }

    public void setReadouts(String readouts)
    {
      _readouts = readouts;
    }

    public UISelectOneBean<CollationOrder> getCollationOrder()
    {
      return _collationOrder;
    }

    /** @motivation for serialization */
    public List<PlateOrderingGroup> getCollationOrderOrdering()
    {
      return getCollationOrder().getSelection().getOrdering();
    }

    /** @motivation for serialization */
    public void setCollationOrderOrdering(List<PlateOrderingGroup> ordering)
    {
      _collationOrder.setSelection(new CollationOrder(ordering));
    }

    public Integer getExpectedPlateMatrixCount()
    {
      return _expectedPlateMatrixCount;
    }

    public void setExpectedPlateMatrixCount(int size)
    {
      _expectedPlateMatrixCount = size;
    }

    public Integer getUploadedPlateMatrixCount()
    {
      return _uploadedPlateMatrixCount;
    }

    public void setUploadedPlateMatrixCount(Integer uploadedPlateMatrixCount)
    {
      _uploadedPlateMatrixCount = uploadedPlateMatrixCount;
    }

    /**
     * Serialize to a JSON representation using {@link ObjectMapper#writeValue(java.io.Writer, Object)}
     * 
     * @return a JSON representation
     * @throws IOException
     */
    public String serialize() throws IOException
    {
      ObjectMapper mapper = new ObjectMapper();
      FilterProvider filters = new SimpleFilterProvider().addFilter("savedForm2Properties",
                                                                    SimpleBeanPropertyFilter.filterOutAllExcept("uploadedFilename",
                                                                                                                "conditions",
                                                                                                                "replicates",
                                                                                                                "readoutTypeSelection",
                                                                                                                "readouts",
                                                                                                                "collationOrderOrdering"));
      return mapper.writer(filters).writeValueAsString(this);
    }

    /**
     * deSerialize from a JSON representation using {@link ObjectMapper}
     * 
     * @return the deserialized InputFileParams
     */
    public static InputFileParams deserialize(String input) throws IOException
    {
      ObjectMapper mapper = new ObjectMapper();
      InputFileParams params = mapper.readValue(input.getBytes(), InputFileParams.class);
      return params;
    }

    /**
     * Parse out strings, delimited by newlines or commas, trimming surrounding whitespace from each token (but not
     * internal whitespace)
     */
    private List<String> parseStrings(String input)
    {
      if (StringUtils.isEmpty(input)) {
        return ImmutableList.of();
      }
      Scanner scanner = new Scanner(input).useDelimiter("(\\n|,)+");
      List<String> result = Lists.newArrayList();
      while (scanner.hasNext()) {
        result.add(scanner.next().trim());
      }
      return result;
    }
  }

  private GenericEntityDAO _dao;
  private ScreenViewer _screenViewer;
  private edu.harvard.med.screensaver.service.screenresult.PlateReaderRawDataTransformer _transformer;

  private FormOne _formOne = new FormOne();
  private List<InputFileParams> _inputFiles = Lists.newArrayList();
  private Result _result;
  private String _comments;
  private List<? extends ParseError> _lastParseErrors;

  /**
   * @motivation for CGLIB2
   */
  protected PlateReaderRawDataTransformer()
  {
  }

  public PlateReaderRawDataTransformer(GenericEntityDAO dao, 
                                       ScreenViewer screenViewer,
                                       edu.harvard.med.screensaver.service.screenresult.PlateReaderRawDataTransformer transformer)
  {
    _dao = dao;
    _screenViewer = screenViewer;
    _transformer = transformer;
  }

  public Screen getScreen()
  {
    return _screenViewer.getEntity();
  }

  public String getComments()
  {
    return _comments;
  }

  public void setComments(String comments)
  {
    _comments = comments;
  }

  public List<InputFileParams> getInputFiles()
  {
    return _inputFiles;
  }

  public void setInputFiles(List<InputFileParams> inputFiles)
  {
    _inputFiles = inputFiles;
  }

  @UICommand
  public String cancel()
  {
    return _screenViewer.reload();
  }

  @UICommand
  public String view()
  {
    reset();
    restoreInputParams();
    return TRANSFORM_PLATE_READER_RAW_DATA;
  }

  private void restoreInputParams()
  {
    if (!getScreen().getUpdateActivitiesOfType(AdministrativeActivityType.PLATE_RAW_DATA_TRANSFORMATION).isEmpty()) {
      String comments = NullSafeUtils.value(getScreen().getUpdateActivitiesOfType(AdministrativeActivityType.PLATE_RAW_DATA_TRANSFORMATION).last().getComments(), "");
      String[] parts = NullSafeUtils.value(comments.split(ACTIVITY_INPUT_PARAMS_DELIMITER), new String[] {}); 
      if (parts.length > 0) {
        try {
          setFormOne(new FormOne(parts[0]));

          List<InputFileParams> inputFileParams = Lists.newArrayList();
          for (int i = 1; i < parts.length; i++) {
            String s = parts[i];
            InputFileParams ifps = InputFileParams.deserialize(s);
            log.debug("restoring saved inputFileParams: " + ifps.serialize());
            inputFileParams.add(ifps);
          }
          setInputFiles(inputFileParams);
        }
        catch (Exception e) {
          showMessage("screens.restoreInputParametersFailed", e.getMessage(), comments);
        }          
      }
    }
  }

  private void reset()
  {
    _formOne = new FormOne();
    _inputFiles = Lists.newArrayList(new InputFileParams());
    _comments = null;
    _result = null;
  }

  @UICommand
  public String addInputFile()

  {
    _inputFiles.add(new InputFileParams(Iterables.getLast(_inputFiles)));
    return REDISPLAY_PAGE_ACTION_RESULT;
  }
  
  @UICommand
  public String deleteInputFile()
  {
    _inputFiles.remove(getRequestMap().get("inputFile"));
    return REDISPLAY_PAGE_ACTION_RESULT;
  }

  public Result getResult()
  {
    return _result;
  }

  @UICommand
  public String transform() throws IOException
  {
    _result = null;
    getFormOne().invalidateControlWellsModel();
    if (!validate()) {
      return REDISPLAY_PAGE_ACTION_RESULT;
    }

    InputStream multiFileInputStream = null;
    CompositePlateOrdering ordering = new CompositePlateOrdering();
    for (InputFileParams inputFile : getInputFiles()) {
      SimplePlateOrdering simplePlateOrdering = buildSimplePlateOrdering(inputFile);
      ordering.add(simplePlateOrdering);
      if (multiFileInputStream == null ) {
        multiFileInputStream = inputFile.getUploadedFile().getInputStream();
      } 
      else {
        multiFileInputStream = new SequenceInputStream(multiFileInputStream, inputFile.getUploadedFile().getInputStream());
      }
      // TODO: we're duplicating effort here with the below transform() call, simply to get the expected/actual number of plate matrices uploaded; perhaps this can be optimized
      Result result = _transformer.transform(new InputStreamReader(inputFile.getUploadedFile().getInputStream()),
                                             null,
                                             true,
                                             getScreen(),
                                             DEFAULT_PLATE_SIZE,
                                             simplePlateOrdering,
                                             getFormOne().parseControls());
      inputFile.setExpectedPlateMatrixCount(simplePlateOrdering.iterator().size());
      inputFile.setUploadedPlateMatrixCount(result.getPlateMatricesProcessedCount());
    }

    if (StringUtils.isEmpty(getFormOne().getOutputFileName())) {
      getFormOne().setOutputFileName(makeOutputFileName());
    }
    File outputFile = File.createTempFile(getScreen().getFacilityId(), ".xls");
    _result = _transformer.transform(new InputStreamReader(multiFileInputStream),
                                     outputFile,
                                     getFormOne().getOutputFormat().getSelection() == OutputFormat.PlatePerWorksheet,
                                     getScreen(),
                                     DEFAULT_PLATE_SIZE,
                                     ordering,
                                     getFormOne().parseControls());

    showMessage("screens.rawDataTranformationResult",
                _result.plateMatricesProcessedCount(), ordering.iterator().size(),
                _result.platesProcessedCount(), PlateNumbersParser.parse_Java(getFormOne().getPlates()).size());

    return REDISPLAY_PAGE_ACTION_RESULT;
  }

  private String makeOutputFileName()
  {
    return "screen" + getScreen().getFacilityId() + "-" + getFormOne().getPlates().replaceAll("\\s+", "_") + ".xls";
  }

  private boolean validate()
  {
    return getFormOne().validate(this);
  }

  @UICommand
  public String download() throws IOException
  {
    if (_result != null && _result.plateMatricesProcessedCount() > 0) {
      JSFUtils.handleUserFileDownloadRequest(getFacesContext(), _result.outputFile(), getFormOne().getOutputFileName(), Workbook.MIME_TYPE);
      recordActivities();
    }
    return REDISPLAY_PAGE_ACTION_RESULT;
  }

  private void recordActivities()
  {
    // create two Administrative Activities, one for the comment, one for the settings
    if (!StringUtils.isEmpty(getComments())) {
      getScreen().createUpdateActivity(AdministrativeActivityType.COMMENT,
                                       (AdministratorUser) getCurrentScreensaverUser().getScreensaverUser(),
                                       getComments());
    }

    // serialize and save the InputFileParams as a JSON string appended to the comment of the Activity
    try {
      StringBuilder activityComment = new StringBuilder();
      activityComment.append(getFormOne().serialize());
      for (InputFileParams inputFile : getInputFiles()) {
        activityComment.append(ACTIVITY_INPUT_PARAMS_DELIMITER);
        activityComment.append(inputFile.serialize());
      }
      getScreen().createUpdateActivity(AdministrativeActivityType.PLATE_RAW_DATA_TRANSFORMATION, 
                                       (AdministratorUser) getCurrentScreensaverUser().getScreensaverUser(), activityComment.toString());
    }
    catch (Exception e) {
      // TODO: show user message instead
      log.warn("unable to save the parameters", e);
    }
    _dao.mergeEntity(getScreen());
    _screenViewer.reload();
  }

  public SimplePlateOrdering buildSimplePlateOrdering(InputFileParams inputFile) throws IOException
  {
    SimplePlateOrdering filePlateOrdering = new SimplePlateOrdering();
    filePlateOrdering.addReadoutTypes(ImmutableList.of(inputFile.getReadoutType().getSelection()));
    for (PlateOrderingGroup plateOrderingGroup : inputFile.getCollationOrder().getSelection()) {
      addPlateOrderingGroup(filePlateOrdering, plateOrderingGroup, inputFile);
    }
    return filePlateOrdering;
  }

  private void addPlateOrderingGroup(SimplePlateOrdering ordering,
                                     PlateOrderingGroup plateOrderingGroup, 
                                     InputFileParams inputFile) throws IOException
  {
    switch(plateOrderingGroup) {
      case Plates:
        ordering.addPlates((List) PlateNumbersParser.parse_Java(getFormOne().getPlates()));
        break;
      case Conditions:
        if (!inputFile.getParsedConditions().isEmpty()) {
          ordering.addConditions(inputFile.getParsedConditions());
        }
        break;
      case Replicates:
        if (inputFile.getReplicates() != null) {
          ordering.addReplicates(inputFile.getReplicates());
        }
        break;
      case Readouts:
        if (!inputFile.getParsedReadouts().isEmpty()) {
          ordering.addReadouts(inputFile.getParsedReadouts());
        }
        break;
      default: 
        throw new DevelopmentException("unhandled " + PlateOrderingGroup.class.getSimpleName() + ": " + plateOrderingGroup);
    }
  }

  public boolean getHasErrors()
  {
    return _lastParseErrors != null && !_lastParseErrors.isEmpty();
  }

  public DataModel getImportErrors()
  {
    return new ListDataModel(_lastParseErrors);
  }

  public void setFormOne(FormOne formOne)
  {
    _formOne = formOne;
  }

  public FormOne getFormOne()
  {
    return _formOne;
  }
}
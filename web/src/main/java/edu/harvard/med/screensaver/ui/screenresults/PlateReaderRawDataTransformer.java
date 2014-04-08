// $HeadURL$
// $Id$
//
// Copyright © 2006, 2010, 2011, 2012 by the President and Fellows of Harvard College.
//
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.ui.screenresults;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.SequenceInputStream;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;
import java.util.SortedMap;

import javax.faces.model.DataModel;
import javax.faces.model.ListDataModel;

import jxl.write.WritableSheet;
import jxl.write.WriteException;
import jxl.write.biff.RowsExceededException;

import org.apache.log4j.Logger;
import org.apache.myfaces.custom.fileupload.UploadedFile;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.annotate.JsonFilter;
import org.codehaus.jackson.map.ser.FilterProvider;
import org.codehaus.jackson.map.ser.impl.SimpleBeanPropertyFilter;
import org.codehaus.jackson.map.ser.impl.SimpleFilterProvider;
import org.springframework.transaction.annotation.Transactional;

import com.google.common.base.Joiner;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

import edu.harvard.med.screensaver.ScreensaverConstants;
import edu.harvard.med.screensaver.db.GenericEntityDAO;
import edu.harvard.med.screensaver.db.LibrariesDAO;
import edu.harvard.med.screensaver.io.ParseError;
import edu.harvard.med.screensaver.io.workbook2.Workbook;
import edu.harvard.med.screensaver.model.activities.AdministrativeActivityType;
import edu.harvard.med.screensaver.model.cherrypicks.CherryPickRequest;
import edu.harvard.med.screensaver.model.cherrypicks.LabCherryPick;
import edu.harvard.med.screensaver.model.cherrypicks.ScreenerCherryPick;
import edu.harvard.med.screensaver.model.libraries.Gene;
import edu.harvard.med.screensaver.model.libraries.LibraryWellType;
import edu.harvard.med.screensaver.model.libraries.PlateSize;
import edu.harvard.med.screensaver.model.libraries.SilencingReagent;
import edu.harvard.med.screensaver.model.libraries.Well;
import edu.harvard.med.screensaver.model.libraries.WellKey;
import edu.harvard.med.screensaver.model.libraries.WellName;
import edu.harvard.med.screensaver.model.screenresults.AssayWellControlType;
import edu.harvard.med.screensaver.model.screens.AssayReadoutType;
import edu.harvard.med.screensaver.model.screens.Screen;
import edu.harvard.med.screensaver.model.screens.ScreenType;
import edu.harvard.med.screensaver.model.users.AdministratorUser;
import edu.harvard.med.screensaver.ui.arch.util.JSFUtils;
import edu.harvard.med.screensaver.ui.arch.util.UISelectOneBean;
import edu.harvard.med.screensaver.ui.arch.view.AbstractBackingBean;
import edu.harvard.med.screensaver.ui.arch.view.aspects.UICommand;
import edu.harvard.med.screensaver.ui.cherrypickrequests.CherryPickRequestViewer;
import edu.harvard.med.screensaver.ui.screenresults.PlateReaderRawDataParser.MatrixOrder;
import edu.harvard.med.screensaver.ui.screenresults.PlateReaderRawDataParser.MatrixOrder1536;
import edu.harvard.med.screensaver.ui.screenresults.PlateReaderRawDataParser.MatrixOrderPattern;
import edu.harvard.med.screensaver.ui.screens.ScreenViewer;
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
    private UISelectOneBean<PlateSize> _plateSize = new UISelectOneBean<PlateSize>(Lists.newArrayList(PlateSize.values()), ScreensaverConstants.DEFAULT_PLATE_SIZE) {
      @Override
      protected String makeLabel(PlateSize p)
      {
        return p.getValue().toString();
      }
    };
    private UISelectOneBean<PlateSize> _libraryPlateSize = new UISelectOneBean<PlateSize>(Lists.newArrayList(PlateSize.values()), ScreensaverConstants.DEFAULT_PLATE_SIZE) {
      @Override
      protected String makeLabel(PlateSize p)
      {
        return p.getValue().toString();
      }
    };
    private String _outputFileName;
    private String _assayNegativeControls;
    private String _assayPositiveControls;
    private String _assayOtherControls;
    private String _libraryControls;
    private transient DataModel _assayControlWellsModel;
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
      _plateSize.setSelection(tmp._plateSize.getSelection());
      _libraryPlateSize.setSelection(tmp._libraryPlateSize.getSelection());
      _outputFileName = tmp._outputFileName;
      _outputFormat.setSelection(tmp._outputFormat.getSelection());
      _assayNegativeControls = tmp._assayNegativeControls;
      _assayPositiveControls = tmp._assayPositiveControls;
      _assayOtherControls = tmp._assayOtherControls;
      _libraryControls = tmp._libraryControls;
    }

    public String getPlates()
    {
      return _plates;
    }

    public void setPlates(String plates)
    {
      _plates = plates;
    }

    public UISelectOneBean<PlateSize> getAssayPlateSizeSelections()
    {
      return _plateSize;
    }

    /**
     * @motivation for serialization
     */
    public PlateSize getAssayPlateSize()
    {
      return _plateSize.getSelection();
    }

    /**
     * @motivation for deserialization
     */
    public void setAssayPlateSize(PlateSize assayPlateSize)
    {
      _plateSize.setSelection(assayPlateSize);
    }

    public UISelectOneBean<PlateSize> getLibraryPlateSizeSelections()
    {
      return _libraryPlateSize;
    }

    /**
     * @motivation for serialization
     */
    public PlateSize getLibraryPlateSize()
    {
    //  return _libraryPlateSize.getSelection();
      return DEFAULT_PLATE_SIZE;
    }

    /**
     * @motivation for deserialization
     */
    public void setLibraryPlateSize(PlateSize value)
    {
      _libraryPlateSize.setSelection(value);
    }

    public String getOutputFileName()
    {
      return _outputFileName;
    }

    public void setOutputFileName(String outputFileName)
    {
      _outputFileName = outputFileName;
    }

    public UISelectOneBean<OutputFormat> getOutputFormatSelections()
    {
      return _outputFormat;
    }

    /**
     * @motivation for serialization
     */
    public OutputFormat getOutputFormat()
    {
      return _outputFormat.getSelection();
    }

    /** @motivation for deserialization */
    public void setOutputFormat(OutputFormat of)
    {
      _outputFormat.setSelection(of);
    }

    public String getAssayNegativeControls()
    {
      return _assayNegativeControls;
    }

    public void setAssayNegativeControls(String negativeControls)
    {
      if (!NullSafeUtils.nullSafeEquals(_assayNegativeControls, negativeControls)) {
        _assayControlWellsModel = null; // reset
      }
      _assayNegativeControls = negativeControls;
    }

    public String getAssayPositiveControls()
    {
      return _assayPositiveControls;
    }

    public void setAssayPositiveControls(String positiveControls)
    {
      if (!NullSafeUtils.nullSafeEquals(_assayPositiveControls, positiveControls)) {
        _assayControlWellsModel = null; // reset
      }
      _assayPositiveControls = positiveControls;
    }

    public void setAssayOtherControls(String otherControls)
    {
      _assayOtherControls = otherControls;
    }

    public String getAssayOtherControls()
    {
      return _assayOtherControls;
    }

    public String getLibraryControls()
    {
      return _libraryControls;
    }

    public void setLibraryControls(String libraryControls)
    {
      _libraryControls = libraryControls;
    }

    public DataModel getAssayControlWellsModel()
    {
      if (_assayControlWellsModel == null) {
      	Map<WellName, AssayWellControlType> controls = parseAssayControls();
        List<Map<String,AssayWellControlType>> model = Lists.newArrayList();
        for (int r = 0; r < getAssayPlateSize().getRows(); ++r) {
          Map<String,AssayWellControlType> row = Maps.newHashMap();
          model.add(row);
          for (int c = 0; c < getAssayPlateSize().getColumns(); ++c) {
            WellName wellName = new WellName(r, c);
            AssayWellControlType type = controls.get(wellName);
            if (type != null) {
              row.put(wellName.getColumnLabel(), type);
            }
          }
        }
        _assayControlWellsModel = new ListDataModel(model);
      }
      return _assayControlWellsModel;
    }

    public void invalidateAssayControlWellsModel()
    {
      _assayControlWellsModel = null;
    }

    private Map<WellName, String> parseControlLabels()
    {
    	//for the labels, don't care what type they are
    	Map<WellName,String> labels = Maps.newHashMap();
    	Map<String,Set<WellName>> labelledControlSet = PlateReaderRawDataParser.expandNamedWellRanges(getAssayPositiveControls(), getAssayPlateSize().getWellCount());
    	for(String label:labelledControlSet.keySet()) {
    		for(WellName wellName:labelledControlSet.get(label)) {
    			labels.put(wellName,label);
    		}
    	}
    	labelledControlSet = PlateReaderRawDataParser.expandNamedWellRanges(getAssayNegativeControls(), getAssayPlateSize().getWellCount());
    	for(String label:labelledControlSet.keySet()) {
    		for(WellName wellName:labelledControlSet.get(label)) {
    			labels.put(wellName,label);
    		}
    	}
    	labelledControlSet = PlateReaderRawDataParser.expandNamedWellRanges(getAssayOtherControls(), getAssayPlateSize().getWellCount());
    	for(String label:labelledControlSet.keySet()) {
    		for(WellName wellName:labelledControlSet.get(label)) {
    			labels.put(wellName,label);
    		}
    	}    	

    	return labels;
    } 
    
	  private Map<WellName, String> parseLibraryControlLabels()
	  {
    	Map<WellName,String> labels = Maps.newHashMap();
    	Map<String,Set<WellName>> labelledControlSet = PlateReaderRawDataParser.expandNamedWellRanges(getLibraryControls(), getLibraryPlateSize().getWellCount());
	  	for(String label:labelledControlSet.keySet()) {
	  		for(WellName wellName:labelledControlSet.get(label)) {
	  			labels.put(wellName,label);
	  		}
	  	}
	
	  	return labels;
	  } 
  
    private SortedMap<WellName, AssayWellControlType> parseAssayControls()
    {
    	SortedMap<WellName,AssayWellControlType> mapping = Maps.newTreeMap();
//    	
//    	Set<WellName> positiveControls = PlateReaderRawDataParser.expandWellRange(getAssayPositiveControls(),getAssayPlateSize().getWellCount());
//    	Set<WellName> negativeControls = PlateReaderRawDataParser.expandWellRange(getAssayNegativeControls(),getAssayPlateSize().getWellCount());
//    	Set<WellName> otherControls = PlateReaderRawDataParser.expandWellRange(getAssayOtherControls(),getAssayPlateSize().getWellCount());
//    	
//    	if( !Sets.intersection(positiveControls, negativeControls).isEmpty() ||
//    			!Sets.intersection(positiveControls, otherControls).isEmpty() ||
//    			!Sets.intersection(negativeControls, otherControls).isEmpty() ) 
//    		throw new IllegalArgumentException("Control wells are repeated");
//    	
//    	for(WellName wellName:positiveControls) mapping.put(wellName, AssayWellControlType.ASSAY_POSITIVE_CONTROL);
//    	for(WellName wellName:negativeControls) mapping.put(wellName, AssayWellControlType.ASSAY_CONTROL);
//    	for(WellName wellName:otherControls) mapping.put(wellName, AssayWellControlType.OTHER_CONTROL);

    	//for the type, don't care what labels are
    	
    	Map<String,Set<WellName>> positiveControls = PlateReaderRawDataParser.expandNamedWellRanges(getAssayPositiveControls(), getAssayPlateSize().getWellCount());
    	Map<String,Set<WellName>> negativeControls = PlateReaderRawDataParser.expandNamedWellRanges(getAssayNegativeControls(), getAssayPlateSize().getWellCount());
    	Map<String,Set<WellName>> otherControls = PlateReaderRawDataParser.expandNamedWellRanges(getAssayOtherControls(), getAssayPlateSize().getWellCount());

    	for(Set<WellName> wellSet:positiveControls.values()) {
    		for(WellName wellName:wellSet) {
    			if(mapping.containsKey(wellName)) throw new IllegalArgumentException("Control well is repeated: " + wellName);
    			else mapping.put(wellName, AssayWellControlType.ASSAY_POSITIVE_CONTROL);
    		}
    	}    	
    	for(Set<WellName> wellSet:negativeControls.values()) {
    		for(WellName wellName:wellSet) {
    			if(mapping.containsKey(wellName)) throw new IllegalArgumentException("Control well is repeated: " + wellName);
    			else mapping.put(wellName, AssayWellControlType.ASSAY_CONTROL);
    		}
    	}    	
    	for(Set<WellName> wellSet:otherControls.values()) {
    		for(WellName wellName:wellSet) {
    			if(mapping.containsKey(wellName)) throw new IllegalArgumentException("Control well is repeated: " + wellName);
    			else mapping.put(wellName, AssayWellControlType.OTHER_CONTROL);
    		}
    	}
     	return mapping;
    }
    
    private Set<WellName> parseLibraryControls(){
    	Set<WellName> allControls = Sets.newHashSet();
    	Map<String,Set<WellName>> controls = PlateReaderRawDataParser.expandNamedWellRanges(getLibraryControls(), getLibraryPlateSize().getWellCount());
    	for(Set<WellName> set:controls.values()) {
    		allControls.addAll(set);
    	}
    	return allControls;
    }
    
    public boolean validate(PlateReaderRawDataTransformer parent)
    {
      boolean result = true;
      
      try {
      	Integer[] plates = PlateReaderRawDataParser.expandPlatesArg(getPlates());
      } catch (IllegalArgumentException e) {
      	parent.showMessage("invalidUserInput", "Plates", e.getMessage());
      	result = false;
      }

      try {
      	Map<WellName,AssayWellControlType> controls = parseAssayControls();
      }catch(IllegalArgumentException e) {
      	parent.showMessage("invalidUserInput", "Control Wells", e.getMessage());
      }
      return result;
    }

    /** @motivation for control wells plate map UI dataTable */
    public List<String> getPlateRowLabels()
    {
      return _plateSize.getSelection().getRowsLabels();
    }

    /** @motivation for control wells plate map UI dataTable */
    public DataModel getPlateColumns()
    {
      return new ListDataModel(_plateSize.getSelection().getColumnsLabels());
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
                                                                                                                "assayPlateSize",
                                                                                                                "outputFileName",
                                                                                                                "outputFormat",
                                                                                                                "assayPositiveControls",
                                                                                                                "assayNegativeControls",
                                                                                                                "assayOtherControls",
                                                                                                                "libraryControls"));
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
      new UISelectOneBean<CollationOrder>(ImmutableList.of(
          new CollationOrder(ImmutableList.of(PlateOrderingGroup.Plates, PlateOrderingGroup.Quadrants, PlateOrderingGroup.Conditions, PlateOrderingGroup.Replicates, PlateOrderingGroup.Readouts)),
          new CollationOrder(ImmutableList.of(PlateOrderingGroup.Plates, PlateOrderingGroup.Quadrants, PlateOrderingGroup.Replicates, PlateOrderingGroup.Conditions, PlateOrderingGroup.Readouts)),
          new CollationOrder(ImmutableList.of(PlateOrderingGroup.Conditions, PlateOrderingGroup.Plates, PlateOrderingGroup.Quadrants, PlateOrderingGroup.Replicates, PlateOrderingGroup.Readouts)),
          new CollationOrder(ImmutableList.of(PlateOrderingGroup.Conditions, PlateOrderingGroup.Replicates, PlateOrderingGroup.Plates, PlateOrderingGroup.Quadrants, PlateOrderingGroup.Readouts)),
          new CollationOrder(ImmutableList.of(PlateOrderingGroup.Replicates, PlateOrderingGroup.Plates, PlateOrderingGroup.Quadrants, PlateOrderingGroup.Conditions, PlateOrderingGroup.Readouts)),
          new CollationOrder(ImmutableList.of(PlateOrderingGroup.Replicates, PlateOrderingGroup.Conditions, PlateOrderingGroup.Plates, PlateOrderingGroup.Quadrants, PlateOrderingGroup.Readouts))


          ));
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
        log.info("uploaded new file " + uploadedFile);
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
    
    public CollationOrder getCollationOrder1()
    {
      return getCollationOrder().getSelection();
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
        return Lists.newArrayList();
      }
      Scanner scanner = new Scanner(input).useDelimiter("(\\n|,)+");
      List<String> result = Lists.newArrayList();
      while (scanner.hasNext()) {
        result.add(scanner.next().trim());
      }
      return result;
    }
  }
  
  public static class Result{
  	public File outputFile;
  	public int matricesReadIn;
  	public int matricesProcessed;
  	
  	public int getMatricesReadIn() { return this.matricesReadIn; }
  }


  private GenericEntityDAO _dao;
  private ScreenViewer _screenViewer;
//  private edu.harvard.med.screensaver.service.screenresult.PlateReaderRawDataTransformer _transformer;

  private FormOne _formOne = new FormOne();
  private List<InputFileParams> _inputFiles = Lists.newArrayList();
  private Result _result;
  private String _comments;
  private List<? extends ParseError> _lastParseErrors;
	private LibrariesDAO _librariesDAO;
	private CherryPickRequestViewer _cherryPickRequestViewer;

  /**
   * @motivation for CGLIB2
   */
  protected PlateReaderRawDataTransformer()
  {
  }

  public PlateReaderRawDataTransformer(GenericEntityDAO dao,
  		LibrariesDAO librariesDAO, 
  		ScreenViewer screenViewer)
  {
  	log.warn("==========constructor 1");
    _dao = dao;
    _librariesDAO = librariesDAO;
    _screenViewer = screenViewer;
    _cherryPickRequestViewer = null;
  }

  public PlateReaderRawDataTransformer(GenericEntityDAO dao,
  		LibrariesDAO librariesDAO, 
  		CherryPickRequestViewer cherryPickRequestViewer)
  {
  	log.warn("==========constructor 2 : " + cherryPickRequestViewer);
    _dao = dao;
    _librariesDAO = librariesDAO;
    _cherryPickRequestViewer = cherryPickRequestViewer;
    _screenViewer = null;
  }

  public Screen getScreen()
  {
    return _screenViewer.getEntity();
  }
  
  public CherryPickRequest getCherryPickRequest() {
  	if(_cherryPickRequestViewer==null) return null;
    CherryPickRequest cpr = _dao.reloadEntity(_cherryPickRequestViewer.getEntity());
  	return cpr; //_cherryPickRequestViewer.getEntity();
  }
  
  public LibrariesDAO getLibrariesDao() { return _librariesDAO; }

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
  	if(_screenViewer != null) {
  		return _screenViewer.reload();
  	}else if(_cherryPickRequestViewer != null){
  		return _cherryPickRequestViewer.reload();
  	}else {
  		// nop
  		throw new IllegalArgumentException("illegal clause");
  	}
  }

  @UICommand
  @Transactional
  public String view()
  {
    reset();
    restoreInputParams();
    return TRANSFORM_PLATE_READER_RAW_DATA;
  }
  
  @UICommand
  @Transactional
  public String viewCherryPickTransformer()
  {
  	reset();
  	restoreInputParams();
  	return TRANSFORM_CHERRY_PICK_RAW_DATA;
  }

  private void restoreInputParams()
  {
  	if(_screenViewer != null) {
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
  	}else if(_cherryPickRequestViewer != null){
  		CherryPickRequest cpr = getCherryPickRequest();
	    if (!cpr.getUpdateActivitiesOfType(AdministrativeActivityType.PLATE_RAW_DATA_TRANSFORMATION).isEmpty()) {
	      String comments = NullSafeUtils.value(cpr.getUpdateActivitiesOfType(AdministrativeActivityType.PLATE_RAW_DATA_TRANSFORMATION).last().getComments(), "");
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
	    }else {
	  		Set<Integer> lcpPlates = Sets.newHashSet();
	  		for(LabCherryPick lcp:_cherryPickRequestViewer.getEntity().getLabCherryPicks()) {
	  			lcpPlates.add(lcp.getAssayPlate().getPlateOrdinal()+1);
	  		}
	  		getFormOne().setPlates(Joiner.on(",").join(lcpPlates));
	    }
  	}
  }

  private void reset()
  {
    _formOne = new FormOne();
    _inputFiles = Lists.newArrayList(new InputFileParams());
    _comments = null;
    _result = null;
    File _outputFile = null;
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
  
  public Map<WellKey,AssayWellControlType> getPlateControls(
  		PlateReaderRawDataParser.WellFinder finder, 
  		Map<WellName,AssayWellControlType> assayControlMapping,
  		Set<WellName> manuallLibraryControls,
  		Integer[] plates, int aps, int lps)
  {
  	int factor = 1;
  	if(aps<lps) {
  		factor = lps/aps;
  	}
  	Map<WellKey, AssayWellControlType> controlWells = Maps.newHashMap();
  	List<String> exceptions = Lists.newArrayList();
  	
  	Set<WellName> exceptionControlWellsIfAny = Sets.newHashSet(); // temp list to track these, to only show them once in the error output
  	for(Map.Entry<WellName,AssayWellControlType> entry:assayControlMapping.entrySet()) {
  		WellName assayPlateWellName = entry.getKey();
  		for(int i=0;i<plates.length;i=i+factor) {
  			int plate = plates[i];
  			int sourceQuadrant = i % factor; // note, this is ignored if aps>=lps
  			WellName libraryWellName = PlateReaderRawDataParser.convertWell(assayPlateWellName, aps, lps, sourceQuadrant);
  			
    		WellKey libraryWellKey = new WellKey(plate,libraryWellName);
  			if(factor > 1) {
    			int destQuadrant = PlateReaderRawDataParser.deconvoluteMatrix(aps, lps, assayPlateWellName.getRowIndex(), assayPlateWellName.getColumnIndex());
    			libraryWellKey = new WellKey(plate+destQuadrant, libraryWellName);
  			}
    		Well well = finder.findWell(libraryWellKey);
    		if(well == null) throw new IllegalArgumentException("cannot find the well: " + libraryWellKey);
    		if(!manuallLibraryControls.contains(libraryWellName) &&
    				well.getLibraryWellType() == LibraryWellType.EXPERIMENTAL ) 
    		{
      		if(!manuallLibraryControls.contains(libraryWellName)) {
      			if(!exceptionControlWellsIfAny.contains(assayPlateWellName)) { // only add the error for a control well once, not for all plates
      				exceptions.add("Control: "+ assayPlateWellName + ": " + entry.getValue() + " => " + well + ": manual library control");
      				exceptionControlWellsIfAny.add(assayPlateWellName);
      			}
      		}else {
      			if(!exceptionControlWellsIfAny.contains(assayPlateWellName)) {
        			exceptions.add("Control: " + assayPlateWellName + ": " + entry.getValue() + " => " + well + ": " + well.getLibraryWellType() );
      				exceptionControlWellsIfAny.add(assayPlateWellName);
      			}
      		}
    		}else {
    			controlWells.put(libraryWellKey, entry.getValue());
    		}
  		}
  	}
  	if(!exceptions.isEmpty())
		{
  		String msg = "Library plate wells must be 'empty' or 'DMSO' to be used as an assay control: ";
  		if(exceptions.size() > 5 ) msg += exceptions.subList(0, 4) + ", and " + (exceptions.size() -5 ) + " other similar errors.";
  		else msg += exceptions;
  		throw new IllegalArgumentException(msg);
		}
  	return controlWells;
  }

  @UICommand
  @Transactional
  public String transform() throws IOException
  {
		
  	_result = null;
    getFormOne().invalidateAssayControlWellsModel();
    if (!validate()) {
      return REDISPLAY_PAGE_ACTION_RESULT;
    }

    Integer[] plates = PlateReaderRawDataParser.expandPlatesArg(getFormOne().getPlates());
    
    InputStream multiFileInputStream = null;
    
		final int aps = getFormOne().getAssayPlateSize().getWellCount();
		final int lps = getFormOne().getLibraryPlateSize().getWellCount();
    List<MatrixOrderPattern> combinedPlateOrderings = Lists.newArrayList();
    List<List<String[]>> combinedParsedMatrices = Lists.newArrayList();
    int expectedPlateMatrices = 0;
    int expectedMatricesReadIn = 0;
    		
    for (InputFileParams inputFile : getInputFiles()) {
    	log.info("reading file: " + inputFile.getUploadedFilename());
      if (multiFileInputStream == null ) {
        multiFileInputStream = inputFile.getUploadedFile().getInputStream();
      } 
      else {
        multiFileInputStream = new SequenceInputStream(multiFileInputStream, inputFile.getUploadedFile().getInputStream());
      }
      
      AssayReadoutType assayReadoutType = inputFile.getReadoutTypeSelection();
      List<String> assayReadouts = inputFile.getParsedReadouts();
      if(assayReadouts.isEmpty()) assayReadouts.add(assayReadoutType.toString());
      else {
      	for(int i=0;i<assayReadouts.size();i++) {
      		assayReadouts.set(i, assayReadoutType.toString() + "_" + assayReadouts.get(i));
      	}
      }

      int reps = inputFile.getReplicates();
			if (reps < 1) throw new IllegalArgumentException("replicate count must be > 1"); 
			String[] replicates = new String[reps];
			for(int i=0;i<reps; i++ ) replicates[i] = ("" + (char)('A'+i));

			List<String> conditions = inputFile.getParsedConditions();
			if(conditions.isEmpty()) conditions.add("condition1");

      try {
  			MatrixOrderPattern matrixOrder = 
  					new MatrixOrder(inputFile.getCollationOrder1(), 
  							plates, 
  							conditions.toArray(new String[] {}), 
  							assayReadouts.toArray(new String[] {}), 
  							replicates);
  			if(aps == 1536){
  			  matrixOrder = new MatrixOrder1536(inputFile.getCollationOrder1(), 
              plates, 
              conditions.toArray(new String[] {}), 
              assayReadouts.toArray(new String[] {}), 
              replicates);
  			}
  			int tempPlateMatrices = matrixOrder.getExpectedMatrixCount();
  			expectedPlateMatrices += tempPlateMatrices;
  			int tempMatricesToReadIn = tempPlateMatrices * lps / aps;
  			expectedMatricesReadIn += tempMatricesToReadIn;
  			
  			List<List<String[]>> parsedMatrices = 
  					PlateReaderRawDataParser.parseMatrices(new BufferedReader(new InputStreamReader(multiFileInputStream)));

  			if(parsedMatrices.size() !=  tempMatricesToReadIn ) {
  	    	showMessage("invalidUserInput", "Plates", 
  	    			"Expected matrices before collation/deconvolution: " + tempMatricesToReadIn  + ", but found: " + parsedMatrices.size());
  	      return REDISPLAY_PAGE_ACTION_RESULT;
  			}
  			PlateReaderRawDataParser.validateMatrices(parsedMatrices, aps);
  			
  			inputFile.setExpectedPlateMatrixCount(tempPlateMatrices);
  			inputFile.setUploadedPlateMatrixCount(tempMatricesToReadIn);

  			// TODO: converting the matrices into the "standard" 384 format here for all cases, 96to384 and 1536to384
  			// instead, could convert nothing, but use the "convert" functions to convert each cell at write time.
  			// since doing it this way leads to making assumptions about plate/quadrant ordering that differ for 96 and 1536
  			List<List<String[]>> newMatrices = 
  					PlateReaderRawDataParser.convertMatrixFormat(aps, lps, parsedMatrices);
  			if(newMatrices.size() !=  tempPlateMatrices ) {
  				throw new Exception("ExpectedCount adjusted matrix count: " + tempPlateMatrices  + ", but found: " + newMatrices.size());
  			}
  			
  			combinedPlateOrderings.add(matrixOrder);
  			combinedParsedMatrices.addAll(newMatrices);
      } catch (Exception e) {
				log.error("Error loading plate reader raw data", e);
				showMessage("screens.screenResultDataFailed", e.getMessage());
	      return REDISPLAY_PAGE_ACTION_RESULT;
			}
    }// done reading

		try {
			if (StringUtils.isEmpty(getFormOne().getOutputFileName())) {
	      getFormOne().setOutputFileName(makeOutputFileName());
	    }			

			File outputFile = File.createTempFile(getFormOne().getOutputFileName(), "xls");
			if(_screenViewer != null) {
				// Well Finder - for caching
		    final PlateReaderRawDataParser.WellFinder finder = 
		    		new PlateReaderRawDataParser.WellFinder()
		    {
		    	int MAXSIZE_PLATES = 40;
		    	// Construct the LHM with "true" for least-accessed-first order
		    	LinkedHashMap<Integer,Map<WellKey, Well>> plateWellMap = 
		    			new LinkedHashMap<Integer, Map<WellKey,Well>>(MAXSIZE_PLATES,0.75f,true);
					@Override
					public Well findWell(WellKey wellKey) {
						if(!plateWellMap.containsKey(wellKey.getPlateNumber())) {
							Map<WellKey, Well> wellMap = Maps.newHashMap();
							for(Well well:_librariesDAO.findWellsForPlate(wellKey.getPlateNumber())) {
								wellMap.put(well.getWellKey(), well);
							}
							//caching all of the wells is wasteful, so limit size
							while(plateWellMap.size() > MAXSIZE_PLATES) {
								Integer firstKey = plateWellMap.keySet().iterator().next();
								log.debug("plate cache remove least used plate: " + firstKey);
								plateWellMap.remove(firstKey);
							}
							plateWellMap.put(wellKey.getPlateNumber(), wellMap);
						}
						Map<WellKey, Well> wellMap = plateWellMap.get(wellKey.getPlateNumber());
						Well well = wellMap.get(wellKey);
						if(well == null) throw new IllegalArgumentException("Well not found in database: " + wellKey);
						return well;
					}
				};
	      // Map and validate all of the control wells
	      Map<WellKey, AssayWellControlType> plateControls = null;
		    try {
	    		plateControls = getPlateControls(
	    				finder,
	    				getFormOne().parseAssayControls(), 
	    				getFormOne().parseLibraryControls(),
	    				plates, 
	    				getFormOne().getAssayPlateSize().getWellCount(),
	    				getFormOne().getLibraryPlateSize().getWellCount());
			    }catch(Exception e) {
			    	showMessage("businessError", e.getMessage());
			      return REDISPLAY_PAGE_ACTION_RESULT;
			    }
		    if(log.isDebugEnabled()) log.debug("plate controls: " + plateControls);
		    else log.info("plate controls: " + plateControls.size());
			    
				PlateReaderRawDataParser.SheetHeaderWriter headerWriter = new PlateReaderRawDataParser.SheetHeaderWriter() {
					@Override
					public void writeHeaders(WritableSheet sheet, int baseColumns, Map<String, Integer> valueColumnLabels) throws RowsExceededException, WriteException 
					{
						int col = baseColumns;
						sheet.addCell(new jxl.write.Label(col++, 0, "Type"));
						sheet.addCell(new jxl.write.Label(col++, 0, "Exclude"));
						for(Map.Entry<String, Integer> entry: valueColumnLabels.entrySet()) {
							String colName = entry.getKey();
							sheet.addCell(new jxl.write.Label(col + entry.getValue(), 0, colName));
						}
						col += valueColumnLabels.size();
						sheet.addCell(new jxl.write.Label(col++, 0, "Pre-Loaded Controls"));
						
						if(_screenViewer.getEntity().getScreenType() == ScreenType.RNAI) {
							sheet.addCell(new jxl.write.Label(col++, 0, "Entrezgene Symbol"));
							sheet.addCell(new jxl.write.Label(col++, 0, "Entrezgene ID"));
							sheet.addCell(new jxl.write.Label(col++, 0, "Genbank Accession No."));
							sheet.addCell(new jxl.write.Label(col++, 0, "Catalog No."));
							sheet.addCell(new jxl.write.Label(col++, 0, "Gene Name"));
							sheet.addCell(new jxl.write.Label(col++, 0, "Deprecated Pool"));
						}
					}
				};

				final int numberOfValueColumns = expectedPlateMatrices / plates.length+1;
				log.info("numberOfValueColumns: " + numberOfValueColumns + ", epm: " + expectedPlateMatrices);
				if(expectedPlateMatrices % plates.length != 0 ) throw new IllegalArgumentException("Collation order options must be a factor of the number of plates");
				final Map<WellKey, AssayWellControlType> plateControls1 = plateControls;
				final Map<WellName, String> plateControlLabels = getFormOne().parseControlLabels();
				final Map<WellName, String> libraryControlLabels = getFormOne().parseLibraryControlLabels();
				final Set<WellName> libraryControls = getFormOne().parseLibraryControls();
				PlateReaderRawDataParser.WellWriter wellWriter = new PlateReaderRawDataParser.WellWriter() {
					@Override
					public void writeWell(WritableSheet sheet, int sheetRow, WellKey wellReadIn, int baseColumns) throws RowsExceededException, WriteException {
						int i = 0;
						int typeCol = baseColumns + i++;
						int excludeCol = baseColumns + i++;
            int col = baseColumns + i + numberOfValueColumns;
						WellName wellNameReadIn = new WellName(wellReadIn.getWellName());
						if(plateControls1.containsKey(wellReadIn)) {
							sheet.addCell(new jxl.write.Label(typeCol, sheetRow, plateControls1.get(wellReadIn).getAbbreviation()));
							sheet.addCell(new jxl.write.Label(numberOfValueColumns + excludeCol,sheetRow,plateControlLabels.get(new WellName(wellReadIn.getWellName()))));
						} else if (libraryControls.contains(wellNameReadIn)) {
							sheet.addCell(new jxl.write.Label(typeCol, sheetRow, "C")); 
							sheet.addCell(new jxl.write.Label(numberOfValueColumns + excludeCol,sheetRow,libraryControlLabels.get(new WellName(wellReadIn.getWellName()))));
						}else {
							Well well = finder.findWell(wellReadIn);
							String abbreviation = well==null?"U":well.getLibraryWellType().getAbbreviation();
							sheet.addCell(new jxl.write.Label(typeCol, sheetRow, abbreviation));
							if(_screenViewer.getEntity().getScreenType() == ScreenType.RNAI) {
								SilencingReagent sr = ((SilencingReagent)well.getLatestReleasedReagent());
								Gene gene = sr == null ? null : sr.getVendorGene();
								sheet.addCell(new jxl.write.Label(col++, sheetRow, gene == null ? "" : Joiner.on(",").join(gene.getEntrezgeneSymbols())));
								sheet.addCell(new jxl.write.Label(col++, sheetRow, gene == null ? "" : ""+sr.getVendorGene().getEntrezgeneId()));
								sheet.addCell(new jxl.write.Label(col++, sheetRow, gene == null ? "" : Joiner.on(",").join(sr.getVendorGene().getGenbankAccessionNumbers())));
								sheet.addCell(new jxl.write.Label(col++, sheetRow, sr == null || sr.getVendorId() == null ? "" : "" + sr.getVendorId()));
								sheet.addCell(new jxl.write.Label(col++, sheetRow, gene == null || gene.getGeneName() == null ? "" : gene.getGeneName() ));
								sheet.addCell(new jxl.write.Label(col++, sheetRow, well.isDeprecated() ? "Y": ""));
							}
						}
					}
				};
				
				PlateReaderRawDataParser.WellValueWriter wellValueWriter = new PlateReaderRawDataParser.WellValueWriter() {
					@Override
					public void writeWell(WritableSheet sheet,  int sheetRow, int columnPosition, String rawValue) throws NumberFormatException, RowsExceededException, WriteException {
						int wellColumns = 2; // for the colums written above in the wellWriter
						sheet.addCell(new jxl.write.Number( columnPosition+wellColumns, sheetRow,Double.parseDouble(rawValue)));
					}
				};

				PlateReaderRawDataParser.writeParsedMatrices(
						"", // + _screenViewer.getEntity().getFacilityId(),
						aps, lps,
						plates, 
						combinedPlateOrderings, 
						combinedParsedMatrices,
						headerWriter,
						wellWriter,
						wellValueWriter,
						outputFile);
				
			}else if (_cherryPickRequestViewer != null) {
				PlateReaderRawDataParser.SheetHeaderWriter headerWriter = new PlateReaderRawDataParser.SheetHeaderWriter() {
					@Override
					public void writeHeaders(WritableSheet sheet, int baseColumns, Map<String, Integer> valueColumns) throws RowsExceededException, WriteException 
					{
						int col = baseColumns;
						sheet.addCell(new jxl.write.Label(col++, 0, "Type"));
						for(Map.Entry<String, Integer> entry: valueColumns.entrySet()) {
							String colName = entry.getKey();
							sheet.addCell(new jxl.write.Label(col + entry.getValue(), 0, colName));
						}
						col += valueColumns.size();
						sheet.addCell(new jxl.write.Label(col++, 0, "Pre-Loaded Controls"));
						sheet.addCell(new jxl.write.Label(col++, 0, "Library Plate"));
						sheet.addCell(new jxl.write.Label(col++, 0, "Source Well"));
						sheet.addCell(new jxl.write.Label(col++, 0, "Library Name"));
						if(_cherryPickRequestViewer.getEntity().getScreen().getScreenType() == ScreenType.SMALL_MOLECULE) {
							sheet.addCell(new jxl.write.Label(col++, 0, "Reagent Vendor ID"));
							sheet.addCell(new jxl.write.Label(col++, 0, "Vendor Batch"));
						}else { // RNAi
							sheet.addCell(new jxl.write.Label(col++, 0, "Vendor ID"));
							sheet.addCell(new jxl.write.Label(col++, 0, "Vendor Batch ID"));
							sheet.addCell(new jxl.write.Label(col++, 0, "Gene Symbol"));
							sheet.addCell(new jxl.write.Label(col++, 0, "Gene ID"));
							sheet.addCell(new jxl.write.Label(col++, 0, "Genbank Accession No."));
							sheet.addCell(new jxl.write.Label(col++, 0, "Sequence"));
                            sheet.addCell(new jxl.write.Label(col++, 0, "Gene Name"));
							// choose not to include these, informatics meeting 20130207
							//							sheet.addCell(new jxl.write.Label(col++, 0, "Pool Well Plate"));
							//							sheet.addCell(new jxl.write.Label(col++, 0, "Pool Well"));
						}
					}
				};
				
				// map the cp assay plate ("destination") wellkeys to the library source wells
				final PlateReaderRawDataParser.WellFinder sourceWellFinder = new PlateReaderRawDataParser.WellFinder() {
					Map<WellKey, Well> destinationToSourceWellMap = Maps.newHashMap();
					@Override
					public Well findWell(WellKey wellKey) {
						if(destinationToSourceWellMap.isEmpty()) {
							CherryPickRequest cpr = _dao.findEntityById(CherryPickRequest.class, _cherryPickRequestViewer.getEntity().getEntityId());
							for(LabCherryPick lcp: cpr.getLabCherryPicks()) {
								Well well = _librariesDAO.findWell(lcp.getSourceWell().getWellKey());
								destinationToSourceWellMap.put(new WellKey(lcp.getAssayPlate().getPlateOrdinal()+1, lcp.getAssayPlateWellName()), well);
							}
						}
						return destinationToSourceWellMap.get(wellKey);
					}
				};
				
				// map the library duplex source wells to the pool wells (TODO: improve the API!)
				final PlateReaderRawDataParser.WellFinder poolWellFinder = new PlateReaderRawDataParser.WellFinder() {
					Map<WellKey, Well> duplexToPoolWellMap = Maps.newHashMap();
					@Override
					public Well findWell(WellKey wellKey) {
						if(duplexToPoolWellMap.isEmpty()) {
							for(ScreenerCherryPick scp: _cherryPickRequestViewer.getEntity().getScreenerCherryPicks()) {
								Well well = _librariesDAO.findWell(scp.getScreenedWell().getWellKey());
								for(Well duplexWell:((SilencingReagent)well.getLatestReleasedReagent()).getDuplexWells()) {
									duplexToPoolWellMap.put(duplexWell.getWellKey(),well);
								}
							}
						}
						return duplexToPoolWellMap.get(wellKey);
					}
				};
				
				final int numberOfValueColumns = expectedPlateMatrices / plates.length;
				log.info("numberOfValueColumns: " + numberOfValueColumns + ", epm: " + expectedPlateMatrices);
				if(expectedPlateMatrices % plates.length != 0 ) throw new IllegalArgumentException("Collation order options must be a factor of the number of plates");
				final Map<WellName, AssayWellControlType> plateControls = getFormOne().parseAssayControls();
				final Map<WellName, String> plateControlLabels = getFormOne().parseControlLabels();
				final Map<WellName, String> libraryControlLabels = getFormOne().parseLibraryControlLabels();
				final Set<WellName> libraryControls = getFormOne().parseLibraryControls();
				PlateReaderRawDataParser.WellWriter wellWriter = new PlateReaderRawDataParser.WellWriter() {
					@Override
					public void writeWell(WritableSheet sheet, int sheetRow, WellKey wellReadIn, int baseColumns) throws RowsExceededException, WriteException {
						int typeCol = baseColumns;
						int i = numberOfValueColumns +1;
						int controlLabelCol = baseColumns + i++;
						int libraryPlateCol = baseColumns + i++;
						int sourceWellCol = baseColumns + i++;
						int libraryNameCol = baseColumns + i++;
						int rviCol = baseColumns + i++;
						int vendorBatchCol = baseColumns + i++;
						WellName wellNameReadIn = new WellName(wellReadIn.getWellName());
						if(plateControls.containsKey(wellNameReadIn)) {
							sheet.addCell(new jxl.write.Label(typeCol, sheetRow, plateControls.get(wellNameReadIn).getAbbreviation()));
							sheet.addCell(new jxl.write.Label(controlLabelCol,sheetRow,plateControlLabels.get(new WellName(wellReadIn.getWellName()))));
						} else if (libraryControls.contains(wellNameReadIn)) {
							sheet.addCell(new jxl.write.Label(typeCol, sheetRow, "C")); 
							sheet.addCell(new jxl.write.Label(controlLabelCol,sheetRow,libraryControlLabels.get(new WellName(wellReadIn.getWellName()))));
						}else {
							Well well = sourceWellFinder.findWell(wellReadIn);
							String abbreviation = well==null?"E":well.getLibraryWellType().getAbbreviation();
							sheet.addCell(new jxl.write.Label(typeCol, sheetRow, abbreviation));
							if(well != null) {
								sheet.addCell(new jxl.write.Label(libraryPlateCol,sheetRow, "" + well.getPlateNumber()));
								sheet.addCell(new jxl.write.Label(libraryNameCol,sheetRow, well.getLibrary().getShortName()));
								sheet.addCell(new jxl.write.Label(sourceWellCol,sheetRow, well.getWellName()));
								//log.info("_cherryPickRequestViewer.getEntity().getScreen().getScreenType(): " + _cherryPickRequestViewer.getEntity().getScreen().getScreenType());
								if(_cherryPickRequestViewer.getEntity().getScreen().getScreenType() == ScreenType.SMALL_MOLECULE) {
									sheet.addCell(new jxl.write.Label(rviCol, sheetRow, "" + well.getLatestReleasedReagent().getVendorId()));
									sheet.addCell(new jxl.write.Label(vendorBatchCol, sheetRow, well.getLatestReleasedReagent().getVendorBatchId()));
								}else {
									// In the case of the RNAi CP, the source well is a duplex well, so still have to find the corresponding pool well
									SilencingReagent duplexReagent = well.getLatestReleasedReagent();
									Well poolWell = poolWellFinder.findWell(well.getWellKey());
									int col = rviCol;
									sheet.addCell(new jxl.write.Label(col++, sheetRow, ""+duplexReagent.getVendorId()));
									sheet.addCell(new jxl.write.Label(col++, sheetRow, duplexReagent.getVendorBatchId())); 
									sheet.addCell(new jxl.write.Label(col++, sheetRow, Joiner.on(",").join(duplexReagent.getVendorGene().getEntrezgeneSymbols())));
									sheet.addCell(new jxl.write.Label(col++, sheetRow, duplexReagent.getVendorGene().getEntrezgeneId()==null?"":duplexReagent.getVendorGene().getEntrezgeneId().toString()));
									sheet.addCell(new jxl.write.Label(col++, sheetRow, Joiner.on(",").join(duplexReagent.getVendorGene().getGenbankAccessionNumbers())));
									sheet.addCell(new jxl.write.Label(col++, sheetRow, duplexReagent.getSequence()));

									sheet.addCell(new jxl.write.Label(col++, sheetRow, duplexReagent.getVendorGene() == null || duplexReagent.getVendorGene().getGeneName() == null ? "" : duplexReagent.getVendorGene().getGeneName() ));

									// choose not to include these, informatics meeting 20130207
									// sheet.addCell(new jxl.write.Label(col++, sheetRow, poolWell == null ? "" : ""+poolWell.getPlateNumber()));
									// sheet.addCell(new jxl.write.Label(col++, sheetRow, poolWell == null ? "" : poolWell.getWellName()));
								}
							}
						}
					}
				};
				
				PlateReaderRawDataParser.WellValueWriter wellValueWriter = new PlateReaderRawDataParser.WellValueWriter() {
					
					@Override
					public void writeWell(WritableSheet sheet,  int sheetRow, int columnPosition, String rawValue) throws NumberFormatException, RowsExceededException, WriteException {
						if(_cherryPickRequestViewer.getEntity().getScreen().getScreenType() == ScreenType.SMALL_MOLECULE) {
							int wellColumns = 1; // for the type column written above in the wellWriter
							sheet.addCell(new jxl.write.Number( columnPosition+wellColumns, sheetRow,Double.parseDouble(rawValue)));
						}else {
							int wellColumns = 1; // for the type column written above in the wellWriter
							sheet.addCell(new jxl.write.Number( columnPosition+wellColumns, sheetRow,Double.parseDouble(rawValue)));
						}
					}
				};
				
				PlateReaderRawDataParser.writeParsedMatrices(
						"CP" + _cherryPickRequestViewer.getEntity().getCherryPickRequestId(),
						aps, lps,
						plates, 
						combinedPlateOrderings, 
						combinedParsedMatrices,
						headerWriter,
						wellWriter,
						wellValueWriter,
						outputFile);
			} else {
				throw new IllegalArgumentException("invalid operation, not set to screen results or to cherry pick results");
			}
			
			_result = new Result();
			_result.outputFile = outputFile;
			_result.matricesProcessed = expectedPlateMatrices;
			_result.matricesReadIn = expectedMatricesReadIn;

	    
	    showMessage("screens.rawDataTranformationResult",
	        _result.matricesReadIn, _result.matricesProcessed,plates.length);

	    return REDISPLAY_PAGE_ACTION_RESULT;

		} catch (RowsExceededException e) {
    	showMessage("businessError", e.getMessage());
      return REDISPLAY_PAGE_ACTION_RESULT;
		} catch (WriteException e) {
    	showMessage("businessError", e.getMessage());
      return REDISPLAY_PAGE_ACTION_RESULT;
		}

  }

  private String makeOutputFileName()
  {
  	if(_screenViewer != null) {
      return "screen" + getScreen().getFacilityId() + "-" + getFormOne().getPlates().replaceAll("\\s+", "_") + ".xls";
  	}else if(_cherryPickRequestViewer != null) {
      return "cpr" + getCherryPickRequest().getCherryPickRequestId() + "-" + getFormOne().getPlates().replaceAll("\\s+", "_") + ".xls";
  	}else {
  		// nop
  		throw new IllegalArgumentException("illegal clause");
  	}
  }

  private boolean validate()
  {
    return getFormOne().validate(this);
  }

  @UICommand
  @Transactional
  public String download() throws IOException
  {
    if (_result != null && _result.matricesProcessed > 0) {
      JSFUtils.handleUserFileDownloadRequest(getFacesContext(), _result.outputFile, getFormOne().getOutputFileName(), Workbook.MIME_TYPE);
      recordActivities();
    }
    return REDISPLAY_PAGE_ACTION_RESULT;
  }

  private void recordActivities()
  {
  	if(_screenViewer != null) {
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
  	}else if(_cherryPickRequestViewer != null){
  		CherryPickRequest cpr = getCherryPickRequest();
      // create two Administrative Activities, one for the comment, one for the settings
      if (!StringUtils.isEmpty(getComments())) {
        cpr.createUpdateActivity(AdministrativeActivityType.COMMENT,
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
        cpr.createUpdateActivity(AdministrativeActivityType.PLATE_RAW_DATA_TRANSFORMATION, 
                                         (AdministratorUser) getCurrentScreensaverUser().getScreensaverUser(), activityComment.toString());
      }
      catch (Exception e) {
        // TODO: show user message instead
        log.warn("unable to save the parameters", e);
      }
      _dao.mergeEntity(cpr);
      _cherryPickRequestViewer.reload();
  	}
  }

  public boolean getHasErrors()
  {
    return _lastParseErrors != null && !_lastParseErrors.isEmpty();
  }
  
  public boolean getIsScreenResultParse() {
  	return _screenViewer != null;
  }
  
  public boolean getIsCherryPickParse()
  {
  	return _cherryPickRequestViewer != null;
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
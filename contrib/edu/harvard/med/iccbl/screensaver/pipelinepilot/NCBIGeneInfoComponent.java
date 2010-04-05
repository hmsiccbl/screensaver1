package edu.harvard.med.iccbl.screensaver.pipelinepilot;

import org.apache.log4j.Logger;

import com.scitegic.pilot.Component;
import com.scitegic.pilot.Context;
import com.scitegic.pilot.DataRecord;
import com.scitegic.pilot.Property;
import com.scitegic.pilot.PropertyCollection;
import com.scitegic.pilot.Value;

import edu.harvard.med.screensaver.util.StringUtils;
import edu.harvard.med.screensaver.util.eutils.NCBIGeneInfo;
import edu.harvard.med.screensaver.util.eutils.NCBIGeneInfoProvider;
import edu.harvard.med.screensaver.util.eutils.NCBIGeneInfoProviderImpl;

public class NCBIGeneInfoComponent implements com.scitegic.pilot.Component, ScreensaverComponent
{
  private static Logger log = Logger.getLogger(NCBIGeneInfoComponent.class);

  public static final String PROPERTY_FAIL_REASON_FIELD = "ncbi_fail_reason";
  public static final String PROPERTY_VENDOR_ENTREZ_GENE_SYMBOLS = "Vendor EntrezGene Symbols";
  public static final String PROPERTY_VENDOR_GENE_NAME = "Vendor Gene Name";
  public static final String PROPERTY_VENDOR_SPECIES = "Vendor Species";
  
  private String _listDelimiter = ";";
  private String _inputField = "Vendor EntrezGene ID";
  
  private NCBIGeneInfoProvider _geneInfoProvider;

  /**
   * From Pipeline Pilot help: "Java Component Development" guide, pp. 14:<br>
     * &quot;Component.onInitialize(): This method is invoked once before any data records arrive. This
   * method receives one argument, a com.scitegic.pilot.Context object. Also, it must return a
   * Component.State value, one of ReadyForInputData, DoneProcessingData,
   * ReadyForNewData or ReadyForInputThenNewData.&quot;
   */
  public State onInitialize(Context context) throws Exception
  {
    if(context != null)
    {
      PropertyCollection params = context.getComponentParameters();
      Property prop = params.findByName(PROPERTY_LIST_DELIMITER);
      if(prop != null) {
        _listDelimiter = prop.getValue().getString();
      } else {
        String msg = "Define the property: \""+ PROPERTY_LIST_DELIMITER + "\" to use this component";
        log.warn(msg);
        throw new IllegalArgumentException(msg);
      }

      prop = params.findByName(PROPERTY_INPUT_FIELD);
      if(prop != null) {
        _inputField = prop.getValue().getString();
      } else {
        String msg = "Define the property: \""+ PROPERTY_INPUT_FIELD + "\" to use this component";
        log.warn(msg);
        throw new IllegalArgumentException(msg);
      }

    } else {
      String msg = "FATAL: no Context defined";
      log.error(msg);
      throw new IllegalArgumentException(msg);
    }
    _geneInfoProvider = new NCBIGeneInfoProviderImpl();

    return Component.State.ReadyForInputData;
  }

  /**
   * From Pipeline Pilot help: "Java Component Development" guide, pp. 14:<br>
   * &quot;Component.onProcess(): This method is called once per data record. This method receives
   * two arguments (a com.scitegic.pilot.Context object and a com.scitegic.pilot.DataRecord
   * object) and returns a Component.State value to indicate the component's state after
   * processing the data.&quot;
   */
  public State onProcess(Context context, DataRecord data) throws Exception
  {
    log.info("onProcess called" );

    PropertyCollection record = data.getProperties();
    Property property = record.getByName(_inputField);
    Value value = null;
    
    if(( value = property.getValue() )== null || 
         StringUtils.isEmpty(value.getString()) )
    {
      data.routeTo(DataRecord.Route.FailPort);
      String errMsg ="Field: " + _inputField + " is empty."; 
      record.define(PROPERTY_FAIL_REASON_FIELD, errMsg);
      record.define(PROPERTY_FAIL_FAST_FIELD, "true");
      log.warn(errMsg);  //TODO: note that logging is unreliable in multiprocess subprotocols
    } else {
      int vendorEntrezGeneId = value.getInteger();
      
      try {
        long before = System.currentTimeMillis();
        NCBIGeneInfo geneInfo = _geneInfoProvider.getGeneInfoForEntrezgeneId(vendorEntrezGeneId);
        log.info("query time: " + (System.currentTimeMillis()-before) + 
            ", entrezGeneId:  " + vendorEntrezGeneId + ", geneInfo: " + geneInfo);

        record.define(PROPERTY_VENDOR_ENTREZ_GENE_SYMBOLS, geneInfo.getEntrezgeneSymbol() );
        record.define(PROPERTY_VENDOR_GENE_NAME, geneInfo.getGeneName());
        // TODO: is this req'd (not req'd in the spec: https://wiki.med.harvard.edu/ICCBL/RNAiLibrariesExcelFileFormat2 )
        record.define(PROPERTY_VENDOR_SPECIES, geneInfo.getSpeciesName());
      }
      catch (Exception e) 
      {
        data.routeTo(DataRecord.Route.FailPort);
        String errMsg = "Exception on querying on inputfield: " + _inputField 
            + ", value: " + value.toString() 
            + ", exception: " + e.getClass().getName() + ": " + e.getMessage(); 
        record.define(PROPERTY_FAIL_REASON_FIELD, errMsg);
        log.warn(errMsg, e);
      }
    }
    return Component.State.ReadyForInputData;
  }
  
  /**
   * From Pipeline Pilot help: "Java Component Development" guide, pp. 14:<br>
   * &quot;
   * Component.onFinalize(): This method is called when no more data records are available.
   * &quot;
   */
  public void onFinalize(Context arg0) throws Exception
  {
  }
}

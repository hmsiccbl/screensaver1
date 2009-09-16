package edu.harvard.med.iccbl.screensaver.pipelinepilot;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;

import com.scitegic.pilot.Component;
import com.scitegic.pilot.Context;
import com.scitegic.pilot.DataRecord;
import com.scitegic.pilot.PilotException;
import com.scitegic.pilot.Property;
import com.scitegic.pilot.PropertyCollection;

import edu.harvard.med.screensaver.util.StringUtils;
import edu.harvard.med.screensaver.util.eutils.CompoundIdType;
import edu.harvard.med.screensaver.util.eutils.PubchemSmilesOrInchiSearch;
import edu.harvard.med.screensaver.util.eutils.PubchemSmilesOrInchiStandardizer;

public class PubchemCIDForSmilesComponent implements com.scitegic.pilot.Component, ScreensaverComponent
{
  //TODO: note that file logging is unreliable in multiprocess subprotocols
  private static final Logger log = Logger.getLogger(PubchemCIDForSmilesComponent.class);

  public static final String PROPERTY_FAIL_REASON_FIELD = "pubchem_fail_reason";

  private String _listDelimiter = ";";
  private String _inputField = "SMILES";
  private String _outputField = "pubchem_id";

  private PubchemSmilesOrInchiSearch pubchemSmilesOrInchiSearch 
      = new PubchemSmilesOrInchiSearch();
  private PubchemSmilesOrInchiStandardizer pubchemSmilesOrInchiStandardizer 
      = new PubchemSmilesOrInchiStandardizer();
  
  /**
   * From Pipeline Pilot help: "Java Component Development" guide, pp. 14:<br>
   * &quot;Component.onInitialize(): This method is invoked once before any data records arrive. This
   * method receives one argument, a com.scitegic.pilot.Context object. Also, it must return a
   * Component.State value, one of ReadyForInputData, DoneProcessingData,
   * ReadyForNewData or ReadyForInputThenNewData.&quot;
   */
  public State onInitialize(Context context) throws Exception
  {
    // get the component parameters
    if(context != null)
    {
      PropertyCollection params = context.getComponentParameters();
      Property prop = params.findByName(PROPERTY_LIST_DELIMITER);
      if (prop != null) {
        _listDelimiter = prop.getValue().getString();
      } else {
        String msg = "Define the property: \""+ PROPERTY_LIST_DELIMITER + "\" to use this component";
        log.warn(msg);
        throw new IllegalArgumentException(msg);
      }

      prop = params.findByName(PROPERTY_INPUT_FIELD);
      if (prop != null) {
        _inputField = prop.getValue().getString();
      } else {
        String msg = "Define the property: \""+ PROPERTY_INPUT_FIELD + "\" to use this component";
        log.warn(msg);
        throw new IllegalArgumentException(msg);
      }

      prop = params.findByName(PROPERTY_OUTPUT_FIELD);
      if (prop != null) {
        _outputField = prop.getValue().getString();
      } else {
        String msg = "Define the property: \""+ PROPERTY_OUTPUT_FIELD + "\" to use this component";
        log.warn(msg);
        throw new IllegalArgumentException(msg);
      }
    } else {
      String msg = "FATAL: no Context defined";
      log.error(msg);
      throw new IllegalArgumentException(msg);
    }

    //eutilsClient.initializeDocumentBuilder();

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

    PropertyCollection propertyCollection = data.getProperties();
  
    String smiles = null;
    String standardizedSmiles = null;
    // Get the SMILES property
    String stringValue = null;
    String propName = _inputField;
    Property property = propertyCollection.getByName(propName);
    if (property.getValue() != null )
    {
      stringValue = property.getValue().getString();
    }
    if (stringValue == null || StringUtils.isEmpty(stringValue) )
    {
      String errMsg ="Field: " + propName + " is empty."; 
      propertyCollection.define(PROPERTY_FAIL_REASON_FIELD, errMsg);
      propertyCollection.define(PROPERTY_FAIL_FAST_FIELD, "true");
      log.warn(errMsg);  //TODO: note that logging is unreliable in multiprocess subprotocols
      data.routeTo(DataRecord.Route.FailPort);
      return Component.State.ReadyForInputData;
    }
    smiles = stringValue;
    log.info("Smiles: " + smiles);
    
    // Check for Standardized Smiles property
    stringValue = null;
    propName = PROPERTY_STANDARDIZED_SMILES;
    try {
      property = propertyCollection.getByName(propName);
      if (property.getValue() != null )
      {
        stringValue = property.getValue().getString();
      }
    }
    catch (PilotException e1) 
    {
      log.debug("property not defined: " + PROPERTY_STANDARDIZED_SMILES );
    }
    standardizedSmiles = stringValue;

    if (standardizedSmiles==null)
    {
      try 
      {
        standardizedSmiles = pubchemSmilesOrInchiStandardizer
            .getPubchemStandardizedSmilesOrInchi(smiles, CompoundIdType.SMILES);
        if(standardizedSmiles == null) log.warn("Query for standardized smiles failed");
      } catch (Exception e) {
          String errMsg = "Exception trying to run pubchemSmilesOrInchiStandardizer, "
              + ", smiles: " + smiles
              + ", exception: " + e.getClass().getName() + ": " + e.getMessage(); 
          propertyCollection.define(PROPERTY_FAIL_REASON_FIELD, errMsg);
          log.warn(errMsg, e);
          data.routeTo(DataRecord.Route.FailPort);
          return Component.State.ReadyForInputData;
      }
    }

    // use the standardized smiles
    if (standardizedSmiles != null && !standardizedSmiles.equals(smiles))
    {
      log.info("standardized smiles from " + smiles + " to " + standardizedSmiles);
      smiles = standardizedSmiles;
    }
    
    // perform the query
    try 
    {
      long before = System.currentTimeMillis();
      Set<String> newPubchemCids = new HashSet<String>();
      List<String> reportedList = pubchemSmilesOrInchiSearch.getPubchemCidsForSmilesOrInchi(smiles);
      if (reportedList == null )
      {
        log.warn("pubchem client returns a null for pubchemSmilesOrInchiSearch.getPubchemCidsForSmilesOrInchi( " + smiles + " )" );  
      } else {
        newPubchemCids.addAll(reportedList);
        if (log.isDebugEnabled()) 
          log.debug("Smiles: " + smiles + ", Pubchem CIDS: " + newPubchemCids);
        log.info("Pubchem Search time: " + ( System.currentTimeMillis()-before) + " ms" );
        
        StringBuilder newCids = new StringBuilder();
        for (String cid:newPubchemCids)
        {
          newCids.append(_listDelimiter).append(cid);
        }
        if (newCids.length() > 1)
        {
          log.info("Property added: " + _outputField + ": " + newCids.substring(1));
          propertyCollection.define(_outputField, newCids.substring(1) );
        } else {
          log.info("No PubChem IDs found for smiles: " + smiles);
        }
      }
    }
    catch (Exception e) 
    {
      String errMsg = "Exception on querying on inputfield: " + _inputField 
          + ", exception: " + e.getClass().getName() + ": " + e.getMessage(); 
      propertyCollection.define(PROPERTY_FAIL_REASON_FIELD, errMsg);
      data.routeTo(DataRecord.Route.FailPort);
      log.warn(errMsg, e);
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

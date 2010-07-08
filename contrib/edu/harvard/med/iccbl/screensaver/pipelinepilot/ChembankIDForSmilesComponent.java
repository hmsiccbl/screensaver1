package edu.harvard.med.iccbl.screensaver.pipelinepilot;

//NOTE: this class is being retired - will use the command line utility instead of the 
// Pipeline Pilot components to query for the Chembank ID's
public class ChembankIDForSmilesComponent //implements com.scitegic.pilot.Component, ScreensaverComponent
{
//  //TODO: note that file logging is unreliable in multiprocess subprotocols
//  private static Logger log = Logger.getLogger(ChembankIDForSmilesComponent.class);
//  public static final String PROPERTY_FAIL_REASON_FIELD = "chembank_fail_reason";
//
//  private String _listDelimiter = ";";
//  private String _inputField = "SMILES";
//  private String _outputField = "chembank_id";
//  private MoleculeWebService _moWebService;
//
//  /**
//   * From Pipeline Pilot help: "Java Component Development" guide, pp. 14:<br>
//   * &quot;Component.onInitialize(): This method is invoked once before any data records arrive. This
//   * method receives one argument, a com.scitegic.pilot.Context object. Also, it must return a
//   * Component.State value, one of ReadyForInputData, DoneProcessingData,
//   * ReadyForNewData or ReadyForInputThenNewData.&quot;
//   */
//  public State onInitialize(Context context) throws Exception
//  {
//    if(context != null)
//    {
//      PropertyCollection params = context.getComponentParameters();
//      Property prop = params.findByName(PROPERTY_LIST_DELIMITER);
//      if(prop != null) {
//        _listDelimiter = prop.getValue().getString();
//      } else {
//        String msg = "Define the property: \""+ PROPERTY_LIST_DELIMITER + "\" to use this component";
//        log.warn(msg);
//        throw new IllegalArgumentException(msg);
//      }
//
//      prop = params.findByName(PROPERTY_INPUT_FIELD);
//      if(prop != null) {
//        _inputField = prop.getValue().getString();
//      } else {
//        String msg = "Define the property: \""+ PROPERTY_INPUT_FIELD + "\" to use this component";
//        log.warn(msg);
//        throw new IllegalArgumentException(msg);
//      }
//
//      prop = params.findByName(PROPERTY_OUTPUT_FIELD);
//      if(prop != null) {
//        _outputField = prop.getValue().getString();
//      } else {
//        String msg = "Define the property: \""+ PROPERTY_OUTPUT_FIELD + "\" to use this component";
//        log.warn(msg);
//        throw new IllegalArgumentException(msg);
//      }
//    } else {
//      String msg = "FATAL: no Context defined";
//      log.error(msg);
//      throw new IllegalArgumentException(msg);
//    }
//
//    try {
//      getMoleculeWebService();
//    }
//    catch (Exception e) {
//      log.warn("Unable to properly init the getMoleculeWebService (will attempt lazy load with first record).", e);
//    }
//    
//    return Component.State.ReadyForInputData;
//  }
//  
//  /** 
//   * Make a lazy getter for this, since we have been seeing random failures for this.
//   * @return
//   */
//  private MoleculeWebService getMoleculeWebService()
//  {
//    if( _moWebService == null )
//    {
//      _moWebService = new MoleculeWebService_Service().getMoleculeWebService();
//    }
//    return _moWebService;
//  }
//
//  /**
//   * From Pipeline Pilot help: "Java Component Development" guide, pp. 14:<br>
//   * &quot;Component.onProcess(): This method is called once per data record. This method receives
//   * two arguments (a com.scitegic.pilot.Context object and a com.scitegic.pilot.DataRecord
//   * object) and returns a Component.State value to indicate the component's state after
//   * processing the data.&quot;
//   */
//  public State onProcess(Context context, DataRecord data) throws Exception
//  {
//    log.info("onProcess called" );
//
//    PropertyCollection propertyCollection = data.getProperties();
//
//    // Get the SMILES property
//    String stringValue = null;
//    String propName = _inputField;
//    Property property = propertyCollection.getByName(propName);
//    if (property.getValue() != null )
//    {
//      stringValue = property.getValue().getString();
//    }
//    if (stringValue == null || StringUtils.isEmpty(stringValue) )
//    {
//      data.routeTo(DataRecord.Route.FailPort);
//      String errMsg ="Field: " + propName + " is empty."; 
//      propertyCollection.define(PROPERTY_FAIL_REASON_FIELD, errMsg);
//      propertyCollection.define(PROPERTY_FAIL_FAST_FIELD, "true");
//      log.warn(errMsg);  //TODO: note that logging is unreliable in multiprocess subprotocols
//      return Component.State.ReadyForInputData;
//    }
//    String smiles = stringValue;
//    log.info("Smiles: " + smiles);
//    
//    // Check for Standardized Smiles property
//    stringValue = null;
//    propName = PROPERTY_STANDARDIZED_SMILES;
//    try {
//      property = propertyCollection.getByName(propName);
//      if (property.getValue() != null )
//      {
//        stringValue = property.getValue().getString();
//      }
//    }
//    catch (PilotException e1) 
//    {
//      log.debug("property not defined: " + PROPERTY_STANDARDIZED_SMILES );
//    }
//    String standardizedSmiles = stringValue;
//    if (standardizedSmiles==null)
//    {
//      try 
//      {
//        standardizedSmiles = PugSoapUtil.standardizeSmiles(smiles, PugSoapUtil.INTERVAL_BETWEEN_TRIES_MS, PugSoapUtil.TRY_LIMIT);
//        if(standardizedSmiles == null) log.warn("Query for standardized smiles failed");
//      } catch (Exception e) {
//          data.routeTo(DataRecord.Route.FailPort);
//          String errMsg = "Exception trying to run pubchemSmilesOrInchiStandardizer, "
//              + ", smiles: " + smiles
//              + ", exception: " + e.getClass().getName() + ": " + e.getMessage(); 
//          propertyCollection.define(PROPERTY_FAIL_REASON_FIELD, errMsg);
//          log.warn(errMsg, e);
//          return Component.State.ReadyForInputData;
//      }
//    }
//
//    // use the standardized smiles
//    if (standardizedSmiles != null && !standardizedSmiles.equals(smiles))
//    {
//      log.info("standardized smiles from " + smiles + " to " + standardizedSmiles);
//      smiles = standardizedSmiles;
//    }
//
//    try 
//    {
//      StringBuilder newCids = new StringBuilder();
//      for (String cid:getChembankIdsForSmiles(smiles))
//      {
//        newCids.append(_listDelimiter).append(cid);
//      }
//      if (newCids.length() > 1)
//      {
//        propertyCollection.define(_outputField, newCids.substring(1) ); 
//        log.info("Property added: " + _outputField + ": " + newCids.substring(1));
//      } else {
//        log.info("No Chembank IDs found for smiles: " + smiles);
//      }
//    }
//    catch (Exception e) 
//    {
//      data.routeTo(DataRecord.Route.FailPort);
//      String errMsg = "Exception on querying on inputfield: " + _inputField 
//          + ", exception: " + e.getClass().getName() + ": " + e.getMessage(); 
//      propertyCollection.define(PROPERTY_FAIL_REASON_FIELD, errMsg);
//      log.warn(errMsg, e);
//    }
//    
//    return Component.State.ReadyForInputData;
//  }
//  
//  /**
//   * NOTE: originally in the class ChembankIdUpgrader
//   * @param smiles
//   * @throws FindBySimilarity1Fault1 
//   * @throws FindBySimilarity1Fault1
//   */
//  public List<String> getChembankIdsForSmiles(String smiles) throws FindBySimilarity1Fault1
//  {
//    log.debug("query chembank against SMILES " + smiles);
//    List<String> chembankIds = new ArrayList<String>();
//    ArrayOfMolecule arrayOfMolecule = getMoleculeWebService().findBySimilarity(smiles, 1.0);
//    for (Molecule molecule : arrayOfMolecule.getMolecule()) {
//      // you would think they would give you an empty list, instead of a list containing null..
//      if (molecule != null) {
//        log.debug("  got hit " + molecule.getChembankId() + ": " + molecule.getSmiles());
//        chembankIds.add(molecule.getChembankId());
//      }
//    }
//    return chembankIds;
//  }
//  
//  /**
//   * From Pipeline Pilot help: "Java Component Development" guide, pp. 14:<br>
//   * &quot;
//   * Component.onFinalize(): This method is called when no more data records are available.
//   * &quot;
//   */
//  public void onFinalize(Context arg0) throws Exception
//  {
//  }

}

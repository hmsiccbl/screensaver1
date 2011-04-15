//$HeadURL$
//$Id$

//Copyright © 2006, 2010 by the President and Fellows of Harvard College.
//Screensaver is an open-source project developed by the ICCB-L and NSRB labs
//at Harvard Medical School. This software is distributed under the terms of
//the GNU General Public License.

package edu.harvard.med.screensaver.io.libraries.smallmolecule;

import java.io.InputStream;

import edu.harvard.med.screensaver.db.GenericEntityDAO;
import edu.harvard.med.screensaver.io.ParseError;
import edu.harvard.med.screensaver.io.ParseException;
import edu.harvard.med.screensaver.io.libraries.rnai.WorkbookLibraryContentsParser;
import edu.harvard.med.screensaver.io.parseutil.CsvColumn;
import edu.harvard.med.screensaver.io.parseutil.CsvIntegerColumn;
import edu.harvard.med.screensaver.io.parseutil.CsvTextColumn;
import edu.harvard.med.screensaver.model.libraries.Library;
import edu.harvard.med.screensaver.model.libraries.LibraryWellType;
import edu.harvard.med.screensaver.model.libraries.NaturalProductReagent;
import edu.harvard.med.screensaver.model.libraries.ReagentVendorIdentifier;
import edu.harvard.med.screensaver.model.libraries.Well;
import edu.harvard.med.screensaver.model.libraries.WellKey;
import edu.harvard.med.screensaver.model.libraries.WellName;
import edu.harvard.med.screensaver.util.AlphabeticCounter;
import edu.harvard.med.screensaver.util.Pair;
import edu.harvard.med.screensaver.util.StringUtils;

import org.apache.log4j.Logger;

/**
 * This class accomplishes both:
 * <ul>
 * <li>Parsing of the library input file, and
 * <li>Loading of the data into Library and related domain model objects on the database.
 * 
 */
public class NaturalProductsLibraryContentsParser extends WorkbookLibraryContentsParser<NaturalProductReagent> 
{
  private static final Logger log = Logger.getLogger(NaturalProductsLibraryContentsParser.class);
  
  private CsvIntegerColumn PLATE = new CsvIntegerColumn("Plate", AlphabeticCounter.toIndex("A"), true);
  private CsvColumn<WellName> WELL = new CsvColumn<WellName>("Well", AlphabeticCounter.toIndex("B"), true) {
    @Override public WellName parseField(String value) throws ParseException { return new WellName(value); }
  };
  private CsvTextColumn VENDOR_REAGENT_ID = new CsvTextColumn("Vendor Reagent ID", AlphabeticCounter.toIndex("C"), false);
  private CsvTextColumn FACILITY_REAGENT_ID = new CsvTextColumn("Facility Reagent ID", AlphabeticCounter.toIndex("D"), false);
  
  public NaturalProductsLibraryContentsParser(GenericEntityDAO dao, InputStream stream, Library library)
  {
    super(dao, stream, library);
  }

  @Override
  protected Pair<Well,NaturalProductReagent> parse(String[] row) throws ParseException
  {
    if (row.length == 0 || StringUtils.isEmpty(row[PLATE.getColumn()])) {
      return EMPTY_PARSE_RESULT; 
    }

    Integer plate = PLATE.getValue(row);
    WellName wellName = WELL.getValue(row);
    WellKey wellKey = new WellKey(plate, wellName);
    Well well = getDao().findEntityById(Well.class, wellKey.getKey(), false, Well.library);

    if (well == null) {
      throw new ParseException(new ParseError("specified well does not exist: " + wellKey + " (this is probably due to an erroneous plate number)"));
    }
        
    if (! well.getLibrary().equals(getLibrary())) {
      throw new ParseException(new ParseError("Well: " + well.getLibrary().getLibraryName() + " does not match specified input library " + getLibrary().getLibraryName()));
    }
        
    String vendorId = VENDOR_REAGENT_ID.getValue(row);
    NaturalProductReagent reagent = null;
    if (vendorId != null) {
      well.setLibraryWellType(LibraryWellType.EXPERIMENTAL);
      reagent = well.createNaturalProductReagent(new ReagentVendorIdentifier(well.getLibrary().getProvider(), vendorId));
    }
    else {
      well.setLibraryWellType(LibraryWellType.EMPTY);
    }
    
    well.setFacilityId(FACILITY_REAGENT_ID.getValue(row));
    
    return new Pair<Well,NaturalProductReagent>(well, reagent);
  }    
}


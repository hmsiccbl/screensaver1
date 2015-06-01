// $HeadURL:
// http://forge.abcd.harvard.edu/svn/screensaver/branches/iccbl/library-mgmt-rework/src/edu/harvard/med/screensaver/io/libraries/rnai/RNAiLibraryContentsParser.java
// $
// $Id$

// Copyright © 2006, 2010, 2011, 2012 by the President and Fellows of Harvard College.
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.io.libraries.rnai;

import java.io.InputStream;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;

import edu.harvard.med.screensaver.db.GenericEntityDAO;
import edu.harvard.med.screensaver.io.ParseError;
import edu.harvard.med.screensaver.io.ParseException;
import edu.harvard.med.screensaver.io.parseutil.CsvColumn;
import edu.harvard.med.screensaver.io.parseutil.CsvConcentrationColumn;
import edu.harvard.med.screensaver.io.parseutil.CsvIntegerColumn;
import edu.harvard.med.screensaver.io.parseutil.CsvSetColumn;
import edu.harvard.med.screensaver.io.parseutil.CsvTextColumn;
import edu.harvard.med.screensaver.io.parseutil.CsvTextListColumn;
import edu.harvard.med.screensaver.io.parseutil.CsvTextSetColumn;
import edu.harvard.med.screensaver.model.libraries.Gene;
import edu.harvard.med.screensaver.model.libraries.Library;
import edu.harvard.med.screensaver.model.libraries.LibraryWellType;
import edu.harvard.med.screensaver.model.libraries.ReagentVendorIdentifier;
import edu.harvard.med.screensaver.model.libraries.SilencingReagent;
import edu.harvard.med.screensaver.model.libraries.SilencingReagentType;
import edu.harvard.med.screensaver.model.libraries.Well;
import edu.harvard.med.screensaver.model.libraries.WellKey;
import edu.harvard.med.screensaver.model.libraries.WellName;
import edu.harvard.med.screensaver.util.AlphabeticCounter;
import edu.harvard.med.screensaver.util.Pair;
import edu.harvard.med.screensaver.util.StringUtils;

/**
 * This class accomplishes both:
 * <ul>
 * <li>Parsing of the library input file, and
 * <li>Loading of the data into Library and related domain model objects on the database.
 */
public class RNAiLibraryContentsParser extends WorkbookLibraryContentsParser<SilencingReagent>
{
  private static final Logger log = Logger.getLogger(RNAiLibraryContentsParser.class);

  private static LibraryWellType.UserType _libraryWellTypeParser = new LibraryWellType.UserType();
  private static SilencingReagentType.UserType _silencingReagentTypeParser = new SilencingReagentType.UserType();

  private CsvIntegerColumn PLATE = new CsvIntegerColumn("Plate", AlphabeticCounter.toIndex("A"), true);
  private CsvColumn<WellName> WELL = new CsvColumn<WellName>("Well", AlphabeticCounter.toIndex("B"), true) {
    @Override
    public WellName parseField(String value) throws ParseException
    {
      return new WellName(value);
    }
  };
  private CsvColumn<LibraryWellType> WELL_TYPE = new CsvColumn<LibraryWellType>("Well Type", AlphabeticCounter.toIndex("C"), true) {
    @Override
    public LibraryWellType parseField(String value) throws ParseException
    {
      return _libraryWellTypeParser.getTermForValueCaseInsensitive(value);
    }
  };
  private CsvConcentrationColumn CONCENTRATION = new CsvConcentrationColumn("concentration", AlphabeticCounter.toIndex("D"), false);
  private CsvTextColumn VENDOR = new CsvTextColumn("Vendor", AlphabeticCounter.toIndex("E"), false) {
    public boolean isConditionallyRequired(String[] row) throws ParseException
    {
      return WELL_TYPE.getValue(row) == LibraryWellType.EXPERIMENTAL;
    }
  };
  private CsvTextColumn VENDOR_REAGENT_ID = new CsvTextColumn("Vendor Reagent ID", AlphabeticCounter.toIndex("F"), false) {
    public boolean isConditionallyRequired(String[] row) throws ParseException
    {
      return WELL_TYPE.getValue(row) == LibraryWellType.EXPERIMENTAL;
    }
  };

  private CsvTextColumn FACILITY_REAGENT_ID = new CsvTextColumn("Facility Reagent ID", AlphabeticCounter.toIndex("G"), false);
  private CsvColumn<SilencingReagentType> SILENCING_REAGENT_TYPE = new CsvColumn<SilencingReagentType>("Silencing Reagent Type", AlphabeticCounter.toIndex("H"), false) {
    @Override
    protected SilencingReagentType parseField(String value) throws ParseException
    {
      return _silencingReagentTypeParser.getTermForValueCaseInsensitive(value);
    }

    public boolean isConditionallyRequired(String[] row) throws ParseException
    {
      return WELL_TYPE.getValue(row) == LibraryWellType.EXPERIMENTAL;
    }
  };
  private CsvTextColumn SEQUENCES = new CsvTextColumn("Sequences", AlphabeticCounter.toIndex("I"), false);
  private CsvTextColumn ANTISENSE_SEQUENCES = 
      new CsvTextColumn("Anti-sense Sequences", AlphabeticCounter.toIndex("J"), false);
  private CsvIntegerColumn VENDOR_ENTREZGENE_ID = 
      new CsvIntegerColumn("Vendor Entrezgene ID", AlphabeticCounter.toIndex("K"), false);
  private CsvTextListColumn VENDOR_ENTREZGENE_SYMBOLS = 
      new CsvTextListColumn("Vendor Entrezgene Symbols", AlphabeticCounter.toIndex("L"), false);
  private CsvTextColumn VENDOR_GENE_NAME = 
      new CsvTextColumn("Vendor Gene Name", AlphabeticCounter.toIndex("M"), false);
  private CsvTextSetColumn VENDOR_GENBANK_ACCESSION_NUMBERS = 
      new CsvTextSetColumn("Vendor Genbank Accession Numbers", AlphabeticCounter.toIndex("N"), false);
  private CsvTextColumn VENDOR_SPECIES = 
      new CsvTextColumn("Vendor Species", AlphabeticCounter.toIndex("O"), false);
  private CsvIntegerColumn FACILITY_ENTREZGENE_ID = 
      new CsvIntegerColumn("Vendor Entrezgene ID", AlphabeticCounter.toIndex("P"), false);
  private CsvTextListColumn FACILITY_ENTREZGENE_SYMBOLS = 
      new CsvTextListColumn("Vendor Entrezgene Symbols", AlphabeticCounter.toIndex("Q"), false);
  private CsvTextColumn FACILITY_GENE_NAME = 
      new CsvTextColumn("Vendor Gene Name", AlphabeticCounter.toIndex("R"), false);
  private CsvTextSetColumn FACILITY_GENBANK_ACCESSION_NUMBERS = 
      new CsvTextSetColumn("Vendor Genbank Accession Numbers", AlphabeticCounter.toIndex("S"), false);
  private CsvTextColumn FACILITY_SPECIES = 
      new CsvTextColumn("Vendor Species", AlphabeticCounter.toIndex("T"), false);
  private CsvSetColumn<WellKey> DUPLEX_WELLS = 
      new CsvSetColumn<WellKey>("Duplex Wells", AlphabeticCounter.toIndex("U"), false) {
    @Override
    protected WellKey parseElement(String value)
    {
      return new WellKey(value);
    }
  };
  private CsvTextColumn BARCODE = new CsvTextColumn("Barcode", AlphabeticCounter.toIndex("V"), false);


  public RNAiLibraryContentsParser(GenericEntityDAO dao, InputStream stream, Library library)
  {
    super(dao, stream, library);
  }

  @Override
  protected Pair<Well,SilencingReagent> parse(String[] row) throws ParseException
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

    if (!well.getLibrary().equals(getLibrary())) {
      throw new ParseException(new ParseError("Well: " + well.getWellKey() + " does not belong to the library " +
        getLibrary().getLibraryName()));
    }
    LibraryWellType wellType = WELL_TYPE.getValue(row);
    well.setLibraryWellType(wellType);
    String vendorName = VENDOR.getValue(row);
    String vendorReagentId = VENDOR_REAGENT_ID.getValue(row);
    if (vendorName == null ^ vendorReagentId == null) {
      throw new ParseException(new ParseError(VENDOR.getName() + " and " + VENDOR_REAGENT_ID.getName() + " must both be specified, or neither should be specified", 
                                              VENDOR_REAGENT_ID.getLocation(row)));
    }
    ReagentVendorIdentifier rvi = null;
    if (vendorReagentId != null) {
      rvi = new ReagentVendorIdentifier(vendorName, vendorReagentId);
    }

    well.setFacilityId((String) FACILITY_REAGENT_ID.getValue(row));
    well.setBarcode((String) BARCODE.getValue(row));

    if (wellType == LibraryWellType.EXPERIMENTAL)
    {
      if(CONCENTRATION.getValue(row) == null) {
        throw new ParseException(new ParseError(CONCENTRATION.getName() + " must be set for 'experimental' wells"));
      }
      well.setMgMlConcentration(CONCENTRATION.getMgMlConcentration());
      well.setMolarConcentration(CONCENTRATION.getMolarConcentration());
    }
    
    if (wellType != LibraryWellType.EXPERIMENTAL &&
      wellType != LibraryWellType.LIBRARY_CONTROL) {
      // TODO: this is just the first field that should be blank; same
      // applies for the later ones,
      // but we're just going to check this one, ignore the rest
      if (rvi != null) {
        throw new ParseException(new ParseError(VENDOR.getName() + " and " + VENDOR_REAGENT_ID.getName() +
                                                " must be null for well type: " + wellType, 
                                                VENDOR_REAGENT_ID.getLocation(row)));
      }
      // skip other fields for empty and buffer type wells
      log.debug("empty/buffer well: " + well + ", " + well.getLibraryWellType());
      return new Pair<Well,SilencingReagent>(well, null);
    }
    else {
      // parse reagent info
      SilencingReagentType srt = SILENCING_REAGENT_TYPE.getValue(row);
      String sequences = SEQUENCES.getValue(row);
      String antiSequences = ANTISENSE_SEQUENCES.getValue(row);
      
      if (sequences != null && (rvi == null || srt == null)) {
        throw new ParseException(new ParseError("if sequence is specified, " + VENDOR_REAGENT_ID.getName() + " and " + SILENCING_REAGENT_TYPE.getName() + " must be specified",
                                                SEQUENCES.getLocation(row)));
      }
      else if (wellType == LibraryWellType.LIBRARY_CONTROL && rvi == null) {
        // reagent info optional for control wells
        return new Pair<Well,SilencingReagent>(well, null);
      }
      
      SilencingReagent reagent = 
        well.createSilencingReagent(rvi,
                                    srt,
                                    sequences,
                                    antiSequences,
                                    false);
      updateGene(reagent.getVendorGene(),
                 VENDOR_ENTREZGENE_ID.getValue(row),
                 VENDOR_GENE_NAME.getValue(row),
                 VENDOR_SPECIES.getValue(row),
                 VENDOR_ENTREZGENE_SYMBOLS.getValue(row),
                 VENDOR_GENBANK_ACCESSION_NUMBERS.getValue(row));
      updateGene(reagent.getFacilityGene(),
                 FACILITY_ENTREZGENE_ID.getValue(row),
                 FACILITY_GENE_NAME.getValue(row),
                 FACILITY_SPECIES.getValue(row),
                 FACILITY_ENTREZGENE_SYMBOLS.getValue(row),
                 FACILITY_GENBANK_ACCESSION_NUMBERS.getValue(row));

      Set<WellKey> duplexWells = DUPLEX_WELLS.getValue(row);
      if (duplexWells != null) {
        if (!getLibrary().isPool()) {
          throw new ParseException(new ParseError("non-pool library cannot specify duplex wells", 
                                                  DUPLEX_WELLS.getLocation(row)));
        }
        for (WellKey key : duplexWells) {
          well = getDao().findEntityById(Well.class, key.getKey(), false);
          if (well == null) {
            throw new ParseException(new ParseError("specified duplex well does not exist: " + wellKey, 
                                                    DUPLEX_WELLS.getLocation(row)));
          }
          reagent.withDuplexWell(well);
        }
      }
      else if (getLibrary().isPool()) {
        // NOTE: per discussion with Informatix (Dave), we don't want to
        // be so restrictive as to say that
        // the Duplex Wells field _must_ have a value - sde4
        String msg = "Pool Well " + wellKey + " does not reference any duplex wells, cell: " + DUPLEX_WELLS.getLocation(row);
        log.warn(msg);
      }
      return new Pair<Well,SilencingReagent>(well, reagent);        
    }
  }

  private void updateGene(Gene gene,
                          Integer entrezGeneId,
                          String geneName,
                          String speciesName,
                          List<String> entrezSymbols,
                          Set<String> accessionNumbers)
  {
    gene.withEntrezgeneId(entrezGeneId).withGeneName(geneName).withSpeciesName(speciesName);
    if (entrezSymbols != null) {
      gene.getEntrezgeneSymbols().addAll(entrezSymbols);
    }
    if (accessionNumbers != null) {
      gene.getGenbankAccessionNumbers().addAll(accessionNumbers);
    }
  }
}

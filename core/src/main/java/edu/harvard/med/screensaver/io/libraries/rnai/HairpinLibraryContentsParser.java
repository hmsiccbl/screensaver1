// $HeadURL:
// http://forge.abcd.harvard.edu/svn/screensaver/branches/iccbl/library-mgmt-rework/src/edu/harvard/med/screensaver/io/libraries/rnai/RNAiLibraryContentsParser.java
// $
// $Id: RNAiLibraryContentsParser.java 7068 2012-03-08 22:59:44Z wrose $

// Copyright © 2006, 2010, 2011, 2012 by the President and Fellows of Harvard College.
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.io.libraries.rnai;

import java.io.InputStream;
import java.util.List;

import org.apache.log4j.Logger;

import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;

import edu.harvard.med.screensaver.db.GenericEntityDAO;
import edu.harvard.med.screensaver.io.ParseError;
import edu.harvard.med.screensaver.io.ParseException;
import edu.harvard.med.screensaver.io.parseutil.CsvColumn;
import edu.harvard.med.screensaver.io.parseutil.CsvConcentrationColumn;
import edu.harvard.med.screensaver.io.parseutil.CsvIntegerColumn;
import edu.harvard.med.screensaver.io.parseutil.CsvIntegerListColumn;
import edu.harvard.med.screensaver.io.parseutil.CsvTextColumn;
import edu.harvard.med.screensaver.io.parseutil.CsvTextListColumn;
import edu.harvard.med.screensaver.io.parseutil.CsvTextSubListColumn;
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
public class HairpinLibraryContentsParser extends WorkbookLibraryContentsParser<SilencingReagent>
{
  private static final Logger log = Logger.getLogger(HairpinLibraryContentsParser.class);

  private static LibraryWellType.UserType _libraryWellTypeParser = new LibraryWellType.UserType();

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
  private CsvConcentrationColumn CONCENTRATION = new CsvConcentrationColumn("Concentration", AlphabeticCounter.toIndex("D"), false);
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
  private CsvTextColumn SEQUENCES = new CsvTextColumn("Sequences", AlphabeticCounter.toIndex("H"), false);
  private CsvIntegerColumn SCREEN_POOL = new CsvIntegerColumn("Screen Pool", AlphabeticCounter.toIndex("I"), false) {
    public boolean isConditionallyRequired(String[] row) throws ParseException
    {
      return WELL_TYPE.getValue(row) == LibraryWellType.EXPERIMENTAL;
    }
  };
  private CsvIntegerListColumn VENDOR_ENTREZGENE_ID = new CsvIntegerListColumn("Vendor Entrezgene ID", AlphabeticCounter.toIndex("J"), false);
  private CsvTextSubListColumn VENDOR_ENTREZGENE_SYMBOLS = new CsvTextSubListColumn("Vendor Entrezgene Symbols", AlphabeticCounter.toIndex("K"), false);
  private CsvTextListColumn VENDOR_GENE_NAME = new CsvTextListColumn("Vendor Gene Name", AlphabeticCounter.toIndex("L"), false);
  private CsvTextSubListColumn VENDOR_GENBANK_ACCESSION_NUMBERS = new CsvTextSubListColumn("Vendor Genbank Accession Numbers", AlphabeticCounter.toIndex("M"), false);
  private CsvTextListColumn VENDOR_SPECIES = new CsvTextListColumn("Vendor Species", AlphabeticCounter.toIndex("N"), false);
  private CsvIntegerListColumn FACILITY_ENTREZGENE_ID = new CsvIntegerListColumn("Facility Entrezgene ID", AlphabeticCounter.toIndex("O"), false);
  private CsvTextSubListColumn FACILITY_ENTREZGENE_SYMBOLS = new CsvTextSubListColumn("Facility Entrezgene Symbols", AlphabeticCounter.toIndex("P"), false);
  private CsvTextListColumn FACILITY_GENE_NAME = new CsvTextListColumn("Facility Gene Name", AlphabeticCounter.toIndex("Q"), false);
  private CsvTextSubListColumn FACILITY_GENBANK_ACCESSION_NUMBERS = new CsvTextSubListColumn("Facility Genbank Accession Numbers", AlphabeticCounter.toIndex("R"), false);
  private CsvTextListColumn FACILITY_SPECIES = new CsvTextListColumn("Facility Species", AlphabeticCounter.toIndex("S"), false);


  public HairpinLibraryContentsParser(GenericEntityDAO dao, InputStream stream, Library library)
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
    log.debug("Building reagent details for well " + well.getWellId());
    
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

    if (wellType == LibraryWellType.LIBRARY_CONTROL && rvi == null) {
      // reagent info optional for control wells
      return new Pair<Well,SilencingReagent>(well, null);
    }
      
    well.setFacilityId((String) FACILITY_REAGENT_ID.getValue(row));

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
      String sequences = SEQUENCES.getValue(row);
      Integer screenPool = SCREEN_POOL.getValue(row);
      
      if (sequences != null && rvi == null) {
        throw new ParseException(new ParseError("if Sequences are specified, " + VENDOR_REAGENT_ID.getName() + " must be specified",
                                                SEQUENCES.getLocation(row)));
      }
      
      if (screenPool != null && rvi == null) {
          throw new ParseException(new ParseError("if Screen Pool is specified, " + VENDOR_REAGENT_ID.getName() + " must be specified",
                                                  SCREEN_POOL.getLocation(row)));
      }
      
      SilencingReagent reagent = well.createSilencingReagent(rvi, SilencingReagentType.SHRNA, sequences);
      
      reagent.forFacilityBatchId(screenPool);
      
      updateGeneList(reagent.getVendorGenes(), row,
    		     VENDOR_ENTREZGENE_ID,
    		     VENDOR_GENE_NAME,
                 VENDOR_SPECIES,
                 VENDOR_ENTREZGENE_SYMBOLS,
                 VENDOR_GENBANK_ACCESSION_NUMBERS);

      updateGeneList(reagent.getFacilityGenes(), row,
                 FACILITY_ENTREZGENE_ID,
                 FACILITY_GENE_NAME,
                 FACILITY_SPECIES,
                 FACILITY_ENTREZGENE_SYMBOLS,
                 FACILITY_GENBANK_ACCESSION_NUMBERS);
      
      return new Pair<Well,SilencingReagent>(well, reagent);        
    }
  }

  private void updateGeneList(List<Gene> genes,
		  				  String[] row,
                          CsvIntegerListColumn entrezGeneIdColumn,
                          CsvTextListColumn geneNameColumn,
                          CsvTextListColumn speciesNameColumn,
                          CsvTextSubListColumn entrezSymbolsColumn,
                          CsvTextSubListColumn accessionNumbersColumn) throws ParseException
  {
	  // Read list of Entrezgene IDs
	  // This defines the expected number of genes: other columns ought to describe the same number of genes
      List<Integer> entrezGeneIds = entrezGeneIdColumn.getValue(row);
      if(entrezGeneIds == null) {
    	  entrezGeneIds = Lists.newArrayList();
      }
      
      // List of names (zero or one per Entrezgene ID)
      List<String> entrezGeneNames = geneNameColumn.getValue(row);
      if(entrezGeneNames == null) {
    	  entrezGeneNames = Lists.newArrayList();
      }

	  while(entrezGeneNames.size() < entrezGeneIds.size())
		  entrezGeneNames.add(entrezGeneNames.size() > 0 ? Iterables.getLast(entrezGeneNames) : null);

	  // List of species names (zero or one per Entrezgene ID)
      List<String> speciesNames = speciesNameColumn.getValue(row);
      if(speciesNames == null) {
    	  speciesNames = Lists.newArrayList();
      }
      
	  while(speciesNames.size() < entrezGeneIds.size())
		  speciesNames.add(speciesNames.size() > 0 ? Iterables.getLast(speciesNames) : null);
      
	  // List of gene symbols (zero or more per Entrezgene ID)
      List<List<String>> entrezGeneSymbols = entrezSymbolsColumn.getValue(row);
      if(entrezGeneSymbols == null) {
    	  entrezGeneSymbols = Lists.newArrayList();
      }
      
	  while(entrezGeneSymbols.size() < entrezGeneIds.size())
		  entrezGeneSymbols.add(Lists.<String>newArrayList());
     
	  // List of Genbank accession numbers (zero or more per Entrezgene ID)
      List<List<String>> accessionNumbers = accessionNumbersColumn.getValue(row);
      if(accessionNumbers == null) {
    	  accessionNumbers = Lists.newArrayList();
      }
	  while(accessionNumbers.size() < entrezGeneIds.size())
		  accessionNumbers.add(Lists.<String>newArrayList());
      
	  // Check we don't have more descriptions than gene IDs
      if(entrezGeneIds.size() < entrezGeneNames.size()) {
          throw new ParseException(new ParseError("Found more names in " + geneNameColumn.getName() + " than ids in " + entrezGeneIdColumn.getName(),
        		  geneNameColumn.getLocation(row)));
      }
      
      if(entrezGeneIds.size() < entrezGeneSymbols.size()) {
          throw new ParseException(new ParseError("Found more symbols in " + entrezSymbolsColumn.getName() + " than ids in " + entrezGeneIdColumn.getName(),
        		  entrezSymbolsColumn.getLocation(row)));
      }
      
      if(entrezGeneIds.size() < accessionNumbers.size()) {
          throw new ParseException(new ParseError("Found more accession numbers in " + accessionNumbersColumn.getName() + " than ids in " + entrezGeneIdColumn.getName(),
        		  accessionNumbersColumn.getLocation(row)));
      }
      
      // Clear the list in case there is an existing entry (e.g. blank entry created by accessing getVendorGene())
      genes.clear();
      
      // Create the genes and add them to the list
      for(int i = 0; i < entrezGeneIds.size(); ++i) {
    	  Gene gene = new Gene()
    	  	.withEntrezgeneId(entrezGeneIds.get(i))
    	  	.withGeneName(entrezGeneNames.get(i))
    	  	.withSpeciesName(speciesNames.get(i));
	
    	  if (entrezGeneSymbols.get(i) != null) {
    		  gene.getEntrezgeneSymbols().addAll(entrezGeneSymbols.get(i));
    	  }
    
    	  if (accessionNumbers.get(i) != null) {
    		  gene.getGenbankAccessionNumbers().addAll(accessionNumbers.get(i));
    	  }
    
    	  genes.add(gene);
      }
  }
}

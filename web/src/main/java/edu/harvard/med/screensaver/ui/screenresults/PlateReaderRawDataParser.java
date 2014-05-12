package edu.harvard.med.screensaver.ui.screenresults;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import jxl.Workbook;
import jxl.write.WritableSheet;
import jxl.write.WritableWorkbook;
import jxl.write.WriteException;
import jxl.write.biff.RowsExceededException;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.GnuParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.log4j.Logger;
import org.hibernate.annotations.Immutable;

import com.google.common.base.Function;
import com.google.common.base.Joiner;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

import edu.harvard.med.screensaver.model.libraries.LibraryWellType;
import edu.harvard.med.screensaver.model.libraries.PlateSize;
import edu.harvard.med.screensaver.model.libraries.Well;
import edu.harvard.med.screensaver.model.libraries.WellKey;
import edu.harvard.med.screensaver.model.libraries.WellName;
import edu.harvard.med.screensaver.model.screenresults.AssayWellControlType;
import edu.harvard.med.screensaver.model.screens.ScreenType;
import edu.harvard.med.screensaver.util.NullSafeUtils;
import edu.harvard.med.screensaver.util.StringUtils;

public class PlateReaderRawDataParser {
	
  private static final Logger logger = Logger.getLogger(PlateReaderRawDataParser.class);
  public static Pattern rowOnlyPattern = Pattern.compile("[a-zA-Z]+");
  public static Pattern columnOnlyPattern = Pattern.compile("\\d+");

  public interface WellFinder
  {
  	public Well findWell(WellKey wellKey);
  }
  
	/**
   * For testing from the command line
   * @param args
	 * @throws Exception 
   */
  public static void main(String []args) throws Exception
  {
  	Options options = new Options();
    options.addOption(
        OptionBuilder
            .hasArg()
            .withArgName("input_file")
            .isRequired()
            .withDescription("input file")
            .withLongOpt("input_file")
            .create("if"));
    
    Set<String> plateSizes = Sets.newHashSet(new String[] {"96","384","1536"});
    options.addOption(
        OptionBuilder
            .hasArg()
            .withArgName("assay_plate_size")
            .isRequired()
            .withDescription("assay plate size: " + Joiner.on(",").join(plateSizes))
            .withLongOpt("assay_plate_size")
            .create("aps"));
    
    options.addOption(
        OptionBuilder
            .hasArg()
            .withArgName("library_plate_size")
            .isRequired()
            .withDescription("library plate size: " + Joiner.on(",").join(plateSizes))
            .withLongOpt("library_plate_size")
            .create("lps"));
    
    options.addOption(
        OptionBuilder
            .hasArg()
            .withArgName("read out plate ordering")
            .isRequired()
            .withDescription("read out plate orderings (use first letter only, " + 
                             "do not specify whole word):" 
            		+ Joiner.on(" | ").join(CollationOrder.orderings.values()))
            .withLongOpt("read_out_plate_ordering")
            .create("po"));    

    options.addOption(
        OptionBuilder
            .hasArg()
            .withArgName("outputFileName")
            .isRequired()
            .withDescription("Output File Name")
            .withLongOpt("outputFileName")
            .create("o"));

    options.addOption(
        OptionBuilder
            .hasArg()
            .withArgName("plates")
            .isRequired()
            .withDescription(
                "Plate numbers: use ranges or individual items, separated by commas")
            .withLongOpt("plates")
            .create("p"));
    
    options.addOption(
        OptionBuilder
            .hasArg()
            .withArgName("readouts")
            .isRequired()
            .withDescription("Readout names, separated by commas")
            .withLongOpt("readouts")
            .create("ro"));
    
    options.addOption(
        OptionBuilder
            .hasArg()
            .withArgName("replicates")
            .withDescription("# of replicates")
            .withLongOpt("replicates")
            .create("r"));    
    
    options.addOption(
        OptionBuilder
            .hasArg()
            .withArgName("conditions")
            .withDescription("list of conditions, comma separated")
            .withLongOpt("conditions")
            .create("c"));
    
    try {
			CommandLine cmdLine = new GnuParser().parse(options, args);
			
			String temp = cmdLine.getOptionValue("assay_plate_size");
			if (!plateSizes.contains(temp)) {
				throw new ParseException("assay_plate_size incorrect");
			}
			int aps = Integer.parseInt(temp);
			PlateSize assayPlateSize = null;
			for (PlateSize ps: PlateSize.values()) {
				if(ps.getWellCount()==aps) {
					assayPlateSize = ps;
					break;
				}
			}
			if(assayPlateSize == null) 
			    throw new IllegalArgumentException("Unknown plate size: " + aps);
			
			temp = cmdLine.getOptionValue("library_plate_size");
			if (!plateSizes.contains(temp)) {
				throw new ParseException("library_plate_size incorrect");
			}
			int lps = Integer.parseInt(temp);
			PlateSize libraryPlateSize = null;
			for (PlateSize ps: PlateSize.values()) {
				if(ps.getWellCount()==lps) {
					libraryPlateSize = ps;
					break;
				}
			}
			if(libraryPlateSize == null) 
			    throw new IllegalArgumentException("Unknown plate size: " + lps);

			int reps = 1;
			if(cmdLine.hasOption("replicates")) {
				temp = cmdLine.getOptionValue("replicates");
				reps = Integer.parseInt(temp);
			}
			if (reps < 1) throw new IllegalArgumentException("replicate count must be > 1"); 
			String[] replicates = new String[reps];
			for(int i=0;i<reps; i++ ) replicates[i] = ("" + (char)('A'+i));

			
			String outputFileName = cmdLine.getOptionValue("outputFileName");
			
			String po = cmdLine.getOptionValue("read_out_plate_ordering");
			CollationOrder ordering = CollationOrder.getOrder(po);
			if (ordering == null) {
				throw new ParseException("read_out_plate_ordering");
			}

			temp = cmdLine.getOptionValue("plates");
			Integer[] plates = expandPlatesArg(temp);
			
			String[] conditions = new String[] {"condition1"};
			if(cmdLine.hasOption("conditions")) {
				conditions = cmdLine.getOptionValue("conditions").split(",");
			}
						
			String[] readouts = cmdLine.getOptionValue("readouts").split(",");
			String inputFilePath = cmdLine.getOptionValue("input_file");
			
			MatrixOrderPattern matrixOrder = 
			    new MatrixOrder(ordering, plates, conditions, readouts, replicates);
			int expectedMatricesCreated = matrixOrder.getExpectedMatrixCount();
			int expectedMatricesReadIn = expectedMatricesCreated * lps / aps;
			
			File inputFile = new File(inputFilePath);
			BufferedReader reader = new BufferedReader(new FileReader(inputFile));
			List<List<String[]>> parsedMatrices = parseMatrices(reader);
			
			if(parsedMatrices.size() !=  expectedMatricesReadIn ) {
				throw new Exception(
				    "Expected matrices before collation/deconvolution: " + 
		        expectedMatricesReadIn  + ", but found: " + parsedMatrices.size());
			}
			validateMatrices(parsedMatrices, aps);
			
			// FIXME: #134 - matrix format conversion _must_ be done after putting in quadrant order
			List<List<String[]>> newMatrices = convertMatrixFormat(aps, lps, matrixOrder, parsedMatrices);
			if(newMatrices.size() !=  expectedMatricesCreated ) {
				throw new Exception(
				    "ExpectedCount adjusted matrix count: " + expectedMatricesCreated  + 
				    ", but found: " + newMatrices.size());
			}
			
		  // TODO!
			final Map<WellKey, AssayWellControlType> controlWells = Maps.newHashMap();
			
			final WellFinder finder = new WellFinder() {
				@Override
				public Well findWell(WellKey wellKey) {
					return null;
				}
			}; // TODO: wire in the LibrariesDAO if desired to handle this
		
			PlateReaderRawDataParser.SheetHeaderWriter headerWriter = 
			    new PlateReaderRawDataParser.SheetHeaderWriter() {
    				@Override
    				public void writeHeaders(
    				    WritableSheet sheet, int baseColumns, Map<String, Integer> valueColumns) 
    				        throws RowsExceededException, WriteException 
    				{
    					int col = baseColumns;
    					sheet.addCell(new jxl.write.Label(col++, 0, "type"));
    					sheet.addCell(new jxl.write.Label(col++, 0, "exclude"));
    					for(Map.Entry<String, Integer> entry: valueColumns.entrySet()) {
    						String colName = entry.getKey();
    						sheet.addCell(new jxl.write.Label(col + entry.getValue(), 0, colName));
    					}
    				}
    			};

			PlateReaderRawDataParser.WellWriter wellWriter = 
			    new PlateReaderRawDataParser.WellWriter() {
    				@Override
    				public void writeWell(
    				    WritableSheet sheet, int sheetRow, WellKey wellReadIn, int baseColumns) 
    				        throws RowsExceededException, WriteException {
    					int i = 0;
    					int typeCol = baseColumns + i++;
    					int excludeCol = baseColumns + i++;
    					if(controlWells.containsKey(wellReadIn)) {
    						sheet.addCell(new jxl.write.Label(
    						    typeCol, sheetRow, controlWells.get(wellReadIn).getAbbreviation()));
    					}else {
    						Well well = finder.findWell(wellReadIn);
    						String abbreviation = well==null ? 
    						    "U" : well.getLibraryWellType().getAbbreviation();
    						sheet.addCell(new jxl.write.Label(typeCol, sheetRow, abbreviation));
    					}
    				}
    			};
			
			PlateReaderRawDataParser.WellValueWriter wellValueWriter = 
			    new PlateReaderRawDataParser.WellValueWriter() {
      				@Override
      				public void writeWell(
      				    WritableSheet sheet,  int sheetRow, int columnPosition, String rawValue) 
      				        throws NumberFormatException, RowsExceededException, WriteException 
			        {
      					int wellColumns = 2; // for the colums written above in the wellWriter
      					sheet.addCell(new jxl.write.Number( 
      					    columnPosition+wellColumns, sheetRow,Double.parseDouble(rawValue)));
      				}
			    };
			File outputFile = File.createTempFile(outputFileName, ".xls");
			writeParsedMatrices(
					"", 
					aps,lps,
					plates, 
					Lists.newArrayList(matrixOrder), 
					newMatrices, 
					headerWriter,
					wellWriter,
					wellValueWriter,
					outputFile);
			
			String finalFileName = outputFileName + ".xls";
			File outFile = new File(finalFileName);
			copyFileUsingChannel(outputFile, outFile);

			logger.info("Wrote file: " + outFile);
    } catch (ParseException e) {
      System.out.println(e.getMessage());
	    new HelpFormatter().printHelp("command", options, true);
	    System.exit(1);
		}
  }
  
  /**
   * @param newMatrices
   * @param plateSize
   * @throws IllegalArgumentException if number of rows, or number of cols is 
   * incorrect for the given plateSize
   */
  public static void validateMatrices(List<List<String[]>> newMatrices, int plateSize)
  	throws IllegalArgumentException
  {
  	int expectedRows = getNumRows(plateSize);
  	int expectedCols = getNumCols(plateSize);
  	int matrixNumber = 0;
  	for(List<String[]> matrix:newMatrices) {
  		if(matrix.size() < expectedRows) {
  			logger.debug("matrix: " + matrix);
  			throw new IllegalArgumentException(
  			    "Wrong number of rows parsed in matrix: " + matrixNumber + 
  			    ", found: " + matrix.size() + ", expected: " + expectedRows);
  		}
  		int rowNumber = 0;
  		for(String[] row:matrix) {
    		if(row.length < expectedCols) { logger.error("Wrong number of cols parsed: row: " + rowNumber + ", matrix: " + matrixNumber + ", row as read: " + Joiner.on(",").join(row));
    			throw new IllegalArgumentException(
    			    "Wrong number of cols parsed in matrix: " 
    					+ matrixNumber + ", row: " + rowNumber +", found: " + row.length + 
    					", expected: " + expectedCols);
    		} rowNumber++;
  		}
  		matrixNumber++;
  	}
  }
  
	private static void copyFileUsingChannel(File source, File dest) 
	    throws IOException {
		FileChannel sourceChannel = null;
		FileChannel destChannel = null;
		try {
			sourceChannel = new FileInputStream(source).getChannel();
			destChannel = new FileOutputStream(dest).getChannel();
			destChannel.transferFrom(sourceChannel, 0, sourceChannel.size());
		} finally {
			sourceChannel.close();
			destChannel.close();
		}
	}
  
  public static List<List<String[]>> parseMatrices(BufferedReader reader) 
      throws IOException{

		String s;
		int line = 0;
		
		Pattern headerPattern = Pattern.compile("^\\s+\\d{1,2}\\s+\\d{1,2}\\s+.*");
		Pattern rowPattern = Pattern.compile("^\\s?([A-Z]{1,2})\\s+[-]?\\d+.*");
		
		List<List<String[]>> plateMatrices = Lists.newArrayList();
		List<String[]> readMatrix = null;
		boolean inMatrix = false;
		while ((s = reader.readLine()) != null) {
			line++;
		  Matcher headerMatcher = headerPattern.matcher(s);
		  if (headerMatcher.matches() || inMatrix) {
		  	if (!inMatrix) {
		  		readMatrix = Lists.newArrayList();
		  		inMatrix = true;
		  	} else {
		      Matcher matcher = rowPattern.matcher(s);
		      if (matcher.matches()) {
		        String[] row = s.split("\\s+");
		        row = Arrays.copyOfRange(row, 1, row.length);
		      	readMatrix.add(row);
		      }else {
		      	inMatrix = false;
		      	plateMatrices.add(readMatrix);
		      }
		  	}
		  }
		}
    // in case there's no empty lines after last matrix line
		if(!plateMatrices.contains(readMatrix)) plateMatrices.add(readMatrix); 

		logger.info("read: " + line + ", matrices: " + plateMatrices.size());
		reader.close();			
		
		return plateMatrices;
  }

	public interface SheetHeaderWriter{
		public void writeHeaders(
		    WritableSheet sheet, int baseColumns, Map<String, 
		    Integer> valueColumnLabels /*, int inputSetNumber */) 
		        throws RowsExceededException, WriteException;
	}
	public interface WellWriter{
		public void writeWell(
		    WritableSheet sheet, int sheetRow, WellKey wellReadIn, int baseColumns) 
		        throws RowsExceededException, WriteException;
	}
	public interface WellValueWriter{
		public void writeWell(
		    WritableSheet sheet,  int sheetRow, int columnPosition, String rawValue) 
		        throws NumberFormatException, RowsExceededException, WriteException;
	}
	
	/**
	 * Write the set of combinedPlateMatrices out to an xls file.  
	 * @param combinedPlateMatrices an ordered list of matrices, 
	 * representing either x matrices for a matrixOrder of size x, or a combined 
	 * list of 
	 * 	 m*n matrices for n matrixOrders, where m is the sum of the sizes of 
	 *   the matrixOrders.
	 * @param plates the library plates in the order that they appear in the 
	 * combinedMatrices.
	 * @param matrixOrders {@link MatrixOrder} for each of the plateMatrixes read in,
	 *     the matrix order defines collation; output will be per plate.
	 */
	public static void writeParsedMatrices(
			String sheetNamePrefix,
			int aps, int lps,
			Integer[] plates, 
			List<MatrixOrderPattern> matrixOrders,
			List<List<String[]>> combinedPlateMatrices,
			SheetHeaderWriter sheetHeaderWriter,
			WellWriter wellWriter,
			WellValueWriter wellValueWriter,
			File outputFile) 
					throws IOException, RowsExceededException, WriteException
	{
		logger.info("Write result file...");
		WritableWorkbook workbook = Workbook.createWorkbook(outputFile);
		Map<Integer,WritableSheet> sheets = Maps.newHashMap();
		String[] baseColumns = new String[] { "Plate", "Well"};
		if(aps>lps){
		  baseColumns = new String[] { 
		      "Plate", "Well", "Source Plate", "Quadrant", "SourceWell"};
		}else if(lps>aps){
      baseColumns = new String[] { "Plate", "Well", "Quadrant", "Source Well"};
		}
		int wellCol = 1;
		
		int i=0;
		for(i=0; i<plates.length; ){
			String temp = "";
			if(!StringUtils.isEmpty(sheetNamePrefix)) temp = sheetNamePrefix + "_";
			int plate = plates[i];
			if(aps>lps && i%(aps/lps)==0){ // i.e. if 1536 aps
			  int j = i;
        String sourcePlateName = "" + plates[j++];
        sourcePlateName += "," + plates[j++];
        sourcePlateName += "," + plates[j++];
        sourcePlateName += "," + plates[j++];
        WritableSheet sheet = workbook.createSheet(sourcePlateName, i);
        sheets.put(plates[i++], sheet);
        sheets.put(plates[i++], sheet);
        sheets.put(plates[i++], sheet);
        sheets.put(plates[i++], sheet);
			}else{
			  sheets.put(plate,workbook.createSheet(temp + plate, i++));
			}
		}
		
		// Write the value column header labels, map header label to column position
		Map<String, Integer> cumulativeColumns = Maps.newHashMap();
		int cumulativeMatrixCount = 0;
		int inputSetNumber = 0;
		// First, build a list of value columns, mapped to their relative position 
		// to the first value column
		for(MatrixOrderPattern matrixOrder:matrixOrders) {
			Map<String,Integer> columns = matrixOrder.getColumnNamesToMatrixOrder();
			for(Map.Entry<String,Integer> entry:columns.entrySet()) {
				// adjust columns for cumulative position and entryset
				String colName = entry.getKey();
				int colPosition = entry.getValue() + cumulativeMatrixCount;
				if(cumulativeColumns.containsKey(colName)) {
          // have to adjust the column name if the user duplicates the inputs 
				  // for the collation order!
					//colName += "_" + inputSetNumber; 
					// (or maybe we should just require them to be different!)
					throw new IllegalArgumentException(
					    "repeated input params - each file input section must have a " + 
					    "unique set (change condition or readout names)!");
				}
				cumulativeColumns.put(colName, colPosition);
			}
			cumulativeMatrixCount += columns.size();
			inputSetNumber++;
		}
		logger.info("value columns: " + cumulativeColumns);
		// then write the headers to all the plate/sheets
		for(Integer plate:plates) {
			WritableSheet sheet = sheets.get(plate);
			for(int col=0;col<baseColumns.length;col++) {
		  	sheet.addCell(new jxl.write.Label(col, 0, baseColumns[col]));
			}
			sheetHeaderWriter.writeHeaders(sheet, baseColumns.length, cumulativeColumns); //, inputSetNumber);
		}

		// Now write the matrices
		cumulativeMatrixCount = 0;
		inputSetNumber = 0;
		for(MatrixOrderPattern matrixOrder:matrixOrders) {
			logger.info("writing matrix set: " + inputSetNumber);
			List<List<String[]>> plateMatrices = 
					combinedPlateMatrices.subList(
							cumulativeMatrixCount, 
							cumulativeMatrixCount+matrixOrder.getExpectedMatrixCount());
			i = 0;   // i == matrix(plate) number
			int plate = 0;
			int j=0; // j == plate/matrix row letter
			int k=0; // k == plate/matrix column number
			try
			{
				for(List<String[]> matrix:plateMatrices) 
				{
          // Source plate/well; use aps, lps in reverse order
          // FIXME: quadrant is always zero if not lps,aps?
				  int quadrant = 0;
          int sourcePlate = i;
          if(lps < aps){
            quadrant = sourcePlate%(aps/lps);
          }

          String colName = matrixOrder.getColName(i);
					if(!cumulativeColumns.containsKey(colName)) {
							throw new IllegalArgumentException(
							    "Programmer error: Unexpected column: " + colName);
					}
					int col = cumulativeColumns.get(colName) + baseColumns.length;
					plate = matrixOrder.getPlate(i);
					logger.info("matrix: " + i + ", cumulative matrix: " + (cumulativeMatrixCount+i) 
							+ ", plate: " + plate + ", colName: " + colName);
					WritableSheet sheet = sheets.get(plate);
					j=0;
					for(String[] row:matrix)
					{
            for(k=0;k<row.length;k++) // k=1 skip the row label
						{
              int sheetRow = 
                  quadrant*row.length*matrix.size() + j * (row.length) + k +1;
							String plateName = 
							    StringUtils.isEmpty(sheetNamePrefix) ? 
							        ""+plate : sheetNamePrefix + "_" + plate;
              // Plate
							sheet.addCell(new jxl.write.Label(0,sheetRow,plateName)); 
							//WellKey wellKey = new WellKey(plate,j,k-1 ); 
							// j==row (letter) and k==col (number)
              WellKey wellKey = new WellKey(plate,j,k ); 
							sheet.addCell(
							    new jxl.write.Label(wellCol,sheetRow, wellKey.getWellName()));
							
              if(aps>lps) {
                // source plate value is the sheet name
                // quadrant and source well
                sheet.addCell(
                    new jxl.write.Label(wellCol+1, sheetRow, "" + sheet.getName()));
                sheet.addCell(
                    new jxl.write.Label(wellCol+2, sheetRow, "" + (quadrant+1)));
                WellName sourceWell = convertWell(
                    new WellName(wellKey.getWellName()), lps, aps, quadrant);
                sheet.addCell(
                    new jxl.write.Label(wellCol+3, sheetRow, "" + sourceWell));
              }else if(lps>aps){
                // source plate value is the matrix #
                WellName sourceWell = convertWell(
                    new WellName(wellKey.getWellName()), lps, aps, quadrant);
                int internalQuadrant = deconvoluteMatrix(
                    lps,aps,wellKey.getRow(),wellKey.getColumn());
                sheet.addCell(new jxl.write.Label(wellCol+1, sheetRow, 
                              ""+ (internalQuadrant+1) ));
                logger.debug("convert: aps: " + aps + ", lps: " + lps + ", " + 
                    wellKey.getWellName() + ", to: " + sourceWell );
                sheet.addCell(new jxl.write.Label(wellCol+2, sheetRow, "" + sourceWell ));
              }
							wellWriter.writeWell(sheet, sheetRow, wellKey, baseColumns.length);
							wellValueWriter.writeWell(sheet, sheetRow, col, row[k]);
							
						}
						j++;
					}
					i++;
				}
			}catch(NumberFormatException e) {
				String msg = "Error parsing: matrix: " + i + 
				    " (plate: " + plate + "), row: " + getRowLetters(j) + ", col: " + k ;
				logger.warn(msg, e);
				throw new IOException(msg + e.getLocalizedMessage());
			}

			cumulativeMatrixCount += matrixOrder.getExpectedMatrixCount();
			inputSetNumber++;
		}// end cumulative matrix loop
		
		workbook.write();
		workbook.close();
		logger.info("Wrote " + outputFile);
	}
	
	/**
	 * Map the input well from a source screening plate to a destination 
	 * screening plate, using standard HTS interleaved mapping for 3 col : 2 row 
	 * aspect ratio screening plates
	 */
	public static int convoluteRow(
	    int source_plate_size, int dest_plate_size, int source_matrix_quadrant, 
	    int row)
	{
	  if(logger.isDebugEnabled()) 
	    logger.debug("convoluteRow: sps: " + source_plate_size 
	      + ", dps: " + dest_plate_size + ", smq: " + source_matrix_quadrant
	      + ", row: " + row );
    // note factor must be an integer value
		int factor = dest_plate_size/source_plate_size;  
		return row * factor/2 + source_matrix_quadrant/(factor/2);
	}

	/**
	 * Map the input well from a source screening plate to a destination 
	 * screening plate, using standard HTS mapping for 3 col : 2 row aspect ratio 
	 * screening plates.
	 */
	public static int convoluteCol(
	    int source_plate_size, int dest_plate_size, int source_matrix_quadrant, 
	    int col)
	{
    // note factor must be an integer value
		int factor = dest_plate_size/source_plate_size;  
		return col * factor/2 + source_matrix_quadrant%(factor/2);
	}

	/**
	 * Map the input well from a source screening plate to a destination 
	 * screening plate, using standard HTS interleaved mapping for 3 col : 2 row 
	 * aspect ratio screening plates.
	 */
	public static int deconvoluteMatrix(
	    int source_plate_size, int dest_plate_size, int row, int col)
	{
    // note factor must be an integer value
		int factor = source_plate_size/dest_plate_size;  
		return col%(factor/2) +  (row%(factor/2))*(factor/2);
	}
	
	/**
	 * Map the input well from a source screening plate to a destination 
	 * screening plate, using standard HTS interleaved mapping for 3 col : 2 row 
	 * aspect ratio screening plates.
	 * @param row using zero based index
	 */
	public static int deconvoluteRow(
	    int source_plate_size, int dest_plate_size, int row, int col)
	{
		int destMatrixNumber = deconvoluteMatrix(
		    source_plate_size, dest_plate_size, row, col);
    // note factor must be an integer value
		int factor = source_plate_size/dest_plate_size;  
		return row/(factor/2)+ row%(factor/2)-destMatrixNumber/(factor/2);
	}
	
	/**
	 * Map the input well from a source screening plate to a destination 
	 * screening plate, using standard HTS interleaved mapping for 3 col : 2 row 
	 * aspect ratio screening plates
	 * @param col using zero based index
	 */
	public static int deconvoluteCol(
	    int source_plate_size, int dest_plate_size, int row, int col)
	{
		int destMatrixNumber = deconvoluteMatrix(
		    source_plate_size, dest_plate_size, row, col);
    // note factor must be an integer value
		int factor = source_plate_size/dest_plate_size;  
		return col/(factor/2)+ col%(factor/2)-destMatrixNumber%(factor/2);
	}
	
	/**
	 * Convert source plate plateMatrices from one plate size to another size;
	 * either by combining quadrants into larger plates, or subdividing plates 
	 * into quadrants.
	 * @param sourcePlateSize
	 * @param destPlateSize
	 * @param plateMatrices
	 * @return
	 */
  public static List<List<String[]>> convertMatrixFormat(
      int sourcePlateSize, int destPlateSize,
      MatrixOrderPattern matrixOrder,
      List<List<String[]>> plateMatrices) 
  {
    if(sourcePlateSize==destPlateSize) 
    { // aps==lps
      return plateMatrices;
    }
    
    int destCols = getNumCols(destPlateSize);
    int destRows = getNumRows(destPlateSize);
    int srcCols = getNumCols(sourcePlateSize);
    int srcRows = getNumRows(sourcePlateSize);
    
    // convert the matrices if necessary from assay plate format to library 
    // plate format
    if(sourcePlateSize < destPlateSize) {
      
      // interleave to build the lps
      List<List<String[]>> combinedMatrices = Lists.newArrayList();
      
      if (destPlateSize % sourcePlateSize != 0 ) 
          throw new IllegalArgumentException(
              "Library plate size must be a multiple of assay plate size");
      int factor = destPlateSize/sourcePlateSize;
      if (plateMatrices.size() < factor || plateMatrices.size() % factor != 0 )
          throw new IllegalArgumentException(
              "Matrices read must be a multiple of " + factor);
      
      // collect by quadrant
      List<List<String[]>> q1matrices = Lists.newArrayList();
      List<List<String[]>> q2matrices = Lists.newArrayList();
      List<List<String[]>> q3matrices = Lists.newArrayList();
      List<List<String[]>> q4matrices = Lists.newArrayList();
      Object[] qms = new Object[] {
          q1matrices,q2matrices,q3matrices,q4matrices
      };
      
      for(int count = 0; count < plateMatrices.size();) {
        int q = count%4;
        if(matrixOrder!=null)
          q = matrixOrder.getQuadrant(count)-1;
        List<String[]> m = plateMatrices.get(count);
        ((List<List<String[]>>)qms[q]).add(m);
        count++;
      }      
      
      // iterate over combined-by-quadrant matrices
      for(int i=0; i< q1matrices.size(); i++) {
        List<String[]> combinedMatrix = Lists.newArrayList();
        for(int x=0;x<destRows;x++) {
          combinedMatrix.add(x,new String[destCols]);
        }
        for(int q=0; q<4; q++){
          List<String[]> quadrantMatrix = ((List<List<String[]>>)qms[q]).get(i);
          for(int j=0; j< srcRows; j++) {
            String[] sourceRow = quadrantMatrix.get(j);
            for(int k=0; k<srcCols; k++) {
              int destRow = convoluteRow(sourcePlateSize, destPlateSize, q, j);
              int destCol = convoluteCol(sourcePlateSize, destPlateSize, q, k);
              logger.debug("sourceMatrix: " + i + "(" + (q) + ")" + 
                  ", sourceRow: " + j + ", sourceCol: " + k + 
                  ", destRow: " + destRow + ", destCol: " + destCol);
              String[] destRowArray = combinedMatrix.get(destRow);
              destRowArray[destCol] = sourceRow[k];
            }
          }
        }// end quadrant matrices
        combinedMatrices.add(combinedMatrix);
      }
      return combinedMatrices;
    }else { // lps < aps
      // deconvoluting case
      if (sourcePlateSize % destPlateSize != 0 ) 
          throw new IllegalArgumentException(
              "Assay plate size must be a multiple of library plate size");
      int factor = sourcePlateSize/destPlateSize;
      
      // build output matrices
      List<List<String[]>> deCombinedMatrices = Lists.newArrayList();
      for(int k=0;k<plateMatrices.size()*factor; k++){
        List<String[]> temp = Lists.newArrayList();
        for(int i=0; i< destRows; i++) {
          temp.add(new String[destCols]);
        }
        deCombinedMatrices.add(temp);
      }
      
      int plate = 0;
      for(List<String[]> sourceMatrix:plateMatrices) {
        for(int i=0;i<srcRows;i++) {
          for(int j=0;j<srcCols;j++) {
            int destQuad = deconvoluteMatrix(sourcePlateSize,destPlateSize,i,j);
            int destRow = deconvoluteRow(sourcePlateSize,destPlateSize,i,j);
            int destCol = deconvoluteCol(sourcePlateSize,destPlateSize,i,j);
            int destMatrixNumber = destQuad;
            if(matrixOrder != null)
                destMatrixNumber = matrixOrder.getDeconvolutedCount(plate, destQuad);
            
            List<String[]> destMatrix = deCombinedMatrices.get(destMatrixNumber);
            destMatrix.get(destRow)[destCol] = sourceMatrix.get(i)[j];
          }
        }
        plate++;
      }
      return deCombinedMatrices;
    }
  }
  
  
  public static interface MatrixOrderPattern{
    public int getExpectedMatrixCount();
    public Integer getPlate(int matrixCount);
    public Integer getQuadrant(int matrixCount);
    public String getCondtion(int matrixCount);
    public String getReadout(int matrixCount);
    public String getReplicate(int matrixCount);
    public Map<String,Integer> getColumnNamesToMatrixOrder();
    public String getColName(int i);
    /**
     * A quadrant step is how many matrices separate each quadrant in the 
     * current collation.
     */
    public int getQuadrantStep();
    public List<?> getReading(int count);
    public int getSize();
    public int getDeconvolutedCount(int count, int quadrant);
    public MatrixOrderPattern getDeconvolutedMatrixOrder();
  }	
	
	 /**
   * Hack to make 1536 collation work, where input reads are always grouped by 
   * 4 386 well plates in the 4 quadrants of the 1536 well input.
   */
  public static class MatrixOrder1536 implements MatrixOrderPattern
  {
    private Integer[] originalPlates;
    private MatrixOrder matrixOrder;
    
    public MatrixOrder1536(CollationOrder ordering, Integer[] plates,
        String[] conditions, String[] readouts, String[] replicates) {
      this.originalPlates = plates;
      Integer[] plates1536 = new Integer[plates.length/4];
      for(int i=0;i<plates1536.length;i++) plates1536[i] = i;
      Integer[] quadrants = new Integer[] {1,2,3,4};
      
      List<PlateOrderingGroup> _ordering = Lists.newArrayList(ordering.getOrdering());
      _ordering.remove(PlateOrderingGroup.Quadrants);
      _ordering.add(_ordering.size(), PlateOrderingGroup.Quadrants);
      CollationOrder ordering1536 = new CollationOrder(_ordering);
      this.matrixOrder = 
          new MatrixOrder(ordering1536, plates1536, conditions, readouts, 
                          replicates, quadrants);
    }

    @Override
    public int getExpectedMatrixCount() {
      return this.matrixOrder.getExpectedMatrixCount();
    }

    @Override
    public Integer getPlate(int matrixCount) {
      int quadrant = this.matrixOrder.getQuadrant(matrixCount);
      int plate1536 = this.matrixOrder.getPlate(matrixCount);
      return this.originalPlates[plate1536*4+(quadrant-1)];
    }

    @Override
    public String getCondtion(int matrixCount) {
      return this.matrixOrder.getCondtion(matrixCount);
    }

    @Override
    public String getReadout(int matrixCount) {
      return this.matrixOrder.getReadout(matrixCount);
    }

    @Override
    public String getReplicate(int matrixCount) {
      return this.matrixOrder.getReplicate(matrixCount);
    }

    @Override
    public Integer getQuadrant(int matrixCount) {
      return this.matrixOrder.getQuadrant(matrixCount);
    }

    @Override
    public Map<String, Integer> getColumnNamesToMatrixOrder() {
      return this.matrixOrder.getColumnNamesToMatrixOrder();
    }

    public String getColName(int i) { 
      return this.matrixOrder.getColName(i);
    }
    
    public int getQuadrantStep() {
      return this.matrixOrder.getQuadrantStep();
    }
    @Override
    public List<?> getReading(int count) {
      return this.matrixOrder.getReading(count);
    }
    
    @Override
    public int getSize() {
      return this.matrixOrder.getSize();
    }
    
    @Override
    public int getDeconvolutedCount(int count, int quadrant) {
      return this.matrixOrder.getDeconvolutedCount(count, quadrant);
    }
    @Override
    public MatrixOrderPattern getDeconvolutedMatrixOrder() {
      return this.matrixOrder.getDeconvolutedMatrixOrder();
    }
  }

 
	/**
	 * Specialized "Odometer" for counting through source assay plates collated using 
	 * a combination of LibraryPlate, condition, readout and replicate ordering.
	 */
  public static class MatrixOrder implements MatrixOrderPattern
  {
  	private Odometer odometer;
		private CollationOrder ordering;
    private int platePosition;
    private int quadrantPosition;
		private int conditionPosition;
		private int readoutPosition;
		private int replicatePosition;
		private Integer[] plates;
		private Integer[] quadrants;
		private String[] conditions;
		private String[] readouts;
		private String[] replicates;
		
		
		/**
		 * A quadrant step is how many matrices are between each quadrant
		 * @return
		 */
		public int getQuadrantStep() {
		  int qstep = 1;
		  if(this.quadrantPosition != 4){
		    for(int i=0; i!=this.quadrantPosition; i++){
		      qstep *= this.odometer.getCounterSize(i);
		    }
		  }
		  return qstep;
		}
		
		public int getSize(){
		  return this.odometer.getSize();
		}
		
		public List<?> getReading(int count){
		  return this.odometer.getReading(count);
		}
		
		public int getCount(List<?> reading){
		  return this.odometer.getCount(reading);
		}
		
		public MatrixOrderPattern getDeconvolutedMatrixOrder(){
      MatrixOrder internalOrder = 
          new MatrixOrder(ordering, plates, conditions, readouts, replicates, 
                          new Integer[]{1,2,3,4});
		  return internalOrder;
		}
		
		/**
		 * Use count to get a reading, adjust reading to the quadrant value, then
		 * use the new reading to get a new count.
		 */
		public int getDeconvolutedCount(int count, int quadrant){
		  MatrixOrder internalOrder = 
		      new MatrixOrder(ordering, plates, conditions, readouts, replicates, 
		                      new Integer[]{0});
		  List<Object> reading = (List<Object>)internalOrder.getReading(count);
		  
		  reading.set(this.quadrantPosition, (Object)new Integer(quadrant+1));
		  return getCount(reading);
		}
		
		public int getNextByQuadrant(int count, int newQuadrant){
		  List<?> reading = getReading(count);
		  // construct new reading, with the new quadrant
		  // note that this is klunky due to collections interface
		  Object[] newReading = new Object[reading.size()];
		  for(int i=0;i<reading.size();i++){
		    if(i==quadrantPosition){
		      newReading[i] = newQuadrant;
		    }else{
		      newReading[i] = reading.get(i);
		    }
		  }
      if (logger.isDebugEnabled())
        logger.debug(
          "count: " + count + ", reading: " + reading + 
          ", newReading: " + ImmutableList.of(newReading));
		  return getCount(ImmutableList.of(newReading));
		}
		
    public MatrixOrder(CollationOrder ordering, Integer[] plates, String[] conditions, 
        String[] readouts, String[] replicates)
    {
      // create a default case for the non-1536 reads, where we aren't using quadrants.
      // note, if 96 input weren't converted before writing, would be needed for that
      this(ordering, plates, conditions, readouts, replicates, new Integer[] {0});
    }
    public MatrixOrder(
        CollationOrder ordering, Integer[] plates, String[] conditions, 
        String[] readouts, String[] replicates, Integer[] quadrants)
      {
  		this.ordering = ordering;
  		this.plates = plates;
  		this.quadrants = quadrants;
  		this.conditions = conditions;
  		this.readouts = readouts;
  		this.replicates = replicates;
  		
			List<List<?>> orderings = Lists.newArrayList();
			int i = 0;
			for(PlateOrderingGroup o:ordering) {
				switch(o) {
				case Plates:
					orderings.add(0,Arrays.asList(plates));
          // TODO: clean up magic numbers in array reversing 
					// - for the ordering group, first position is 
					// highest significance, last lowest, so reversing here
					this.platePosition = 4-i; 
					i++;
					break;
				case Quadrants:
          orderings.add(0,Arrays.asList(quadrants));
          this.quadrantPosition = 4-i;
          i++;
          break;
				case Conditions:
					orderings.add(0,Arrays.asList(conditions));
					this.conditionPosition = 4-i;
					i++;
					break;
				case Readouts:
					orderings.add(0,Arrays.asList(readouts));
					this.readoutPosition = 4-i;
					i++;
					break;
				case Replicates:
					orderings.add(0,Arrays.asList(replicates));
					this.replicatePosition = 4-i;
					i++;
					break;
				default:
					throw new IllegalArgumentException("unknown ordering: " + o);
				}
			}
  		this.odometer = new Odometer(orderings.toArray(new List<?>[] {}));
  	}
  	
  	public String getColName(int i) { 
  		String colName = getReadout(i);
  		if (this.conditions.length > 1) colName += "_"+ getCondtion(i);
  		if (this.replicates.length > 1) colName += "_" + getReplicate(i);
  		return colName;
		}
  	
  	public Map<String,Integer> getColumnNamesToMatrixOrder()
  	{
			Map<String,Integer> columns = Maps.newHashMap();

			
			// Columns will be defined by Readout_Condition_Replicate
			// Columns will be in the order of the collation order
			List<String> columnNames = Lists.newArrayList();
			for(int k=0;k<this.odometer.getSize();k++) {
				String name = this.getReadout(k);
				if(conditions.length > 1) name += "_" + this.getCondtion(k);
				if(replicates.length > 1) name += "_" + this.getReplicate(k);
				if(!columnNames.contains(name)) columnNames.add(name);
			}
			for(int i=0;i<columnNames.size();i++) columns.put(columnNames.get(i),i);
			return columns;
  	}

		public int getExpectedMatrixCount()
  	{
  		return this.odometer.getSize();
  	}
  	
  	public Integer getPlate(int matrixCount)
  	{
  		return (Integer)this.odometer.getReading(matrixCount).get(this.platePosition);
  	}
  	public String getCondtion(int matrixCount)
  	{
  		return (String)this.odometer.getReading(matrixCount).get(this.conditionPosition);
  	}
  	public String getReadout(int matrixCount)
  	{
  		return (String)this.odometer.getReading(matrixCount).get(this.readoutPosition);
  	}
  	public String getReplicate(int matrixCount)
  	{
  		return (String)this.odometer.getReading(matrixCount).get(this.replicatePosition);
  	}

    @Override
    public Integer getQuadrant(int matrixCount) {
      return (Integer)this.odometer.getReading(matrixCount).get(this.quadrantPosition);
    }
    
    public CollationOrder getOrder(){
      return this.ordering;
    }
  }
  

  /**
   * Load arrays and iterate through them in a defined order; so that each 
   * combination of one value from each array corresponds to a defined count 
   * value.
   */
  public static class Odometer
  {
  	private List<?>[] counters;
  	private String toString;
  	private int size;

  	/** 
  	 * Load the counters with the least significant digit first - so the 
  	 * opposite of how normal numbers are thought of (but not displayed, 
  	 * i.e. left digit is most significant, but right is least and read first).
  	 * So, decimal numbers would be loaded: 1,000 dec loads as "0001".
  	 * @param counters
  	 */
		public Odometer(List<?> ... counters)
  	{
  		this.counters = counters;
  		this.size = 1;
  		StringBuffer buf = new StringBuffer("Odometer: ");
      // iterate backwards, so as to display the odometer with left digits as 
  		// most significant, like arabic numerals
			for(int i=0;i<counters.length;i++) { 
				List<?> list = counters[counters.length-i-1];
				if(list.isEmpty()) 
				    throw new IllegalArgumentException(
				        "Lists used for the odometer must not be empty.");
				buf.append("[" + Joiner.on(",").join(list) + "]");
				size *= list.size();
			}
			toString = buf.toString();
  	}
		
		public int getSize() { return this.size; }
		
		public int getCounterSize(int counterPosition){
		  return this.counters[counterPosition].size();
		}
		
		public int getCount(List<?> reading){
		  
		  int position = 0;
		  int count = 0;
		  int[] counterPositions = new int[counters.length];
		  for(Object o:reading){
		    List<?> counter = counters[position];
		    int counterPosition = 0;
		    for(Object counterObject:counter){
		      if(counterObject.equals(o)) break;
		      counterPosition++;
		    }
		    counterPositions[position] = counterPosition;
		    position++;
		  }
		  
		  int place = 1;
		  for(int i=0;i<counterPositions.length; i++){
		    count += counterPositions[i] * place;
		    place = place*counters[i].size();
		  }
		  return count;
		}
		
		public List<?> getReading(int count)
		{
			if (count > this.getSize() ) 
				throw new IllegalArgumentException(
				    "count requested: " + count + " exceeds this counter's size: " + getSize() );
			
			List<Object> reading = Lists.newArrayList();
			int i = 0;
			int cumulative = -1;
			for(List<?> list:counters) {
				if(cumulative == -1) {
					int counter = count % list.size();
					reading.add(list.get(counter));
					cumulative = list.size();
				}else {
					int counter = (count / cumulative) % list.size();
					reading.add(list.get(counter));
					cumulative *= list.size();
				}
			}
			return reading;
		}
		
		public String toString()
		{
			return this.toString;
		}
  }

  /**
   * Expand a user-entered list of plates, and plate ranges into a list of plates.
   */
	public static Integer[] expandPlatesArg(String temp) {
		List<Integer> plates = Lists.newArrayList();
		
		String[] plateArgs = temp.split(",");
		
		for (String arg:plateArgs) {
			arg = arg.trim();
			if (arg.contains("-")) {
				String[] range = arg.split("-");
				if(range.length != 2) {
					throw new IllegalArgumentException("range is incorrect: " + arg);
				}
				int begin = Integer.parseInt(range[0]);
				int end = Integer.parseInt(range[1]);
				// for issue #105 Preserve the user entered ordering for the plate list
        //if (end<begin) { int tmp=end; end=begin; begin=tmp; }
				int dir = 1;
        if (end<begin) dir = -1;
				for(int i=begin; i != end+dir; ){
				  plates.add(i);
				  i += dir;
				}
			}else {
				plates.add(Integer.parseInt(arg));
			}
		}
		
		return plates.toArray(new Integer[] {});
	}	
  
	/**
	 * Parse screening plate row letter index into a zero based index for that letter.
	 */
  public static int getRow(String rowLetter) {
  	rowLetter = rowLetter.toUpperCase();
  	if(rowLetter.length()==2) {
  		if(rowLetter.charAt(0) != 'A') 
  		    throw new IllegalArgumentException(
  		        "Two letter row names must begin with 'A' (only 1536 size plates allowed");
  		return 25 + rowLetter.charAt(1)-'A';
  	}else if (rowLetter.length() ==1 ) {
  		return rowLetter.charAt(0) - 'A';
  	}else {
  		throw new IllegalArgumentException(
  		    "Row letters may be either one or two characters long.");
  	}
  }

  /**
   * Convert screening plate zero based row index into screening plate row 
   * index letters.
   */
  public static String getRowLetters(int row) {
  	if(row < 0 || row > 31) 
  	    throw new IllegalArgumentException(
  	        "Row value outside of allowed range (0-31): " + row);
  	if(row > 25) {
  		return "A" + (char)( ((int)'A')+ (row-26));
  	}else {
  		return "" + (char)((int)'A' + row);
  	}
  }
  
	/**
	 * Assume standard screening matrix aspect ratio: i.e. cols:3 to rows:2
	 */
  public static int getNumCols(int plateSize) {
		return (int)Math.sqrt(3*plateSize/2);
  }
  
	/**
	 * Assume standard screening matrix aspect ratio: i.e. cols:3 to rows:2
	 */
  public static int getNumRows(int plateSize) {
		return (int)Math.sqrt(2*plateSize/3);
  }
  
  public static Set<Integer> allowedPlateSizes = Sets.newHashSet(new Integer[] { 96,384,1536 });
  
  /**
   * Convert a row/column well name matrix index from the source plate size 
   * into the destination plate size.
   * @param sourceWellName
   * @param sourcePlateSize
   * @param destinationPlateSize
   * @param sourceQuadrant either [0,1,2,3] if sourcePlateSize<destPlateSize, 
   *        otherwise, ignored
   * @return
   */
  public static WellName convertWell(
      WellName sourceWellName, int sourcePlateSize, int destinationPlateSize, 
      int sourceQuadrant) 
  {
  	if (sourceQuadrant < 0 || sourceQuadrant > 3){ 
  	  throw new IllegalArgumentException("Source quadrant must from 0 to 3.");
  	}
  	if(!allowedPlateSizes.contains(sourcePlateSize) 
  	    || ! allowedPlateSizes.contains(destinationPlateSize) ) {
  		throw new IllegalArgumentException(
  		    "Unknown plate size: " + sourcePlateSize + ", " + destinationPlateSize);
  	}
  	int sourceRow = sourceWellName.getRowIndex();
  	int sourceCol = sourceWellName.getColumnIndex();
  	
  	if(sourcePlateSize==destinationPlateSize) return sourceWellName;
  	if(sourcePlateSize > destinationPlateSize)
  	{
  		return new WellName(
  		    deconvoluteRow(sourcePlateSize, destinationPlateSize, sourceRow, sourceCol),
  				deconvoluteCol(sourcePlateSize, destinationPlateSize, sourceRow, sourceCol));
  	}else {
  		return new WellName(
  		    convoluteRow(sourcePlateSize, destinationPlateSize, sourceQuadrant, sourceRow), 
  				convoluteCol(sourcePlateSize, destinationPlateSize, sourceQuadrant, sourceCol));
  	}
  }
  
  /**
   * Parse user input for a (newline separated list of) labeled well ranges 
   * (wells and well ranges) - see
   * {@link PlateReaderRawDataParser#expandWellRange(String, int)}, 
   * where each range is followed on its
   * line by an equal sign (&quot;=&quot;) and then the label for that range.
   * @param input
   * @param plateSize
   * @return
   */
  public static Map<String,Set<WellName>> expandNamedWellRanges(
      String input, int plateSize)
  {
  	Map<String,Set<WellName>> output = Maps.newHashMap();
		if (StringUtils.isEmpty(input)) return output;
		// first split args
		String[] inputs = input.trim().split("\\n");
  	for(String temp:inputs) {
  		temp = temp.trim();
  		String[] rangeToLabel = temp.split("=");
  		String label = "";
  		String unparsedRange = rangeToLabel[0];
  		if(rangeToLabel.length == 2) label = rangeToLabel[1].replace("\"", "");
  		else if(rangeToLabel.length > 2) 
  		    throw new IllegalArgumentException(
  		        "range to label inputs may only have one equal sign per line: re: " + temp);
  		
  		Set<WellName> parsedRange = expandWellRange(unparsedRange, plateSize);
  		
  		if(output.containsKey(label)) output.get(label).addAll(parsedRange);
  		else output.put(label, parsedRange);
  		
  	}
		return output;
  }

  /**
   * Parse user input for a (comma separted list of) wells and well ranges, in 
   * the form of
   * <ul>
   * <li> single well specifiers
   * <li> single row or column specifiers
   * <li> well blocks, defined by the upper left to the lower right well
   * <li> column or row blocks
   * </ul>
   * Well range elements are separated by a dash (&quot;-&quot;).
   * @param input
   * @param plateSize
   * @return
   */
  public static Set<WellName> expandWellRange(String input, int plateSize)
  {
		Set<WellName> output = Sets.newHashSet();
		if (StringUtils.isEmpty(input)) return output;
		// first split args
		String[] inputs = input.trim().split(",");
		
		
		for(String temp:inputs) {
			temp = temp.trim();
			String[] range = temp.split("-");
			if(range.length == 2) {
				if (rowOnlyPattern.matcher(range[0]).matches()) {
					if (!(rowOnlyPattern.matcher(range[1]).matches())) 
					    throw new IllegalArgumentException(
					        "Both values of the range must be the same type, range: " + temp);
					int startRow = getRow(range[0]);
					int stopRow = getRow(range[1]);
					if(startRow>stopRow) {
						int tempVal=startRow; startRow=stopRow; stopRow=tempVal;
					}
					for(int i=1;i<=getNumCols(plateSize);i++) {
						for(int j=startRow; j<=stopRow; j++)
						{
							output.add(new WellName(j,i));
						}
					}
				}else if (columnOnlyPattern.matcher(range[0]).matches()) {
					if (!(columnOnlyPattern.matcher(range[1]).matches())) 
					    throw new IllegalArgumentException(
					        "Both values of the range must be the same type, range: " + temp);
					int startCol = Integer.parseInt(range[0]);
					int stopCol = Integer.parseInt(range[1]);
					if(startCol>stopCol) {
						int tempVal = startCol; startCol=stopCol; stopCol=tempVal;
					}
					for(int i=startCol; i<=stopCol; i++) {
						for(int j=0; j<getNumRows(plateSize); j++) {
							output.add(new WellName(j,i-1));
						}
					}
				} else { // block defined by wells
					Matcher matcher1 = WellName.WELL_NAME_PATTERN.matcher(range[0]);
					Matcher matcher2 = WellName.WELL_NAME_PATTERN.matcher(range[1]);
					
					if(!(matcher1.matches() && matcher2.matches())) 
					    throw new IllegalArgumentException(
					        "Both values in the range must be well patterns, col patterns," + 
					        " or row patterns: " + temp);
					
					WellName one = new WellName(range[0]);
					WellName two = new WellName(range[1]);
					
					int startRow = one.getRowIndex();
					int stopRow = two.getRowIndex();
					if (startRow>stopRow) {
						int tempVal=startRow; startRow=stopRow; stopRow=tempVal;
					}
					int startCol = one.getColumnIndex();
					int stopCol = two.getColumnIndex();
					
					if(startCol>stopCol) {
						int tempVal = startCol; startCol=stopCol; stopCol=tempVal;
					}
					for(int i=startCol; i<=stopCol; i++) {
						for(int j=startRow; j<=stopRow; j++) {
							output.add(new WellName(j,i));
						}
					}
				}
			}else if (range.length == 1) {
				if (rowOnlyPattern.matcher(range[0]).matches()) {
					int rowStart = new WellName(range[0] + 1).getRowIndex();
					for(int i=0;i<getNumCols(plateSize);i++) {
						output.add(new WellName(rowStart,i));
					}
				}else if(columnOnlyPattern.matcher(range[0]).matches()) {
          // subtract 1, since user input is mean to be 1's based, 
				  // and wellname const expects zero based
					int colStart = new WellName(0,Integer.parseInt(range[0])).getColumnIndex()-1; 
					for (int j=0;j<getNumRows(plateSize);j++) {
						output.add(new WellName(j,colStart));
					}
				} else {
					if(!WellName.WELL_NAME_PATTERN.matcher(range[0]).matches()) 
					    throw new IllegalArgumentException(
					        "Value must be a well pattern, col pattern, or row pattern: " + temp);
					output.add(new WellName(range[0]));
				}
			}else {
				throw new IllegalArgumentException("Invalid well range: " + temp);
			}
		}
		return output;
  }
  
}
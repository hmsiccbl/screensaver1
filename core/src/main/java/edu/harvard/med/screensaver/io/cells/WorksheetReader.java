package edu.harvard.med.screensaver.io.cells;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import com.google.common.collect.Lists;

import edu.harvard.med.screensaver.io.workbook2.Row;
import edu.harvard.med.screensaver.io.workbook2.Workbook;
import edu.harvard.med.screensaver.io.workbook2.Worksheet;

/* Stateful utility for iterating through a worksheet by lines, translating each row into a String[] <br/>
 * <br/>
 * Each row is **/
public class WorksheetReader 
{
	private static final Logger log = Logger.getLogger(CellParser.class);

	private Workbook workbook = null;
	private Worksheet worksheet = null;
	private Iterator<Row> rowsIterator;
	private int linesRead = 0;
	
	public WorksheetReader( File file) throws FileNotFoundException 
	{
		workbook = new Workbook(file);
		worksheet = workbook.getWorksheet(0).forOrigin(0, 0);
		rowsIterator = worksheet.iterator();
	}

	// TODO: rework, supporting column definitions, so that we aren't converting each row to a string
	public String[] parseNext() {
		List<String> cells = Lists.newArrayList();
		if (rowsIterator.hasNext()) {
			linesRead++;
			int i = 0;
			for (edu.harvard.med.screensaver.io.workbook2.Cell cell : rowsIterator.next()) {
				if (i++ == 0 && StringUtils.isEmpty(cell.getAsString()))
					return null; // if cell 1 is empty, return
				cells.add(cell.getAsString());
			}
			return cells.toArray(new String[] {});
		}
		return null;
	}

	public int getLinesRead() {
		return linesRead;
	}

}
package edu.harvard.med.screensaver.service.screenresult
import edu.harvard.med.iccbl.platereader.FindWellType
import edu.harvard.med.screensaver.db.LibrariesDAO
import edu.harvard.med.screensaver.model.libraries.WellKey
import edu.harvard.med.screensaver.model.screens.Screen
import edu.harvard.med.iccbl.platereader.parser.PlateOrdering
import edu.harvard.med.screensaver.model.libraries.WellName
import edu.harvard.med.screensaver.model.screenresults.AssayWellControlType
import edu.harvard.med.iccbl.platereader.ScreenResultWriter
import edu.harvard.med.iccbl.platereader.parser.PlateReaderRawDataParser
import edu.harvard.med.iccbl.platereader.parser.TabDelimitedPlateReaderRawDataParser
import edu.harvard.med.screensaver.model.libraries.PlateSize
import org.springframework.transaction.annotation.Transactional
import scala.collection.JavaConversions._

class PlateReaderRawDataTransformer (librariesDao: LibrariesDAO) {
  
  // for CGLIB2
  private[screenresult] def this() = this(null)

  private object FindWellTypeImpl extends FindWellType {
    def apply(wellKey: WellKey) = librariesDao.findWell(wellKey).getLibraryWellType
  }
  
  case class Result(outputFile: java.io.File,
                    screen: Screen,
                    plateSize: PlateSize,
                    ordering: PlateOrdering, 
                    controls: java.util.Map[WellName, AssayWellControlType]) {
  }

  @Transactional                
  def transform(input: java.io.Reader,
                outputFile: java.io.File,
                screen: Screen,
                plateSize: PlateSize,
                ordering: PlateOrdering, 
                controls: java.util.Map[WellName, AssayWellControlType]) = {
    val parser: PlateReaderRawDataParser = new TabDelimitedPlateReaderRawDataParser(plateSize.getRows, plateSize.getColumns)
    // TODO: verify parsed data size matches size expected by the ordering
    // TODO: verify plate numbers have been screened
    // TODO: verify controls are only for empty wells
    val plateMatrices = parser.parse(input)
    val srw = new ScreenResultWriter(plateMatrices, ordering, controls.toMap, FindWellTypeImpl)
    srw.write(outputFile)
    new Result(outputFile,
               screen,
               plateSize,
               ordering,
               controls)
  }
}
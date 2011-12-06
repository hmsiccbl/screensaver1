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
import java.io.StringReader
import edu.harvard.med.iccbl.platereader.parser.EnvisionRawDataCleaner

class PlateReaderRawDataTransformer(librariesDao: LibrariesDAO) {

  // for CGLIB2
  private[screenresult] def this() = this(null)

  private object FindWellTypeImpl extends FindWellType {
    def apply(wellKey: WellKey) = librariesDao.findWell(wellKey).getLibraryWellType
  }

  case class Result(outputFile: java.io.File,
                    screen: Screen,
                    plateSize: PlateSize,
                    ordering: PlateOrdering,
                    controls: java.util.Map[WellName, AssayWellControlType],
                    plateMatricesProcessedCount: Int,
                    platesProcessedCount: Int) {
    def getPlateMatricesProcessedCount = plateMatricesProcessedCount
  }

  @Transactional
  def transform(input: java.io.Reader,
                outputFile: java.io.File,
                platePerWorksheet: Boolean,
                screen: Screen,
                plateSize: PlateSize,
                ordering: PlateOrdering,
                controls: java.util.Map[WellName, AssayWellControlType]) = {
    val parser: PlateReaderRawDataParser = new TabDelimitedPlateReaderRawDataParser(plateSize.getRows, plateSize.getColumns)
    // TODO: verify plate numbers have been screened
    val plateMatrices = parser.parse(input)
    val srw = new ScreenResultWriter(plateMatrices, ordering, controls.toMap, FindWellTypeImpl)
    if (outputFile != null) {
      srw.write(outputFile, platePerWorksheet)
    }
    new Result(outputFile,
      screen,
      plateSize,
      ordering,
      controls,
      plateMatrices.size,
      ordering.take(plateMatrices.size).map(_.plate.get).toSet.size)
  }
}
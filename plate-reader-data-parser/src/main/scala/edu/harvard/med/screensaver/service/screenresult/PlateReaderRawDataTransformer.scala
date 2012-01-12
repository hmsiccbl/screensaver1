package edu.harvard.med.screensaver.service.screenresult
import scala.collection.JavaConversions._
import org.springframework.transaction.annotation.Transactional
import edu.harvard.med.iccbl.platereader.parser.PlateOrdering
import edu.harvard.med.iccbl.platereader.parser.PlateReaderRawDataParser
import edu.harvard.med.iccbl.platereader.parser.TabDelimitedPlateReaderRawDataParser
import edu.harvard.med.iccbl.platereader.ColumnsWriter
import edu.harvard.med.iccbl.platereader.FindSilencingReagent
import edu.harvard.med.iccbl.platereader.FindWellType
import edu.harvard.med.iccbl.platereader.PlateMatrix
import edu.harvard.med.iccbl.platereader.PreLoadedControlsWriter
import edu.harvard.med.iccbl.platereader.RNAiColumnsWriter
import edu.harvard.med.iccbl.platereader.ScreenResultWriter
import edu.harvard.med.screensaver.db.LibrariesDAO
import edu.harvard.med.screensaver.io.libraries.WellMetaData
import edu.harvard.med.screensaver.model.libraries.PlateSize
import edu.harvard.med.screensaver.model.libraries.SilencingReagent
import edu.harvard.med.screensaver.model.libraries.WellKey
import edu.harvard.med.screensaver.model.screens.Screen
import edu.harvard.med.screensaver.model.screens.ScreenType
import edu.harvard.med.screensaver.io.libraries.ControlWell
import edu.harvard.med.screensaver.model.libraries.WellName
import edu.harvard.med.screensaver.io.libraries.WellNameLabel
import edu.harvard.med.iccbl.platereader.IccblQuadrantMapping
import edu.harvard.med.iccbl.platereader.AssayPlateWellAndQuadrantWriter

class PlateReaderRawDataTransformer(librariesDao: LibrariesDAO) {

  // for CGLIB2
  private[screenresult] def this() = this(null)

  private object FindWellTypeImpl extends FindWellType {
    def apply(wellKey: WellKey) = librariesDao.findWell(wellKey).getLibraryWellType
  }

  case class Result(outputFile: java.io.File,
                    //screen: Screen,
                    //assayPlateSize: PlateSize,
                    //libraryPlateSize: PlateSize,
                    //ordering: PlateOrdering,
                    //controls: java.util.Set[WellMetaData],
                    plateMatricesProcessedCount: Int,
                    libraryPlatesProcessedCount: Int) {
    def getPlateMatricesProcessedCount = plateMatricesProcessedCount
  }

  val findSilencingReagent = new FindSilencingReagent() {
    def apply(wk: WellKey) = {
      val well = librariesDao.findWell(wk)
      if (well == null) null else
        well.getLatestReleasedReagent.asInstanceOf[SilencingReagent]
    }
  }

  /** maps assay plate controls back to the corresponding library plate wells (e.g. from 96-well assay plates to 384-well assay plates) */
  def mapControlsToLibraryPlateSize(assayControls: Set[ControlWell],
                                    assayPlateSize: PlateSize,
                                    libraryPlateSize: PlateSize) = {
    if ((libraryPlateSize.getWellCount % assayPlateSize.getWellCount) % 4 != 0)
      throw new IllegalArgumentException("library plate size must be a 4x multiple of assayPlateSize")
    val multiple = libraryPlateSize.getWellCount / assayPlateSize.getWellCount
    val factor = Math.sqrt(multiple).toInt
    if (multiple == 1) assayControls else {
      val quadrantLabelsRE = """(.*?)\|(.*)""".r
      def controlLabel(l: Option[String], i: Int): Option[String] = l match {
        case Some("") => None
        case Some(quadrantLabelsRE(f, r)) => {
          if (i == 0) { if (f.isEmpty()) None else Some(f) } else controlLabel(Option(r), i - 1)
        }
        case _ => l
      }
      def mappedControlWell(ac: ControlWell, i: Int) =
        // TODO: this mapping is the inverse of what the QuadrantMapping class performs, and it would be beneficial to define the inverse mapping in that class, since the below is a fixed mapping strategy
        ControlWell(new WellName(ac.wellName.getRowIndex * factor + (i / factor).toInt, ac.wellName.getColumnIndex * factor + (i % factor)), controlLabel(ac.label, i), ac.controlType)
      val r = assayControls flatMap { c => for (i <- 0 until multiple) yield mappedControlWell(c, i) }
      r
    }
  }

  @Transactional
  def transform(input: java.io.Reader,
                outputFile: java.io.File,
                platePerWorksheet: Boolean,
                screen: Screen,
                assayPlateSize: PlateSize,
                libraryPlateSize: PlateSize,
                ordering: PlateOrdering,
                assayControls: java.util.Set[ControlWell],
                libraryControls: java.util.Set[WellNameLabel]) = {
    val parser: PlateReaderRawDataParser = new TabDelimitedPlateReaderRawDataParser(assayPlateSize)
    val assayPlateMatrices = parser.parse(input)

    val allControls = mapControlsToLibraryPlateSize(assayControls.toSet, assayPlateSize, libraryPlateSize) ++ libraryControls.toSet

    val quadMapping = new IccblQuadrantMapping(assayPlateSize, libraryPlateSize)
    val srw = new ScreenResultWriter(assayPlateMatrices, ordering, allControls, FindWellTypeImpl, libraryPlateSize, quadMapping)
    if (screen.getScreenType() == ScreenType.RNAI) {
      if(assayPlateSize != libraryPlateSize)
        srw.colWriters += new AssayPlateWellAndQuadrantWriter(quadMapping)
      srw.colWriters += new PreLoadedControlsWriter(allControls)
      srw.colWriters += new RNAiColumnsWriter(findSilencingReagent)
    }
    if (outputFile != null) {
      srw.write(outputFile, platePerWorksheet)
    }
    Result(outputFile,
      //screen,
      //assayPlateSize,
      //libraryPlateSize,
      //ordering,
      //allControls,
      assayPlateMatrices.size,
      ordering.take(assayPlateMatrices.size).map(_.plate.get).toSet.size)
  }
}
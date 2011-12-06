package edu.harvard.med.iccbl.platereader
import edu.harvard.med.iccbl.platereader.parser.PlateOrdering
import edu.harvard.med.screensaver.model.libraries.WellKey
import edu.harvard.med.iccbl.platereader.parser.PlateMetaData
import scala.collection.immutable.TreeSet
import java.io.File
import jxl.write.WritableWorkbook
import jxl.Workbook
import edu.harvard.med.screensaver.model.libraries.LibraryWellType
import edu.harvard.med.screensaver.model.libraries.WellName
import edu.harvard.med.screensaver.model.screenresults.AssayWellControlType
import jxl.write.WritableSheet
import scala.collection.SortedSet
import scala.collection.SortedMap
import scala.collection.immutable.TreeMap
import edu.harvard.med.screensaver.model.libraries.AssayWellControlTypeException
import edu.harvard.med.screensaver.model.screens.AssayReadoutType
import scala.collection.mutable.LinkedHashSet

trait FindWellType extends (WellKey => LibraryWellType)

class ScreenResultWriter(data: Seq[PlateMatrix], ordering: PlateOrdering, controls: Map[WellName, AssayWellControlType], findWellType: FindWellType) {

  private def sansPlate(pmd: PlateMetaData) = PlateMetaData(condition = pmd.condition, readoutType = pmd.readoutType, replicate = pmd.replicate, readout = pmd.readout)

  private val collated = ordering.collate(data)

  val rowKeys = TreeSet.empty[WellKey] ++ (for { e <- collated; w <- e._2.wellNames } yield new WellKey(e._1.plate.get, w))
  /* HACK: we are using PlateMetaData objects to represent our column descriptions, since PlateMetaData contains all of the pertinent information; 
   * however, we ignore its plateNumber property */
  // note: use of LinkedHashSet maintains expected column ordering
  val colKeys = LinkedHashSet() ++ (ordering.iterator map sansPlate)
  def readoutTypeIsMultiReplicate(readoutType: AssayReadoutType) = colKeys exists ((x) => { x.readoutType.get == readoutType && x.replicate > 1 })
  val fixedColumnHeaders = List("Plate", "Well", "Type", "Exclude")

  def lookup(wk: WellKey, col: PlateMetaData) = {
    val plateKey = col.copy(plate = Some(wk.getPlateNumber))
    if (collated.contains(plateKey))
      Some(collated(plateKey).well(wk.getRow, wk.getColumn))
    else None
  }

  def write(file: File, platePerWorksheet: Boolean = true) {
    val wbk = Workbook.createWorkbook(file);
    if (platePerWorksheet)
      writeSheets(wbk)
    else
      writeSheet(wbk, rowKeys, "All Plates", 0)

    wbk.write()
    wbk.close()
  }

  private def columnLabel(pmd: PlateMetaData) =
    pmd.readoutType.get.getValue +
      (if (pmd.condition.isDefined) { "_" + pmd.condition.get } else "") +
      (if (readoutTypeIsMultiReplicate(pmd.readoutType.get)) { "_" + ('A' + pmd.replicate - 1).toChar } else "") +
      (if (pmd.readout.isDefined) { "_" + pmd.readout.get } else "")

  private def writeHeaders(sheet: jxl.write.WritableSheet) {
    def doWriteHeaders(items: Iterable[String], iFrom: Int) {
      for (e <- items.zipWithIndex) sheet.addCell(new jxl.write.Label(iFrom + e._2, 0, e._1))
    }
    doWriteHeaders(fixedColumnHeaders, 0)
    doWriteHeaders(colKeys map { columnLabel(_) }, fixedColumnHeaders.size)
  }

  private def lookupWellType(well: WellKey) = {
    val wellType = findWellType(well)
    val wellName = new WellName(well.getWellName)
    if (controls.contains(wellName)) {
      if (!AssayWellControlType.isControlAllowed(wellType)) throw new AssayWellControlTypeException(well)
      controls(wellName).getAbbreviation
    } else if (wellType == LibraryWellType.EXPERIMENTAL) "X" else "E"
  }

  // TODO: verify that there is sufficient plate data for the ordering
  private def writeSheets(wbk: WritableWorkbook) {
    val wellKeysByPlate = new TreeMap[Int, SortedSet[WellKey]]() ++ rowKeys.groupBy(_.getPlateNumber)
    for (e <- wellKeysByPlate.keys.zipWithIndex; p = e._1; i = e._2)
      writeSheet(wbk, wellKeysByPlate(p), p.toString, i)
  }

  private def writeSheet(wbk: WritableWorkbook, rowKeys: SortedSet[WellKey], sheetName: String, i: Int) {
    val sheet = wbk.createSheet(sheetName, i)
    writeHeaders(sheet)
    writeRows(sheet, rowKeys)
  }

  private def writeRows(sheet: WritableSheet, rowKeys: SortedSet[WellKey]) {
    for (r <- rowKeys.zipWithIndex; wellKey = r._1; iRow = r._2 + 1) {
      sheet.addCell(new jxl.write.Number(0, iRow, wellKey.getPlateNumber))
      sheet.addCell(new jxl.write.Label(1, iRow, wellKey.getWellName))
      sheet.addCell(new jxl.write.Label(2, iRow, lookupWellType(wellKey).toString))
      for (c <- colKeys.zipWithIndex; col = c._1; iCol = c._2 + fixedColumnHeaders.size) {
        val datum = lookup(wellKey, col)
        if (datum.isDefined) sheet.addCell(new jxl.write.Number(iCol, iRow, datum.get.toDouble))
      }
    }
  }
}
package edu.harvard.med.iccbl.platereader
import java.io.File

import scala.collection.immutable.TreeMap
import scala.collection.immutable.TreeSet
import scala.collection.mutable.LinkedHashSet
import scala.collection.mutable.ListBuffer
import scala.collection.SortedSet

import edu.harvard.med.iccbl.platereader.parser.PlateMetaData
import edu.harvard.med.iccbl.platereader.parser.PlateOrdering
import edu.harvard.med.iccbl.platereader.PlateMatrix
import edu.harvard.med.screensaver.io.libraries.ControlWell
import edu.harvard.med.screensaver.io.libraries.WellMetaData
import edu.harvard.med.screensaver.model.libraries.AssayWellControlTypeException
import edu.harvard.med.screensaver.model.libraries.LibraryWellType
import edu.harvard.med.screensaver.model.libraries.PlateSize
import edu.harvard.med.screensaver.model.libraries.WellKey
import edu.harvard.med.screensaver.model.libraries.WellName
import edu.harvard.med.screensaver.model.screenresults.AssayWellControlType
import edu.harvard.med.screensaver.model.screens.AssayReadoutType
import jxl.write.WritableSheet
import jxl.write.WritableWorkbook
import jxl.Workbook

trait FindWellType extends (WellKey => LibraryWellType)

abstract class ColumnsWriter {
  def writeValues(s: WritableSheet, wk: WellKey, row: Int, fromCol: Int): Int
  val headers: Seq[String]
}

class ScreenResultWriter(data: Seq[PlateMatrix],
  ordering: PlateOrdering,
  controls: Set[WellMetaData],
  findWellType: FindWellType,
  libraryPlateDim: PlateDim,
  quadMapping: QuadrantMapping) {

  private def sansPlate(pmd: PlateMetaData) = PlateMetaData(condition = pmd.condition, readoutType = pmd.readoutType, replicate = pmd.replicate, readout = pmd.readout)

  private val collated = ordering.collate(data)

  val rowKeys = TreeSet.empty[WellKey] ++
    (for {
      pmd <- collated.keys;
      w <- new PlateMatrix(libraryPlateDim).wellNames
    } yield new WellKey(pmd.plate.get, w))
  /* HACK: we are using PlateMetaData objects to represent our column descriptions, since PlateMetaData contains all of the pertinent information; 
   * however, we ignore its plateNumber property */
  // note: use of LinkedHashSet maintains expected column ordering
  val colKeys = LinkedHashSet() ++ (ordering.iterator map sansPlate)

  def readoutTypeIsMultiReplicate(readoutType: AssayReadoutType) = colKeys exists ((x) => { x.readoutType.get == readoutType && x.replicate > 1 })

  def lookup(lwk: WellKey, col: PlateMetaData) = {
    val plateKey = col.copy(plate = Some(lwk.getPlateNumber), quadrant = quadMapping.quadrant(lwk))
    val assayWellName = quadMapping.wellName(lwk)
    collated.get(plateKey) map { _.well(assayWellName.getRowIndex, assayWellName.getColumnIndex) }
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
    (if (pmd.readout.isDefined) { pmd.readout.get + "_" } else "") +
      pmd.readoutType.get.getValue +
      (if (pmd.condition.isDefined) { "_" + pmd.condition.get } else "") +
      (if (readoutTypeIsMultiReplicate(pmd.readoutType.get)) { "_" + ('A' + pmd.replicate - 1).toChar } else "")

  private def writeHeaders(sheet: jxl.write.WritableSheet): Int = {
    // TODO: can we eliminate this state variable with a colWriters.foldLeft?
    var col = 0
    for (cw <- colWriters) {
      for (e <- cw.headers.zipWithIndex) sheet.addCell(new jxl.write.Label(col + e._2, 0, e._1))
      col += cw.headers.size
    }
    col
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
      writeRow(sheet, wellKey, iRow);
    }
  }

  val writeFixedColumns = new ColumnsWriter() {
    val headers = List("Plate", "Well", "Type", "Exclude")

    private def lookupWellType(well: WellKey): String = {
      val wellType = findWellType(well)
      val control = controls.find(_.wellName == new WellName(well.getWellName))
      if (control.isDefined && control.get.isInstanceOf[ControlWell]) {
        if (!AssayWellControlType.isControlAllowed(wellType)) throw new AssayWellControlTypeException(well)
        control.get.asInstanceOf[ControlWell].controlType.getAbbreviation
      } else
        wellType match {
          // TODO: this abbreviations should be maintained by AssayWellControlType
          case LibraryWellType.EXPERIMENTAL => "X"
          case LibraryWellType.DMSO => "D"
          case LibraryWellType.RNAI_BUFFER => "B"
          case LibraryWellType.EMPTY => "E"
          case LibraryWellType.LIBRARY_CONTROL => "C"
          case _ => "U"
        }
    }

    def writeValues(sheet: WritableSheet, wellKey: WellKey, iRow: Int, fromCol: Int) = {
      sheet.addCell(new jxl.write.Number(fromCol + 0, iRow, wellKey.getPlateNumber))
      sheet.addCell(new jxl.write.Label(fromCol + 1, iRow, wellKey.getWellName))
      sheet.addCell(new jxl.write.Label(fromCol + 2, iRow, lookupWellType(wellKey).toString))
      headers.size;
    }
  }

  val writePlateData = new ColumnsWriter() {
    val headers = (colKeys map { columnLabel(_) }) toSeq
    def writeValues(sheet: WritableSheet, wellKey: WellKey, iRow: Int, fromCol: Int): Int = {
      for (c <- colKeys.zipWithIndex; col = c._1; iCol = c._2 + fromCol) {
        val datum = lookup(wellKey, col)
        if (datum.isDefined) sheet.addCell(new jxl.write.Number(iCol, iRow, datum.get.toDouble))
      }
      colKeys.size
    }
  }

  val colWriters = new ListBuffer[ColumnsWriter]()
  colWriters ++= List[ColumnsWriter](writeFixedColumns, writePlateData)

  protected def writeRow(sheet: WritableSheet, wellKey: WellKey, iRow: Int): Int = {
    // TODO: can we eliminate this state variable with a colWriters.foldLeft?
    var col = 0
    for (cw <- colWriters) {
      col += cw.writeValues(sheet, wellKey, iRow, col)
    }
    col
  }
}
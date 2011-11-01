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

trait FindWellType extends (WellKey => LibraryWellType)

class ScreenResultWriter(data: Seq[PlateMatrix], ordering: PlateOrdering, controls: Map[WellName, AssayWellControlType], findWellType: FindWellType) {
  private class ColumnOrdering extends Ordering[PlateMetaData] {
    def compare(pmd1: PlateMetaData, pmd2: PlateMetaData) =
      if (pmd1.readoutType.get.ordinal < pmd2.readoutType.get.ordinal) -1
      else if (pmd1.readoutType.get.ordinal > pmd2.readoutType.get.ordinal) 1
      else if (pmd1.condition.getOrElse("") < pmd2.condition.getOrElse("")) -1
      else if (pmd1.condition.getOrElse("") > pmd2.condition.getOrElse("")) 1
      else if (pmd1.replicate < pmd2.replicate) -1
      else if (pmd1.replicate > pmd2.replicate) 1
      else 0
  }
  private def sansPlate(pmd: PlateMetaData) = PlateMetaData(condition = pmd.condition, readoutType = pmd.readoutType, replicate = pmd.replicate)

  private val collated = ordering.collate(data)

  val rowKeys = TreeSet.empty[WellKey] ++ (for { e <- collated; w <- e._2.wellNames } yield new WellKey(e._1.plate.get, w))
  val colKeys = TreeSet.empty(new ColumnOrdering) ++ (collated.keys map sansPlate)
  val replicates = colKeys.maxBy(_.replicate).replicate
  val fixedColumnHeaders = List("Plate", "Well", "Type", "Exclude")

  def lookup(wk: WellKey, col: PlateMetaData) = collated(col.copy(plate = Some(wk.getPlateNumber))).well(wk.getRow, wk.getColumn)

  def write(file: File) {
    val wbk = Workbook.createWorkbook(file);
    writeSheets(wbk)
    wbk.write()
    wbk.close()
  }

  private def columnLabel(pmd: PlateMetaData) =
    pmd.readoutType.get.getValue +
      (if (pmd.condition.isDefined) { "_" + pmd.condition.get } else "") +
      (if (replicates > 1) { ("_" + ('A' + pmd.replicate - 1).toChar) } else "")

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
      assert(wellType == LibraryWellType.EMPTY)
      controls(wellName).getAbbreviation
    } else if (wellType == LibraryWellType.EXPERIMENTAL) "X" else "E"
  }

  // TODO: verify that there is sufficient plate data for the ordering
  private def writeSheets(wbk: WritableWorkbook) {
    val wellKeysByPlate = TreeMap.empty[Int,SortedSet[WellKey]] ++ rowKeys.groupBy(_.getPlateNumber)
    for (e <- wellKeysByPlate.keys.zipWithIndex; p = e._1; i = e._2) {
      val sheet = wbk.createSheet(p.toString, i)
      writeHeaders(sheet)
      writeRows(sheet, wellKeysByPlate(p))
    }
  }

  private def writeRows(sheet: WritableSheet, rowKeys: SortedSet[WellKey]) {
    for (r <- rowKeys.zipWithIndex; wellKey = r._1; iRow = r._2 + 1) {
      sheet.addCell(new jxl.write.Number(0, iRow, wellKey.getPlateNumber))
      sheet.addCell(new jxl.write.Label(1, iRow, wellKey.getWellName))
      sheet.addCell(new jxl.write.Label(2, iRow, lookupWellType(wellKey).toString))
      for (c <- colKeys.zipWithIndex; col = c._1; iCol = c._2 + fixedColumnHeaders.size) {
        sheet.addCell(new jxl.write.Number(iCol, iRow, lookup(wellKey, col).toDouble))
      }
    }
  }
}
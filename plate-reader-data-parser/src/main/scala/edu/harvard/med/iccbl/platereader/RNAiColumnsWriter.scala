package edu.harvard.med.iccbl.platereader

import scala.collection.JavaConversions.asScalaSet

import edu.harvard.med.screensaver.io.libraries.WellMetaData
import edu.harvard.med.screensaver.model.libraries.SilencingReagent
import edu.harvard.med.screensaver.model.libraries.WellKey
import edu.harvard.med.screensaver.model.libraries.WellName
import jxl.write.WritableSheet

trait FindSilencingReagent extends (WellKey => /*Option[*/ SilencingReagent /*]*/ )

class RNAiColumnsWriter(findSilencingReagent: FindSilencingReagent) extends ColumnsWriter {
  val headers = Seq("Entrezgene Symbol", "Entrezgene ID", "Genbank Accession No.", "Catalog No.", "Gene Name", "Deprecated Pool")
  def writeValues(sheet: WritableSheet, wellKey: WellKey, iRow: Int, fromCol: Int) = {
    val reagent = Option(findSilencingReagent(wellKey))
    val gene = reagent map { _.getFacilityGene }
    val geneSymbol = gene map { _.getEntrezgeneSymbols } map { _.headOption } filter { _.isDefined } map { _.get }
    if (geneSymbol.isDefined) sheet.addCell(new jxl.write.Label(fromCol, iRow, geneSymbol.get));
    val entrezGeneId = gene map { _.getEntrezgeneId } filter { _ != null } map { _.toDouble }
    if (entrezGeneId.isDefined) sheet.addCell(new jxl.write.Number(fromCol + 1, iRow, entrezGeneId.get))
    val genbankAccNo = gene map { _.getGenbankAccessionNumbers } map { _.headOption } filter { _.isDefined } map { _.get }
    if (genbankAccNo.isDefined) sheet.addCell(new jxl.write.Label(fromCol + 2, iRow, genbankAccNo.get))
    val catalogNo = reagent map { _.getVendorId.getVendorIdentifier }
    if (catalogNo.isDefined) sheet.addCell(new jxl.write.Label(fromCol + 3, iRow, catalogNo.get))
    val geneName = gene map { _.getGeneName } filter { _ != null }
    if (geneName.isDefined) sheet.addCell(new jxl.write.Label(fromCol + 4, iRow, geneName.get))
    val deprecatedPool = reagent map { r => r.getWell.isDeprecated && r.getWell.getLibrary.isPool } getOrElse (false)
    if (deprecatedPool) sheet.addCell(new jxl.write.Boolean(fromCol + 5, iRow, deprecatedPool))
    headers.size
  }
}

// TODO: needs a unit test
class PreLoadedControlsWriter(controls: Set[WellMetaData]) extends ColumnsWriter {
  val headers = Seq("Pre-loaded Controls")
  val controlsMap = controls map { e => (e.wellName, e) } toMap
  def writeValues(sheet: WritableSheet, wellKey: WellKey, iRow: Int, fromCol: Int): Int = {
    val v = controlsMap.get(new WellName(wellKey.getWellName)).map(_.label.orNull).orNull
    sheet.addCell(new jxl.write.Label(fromCol, iRow, v))
    headers.size
  }
}

// TODO: needs a unit test
class AssayPlateWellAndQuadrantWriter(mapping: QuadrantMapping) extends ColumnsWriter {
  val headers = Seq("Quadrant", "Assay Well")
  def writeValues(sheet: WritableSheet, wellKey: WellKey, iRow: Int, fromCol: Int): Int = {
    sheet.addCell(new jxl.write.Number(fromCol, iRow, mapping.quadrant(wellKey) + 1))
    sheet.addCell(new jxl.write.Label(fromCol + 1, iRow, mapping.wellName(wellKey).toString))
    headers.size
  }
}

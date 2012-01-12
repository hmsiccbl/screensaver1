package edu.harvard.med.iccbl.platereader
import edu.harvard.med.screensaver.model.libraries.WellKey
import edu.harvard.med.screensaver.model.libraries.WellName

/** Encapsulates the strategy of how to map wells from larger library plates  to smaller assay plates (e.g. 384-well library plates mapping to 96-well assay plates)*/
trait QuadrantMapping {
  def quadrant(wk: WellKey): Int
  def wellName(wk: WellKey): WellName
}

class IccblQuadrantMapping(from: PlateDim, to: PlateDim) extends QuadrantMapping {

  if ((to.size % from.size) % 4 != 0)
    throw new IllegalArgumentException("library plate size must be a 4x multiple of assay plate size")

  val plateDimFactor = Math.sqrt(to.size / from.size).toInt

  /** calculate quadrant of assay plate for the library well key */
  def quadrant(lwk: WellKey) = (lwk.getRow % plateDimFactor) * plateDimFactor + lwk.getColumn % plateDimFactor

  /** calculate assay well coord from the specific library wellKey */
  def wellName(wk: WellKey) = {
    val ar = wk.getRow / plateDimFactor
    val ac = wk.getColumn / plateDimFactor
    new WellName(ar, ac)
  }
}
package edu.harvard.med.iccbl.platereader.parser

import scala.collection.mutable.ArrayBuffer
import scala.collection.mutable.ArraySeq
import edu.harvard.med.screensaver.model.screens.AssayReadoutType
import scala.annotation.tailrec
import scala.collection.IterableLike

case class PlateMetaData(plate: Option[Int] = None,
                         readoutType: Option[AssayReadoutType] = None,
                         replicate: Int = 1,
                         condition: Option[String] = None,
                         readout: Option[String] = None) {
  if (replicate < 1) throw new IllegalArgumentException("replicate must be >= 1")
}

object PlateMetaDataBuilder {
  def forPlate(pmd: PlateMetaData, p: Int) = { pmd.copy(plate = Some(p)) }
  def forReadoutType(pmd: PlateMetaData, r: AssayReadoutType) = { pmd.copy(readoutType = Some(r)) }
  def forReplicate(pmd: PlateMetaData, r: Int) = { pmd.copy(replicate = r) }
  def forCondition(pmd: PlateMetaData, c: String) = { pmd.copy(condition = Some(c)) }
  def forReadout(pmd: PlateMetaData, ro: String) = { pmd.copy(readout = Some(ro)) }
}

/**
 * Defines an ordering of plates by PlateMetaData descriptions. The ordering can then be used to collate input data, associating each datum with a PlateMetaData description.
 */
trait PlateOrdering extends Iterable[PlateMetaData] {
  /** Creates a map that associates a PlateMetaData with each element of the input data sequence, using the ordering of PlateMetaData that is defined in this PlateOrdering */
  def collate[T](data: Seq[T]): Map[PlateMetaData, T]
}

/**
 * An ordering of plates that is composed of multiple PlateOrderings, allowing heterogeneous orderings to be concatenated together.
 */
class CompositePlateOrdering extends PlateOrdering {
  val orderings = scala.collection.mutable.ListBuffer[PlateOrdering]()
  def add(ordering: PlateOrdering) = { orderings += ordering; this }
  def iterator = orderings.foldLeft(Iterator[PlateMetaData]()) { _ ++ _.iterator }
  def collate[T](data: Seq[T]) = doCollate(orderings, data)
  private def doCollate[T](orderings: Seq[PlateOrdering], data: Seq[T]): Map[PlateMetaData, T] = {
    if (orderings.isEmpty) {
      Map[PlateMetaData, T]()
    } else {
      val collated = orderings.head.collate(data)
      collated ++ doCollate(orderings.tail, data.drop(collated.size))
    }
  }
}

/**
 * An ordering of plates where each PlateMetaData property is repeated a fixed number of times, relative to its parent property.
 */
class SimplePlateOrdering extends PlateOrdering {
  /** Generator is a function that expands a single PlateMetaData into a sequence of PlateMetaData, setting a given property of each new PlateMetaData to one of the specified values */
  class Generator[+T](values: Seq[T], setter: ((PlateMetaData, T) => PlateMetaData)) extends (PlateMetaData => Seq[PlateMetaData]) {
    def apply(pmd: PlateMetaData) = if (values.isEmpty) { Seq(pmd) } else { for (v <- values) yield setter(pmd, v) }
  }

  private val generators = new ArrayBuffer[Generator[Any]]
  private var _plates: Seq[Int] = Seq()
  def plates = _plates

  import PlateMetaDataBuilder._

  def addPlates(plates: Seq[Int]) = { generators += new Generator[Int](plates, forPlate); _plates = plates; this }
  def addReplicates(replicates: Int) = { generators += new Generator[Int](1 to replicates, forReplicate); this }
  def addConditions(conditions: Seq[String]) = { generators += new Generator[String](conditions, forCondition); this }
  def addReadoutTypes(readoutTypes: Seq[AssayReadoutType]) = { generators += new Generator[AssayReadoutType](readoutTypes, forReadoutType); this }
  def addReadouts(readouts: Seq[String]) = { generators += new Generator[String](readouts, forReadout); this }
  def iterator = build(List(new PlateMetaData()), generators).iterator
  /** Creates a map that associates a PlateMetaData with each element of the input data sequence, using the ordering of PlateMetaData that is defined in this PlateOrdering */
  override def collate[T](data: Seq[T]) = iterator.zip(data.iterator).toMap

  import scala.collection.JavaConversions._

  def addPlates(plates: java.util.List[Int]): PlateOrdering = addPlates(plates.toSeq)
  def addConditions(conditions: java.util.List[String]): PlateOrdering = { generators += new Generator[String](conditions, forCondition); this }
  def addReadoutTypes(readoutTypes: java.util.List[AssayReadoutType]): PlateOrdering = { generators += new Generator[AssayReadoutType](readoutTypes, forReadoutType); this }
  def addReadouts(readouts: java.util.List[String]): PlateOrdering = { generators += new Generator[String](readouts, forReadout); this }
  def collate[T](data: java.util.List[T]): java.util.Map[PlateMetaData, T] = collate(data.toSeq)

  @tailrec
  private def build(pmds: Seq[PlateMetaData], generators: Seq[Generator[Any]]): Seq[PlateMetaData] =
    if (generators.isEmpty)
      pmds
    else
      build((for (pmd <- pmds) yield generators.head(pmd)) flatten, generators.tail)
}

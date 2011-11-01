package edu.harvard.med.iccbl.platereader.parser

import scala.collection.mutable.ArrayBuffer
import scala.collection.mutable.ArraySeq
import edu.harvard.med.screensaver.model.screens.AssayReadoutType
import scala.annotation.tailrec

case class PlateMetaData(plate: Option[Int] = None,
  readoutType: Option[AssayReadoutType] = None,
  replicate: Int = 1,
  condition: Option[String] = None) {
  if (replicate < 1) throw new IllegalArgumentException("replicate must be >= 1")
}

object PlateMetaDataBuilder {
  def forPlate(pmd: PlateMetaData, p: Int) = { pmd.copy(plate = Some(p)) }
  def forReadoutType(pmd: PlateMetaData, r: AssayReadoutType) = { pmd.copy(readoutType = Some(r)) }
  def forReplicate(pmd: PlateMetaData, r: Int) = { pmd.copy(replicate = r) }
  def forCondition(pmd: PlateMetaData, c: String) = { pmd.copy(condition = Some(c)) }
}

class PlateOrdering {
  /** Generator is a function that expands a single PlateMetaData into a sequence of PlateMetaData, setting a given property of each new PlateMetaData to one of the specified values */
  class Generator[+T](values: Seq[T], setter: ((PlateMetaData, T) => PlateMetaData)) extends (PlateMetaData => Seq[PlateMetaData]) {
    def apply(pmd: PlateMetaData) = for (v <- values) yield setter(pmd, v)
  }

  private val generators = new ArrayBuffer[Generator[Any]]
  private var _plates: Seq[Int] = Seq()
  def plates = _plates

  import PlateMetaDataBuilder._

  def addPlates(plates: Seq[Int]) = { generators += new Generator[Int](plates, forPlate); _plates = plates; this }
  def addReplicates(replicates: Int) = { generators += new Generator[Int](1 to replicates, forReplicate); this }
  def addConditions(conditions: Seq[String]) = { generators += new Generator[String](conditions, forCondition); this }
  def addReadoutTypes(readoutTypes: Seq[AssayReadoutType]) = { generators += new Generator[AssayReadoutType](readoutTypes, forReadoutType); this }
  def iterator = build(List(new PlateMetaData()), generators).iterator
  def collate[T](data: Seq[T]) = iterator.zip(data.iterator).toMap

  import scala.collection.JavaConversions._
  
  def addPlates(plates: java.util.List[Int]): PlateOrdering = addPlates(plates.toSeq)
  def addConditions(conditions: java.util.List[String]): PlateOrdering = { generators += new Generator[String](conditions, forCondition); this }
  def addReadoutTypes(readoutTypes: java.util.List[AssayReadoutType]): PlateOrdering = { generators += new Generator[AssayReadoutType](readoutTypes, forReadoutType); this }
  def collate[T](data: java.util.List[T]): java.util.Map[PlateMetaData,T] = collate(data.toSeq)
  
  @tailrec
  private def build(pmds: Seq[PlateMetaData], generators: Seq[Generator[Any]]): Seq[PlateMetaData] =
    if (generators.isEmpty)
      pmds
    else
      build((for (pmd <- pmds) yield generators.head(pmd)) flatten, generators.tail)
}

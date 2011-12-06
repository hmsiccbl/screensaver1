package edu.harvard.med.iccbl.platereader.parser

import edu.harvard.med.iccbl.platereader.PlateMatrix
import scala.util.parsing.combinator.Parsers
import scala.util.parsing.input.Reader

trait PlateReaderRawDataParser {
  def parse(in: java.io.Reader): java.util.List[PlateMatrix]
}

trait RawDataCleaner {
  def clean(in: Reader[Char]): Reader[Char]
}

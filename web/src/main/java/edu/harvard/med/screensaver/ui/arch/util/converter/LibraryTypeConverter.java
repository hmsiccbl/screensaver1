package edu.harvard.med.screensaver.ui.arch.util.converter;

import edu.harvard.med.screensaver.model.libraries.LibraryType;


public class LibraryTypeConverter extends VocabularyConverter<LibraryType>
{
  public LibraryTypeConverter()
  {
   super(LibraryType.values());
  }

}


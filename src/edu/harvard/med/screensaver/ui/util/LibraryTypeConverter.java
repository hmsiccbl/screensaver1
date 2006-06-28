// $HeadURL$
// $Id$
//
// Copyright 2006 by the President and Fellows of Harvard College.
// 
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.ui.util;

import java.util.EnumSet;

import javax.faces.convert.Converter;

import edu.harvard.med.screensaver.model.libraries.LibraryType;

public class LibraryTypeConverter extends EnumTypeConverter<LibraryType> implements Converter
{
  public LibraryTypeConverter()
  {
    super(EnumSet.allOf(LibraryType.class));
  }
}

// $HeadURL$
// $Id$
//
// Copyright 2006 by the President and Fellows of Harvard College.
// 
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.model;

public interface QuantityUnit<E extends Enum<E> & QuantityUnit<E>> 
  extends VocabularyTerm
{
  public String getSymbol();
  public int getScale();
  public E[] getValues(); // pass through to underlying enum
  public E getDefault();
}

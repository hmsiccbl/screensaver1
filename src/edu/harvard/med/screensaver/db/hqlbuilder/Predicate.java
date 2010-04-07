// $HeadURL: http://forge.abcd.harvard.edu/svn/screensaver/trunk/src/edu/harvard/med/screensaver/db/hibernate/Predicate.java $
// $Id: Predicate.java 3831 2010-02-25 16:07:09Z atolopko $
//
// Copyright 2006 by the President and Fellows of Harvard College.
//
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.db.hqlbuilder;

public abstract class Predicate
{
  public abstract String toHql();

  final public String toString()
  {
    return toHql();
  }

  @Override
  public boolean equals(Object obj)
  {
    return obj == this || (obj instanceof Predicate && hashCode() == obj.hashCode());
  }

  @Override
  public int hashCode()
  {
    return toHql().hashCode();
  }
}
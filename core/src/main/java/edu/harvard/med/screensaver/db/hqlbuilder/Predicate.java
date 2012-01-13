// $HeadURL$
// $Id$
//
// Copyright © 2006, 2010, 2011, 2012 by the President and Fellows of Harvard College.
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
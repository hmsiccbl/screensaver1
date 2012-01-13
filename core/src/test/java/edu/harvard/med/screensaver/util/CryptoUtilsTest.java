// $HeadURL$
// $Id$
//
// Copyright © 2006, 2010, 2011, 2012 by the President and Fellows of Harvard College.
// 
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.util;

import junit.framework.TestCase;

public class CryptoUtilsTest extends TestCase
{
  private static String INPUT = "Security is of the utmost importance!";
  /**
   * Expected SHA1 hash value, as calculated by the trusty Digest::SHA1 perl package with the following code:
   * <code>perl -e 'use Digest::SHA1; my $sha1 = Digest::SHA1->new; $sha1->add("Security is of the utmost importance!"); print $sha1->hexdigest(), "\n";'</code>
   */
  private static String EXPECTED_HASH = "914d3f312502446475dbe6e34067a2c0e0e1a784";
  
  public void testDigestString()
  {
    assertEquals(EXPECTED_HASH,
                 CryptoUtils.digest(INPUT));
  }

  public void testDigestCharArray()
  {
    char[] a = new char[INPUT.length()];
    INPUT.getChars(0, a.length, a, 0);
    assertEquals(EXPECTED_HASH,
                 CryptoUtils.digest(a));
  }

  public void testDigestByteArray()
  {
    assertEquals(EXPECTED_HASH,
                 CryptoUtils.digest(INPUT.getBytes()));
  }
}

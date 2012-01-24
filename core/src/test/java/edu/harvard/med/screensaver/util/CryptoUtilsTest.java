// $HeadURL$
// $Id$
//
// Copyright © 2006, 2010, 2011, 2012 by the President and Fellows of Harvard College.
// 
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.util;

import javax.crypto.KeyGenerator;

import org.apache.log4j.Logger;

import junit.framework.TestCase;

public class CryptoUtilsTest extends TestCase
{
  private static final Logger log = Logger.getLogger(CryptoUtilsTest.class);

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
  
  public void testDesEncryptDecrypt()
  {
  	String secretKey = "tellNo1";
  	String testPhrase = "HMSL10001";
  	CryptoUtils.DesEncrypter encrypter = new CryptoUtils.DesEncrypter(secretKey);
  	
  	String encryptedPhrase = encrypter.encrypt(testPhrase);
  	log.info("original: " + testPhrase + ", encrypted: " + encryptedPhrase);
  	
  	assertFalse("encrypted phrase equals original", testPhrase.equals(encryptedPhrase));
  	
  	String decryptedPhrase = encrypter.decrypt(encryptedPhrase);
  	
  	assertTrue("decrypted phrase not equal to the original: " + decryptedPhrase, testPhrase.equals(decryptedPhrase));
  	
  }
  
  public void testURLEncryptDecrypt()
  {
  	String secretKey = "tellNo1";
  	String testPhrase = "HMSL10001";
  	CryptoUtils.DesEncrypter encrypter = new CryptoUtils.DesEncrypter(secretKey);

  	String urlEncryptedPhrase = encrypter.urlEncrypt(testPhrase);
  	
  	log.info("original: " + testPhrase + ", encrypted: " + urlEncryptedPhrase);
  	assertFalse("url encrypted phrase equals original", testPhrase.equals(urlEncryptedPhrase));
  	String urlDecryptedPhrase = encrypter.urlDecrypt(urlEncryptedPhrase);
  	assertTrue("url decrypted phrase not equal to the original: " + urlDecryptedPhrase, testPhrase.equals(urlDecryptedPhrase));
  }
  
}

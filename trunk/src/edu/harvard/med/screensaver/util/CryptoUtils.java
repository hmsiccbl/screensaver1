// $HeadURL$
// $Id$
//
// Copyright 2006 by the President and Fellows of Harvard College.
// 
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.util;

import java.security.MessageDigest;

import org.apache.commons.codec.binary.Hex;
import org.apache.log4j.Logger;

public class CryptoUtils
{
  private static final Logger log = Logger.getLogger(CryptoUtils.class);

  /**
   * Utility method for generating a SHA-digested (hashed) version of a String.
   * @param s string to be hashed
   * @return the digested (hashed) version of the String, as a hex String.
   */
  public static String digest(String s)
  {
    return digest(s.getBytes());
  }

  /**
   * Utility method for generating a SHA-digested (hashed) version of a char[].
   * @param a array of characters to be hashed
   * @return the digested (hashed) version of the char[], as a hex String.
   */
  public static String digest(char[] a)
  {
    return digest(new String(a).getBytes());
  }

  /**
   * Utility method for generating a SHA-digested (hashed) version of a byte[].
   * @param bytes array of bytes to be hashed
   * @return the digested (hashed) version of the byte[], as a hex String.
   */
  public static String digest(byte[] bytes)
  {
    try {
      byte[] resultBytes = MessageDigest.getInstance("SHA").digest(bytes);
      char[] resultHexChars = Hex.encodeHex(resultBytes);
      return new String(resultHexChars);
    }
    catch (Exception e) {
      e.printStackTrace();
      log.error("error trying to digest bytes: " + e.getMessage());
      return null;
    }
  }
  
}

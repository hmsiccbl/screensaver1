// $HeadURL$
// $Id$
//
// Copyright © 2006, 2010, 2011, 2012 by the President and Fellows of Harvard College.
// 
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.util;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.security.spec.AlgorithmParameterSpec;
import java.security.spec.KeySpec;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.PBEParameterSpec;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.codec.binary.Hex;
import org.apache.log4j.Logger;

public class CryptoUtils {
	private static final Logger log = Logger.getLogger(CryptoUtils.class);

	/**
	 * Utility method for generating a SHA-digested (hashed) version of a String.
	 * 
	 * @param s
	 *          string to be hashed
	 * @return the digested (hashed) version of the String, as a hex String.
	 */
	public static String digest(String s) {
		return digest(s.getBytes());
	}

	/**
	 * Utility method for generating a SHA-digested (hashed) version of a char[].
	 * 
	 * @param a
	 *          array of characters to be hashed
	 * @return the digested (hashed) version of the char[], as a hex String.
	 */
	public static String digest(char[] a) {
		return digest(new String(a).getBytes());
	}

	/**
	 * Utility method for generating a SHA-digested (hashed) version of a byte[].
	 * 
	 * @param bytes
	 *          array of bytes to be hashed
	 * @return the digested (hashed) version of the byte[], as a hex String.
	 */
	public static String digest(byte[] bytes) {
		try {
			byte[] resultBytes = MessageDigest.getInstance("SHA").digest(bytes);
			char[] resultHexChars = Hex.encodeHex(resultBytes);
			return new String(resultHexChars);
		} catch (Exception e) {
			e.printStackTrace();
			log.error("error trying to digest bytes: " + e.getMessage());
			return null;
		}
	}

	/**
	 * Encrypt/Decrypt using the Java {@link Cipher } class.
	 * 
	 * Example modified from:
	 * http://www.exampledepot.com/egs/javax.crypto/PassKey.html
	 */
	public static class DesEncrypter {
		Cipher ecipher;
		Cipher dcipher;

		// TODO: 8-byte Salt - input this from the administrator for greater protection
		byte[] salt = { (byte) 0xB9, (byte) 0x9B, (byte) 0xC8, (byte) 0x32,
				(byte) 0x56, (byte) 0xA5, (byte) 0x13, (byte) 0x0F };

		// Iteration count
		int iterationCount = 19;

		public DesEncrypter(String passPhrase) {
			try {
				// Create the key
				KeySpec keySpec = new PBEKeySpec(passPhrase.toCharArray(), 
						salt,
						iterationCount);
				SecretKey key = SecretKeyFactory.getInstance("PBEWithMD5AndDES")
						.generateSecret(keySpec);
				ecipher = Cipher.getInstance(key.getAlgorithm());
				dcipher = Cipher.getInstance(key.getAlgorithm());

				// Prepare the parameter to the ciphers
				AlgorithmParameterSpec paramSpec = new PBEParameterSpec(salt,
						iterationCount);

				// Create the ciphers
				ecipher.init(Cipher.ENCRYPT_MODE, key, paramSpec);
				dcipher.init(Cipher.DECRYPT_MODE, key, paramSpec);
			} catch (GeneralSecurityException e) {
				throw new RuntimeException(e);
			}
		}

		@SuppressWarnings("restriction")
		public String encrypt(String str) {
			try {
				// Encode the string into bytes using utf-8
				byte[] utf8 = str.getBytes("UTF8");

				// Encrypt
				byte[] enc = ecipher.doFinal(utf8);

				// Encode bytes to base64 to get a string
				return new String(Base64.encodeBase64(enc));

			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		}

		public String urlEncrypt(String str) {
			try {
				return URLEncoder.encode(encrypt(str), "UTF-8");
			} catch (UnsupportedEncodingException e) {
				throw new RuntimeException(e);
			}
		}

		public String decrypt(String str) {
			try {
				// Decode base64 to get bytes
				byte[] dec = Base64.decodeBase64(str.getBytes());
				//byte[] dec = new sun.misc.BASE64Decoder().decodeBuffer(str);

				// Decrypt
				byte[] utf8 = dcipher.doFinal(dec);

				// Decode using utf-8
				return new String(utf8, "UTF8");
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		}

		public String urlDecrypt(String str) {
			try {
				return decrypt(URLDecoder.decode(str, "UTF-8"));
			} catch (UnsupportedEncodingException e) {
				throw new RuntimeException(e);
			}
		}
	}

}

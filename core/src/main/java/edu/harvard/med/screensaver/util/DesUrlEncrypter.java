package edu.harvard.med.screensaver.util;

import org.apache.log4j.Logger;

/**
 * Bean that uses DES encryption and Base64 encoding to encrypt plain text URL paths.
 * Internally uses the {@link CryptoUtils } class to encrypt and decrypt URL's 
 *
 */
public class DesUrlEncrypter implements UrlEncrypter
{
  private static Logger log = Logger.getLogger(DesUrlEncrypter.class);
  public String delimiter = "xxxx"; // necessary because the encryption can insert path '/' chars into the url, which we don't know how to detect from real paths -sde4
  
	CryptoUtils.DesEncrypter encrypter;
	
	public DesUrlEncrypter(String passphrase, String delimiter)
	{
		if(StringUtils.isEmpty(passphrase)) {
			log.warn("passphrase is null, disabling encryption");
			return;
		}
		encrypter = new CryptoUtils.DesEncrypter(passphrase);
		this.delimiter = delimiter;
	}

	@Override
	public String encryptUrl(String urlString) {
		return encrypter==null ? urlString :  delimiter + encrypter.encrypt(urlString) + delimiter;
	}

	@Override
	public String decryptUrl(String encryptedUrlString) {
		return encrypter == null ? encryptedUrlString : encrypter.decrypt(encryptedUrlString.replaceAll(delimiter, ""));
	}
	
	@Override
	public String getDelimiter() {
		return delimiter;
	}

}

package edu.harvard.med.screensaver.util;

public interface UrlEncrypter 
{
	public String encryptUrl(String urlString);
	public String decryptUrl(String encryptedUrlString);
	public String getDelimiter();
}

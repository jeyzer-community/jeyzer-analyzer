package org.jeyzer.analyzer.util;

/*-
 * ---------------------------LICENSE_START---------------------------
 * Jeyzer Analyzer
 * --
 * Copyright (C) 2020 Jeyzer
 * --
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 * ----------------------------LICENSE_END----------------------------
 */


import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Date;

import org.jeyzer.analyzer.error.JzrException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SystemHelper {
	
	private static final Logger logger = LoggerFactory.getLogger(SystemHelper.class);

	public static final String SYSTEM_PROPERTY_OS_NAME = "os.name";
	public static final String SYSTEM_PROPERTY_JAVA_RUNTIME_VERSION = "java.runtime.version";
	
	public static final boolean WINDOWS_OS = System.getProperty(SYSTEM_PROPERTY_OS_NAME).startsWith("Win");
	public static final String CR = System.getProperty("line.separator");
	public static final String JPG_EXTENSION = ".jpg";
	public static final String JSON_EXTENSION = ".json";
	private static final Date JAVA_START_DATE = new Date(0); // 1970
	
	public static final String HTTP_PREFIX = "http://";
	public static final String HTTPS_PREFIX = "https://";
	public static final String FTP_PREFIX = "ftp://";
	public static final String WIN_FILE_PREFIX = "file:///";
	public static final String UNIX_FILE_PREFIX = "file://";
	
	public static final String JAVA_VERSION = System.getProperty(SYSTEM_PROPERTY_JAVA_RUNTIME_VERSION).toLowerCase();

	private SystemHelper() {
	}

	public static boolean isWindows(){
		return WINDOWS_OS;
	}
	
	public static boolean isAtLeastJdK9() {
		return !JAVA_VERSION.startsWith("1."); // Stands for 1.8, 1.7. From Java 9, "1." is not used anymore.
	}
	
	public static Date getUnixEpochDate() {
		return new Date(JAVA_START_DATE.getTime());
	}

	public static String sanitizePathSeparators(String path){
		if (isWindows()){
			path = path.replace('/', '\\');     //convert to Windows
			return path.replace("\\\\", "\\");  //remove duplicate slashes
		}
		else{
			path = path.replace('\\', '/');     //convert to Unix
			return path.replace("//", "/");     //remove duplicate slashes
		}
	}
	
	public static String sanitizePathElement(String pathElement){
		if (isWindows()){
			return pathElement.replaceAll("[\\\\/:*?\"<>|]", "-");
		}
		else{
			return pathElement.replace('/', '-');
		}
	}
	
	public static void createDirectory(String path) throws JzrException{
		if (path == null)
			throw new JzrException("Directory to create is not set.");
		
		 File dir = new File(path);
		 if (!dir.exists() || (dir.exists() && !dir.isDirectory())){
			 if (!dir.mkdirs())
				 throw new JzrException("Failed to create directory : " + sanitizePathSeparators(path));
		 }
	}

	public static boolean deleteFile(String path) {
		File targetFile = new File(path);
		// remove if already exists
		if (targetFile.exists())
			return targetFile.delete();
		else
			return false;
	}
	
	public static void downloadFile(String urlStr, String file) throws IOException{   	
        URL url = new URL(urlStr);
        
        try (
            	BufferedInputStream bis = new BufferedInputStream(url.openStream());
            	FileOutputStream fis = new FileOutputStream(file);
        	)
        {
            byte[] buffer = new byte[1024];
            int count=0;
            while((count = bis.read(buffer,0,1024)) != -1)
            {
                fis.write(buffer, 0, count);
            }
        }
    }
	
	public static void copyFile(File source, File dest) throws IOException {
	    try (
	    	    InputStream is = new FileInputStream(source);
	    	    OutputStream os = new FileOutputStream(dest);
	    	)
	    {
	        byte[] buffer = new byte[1024];
	        int length;
	        while ((length = is.read(buffer)) > 0) {
	            os.write(buffer, 0, length);
	        }
	    }
	}
	
	public static boolean isRemoteProtocol(String path){
		return path != null ? path.startsWith(HTTP_PREFIX) 
							|| path.startsWith(HTTPS_PREFIX) 
							|| path.startsWith(FTP_PREFIX) 
							:  false;
	}
	
	public static boolean isHttpProtocol(String path){
		return path != null ? path.startsWith(HTTP_PREFIX) 
							|| path.startsWith(HTTPS_PREFIX) 
							:  false;
	}

	public static boolean doesURLExist (String urlName) {
	    try {
	    	if (isHttpProtocol(urlName))
	    		return doesWebURLExist(new URL(urlName));
	    	else{
	    		new URL(urlName).openStream().close();
	    		return true;
	    	}
	    } catch (IOException e) {
	        return false;
	    }
	}
	
	public static boolean doesWebURLExist(URL url)
	{
		HttpURLConnection httpURLConnection = null;
		boolean success = false;
		
		long checkStart = System.currentTimeMillis();
		
		try {
			// We want to check the current URL
			HttpURLConnection.setFollowRedirects(false);

			httpURLConnection = (HttpURLConnection) url.openConnection();

			// We don't need to get data
			httpURLConnection.setRequestMethod("HEAD");

			// Some web sites don't like programmatic access so pretend to be a browser
			httpURLConnection.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows; U; Windows NT 6.0; en-US; rv:1.9.1.2) Gecko/20090729 Firefox/3.5.2 (.NET CLR 3.5.30729)");
						
			int responseCode = httpURLConnection.getResponseCode();
			
			success = true;
			
			// We only accept response code 200
			return responseCode == HttpURLConnection.HTTP_OK;
			
	    } catch (IOException e) {
	        return false;
	    } finally {
	    	// Log a warning when it is too slow to connect as it impacts the calling program performance
			long checkEnd = System.currentTimeMillis();
			if (checkEnd - checkStart > 5000L) {
				if (success) {
					logger.warn("URL connection test was very slow although it succeeded. It took " + (checkEnd - checkStart)/1000L + " seconds to connect to this network resource : " + url);
					logger.warn("  There is possibly a network issue or the remote server is under load.");					
				}
				else {
					logger.warn("URL connection test was very slow and failed. It took " + (checkEnd - checkStart)/1000L + " seconds to try to connect to this network resource : " + url);
					logger.warn("  This may be due to a DNS problem or the remote target is simply not reachable (then update the URL configuration).");	
				}
			}
	    	
	    	// Disconnect
	    	if (httpURLConnection != null)
	    		try { httpURLConnection.disconnect(); } catch (Exception ex) {}
	    }
	}

	public static boolean doesFileExist(String path) {
		if (path == null)
			return false;
		
		File file = new File(path);
		return file.isFile() && file.exists();
	}

	public static String getFilePrefix() {
		return isWindows() ? WIN_FILE_PREFIX :UNIX_FILE_PREFIX;
	}
	
	public static byte[] read(File file) throws IOException{
	    byte[] buffer = new byte[(int) file.length()];

	    try (
	    	    InputStream ios = new FileInputStream(file);
	    	)
	    {
	        if (ios.read(buffer) == -1) {
	            throw new IOException("EOF reached while trying to read the whole file");
	        }
	    }
	    return buffer;
	}
}

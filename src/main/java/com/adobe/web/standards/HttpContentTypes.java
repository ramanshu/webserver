package com.adobe.web.standards;

import java.util.Dictionary;
import java.util.Hashtable;
/**
 * Stores Content types for different extensions
 * @author Ramanshu Mahaur
 *
 */
public class HttpContentTypes {
	static Dictionary< String, String> contentTypes;
	static
	{
		contentTypes = new Hashtable< String, String>();
		// Type Application
//		contentTypes.put("", "application/atom+xml");
//		contentTypes.put("", "application/ecmascript");
//		contentTypes.put("", "application/EDI-X12");
//		contentTypes.put("", "application/EDIFACT");
//		contentTypes.put("", "application/json");
//		contentTypes.put("", "application/javascript");
		contentTypes.put("exe", "application/octet-stream");
//		contentTypes.put("", "application/ogg");
		contentTypes.put("pdf", "application/pdf");
//		contentTypes.put("", "application/postscript");
//		contentTypes.put("", "application/rdf+xml");
//		contentTypes.put("", "application/rss+xml");
//		contentTypes.put("", "application/soap+xml");
//		contentTypes.put("", "application/font-woff");
//		contentTypes.put("", "application/x-font-woff");
//		contentTypes.put("", "application/xhtml+xml");
//		contentTypes.put("", "application/xml");
//		contentTypes.put("", "application/xml-dtd");
//		contentTypes.put("", "application/xop+xml");
		contentTypes.put("zip", "application/zip");
//		contentTypes.put("", "application/gzip");
//		// Type Audio
//		contentTypes.put("", "audio/basic");
//		contentTypes.put("", "audio/L24");
//		contentTypes.put("", "audio/mp4");
		contentTypes.put("mp3", "audio/mpeg");
		contentTypes.put("wav", "audio/x-wav");
//		contentTypes.put("", "audio/ogg");
//		contentTypes.put("", "audio/vorbis");
//		contentTypes.put("", "audio/vnd.rn-realaudio");
//		contentTypes.put("", "audio/vnd.wave");
//		contentTypes.put("", "audio/webm");
//		// Type Image
		contentTypes.put("gif", "image/gif");
		contentTypes.put("jpg", "image/jpeg");
		contentTypes.put("jpeg", "image/jpeg");
		contentTypes.put("pjpeg", "image/pjpeg");
		contentTypes.put("png", "image/png");
//		contentTypes.put("", "image/svg+xml");
		contentTypes.put("tiff", "image/tiff");
//		// Type message
//		contentTypes.put("", "message/http");
//		contentTypes.put("", "message/imdn+xml");
//		contentTypes.put("", "message/partial");
//		contentTypes.put("", "message/rfc822");
//		// Type model
//		contentTypes.put("", "model/example");
//		contentTypes.put("", "model/iges");
//		contentTypes.put("", "model/mesh");
//		contentTypes.put("", "model/vrml");
//		contentTypes.put("", "model/x3d+binary");
//		contentTypes.put("", "model/x3d+vrml");
//		contentTypes.put("", "model/x3d+xml");
//		// Type multipart
//		contentTypes.put("", "multipart/mixed");
//		contentTypes.put("", "multipart/alternative");
//		contentTypes.put("", "multipart/related");
//		contentTypes.put("", "multipart/form-data");
//		contentTypes.put("", "multipart/signed");
//		contentTypes.put("", "multipart/encrypted");
//		// Type Text
//		contentTypes.put("", "text/cmd");
		contentTypes.put("css", "text/css");
		contentTypes.put("csv", "text/csv");
		contentTypes.put("html", "text/html");
		contentTypes.put("js", "text/javascript");
//		contentTypes.put("", "text/plain");
//		contentTypes.put("", "text/vcard");
		contentTypes.put("xml", "text/xml");
//		// Type Video
		contentTypes.put("mpeg", "video/mpeg");
		contentTypes.put("mpe", "video/mpeg");
		contentTypes.put("mpg", "video/mpeg");
		contentTypes.put("mp4", "video/mp4");
		contentTypes.put("ogg", "video/ogg");
		contentTypes.put("mov", "video/quicktime");
//		contentTypes.put("", "video/webm");
//		contentTypes.put("", "video/x-matroska");
		contentTypes.put("avi", "video/x-msvideo");
		contentTypes.put("wmv", "video/x-ms-wmv");
		contentTypes.put("doc", "application/msword");				
		contentTypes.put("xls", "application/vnd.ms-excel");				
		contentTypes.put("ppt", "application/vnd.ms-powerpoint");				
		contentTypes.put("flv", "video/x-flv");				
		contentTypes.put("flv", "video/x-flv");				
		contentTypes.put("flv", "video/x-flv");				
		contentTypes.put("flv", "video/x-flv");				
			
		}
	public static String get(String key)
	{
		String value=contentTypes.get(key);
		if(value!=null)
		{
			return value;
		}
		else return "text/plain";
	}	
}

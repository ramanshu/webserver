package com.adobe.web.standards;

import java.util.Hashtable;
/**
 * Stores reponse messages for different response codes.
 * @author mahaur
 *
 */
public class HttpResponseCodes {
	static Hashtable< Integer, String> responseCodes;
	static 
	{
		responseCodes = new Hashtable< Integer, String>();
		responseCodes.put(100, "Continue");
		responseCodes.put(101, "Switching Protocols");
		responseCodes.put(102, "Processing");
		responseCodes.put(200, "OK");
		responseCodes.put(201, "Created");
		responseCodes.put(202, "Accepted");
		responseCodes.put(203, "Non-Authoritative Information");
		responseCodes.put(204, "No Content");
		responseCodes.put(205, "Reset Content");
		responseCodes.put(206, "Partial Content");
		responseCodes.put(207, "Multi-Status");
		responseCodes.put(208, "Already Reported");
		responseCodes.put(226, "IM Used");
		responseCodes.put(300, "Multiple Choices");
		responseCodes.put(301, "Moved Permanently");
		responseCodes.put(302, "Found");
		responseCodes.put(303, "See Other");
		responseCodes.put(304, "Not Modified");
		responseCodes.put(305, "Use Proxy");
		responseCodes.put(306, "Switch Proxy");
		responseCodes.put(307, "Temporary Redirect");
		responseCodes.put(308, "Permanent Redirect");
		responseCodes.put(400, "Bad Request");
		responseCodes.put(401, "Unauthorized");
		responseCodes.put(402, "Payment Required");
		responseCodes.put(403, "Forbidden");
		responseCodes.put(404, "Not Found");
		responseCodes.put(405, "Method Not Allowed");
		responseCodes.put(406, "Not Acceptable");
		responseCodes.put(407, "Proxy Authentication Required");
		responseCodes.put(408, "Request Timeout");
		responseCodes.put(409, "Conflict");
		responseCodes.put(410, "Gone");
		responseCodes.put(411, "Length Required");
		responseCodes.put(412, "Precondition Failed");
		responseCodes.put(413, "Request Entity Too Large");
		responseCodes.put(414, "Request-URI Too Long");
		responseCodes.put(415, "Unsupported Media Type");
		responseCodes.put(416, "Requested Range Not Satisfiable");
		responseCodes.put(417, "Expectation Failed");
		//responseCodes.put(418, "I'm a teapot");
		responseCodes.put(419, "Authentication Timeout");
		responseCodes.put(420, "Enhance Your Calm");
		responseCodes.put(422, "Unprocessable Entity");
		responseCodes.put(423, "Locked");
		responseCodes.put(424, "Failed Dependency");
		//responseCodes.put(424, "Method Failure");
		responseCodes.put(425, "Unordered Collection");
		responseCodes.put(426, "Upgrade Required");
		responseCodes.put(428, "Precondition Required");
		responseCodes.put(429, "Too Many Requests");
		responseCodes.put(431, "Request Header Fields Too Large");
		responseCodes.put(444, "No Response");
		responseCodes.put(449, "Retry With");
		responseCodes.put(450, "Blocked by Windows Parental Controls");
		responseCodes.put(451, "Unavailable For Legal Reasons");
		//responseCodes.put(451, "Redirect");
		responseCodes.put(494, "Request Header Too Large");
		responseCodes.put(495, "Cert Error");
		responseCodes.put(496, "No Cert");
		responseCodes.put(497, "HTTP to HTTPS");
		responseCodes.put(499, "Client Closed Request");
				
	}
	public static String get(Integer key)
	{
		return responseCodes.get(key);
	}	
}

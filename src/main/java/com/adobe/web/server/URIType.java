package com.adobe.web.server;
/**
 * Identifies the uri type from the uri message.
 * 
 * @author mahaur
 *
 */
// Currently all requests are served as file/directory/admin/ request 
public enum URIType {
	FILE , DIRECTORY , PHP , JSP, ASP, ADMIN;
	
	 static URIType getType(String uri)
	{
		if (uri.startsWith("/admin/"))
			return ADMIN;
		else if( uri.endsWith(".php"))
			return PHP;
		else if(uri.endsWith(".jsp"))
			return JSP;
		else if (uri.endsWith(".asp"))
			return ASP;
		else if (uri.matches(".*[.].*"))
			return FILE;
		else 
			return DIRECTORY;		
	}
}

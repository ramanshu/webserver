package com.adobe.web.server;

public class URIType {
	static final int FILE = 0;
	static final int DIRECTORY = 1;
	static final int PHP = 2;
	static final int JSP = 3;
	static final int ASP = 4;
	static final int ADMIN = 5;
	//static final FILE = 0;
	
	static int getType(String uri)
	{
		if (uri.startsWith("/admin"))
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

package com.adobe.web.protocol;

import java.io.PrintStream;
import java.util.Dictionary;
import java.util.Enumeration;

public class HttpWebPacket {
	String head;
	Dictionary<String, String> headers;
	byte[] body;
	
	void send(PrintStream out)
	{
		out.print(head+"\r\n");
		if(headers!=null)
		{
			Enumeration<String> list = headers.keys();
			while(list.hasMoreElements())
			{
				String key=list.nextElement();
				out.print(key+": "+headers.get(key)+"\r\n");
			}
		}
		out.print("\r\n");
		out.write(body,0,body.length);
		out.flush();
		
	}

}

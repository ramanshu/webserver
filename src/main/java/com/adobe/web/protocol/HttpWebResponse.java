package com.adobe.web.protocol;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.StringTokenizer;

import com.adobe.web.standards.HttpResponseCodes;


public class HttpWebResponse {

	public int responseCode;
	public byte[] responseData;
	public ArrayList<String> headers;
	public HttpWebResponse(int code, byte[] body,ArrayList<String> headers)
	{
		this.responseCode = code;
		this.responseData = body;
		this.headers = headers;
	}
	public HttpWebResponse() 
	{
		headers=new ArrayList<String>();
	}
	public HttpWebResponse(InputStream in,boolean read) throws Exception
	{
		String line = readLine(in);
		StringTokenizer st = new StringTokenizer(line, " ");
		st.nextToken();
		responseCode = Integer.parseInt(st.nextToken());
		headers = new ArrayList<String>();
		String header=readLine(in);
		while(header!=null && header.length()>0)
		{
			headers.add(header);
			header = readLine(in);
		}
		int bodySize=0;
		if(read)
		{
			for(int i=0;i<headers.size();i++)
			{
				if(headers.get(i).startsWith("Content-length: "))
				{
					bodySize = Integer.parseInt((headers.get(i).substring("Content-length: ".length())));
					break;
				}
			}
			if(bodySize>0)
			{	
				ByteArrayOutputStream buffer = new ByteArrayOutputStream();
				int nRead,total=0;
				byte[] data = new byte[16384];
				while (total<bodySize && (nRead = in.read(data, 0, data.length)) != -1 ) 
				{
					buffer.write(data, 0, nRead);
					total+=nRead;
				}
				buffer.flush();
				responseData = buffer.toByteArray();
			}
		}
		else
			responseData = new byte[0];
	}
	private String readLine(InputStream in) throws IOException {
		String buffer = "";
		char c;
		while (true) {
			c = (char) in.read();
			if (c == -1 || c == '\n' || c == '\r') {
				if (c == '\r')
					c = (char) in.read();
				return buffer;
			} else
				buffer += c;
		}

	}
	public boolean respond(PrintStream out)
	{
		try{
		out.print("HTTP/1.0 "+responseCode+" "+HttpResponseCodes.get(responseCode)+"\r\n");
		for (int i=0;i<headers.size();i++)
		{
			out.print(headers.get(i)+"\r\n");
		}
		out.print("\r\n");
		out.write(responseData);
		out.flush();
		return true;
		}
		catch(Exception e)
		{
			return false;			
		}
	}
	
	public static boolean httpXXXResponse(int responseCode,ArrayList<String> headers,byte[] responseData,PrintStream out)
	{
		try{
		out.print("HTTP/1.0 "+responseCode+" "+HttpResponseCodes.get(responseCode)+"\r\n");
		for (int i=0;i<headers.size();i++)
		{
			out.print(headers.get(i)+"\r\n");
		}
		out.print("\r\n");
		out.write(responseData);
		out.flush();
		return true;
		}
		catch(Exception e)
		{
			return false;			
		}
	}	
}

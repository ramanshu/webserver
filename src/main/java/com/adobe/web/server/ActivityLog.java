package com.adobe.web.server;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;

import com.adobe.web.protocol.HttpWebRequest;
import com.adobe.web.protocol.HttpWebResponse;
import com.adobe.web.standards.HttpResponseCodes;
/**
 * Stores Activity log for a single session of server run.
 * @author mahaur
 *
 */

public class ActivityLog extends ArrayList<String>{
	int size;
	int current;
	ActivityLog(int length) throws IOException
	{
		
		for(int i=0;i<length;i++)
		super.add("");
		size = length;
		current = 0;
	}
	/**
	 * Add a log entry to the log with given parameters
	 * @param request
	 * 			Request served
	 * @param response
	 * 			Response generated
	 * @param startTime
	 * 			Time at which server started reading request.
	 * @throws IOException 
	 */
	void addLog(HttpWebRequest request,HttpWebResponse response, long startTime)
	{
			Date date = new Date();
			set(current,date.toGMTString() + " : <span class=\"request\">\""+request.method +" "+ request.url +" " +request.version+ "\"</span> with response "+
					"<span class=\"response\">\""+response.responseCode +" " + HttpResponseCodes.get(response.responseCode)+"\"</span> in <span class=\"reponse-time\">"+ (System.currentTimeMillis()-startTime)+ "ms</span>\n");
			current = (current + 1)%size;
	}
	
	String getLog() throws IOException
	{
		String log="";
		for(int i =1 ;i<=size;i++){
			log+=get((current-i+size)%size)+"\n";
		}
		return log;
	}

}

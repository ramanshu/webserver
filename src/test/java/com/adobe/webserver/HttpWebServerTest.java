package com.adobe.webserver;

import static org.junit.Assert.*;

import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Properties;

import org.apache.log4j.PropertyConfigurator;
import org.junit.Test;

import com.adobe.web.client.HttpWebClient;
import com.adobe.web.protocol.HttpWebResponse;
import com.adobe.web.server.HttpWebServer;

public class HttpWebServerTest {
	HttpWebServer web;
	HttpWebResponse response;
	Properties prop;
	Integer requestsProcessed;
	String testUrl;
	
	public class HttpWebClientThread implements Runnable{
		int taskId;
		String url;
		byte[] data;
		int numRequest;
		/*
		 * Taskid if
		 * 0 -> Get url
		 * 1 -> Post url
		 * 2 -> Head url
		 * 3 -> Put url
		 * 4 -> Delete url
		 * 5 -> Stress test
		 * */
		
		HttpWebClientThread(int id,String url)
		{
			taskId=id;
			this.url = url;
		}
		HttpWebClientThread(int id,String url,int numRequest)
		{
			taskId=id;
			this.url = url;
			this.numRequest= numRequest;
		}
		HttpWebClientThread(int id,String url,byte[] data)
		{
			taskId=id;
			this.url = url;
			this.data = data;
		}
		public void run ()
		{
					try {
						HttpWebClient client = new HttpWebClient();
						client.openSocket("localhost", 8000);
						switch(taskId)
						{
						case 0:
							response = client.makeGetRequest(url);
							break;
						case 1:
							response  =client.makePostRequest(url,data);
							break;
						case 2:
							response  = client.makeHeadRequest(url);
							break;
						case 3:
							response = client.makePutRequest(url,data);
							break;
						case 4:
							response = client.makeDeleteRequest(url);
							break;
						case 5:
						{
							int i=0;
							while(i<numRequest)
							{
								HttpWebResponse response = client.makeGetRequest(url);
								if(response.responseCode == 200)
								{
									synchronized(requestsProcessed)
									{
										requestsProcessed++;	
									}
								}
								client.closeSocket();
								client.openSocket("localhost", 8000);
								i++;
							}
						}
						break;
						}
				    } catch (Exception e) {
						e.printStackTrace();
					}

		}
	}
	public class HttpWebServerTesTThread implements Runnable{
		public  void run(){
			 PropertyConfigurator.configure(this.getClass().getClassLoader().getResourceAsStream("properties/log4j.properties"));
			 
	    	try {
	    		prop= new Properties();
	    		prop.load(this.getClass().getClassLoader().getResourceAsStream("properties/config.properties"));
	    		web = new HttpWebServer(prop);
				web.Start();
	    	} catch (Exception ex) {
	    		ex.printStackTrace();
	        }		
		}	
	}
	
	void setupTest(int branch,String testUrl) throws IOException, InterruptedException
	{
		Thread server = new Thread(new HttpWebServerTesTThread());
		HttpWebClientThread client =(new HttpWebClientThread(branch,testUrl));
		server.start();
		client.run();
		web.Stop();
	}
	@Test
	public void testGet() throws InterruptedException, IOException {
		
		String testUrl ="/admin/js/jquery.js";
		
		setupTest(0,testUrl);

		InputStream in = getClass().getClassLoader().getResourceAsStream("admin/js/jquery.js");
		ByteArrayOutputStream buffer = new ByteArrayOutputStream();
		int nRead;
		byte[] data = new byte[16384];

		while ((nRead = in.read(data, 0, data.length)) != -1 ) 
		{
		  buffer.write(data, 0, nRead);
		}
		buffer.flush();
		byte[] fileData = buffer.toByteArray();

		assertArrayEquals("File is same as response", fileData, response.responseData); 
	}


	@Test
	public void testHead() throws InterruptedException, IOException {
		String testUrl ="/admin/js/jquery.js";
		setupTest(2,testUrl);
		
		InputStream in = getClass().getClassLoader().getResourceAsStream("admin/js/jquery.js");
		ByteArrayOutputStream buffer = new ByteArrayOutputStream();
		int nRead;
		byte[] data = new byte[16384];

		while ((nRead = in.read(data, 0, data.length)) != -1 ) 
		{
		  buffer.write(data, 0, nRead);
		}
		buffer.flush();
		byte[] fileData = buffer.toByteArray();
		int bodySize =0;
		for(int i=0;i<response.headers.size();i++)
		{
			if(response.headers.get(i).startsWith("Content-length: "))
			{
				bodySize = Integer.parseInt((response.headers.get(i).substring("Content-length: ".length())));
				break;
			}
		}

	 	assertEquals("File Size is correct", fileData.length, bodySize); 		
	 	assertEquals("Response Data is null", 0, response.responseData.length); 		
	}
	@Test
	public void testDelete() throws InterruptedException, IOException {

		InputStream in = getClass().getClassLoader().getResourceAsStream("admin/js/jquery.js");
		ByteArrayOutputStream buffer = new ByteArrayOutputStream();
		int nRead;
		byte[] data = new byte[16384];

		while ((nRead = in.read(data, 0, data.length)) != -1 ) 
		{
		  buffer.write(data, 0, nRead);
		}
		buffer.flush();
		byte[] fileData = buffer.toByteArray();
		Properties prop= new Properties();
		prop.load(this.getClass().getClassLoader().getResourceAsStream("properties/config.properties"));
		String path = (String) prop.getProperty("documentRoot");		
		FileOutputStream fos = new FileOutputStream(path+"/testFileDelte");
		fos.write(fileData);
		fos.close();		
		
		String testUrl ="/testFileDelte";
		
		Thread server = new Thread(new HttpWebServerTesTThread());
		Thread client = new Thread(new HttpWebClientThread(0,testUrl));
		server.start();
		client.start();		
		client.join();
		assertEquals("File exist on server",response.responseCode,200);
		client = new Thread(new HttpWebClientThread(4,testUrl));
		client.start();		
		client.join();
		client = new Thread(new HttpWebClientThread(0,testUrl));
		client.start();		
		client.join();
		assertEquals("File deleted",response.responseCode,404);
		web.Stop();
	}
	@Test
	public void testPut() throws InterruptedException, IOException {
		String testUrl ="/testFile3";
		String fileToPut ="admin/js/jquery.js";
		Properties prop= new Properties();
		prop.load(this.getClass().getClassLoader().getResourceAsStream("properties/config.properties"));

		InputStream in = getClass().getClassLoader().getResourceAsStream(fileToPut);
		ByteArrayOutputStream buffer = new ByteArrayOutputStream();
		int nRead;
		byte[] data = new byte[16384];

		while ((nRead = in.read(data, 0, data.length)) != -1 ) 
		{
		  buffer.write(data, 0, nRead);
		}
		buffer.flush();
		byte[] fileData = buffer.toByteArray();
		
		Thread server = new Thread(new HttpWebServerTesTThread());
		HttpWebClientThread client = new HttpWebClientThread(3,testUrl,fileData);
		server.start();
		client.run();		
		client = new HttpWebClientThread(0,testUrl);
		client.run();		
		web.Stop();
		String path = (String) prop.getProperty("documentRoot");
		Path file2 = Paths.get(path+testUrl);
		byte[] fileData2;
		fileData2 = Files.readAllBytes(file2);
	
		assertArrayEquals("File is same as response", fileData, fileData2); 		
	}
	@Test
	public void testStress() throws InterruptedException, IOException {
		int numClients = 10;
		int numRequests = 10;
		String testUrl ="/admin/javadocs/index.html";
		Thread server = new Thread(new HttpWebServerTesTThread());
		server.start();
		requestsProcessed=0;
		Thread[] client = new Thread[numClients];
		for(int i =0;i<numClients;i++)
		{
			client[i] = new Thread(new HttpWebClientThread(5,testUrl,numRequests));
			client[i].start();			
		}
		
		for(int i =0;i<numClients;i++)
		{
			client[i].join();
		}
		//web.Stop();
		assertTrue("All requests Processed",this.requestsProcessed==numClients*numRequests); 		
	}
	
}

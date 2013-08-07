package com.adobe.webserver;

import static org.junit.Assert.*;

import java.io.FileInputStream;
import java.io.IOException;
import java.net.URISyntaxException;
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
	boolean isRunning;
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
	public class HttpWebServerThread implements Runnable{
		public  void run(){
			 PropertyConfigurator.configure("log4j.properties");
			 
	    	try {
	    		prop= new Properties();
	    		prop.load(new FileInputStream("config.properties"));
	    		//System.out.println((String) prop.getProperty("port"));
	    		web = new HttpWebServer(prop);
				web.Start();
	    	} catch (Exception ex) {
	    		ex.printStackTrace();
	        }		
		}	
	}

	@Test
	public void testGet() throws InterruptedException, IOException {
		
		String testUrl ="/testFile1.txt";
		
		Thread server = new Thread(new HttpWebServerThread());
		Thread client = new Thread(new HttpWebClientThread(0,testUrl));
		
		server.start();
		client.start();
		
		client.join();
		web.Stop();
		String path = (String) prop.getProperty("documentRoot");
		Path file = Paths.get(path+testUrl);
		byte[] fileData;
		fileData = Files.readAllBytes(file);
	 	assertArrayEquals("File is same as response", fileData, response.responseData); 
	}

//	@Test
//	public void testPost() {
//		
//		
//		fail("Not yet implemented");
//	}
	@Test
	public void testHead() throws InterruptedException, IOException {
		String testUrl ="/testFile1.txt";
		
		Thread server = new Thread(new HttpWebServerThread());
		Thread client = new Thread(new HttpWebClientThread(2,testUrl));
		
		server.start();
		client.start();
		
		client.join();
		web.Stop();
		String path = (String) prop.getProperty("documentRoot");
		Path file = Paths.get(path+testUrl);
		byte[] fileData;
		fileData = Files.readAllBytes(file);
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
		String testUrl ="/testFile2.txt";
		
		Thread server = new Thread(new HttpWebServerThread());
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
		String testUrl ="/testFile3.txt";
		String fileToPut ="/testFile1.txt";
		Properties prop= new Properties();
		prop.load(new FileInputStream("config.properties"));
		String path = (String) prop.getProperty("documentRoot");
		Path file = Paths.get(path+fileToPut);
		byte[] fileData;
		fileData = Files.readAllBytes(file);
		
		Thread server = new Thread(new HttpWebServerThread());
		Thread client = new Thread(new HttpWebClientThread(3,testUrl,fileData));
		server.start();
		client.start();		
		client.join();
		client = new Thread(new HttpWebClientThread(0,testUrl));
		client.start();		
		web.Stop();
		Path file2 = Paths.get(path+testUrl);
		byte[] fileData2;
		fileData2 = Files.readAllBytes(file2);
	
		assertArrayEquals("File is same as response", fileData, fileData2); 		
	}
	@Test
	public void testStress() throws InterruptedException, IOException {
		int numClients = 100;
		int numRequests = 100;
		String testUrl ="/testFile1.txt";
		Thread server = new Thread(new HttpWebServerThread());
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

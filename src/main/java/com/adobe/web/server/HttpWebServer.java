package com.adobe.web.server;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedList;
import java.util.Properties;
import java.util.Queue;

import org.apache.log4j.Logger;

import com.adobe.web.protocol.HttpWebRequest;
import com.adobe.web.protocol.HttpWebResponse;
import com.adobe.web.standards.HttpResponseCodes;

public class HttpWebServer {
	private static int port;
	private static ServerSocket serverSocket;
	private String host;
	private static int threadPoolSize;
	private static int requestTimeOut;
	private static int readTimeOut;
	private static int requestQueueMaxSize;
	private static Thread[] httpWebServerThread;
	private static HttpRequestProcessor requestProcessor;
	private static Queue<Socket> requestQueue = new LinkedList<Socket>();
	private static long[] lastSeenAt;
	private static boolean[] hasRequest;
	private static boolean keepAlive;
	private static int requestsRecieved=0;
	private static Integer requestsServed=0;
	static Logger log = Logger.getLogger(HttpWebServer.class);
	public static ArrayList<String> requestLog = new ArrayList<String>();

	public HttpWebServer() throws InstantiationException, IllegalAccessException, URISyntaxException, IOException {
		port = 8000;
		host="localhost";
		threadPoolSize = 10;
		requestQueueMaxSize =10;
		requestProcessor = new HttpRequestProcessor(null,this);
		httpWebServerThread = new Thread[threadPoolSize];
		keepAlive=false;
		//log4j.appender.file.File
		hasRequest = new boolean[threadPoolSize];
		lastSeenAt = new long[threadPoolSize];
		}
	public HttpWebServer(Properties prop) throws InstantiationException, IllegalAccessException, URISyntaxException, IOException {
		//get the property value and print it out
		port = Integer.parseInt(prop.getProperty("port").trim());
		host=prop.getProperty("hostname").trim();
		threadPoolSize = Integer.parseInt(prop.getProperty("threadPoolSize").trim());
		requestQueueMaxSize =Integer.parseInt(prop.getProperty("requestQueueMaxSize").trim());
		requestTimeOut =Integer.parseInt(prop.getProperty("requestTimeOut").trim());
		readTimeOut =Integer.parseInt(prop.getProperty("readTimeOut").trim());
		requestProcessor = new HttpRequestProcessor(prop,this);
		httpWebServerThread = new Thread[threadPoolSize];
		if(prop.getProperty("keepAlive").trim().equalsIgnoreCase("true"))
			keepAlive=true;
		else 
			keepAlive=false;
		hasRequest = new boolean[threadPoolSize];
		lastSeenAt = new long[threadPoolSize];
	}
	public class HttpWebSchedulerThread implements Runnable{
		
		public void run ()
		{
			long current;
			try{
				
				while(true)
				{
					current = System.currentTimeMillis();
					for(int i =0;i<threadPoolSize;i++)
					{	if(hasRequest[i] && (current - lastSeenAt[i])>requestTimeOut)
						{
							httpWebServerThread[i].stop(new Exception());
						}
					}
					Thread.sleep(10);
				}
			}
			catch(InterruptedException e)
			{
				e.printStackTrace();
			}
		}
	}
	private static class HttpWebServerThread implements Runnable {
		private int threadID;
		Socket socket;
		int processed;
		HttpWebServerThread(int i) {
			threadID = i;
			processed = 0;
		}

		public void run() {
			while (true) {
				try {
					
					synchronized(requestQueue)
					{
						while(requestQueue.isEmpty())
							requestQueue.wait();
						socket = requestQueue.poll();
						requestQueue.notify();
					}
					InputStream in = socket.getInputStream();
					socket.setSoTimeout(readTimeOut);
					PrintStream out = new PrintStream(new BufferedOutputStream(socket.getOutputStream()));
					boolean alive = true;
					hasRequest[threadID] =true;
					while(alive)
					{

						try
						{
							lastSeenAt[threadID] = System.currentTimeMillis();
							HttpWebRequest request = new HttpWebRequest(in);
							HttpWebResponse response= requestProcessor.requestProcessorTemp(request);
							response.respond(out);
							out.flush();
							log.info("URL "+request.url+" "+response.responseCode+" "+(System.currentTimeMillis()-lastSeenAt[threadID])+"ms ");
							long current =System.currentTimeMillis();
							Date date = new Date(current);
							requestLog.add(date.toGMTString() + " : <span class=\"request\">\""+request.method +" "+ request.url +" " +request.version+ "\"</span> with response "+
									"<span class=\"response\">\""+response.responseCode +" " + HttpResponseCodes.get(response.responseCode)+"\"</span> in <span class=\"reponse-time\">"+ (current -lastSeenAt[threadID])+ "ms</span>");
							processed++;
							synchronized(requestsServed)
							{
								requestsServed++;
								//System.out.println(requestsRecieved+" "+requestsServed);
							}
							String connection=request.headers.get("Connection");
							if(!keepAlive ||(connection!=null && connection.equalsIgnoreCase("close")) )
							{
								alive=false;
								hasRequest[threadID] = false;
							}
						}
						catch(Exception e)
						{
							alive=false;
							hasRequest[threadID] = false;
							Thread.interrupted();
						}
					}
					out.close();
					in.close();
					socket.close();				
				} catch (IOException e) {

				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
	}
	}
	public void Start() throws IOException, InterruptedException {
		
		try{
		serverSocket = new ServerSocket(port);
		log.info("Starting server on port " + port);
		}
		catch(Exception e)
		{
			log.info("Could not start server on port: " + port+". Looks like port is already in use.");
			System.exit(0);
		}
		for (int i = 0; i < threadPoolSize; i++) {
			httpWebServerThread[i] = new Thread(new HttpWebServerThread(i));
			httpWebServerThread[i].start();
		}
		Thread httpWebSchedulerThread= new Thread(new HttpWebSchedulerThread());
		httpWebSchedulerThread.start();
		while(true)
		{
			Socket socket = serverSocket.accept();
			requestsRecieved++;
			synchronized(requestQueue)
			{
				while(requestQueue.size()>=requestQueueMaxSize)
					requestQueue.wait();
				requestQueue.add(socket);
				requestQueue.notify();
			}
		}
	}
	public void Stop() throws IOException, InterruptedException {
		for (int i = 0; i < threadPoolSize; i++) {
			httpWebServerThread[i].stop();
		}
		serverSocket.close();
		
	}

}

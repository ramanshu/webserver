package com.adobe.web.server;
import java.io.IOException;
import java.net.ServerSocket;
import java.util.HashMap;
import java.util.Properties;
import org.apache.log4j.Logger;

public class HttpWebServer {
	ThreadPool threadPool; 
	private int port;
	private ServerSocket serverSocket;
	private String host;
	private int threadPoolSize;
	
	private int requestTimeOut;
	private int readTimeOut;
	private int requestQueueMaxSize;
	private boolean keepAlive;
	static Logger log = Logger.getLogger(HttpWebServer.class);
	Properties prop;
	public HttpWebServer(Properties prop1) {
		//get the property value and print it out
		this.prop = prop1;
		port = Integer.parseInt(prop.getProperty("port").trim());
		host=prop.getProperty("hostname").trim();
		threadPoolSize = Integer.parseInt(prop.getProperty("threadPoolSize").trim());
		requestQueueMaxSize =Integer.parseInt(prop.getProperty("requestQueueMaxSize").trim());
		requestTimeOut =Integer.parseInt(prop.getProperty("requestTimeOut").trim());
		readTimeOut =Integer.parseInt(prop.getProperty("readTimeOut").trim());
		if(prop.getProperty("keepAlive").trim().equalsIgnoreCase("true"))
			keepAlive=true;
		else 
			keepAlive=false;
		//threadPoolSize,requestTimeOut,serverSocket,requestQueueMaxSize
	}
	public void Start() throws IOException, InterruptedException {
		
		try
		{
			// Start sever socket.
			serverSocket = new ServerSocket(port);
			log.info("Starting server on port " + port);
		}
		catch(Exception e)
		{
			log.info("Could not start server on port: " + port+". Looks like port is already in use.");
			System.exit(0);
		}
		try
		{
			// Initialize and start a thread pool with specified parameters
			threadPool = new ThreadPool (threadPoolSize,requestTimeOut,serverSocket,requestQueueMaxSize);
			threadPool.setSharedVariables(readTimeOut, keepAlive, prop);
			threadPool.start();
			log.info("Thread Pool Running with "+threadPool.size()+" server threads.");
		}
		catch(Exception e)
		{
			threadPool.stop(0);
			log.info("Thread Pool Initailization error");
			System.exit(0);
		}
		
	}
	public void Stop() throws IOException, InterruptedException 
	{
		// Stop server socket
		serverSocket.close();
		// Stop thread pool
		threadPool.stop(0);
	}
}

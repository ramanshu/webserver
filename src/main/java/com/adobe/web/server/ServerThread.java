package com.adobe.web.server;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.net.Socket;
import java.util.Queue;

import org.apache.log4j.Logger;

import com.adobe.web.exceptions.RequestTimeOutException;
import com.adobe.web.protocol.HttpWebRequest;
import com.adobe.web.protocol.HttpWebResponse;
import com.adobe.web.standards.HttpResponseCodes;

class ServerThread extends Thread {
	private int threadID;
	private Socket socket;
	public static int readTimeOut;
	public static boolean keepAlive;
	private int processed;
	private long lastSeenAt;
	private boolean hasRequest;
	static ActivityLog activityLog ;
	static Queue<Socket> requestQueue ;
	static HttpRequestProcessor requestProcessor;
	private static Logger log = Logger.getLogger(ServerThread.class);
	ServerThread(int id) {
		this.threadID = id;
		processed = 0;
		hasRequest= false;
	}
	ServerThread() {
	}
	
	boolean isBusy(long requestTimeOut)
	{
		return (hasRequest	&& (System.currentTimeMillis()-lastSeenAt)>requestTimeOut);
	}
	int processCount()
	{
		return processed;
	}
	boolean persistConnection(HttpWebRequest request)
	{
		
		String connection=request.headers.get("Connection");
		return (!keepAlive ||(connection!=null && connection.equalsIgnoreCase("close")));
	}
	Socket readSocketFromQueue() throws InterruptedException
	{
		Socket socket;
		synchronized(requestQueue)
		{
			while(requestQueue.isEmpty())
				requestQueue.wait();
			socket = requestQueue.poll();
			requestQueue.notify();
		}
		return socket;

	}
	
	public void run() {
		while (true) {
			try {
				// read socket from queue
				socket = readSocketFromQueue();
				socket.setSoTimeout(readTimeOut);
				InputStream in = socket.getInputStream();
				PrintStream out = new PrintStream(new BufferedOutputStream(socket.getOutputStream()));
				// Set has request to true
				boolean persistent = true,requestServed=false;
				hasRequest =true;
				// Wait for a request
				while(persistent)
				{
					try
					{
						// Save current time 
						lastSeenAt = System.currentTimeMillis();
						// Get request object
						HttpWebRequest request = new HttpWebRequest(in);
						// Get response for the given request
						HttpWebResponse response= requestProcessor.requestProcessor(request);
						// Write the response to outputstream
						response.respond(out);
						// Mark request as served
						requestServed=true;
						log.info("{"+request.method+" "+request.url+"} {"+response.responseCode+" "+HttpResponseCodes.get(response.responseCode)+"} "+(System.currentTimeMillis()-lastSeenAt)+"ms ");						
						activityLog.addLog(request,response, lastSeenAt);

						processed++;
						
						
						// Check if to read another request over the same socket
						if(persistConnection(request))
						{
							persistent=false;
							hasRequest = false;
						}
					}
					// Catch time out exception and do not listen to more requests over same socket
					catch(RequestTimeOutException e)
					{
						
						persistent=false;
						hasRequest = false;
						log.info("Server Thread Timed out");
						// If request is not yet served return a 408 response
						if(!requestServed)
						{
							HttpWebResponse response= requestProcessor.http408requestTimeout(null);
							response.respond(out);
							requestServed=true;
							
						}
						Thread.interrupted();
						
					} 
					catch (Exception e) {
						persistent=false;
						hasRequest = false;
						// If request is not yet served and return Server error
						if(!requestServed)
						{
							HttpWebResponse response= requestProcessor.http500InternalError(null);
							response.respond(out);
							requestServed=true;
						}
						Thread.interrupted();
					}
				}
				// Close output stream and close socket
				out.close();
				socket.close();				
			} catch (IOException e) {
				
				// Log I/o error
				log.info("I/O error in socket");

			} catch (InterruptedException e) {

				// Log interrupted exception
				log.info("Thread Interrupted Abnormally");
				
			}
			 catch (Exception e) {
				 
					log.info("Server Exception "+e.toString());
			 }

		}
	}
}

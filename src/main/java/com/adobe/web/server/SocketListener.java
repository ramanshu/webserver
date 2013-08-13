package com.adobe.web.server;

import java.net.ServerSocket;
import java.net.Socket;
import java.util.Queue;

import org.apache.log4j.Logger;

public class SocketListener extends Thread{
	
	private Queue<Socket> requestQueue;
	private ServerSocket serverSocket;
	private int requestsRecieved;
	private int requestQueueMaxSize;
	static Logger log = Logger.getLogger(SocketListener.class);
	
	SocketListener(Queue<Socket> queue,ServerSocket serverSocket,int requestQueueMaxSize)
	{
		this.requestsRecieved =0;
		this.serverSocket=serverSocket;
		this.requestQueue = queue;		
		this.requestQueueMaxSize =requestQueueMaxSize;
	}
	public void run()
	{
		while(true)
		{
			// Listen for connections over a socket.
			try
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
			catch(Exception e)
			{
				if(serverSocket.isClosed())
					log.info("Server Down");
				return;
			}
		}
	}
	int requestCount()
	{
		return requestsRecieved;
	}
}

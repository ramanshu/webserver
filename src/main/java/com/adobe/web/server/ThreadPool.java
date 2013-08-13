package com.adobe.web.server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.URISyntaxException;
import java.util.LinkedList;
import java.util.Properties;
import java.util.Queue;

import org.apache.log4j.Logger;

/**
 * Serves as a thread pool for the server threads.
 * 
 * @author Ramanshu Mahaur
 *
 */
public class ThreadPool {
	private int threadPoolSize;
	private ServerThread[] serverThreads;
	private SocketListener socketListener;
	private ServerTimeOutThread timeOutThread;
	private Queue<Socket> requestQueue;
	private static Logger log = Logger.getLogger(ThreadPool.class);
	ThreadPool(int poolSize,long requestTimeOut,ServerSocket serverSocket,int requestQueueMaxSize)
	{
		requestQueue = new LinkedList<Socket>();
		initPool(poolSize,requestTimeOut,serverSocket,requestQueueMaxSize);
	}
	
	/**
	 * Set up parameters that can be shared by all request serving threads.
	 * @param readTimeOut
	 * @param keepAlive
	 * @param prop
	 * @throws InstantiationException
	 * @throws IllegalAccessException
	 * @throws URISyntaxException
	 * @throws IOException
	 */
	void setSharedVariables(int readTimeOut, boolean keepAlive, Properties prop) throws InstantiationException, IllegalAccessException, URISyntaxException, IOException
	{
		ActivityLog activityLog  		= new ActivityLog(50);
		ServerThread.readTimeOut 		= readTimeOut;
		ServerThread.keepAlive	 		= keepAlive;
		ServerThread.activityLog 		= activityLog;
		ServerThread.requestQueue		= requestQueue ;
		ServerThread.requestProcessor	= new HttpRequestProcessor(prop,activityLog);
	}
	
	/**
	 * Initialize the pool
	 * @param poolSize
	 * @param requestTimeOut
	 * @param serverSocket
	 * @param requestQueueMaxSize
	 */
	void initPool(int poolSize,long requestTimeOut,ServerSocket serverSocket,int requestQueueMaxSize)
	{
		this.threadPoolSize = poolSize;
		serverThreads= new ServerThread[threadPoolSize];
		for (int i =0 ; i < threadPoolSize;i++)
		{
			serverThreads[i] = new ServerThread(i);
		}
		socketListener = new SocketListener(requestQueue,serverSocket,requestQueueMaxSize);
		timeOutThread =new ServerTimeOutThread(requestTimeOut,serverThreads);
	}
	
	/**
	 * Start all threads in the pool
	 */
	void start()
	{
		for (int i =0 ; i < threadPoolSize;i++)
		{
			serverThreads[i].start();
		}
		timeOutThread.start();
		socketListener.start();			
	}

	/**
	 * Stop all threads in the pool
	 * @param shutDownTime
	 * @throws InterruptedException
	 * @throws IOException 
	 */
	void stop(long shutDownTime) throws InterruptedException, IOException
	{
		Thread.sleep(shutDownTime);
		int totalProcessed = 0;
		socketListener.stop();
		for (int i =0 ; i < threadPoolSize;i++)
		{
			serverThreads[i].stop();
			totalProcessed += serverThreads[i].processCount();
		}
		
		timeOutThread.stop();
		log.info("Requests Recieved: "+socketListener.requestCount()+" Processed: "+totalProcessed);
	}
	int size()
	{
		return serverThreads.length;
	}
}

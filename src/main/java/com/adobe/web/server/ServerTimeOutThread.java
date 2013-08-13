package com.adobe.web.server;

import com.adobe.web.exceptions.RequestTimeOutException;

public class ServerTimeOutThread extends Thread 
{
	private ServerThread[] serverThreads;
	private long requestTimeOut;
	
	ServerTimeOutThread(long requestTimeOut,ServerThread[] serverThreads)
	{
		this.requestTimeOut = requestTimeOut;
		this.serverThreads = serverThreads;
	}
	
	@Override
	public void run ()
	{
		try
		{
			while(true)
			{
				for(int i =0;i<serverThreads.length;i++)
				{	
					// Check if a server thread is taking more time to process a request than specified. 
					if(serverThreads[i].isBusy(requestTimeOut))
					{
						// Send a time out exception to the thread
						serverThreads[i].stop(new RequestTimeOutException());
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

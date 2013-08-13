package com.adobe.web.client;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.net.Socket;
import java.net.UnknownHostException;

import com.adobe.web.protocol.HttpWebRequest;
import com.adobe.web.protocol.HttpWebResponse;

/**
 * To be used as a http web client to generate send and recieve requests and response.
 * 
 * @author Ramanshu Mahaur
 *
 */
public class HttpWebClient {
	
	
	/** Socket object to connect to a server */
	private Socket socket;
	/** Byte array to store requests */
	public byte[] buffer;
	/**
	 * Connect to a given host , port pair.
	 * 
	 * @param host
	 * 			Host to connect to.
	 * @param port
	 * 			Port to connect to.
	 * @throws UnknownHostException
	 * @throws IOException
	 */
	public void openSocket(String host,int port) throws IOException 
	{
		if(socket!=null) socket.close();
		socket = new Socket(host, port);
	}
	public void closeSocket() throws UnknownHostException, IOException
	{
		if(socket!=null) socket.close();
	}
	/**
	 * Make request on the open socket.
	 * @param request
	 * 			request object to be sent over socket.
	 * @throws Exception 
	 */
	public HttpWebResponse makeRequest(HttpWebRequest request) throws Exception 
	{
		InputStream in = socket.getInputStream();
	    PrintStream out =  new PrintStream(new BufferedOutputStream(socket.getOutputStream()));
		request.request(out);
		HttpWebResponse response;
		if(request.method.equalsIgnoreCase("HEAD"))
			response  = new HttpWebResponse(in,false);
		else
			response  = new HttpWebResponse(in,true);
		return response;
	}
	/**
	 * Make a GET request with a given request string
	 * @param uri
	 * 		Request string.
	 * @throws Exception 
	 */
	public HttpWebResponse makeGetRequest(String uri) throws Exception
	{
		HttpWebRequest request= new HttpWebRequest();
		request.method = "GET";
		request.request = uri;
		request.headers.put("Content-Length", "0");
		return makeRequest(request);
	}
	/**
	 * Make a PUT request with given data to be put to specified url.
	 * @param uri
	 * 		Request url.
	 * @param data
	 * 		Post data.
	 * @return 
	 * @throws Exception 
	 */
	public HttpWebResponse makePutRequest(String uri,byte[] data) throws Exception
	{
		HttpWebRequest request= new HttpWebRequest();
		request.method = "PUT";
		request.request = uri;
		request.requestBody = data;
		request.headers.put("Content-Length", ""+request.requestBody.length);
		return makeRequest(request);
	}
	/**
	 * Make a post request to given url.
	 * @param uri
	 * 		Request String
	 * @param data
	 * 		Post data
	 * @return 
	 * @throws Exception 
	 */
	public HttpWebResponse makePostRequest(String uri,byte[] data) throws Exception
	{
		HttpWebRequest request= new HttpWebRequest();
		request.method = "PUT";
		request.request = uri;
		request.requestBody = data;
		request.headers.put("Content-Length", ""+request.requestBody.length);
		request.headers.put("Content-Type", "application/x-www-form-urlencoded");
		return makeRequest(request);
	}
	/**
	 * Make a Head request to given url
	 * @param uri
	 * 			Request string.
	 * @throws Exception 
	 */
	public HttpWebResponse makeHeadRequest(String uri) throws Exception
	{
		HttpWebRequest request= new HttpWebRequest();
		request.method = "HEAD";
		request.request = uri;
		request.headers.put("Content-Length", "0");
		return makeRequest(request);
	}
	/**
	 * Make a DELETE request at given url.
	 * @param uri
	 * 		Request url.
	 * @return 
	 * @throws Exception 
	 */
	public HttpWebResponse makeDeleteRequest(String uri) throws Exception
	{
		HttpWebRequest request= new HttpWebRequest();
		request.method = "DELETE";
		request.request = uri;
		request.headers.put("Content-Length", "0");
		return makeRequest(request);
	}
}

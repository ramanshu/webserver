package com.adobe.web.server;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.net.URLDecoder;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Date;
import java.util.Enumeration;
import java.util.Properties;

import org.apache.log4j.Logger;

import com.adobe.web.exceptions.InvalidRequestException;
import com.adobe.web.exceptions.UnauthorizedException;
import com.adobe.web.protocol.FileObject;
import com.adobe.web.protocol.HttpWebRequest;
import com.adobe.web.protocol.HttpWebResponse;
import com.adobe.web.standards.HttpContentTypes;

/**
 * Takes in a request processes it and generates appropirate response on the basis of server logic
 * @author Ramanshu Mahaur
 *
 */
public class HttpRequestProcessor {
	/** Document root from where the files would be served.*/
	String document_root;
	/** Check if Server was run in admin mode or not*/
	boolean admin_mode;
	/** Logger for this class*/
	static Logger log = Logger.getLogger(HttpRequestProcessor.class);
	/** Activity log for the current server session*/
	ActivityLog activityLog;
	
	private byte[] http404responseTemplate = "<html><title>404 Not Found</title><body><h1>Not Found</h1><h3>The requested URL was not found on this server.</h3><hr/>For any queries contact webmaster<i><a href=\"mailto:mahaur@adobe.com\">ramanshu.mahaur</a></i></body></html>"
			.getBytes();

	/** Set up fields required for request processing*/
	HttpRequestProcessor(Properties properties,ActivityLog activityLog) throws InstantiationException,
			IllegalAccessException, URISyntaxException, IOException {
		this.activityLog = activityLog;
		try{
			document_root = properties.getProperty("documentRoot").trim();
		}
		catch(Exception e)
		{
			document_root = ".";
			
		}
		try{
			admin_mode = properties.getProperty("adminMode").trim().equalsIgnoreCase("true");
		}
		catch(Exception e)
		{
			admin_mode= false;
		}
		File docRoot = new File(document_root);
		if (!docRoot.exists() || !docRoot.isDirectory()) {
			log.error("Document root must be a directory that exists. Not starting...");
			System.exit(1);
		}
	}
	
	/**
	 * Takes in a request and return a corresponding response object
	 * @param request
	 * @return
	 * @throws IOException
	 */
	  
	  HttpWebResponse requestProcessor(HttpWebRequest request)
	 
			throws IOException {
		URIType uriType = URIType.getType(request.url);
		// If server is not in admin mode server is not in admin mode all requests are treated as file requests.
		if(!admin_mode &&  uriType==URIType.ADMIN) uriType = URIType.FILE;
		
		// Branch on the basis of request method type.
		if (request.method.equalsIgnoreCase("GET")) {
			// Handle admin reuqest separately
			if (uriType == URIType.ADMIN && request.url.startsWith("/admin/activity/"))
			{
				String log ="";
					log+=activityLog.getLog();
				HttpWebResponse response = new HttpWebResponse();
				response.responseData=log.getBytes();
				response.responseCode = 200;
				response.headers.add("Content-length: " + response.responseData.length);
				return response;										
			}
			else if (uriType == URIType.ADMIN && request.url.startsWith("/admin/list/"))
			{
				
				{
					String url = (String)request.getFields.get("url");
					if(url!=null)
					{
						File file = new File(document_root + url);
						return processDirectoryListRequest(request,file,url);								
					}
					else 
					{
						return http404Response(request);
					}
					
				}
			}
			else
			{
			// Handle other requests.
			HttpWebResponse response = process(request,uriType);
			return response;
			}
		}
		else if (request.method.equalsIgnoreCase("HEAD")) {

			// Process a request and remove the response data if request type was head.
			HttpWebResponse response = process(request,uriType);
			response.responseData = null;
			return response;
		}
		else if (request.method.equalsIgnoreCase("POST")) 
		{
				if (uriType == URIType.ADMIN && request.url.startsWith("/admin/upload/"))
				{
					return adminFileManager(request,0);
				}
				else if (uriType == URIType.ADMIN && request.url.startsWith("/admin/delete/"))
				{
					return adminFileManager(request, 1);
				}
				else if (uriType == URIType.ADMIN && request.url.startsWith("/admin/list/"))
				{
					
					{
						String url = (String)request.postFields.get("url");
						if(url!=null)
						{
							File file = new File(document_root + url);
							return processDirectoryListRequest(request,file,url);								
						}
						else 
						{
							return http404Response(request);
						}
					}
				}
				else 
				{
					HttpWebResponse response = process(request,uriType);
					return response;
				}			
		}
		else if (request.method.equalsIgnoreCase("PUT")) {
			if(uriType == URIType.ADMIN)
			{
				return http405NotAllowed(request);

			}
			else
			{
				FileOutputStream stream = new FileOutputStream(document_root + request.url);
				stream.write(request.requestBody);
				stream.close();
				HttpWebResponse response = new HttpWebResponse();
				response.responseCode = 201;
				response.headers.add("Content-length: " + 0);
				return response;
			}
		} else if (request.method.equalsIgnoreCase("DELETE")) {
			if(uriType == URIType.ADMIN)
			{
				return http405NotAllowed(request);
			}
			else
			{
				File file=  new File (document_root+request.url);
				 if(file==null ||  !file.delete())
				 {
					 return http404Response(request);
				 }
				 else
				 {
						HttpWebResponse response = new HttpWebResponse();
						response.responseCode = 200;
						response.headers.add("Content-length: " + 0);
						return response;
				 }
			}
		}
		else 
		{
			return http405NotAllowed(request);
		}

	}

	private void postDataHandler(HttpWebRequest request) throws IOException {
		if (request.postFields != null) {
			Enumeration<String> list = request.postFields.keys();
			while (list.hasMoreElements()) {
				String key = list.nextElement();
				Object value = request.postFields.get(key);
				if (value instanceof FileObject) {
					FileOutputStream stream = new FileOutputStream(
							document_root + request.url
									+ ((FileObject) value).fileName);
					stream.write(((FileObject) value).fileData);
					//System.out.println("Writeing File at:" + document_root	+ request.url + ((FileObject) value).fileName);
					stream.close();
				} else {
					//System.out.println("Writeing Value ");
				}
			}
		}

	}
	
	
	/**
	 * Handle requests for adding or removing files. 
	 * @param request
	 * @param action
	 * @return
	 * @throws IOException
	 */
	private HttpWebResponse adminFileManager(HttpWebRequest request, int action)
			throws IOException {
		// Action is 0 for upload and 1 for put.
		try {
			if (action == 0) {
				if (request.postFields != null) {
					String url = (String)request.postFields.get("url");
					String type = (String)request.postFields.get("type");
					if(url!=null && type!=null ){
						if(type.equalsIgnoreCase("file"))
						{
							FileObject file= (FileObject)request.postFields.get("file");
							if(file!=null)
							{
								File path = new File(document_root + URLDecoder.decode(url, "UTF-8") + ((FileObject) file).fileName);
								if(!path.getCanonicalPath().startsWith(document_root))
									throw new UnauthorizedException();
								boolean replacement = path.exists();
								FileOutputStream stream = new FileOutputStream(	document_root + URLDecoder.decode(url, "UTF-8") + ((FileObject) file).fileName);
								stream.write(((FileObject) file).fileData);
								stream.close();

								HttpWebResponse response = new HttpWebResponse();
								if(!replacement)
								{
								response.responseData="Created Successfully".getBytes();
								response.responseCode = 201;
								}
								else
								{
									response.responseData="File replaced Successfully".getBytes();
									response.responseCode = 200;
								}
									response.headers.add("Content-length: " + response.responseData.length);
								return response;						
							}
							else throw new Exception();
						}
						else if(type.equalsIgnoreCase("folder"))
						{
							
							String folder= (String)request.postFields.get("folder");
							File file = new File (	document_root + URLDecoder.decode(url, "UTF-8") + folder);
							if(!file.getCanonicalPath().startsWith(document_root))
								throw new UnauthorizedException();
							if(!file.mkdirs())
								throw new UnauthorizedException();
							boolean replacement = file.exists();
							if(replacement)
							{
								HttpWebResponse response = new HttpWebResponse();
								response.responseData="Created Successfully".getBytes();
								response.responseCode = 201;
								response.headers.add("Content-length: " + response.responseData.length);
								return response;						
							}
							else
							{
								HttpWebResponse response = new HttpWebResponse();
								response.responseData="Already Present".getBytes();
								response.responseCode = 200;
								response.headers.add("Content-length: " + response.responseData.length);
								return response;						
							}
						}
						else 
							throw new InvalidRequestException();
					}
					else throw new InvalidRequestException();
				}
			} 
			else if (action == 1) 
			{
				if (request.postFields != null) 
				{
					String url = (String)request.postFields.get("url");
					if(url!=null)
					{
						
						File file=  new File (document_root+URLDecoder.decode(url, "UTF-8"));
						 if(!file.exists()) 
							 	throw new FileNotFoundException();
						 else if(!file.delete())
						 		throw new UnauthorizedException();
						 else
						 {
								HttpWebResponse response = new HttpWebResponse();
								response.responseData="Deleted Successfully".getBytes();
								response.responseCode = 200;
								response.headers.add("Content-length: " + response.responseData.length);
								return response;						
						 }
						 
					}
					else throw new InvalidRequestException();
				}
			}
			else throw new InvalidRequestException();
		}
		 catch (FileNotFoundException e) 
		 {
			 return http404Response(request);
		 }
		 catch (InvalidRequestException e) 
		 {
			 return http400BadRequest(request);
		 }
		catch (UnauthorizedException e)
		{
			 return http401Unauthorized(request);
			
		}
		catch(Exception e)
		{
			e.printStackTrace();
			return http500InternalError(request);
		}
		return null;
	}
	
	
	/**
	 * Process a request on the basis of its type.
	 * @param request
	 * @param uriType
	 * @return
	 * @throws FileNotFoundException
	 * @throws IOException
	 */
	HttpWebResponse process(HttpWebRequest request,URIType uriType)
			throws FileNotFoundException, IOException {
		if (uriType == URIType.ADMIN || uriType == URIType.FILE || uriType == URIType.DIRECTORY)
			return processStaticFileRequest(request,uriType);
		else
			return processStaticFileRequest(request,uriType);
	}

	/**
	 * Generate a response in case of a request for a directory's file list.
	 * @param request
	 * @param file
	 * @param base
	 * @return
	 * @throws IOException
	 */
	HttpWebResponse processDirectoryListRequest(HttpWebRequest request,File file,String base)
			throws IOException {
		if (!file.exists() || !file.isDirectory()) {
			return http404Response(request);
		} else {
			HttpWebResponse response = new HttpWebResponse();
			response.responseData = fileListTemplate(file, base);
			response.responseCode = 200;
			response.headers.add("Content-type: "
					+ HttpContentTypes.get("html"));
			response.headers.add("Content-length: "
					+ response.responseData.length);
			return response;
		}
	}

	/**
	 * Generate html response for directory list request over specified template
	 * @param directory
	 * @param path
	 * @return
	 */
	byte[] fileListTemplate2(File directory, String path) {
		File[] fileList = directory.listFiles();
		String tempLate = "<!DOCTYPE html><HTML><TITLE>" + path
				+ "</TITLE><BODY><H1>";
		tempLate += directory.getName() + "</H1><HR>";
		tempLate += "<TABLE ><TR><TD>Name</TD><TD>Last Modified</TD><TD>Size</TD></TR>";
		for (int i = 0; i < fileList.length; i++) {
			tempLate += "<TR><TD><A href=\"" + path  + fileList[i].getName()
					+ "\">" + fileList[i].getName() + "</A></TD>\n";
			Date date = new Date();
			date.setTime(fileList[i].lastModified());
			tempLate += "<TD>" + date.toGMTString() + "</TD>";
			tempLate += "<TD>" + fileList[i].length() + " bytes</TD></TR>";
		}
		tempLate += "</TABLE><HR>";
		tempLate += "</BODY></HTML>";

		byte[] temp = tempLate.getBytes();
		return temp;
	}
	/**
	 * Generate html response for directory list request over specified template
	 * @param directory
	 * @param path
	 * @return
	 */
	byte[] fileListTemplate(File directory, String path) {
		File[] fileList = directory.listFiles();
		String parent;		
		if(path.equalsIgnoreCase("/"))			
			parent = "/";
		else if(path.endsWith("/"))
			parent = path.substring(0,path.length()-1).substring(0,path.lastIndexOf('/'));
		else
			parent = path.substring(0,path.lastIndexOf('/'));
		if(parent.equalsIgnoreCase(""))
			parent ="/";
		if(!path.endsWith("/"))
			path +="/";
		String tempLate = "";
		tempLate +="<div class=\"browser-item current-folder\" link=\""+path+"\">\n";
		tempLate +="<i class=\"icon-folder-open\"></i>";
		tempLate +=path;
		tempLate +="<div class=\"pull-right\"><a class=\"btn btn-small view-file\" type=\""+"true"+"\" link=\""+parent +"\"><i class=\"icon-arrow-up\"></i>/..</a></div></div><hr>";
		tempLate +="<div class=\"browser-list\" >";
		for (int i = 0; i < fileList.length; i++) {
			if(fileList[i].isDirectory())
			{
				tempLate +="<div class=\"browser-item\">\n";
				tempLate +="<i class=\"icon-folder-open\"></i>";
				tempLate +=fileList[i].getName();
				tempLate +="<div class=\"pull-right\"><a class=\"btn btn-small delete-file\" type=\""+fileList[i].isDirectory()+"\" link=\""+path  + fileList[i].getName()+"\"><i class=\"icon-trash\"></i></a><a class=\"btn btn-small view-file\" type=\""+fileList[i].isDirectory()+"\" link=\""+path  + fileList[i].getName()+"\"><i class=\"icon-arrow-right\"></i></a></div></div>";
			}else
			{
				tempLate +="<div class=\"browser-item\">\n";
				tempLate +="<i class=\"icon-file\"></i>";
				tempLate +=fileList[i].getName();
				tempLate +="<div class=\"pull-right\"><a class=\"btn\">"+fileList[i].length()+"bytes</a><a class=\"btn btn-small delete-file\" type=\""+fileList[i].isDirectory()+"\" link=\""+path  + fileList[i].getName()+"\"><i class=\"icon-trash\"></i></a><a class=\"btn btn-small view-file\" type=\""+fileList[i].isDirectory()+"\" link=\""+path  + fileList[i].getName()+"\"><i class=\"icon-arrow-right\"></i></a></div></div>";
			}
			}
		tempLate +="</div><hr>";
		tempLate +="<div class=\"browser-item post-file-form\">";
		tempLate +="<form class=\"form-inline\" method=\"post\" action=\"/admin/upload/\" enctype=\"multipart/form-data\">Post a file at current address.    <input type=\"file\" name=\"file\"  /><input type=\"hidden\" name=\"url\" value=\""+path+"\"/><input type=\"hidden\" name=\"type\" value=\""+"file"  +"\"/><input class=\"btn\" type=\"submit\" value=\"Post\"/></form>";
		tempLate +="<form class=\"form-inline\" method=\"post\" action=\"/admin/upload/\">Create a folder at current address.<input type=\"text\" name=\"folder\"/><input type=\"hidden\" name=\"url\" value=\""+path+"\"/><input type=\"hidden\" name=\"type\" value=\""+"folder"+"\"/><input class=\"btn\" type=\"submit\" value=\"Create\"/></form></div>";

		byte[] temp = tempLate.getBytes();
		return temp;
	}

	/**
	 * Serve a file from document root and handle appropriate cases.
	 * @param request
	 * @param uriType
	 * @return
	 * @throws IOException
	 */
	HttpWebResponse processStaticFileRequest(HttpWebRequest request,URIType uriType)
			throws IOException {
		if(admin_mode && request.url.matches("/"))
			return http302Redirect(request, "/admin/index.html");

		File file;
		String base;
		if(uriType == URIType.ADMIN)
		{
			
			try{
				
			if(!request.url.endsWith("/"))
			{
			InputStream is = getClass().getClassLoader().getResourceAsStream(request.url.substring(1));
			ByteArrayOutputStream buffer = new ByteArrayOutputStream();

			int nRead;
			byte[] data = new byte[16384];

			while ((nRead = is.read(data, 0, data.length)) != -1) {
			  buffer.write(data, 0, nRead);
			}

			buffer.flush();

			data = buffer.toByteArray();
			int dot = request.url.lastIndexOf('.');
			String extension = request.url.substring(dot + 1);
			HttpWebResponse response = new HttpWebResponse();
			response.responseCode = 200;
			response.responseData = data;
			response.headers.add("Content-type: "
					+ HttpContentTypes.get(extension));
			response.headers.add("Content-length: "
					+ response.responseData.length);
			return response;
			}
			else 
			return http302Redirect(request, request.url+"index.html");
			}
			catch(Exception e)
			{
				return http404Response(request);
				
			}
		}
		else
		{
			base=  request.url;
			file = new File(document_root + base);
		if (!file.exists()) {

			return http404Response(request);

		} else if (file.isDirectory()) {

			if (request.url.endsWith("/")) {
				String list = request.getFields.get("list");
				if (list != null && list.equalsIgnoreCase("true"))
					return processDirectoryListRequest(request,file,base);
				else
					return http302Redirect(request, request.url + "index.html");
			} else
				return http302Redirect(request, request.url + "/");
		} else {

			Path path = Paths.get(file.getPath());
			int dot = request.url.lastIndexOf('.');
			String extension = request.url.substring(dot + 1);
			HttpWebResponse response = new HttpWebResponse();
			response.responseCode = 200;
			response.responseData = Files.readAllBytes(path);
			response.headers.add("Content-type: "
					+ HttpContentTypes.get(extension));
			response.headers.add("Content-length: "
					+ response.responseData.length);
			return response;
		}
		}
	}
	/**
	 * Generate a 404 response
	 * @param request
	 * @return
	 */

	HttpWebResponse http404Response(HttpWebRequest request) {
		HttpWebResponse response = new HttpWebResponse();
		response.responseCode = 404;
		response.responseData = http404responseTemplate;
		response.headers.add("Content-length: " + response.responseData.length);
		return response;
	}

	/**
	 * Generate a 302 redirect response to specified location
	 * @param request
	 * @param location
	 * @return
	 */
	HttpWebResponse http302Redirect(HttpWebRequest request, String location) {
		HttpWebResponse response = new HttpWebResponse();
		response.responseCode = 302;
		response.responseData = null;
		response.headers.add("Location: " + location);
		return response;
	}
	/**
	 * Generate a 405 not allowed response.
	 * @param request
	 * @return
	 */
	HttpWebResponse http405NotAllowed(HttpWebRequest request) {
	HttpWebResponse response = new HttpWebResponse();
	response.responseCode = 405;
	response.headers.add("Content-length: " + 0);
	return response;
	}
	/**
	 * Generate a 400 bad request response.
	 * @param request
	 * @return
	 */
	HttpWebResponse http400BadRequest(HttpWebRequest request) {
	HttpWebResponse response = new HttpWebResponse();
	response.responseCode = 400;
	response.headers.add("Content-length: " + 0);
	return response;
	}
	/**
	 * Generate a 401 unauthorized response.
	 * @param request
	 * @return
	 */
	HttpWebResponse http401Unauthorized(HttpWebRequest request) {
	HttpWebResponse response = new HttpWebResponse();
	response.responseCode = 401;
	response.headers.add("Content-length: " + 0);
	return response;
	}
	/**
	 * Generate a 408 requestTimeout response.
	 * @param request
	 * @return
	 */
	HttpWebResponse http408requestTimeout(HttpWebRequest request) {
	HttpWebResponse response = new HttpWebResponse();
	response.responseCode = 408;
	response.headers.add("Content-length: " + 0);
	return response;
	}
	/**
	 * Generate a 500 internal error response.
	 * @param request
	 * @return
	 */
	HttpWebResponse http500InternalError(HttpWebRequest request) {
	HttpWebResponse response = new HttpWebResponse();
	response.responseCode = 500;
	response.headers.add("Content-length: " + 0);
	return response;
	}

}

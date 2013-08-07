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

import com.adobe.web.protocol.FileObject;
import com.adobe.web.protocol.HttpWebRequest;
import com.adobe.web.protocol.HttpWebResponse;
import com.adobe.web.standards.HttpContentTypes;

public class HttpRequestProcessor {
	String document_root;
	boolean admin_mode;
	static Logger log = Logger.getLogger(HttpRequestProcessor.class);

	HttpWebServer server;
	final boolean dir_listing_allowed;
	private byte[] http404responseTemplate = "<html><title>404 Not Found</title><body><h1>Not Found</h1><h3>The requested URL was not found on this server.</h3><hr/>For any queries contact webmaster<i><a href=\"mailto:mahaur@adobe.com\">ramanshu.mahaur</a></i></body></html>"
			.getBytes();

	HttpRequestProcessor(Properties prop,HttpWebServer server) throws InstantiationException,
			IllegalAccessException, URISyntaxException, IOException {
		this.server = server;
		
		try{
			document_root = prop.getProperty("documentRoot").trim();
		}
		catch(Exception e)
		{
			document_root = "C:\\Users\\mahaur\\Desktop";
			
		}
		try{
			admin_mode = prop.getProperty("adminMode").trim().equalsIgnoreCase("true");
		}
		catch(Exception e)
		{
			admin_mode= false;
		}
		dir_listing_allowed=true;
		File docRoot = new File(document_root);
		if (!docRoot.exists() || !docRoot.isDirectory()) {
			log.error("Document root must be a directory that exists. Not starting...");
			System.exit(1);
		}
	}

	HttpWebResponse requestProcessorTemp(HttpWebRequest request)
			throws IOException {
		int uriType = URIType.getType(request.url);
		if(!admin_mode &&  uriType==URIType.ADMIN) uriType = URIType.FILE;
		
		if (request.method.equalsIgnoreCase("GET")) {
			if (uriType == URIType.ADMIN && request.url.startsWith("/admin/activity/"))
			{
				String log ="";
				for (int i = server.requestLog.size()-1;i>=0 && i>server.requestLog.size()-50;i--)
				{
					log+=server.requestLog.get(i)+"\n";
				}
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
			HttpWebResponse response = process(request,uriType);
			return response;
			}
		}
		else if (request.method.equalsIgnoreCase("HEAD")) {

			HttpWebResponse response = process(request,uriType);
			response.responseData = null;
			return response;
		}
		else if (request.method.equalsIgnoreCase("POST")) 
		{
				if (uriType == URIType.ADMIN && request.url.startsWith("/admin/upload/"))
				{
					if(adminFileManager(request, 0))
					{
						HttpWebResponse response = new HttpWebResponse();
						response.responseData="Created Successfully".getBytes();
						response.responseCode = 201;
						response.headers.add("Content-length: " + response.responseData.length);
						return response;						
					}
					else 
					return http404Response(request);
				}
				else if (uriType == URIType.ADMIN && request.url.startsWith("/admin/delete/"))
				{
					
					if(adminFileManager(request, 1))
					{
						HttpWebResponse response = new HttpWebResponse();
						response.responseData="Deleted Successfully".getBytes();
						response.responseCode = 201;
						response.headers.add("Content-length: " + response.responseData.length);
						return response;						
					}
					else 
						return http404Response(request);
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

	private boolean adminFileManager(HttpWebRequest request, int action)
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
							//System.out.println(url +" "+ type+" "+request.postFields.size());
							FileObject file= (FileObject)request.postFields.get("file");
							if(file!=null)
							{

								FileOutputStream stream = new FileOutputStream(	document_root + URLDecoder.decode(url, "UTF-8") + ((FileObject) file).fileName);
								stream.write(((FileObject) file).fileData);
								stream.close();
							}
							else throw new Exception();
						}
						else if(type.equalsIgnoreCase("folder"))
						{
							
							String folder= (String)request.postFields.get("folder");
							File file = new File (	document_root + URLDecoder.decode(url, "UTF-8") + folder);
							if(!file.mkdirs())
								throw new Exception();;
						}
						else 
							throw new Exception();

					}
					else throw new Exception();
				}
			} 
			else if (action == 1) 
			{
				if (request.postFields != null) 
				{
					String url = (String)request.postFields.get("url");
					//System.out.print("Hre"+action+" "+request.url+ ' '+(request.postFields != null)+" "+url);
					if(url!=null)
					{
						
						File file=  new File (document_root+URLDecoder.decode(url, "UTF-8"));
						//System.out.println("\n"+file.getAbsolutePath());
						 if(file==null ||  !file.delete())
							 	throw new Exception();
					}
				}
			}
			return true;
		}
		 catch (Exception e) 
		 {
			return false;
		 }
	}

	HttpWebResponse process(HttpWebRequest request,int uriType)
			throws FileNotFoundException, IOException {
		if (uriType == URIType.ADMIN || uriType == URIType.FILE || uriType == URIType.DIRECTORY)
			return processStaticFileRequest(request,uriType);
		else
			return processStaticFileRequest(request,uriType);
	}

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

	HttpWebResponse processStaticFileRequest(HttpWebRequest request,int uriType)
			throws IOException {
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

	HttpWebResponse http404Response(HttpWebRequest request) {
		HttpWebResponse response = new HttpWebResponse();
		response.responseCode = 404;
		response.responseData = http404responseTemplate;
		response.headers.add("Content-length: " + response.responseData.length);
		return response;
	}

	HttpWebResponse http302Redirect(HttpWebRequest request, String location) {
		HttpWebResponse response = new HttpWebResponse();
		response.responseCode = 302;
		response.responseData = null;
		response.headers.add("Location: " + location);
		return response;
	}
	HttpWebResponse http405NotAllowed(HttpWebRequest request) {
	HttpWebResponse response = new HttpWebResponse();
	response.responseCode = 405;
	response.headers.add("Content-length: " + 0);
	return response;
	}

}

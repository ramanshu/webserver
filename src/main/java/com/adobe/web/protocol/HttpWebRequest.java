package com.adobe.web.protocol;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.Dictionary;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.StringTokenizer;
/**
 * HttpWebRequest is the used to represent a standard http request which constists of the request url, headers and body.
 * Additional fields that have been used are the post data and get data.
 * 
 * @author Ramanshu Mahaur
 *
 */

public class HttpWebRequest {
	/** Request Method type*/
	public String method;
	/** Request string*/
	public String request;
	/** Http version*/
	public String version;
	/** request url after extracting get fields*/
	public String url;
	/** Request body content*/
	public byte[] requestBody;
	/** Request headers*/
	public Dictionary<String, String> headers;
	/** Parsed post fields in case of a post request*/
	public Dictionary<String, Object> postFields;
	/** Parsed get Fields.*/
	public Dictionary<String, String> getFields;
	
	
	/**
	 * HttpWebRequest construct which takes in an inputstream as input reads data over it which contain all the fields in the request.
	 * 
	 * 
	 * @param in
	 * 			InputStream to be read.
	 * @throws Exception
	 */
	public HttpWebRequest(InputStream in) throws Exception {
		String line = readLine(in);
		StringTokenizer st = new StringTokenizer(line, " ");
		method = st.nextToken();
		request = st.nextToken();
		version = st.nextToken();
		headers = setHeaders(in);
		getFields = setGetFields(request);
		String contentLength= headers.get("Content-Length");
		int bodySize;
		try
		{
			bodySize= Integer.parseInt(contentLength);
		}
		catch(Exception e)
		{
			bodySize =0;
		}
		if(bodySize>0)
		{	
			ByteArrayOutputStream buffer = new ByteArrayOutputStream();
			int nRead,total=0;
			byte[] data = new byte[16384];

			while (total<bodySize && (nRead = in.read(data, 0, data.length)) != -1 ) 
			{
			  buffer.write(data, 0, nRead);
			  total+=nRead;
			}
			buffer.flush();
			requestBody = buffer.toByteArray();
		}
		if (method.equalsIgnoreCase("POST")) {
			String contentType = headers.get("Content-Type");
			if(contentType!=null)
			{
				postFields = new Hashtable<String,Object>();
				
			}
			if (contentType.startsWith("application/x-www-form-urlencoded")) {
				postFields = setPostFields(new String(requestBody), contentType);

			} 
			else if (contentType.startsWith("multipart/form-data")) {
				
				byte[] boundary = contentType.substring("multipart/form-data; boundary=".length()).getBytes();
				postFields = setPostFields(requestBody, contentType, boundary);
			}
			else 
			{
				postFields = new Hashtable<String,Object>();
			}

		}
		else 
		{
			postFields = new Hashtable<String,Object>();
		}
	}

	/**
	 * HttpWebRequest constructor to set up a simple request object
	 */
	public HttpWebRequest() {
		version = "HTTP/1.1";
		headers = new Hashtable<String, String>();
		requestBody=new byte[0];
	}
	
	

	/**
	 * Send a request to the server over an outputstream, which may be a socket's output stream.
	 * 
	 * @param out
	 * 			Outputstream tosend the request to.
	 * @return
	 * @throws IOException
	 */
	public boolean request(PrintStream out) throws IOException
	{
		out.print(method+" "+request+" "+version+"\r\n");
		if(headers!=null)
		{
			Enumeration<String> list = headers.keys();
			while(list.hasMoreElements())
			{
				String key=list.nextElement();
				out.print(key+": "+headers.get(key)+"\r\n");
			}
		}
		out.print("\r\n");
		out.write(requestBody,0,requestBody.length);
		out.flush();
		return true;		
	}
	/**
	 * Read a single line terminated by /n or /r/n from the inputstream
	 * @param in
	 * 			Inputstream to be read
	 * @return
	 * @throws IOException
	 */
	private String readLine(InputStream in) throws IOException {
		String buffer = "";
		char c;
		while (true) {
			c = (char) in.read();
			if (c == -1 || c == '\n' || c == '\r') {
				if (c == '\r')
					c = (char) in.read();
				return buffer;
			} else
				buffer += c;
		}

	}
	/**
	 * Read headers and parse them accordingly
	 * @param in
	 * @return
	 * @throws IOException
	 */
	Hashtable<String, String> setHeaders(InputStream in) throws IOException
	{
		Hashtable<String, String> headers = new Hashtable<String, String>();
		String line;
		String property;
		while (true) {
			line = readLine(in);
			if (line == null || line.equals(""))
				break;
			StringTokenizer st1 = new StringTokenizer(line, ": ");
			 property = st1.nextToken();
			 headers.put(property, line.substring(property.length() + 2));
		}
		return headers;
	}
	/**
	 * Read request string and extract the get fields from it.
	 * @param request
	 * @return
	 * @throws UnsupportedEncodingException
	 */
	Dictionary<String, String> setGetFields(String request) throws UnsupportedEncodingException {
		Dictionary<String, String> getFields = new Hashtable<String, String>();

		String[] parts = request.split("\\?");
		url = URLDecoder.decode(parts[0], "UTF-8");
		if (parts.length > 1) {

			String getData = parts[1];
			String[] getFieldData = getData.split("&");
			for (int i = 0; i < getFieldData.length; i++) {
				String[] getFieldDataParts = getFieldData[i].split("=");
				if (getFieldDataParts.length == 2)
					getFields.put(URLDecoder.decode(getFieldDataParts[0], "UTF-8"), URLDecoder.decode(getFieldDataParts[1], "UTF-8"));
			}
		}
		return getFields;
	}
	/**
	 * Read request body and extract post fields from it in case of a post request, if contenttype = "application/x-www-form-urlencoded".
	 * @param requestBody
	 * @param contentType
	 * @return
	 */
	Dictionary<String, Object> setPostFields(String requestBody,
			String contentType) {
		Dictionary<String, Object> postFields = new Hashtable<String, Object>();
		if (contentType.startsWith("application/x-www-form-urlencoded")) {
			String postData = requestBody;
			String[] postFieldData = postData.split("&");
			for (int i = 0; i < postFieldData.length; i++) {
				String[] postFieldDataParts = postFieldData[i].split("=");
				if (postFieldDataParts.length == 2)
					postFields.put(postFieldDataParts[0], postFieldDataParts[1]);
			}
		}
		return postFields;
	}

	/**
	 * Read request body and extract post fields from it in case of a post request, if contenttype = "multipart/form-data".
	 * 
	 * @param requestBody
	 * @param contentType
	 * @param boundary
	 * @return
	 * @throws IOException
	 */
	Dictionary<String, Object> setPostFields(byte[] requestBody,
			String contentType, byte[] boundary) throws IOException {
		Dictionary<String, Object> postFields = new Hashtable<String, Object>();
		MIMEData mimeData = new MIMEData(requestBody,boundary);
		for (int i =0 ; i<mimeData.size();i++)
		{
			String fieldName = "";
			boolean isFile = false;
			String fileName = "";
			String fileType = "";
			for (int j=0 ; j<mimeData.data.get(i).first.size();j++)
			{
				String header = mimeData.data.get(i).first.get(j);
				String[] headerParts = header.split(": ");
				if (headerParts.length == 2) {
					if (headerParts[0].equalsIgnoreCase("content-disposition")) {
						String[] headerField = headerParts[1].split("; ");
						for (int l = 0; l < headerField.length; l++) {
							if (headerField[l].startsWith("name")) {
								fieldName = headerField[l].substring(
										"name=\"".length(),
										headerField[l].length() - 1);

							} else if (headerField[l].startsWith("filename")) {
								isFile = true;
								fileName = headerField[l].substring(
										"finename=\"".length(),
										headerField[l].length() - 1);
							}
						}
					} else if (headerParts[0].equalsIgnoreCase("Content-Type")) {
						fileType = headerParts[1];
					}
				}				
			}
			if (isFile) {
				FileObject file = new FileObject();
				file.contentType = fileType;
				file.fileData = mimeData.data.get(i).second;
				file.fileSize = mimeData.data.get(i).second.length;
				file.fileName = fileName;
				postFields.put(fieldName, file);
			} else {
				String value = new String(mimeData.data.get(i).second);
				postFields.put(fieldName, value);
			}

		}
		return postFields;
	}
}

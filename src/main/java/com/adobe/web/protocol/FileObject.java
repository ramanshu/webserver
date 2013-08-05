package com.adobe.web.protocol;
/**
 * FileObject to store the file instances to be used at various places within project.
 * @author Ramanshu Mahaur
 *
 */
public class FileObject {
	
	/** file name for the file stored. */
	public String fileName;
	/** File type to chosen from the standard file type*/
	public String contentType;
	/** File size */
	public int fileSize;
	/** file data. */
	public byte[] fileData;
}

package com.adobe.web.protocol;
import java.io.IOException;
import java.util.ArrayList;

import com.adobe.web.algorithm.KMPSearch;

/**
 * Represents MIME content  
 * An object of this type contains a arraylist of fields which have associated headers and corresponding content
 * @author Ramanshu Mahaur
 *
 */

public class MIMEData {
	ArrayList<Pair<ArrayList<String> , byte[]>> data;

	class Pair<F,S>{
		public F first;
		public S second;
		
		Pair(F f, S s)
		{
			first = f;
			second =s;
			
		}
	}
	
	
	MIMEData(byte[] requestBody, byte[] boundary) throws IOException {
		KMPSearch boundarySearch = new KMPSearch(boundary);
		KMPSearch newLineSearch = new KMPSearch("\r\n".getBytes());
		data = new ArrayList<Pair<ArrayList<String> , byte[]>>();
		int offset = 0;
		// Get positions of boundary in the body
		ArrayList<Integer> position = boundarySearch.searchAllSubString(requestBody,
				offset, requestBody.length);
		for (int i = 0; i < position.size() - 1; i++) {
			
			ArrayList<String> headers = new ArrayList<String>();
			//Get positions of the new line in the body to extract headers for a single field in mime data
			ArrayList<Integer> newLine = newLineSearch.searchAllSubString(requestBody,
					(int) position.get(i) + boundary.length
							+ "\r\n".getBytes().length,
					(int) position.get(i + 1) - "\r\n".getBytes().length);
			int prev = (int) position.get(i) + boundary.length
					+ "\r\n".getBytes().length;
			int k;
			// Extract all headers from the body
			for (k = 0; k < newLine.size() - 1; k++) {
				
				String header = new String(requestBody, prev,
						(int) newLine.get(k) - prev);
				prev = (int) newLine.get(k) + "\r\n".getBytes().length;
				if (header.isEmpty())
					break;
				else
					headers.add(header);
			}
			// Initialize byte array to hold the data for calculated size
			byte[] fieldData = new byte[(int) position.get(i + 1) - 3* ("\r\n".getBytes().length) - (int) newLine.get(k)];
			// Copy the portion of the request body, corresponding to the field to the byte array 
			 System.arraycopy(requestBody,(int) newLine.get(k) + "\r\n".getBytes().length,fieldData,0,(int) position.get(i + 1) - 3* ("\r\n".getBytes().length)	- (int) newLine.get(k));
			 // Add the header fields and byte data to the mime object's arraylist
			 Pair< ArrayList<String> , byte[]> pair = new Pair< ArrayList<String> , byte[]>( headers , fieldData);
			 data.add(pair);
		}
	}
	int size()
	{
		if(data!=null)
		{
			return data.size();
		}
		else
			return 0;		
	}	
}

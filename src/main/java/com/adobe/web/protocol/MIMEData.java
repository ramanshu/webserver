package com.adobe.web.protocol;
import java.io.IOException;
import java.util.ArrayList;

import com.adobe.web.algorithm.KMPSearch;


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
		ArrayList<Integer> position = boundarySearch.searchAllSubString(requestBody,
				offset, requestBody.length);
		for (int i = 0; i < position.size() - 1; i++) {
			ArrayList<String> headers = new ArrayList<String>();
			ArrayList<Integer> newLine = newLineSearch.searchAllSubString(requestBody,
					(int) position.get(i) + boundary.length
							+ "\r\n".getBytes().length,
					(int) position.get(i + 1) - "\r\n".getBytes().length);
			int prev = (int) position.get(i) + boundary.length
					+ "\r\n".getBytes().length;
			int k;
			for (k = 0; k < newLine.size() - 1; k++) {
				String header = new String(requestBody, prev,
						(int) newLine.get(k) - prev);
				prev = (int) newLine.get(k) + "\r\n".getBytes().length;
				if (header.isEmpty())
					break;
				else
					headers.add(header);
			}
			byte[] fieldData = new byte[(int) position.get(i + 1) - 3* ("\r\n".getBytes().length) - (int) newLine.get(k)];
			 System.arraycopy(requestBody,(int) newLine.get(k) + "\r\n".getBytes().length,fieldData,0,(int) position.get(i + 1) - 3* ("\r\n".getBytes().length)	- (int) newLine.get(k));
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

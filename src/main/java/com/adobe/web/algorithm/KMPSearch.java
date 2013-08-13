package com.adobe.web.algorithm;
import java.util.ArrayList;
/**
 * This is a standard Implementation of the Knuth Morris Patt(KMP) pattern search algorithm.
 * It is an linear order search algorithm which requires computation of a failure matrix which can also be computed in linear time.
 * 
 * @author Ramanshu Mahaur
 *
 */
public class KMPSearch {
	/** Search pattern  */
	byte[] pattern;
	/** Failure matrix for the pattern  */
	int[] failure;
	/**
	 * Set up KMPSearch Object for the given pattern
	 * 
	 * @param pattrn
	 *  			Pattern to be searched
	 */
	public KMPSearch(byte[] pattrn)
	{
		pattern = pattrn;
		failure = preProcessPattern(pattrn);
	}
	
	/**
	 * Preprocess the failure matrix for the search pattern.
	 * 
	 * @param ptrn
	 * 				The pattern to be searched.
	 * @return
	 * 		The failure matrix for the pattern to be searched.
	 */
	public int[] preProcessPattern(byte[] ptrn) {
	    int i = 0, j = -1;
	    int ptrnLen = ptrn.length;
	    int[] b = new int[ptrnLen + 1];
	 
	    b[i] = j;
	    while (i < ptrnLen) {           
	            while (j >= 0 && ptrn[i] != ptrn[j]) {
	            j = b[j];
	        }
	        i++;
	        j++;
	        b[i] = j;
	    }
	    return b;
	}	
	/**
	 * Search for the pattern in the byte array provided.
	 * 
	 * @param text
	 * 			Byte Array over which search happens.
	 * @param offset
	 * 			Array offset from which to start the search.
	 * @param limit
	 * 			Array offset until up till which to search the pattern.
	 * @return
	 * 			First index at which the pattern is found in array region bounded by limits.
	 */
    public int searchSubString(byte[] text,int offset,int limit) {
        int i = offset, j = 0;
        // pattern and text lengths
        byte[] ptrn = pattern;
        int ptrnLen = ptrn.length;
        int txtLen = limit;//text.length;
 
        // initialize new array and preprocess the pattern
        int[] b = failure;
 
        while (i < txtLen) {
            while (j >= 0 && text[i] != ptrn[j]) {
                j = b[j];
            }
            i++;
            j++;
 
            // a match is found
            if (j == ptrnLen) {
                return (i-ptrnLen);
                //j = b[j];
            }
        }
        return -1;
    }
	/**
	 * Search for all instances of the pattern in the byte array provided.
	 * 
	 * @param text
	 * 			Byte Array over which search happens.
	 * @param offset
	 * 			Array offset from which to start the search.
	 * @param limit
	 * 			Array offset until up till which to search the pattern.
	 * @return
	 * 			Array list of indices at which the pattern is found in the array region bounded by limits.
	 */
    public ArrayList<Integer> searchAllSubString(byte[] text,int offset,int limit) {
        int i = offset, j = 0;
        ArrayList<Integer> results= new ArrayList<Integer>();
        // pattern and text lengths
        byte[] ptrn = pattern;
        int ptrnLen = ptrn.length;
        int txtLen = limit;//text.length;
 
        // initialize new array and preprocess the pattern
        int[] b = failure;
 
        while (i < txtLen) {
            while (j >= 0 && text[i] != ptrn[j]) {
                j = b[j];
            }
            i++;
            j++;
 
            // a match is found
            if (j == ptrnLen) {
                results.add((i-ptrnLen));
                j = b[j];
            }
        }
        return results;
    }
}

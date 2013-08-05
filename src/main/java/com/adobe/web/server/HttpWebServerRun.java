package com.adobe.web.server;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Properties;

import org.apache.log4j.Logger;//log4j.BasicConfigurator;
import org.apache.log4j.PropertyConfigurator;
public class HttpWebServerRun {
	static Logger log = Logger.getLogger(HttpWebServerRun.class);
	public static void main(String[] args) throws IOException, InterruptedException, InstantiationException, IllegalAccessException, URISyntaxException
	{
		 PropertyConfigurator.configure("log4j.properties");
		 
    	try {
    		Properties prop = new Properties();
    		prop.load(new FileInputStream("config.properties"));
 		    HttpWebServer web = new HttpWebServer(prop);
 			web.Start();
// 
    	} catch (IOException ex) {
    		ex.printStackTrace();
        }		
	}
}

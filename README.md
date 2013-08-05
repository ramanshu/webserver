=== Webserver 1.0 ===
Author: Ramanshu Mahaur
Requires at least: 3.0.1
Tested up to: 3.4
Stable tag: 4.3

Webserver 1.0 is a simple HTTP webserver implementation in java.   


== Description ==

Current version of the Webserver support handling the HTTP rfc specified requests namely GET, POST, HEAD, PUT and DELETE.
 

== Prerequisites ==

Java Runtime Environment(JRE) 7
- The application has been run and found to have to issues with JRE 7.

== Usage ==


To simply run this server 
java -jar webserver-1.0.jar 

The server will look for a config.properties and log.properties in the current directory where the jar is run. You can copy the example config file to use configure the srever 

Example Config file:
###########################################

hostname=localhost
port=80
threadPoolSize=10
requestQueueMaxSize=10000
requestTimeOut=100
readTimeOut=100
documentRoot=C:\\Users\\mahaur\\Desktop
keepAlive=true
adminMode=true

###########################################

Guide to Configuration:
1. threadPoolSize : Number of threads in the thread pool.
2. requestQueueMaxSize : You can specify the maximum number of requests to hold waiting before they can be processed. Once this limit is reached further requests will be dropped.
3. documentRoot: this is the document root from where the files would be served. Ideally this should be set separate from the applicaiton folder.
4. port : Port to start the server at.
5. hostname : Name of the host. Server can be configured to use this field to mark the "Host" header in the server responses.
6. requestTimeOut : Maximum time allowed for the server to process a single request. If it takes more time than that the request is dropped. 
7. readTimeOut : Maximum time allowed to wait for the client to write on the socket stream.
8. keepAlive : Set it to be true if you want server's default behaviour to the incoming Connections to "keep-alive". The server will still close the connection if the client requests to close connection via "Connection: close".
9. adminMode : Set it to be true if you want to allow any users to manage/add the files on the server.


There are two modes in which you can run this server:
1. Admin Mode
2. General Mode

General Mode:
In this mode the server acts as static-file server. No changes can be made to the files at server in this mode. GET and HEAD requests work in the expected way. 
POST requests are processed but the post data accompanied is not request and it return it as a get request. PUT and DELETE are not supported in this mode. This is the default mode with which the server runs.

Admin Mode:
The admin mode allows you to view, add and delete files in the working document directory. The admin section also allows you to see admin section which contains the source files, documentaion and other project related information.
You can POST files to '/admin/upload/' with folder location as 'url' fields and file as 'file' field in the post data. 
You can delete files by a POST request to '/admin/delete/' with file location as 'url' in the post data. 
General mode works as normal in this mode.

== Dependencies ==

The Webserver code uses log4j for its logging requirements. The used version is log4j-1.2.17. 
The required dependencies have been packages within the jar file shipped, so they do not need to be installed along with.

== Sources ==

The server has been built upon the Java socket implementation. The protocol standards have been adopted as per specified in the standard RFC documentation.
Standard code for KMP has been adapted for  for byte search from an implementation  available at http://tekmarathon.wordpress.com/2013/05/14/algorithm-to-find-substring-in-a-string-kmp-algorithm/.
Maven was been used for project management and packaging.
All the classes have been authored by Ramanshu Mahaur. 


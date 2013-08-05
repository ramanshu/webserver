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
To simply run this server with default configuration use 

java -jar webserver-1.0.jar 

Configuration:
The server can be configured to run with varying server parameters that are:
1. threadPoolSize : Number of threads in the thread pool.
2. requestQueueMaxSize : You can specify the maximum number of requests to hold waiting before they can be processed. Once this limit is reached further requests will be dropped.
3. documentRoot: this is the document root from where the files would be served. Ideally this should be set separate from the applicaiton folder.
4. port : Port to start the server at.
5. host : Name of the host. Server can be configured to use this field to mark the "Host" header in the server responses.
6. requestTimeOut : Maximum time allowed for the server to process a single request. If it takes more time than that the request is dropped. 

You need to setup basic configuration in the 'config.properties' file and put it in the same working directory to allow application to automatically read these settings ,
else you can provide the path to the config file as  parameter config=path/to/configurationfile/configfilename.
Example Config file:
###########################################
#Mon Jan 11 18:54:40 MYT 2010

hostname=localhost
port=8000
threadPoolSize=10
requestQueueMaxSize=10
requestTimeOut=1000
documentRoot=C:\\Users\\mahaur\\Desktop
###########################################

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


== Frequently Asked Questions ==

= A question that someone might have =

An answer to that question.

= What about foo bar? =

Answer to foo bar dilemma.

== Screenshots ==

1. This screen shot description corresponds to screenshot-1.(png|jpg|jpeg|gif). Note that the screenshot is taken from
the /assets directory or the directory that contains the stable readme.txt (tags or trunk). Screenshots in the /assets 
directory take precedence. For example, `/assets/screenshot-1.png` would win over `/tags/4.3/screenshot-1.png` 
(or jpg, jpeg, gif).
2. This is the second screen shot

== Changelog ==

= 1.0 =
* A change since the previous version.
* Another change.

= 0.5 =
* List versions from most recent at top to oldest at bottom.

== Upgrade Notice ==

= 1.0 =
Upgrade notices describe the reason a user should upgrade.  No more than 300 characters.

= 0.5 =
This version fixes a security related bug.  Upgrade immediately.

== Arbitrary section ==

You may provide arbitrary sections, in the same format as the ones above.  This may be of use for extremely complicated
plugins where more information needs to be conveyed that doesn't fit into the categories of "description" or
"installation."  Arbitrary sections will be shown below the built-in sections outlined above.

== A brief Markdown Example ==

Ordered list:

1. Some feature
1. Another feature
1. Something else about the plugin

Unordered list:

* something
* something else
* third thing

Here's a link to [WordPress](http://wordpress.org/ "Your favorite software") and one to [Markdown's Syntax Documentation][markdown syntax].
Titles are optional, naturally.

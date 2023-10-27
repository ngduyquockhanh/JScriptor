# JScriptor
Pre-Script and Post-Script like Postman extension for Burpsuite

# Usage
With extension, you can import JavaScript code and it will be run like Pre-script and Post-script in Postman. You can see output of pre-script/post-script in output of extension.
For detailed usage please go to [Wiki](https://github.com/ngduyquockhanh/JScriptor/wiki)
 
## Global Object
- **jsrequest**
  
| Modifier and Type | Method | Description | 
| - | - | - |
| String  | bodyToString()  | Body of a message as a String.  | 
| ContentType  | contentType()  |   | 
| List<HttpHeader>  | headers()  | HTTP headers contained in the message.  | 
| HttpService  | httpService()  | HTTP service for the request.  | 
| String  | httpVersion()  | HTTP Version text parsed from the request line for HTTP 1 messages.  | 
| String  | method()  | HTTP method for the request.  | 
| String  | path()  | Path and File for the request.  |  
| String  | toString()  | Message as a String.  | 
| String  | url()  | URL for the request.  | 
| HttpRequest  | withAddedHeader(String name, String value)  | Create a copy of the HttpRequest with the added header.  | 
| HttpRequest  | withBody(String body)  | Create a copy of the HttpRequest with the updated body. Updates Content-Length header.  | 
| HttpRequest  | withHeader(String name, String value)  | Create a copy of the HttpRequest with the added or updated header. If the header exists in the request, it is updated. If the header doesn't exist in the request, it is added.  | 
| HttpRequest  | withMethod(String method)  | Create a copy of the HttpRequest with the new method.  | 
| HttpRequest  | withPath(String path)  | Create a copy of the HttpRequest with the new path.  | 
| HttpRequest  | withRemovedHeader(String name)  | Removes an existing HTTP header from the current request.  | 
| HttpRequest  | withUpdatedHeader(String name, String value)  | Create a copy of the HttpRequest with the updated header.  | 


- **jsresponse**
  
| Modifier and Type | Method | Description | 
| - | - | - |
| String  | bodyToString()  | Body of a message as a String.  | 
| List<Cookie>  | cookies()  | Obtain details of the HTTP cookies set in the response.  | 
| List<HttpHeader>  | headers()  | HTTP headers contained in the message.  |  
| String  | httpVersion()  | Return the HTTP Version text parsed from the response line for HTTP 1 messages.  | 
| String  | reasonPhrase()  | Obtain the HTTP reason phrase contained in the response for HTTP 1 messages.  | 
| short  | statusCode()  | Obtain the HTTP status code contained in the response.  | 
| String  | toString()  | Message as a String.  | 
| HttpResponse  | withAddedHeader(String name, String value)  | Create a copy of the HttpResponse with the added header.  | 
| HttpResponse  | withBody(String body)  | Create a copy of the HttpResponse with the updated body. Updates Content-Length header.  | 
| HttpResponse  | withHttpVersion(String httpVersion)  | Create a copy of the HttpResponse with the new http version.  |  
| HttpResponse  | withReasonPhrase(String reasonPhrase)  | Create a copy of the HttpResponse with the new reason phrase.  | 
| HttpResponse  | withRemovedHeader(String name)  | Create a copy of the HttpResponse with the removed header.  | 
| HttpResponse  | withStatusCode(short statusCode)  | Create a copy of the HttpResponse with the provided status code.  | 
| HttpResponse  | withUpdatedHeader(String name, String value)  | Create a copy of the HttpResponse with the updated header.  | 

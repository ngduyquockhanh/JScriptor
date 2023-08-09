# JScriptor
Pre-Script and Post-Script like Postman extension for Burpsuite

# Usage
## Pre-script
- End of script is a **HttpRequest** object
- Example:
```JS
jsrequest.withHeader("Hash":"testHeader");
```

## Post-script
- End of script is a **HttpResponse** object
- Example:
```JS
jsresponse.withStatusCode(200);
```

## Setting Library
- User can import JavaScript Library to extension in Setting Library Tab
- Example: Add CryptoJS to Library
  - Download file cryptojs library: https://cdnjs.cloudflare.com/ajax/libs/crypto-js/4.1.1/crypto-js.min.js
  - Import to extension
  - Use it
```JS
var hash = CryptoJS.SHA256("Message");
var hashHeader = hash.toString(CryptoJS.enc.Base64);
jsrequest.withHeader("Hash", hashHeader);
```

## Global Object
- **jsrequest**
  
| Modifier and Type | Method | Description | 
| - | - | - |
| ByteArray  | body()  | Body of a message as a byte array.  | 
| int  | bodyOffset()  | Offset within the message where the message body begins.  | 
| String  | bodyToString()  | Body of a message as a String.  | 
| ContentType  | contentType()  |   | 
| List<HttpHeader>  | headers()  | HTTP headers contained in the message.  | 
| HttpService  | httpService()  | HTTP service for the request.  | 
| String  | httpVersion()  | HTTP Version text parsed from the request line for HTTP 1 messages.  | 
| List<Marker>  | markers()  | Markers for the message.  | 
| String  | method()  | HTTP method for the request.  | 
| List<ParsedHttpParameter>  | parameters()  |   | 
| String  | path()  | Path and File for the request.  | 
| ByteArray  | toByteArray()  | Message as a byte array.  | 
| String  | toString()  | Message as a String.  | 
| String  | url()  | URL for the request.  | 
| HttpRequest  | withAddedHeader(String name, String value)  | Create a copy of the HttpRequest with the added header.  | 
| HttpRequest  | withAddedParameters(HttpParameter... parameters)  | Create a copy of the HttpRequest with the added HTTP parameters.  | 
| HttpRequest  | withAddedParameters(List\<HttpParameter\> parameters)  | Create a copy of the HttpRequest with the added HTTP parameters.  | 
| HttpRequest  | withBody(ByteArray body)  | Create a copy of the HttpRequest with the updated body. Updates Content-Length header.  | 
| HttpRequest  | withBody(String body)  | Create a copy of the HttpRequest with the updated body. Updates Content-Length header.  | 
| HttpRequest  | withDefaultHeaders()  | Create a copy of the HttpRequest with added default headers.  | 
| HttpRequest  | withHeader(String name, String value)  | Create a copy of the HttpRequest with the added or updated header. If the header exists in the request, it is updated. If the header doesn't exist in the request, it is added.  | 
| HttpRequest  | withMethod(String method)  | Create a copy of the HttpRequest with the new method.  | 
| HttpRequest  | withParameter(HttpParameter parameters)  | Create a copy of the HttpRequest with the HTTP parameter. If the parameter exists in the request, it is updated. If the parameter doesn't exist in the request, it is added.  | 
| HttpRequest  | withPath(String path)  | Create a copy of the HttpRequest with the new path.  | 
| HttpRequest  | withRemovedHeader(String name)  | Removes an existing HTTP header from the current request.  | 
| HttpRequest  | withRemovedParameters(HttpParameter... parameters)  | Create a copy of the HttpRequest with the removed HTTP parameters.  | 
| HttpRequest  | withRemovedParameters(List\<HttpParameter\> parameters)  | Create a copy of the HttpRequest with the removed HTTP parameters.  | 
| HttpRequest  | withUpdatedHeader(String name, String value)  | Create a copy of the HttpRequest with the updated header.  | 
| HttpRequest  | withUpdatedParameters(HttpParameter... parameters)  | Create a copy of the HttpRequest with the updated HTTP parameters.  | 
| HttpRequest  | withUpdatedParameters(List\<HttpParameter\> parameters)  | Create a copy of the HttpRequest with the updated HTTP parameters.  | 


- **jsresponse**
  
| Modifier and Type | Method | Description | 
| - | - | - |
| List<Attribute>  | attributes(AttributeType... types)  | Retrieve the values of response attributes.  | 
| ByteArray  | body()  | Body of a message as a byte array.  | 
| int  | bodyOffset()  | Offset within the message where the message body begins.  | 
| String  | bodyToString()  | Body of a message as a String.  | 
| List<Cookie>  | cookies()  | Obtain details of the HTTP cookies set in the response.  | 
| HttpResponse  | copyToTempFile()  | Create a copy of the HttpResponse in temporary file. This method is used to save the HttpResponse object to a temporary file, so that it is no longer held in memory.  | 
| List<HttpHeader>  | headers()  | HTTP headers contained in the message.  | 
| static HttpResponse  | httpResponse()  | Create a new empty instance of HttpResponse.  | 
| static HttpResponse  | httpResponse(ByteArray response)  | Create a new instance of HttpResponse.  | 
| static HttpResponse  | httpResponse(String response)  | Create a new instance of HttpResponse.  | 
| String  | httpVersion()  | Return the HTTP Version text parsed from the response line for HTTP 1 messages.  | 
| MimeType  | inferredMimeType()  | Obtain the MIME type of the response, as inferred from the contents of the HTTP message body.  | 
| List<KeywordCount>  | keywordCounts(String... keywords)  | Retrieve the number of types given keywords appear in the response.  | 
| List<Marker>  | markers()  | Markers for the message.  | 
| String  | reasonPhrase()  | Obtain the HTTP reason phrase contained in the response for HTTP 1 messages.  | 
| MimeType  | statedMimeType()  | Obtain the MIME type of the response, as stated in the HTTP headers.  | 
| short  | statusCode()  | Obtain the HTTP status code contained in the response.  | 
| ByteArray  | toByteArray()  | Message as a byte array.  | 
| String  | toString()  | Message as a String.  | 
| HttpResponse  | withAddedHeader(String name, String value)  | Create a copy of the HttpResponse with the added header.  | 
| HttpResponse  | withBody(ByteArray body)  | Create a copy of the HttpResponse with the updated body. Updates Content-Length header.  | 
| HttpResponse  | withBody(String body)  | Create a copy of the HttpResponse with the updated body. Updates Content-Length header.  | 
| HttpResponse  | withHttpVersion(String httpVersion)  | Create a copy of the HttpResponse with the new http version.  |  
| HttpResponse  | withReasonPhrase(String reasonPhrase)  | Create a copy of the HttpResponse with the new reason phrase.  | 
| HttpResponse  | withRemovedHeader(String name)  | Create a copy of the HttpResponse with the removed header.  | 
| HttpResponse  | withStatusCode(short statusCode)  | Create a copy of the HttpResponse with the provided status code.  | 
| HttpResponse  | withUpdatedHeader(String name, String value)  | Create a copy of the HttpResponse with the updated header.  | 

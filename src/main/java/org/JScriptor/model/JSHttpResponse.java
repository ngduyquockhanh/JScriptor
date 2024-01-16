package org.JScriptor.model;

import burp.api.montoya.http.message.HttpHeader;
import burp.api.montoya.http.message.responses.HttpResponse;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;


public class JSHttpResponse {
    private String body;
    private Map<String, String> headers;
    private String httpVersion;
    private String mimeType;
    private String reasonPhrase;
    private short statusCode;

    public JSHttpResponse() {
    }

    public void loadValue(HttpResponse httpResponse){
        this.body = httpResponse.bodyToString();
        this.headers = new HashMap<>();
        for (HttpHeader httpHeader: httpResponse.headers()){
            this.headers.put(httpHeader.name(), httpHeader.value());
        }
        this.httpVersion = httpResponse.httpVersion();
        this.mimeType = httpResponse.mimeType().toString();
        this.reasonPhrase = httpResponse.reasonPhrase();
        this.statusCode = httpResponse.statusCode();
    }

    public HttpResponse getHttpResponse(HttpResponse httpResponse){
        HttpResponse modifiedResponse = httpResponse;
        Set<String> headerNames = getHeaders().keySet();

        for (String headerName : headerNames){
            HttpHeader httpHeader = HttpHeader.httpHeader(headerName, getHeaders().get(headerName));
            if (httpResponse.hasHeader(headerName)){
                modifiedResponse = modifiedResponse.withUpdatedHeader(httpHeader);
            }else{
                modifiedResponse = modifiedResponse.withAddedHeader(httpHeader);
            }
        }
        modifiedResponse = modifiedResponse.withBody(getBody());
        modifiedResponse = modifiedResponse.withHttpVersion(getHttpVersion());
        modifiedResponse = modifiedResponse.withReasonPhrase(getReasonPhrase());
        modifiedResponse = modifiedResponse.withStatusCode(getStatusCode());

        return modifiedResponse;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public Map<String, String> getHeaders() {
        return headers;
    }

    public void setHeaders(Map<String, String> headers) {
        this.headers = headers;
    }

    public String getHttpVersion() {
        return httpVersion;
    }

    public void setHttpVersion(String httpVersion) {
        this.httpVersion = httpVersion;
    }

    public String getMimeType() {
        return mimeType;
    }

    public void setMimeType(String mimeType) {
        this.mimeType = mimeType;
    }

    public String getReasonPhrase() {
        return reasonPhrase;
    }

    public void setReasonPhrase(String reasonPhrase) {
        this.reasonPhrase = reasonPhrase;
    }

    public short getStatusCode() {
        return statusCode;
    }

    public void setStatusCode(short statusCode) {
        this.statusCode = statusCode;
    }

    @Override
    public String toString() {
        return "JSHttpResponse{" +
                "body='" + body + '\'' +
                ", headers=" + headers +
                ", httpVersion='" + httpVersion + '\'' +
                ", mimeType='" + mimeType + '\'' +
                ", reasonPhrase='" + reasonPhrase + '\'' +
                ", statusCode=" + statusCode +
                '}';
    }
}

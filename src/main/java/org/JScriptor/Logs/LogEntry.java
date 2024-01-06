package org.JScriptor.Logs;

import burp.api.montoya.http.message.requests.HttpRequest;
import burp.api.montoya.http.message.responses.HttpResponse;
import java.net.MalformedURLException;
import java.net.URL;

/* loaded from: AutoPencrypt.jar:extension/Logs/LogEntry.class */
public class LogEntry {

    private int id;
    private long requestResponseId;
    private HttpRequest originalHttpRequest;
    private HttpResponse originalHttpResponse;
    private HttpRequest modifiedHttpRequest;
    private HttpResponse modifiedHttpResponse;

    public LogEntry() {
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public long getRequestResponseId() {
        return requestResponseId;
    }

    public void setRequestResponseId(long requestResponseId) {
        this.requestResponseId = requestResponseId;
    }

    public HttpRequest getOriginalHttpRequest() {
        return originalHttpRequest;
    }

    public void setOriginalHttpRequest(HttpRequest originalHttpRequest) {
        this.originalHttpRequest = originalHttpRequest;
    }

    public HttpResponse getOriginalHttpResponse() {
        return originalHttpResponse;
    }

    public void setOriginalHttpResponse(HttpResponse originalHttpResponse) {
        this.originalHttpResponse = originalHttpResponse;
    }

    public HttpRequest getModifiedHttpRequest() {
        return modifiedHttpRequest;
    }

    public void setModifiedHttpRequest(HttpRequest modifiedHttpRequest) {
        this.modifiedHttpRequest = modifiedHttpRequest;
    }

    public HttpResponse getModifiedHttpResponse() {
        return modifiedHttpResponse;
    }

    public void setModifiedHttpResponse(HttpResponse modifiedHttpResponse) {
        this.modifiedHttpResponse = modifiedHttpResponse;
    }
}
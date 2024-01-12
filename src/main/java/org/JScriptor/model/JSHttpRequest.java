package org.JScriptor.model;

import burp.api.montoya.http.HttpService;
import burp.api.montoya.http.message.HttpHeader;
import burp.api.montoya.http.message.params.HttpParameter;
import burp.api.montoya.http.message.params.HttpParameterType;
import burp.api.montoya.http.message.params.ParsedHttpParameter;
import burp.api.montoya.http.message.requests.HttpRequest;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class JSHttpRequest {
    private String body;
    private Map<String, String> headers;
    private JSHttpService httpService;
    private String httpVersion;
    private String method;
    private Map<String, String> parameters;
    private Map<String, String> queries;
    private String path;
    private String pathWithoutQuery;
    private String url;


    public JSHttpRequest() {
    }

    public void loadValue(HttpRequest httpRequest){
        this.body = httpRequest.bodyToString();
        this.headers = new HashMap<>();
        for (HttpHeader httpHeader : httpRequest.headers()){
            this.headers.put(httpHeader.name(), httpHeader.value());
        }
        this.httpService = new JSHttpService(httpRequest.httpService().host(), httpRequest.httpService().port(),
                httpRequest.httpService().secure());
        this.httpVersion = httpRequest.httpVersion();
        this.method = httpRequest.method();
        this.parameters = new HashMap<>();
        this.queries = new HashMap<>();
        for (ParsedHttpParameter parsedHttpParameter: httpRequest.parameters()){
            if ((parsedHttpParameter.type().toString().equals("BODY"))){
                this.parameters.put(parsedHttpParameter.name(), parsedHttpParameter.value());
            }
            if ((parsedHttpParameter.type().toString().equals("URL"))){
                this.queries.put(parsedHttpParameter.name(), parsedHttpParameter.value());
            }
        }
        this.path = httpRequest.path();
        this.pathWithoutQuery = httpRequest.pathWithoutQuery();
        this.url = httpRequest.url();

    }

    public HttpRequest getHttpRequest(HttpRequest httpRequest) {
        HttpRequest modifiedHttpRequest = httpRequest;
        Set<String> headerNameList = getHeaders().keySet();
        for (String headerName: headerNameList){
            HttpHeader httpHeader = HttpHeader.httpHeader(headerName, getHeaders().get(headerName));
            modifiedHttpRequest = modifiedHttpRequest.withHeader(httpHeader);
        }
        if (!httpRequest.path().equals(getPath())){
            modifiedHttpRequest = modifiedHttpRequest.withPath(getPath());
        }
        Set<String> bodyParamList = getParameters().keySet();
        for (String bodyParam: bodyParamList){
            HttpParameterType httpParameterType = HttpParameterType.valueOf("BODY");
            HttpParameter httpParameter = HttpParameter.parameter(bodyParam, getParameters().get(bodyParam), httpParameterType);
            modifiedHttpRequest = modifiedHttpRequest.withParameter(httpParameter);
        }
        Set<String> queryParamList = getQueries().keySet();
        for (String queryParam: queryParamList){
            HttpParameterType httpParameterType = HttpParameterType.valueOf("URL");
            HttpParameter httpParameter = HttpParameter.parameter(queryParam, getQueries().get(queryParam), httpParameterType);
            modifiedHttpRequest = modifiedHttpRequest.withParameter(httpParameter);
        }

        if(!httpRequest.method().equals(getMethod())){
            modifiedHttpRequest = modifiedHttpRequest.withMethod(getMethod());
        }
        if (!httpRequest.bodyToString().equals(getBody()))
            modifiedHttpRequest = modifiedHttpRequest.withBody(getBody());

        HttpService httpService1 = HttpService.httpService(getHttpService().getHost(),
                getHttpService().getPort(), getHttpService().isSecure());
        modifiedHttpRequest = modifiedHttpRequest.withService(httpService1);

        return modifiedHttpRequest;
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

    public Map<String, String> getParameters() {
        return parameters;
    }

    public void setParameters(Map<String, String> parameters) {
        this.parameters = parameters;
    }

    public Map<String, String> getQueries() {
        return queries;
    }

    public void setQueries(Map<String, String> queries) {
        this.queries = queries;
    }

    public JSHttpService getHttpService() {
        return httpService;
    }

    public void setHttpService(JSHttpService httpService) {
        this.httpService = httpService;
    }

    public String getHttpVersion() {
        return httpVersion;
    }

    public void setHttpVersion(String httpVersion) {
        this.httpVersion = httpVersion;
    }

    public String getMethod() {
        return method;
    }

    public void setMethod(String method) {
        this.method = method;
    }



    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getPathWithoutQuery() {
        return pathWithoutQuery;
    }

    public void setPathWithoutQuery(String pathWithoutQuery) {
        this.pathWithoutQuery = pathWithoutQuery;
    }


    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    @Override
    public String toString() {
        return "JSHttpRequest{" +
                "bodyToString='" + body + '\'' +
                ", headers=" + headers +
                ", httpService=" + httpService +
                ", httpVersion='" + httpVersion + '\'' +
                ", method='" + method + '\'' +
                ", parameters=" + parameters +
                ", path='" + path + '\'' +
                ", pathWithoutQuery='" + pathWithoutQuery + '\'' +
                ", url='" + url + '\'' +
                '}';
    }
}

package org.JScriptor.Helper;

import burp.api.montoya.MontoyaApi;
import burp.api.montoya.http.handler.HttpRequestToBeSent;
import burp.api.montoya.http.handler.HttpResponseReceived;
import burp.api.montoya.http.message.requests.HttpRequest;
import burp.api.montoya.http.message.responses.HttpResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.JScriptor.Logs.LogEntry;
import org.JScriptor.UI.JScriptorPanel;
import org.JScriptor.model.JSHttpRequest;
import org.JScriptor.model.JSHttpResponse;
import org.JScriptor.model.JSLogger;
import org.JScriptor.model.JSVariableList;
import org.graalvm.polyglot.Context;
import org.graalvm.polyglot.HostAccess;
import org.graalvm.polyglot.Source;
import org.graalvm.polyglot.Value;

import javax.swing.*;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class GraalJSEngine {
    private MontoyaApi montoyaApi;
    private JScriptorPanel jScriptorPanel;
    private String pre_script;
    private String post_script;

    public GraalJSEngine(MontoyaApi montoyaApi, JScriptorPanel jScriptorPanel, String pre_script, String post_script) {
        this.montoyaApi = montoyaApi;
        this.jScriptorPanel = jScriptorPanel;
        this.pre_script = pre_script;
        this.post_script = post_script;
    }

    public HttpRequest handleRequest(HttpRequestToBeSent requestToBeSent){
        try {
            JSHttpRequest jsHttpRequest = new JSHttpRequest();
            jsHttpRequest.loadValue(requestToBeSent);
            ObjectMapper objectMapper = new ObjectMapper();
            String jsRequestString = objectMapper.writeValueAsString(jsHttpRequest);

            JSVariableList variableList = new JSVariableList();
            variableList.loadValue(this.montoyaApi);
            String jsVariableString = objectMapper.writeValueAsString(variableList);

            JSLogger jsLogger = new JSLogger();
            String jsLoggerString = objectMapper.writeValueAsString(jsLogger);

            Map<String, String> nodejsOptions = optionsNodejs();
            Context context = Context.newBuilder("js")
                    .allowExperimentalOptions(true)
                    .allowIO(true)
                    .allowHostAccess(HostAccess.ALL)
                    .allowAllAccess(true)
                    .options(nodejsOptions)
                    .build();

            context.getBindings("js").putMember("jsvariables", jsVariableString);
            context.getBindings("js").putMember("jsrequest", jsRequestString);
            context.getBindings("js").putMember("jslogger", jsLoggerString);
            context.eval("js", "jsvariables = JSON.parse(jsvariables)");
            context.eval("js", "jsrequest = JSON.parse(jsrequest)");
            context.eval("js", "jslogger = JSON.parse(jslogger)");
            context.eval("js", "jsresult = {request:null}");
            context.eval("js", "window = {}");
            context = loadLibrary(context);

            context.eval("js", pre_script);

            context.eval("js", "jslogger = JSON.stringify(jslogger);");
            context.eval("js", "jsvariables = JSON.stringify(jsvariables);");

            String jsVariableJSON = context.getBindings("js").getMember("jsvariables").asString();
            variableList = objectMapper.readValue(jsVariableJSON, JSVariableList.class);
            variableList.saveVariable(this.montoyaApi);

            String jsLoggerJSON = context.getBindings("js").getMember("jslogger").asString();
            jsLogger = objectMapper.readValue(jsLoggerJSON, JSLogger.class);
            jsLogger.printLogs(this.montoyaApi);

            try{

                context.eval("js", "jsresult.request = JSON.stringify(jsresult.request);");

                Value jsResult = context.getBindings("js").getMember("jsresult");
                String jsRequestJSON = jsResult.getMember("request").asString();
                jsHttpRequest = objectMapper.readValue(jsRequestJSON, JSHttpRequest.class);
                HttpRequest modifiedRequest = jsHttpRequest.getHttpRequest(requestToBeSent);

                if (this.jScriptorPanel.getSaveLogButton().isSelected()){
                    this.updateLogTable(requestToBeSent.messageId(), null, null,
                            modifiedRequest, null);
                }

                return modifiedRequest;
            }catch (Exception e){
                this.montoyaApi.logging().logToError(e.getMessage());
                return requestToBeSent;
            }

        } catch (Exception e) {
            this.montoyaApi.logging().logToError(e.getMessage());
            return requestToBeSent;
        }
    }

    public HttpResponse handleResponse(HttpResponseReceived responseReceived){
        try {
            JSHttpResponse jsHttpResponse = new JSHttpResponse();
            jsHttpResponse.loadValue(responseReceived);
            ObjectMapper objectMapper = new ObjectMapper();
            String jsResponseString = objectMapper.writeValueAsString(jsHttpResponse);

            JSVariableList variableList = new JSVariableList();
            variableList.loadValue(this.montoyaApi);
            String jsVariableString = objectMapper.writeValueAsString(variableList);

            JSLogger jsLogger = new JSLogger();
            String jsLoggerString = objectMapper.writeValueAsString(jsLogger);

            Map<String, String> nodejsOptions = optionsNodejs();
            Context context = Context.newBuilder("js")
                    .allowExperimentalOptions(true)
                    .allowIO(true)
                    .allowHostAccess(HostAccess.ALL)
                    .allowAllAccess(true)
                    .options(nodejsOptions)
                    .build();

            context.getBindings("js").putMember("jsvariables", jsVariableString);
            context.getBindings("js").putMember("jsresponse", jsResponseString);
            context.getBindings("js").putMember("jslogger", jsLoggerString);
            context.eval("js", "jsvariables = JSON.parse(jsvariables)");
            context.eval("js", "jsresponse = JSON.parse(jsresponse)");
            context.eval("js", "jslogger = JSON.parse(jslogger)");
            context.eval("js", "jsresult = {response:null}");
            context.eval("js", "window = {}");
            context = loadLibrary(context);

            context.eval("js", post_script);

            context.eval("js", "jsvariables = JSON.stringify(jsvariables);");
            context.eval("js", "jslogger = JSON.stringify(jslogger);");

            String jsVariableJSON = context.getBindings("js").getMember("jsvariables").asString();
            variableList = objectMapper.readValue(jsVariableJSON, JSVariableList.class);
            variableList.saveVariable(this.montoyaApi);

            String jsLoggerJSON = context.getBindings("js").getMember("jslogger").asString();
            jsLogger = objectMapper.readValue(jsLoggerJSON, JSLogger.class);
            jsLogger.printLogs(this.montoyaApi);

            try{
                context.eval("js", "jsresult.response = JSON.stringify(jsresult.response);");

                Value jsResult = context.getBindings("js").getMember("jsresult");
                String jsResponseJSON = jsResult.getMember("response").as(String.class);
                jsHttpResponse = objectMapper.readValue(jsResponseJSON, JSHttpResponse.class);
                HttpResponse modifiedResponse = jsHttpResponse.getHttpResponse(responseReceived);

                if (this.jScriptorPanel.getSaveLogButton().isSelected()){
                    this.updateLogTable(responseReceived.messageId(), null, null,
                            null, modifiedResponse);
                }

                return modifiedResponse;
            }catch (Exception e){
                this.montoyaApi.logging().logToError(e.getMessage());
                return responseReceived;
            }

        } catch (Exception e) {
            this.montoyaApi.logging().logToError(e.getMessage());
            return responseReceived;
        }
    }

    private Context loadLibrary(Context context) throws IOException {
        ArrayList<Source> listPureLibrary = this.jScriptorPanel.getListPureLibray();

        for (int i = 0; i < listPureLibrary.size(); i++){
            context.eval(listPureLibrary.get(i));
        }

        return context;
    }

    private Map<String, String> optionsNodejs() throws IOException {
        Map<String, String> options = new HashMap<>();

        if (this.jScriptorPanel.getNodejsTableModel().getRowCount() > 0){
            Object value = this.jScriptorPanel.getNodejsTableModel().getValueAt(0, 0);
            String libraryPath = value.toString();


            options.put("js.commonjs-require", "true");
            options.put("js.commonjs-require-cwd", libraryPath);
        }

        return options;
    }

    private void updateLogTable(int requestid, HttpRequest originalRequest, HttpResponse originalResponse,
                                HttpRequest modifiedRequest, HttpResponse modifiedResponse){

        if (originalRequest != null){
            LogEntry logEntry = new LogEntry();
            logEntry.setId(requestid);
            logEntry.setOriginalHttpRequest(originalRequest);
            this.jScriptorPanel.getLogEntryHashMap().put(requestid, logEntry);

        }
        else{
            LogEntry logEntry = this.jScriptorPanel.getLogEntryHashMap().get(requestid);
            if (originalResponse != null){
                logEntry.setOriginalHttpResponse(originalResponse);
                SwingUtilities.invokeLater(() -> {
                    this.jScriptorPanel.getLogTableModel().addRow(new Object[]{
                            requestid, logEntry.getOriginalHttpRequest().method(), logEntry.getOriginalHttpRequest().url(),
                            originalResponse.statusCode(),
                            originalResponse.toString().length()
                    });

                    this.jScriptorPanel.getLogTableModel().fireTableRowsInserted(
                            this.jScriptorPanel.getLogTableModel().getRowCount() - 1,
                            this.jScriptorPanel.getLogTableModel().getRowCount() - 1);
                });

            }
            if (modifiedRequest != null){
                logEntry.setModifiedHttpRequest(modifiedRequest);
            }
            if (modifiedResponse != null){
                logEntry.setModifiedHttpResponse(modifiedResponse);
            }
        }
    }
}

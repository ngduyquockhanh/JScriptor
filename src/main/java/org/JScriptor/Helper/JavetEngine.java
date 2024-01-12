package org.JScriptor.Helper;

import burp.api.montoya.MontoyaApi;
import burp.api.montoya.http.handler.HttpRequestToBeSent;
import burp.api.montoya.http.handler.HttpResponseReceived;
import burp.api.montoya.http.message.requests.HttpRequest;
import burp.api.montoya.http.message.responses.HttpResponse;
import com.caoccao.javet.exceptions.JavetException;
import com.caoccao.javet.interop.NodeRuntime;
import com.caoccao.javet.interop.V8Host;
import com.caoccao.javet.node.modules.NodeModuleModule;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.JScriptor.Logs.LogEntry;
import org.JScriptor.UI.JScriptorPanel;
import org.JScriptor.model.JSHttpRequest;
import org.JScriptor.model.JSHttpResponse;
import org.JScriptor.model.JSLogger;
import org.JScriptor.model.JSVariableList;

import javax.swing.*;
import java.io.File;
import java.io.IOException;

public class JavetEngine {
    private MontoyaApi montoyaApi;
    private JScriptorPanel jScriptorPanel;
    private String pre_script;
    private String post_script;

    public JavetEngine(MontoyaApi montoyaApi, JScriptorPanel jScriptorPanel, String pre_script, String post_script) {
        this.montoyaApi = montoyaApi;
        this.jScriptorPanel = jScriptorPanel;
        this.pre_script = pre_script;
        this.post_script = post_script;
    }

    public HttpRequest handleRequest(HttpRequestToBeSent requestToBeSent){
        try {
            try (NodeRuntime nodeRuntime = loadLibrary(V8Host.getNodeInstance().createV8Runtime())) {

                JSHttpRequest jsHttpRequest = new JSHttpRequest();
                jsHttpRequest.loadValue(requestToBeSent);
                ObjectMapper objectMapper = new ObjectMapper();
                String jsRequestString = objectMapper.writeValueAsString(jsHttpRequest);

                JSVariableList variableList = new JSVariableList();
                variableList.loadValue(this.montoyaApi);
                String jsVariableString = objectMapper.writeValueAsString(variableList);

                JSLogger jsLogger = new JSLogger();
                String jsLoggerString = objectMapper.writeValueAsString(jsLogger);

                nodeRuntime.getGlobalObject().set("jsvariables", jsVariableString);
                nodeRuntime.getGlobalObject().set("jsrequest", jsRequestString);
                nodeRuntime.getGlobalObject().set("jslogger", jsLoggerString);

                nodeRuntime.getExecutor("jsrequest = JSON.parse(jsrequest);").executeVoid();
                nodeRuntime.getExecutor("jsvariables = JSON.parse(jsvariables);").executeVoid();
                nodeRuntime.getExecutor("jslogger = JSON.parse(jslogger);").executeVoid();
                nodeRuntime.getExecutor("var jsresult = {request:null};").executeVoid();
                nodeRuntime.getExecutor(pre_script).executeVoid();

                nodeRuntime.getExecutor("jsvariables = JSON.stringify(jsvariables);").executeVoid();
                nodeRuntime.getExecutor("jslogger = JSON.stringify(jslogger);").executeVoid();

                String jsVariableJSON =  nodeRuntime.getGlobalObject().get("jsvariables").toString();
                variableList = objectMapper.readValue(jsVariableJSON, JSVariableList.class);
                variableList.saveVariable(this.montoyaApi);

                String jsLoggerJSON =  nodeRuntime.getGlobalObject().get("jslogger").toString();
                jsLogger = objectMapper.readValue(jsLoggerJSON, JSLogger.class);
                jsLogger.printLogs(this.montoyaApi);

                try {
                    nodeRuntime.getExecutor("jsresult.request = JSON.stringify(jsresult.request);").executeVoid();
                    nodeRuntime.getExecutor("var jsresult = jsresult.request").executeVoid();
                    String jsRequestJSON = nodeRuntime.getGlobalObject().get("jsresult").toString();

                    jsHttpRequest = objectMapper.readValue(jsRequestJSON, JSHttpRequest.class);

                    HttpRequest modifiedRequest = jsHttpRequest.getHttpRequest(requestToBeSent);

                    if (this.jScriptorPanel.getSaveLogButton().isSelected()){
                        this.updateLogTable(requestToBeSent.messageId(), null, null,
                                modifiedRequest, null);
                    }
                    return modifiedRequest;
                } catch (Exception e) {
                    this.montoyaApi.logging().logToError(e.getMessage());
                    return requestToBeSent;
                }
            }catch (Exception e) {
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
            try (NodeRuntime nodeRuntime = loadLibrary(V8Host.getNodeInstance().createV8Runtime())) {

                JSHttpResponse jsHttpResponse = new JSHttpResponse();
                jsHttpResponse.loadValue(responseReceived);
                ObjectMapper objectMapper = new ObjectMapper();
                String jsResponseString = objectMapper.writeValueAsString(jsHttpResponse);

                JSVariableList variableList = new JSVariableList();
                variableList.loadValue(this.montoyaApi);
                String jsVariableString = objectMapper.writeValueAsString(variableList);

                JSLogger jsLogger = new JSLogger();
                String jsLoggerString = objectMapper.writeValueAsString(jsLogger);

                nodeRuntime.getGlobalObject().set("jsvariables", jsVariableString);
                nodeRuntime.getGlobalObject().set("jsresponse", jsResponseString);
                nodeRuntime.getGlobalObject().set("jslogger", jsLoggerString);

                nodeRuntime.getExecutor("jsresponse = JSON.parse(jsresponse);").executeVoid();
                nodeRuntime.getExecutor("jsvariables = JSON.parse(jsvariables);").executeVoid();
                nodeRuntime.getExecutor("jslogger = JSON.parse(jslogger);").executeVoid();
                nodeRuntime.getExecutor("var jsresult = {response:null};").executeVoid();

                nodeRuntime.getExecutor(post_script).executeVoid();

                nodeRuntime.getExecutor("jsvariables = JSON.stringify(jsvariables);").executeVoid();
                nodeRuntime.getExecutor("jslogger = JSON.stringify(jslogger);").executeVoid();

                String jsVariableJSON = nodeRuntime.getGlobalObject().get("jsvariables").toString();
                variableList = objectMapper.readValue(jsVariableJSON, JSVariableList.class);
                variableList.saveVariable(this.montoyaApi);

                String jsLoggerJSON = nodeRuntime.getGlobalObject().get("jslogger").toString();
                jsLogger = objectMapper.readValue(jsLoggerJSON, JSLogger.class);
                jsLogger.printLogs(this.montoyaApi);


                try {
                    nodeRuntime.getExecutor("jsresult.response = JSON.stringify(jsresult.response);").executeVoid();

                    nodeRuntime.getExecutor("var jsresult = jsresult.response").executeVoid();
                    String jsResponseJSON = nodeRuntime.getGlobalObject().get("jsresult").toString();

                    jsHttpResponse = objectMapper.readValue(jsResponseJSON, JSHttpResponse.class);
                    HttpResponse modifiedResponse = jsHttpResponse.getHttpResponse(responseReceived);

                    if (this.jScriptorPanel.getSaveLogButton().isSelected()){
                        this.updateLogTable(responseReceived.messageId(), null, null,
                                null, modifiedResponse);
                    }

                    return modifiedResponse;

                } catch (Exception e) {
                    this.montoyaApi.logging().logToError(e.getMessage());
                    return responseReceived;
                }
            }

        } catch (Exception e) {
            this.montoyaApi.logging().logToError(e.getMessage());
            return responseReceived;
        }
    }

    private NodeRuntime loadLibrary(NodeRuntime nodeRuntime) throws IOException, JavetException {
        int rowCount = jScriptorPanel.getNodejsTableModel().getRowCount();
        int columnCount = jScriptorPanel.getNodejsTableModel().getColumnCount();

        for (int row = 0; row < rowCount; row++) {
            for (int column = 0; column < columnCount; column++) {
                Object libraryPath = jScriptorPanel.getNodejsTableModel().getValueAt(row, column);
                File libraryFile = new File(libraryPath.toString());

                nodeRuntime.getNodeModule(NodeModuleModule.class).setRequireRootDirectory(libraryFile);
            }
        }
        return nodeRuntime;
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

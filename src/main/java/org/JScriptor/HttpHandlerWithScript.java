package org.JScriptor;

import burp.api.montoya.*;
import burp.api.montoya.core.ToolType;
import burp.api.montoya.http.handler.*;
import burp.api.montoya.http.message.HttpRequestResponse;
import burp.api.montoya.http.message.params.HttpParameterType;
import burp.api.montoya.http.message.requests.HttpRequest;
import burp.api.montoya.http.message.responses.HttpResponse;
import burp.api.montoya.http.message.params.HttpParameter;
import burp.api.montoya.persistence.PersistedList;
import jdk.jfr.StackTrace;
import org.JScriptor.Logs.LogEntry;
import org.JScriptor.UI.JScriptorPanel;
import org.graalvm.polyglot.*;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.io.IOException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class HttpHandlerWithScript implements HttpHandler {
    private MontoyaApi api;
    private JScriptorPanel jScriptorPanel;

    public HttpHandlerWithScript(MontoyaApi api, JScriptorPanel jScriptorPanel) {
        this.api = api;
        this.jScriptorPanel = jScriptorPanel;

    }

    @Override
    public RequestToBeSentAction handleHttpRequestToBeSent(HttpRequestToBeSent requestToBeSent) {
        if (this.jScriptorPanel.getPrescriptTextArea().isModified()){
            this.api.persistence().extensionData().setByteArray("prescript_code", this.jScriptorPanel.getPrescriptTextArea().getContents());
        }
        if (this.jScriptorPanel.getSaveLogButton().isSelected() & (
                this.jScriptorPanel.getRunPrescriptButton().isSelected() ||
                        this.jScriptorPanel.getRunPostscriptButton().isSelected())){
            updateLogTable(requestToBeSent.messageId(), requestToBeSent, null, null, null);
        }
        if (this.jScriptorPanel.getRunPrescriptButton().isSelected()){
            String pre_script = new String(this.jScriptorPanel.getPrescriptTextArea().getContents().getBytes()).trim();
            if (!pre_script.isEmpty()){
                if (this.jScriptorPanel.getPrescriptIsInScopeCheckBox().isSelected()){
                    if (!this.api.scope().isInScope(requestToBeSent.url())){
                        return RequestToBeSentAction.continueWith(requestToBeSent);
                    }
                }
                if (this.jScriptorPanel.getPrescriptIsMatchRegexCheckBox().isSelected()){
                    Pattern pattern = Pattern.compile(new String(this.jScriptorPanel.getPostscriptRegexTextArea().getContents().getBytes()).trim());
                    Matcher matcher = pattern.matcher(requestToBeSent.toString());
                    if (!matcher.find()){
                        return RequestToBeSentAction.continueWith(requestToBeSent);
                    }
                }
                if (this.jScriptorPanel.getPrescriptIsNotModifyRequestFromProxyCheckbox().isSelected()){
                    if (requestToBeSent.toolSource().isFromTool(ToolType.PROXY)){
                        return RequestToBeSentAction.continueWith(requestToBeSent);
                    }
                }
                try {
                    Map<String, String> nodejsOptions = optionsNodejs();
                    Context context = Context.newBuilder("js")
                            .allowExperimentalOptions(true)
                            .allowIO(true)
                            .allowHostAccess(HostAccess.ALL)
                            .allowAllAccess(true)
                            .options(nodejsOptions)
                            .build();

                    context.getBindings("js").putMember("jsrequest", requestToBeSent);
                    context.getBindings("js").putMember("jsvariable", this.api.persistence().extensionData());
                    context.eval("js", "jsresult = {request:null, response:null}");
                    context.eval("js", "window = {}");
                    context = loadLibrary(context);

                    Value jsResult = context.eval("js", pre_script);


                    try{
                        HttpRequest modifiedRequest = jsResult.getMember("request").as(HttpRequest.class);

                        if (this.jScriptorPanel.getSaveLogButton().isSelected()){
                            this.updateLogTable(requestToBeSent.messageId(), null, null,
                                    modifiedRequest, null);
//                            this.api.logging().logToOutput("Modified Request: \n" + modifiedRequest.toString());
                        }

                        return RequestToBeSentAction.continueWith(modifiedRequest);
                    }catch (Exception e){
                        this.api.logging().logToOutput(jsResult + "");
                        return RequestToBeSentAction.continueWith(requestToBeSent);
                    }

                } catch (Exception e) {
                    StackTraceElement[] stackTrace = e.getStackTrace();
                    for (StackTraceElement st: stackTrace){
                        this.api.logging().logToError(st.toString());
                    }
                    this.api.logging().logToError(e.getMessage());

                }
            }
        }
        return RequestToBeSentAction.continueWith(requestToBeSent);
    }

    @Override
    public ResponseReceivedAction handleHttpResponseReceived(HttpResponseReceived responseReceived) {
        if (this.jScriptorPanel.getPostscriptTextArea().isModified()){
            this.api.persistence().extensionData().setByteArray("postscript_code", this.jScriptorPanel.getPostscriptTextArea().getContents());
        }
        if (this.jScriptorPanel.getSaveLogButton().isSelected() & (
                this.jScriptorPanel.getRunPrescriptButton().isSelected() ||
                        this.jScriptorPanel.getRunPostscriptButton().isSelected())){
            updateLogTable(responseReceived.messageId(), null, responseReceived, null, null);
        }
        if (this.jScriptorPanel.getRunPostscriptButton().isSelected()){
            String post_script = new String(this.jScriptorPanel.getPostscriptTextArea().getContents().getBytes()).trim();
            if (!post_script.isEmpty()){

                if (this.jScriptorPanel.getPostscriptIsMatchRegexCheckBox().isSelected()){
                    Pattern pattern = Pattern.compile(new String(this.jScriptorPanel.getPostscriptRegexTextArea().getContents().getBytes()).trim());
                    Matcher matcher = pattern.matcher(responseReceived.toString());
                    if (!matcher.find()){
                        return ResponseReceivedAction.continueWith(responseReceived);
                    }
                }

                if (this.jScriptorPanel.getPostscriptIsNotModifyResponseFromProxyCheckBox().isSelected()){
                    if (responseReceived.toolSource().isFromTool(ToolType.PROXY)){
                        return ResponseReceivedAction.continueWith(responseReceived);
                    }
                }

                try {
                    Map<String, String> nodejsOptions = optionsNodejs();
                    Context context = Context.newBuilder("js")
                            .allowExperimentalOptions(true)
                            .allowIO(true)
                            .allowHostAccess(HostAccess.ALL)
                            .allowAllAccess(true)
                            .options(nodejsOptions)
                            .build();

                    context.getBindings("js").putMember("jsresponse", responseReceived);
                    context.getBindings("js").putMember("jsvariable", this.api.persistence().extensionData());
                    context.eval("js", "jsresult = {request:null, response:null}");
                    context.eval("js", "window = {}");
                    context = loadLibrary(context);

                    Value jsResult = context.eval("js", post_script);

                    try{
                        HttpResponse modifiedResponse = jsResult.getMember("response").as(HttpResponse.class);


                        if (this.jScriptorPanel.getSaveLogButton().isSelected()){
                            this.updateLogTable(responseReceived.messageId(), null, null,
                                    null, modifiedResponse);
                        }

                        return ResponseReceivedAction.continueWith(modifiedResponse);
                    }catch (Exception e){
                        this.api.logging().logToOutput("" + jsResult);
                        return ResponseReceivedAction.continueWith(responseReceived);
                    }

                } catch (Exception e) {
                    this.api.logging().logToError(e.getMessage().replaceAll("\\s+", " ").replaceAll("(?m)\\n{2,}", "\n"));
                }
            }
        }
        return ResponseReceivedAction.continueWith(responseReceived);
    }

    private void updateLogTable(int requestid, HttpRequest originalRequest, HttpResponse originalResponse,
                                HttpRequest modifiedRequest, HttpResponse modifiedResponse){

        if (originalRequest != null){
            LogEntry logEntry = new LogEntry();
            logEntry.setId(requestid);
            logEntry.setOriginalHttpRequest(originalRequest);
            this.jScriptorPanel.getLogEntryHashMap().put(requestid, logEntry);

//            this.api.logging().logToError(requestid + " - " + originalRequest.url());
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

//

            }
            if (modifiedRequest != null){
                logEntry.setModifiedHttpRequest(modifiedRequest);
            }
            if (modifiedResponse != null){
                logEntry.setModifiedHttpResponse(modifiedResponse);
            }
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
}

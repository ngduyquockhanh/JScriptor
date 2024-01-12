package org.JScriptor;

import burp.api.montoya.*;
import burp.api.montoya.core.ByteArray;
import burp.api.montoya.core.ToolType;
import burp.api.montoya.http.handler.*;
import burp.api.montoya.http.message.requests.HttpRequest;
import burp.api.montoya.http.message.responses.HttpResponse;
import org.JScriptor.Helper.GraalJSEngine;
import org.JScriptor.Helper.JavetEngine;
import org.JScriptor.Logs.LogEntry;
import org.JScriptor.UI.JScriptorPanel;
import javax.swing.*;
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
        this.api.persistence().extensionData().setByteArray("prescript_code", ByteArray.byteArray(this.jScriptorPanel.getPrescriptTextArea().getText()));
        if (this.jScriptorPanel.getSaveLogButton().isSelected() & (
                this.jScriptorPanel.getRunPrescriptButton().isSelected() ||
                        this.jScriptorPanel.getRunPostscriptButton().isSelected())){
            updateLogTable(requestToBeSent.messageId(), requestToBeSent, null, null, null);
        }
        if (this.jScriptorPanel.getRunPrescriptButton().isSelected()){
            String pre_script = (this.jScriptorPanel.getPrescriptTextArea().getText()).trim();
            if (!pre_script.isEmpty()){
                if (this.jScriptorPanel.getPrescriptIsInScopeCheckBox().isSelected()){
                    if (!this.api.scope().isInScope(requestToBeSent.url())){
                        return RequestToBeSentAction.continueWith(requestToBeSent);
                    }
                }
                if (this.jScriptorPanel.getPrescriptIsMatchRegexCheckBox().isSelected()){
                    Pattern pattern = Pattern.compile((this.jScriptorPanel.getPostscriptRegexTextArea().getText()).trim());
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
                if (this.jScriptorPanel.getRunWithGraaljs().isSelected()){
                    GraalJSEngine graalJSEngine = new GraalJSEngine(this.api, this.jScriptorPanel, pre_script, null);
                    return RequestToBeSentAction.continueWith(graalJSEngine.handleRequest(requestToBeSent));
                }
                if (this.jScriptorPanel.getRunWithJavet().isSelected()){
                    JavetEngine javetEngine = new JavetEngine(this.api, this.jScriptorPanel, pre_script, null);
                    return RequestToBeSentAction.continueWith(javetEngine.handleRequest(requestToBeSent));

                }


            }
        }
        return RequestToBeSentAction.continueWith(requestToBeSent);
    }

    @Override
    public ResponseReceivedAction handleHttpResponseReceived(HttpResponseReceived responseReceived) {
        this.api.persistence().extensionData().setByteArray("postscript_code", ByteArray.byteArray(this.jScriptorPanel.getPostscriptTextArea().getText()));
        if (this.jScriptorPanel.getSaveLogButton().isSelected() & (
                this.jScriptorPanel.getRunPrescriptButton().isSelected() ||
                        this.jScriptorPanel.getRunPostscriptButton().isSelected())){
            updateLogTable(responseReceived.messageId(), null, responseReceived, null, null);
        }
        if (this.jScriptorPanel.getRunPostscriptButton().isSelected()){
            String post_script = (this.jScriptorPanel.getPostscriptTextArea().getText()).trim();
            if (!post_script.isEmpty()){

                if (this.jScriptorPanel.getPostscriptIsMatchRegexCheckBox().isSelected()){
                    Pattern pattern = Pattern.compile((this.jScriptorPanel.getPostscriptRegexTextArea().getText()).trim());
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

                if (this.jScriptorPanel.getRunWithGraaljs().isSelected()){
                    GraalJSEngine graalJSEngine = new GraalJSEngine(this.api, this.jScriptorPanel, null, post_script);
                    return ResponseReceivedAction.continueWith(graalJSEngine.handleResponse(responseReceived));
                }
                if (this.jScriptorPanel.getRunWithJavet().isSelected()){
                    JavetEngine javetEngine = new JavetEngine(this.api, this.jScriptorPanel, null, post_script);
                    return ResponseReceivedAction.continueWith(javetEngine.handleResponse(responseReceived));

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

}

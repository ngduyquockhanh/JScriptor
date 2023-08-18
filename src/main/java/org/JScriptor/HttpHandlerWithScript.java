package org.JScriptor;

import burp.api.montoya.*;
import burp.api.montoya.http.handler.*;
import burp.api.montoya.http.message.requests.HttpRequest;
import burp.api.montoya.http.message.responses.HttpResponse;
import org.graalvm.polyglot.*;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class HttpHandlerWithScript implements HttpHandler {
    private MontoyaApi api;
    private JTextArea prescript, postscript;
    private JToggleButton runPrescript, runPostscript;
    private DefaultTableModel tableModel;
    private JTextArea regextPreTextArea, regexPostTextArea;
    private JCheckBox isScopePreButton, isRegexPreButton, isRegexPostButton;
    private Context context = Context.newBuilder("js").allowHostAccess(HostAccess.ALL).build();
    public HttpHandlerWithScript(MontoyaApi api, JTextArea prescript, JTextArea postscript, JToggleButton runPrescript,
                                 JToggleButton runPostscript, DefaultTableModel tableModel,
                                 JCheckBox isScopePreButton, JCheckBox isRegexPreButton, JTextArea regextPreTextArea,
                                 JCheckBox isRegexPostButton, JTextArea regexPostTextArea) {
        this.api = api;
        this.prescript = prescript;
        this.postscript = postscript;
        this.runPrescript = runPrescript;
        this.runPostscript = runPostscript;
        this.tableModel = tableModel;
        this.isRegexPreButton = isRegexPreButton;
        this.isScopePreButton = isScopePreButton;
        this.regextPreTextArea = regextPreTextArea;
        this.isRegexPostButton = isRegexPostButton;
        this.regexPostTextArea = regexPostTextArea;
        this.context.eval("js", "jsenv = {}");
    }

    @Override
    public RequestToBeSentAction handleHttpRequestToBeSent(HttpRequestToBeSent requestToBeSent) {
        if (this.runPrescript.isSelected()){
            String pre_script = this.prescript.getText().trim();
            if (!pre_script.isEmpty()){
                if (this.isScopePreButton.isSelected()){
                    if (!this.api.scope().isInScope(requestToBeSent.url())){
                        return RequestToBeSentAction.continueWith(requestToBeSent);
                    }
                }
                if (this.isRegexPreButton.isSelected()){
                    Pattern pattern = Pattern.compile(this.regextPreTextArea.getText().trim());
                    Matcher matcher = pattern.matcher(requestToBeSent.toString());
                    if (!matcher.find()){
                        return RequestToBeSentAction.continueWith(requestToBeSent);
                    }
                }
                try {
                    this.context.getBindings("js").putMember("jsrequest", requestToBeSent);
                    context = loadLibrary(context);
                    Value jsResult = context.eval("js", pre_script);

                    try{
                        HttpRequest modifiedRequest = jsResult.as(HttpRequest.class);
                        this.api.logging().logToOutput("Modified Request: \n" + modifiedRequest.toString());
                        return RequestToBeSentAction.continueWith(modifiedRequest);
                    }catch (Exception e){
                        this.api.logging().logToOutput("" + jsResult);
                        return RequestToBeSentAction.continueWith(requestToBeSent);
                    }

                } catch (Exception e) {
                    this.api.logging().logToError(e.getMessage());
                }
            }
        }
        return RequestToBeSentAction.continueWith(requestToBeSent);
    }

    @Override
    public ResponseReceivedAction handleHttpResponseReceived(HttpResponseReceived responseReceived) {
        if (this.runPostscript.isSelected()){
            String post_script = this.postscript.getText().trim();
            if (!post_script.isEmpty()){
                if (this.isRegexPostButton.isSelected()){
                    Pattern pattern = Pattern.compile(this.regexPostTextArea.getText().trim());
                    Matcher matcher = pattern.matcher(responseReceived.toString());
                    if (!matcher.find()){
                        return ResponseReceivedAction.continueWith(responseReceived);
                    }
                }

                try {
                    this.context.getBindings("js").putMember("jsresponse", responseReceived);
                    context = loadLibrary(context);
                    Value jsResult = context.eval("js", post_script);

                    try{
                        HttpResponse modifiedResponse = jsResult.as(HttpResponse.class);
                        this.api.logging().logToOutput("Modified response: \n" + modifiedResponse.toString());
                        return ResponseReceivedAction.continueWith(modifiedResponse);
                    }catch (Exception e){
                        this.api.logging().logToOutput("" + jsResult);
                        return ResponseReceivedAction.continueWith(responseReceived);
                    }

                } catch (Exception e) {
                    this.api.logging().logToError(e.getMessage());
                }
            }
        }
        return ResponseReceivedAction.continueWith(responseReceived);
    }

    private Context loadLibrary(Context context) throws IOException {
        int rowCount = this.tableModel.getRowCount();
        int columnCount = this.tableModel.getColumnCount();

        for (int row = 0; row < rowCount; row++) {
            for (int column = 0; column < columnCount; column++) {
                Object value = tableModel.getValueAt(row, column);
                String libraryPath = value.toString();
                Source librarySource = Source.newBuilder("js", new java.io.File(libraryPath)).build();
                String libraryContent = librarySource.getCharacters().toString();
                context.eval("js", libraryContent);
            }
        }

        return context;
    }
}

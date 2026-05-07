package org.JScriptor;

import burp.api.montoya.MontoyaApi;
import burp.api.montoya.core.ByteArray;
import burp.api.montoya.http.message.requests.HttpRequest;
import burp.api.montoya.proxy.http.*;
import org.JScriptor.Helper.GraalJSEngine;
import org.JScriptor.Helper.JavetEngine;
import org.JScriptor.UI.JScriptorPanel;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ProxyResponseHandlerWithScript implements ProxyResponseHandler {
    private MontoyaApi api;
    private JScriptorPanel jScriptorPanel;

    public ProxyResponseHandlerWithScript(MontoyaApi api, JScriptorPanel jScriptorPanel) {
        this.api = api;
        this.jScriptorPanel = jScriptorPanel;

    }


    @Override
    public ProxyResponseReceivedAction handleResponseReceived(InterceptedResponse interceptedResponse) {
        return null;
    }

    @Override
    public ProxyResponseToBeSentAction handleResponseToBeSent(InterceptedResponse interceptedResponse) {
        this.api.persistence().extensionData().setByteArray("postburp_code", ByteArray.byteArray(this.jScriptorPanel.getPrescriptTextArea().getText()));
        if (this.jScriptorPanel.getRunPostBurpButton().isSelected()){
            String post_burp = (this.jScriptorPanel.getPostBurpTextArea().getText()).trim();
            if (!post_burp.isEmpty()){
                if (this.jScriptorPanel.getPostBurpIsMatchRegexCheckBox().isSelected()) {
                    Pattern pattern = Pattern.compile((this.jScriptorPanel.getPostBurpRegexTextArea().getText()).trim());
                    Matcher matcher = pattern.matcher(interceptedResponse.toString());
                    if (!matcher.find()) {
                        return ProxyResponseToBeSentAction.continueWith(interceptedResponse);
                    }
                }
                if (this.jScriptorPanel.getRunWithGraaljs().isSelected()){
                    GraalJSEngine graalJSEngine = new GraalJSEngine(this.api, this.jScriptorPanel, null, null, null, post_burp);
                    return ProxyResponseToBeSentAction.continueWith(graalJSEngine.handleProxyResponse(interceptedResponse));
                }
                if (this.jScriptorPanel.getRunWithJavet().isSelected()){
                    JavetEngine javetEngine = new JavetEngine(this.api, this.jScriptorPanel, null, null, null, post_burp);
                    return ProxyResponseToBeSentAction.continueWith(javetEngine.handleProxyResponse(interceptedResponse));

                }


            }
        }
        return ProxyResponseToBeSentAction.continueWith(interceptedResponse);
    }
}

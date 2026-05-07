package org.JScriptor;

import burp.api.montoya.MontoyaApi;
import burp.api.montoya.core.ByteArray;
import burp.api.montoya.core.ToolType;
import burp.api.montoya.http.handler.*;
import burp.api.montoya.http.message.requests.HttpRequest;
import burp.api.montoya.http.message.responses.HttpResponse;
import burp.api.montoya.proxy.http.InterceptedRequest;
import burp.api.montoya.proxy.http.ProxyRequestHandler;
import burp.api.montoya.proxy.http.ProxyRequestReceivedAction;
import burp.api.montoya.proxy.http.ProxyRequestToBeSentAction;
import org.JScriptor.Helper.GraalJSEngine;
import org.JScriptor.Helper.JavetEngine;
import org.JScriptor.Logs.LogEntry;
import org.JScriptor.UI.JScriptorPanel;

import javax.swing.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ProxyRequestHandlerWithScript implements ProxyRequestHandler {
    private MontoyaApi api;
    private JScriptorPanel jScriptorPanel;

    public ProxyRequestHandlerWithScript(MontoyaApi api, JScriptorPanel jScriptorPanel) {
        this.api = api;
        this.jScriptorPanel = jScriptorPanel;

    }

    @Override
    public ProxyRequestReceivedAction handleRequestReceived(InterceptedRequest interceptedRequest) {
        this.api.persistence().extensionData().setByteArray("preburp_code", ByteArray.byteArray(this.jScriptorPanel.getPrescriptTextArea().getText()));
        if (this.jScriptorPanel.getRunPreBurpButton().isSelected()){
            String pre_burp = (this.jScriptorPanel.getPreBurpTextArea().getText()).trim();
            if (!pre_burp.isEmpty()){
                if (this.jScriptorPanel.getPreBurpIsInScopeCheckBox().isSelected()){
                    if (!this.api.scope().isInScope(interceptedRequest.url())){
                        return ProxyRequestReceivedAction.continueWith(interceptedRequest);
                    }
                }
                if (this.jScriptorPanel.getPreBurpIsMatchRegexCheckBox().isSelected()) {
                    Pattern pattern = Pattern.compile((this.jScriptorPanel.getPreBurpRegexTextArea().getText()).trim());
                    Matcher matcher = pattern.matcher(interceptedRequest.toString());
                    if (!matcher.find()) {
                        return ProxyRequestReceivedAction.continueWith(interceptedRequest);
                    }
                }
                if (this.jScriptorPanel.getRunWithGraaljs().isSelected()){
                    GraalJSEngine graalJSEngine = new GraalJSEngine(this.api, this.jScriptorPanel, null, null, pre_burp, null);
                    return ProxyRequestReceivedAction.continueWith(graalJSEngine.handleProxyRequest(interceptedRequest));
                }
                if (this.jScriptorPanel.getRunWithJavet().isSelected()){
                    JavetEngine javetEngine = new JavetEngine(this.api, this.jScriptorPanel, null, null, pre_burp, null);
                    return ProxyRequestReceivedAction.continueWith(javetEngine.handleProxyRequest(interceptedRequest));

                }


            }
        }
        return ProxyRequestReceivedAction.continueWith(interceptedRequest);
    }

    @Override
    public ProxyRequestToBeSentAction handleRequestToBeSent(InterceptedRequest interceptedRequest) {
        return ProxyRequestToBeSentAction.continueWith(interceptedRequest);
    }


}
